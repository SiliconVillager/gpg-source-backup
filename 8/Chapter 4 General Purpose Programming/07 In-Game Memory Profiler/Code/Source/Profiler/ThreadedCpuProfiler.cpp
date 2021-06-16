//-------------------------------------------------------------------
// In Game Memory Profiler
// by Ricky Lung Man Tat (mtlung@gmail.com)


#include "ThreadedCpuProfiler.h"
#include "PlatformInclude.h"
#include <assert.h>
#include <iomanip>
#include <sstream>
#include <vector>

namespace Profiler {

namespace {

//! A structure that group all global varaibles into a thread local storage.
struct TlsStruct
{
	TlsStruct(const char* name) :
		recurseCount(0),
		// We want every thread have it's own copy of thread name, therefore strdup() is used
		mCurrentNode(nullptr), threadName(::strdup(name))
	{}

	~TlsStruct() {
		::free((void*)threadName);
	}

	sal_notnull ThreadedCpuProfilerNode* currentNode()
	{
		// If node is null, means a new thread is started
		if(!mCurrentNode) {
			CallstackNode* rootNode = ThreadedCpuProfiler::singleton().getRootNode();
			assert(rootNode);
			recurseCount++;
			mCurrentNode = static_cast<ThreadedCpuProfilerNode*>(
				rootNode->getChildByName(threadName)
			);
			recurseCount--;
		}

		return mCurrentNode;
	}

	ThreadedCpuProfilerNode* setCurrentNode(CallstackNode* node) {
		return mCurrentNode = static_cast<ThreadedCpuProfilerNode*>(node);
	}

