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

using namespace Profiler;

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

}	// namespace

void testMemoryProfiler()
{
	MyObject obj;
	Timer timer;

	MemoryProfiler& memoryProfiler = MemoryProfiler::singleton();
	memoryProfiler.setEnable(true);

	LoopRunnable runnable1, runnable2, runnable3;
	Thread thread1(runnable1, false);
	thread1.setPriority(Thread::NormalPriority);
//	Thread thread2(runnable2, false);
//	thread2.setPriority(Thread::LowPriority);
//	Thread thread3(runnable3, false);
//	thread3.setPriority(Thread::LowPriority);

	while(true) {
		obj.functionA();

		// Inform the profiler we move to the next iteration
		memoryProfiler.nextFrame();

		// Refresh and display the profiling result every 1 second
		if(timer.get().asSecond() > 1) {
			{	MemoryProfiler::Scope s("system(\"cls\")");
				::system("cls");
			}

			std::string s = memoryProfiler.defaultReport(20, 0);
			std::cout << "Press any key to quit...\n\n";
			std::cout << s << std::endl;
			memoryProfiler.reset();
			timer.reset();

			if(_kbhit())
				return;
		}
	}
}
