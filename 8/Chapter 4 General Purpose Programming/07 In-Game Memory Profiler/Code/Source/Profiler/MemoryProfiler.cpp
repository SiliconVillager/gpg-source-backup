//-------------------------------------------------------------------
// In Game Memory Profiler
// by Ricky Lung Man Tat (mtlung@gmail.com)


#include "MemoryProfiler.h"

#if defined(_MSC_VER)

#include "Atomic.h"
#include "FunctionPatcher.inc"
#include <assert.h>
#include <iomanip>
#include <sstream>
#include <tchar.h>	// For _T()
#include <vector>

/*!	When working with run-time analysis tools like Intel Parallel Studio, the use of dll main
	make cause false positive, therefore we hace a macro to turn on and off the dll main.
	Use dll main has the benift of capturing more memory allocation.
 */
#define USE_DLL_MAIN 1

using namespace Profiler;

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

	sal_notnull MemoryProfilerNode* currentNode()
	{
		// If node is null, means a new thread is started
		if(!mCurrentNode) {
			CallstackNode* rootNode = MemoryProfiler::singleton().getRootNode();
			assert(rootNode);
			recurseCount++;
			mCurrentNode = static_cast<MemoryProfilerNode*>(
				rootNode->getChildByName(threadName)
			);
			recurseCount--;
		}

		return mCurrentNode;
	}

	MemoryProfilerNode* setCurrentNode(CallstackNode* node) {
		return mCurrentNode = static_cast<MemoryProfilerNode*>(node);
	}

	size_t recurseCount;
	const char* threadName;

protected:
	MemoryProfilerNode* mCurrentNode;
};	// TlsStruct

typedef LPVOID (WINAPI *MyHeapAlloc)(HANDLE, DWORD, SIZE_T);
typedef LPVOID (WINAPI *MyHeapReAlloc)(HANDLE, DWORD, LPVOID, SIZE_T);
typedef LPVOID (WINAPI *MyHeapFree)(HANDLE, DWORD, LPVOID);

LPVOID WINAPI myHeapAlloc(HANDLE, DWORD, SIZE_T);
LPVOID WINAPI myHeapReAlloc(HANDLE, DWORD, LPVOID, SIZE_T);
LPVOID WINAPI myHeapFree(HANDLE, DWORD, LPVOID);

FunctionPatcher functionPatcher;
MyHeapAlloc orgHeapAlloc;
MyHeapReAlloc orgHeapReAlloc;
MyHeapFree orgHeapFree;

DWORD gTlsIndex = 0;

/*!	A global mutex to protect the footer information of each allocation.
	NOTE: Intel parallel studio not able to detect the creation of a static mutex,
	therefore we need to delay it's construction until MemoryProfiler constructor.
 */
Mutex* gFooterMutex = nullptr;

TlsStruct* getTlsStruct()
{
	assert(gTlsIndex != 0);
	return reinterpret_cast<TlsStruct*>(TlsGetValue(gTlsIndex));
}

/*!	A footer struct that insert to every patched memory allocation,
	aim to indicate which call stack node this allocation belongs to.
 */
struct MyMemFooter
{
	/*! The node pointer is placed in-between the 2 fourcc values,
		ensure maximum protected as possible.
	 */
	uint32_t fourCC1;
	MemoryProfilerNode* node;
	uint32_t fourCC2;

	/*!	Magic number for HeapFree verification.
		The node pointer is not used if any of the fourcc is invalid.
		It's kind of dirty, but there is no other way to indicate a pointer
		is allocated by original HeapAlloc or our patched version.
	 */
	static const uint32_t cFourCC1 = 123456789;
	static const uint32_t cFourCC2 = 987654321;
};	// MyMemFooter

/*!	nBytes does not account for the extra footer size
 */
