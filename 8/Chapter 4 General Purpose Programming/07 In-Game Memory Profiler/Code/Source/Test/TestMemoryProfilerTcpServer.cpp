//-------------------------------------------------------------------
// In Game Memory Profiler
// by Ricky Lung Man Tat (mtlung@gmail.com)


#include "../Profiler/MemoryProfiler.h"
#include "../Profiler/Thread.h"
#include "../Profiler/Timer.h"
#include <conio.h>	// For _kbhit()
#include <iostream>	// For std::cout
#include <list>
#include <stdlib.h>	// For system()
#include <sstream>	// For ostringstream

using namespace Profiler;

#include <Winsock2.h>
#pragma comment(lib, "Ws2_32")

//! A simple TCP server to send the profiling result as a string to a client.
class ProfilerServer
{
public:
	ProfilerServer() : connected(false)
	{
		WSADATA	wsad;
		::WSAStartup(WINSOCK_VERSION, &wsad);
	}

	~ProfilerServer()
	{
		::WSACleanup();
	}

	bool listern()
	{
		if((sock = ::socket(AF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET)
			return false;

		unsigned long nonBlocking = 1;
		if(::ioctlsocket(sock, FIONBIO, &nonBlocking) == SOCKET_ERROR)
			return false;

		serverAddr.sin_family = AF_INET;
		serverAddr.sin_port = ::htons(5000);
		serverAddr.sin_addr.s_addr = INADDR_ANY;
		::memset(&(serverAddr.sin_zero), 0, 8);

		if(::bind(sock, (sockaddr*)(&serverAddr), sizeof(serverAddr)) != 0)
			return false;

		if(::listen(sock, 5) != 0)
			return false;

		return true;
	}

	/*!	Try to accept any pending client connection.
		This function is non-blocking so you might need to call it periodically.
	 */
	bool accept()
	{
		int sin_size = sizeof(struct sockaddr_in);

		if(connected)
			return false;

		clientSock = ::accept(sock, (sockaddr*)(&clientAddr), &sin_size);

		if(clientSock != INVALID_SOCKET)
		{
			connected = true;
			std::cout << "Client connected!!" << std::endl;
			return true;
		}

		return false;
	}

	//! Send a string protocol to the client
	void update(const std::string& str)
	{
		if(!connected)
			return;

		if(::send(clientSock, str.c_str(), str.length(), 0) == SOCKET_ERROR) {
			std::cout << "sendto() failed" << std::endl;
			connected = false;
		}
	}

	int sock;
	int clientSock;
	struct sockaddr_in serverAddr, clientAddr;
	bool connected;
};	// ProfilerServer

namespace {

template<typename T>
class TestList : protected std::list<T>
{
	typedef std::list<T> Super;

public:
	void push_back(const T& val)
	{
		ScopeLock lock(mutex);
		Super::push_back(val);
	}

	void pop_front()
	{
		ScopeLock lock(mutex);
		if(!Super::empty())
			Super::pop_front();
	}

	mutable Profiler::Mutex mutex;
};	// TestList

class MyObject
{
	typedef MemoryProfiler::Scope Scope;

public:
	void functionA() {
		Scope s("functionA");

		{	// Let's make some noise!
			void* b = malloc(10);
			free(b);

			b = malloc(0);
			free(b);
			free(nullptr);

			b = realloc(nullptr, 10);
			b = realloc(b, 20);		// Most likely the memory pointer does not altered
			b = realloc(b, 2000);	// Most likely the memory pointer is altered
			b = realloc(b, 0);

			b = calloc(10, 4);
			free(b);
			std::string s("hello world!");

			testList.push_back(123);	// pop_front in functionD()
		}

		functionB();
		functionD();
	}

	void functionB() {
		Scope s("functionB");
		functionC(new int[10]);
		recurse1(10);
	}

	void functionC(void* newByFunB) {
		Scope s("functionC");
		delete[] newByFunB;
		recurse(10);
		functionD();
	}

	void functionD() {
		Scope s("functionD");
		testList.pop_front();	// push_back in functionA()
	}

	// Recurse itself
	void recurse(int count) {
		Scope s("recurse");
		free(malloc(1));
		if(count > 0)
			recurse(count - 1);
	}

	/*!	Recurse between two functions:
		recurse1 -> recurse2 -> recurse1 -> recurse2 ...
	 */
	void recurse1(int count) {
		Scope s("recurse1");
		recurse2(count);
	}

	void recurse2(int count) {
		Scope s("recurse2");
		if(count > 0)
			recurse1(count - 1);
		free(malloc(1));
	}

	// A static variable shared by all threads
	static TestList<int> testList;
};	// MyObject

TestList<int> MyObject::testList;

//! Keep active until the thread inform it to quit
class LoopRunnable : public Thread::IRunnable
{
protected:
	sal_override void run(Thread& thread) throw()
	{
		while(thread.keepRun()) {
			MemoryProfiler::Scope s("LoopRunnable::run");
			mMyObject.functionA();
		}
	}

	MyObject mMyObject;
};	// LoopRunnable

std::string makeReport(MemoryProfiler& profiler)
{
	std::ostringstream ss;
	MemoryProfilerNode* n = static_cast<MemoryProfilerNode*>(profiler.getRootNode());

	while(n)
	{	// Race with MemoryProfiler::begin(), MemoryProfiler::end(), commonAlloc() and commonDealloc()
		ScopeRecursiveLock lock(n->mMutex);

		// Skip node that have no allocation at all
		if(n->inclusiveCount() != 0 || n->countSinceLastReset != 0)
		{
			size_t callDepth = n->callDepth();
			const char* name = n->name;
			size_t iCount = n->inclusiveCount();
			size_t eCount = n->exclusiveCount;
			float iBytes = float(n->inclusiveBytes()) / 1024;
			float eBytes = float(n->exclusiveBytes) / 1024;
			float countSinceLastReset = float(n->countSinceLastReset) / profiler.frameCount;
			float callCount = float(n->callCount) / profiler.frameCount;

			{	// The string stream will make allocations, therefore we need to unlock the mutex
				// to prevent dead lock.
				ScopeRecursiveUnlock unlock(n->mMutex);
				ss	<< callDepth << ";"
					<< n << ";"	// Send the address as the node identifier
					<< name << ";"
					<< iCount << ";"
					<< eCount << ";"
					<< iBytes << ";"
					<< eBytes << ";"
					<< countSinceLastReset << ";"
					<< callCount << ";"
					<< std::endl;
			}
		}

		n = static_cast<MemoryProfilerNode*>(CallstackNode::traverse(n));
	}

	return ss.str();
}

}	// namespace

void testMemoryProfilerTcpServer()
{
	MemoryProfiler& memoryProfiler = MemoryProfiler::singleton();
	memoryProfiler.setEnable(true);

	ProfilerServer server;
	server.listern();

	MyObject obj;
	Timer timer;

	LoopRunnable runnable1, runnable2, runnable3;
	Thread thread1(runnable1, false);
	thread1.setPriority(Thread::NormalPriority);
	Thread thread2(runnable2, false);
	thread2.setPriority(Thread::LowPriority);
//	Thread thread3(runnable3, false);
//	thread3.setPriority(Thread::LowPriority);

	while(true) {
		obj.functionA();

		// Inform the profiler we move to the next iteration
		memoryProfiler.nextFrame();

		server.accept();

		// Refresh and display the profiling result every 1 second
		if(timer.get().asSecond() > 1)
		{
			{	MemoryProfiler::Scope s("system(\"cls\")");
				::system("cls");
			}

			std::string s = makeReport(memoryProfiler);
			server.update(s + "\n\n");
			if(!server.connected)
				std::cout << "Waiting for connection on port 5000\n";
			std::cout << "Press any key to quit...\n\n";
			std::cout << s << std::endl;
			memoryProfiler.reset();
			timer.reset();

			if(_kbhit())
				return;
		}
	}
}
