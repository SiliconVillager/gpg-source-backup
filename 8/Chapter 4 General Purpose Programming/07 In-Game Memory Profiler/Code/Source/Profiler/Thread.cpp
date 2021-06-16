//-------------------------------------------------------------------
// In Game Memory Profiler
// by Ricky Lung Man Tat (mtlung@gmail.com)


#include "Thread.h"
#include "PlatformInclude.h"
#include <assert.h>
#include <iostream>
#include <memory.h> // For memset

#ifdef NDEBUG
#	define VERIFY(X) X
#else
#	define VERIFY(X) assert(X)
#endif

namespace Profiler {

#ifdef _WIN32
DWORD WINAPI _Run(sal_notnull LPVOID p) {
#else
void* _Run(sal_notnull void* p) {
#endif
	Thread* t = reinterpret_cast<Thread*>(p);
	Thread::IRunnable* runnable = t->runnable();
	assert(runnable != nullptr);
	runnable->run(*t);
	return 0;
}

Thread::Thread()
{
	init();
}

Thread::Thread(IRunnable& runnable, bool autoDeleteRunnable)
{
	init();
	start(runnable, autoDeleteRunnable);
}

void Thread::init()
{
	mRunnable = nullptr;
#ifdef _WIN32
	mHandle = nullptr;
	mId = 0;
#else
	mHandle = pthread_t(nullptr);
#endif
	mPriority = NormalPriority;
	mAutoDeleteRunnable = false;
	mKeepRun = false;
}

Thread::~Thread()
{
	// No need to lock

	if(isWaitable())
		wait();
	else
		cleanup();

	if(mAutoDeleteRunnable)
		delete mRunnable;
}

void Thread::start(IRunnable& runnable, bool autoDeleteRunnable)
{
	ScopeRecursiveLock lock(mMutex);

	if(mHandle)
		throw std::logic_error("A thread is already in execution, call wait() before starting another one");

	// Delete the previous runnable if needed
	if(mRunnable && mAutoDeleteRunnable)
		delete mRunnable;

	mAutoDeleteRunnable = autoDeleteRunnable;
	mRunnable = &runnable;
	mKeepRun = true;

#ifdef _WIN32
	assert(mId == 0);
	mHandle = reinterpret_cast<intptr_t>(::CreateThread(nullptr, 0, &_Run, this, 0, (LPDWORD)&mId));
	if(!mHandle)
#else
	if(::pthread_create(&mHandle, nullptr, &_Run, this) != 0)
#endif
		std::cout << "Warning: Error creating thread\n";
}

void Thread::postQuit()
{
	// No need to lock (mKeepRun is an AtomicValue)
	mKeepRun = false;
}

bool Thread::keepRun() const
{
	// No need to lock (mKeepRun is an AtomicValue)
	return mKeepRun;
}

bool Thread::isWaitable() const
{
	ScopeRecursiveLock lock(mMutex);
	return mHandle && Profiler::getCurrentThreadId() != id();
}

void Thread::throwIfWaited() const
{
	ScopeRecursiveLock lock(mMutex);
	if(!mHandle)
		throw std::logic_error("The thread is already stopped");
}

void Thread::cleanup()
{
	ScopeRecursiveLock lock(mMutex);

	if(!mHandle)
		return;

#ifdef _WIN32
	VERIFY(::CloseHandle(reinterpret_cast<HANDLE>(mHandle)));
	mHandle = nullptr;

	assert(mId != 0);
	mId = 0;
#else
	assert(mHandle);
	::pthread_detach(mHandle);
	mHandle = pthread_t(nullptr);
#endif
}

Thread::Priority Thread::getPriority() const
{
	ScopeRecursiveLock lock(mMutex);

	throwIfWaited();

#ifdef MCD_CYGWIN
	throw std::logic_error("Not implemented.");
#elif defined(_WIN32)
	int ret = ::GetThreadPriority(reinterpret_cast<HANDLE>(mHandle));
	if(ret == THREAD_PRIORITY_ERROR_RETURN)
		std::cout << "Warning: Error getting thread priority\n";

	if(ret > 0)
		ret = HighPriority;
	else if(ret < 0)
		ret = LowPriority;

	return Priority(ret);
#else
	sched_param param;
	int policy;
	MCD_VERIFY(::pthread_getschedparam(mHandle, &policy, &param) == 0);

	if(policy == SCHED_RR)
		return NormalPriority;
	else if(param.sched_priority > 50)
		return HighPriority;
	else
		return LowPriority;
#endif
}

void Thread::setPriority(Priority priority)
{
	ScopeRecursiveLock lock(mMutex);

	throwIfWaited();

#ifdef MCD_CYGWIN
	(void)priority;
	throw std::logic_error("Not implemented.");
#elif defined(_WIN32)
	if(::SetThreadPriority(reinterpret_cast<HANDLE>(mHandle), int(priority)) == 0)
		std::cout << "Warning: Error setting thread priority\n";
#else
	// How to set thread priority on Linux:
	// http://cs.pub.ro/~apc/2003/resources/pthreads/uguide/users-31.htm
	// http://www.developertutorials.com/tutorials/linux/processes-threads-050616/page10.html
	sched_param param;
	::memset(&param, 0, sizeof(param));

	int policy = SCHED_RR;
	if(priority == NormalPriority)
		policy = SCHED_OTHER;
	else if(priority == LowPriority)
		param.sched_priority = 20;
	else if(priority == HighPriority)
		param.sched_priority = 70;
	else
		assert(false);
	if(::pthread_setschedparam(mHandle, policy, &param) != 0)
		std::cout << "Warning: Error setting thread priority\n";
#endif
}

void Thread::wait()
{
	ScopeRecursiveLock lock(mMutex);

	if(!isWaitable())
		throw std::logic_error("The thread is not waitable");

	postQuit();

	assert(mHandle);

#ifdef _WIN32
	HANDLE handleBackup = reinterpret_cast<HANDLE>(mHandle);
	mHandle = nullptr;

	assert(mId != 0);
	mId = 0;

	// Unlock the mutex before the actual wait operation
	lock.mutex().unlock();
	DWORD res = ::WaitForSingleObject(handleBackup, INFINITE);
	lock.mutex().lock();

	if(res == WAIT_FAILED)
		std::cout << "Warning: Error waiting thread\n";
	assert(res == WAIT_OBJECT_0);

	VERIFY(::CloseHandle(handleBackup));
#else
	pthread_t handleBackup = mHandle;
	mHandle = pthread_t(nullptr);

	lock.m.unlock();
	lock.cancel();

	MCD_VERIFY(::pthread_join(handleBackup, nullptr) == 0);
#endif
}

int Thread::id() const
{
	ScopeRecursiveLock lock(mMutex);

#ifdef _WIN32
	return mId;
#else
	return int(mHandle);
#endif
}

int getCurrentThreadId()
{
#ifdef _WIN32
	return ::GetCurrentThreadId();
#else
	return int(::pthread_self());
#endif
}

void mSleep(size_t millseconds)
{
#ifdef _WIN32
	::Sleep(DWORD(millseconds));
#else
	::usleep(useconds_t(millseconds * 1000));
#endif
}

void uSleep(useconds_t microseconds)
{
#ifdef _WIN32
	::Sleep(microseconds / 1000);
#else
	::usleep(microseconds);
#endif
}

}	// namespace Profiler