void* commonAlloc(sal_in TlsStruct* tls, sal_in void* p, size_t nBytes)
{
	assert(tls && p && "caller of commonAlloc should ensure tls and p is valid");

	MemoryProfilerNode* node = tls->currentNode();

	{	// Race with MemoryProfiler::reset(), MemoryProfiler::defaultReport() and commonDealloc()
		ScopeRecursiveLock lock(node->mMutex);
		node->exclusiveCount++;
		node->countSinceLastReset++;
		node->exclusiveBytes += nBytes;
	}

	{	ScopeLock lock(gFooterMutex);
		MyMemFooter* footer = reinterpret_cast<MyMemFooter*>(nBytes + (char*)p);
		footer->node = node;
		footer->fourCC1 = MyMemFooter::cFourCC1;
		footer->fourCC2 = MyMemFooter::cFourCC2;
	}

	return p;
}

void commonDealloc(__in HANDLE hHeap, __in DWORD dwFlags, __deref LPVOID lpMem)
{
	if(!lpMem)
		return;

	size_t size = HeapSize(hHeap, dwFlags, lpMem) - sizeof(MyMemFooter);

	ScopeLock lock1(*gFooterMutex);
	MyMemFooter* footer = (MyMemFooter*)(((char*)lpMem) + size);

	if(footer->fourCC1 == MyMemFooter::cFourCC1 && footer->fourCC2 == MyMemFooter::cFourCC2)
	{
		// Reset the magic number so that commonDealloc() will not applied more than once.
		footer->fourCC1 = footer->fourCC2 = 0;

		MemoryProfilerNode* node = reinterpret_cast<MemoryProfilerNode*>(footer->node);
		assert(node);

		// Race with MemoryProfiler::defaultReport() and all other
		// operations if lpMem is allocated from thread A but now free in thread B (this thread)
		{	ScopeUnlock unlock(gFooterMutex);	// Prevent lock hierarchy.
			ScopeRecursiveLock lock2(node->mMutex);

			node->exclusiveCount--;
			node->exclusiveBytes -= size;
		}
	}
}

LPVOID WINAPI myHeapAlloc(__in HANDLE hHeap, __in DWORD dwFlags, __in SIZE_T dwBytes)
{
	TlsStruct* tls = getTlsStruct();

	// HeapAlloc will invoke itself recursivly, so we need a recursion counter
	if(!tls || tls->recurseCount > 0)
		return orgHeapAlloc(hHeap, dwFlags, dwBytes);

	tls->recurseCount++;
	void* p = orgHeapAlloc(hHeap, dwFlags, dwBytes + sizeof(MyMemFooter));
	tls->recurseCount--;

	return commonAlloc(tls, p, dwBytes);
}

LPVOID WINAPI myHeapReAlloc(__in HANDLE hHeap, __in DWORD dwFlags, __deref LPVOID lpMem, __in SIZE_T dwBytes)
{
	TlsStruct* tls = getTlsStruct();

	if(!tls || tls->recurseCount > 0 || lpMem == nullptr)
		return orgHeapReAlloc(hHeap, dwFlags, lpMem, dwBytes);

	// Remove the statistics for the previous allocation first.
	commonDealloc(hHeap, dwFlags, lpMem);

	if(dwBytes == 0)
		return orgHeapReAlloc(hHeap, dwFlags, lpMem, dwBytes);

	// On VC 2005, orgHeapReAlloc() will not invoke HeapAlloc() and HeapFree(),
	// but it does on VC 2008
	tls->recurseCount++;
	void* p = orgHeapReAlloc(hHeap, dwFlags, lpMem, dwBytes + sizeof(MyMemFooter));
	tls->recurseCount--;

	return commonAlloc(tls, p, dwBytes);
}

LPVOID WINAPI myHeapFree(__in HANDLE hHeap, __in DWORD dwFlags, __deref LPVOID lpMem)
{
	commonDealloc(hHeap, dwFlags, lpMem);
	return orgHeapFree(hHeap, dwFlags, lpMem);
}

}	// namespace

