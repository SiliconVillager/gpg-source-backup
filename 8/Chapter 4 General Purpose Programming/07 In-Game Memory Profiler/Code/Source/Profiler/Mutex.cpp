//-------------------------------------------------------------------
// In Game Memory Profiler
// by Ricky Lung Man Tat (mtlung@gmail.com)


#include "Mutex.h"
#include "PlatformInclude.h"
#include <assert.h>

namespace Profiler {

#ifdef _WIN32

Mutex::Mutex(int spinCount)
{
	// If you see this static assert, please check the size of the CRITICAL_SECTION
	assert(sizeof(mMutex) == sizeof(CRITICAL_SECTION));

	// Fallback to InitializeCriticalSection if InitializeCriticalSectionAndSpinCount didn't success
	if(spinCount < 0 || !::InitializeCriticalSectionAndSpinCount((LPCRITICAL_SECTION)&mMutex, spinCount))
		::InitializeCriticalSection((LPCRITICAL_SECTION)&mMutex);

#ifndef NDEBUG
	_locked = false;
#endif
}

Mutex::~Mutex()
{
#ifndef NDEBUG
	assert(!_locked && "Delete before unlock");
#endif
	::DeleteCriticalSection((LPCRITICAL_SECTION)&mMutex);
}

void Mutex::lock()
{
	::EnterCriticalSection((LPCRITICAL_SECTION)&mMutex);
#ifndef NDEBUG
	assert(!_locked && "Double lock");
	_locked = true;
#endif
}

void Mutex::unlock()
{
#ifndef NDEBUG
	assert(_locked && "Unlock when not locked");
	_locked = false;
#endif
	::LeaveCriticalSection((LPCRITICAL_SECTION)&mMutex);
}

//! Return true if already locked, otherwise false
bool Mutex::tryLock()
{
	if(::TryEnterCriticalSection((LPCRITICAL_SECTION)&mMutex) > 0) {
#ifndef NDEBUG
		assert(!_locked && "Double lock");
		_locked = true;
#endif
		return true;
	} else
		return false;
}

RecursiveMutex::RecursiveMutex(int spinCount)
{
	// Fallback to InitializeCriticalSection if InitializeCriticalSectionAndSpinCount didn't success
	if(spinCount < 0 || !::InitializeCriticalSectionAndSpinCount((LPCRITICAL_SECTION)&mMutex, spinCount))
		::InitializeCriticalSection((LPCRITICAL_SECTION)&mMutex);

#ifndef NDEBUG
	_lockCount = 0;
#endif
}

RecursiveMutex::~RecursiveMutex()
{
#ifndef NDEBUG
	assert(!isLocked() && "Delete before unlock");
#endif
	::DeleteCriticalSection((LPCRITICAL_SECTION)&mMutex);
}

void RecursiveMutex::lock()
{
	::EnterCriticalSection((LPCRITICAL_SECTION)&mMutex);
#ifndef NDEBUG
	++_lockCount;
#endif
}

void RecursiveMutex::unlock()
{
#ifndef NDEBUG
	assert(_lockCount > 0);
	--_lockCount;
#endif
	::LeaveCriticalSection((LPCRITICAL_SECTION)&mMutex);
}

bool RecursiveMutex::tryLock()
{
#ifndef NDEBUG
	bool locked = ::TryEnterCriticalSection((LPCRITICAL_SECTION)&mMutex) > 0;
	if(locked) ++_lockCount;
	return locked;
#else
	return ::TryEnterCriticalSection((LPCRITICAL_SECTION)&mMutex) > 0;
#endif
}

#else

Mutex::Mutex(int spinCount)
{
#ifndef NDEBUG
	_locked = false;
#endif
	// TODO: Spin count is not yet supported.
	::pthread_mutex_init(&mMutex, nullptr);
}

Mutex::~Mutex()
{
#ifndef NDEBUG
	MCD_ASSUME(!_locked && "Delete before unlock");
#endif
	::pthread_mutex_destroy(&mMutex);
}

void Mutex::lock()
{
	verify(::pthread_mutex_lock(&mMutex) == 0);
#ifndef NDEBUG
	MCD_ASSUME(!_locked && "Double lock");
	_locked = true;
#endif
}

void Mutex::unlock()
{
#ifndef NDEBUG
	MCD_ASSUME(_locked && "Unlock when not locked");
	_locked = false;
#endif
	verify(::pthread_mutex_unlock(&mMutex) == 0);
}

bool Mutex::tryLock()
{
	if(!::pthread_mutex_trylock(&mMutex)) {
#ifndef NDEBUG
		MCD_ASSUME(!_locked && "Double lock");
		_locked = true;
#endif
		return true;
	} else {
		return false;
	}
}

RecursiveMutex::RecursiveMutex()
{
	pthread_mutexattr_t attr;
	verify(::pthread_mutexattr_init(&attr) == 0);
	verify(::pthread_mutexattr_settype(&attr, PTHREAD_MUTEX_RECURSIVE) == 0);
	verify(::pthread_mutex_init(&mMutex, &attr) == 0);
	verify(::pthread_mutexattr_destroy(&attr) == 0);
}

RecursiveMutex::~RecursiveMutex()
{
	assert(!isLocked() && "Delete before unlock");
	verify(::pthread_mutex_destroy(&mMutex) == 0);
}

void RecursiveMutex::lock()
{
	::pthread_mutex_lock(&mMutex);
#ifndef NDEBUG
	++_lockCount;
#endif
}

void RecursiveMutex::unlock()
{
#ifndef NDEBUG
	assert(_lockCount > 0);
	--_lockCount;
#endif
	verify(::pthread_mutex_unlock(&mMutex) == 0);
}

bool RecursiveMutex::tryLock()
{
#ifndef NDEBUG
	bool locked = !::pthread_mutex_trylock(&mMutex);
	if(locked) ++_lockCount;
	return locked;
#else
	return !::pthread_mutex_trylock(&mMutex);
#endif
}

#endif	// _WIN32

#ifndef NDEBUG
bool RecursiveMutex::isLocked() const
{
	return _lockCount > 0;
}
int RecursiveMutex::lockCount() const
{
	return _lockCount;
}
#endif

}	// namespace Profiler
