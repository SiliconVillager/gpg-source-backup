//-------------------------------------------------------------------
// In Game Memory Profiler
// by Ricky Lung Man Tat (mtlung@gmail.com)


#include "../Profiler/Thread.h"
#include "../Profiler/ThreadedCpuProfiler.h"
#include <conio.h>	// For _kbhit()
#include <iostream>	// For std::cout
#include <stdlib.h>	// For system()

using namespace Profiler;

namespace {

class MyObject
{
	typedef ThreadedCpuProfiler::Scope Scope;

public:
	void functionA() {
		Scope s("functionA");
		functionB();
		functionD();
	}

	void functionB() {
		Scope s("functionB");
		functionC();
		recurse1(10);
	}

	void functionC() {
		Scope s("functionC");
		recurse(10);
		functionD();
	}

	void functionD() {
		Scope s("functionD");
	}

	// Recurse itself
	void recurse(int count) {
		Scope s("recurse");
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
	}
};	// MyObject

//! Keep active until the thread inform it to quit
class LoopRunnable : public Thread::IRunnable
{
protected:
	sal_override void run(Thread& thread) throw()
	{
		while(thread.keepRun()) {
			ThreadedCpuProfiler::Scope s("LoopRunnable::run");
			mMyObject.functionA();
		}
	}

	MyObject mMyObject;
};	// LoopRunnable

}	// namespace

void testCpuProfiler()
{
	MyObject obj;

	ThreadedCpuProfiler& cpuProfiler = ThreadedCpuProfiler::singleton();
	cpuProfiler.enable = true;

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
		cpuProfiler.nextFrame();

		// Refresh and display the profiling result every 1 second
		if(cpuProfiler.timeSinceLastReset.asSecond() > 1) {
			::system("cls");
			std::string s = cpuProfiler.defaultReport(20, 0);
			std::cout << "Press any key to quit...\n\n";
			std::cout << s << std::endl;
			cpuProfiler.reset();

			if(_kbhit())
				return;
		}
	}
}