	size_t recurseCount;
	const char* threadName;

protected:
	ThreadedCpuProfilerNode* mCurrentNode;
};	// TlsStruct

DWORD gTlsIndex = 0;

TlsStruct* getTlsStruct()
{
	assert(gTlsIndex != 0);
	return reinterpret_cast<TlsStruct*>(TlsGetValue(gTlsIndex));
}

}	// namespace

struct ThreadedCpuProfiler::TlsList : public std::vector<TlsStruct*>
{
	~TlsList()
	{
		for(iterator i=begin(); i!=end(); ++i)
			delete (*i);
	}
	Mutex mutex;
};	// TlsList

ThreadedCpuProfilerNode::ThreadedCpuProfilerNode(const char name[], CallstackNode* parent)
	: CallstackNode(name, parent), callCount(0), inclusiveTime(uint64_t(0))
{}

void ThreadedCpuProfilerNode::begin()
{
	// Start the timer for the first call, ignore all later recursive call
	if(recursionCount == 0)
		timer.reset();

	++callCount;
}

void ThreadedCpuProfilerNode::end()
{
	if(recursionCount == 0)
		inclusiveTime += timer.get();
}

CallstackNode* ThreadedCpuProfilerNode::createNode(const char name[], CallstackNode* parent)
{
	return new ThreadedCpuProfilerNode(name, parent);
}

void ThreadedCpuProfilerNode::reset()
{
	ThreadedCpuProfilerNode* n1, *n2;
	{	// Race with ThreadedCpuProfiler::begin(), ThreadedCpuProfiler::end()
		ScopeRecursiveLock lock(mutex);
		callCount = 0;
		inclusiveTime.set(uint64_t(0));
		n1 = static_cast<ThreadedCpuProfilerNode*>(firstChild);
		n2 = static_cast<ThreadedCpuProfilerNode*>(sibling);
	}

	if(n1) n1->reset();
	if(n2) n2->reset();
}

float ThreadedCpuProfilerNode::selfTime() const
{
	// Loop and sum for all direct children
	TimeInterval sum = 0;
	const ThreadedCpuProfilerNode* n = static_cast<ThreadedCpuProfilerNode*>(firstChild);
	while(n) {
		ScopeRecursiveLock lock(n->mutex);
		sum += n->inclusiveTime;
		n = static_cast<ThreadedCpuProfilerNode*>(n->sibling);
	}

	return float((inclusiveTime - sum).asSecond());
}

ThreadedCpuProfiler::ThreadedCpuProfiler()
{
	mTlsList = new TlsList();
	gTlsIndex = TlsAlloc();
	setRootNode(new ThreadedCpuProfilerNode("root"));

	onThreadAttach("MAIN THREAD");
}

ThreadedCpuProfiler::~ThreadedCpuProfiler()
{
	assert(gTlsIndex != 0);
	TlsSetValue(gTlsIndex, nullptr);
	TlsFree(gTlsIndex);
	gTlsIndex = 0;

	// We assume that all thread will be stopped before ThreadedCpuProfiler is destroyed
	delete mTlsList;
}

ThreadedCpuProfiler& ThreadedCpuProfiler::singleton()
{
	static ThreadedCpuProfiler instance;
	return instance;
}

void ThreadedCpuProfiler::begin(const char name[])
{
	if(!enable)
		return;

	TlsStruct* tls = getTlsStruct();
	if(!tls)
		tls = reinterpret_cast<TlsStruct*>(onThreadAttach());
	ThreadedCpuProfilerNode* node = tls->currentNode();

	// Race with ThreadedCpuProfiler::reset(), ThreadedCpuProfiler::defaultReport()
	ScopeRecursiveLock lock(node->mutex);

	if(name != node->name) {
		ThreadedCpuProfilerNode* tmp = static_cast<ThreadedCpuProfilerNode*>(node->getChildByName(name));
		lock.swapMutex(tmp->mutex);
		node = tmp;

		// Only alter the current node, if the child node is not recursing
		if(node->recursionCount == 0)
			tls->setCurrentNode(node);
	}

	node->begin();
	node->recursionCount++;
}

void ThreadedCpuProfiler::end()
{
	if(!enable)
		return;

	TlsStruct* tls = getTlsStruct();
	ThreadedCpuProfilerNode* node = tls->currentNode();

	// Race with ThreadedCpuProfiler::reset(), ThreadedCpuProfiler::defaultReport()
	ScopeRecursiveLock lock(node->mutex);

	node->recursionCount--;
	node->end();

	// Only back to the parent when the current node is not inside a recursive function
	if(node->recursionCount == 0)
		tls->setCurrentNode(node->parent);
}

void ThreadedCpuProfiler::setRootNode(CallstackNode* root)
{
	CallstackProfiler::setRootNode(root);
	reset();
}

void ThreadedCpuProfiler::nextFrame()
{
	assert(mCurrentNode == mRootNode && "Do not call nextFrame() inside a profiling code block");
	++frameCount;
	timeSinceLastReset = timer.get();
}

void ThreadedCpuProfiler::reset()
{
	if(!mRootNode)
		return;

	assert(mCurrentNode == mRootNode && "Do not call reset() inside a profiling code block");
	frameCount = 0;
	static_cast<ThreadedCpuProfilerNode*>(mRootNode)->reset();
	timeSinceLastReset = TimeInterval::getMax();
	timer.reset();
}

float ThreadedCpuProfiler::fps() const
{
	return float(frameCount / timeSinceLastReset.asSecond());
}

std::string ThreadedCpuProfiler::defaultReport(size_t nameLength, float skipMargin) const
{
	using namespace std;
	ostringstream ss;

	ss << "FPS: " << static_cast<size_t>(fps()) << endl;

	const std::streamsize percentWidth = 8;
	const std::streamsize floatWidth = 12;

	ss.flags(ios_base::left);
	ss	<< setw(std::streamsize(nameLength)) << "Name"
		<< setw(percentWidth)	<< "TT/F %"
		<< setw(percentWidth)	<< "ST/F %"
		<< setw(floatWidth)		<< "TT/C"
		<< setw(floatWidth)		<< "ST/C"
		<< setw(floatWidth)		<< "C/F"
		<< endl;

	ThreadedCpuProfilerNode* n = static_cast<ThreadedCpuProfilerNode*>(mRootNode);

	do
	{
		// Race with ThreadedCpuProfiler::begin() and ThreadedCpuProfiler::end()
		ScopeRecursiveLock lock(n->mutex);

		float percent = 100 * fps();
		float inclusiveTime = float(n->inclusiveTime.asSecond());

		// Skip node that have total time less than 1%
		if(inclusiveTime / frameCount * percent >= skipMargin)
		{
			float selfTime = n->selfTime();

			std::streamsize callDepth = std::streamsize(n->callDepth());
			ss	<< setw(callDepth) << ""
				<< setw(std::streamsize(nameLength - callDepth)) << n->name
				<< setprecision(3)
				<< setw(percentWidth)	<< (inclusiveTime / frameCount * percent)
				<< setw(percentWidth)	<< (selfTime / frameCount * percent)
				<< setw(floatWidth)		<< (n->callCount == 0 ? 0 : inclusiveTime / n->callCount)
				<< setw(floatWidth)		<< (n->callCount == 0 ? 0 : selfTime / n->callCount)
				<< setprecision(2)
				<< setw(floatWidth-2)	<< (float(n->callCount) / frameCount)
				<< endl;
		}

		n = static_cast<ThreadedCpuProfilerNode*>(CallstackNode::traverse(n));
	} while(n != nullptr);

	return ss.str();
}

void* ThreadedCpuProfiler::onThreadAttach(const char* threadName)
{
	TlsStruct* tls = new TlsStruct(threadName);

	{
		ScopeLock lock(mTlsList->mutex);
		mTlsList->push_back(tls);
	}

	TlsSetValue(gTlsIndex, tls);

	return tls;
}

}	// namespace Profiler
