//-------------------------------------------------------------------
// In Game Memory Profiler
// by Ricky Lung Man Tat (mtlung@gmail.com)


#ifndef __PROFILER_THREAD__
#define __PROFILER_THREAD__

#include "Atomic.h"
#include <stdexcept>

typedef unsigned int useconds_t;

namespace Profiler {

/*!	A thread of execution in a program.
	User need to submit an object of Thread::IRunnable in order for Thread to execute.

	\note Suspend and Resume is removed, since user can use CondVar to do the same.
		Using Suspend and Resume is dangerous, since you don't know where the thread
		stops if you invoke Suspend in another thread.

	\code
	class MyRunnable : public Profiler::Thread::IRunnable {
	public:
		MyRunnable() : LoopCount(0) {}
		// Keep printing hello world until the thread wants to stop
		sal_override void run(Thread& thread) throw() {
			while(thread.keepRun())
				std::cout << "Hello world!\n";
		}
	};	// MyRunnable

	// ...

	MyRunnable runnable;
	Thread thread(runnable, false);	// Tells thread not to delete runnable, since it's on the stack
	Profiler::mSleep(1000);	// Sleep for 1 seconds
	thread.wait();	// Tells the thread we are going to stop and then wait until MyRunnable::run() returns
	\endcode
 */
class PROFILER_API Thread : Noncopyable
{
public:
	//! Sub-classing IRunnable to do the real work for Thread.
	class ABSTRACT_CLASS IRunnable
	{
	public:
		virtual ~IRunnable() {}

		/*! Override this method to do the actual work.
			\note This method may invoked by a number of different threads since different
				Thread can construct with the same IRunnable instance. Therefore please pay
				attention to thread safety on this method.
			\param thread Represent the thread context which invoke this run() method
		 */
		virtual void run(Thread& thread) throw() = 0;
	};	// IRunnable

	//! Construct a Thread instance but without any thread created.
	Thread();

	/*!	Construct a Thread and start it with an IRunnable.
		\sa start()
	 */
	Thread(IRunnable& runnable, bool autoDeleteRunnable=true);

	//! The destructor will wait for the thread to finish.
	~Thread();

	/*!	Associate the thread with an IRunnable and start it.
		\param runnable Make sure it will \em not be deleted before the thread function finish.
		\param autoDeleteRunnable Indicate Thread should manage the life-time of the runnable or not.
		\note Thread creation is submitted to the under laying operation system inside
			the constructor, it didn't guarantee the IRunnable::run() function get
			started before the constructor exit.
		\note Throw exception if you call the start() function without waiting for the
			previous thread function finish.
	 */
	void start(IRunnable& runnable, bool autoDeleteRunnable=true);

	sal_maybenull IRunnable* runnable() const {
		return mRunnable;
	}

	/*! Set a flag so that keepRun() will return false.
		This function is used to inform the IRunnable to quit the loop as soon as possible.
	 */
	void postQuit();

	//! Return false if postQuit() is invoked.
	bool keepRun() const;

	/*! Wait until the runnable finish.
		\note It will invoke postQuit()
		\note Exception if it's not in a waitable state (eg deadlock if it's called inside IRunnable::run())
	 */
	void wait();

	enum Priority
	{
		LowPriority = -1,
		NormalPriority = 0,
		HighPriority = 1
	};	// Priority

	/*!	Get the thread's scheduling priority.
		\note Not working on CYGWIN, only work for super user on Linux
	 */
	Priority getPriority() const;

	/*!	Set the thread's scheduling priority.
		\note Not working on CYGWIN, only work for super user on Linux
	 */
	void setPriority(Priority priority);

	//! Get this thread's ID
	int id() const;

	//! Check that calling wait() will not causing error or deadlock.
	bool isWaitable() const;

	//! Throw if the thread is already wait and finished.
	void throwIfWaited() const;

protected:
	void init();
	void cleanup();

protected:
	IRunnable* mRunnable;
#ifdef _WIN32
	intptr_t mHandle;
	int mId;
#else
	pthread_t mHandle;
#endif
	Priority mPriority;
	bool mAutoDeleteRunnable;
	AtomicValue<bool> mKeepRun;
	mutable RecursiveMutex mMutex;	//! To protect member like mHandle and mId against race-conditions.
};	// Thread

PROFILER_API int getCurrentThreadId();

//! Mill-second sleep.
PROFILER_API void mSleep(size_t millseconds);

//! Portable Unix usleep.
PROFILER_API void uSleep(useconds_t microseconds);

}	// namespace Profiler

#endif	// __PROFILER_THREAD__