namespace Profiler {

struct MemoryProfiler::TlsList : public std::vector<TlsStruct*>
{
	~TlsList() {
		for(iterator i=begin(); i!=end(); ++i)
			delete (*i);
	}
	Mutex mutex;
};	// TlsList

MemoryProfilerNode::MemoryProfilerNode(const char name[], CallstackNode* parent)
	:
	CallstackNode(name, parent), callCount(0),
	exclusiveCount(0), exclusiveBytes(0), countSinceLastReset(0),
	mIsMutexOwner(false), mMutex(nullptr)
{
}

MemoryProfilerNode::~MemoryProfilerNode()
{
	if(mIsMutexOwner)
		delete mMutex;
}

void MemoryProfilerNode::begin()
{
	assert(mMutex->isLocked());
	++callCount;
}

void MemoryProfilerNode::reset()
{
	MemoryProfilerNode* n1, *n2;
	{	// Race with MemoryProfiler::begin(), MemoryProfiler::end(), commonAlloc() and commonDealloc()
		ScopeRecursiveLock lock(mMutex);
		callCount = 0;
		countSinceLastReset = 0;
		n1 = static_cast<MemoryProfilerNode*>(firstChild);
		n2 = static_cast<MemoryProfilerNode*>(sibling);
	}

	if(n1) n1->reset();
	if(n2) n2->reset();
}

size_t MemoryProfilerNode::inclusiveCount() const
{
	size_t total = exclusiveCount;
	const MemoryProfilerNode* n = static_cast<MemoryProfilerNode*>(firstChild);
	if(!n)
		return total;

	do {
		ScopeRecursiveLock lock(n->mMutex);
		total += n->inclusiveCount();
		n = static_cast<MemoryProfilerNode*>(n->sibling);
	} while(n);

	return total;
}

size_t MemoryProfilerNode::inclusiveBytes() const
{
	size_t total = exclusiveBytes;
	const MemoryProfilerNode* n = static_cast<MemoryProfilerNode*>(firstChild);
	if(!n)
		return total;

	do {
		ScopeRecursiveLock lock(n->mMutex);
		total += n->inclusiveBytes();
		n = static_cast<MemoryProfilerNode*>(n->sibling);
	} while(n);

	return total;
}

MemoryProfiler::MemoryProfiler()
{
	mTlsList = new TlsList();
	gTlsIndex = TlsAlloc();

	setRootNode(new MemoryProfilerNode("root"));

	setEnable(enable());

	// The locking of gFooterMutex should be a very short period, so use a spin lock.
	gFooterMutex = new Mutex(200);

#if !USE_DLL_MAIN
	onThreadAttach("MAIN THREAD");
#endif	// !USE_DLL_MAIN
}

MemoryProfiler::~MemoryProfiler()
{
	setEnable(false);

	// Delete all profiler node
	CallstackProfiler::setRootNode(nullptr);

	assert(gTlsIndex != 0);
	TlsSetValue(gTlsIndex, nullptr);
	TlsFree(gTlsIndex);
	gTlsIndex = 0;

	delete gFooterMutex;
	delete mTlsList;
}

void MemoryProfiler::setRootNode(CallstackNode* root)
{
	CallstackProfiler::setRootNode(root);
	reset();
}

void MemoryProfiler::begin(const char name[])
{
	if(!enable())
		return;

	TlsStruct* tls = getTlsStruct();
	if(!tls)
		tls = reinterpret_cast<TlsStruct*>(onThreadAttach());
	MemoryProfilerNode* node = tls->currentNode();

	// Race with MemoryProfiler::reset(), MemoryProfiler::defaultReport() and commonDealloc()
	// Yes, not race with commonAlloc(), because only the allocation on the same thread will
	// access this node.
	ScopeRecursiveLock lock(node->mMutex);

	if(name != node->name) {
		// NOTE: We have changed the node, but there is no need to lock the
		// mutex for the new node, since both mutex must be just the same instance.
		tls->recurseCount++;
		node = static_cast<MemoryProfilerNode*>(node->getChildByName(name));
		tls->recurseCount--;

		// Only alter the current node, if the child node is not recursing
		if(node->recursionCount == 0)
			tls->setCurrentNode(node);
	}

	node->begin();
	node->recursionCount++;
}

void MemoryProfiler::end()
{
	if(!enable())
		return;

	TlsStruct* tls = getTlsStruct();

	// The code in MemoryProfiler::begin() may be skipped because of !enable()
	// therefore we need to detect and create tls for MemoryProfiler::end() also.
	if(!tls)
		tls = reinterpret_cast<TlsStruct*>(onThreadAttach());

	MemoryProfilerNode* node = tls->currentNode();

	// Race with MemoryProfiler::reset(), MemoryProfiler::defaultReport() and commonDealloc()
	ScopeRecursiveLock lock(node->mMutex);

	node->recursionCount--;
	node->end();

	// Only back to the parent when the current node is not inside a recursive function
	if(node->recursionCount == 0)
		tls->setCurrentNode(node->parent);
}

void MemoryProfiler::nextFrame()
{
	if(!enable())
		return;

	assert(getTlsStruct()->currentNode()->parent == mRootNode
		&& "Do not call nextFrame() inside a profiling code block");
	++frameCount;
}

void MemoryProfiler::reset()
{
	if(!mRootNode || !enable())
		return;

	assert(!getTlsStruct() || getTlsStruct()->currentNode()->parent == mRootNode
		&& "Do not call reset() inside a profiling code block");
	frameCount = 0;
	static_cast<MemoryProfilerNode*>(mRootNode)->reset();
}

std::string MemoryProfiler::defaultReport(size_t nameLength, size_t skipMargin) const
{
	using namespace std;
	ostringstream ss;

	const size_t countWidth = 9;
	const size_t bytesWidth = 12;

	ss.flags(ios_base::left);
	ss	<< setw(nameLength)		<< "Name" << setiosflags(ios::right)
		<< setw(countWidth)		<< "TCount"
		<< setw(countWidth)		<< "SCount"
		<< setw(bytesWidth)		<< "TkBytes"
		<< setw(bytesWidth)		<< "SkBytes"
		<< setw(countWidth)		<< "SCount/F"
		<< setw(countWidth-2)	<< "Call/F"
		<< endl;

	MemoryProfilerNode* n = static_cast<MemoryProfilerNode*>(mRootNode);

	while(n)
	{	// NOTE: The following std stream operation may trigger HeapAlloc,
		// there we need to use recursive mutex here.

		// Race with MemoryProfiler::begin(), MemoryProfiler::end(), commonAlloc() and commonDealloc()
		ScopeRecursiveLock lock(n->mMutex);

		// Skip node that have no allocation at all
		if(n->callDepth() == 0 || n->exclusiveCount != 0 || n->countSinceLastReset != 0)
		{
			size_t callDepth = n->callDepth();
			const char* name = n->name;
			size_t iCount = n->inclusiveCount();
			size_t eCount = n->exclusiveCount;
			float iBytes = float(n->inclusiveBytes()) / 1024;
			float eBytes = float(n->exclusiveBytes) / 1024;
			float countSinceLastReset = float(n->countSinceLastReset) / frameCount;
			float callCount = float(n->callCount) / frameCount;

			{	// The string stream will make allocations, therefore we need to unlock the mutex
				// to prevent dead lock.
				ScopeRecursiveUnlock unlock(n->mMutex);
				ss.flags(ios_base::left);
				ss	<< setw(callDepth) << ""
					<< setw(nameLength - callDepth) << name
					<< setiosflags(ios::right)// << setprecision(3)
					<< setw(countWidth)		<< iCount
					<< setw(countWidth)		<< eCount
					<< setw(bytesWidth)		<< iBytes
					<< setw(bytesWidth)		<< eBytes
					<< setw(countWidth)		<< countSinceLastReset
					<< setprecision(2)
					<< setw(countWidth-2)	<< callCount
					<< endl;
			}
		}

		n = static_cast<MemoryProfilerNode*>(CallstackNode::traverse(n));
	}

	return ss.str();
}

void* MemoryProfiler::onThreadAttach(const char* threadName)
{
	assert(getTlsStruct() == nullptr);

	// NOTE: Allocation of TlsStruct didn't trigger commonAlloc() since we
	// haven't called TlsSetValue() yet and so myHeapAlloc will by pass it.
	TlsStruct* tls = new TlsStruct(threadName);

	{	// We haven't call TlsSetValue() yet so push_back will
		// not trigger myHeapAlloc thus no dead lock.
		ScopeLock lock(mTlsList->mutex);
		mTlsList->push_back(tls);
	}

	TlsSetValue(gTlsIndex, tls);

	return tls;
}

bool MemoryProfiler::enable() const
{
	return CallstackProfiler::enable;
}

void MemoryProfiler::setEnable(bool flag)
{
	CallstackProfiler::enable = flag;
	functionPatcher.UnpatchAll();

	if(flag) {
		// Pre-computed prologue size (for different version of Visual Studio) using libdasm
#if _MSC_VER == 1400	// VC 2005
		const int prologueSize[] = { 5, 5, 5 };
#else _MSC_VER > 1400	// VC 2008
		const int prologueSize[] = { 5, 5 ,5 };
#endif

		// Hooking RtlAllocateHeap is more reliable than hooking HeapAlloc, especially in Vista.
		HMODULE h = GetModuleHandle(_T("ntdll.dll"));
		void* pAlloc, *pReAlloc, *pFree;
		if(h) {
			pAlloc = GetProcAddress(h, "RtlAllocateHeap");
			pReAlloc = GetProcAddress(h, "RtlReAllocateHeap");
			pFree = GetProcAddress(h, "RtlFreeHeap");
		}
		else {
			pAlloc = &HeapAlloc;
			pReAlloc = &HeapReAlloc;
			pFree = &HeapFree;
		}

		// Back up the original function and then do patching
		orgHeapAlloc = (MyHeapAlloc) functionPatcher.copyPrologue(pAlloc, prologueSize[0]);
		orgHeapReAlloc = (MyHeapReAlloc) functionPatcher.copyPrologue(pReAlloc, prologueSize[1]);
		orgHeapFree = (MyHeapFree) functionPatcher.copyPrologue(pFree, prologueSize[2]);

		functionPatcher.patch(pAlloc, &myHeapAlloc);
		functionPatcher.patch(pReAlloc, &myHeapReAlloc);
		functionPatcher.patch(pFree, &myHeapFree);
	}
}

}	// namespace Profiler

#if USE_DLL_MAIN
BOOL APIENTRY DllMain(HINSTANCE hModule, DWORD dwReason, PVOID lpReserved)
{
	switch(dwReason) {
	case DLL_PROCESS_ATTACH:
		// Force the profiler to instanciate
		MemoryProfiler::singleton().onThreadAttach("MAIN THREAD");
		break;
	case DLL_THREAD_ATTACH:
		MemoryProfiler::singleton().onThreadAttach();
		break;
	default:
		break;
	}
	return TRUE;
}
#endif	// USE_DLL_MAIN

#else

namespace Profiler {

MemoryProfilerNode::MemoryProfilerNode(const char name[], CallstackNode* parent)
	: CallstackNode(name, parent)
{}

void MemoryProfilerNode::begin() {}

MemoryProfiler::MemoryProfiler() {}

MemoryProfiler::~MemoryProfiler() {}

void MemoryProfiler::setRootNode(CallstackNode* root) {
	CallstackProfiler::setRootNode(root);
}

void MemoryProfiler::begin(const char name[]) {}

void MemoryProfiler::end() {}

void MemoryProfiler::nextFrame() {}

void MemoryProfiler::reset() {}

std::string MemoryProfiler::defaultReport(size_t nameLength) const {
	return std::string();
}

void MemoryProfiler::onThreadAttach(const char* threadName) {}

bool MemoryProfiler::enable() const { return false; }

void MemoryProfiler::setEnable(bool flag) { (void)flag; }

}	// namespace Profiler

#endif	// _MSC_VER

Profiler::CallstackNode* Profiler::MemoryProfilerNode::createNode(const char name[], Profiler::CallstackNode* parent)
{
	MemoryProfilerNode* parentNode = static_cast<MemoryProfilerNode*>(parent);
	MemoryProfilerNode* n = new MemoryProfilerNode(name, parent);

	// Every thread should have it's own root node which owns a mutex
	if(!parentNode || parentNode->mMutex == nullptr) {
		n->mMutex = new RecursiveMutex();
		n->mIsMutexOwner = true;
	}
	else {
		n->mMutex = parentNode->mMutex;
		n->mIsMutexOwner = false;
	}

	return n;
}

MemoryProfiler& MemoryProfiler::singleton()
{
	static MemoryProfiler instance;
	return instance;
}
