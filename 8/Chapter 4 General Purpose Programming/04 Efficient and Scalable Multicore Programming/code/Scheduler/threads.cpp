#include "platform.h"
#include "threads.h"

// Per-thread information
THREADLS ThreadInfo* GThreadDesc = NULL;

namespace threads
{
#ifdef _MSC_VER
    // Thread wrapper
    struct ThreadHandleWin: ThreadHandle
    {
        HANDLE ThreadHandle;

        ThreadHandleWin(Thread* Thread, sint StackSize, char* ThreadName)
        {
            DWORD ThreadID;
            ThreadHandle = ::CreateThread(NULL, StackSize, ThreadProc, Thread, 0, &ThreadID);

            if(ThreadHandle)
            {
                SetThreadName(ThreadID, ThreadName);
            }
        }

        ~ThreadHandleWin()
        {
            if(ThreadHandle)
            {
                CloseHandle(ThreadHandle);
            }
        }

        bool IsValid()
        {
            return (ThreadHandle != NULL);
        }

        static DWORD __stdcall ThreadProc(LPVOID ThreadPtr)
        {
	        CHECK(ThreadPtr);
            Thread* ThreadInstance = (Thread*)ThreadPtr;

            ThreadInstance->StartRun();
            ThreadInstance->RegisterThread();

            uint32 ReturnCode = 1;

            if(ThreadInstance->Init())
            {
                while(!ThreadInstance->IsForceExit() && ThreadInstance->Run())
                {
                }

                ThreadInstance->Exit();
            }

            ThreadInstance->EndRun();
            return ReturnCode;
        }
    };

    static ThreadHandle* CreateThreadHandle(Thread* ThreadInstance, sint StackSize, char* ThreadName)
    {
        return new ThreadHandleWin(ThreadInstance, StackSize, ThreadName);
    }

    Thread::~Thread()
    {
        CHECK(!IsRunning());

        delete ThreadDesc;        
        delete ThreadHandle;
    }

    void Thread::CreateThread(sint StackSize, char* ThreadName)
    {
        ThreadDesc = new ThreadInfo;
        strcpy(ThreadDesc->ThreadName, ThreadName);

        ThreadHandle = CreateThreadHandle(this, StackSize, ThreadDesc->ThreadName);

        CHECK(ThreadHandle);
        CHECK(ThreadHandle->IsValid());
    }

    void Thread::RegisterThread()
    {
        CHECK(!GThreadDesc);
        CHECK(ThreadDesc);

        ThreadDesc->ThreadID = GetCurrentThreadId();
        GThreadDesc = ThreadDesc;
    }

    void Thread::RegisterThread(char* ThreadName)
    {
        CHECK(!GThreadDesc);

        GThreadDesc = new ThreadInfo;
        strcpy(GThreadDesc->ThreadName, ThreadName);
        GThreadDesc->ThreadID = GetCurrentThreadId();
    }

    // Synchronization
    class SyncEventWin: public SyncEvent
    {
    protected:
        HANDLE EventHandle;

    public:
        SyncEventWin(bool ManualReset, char* Name)
        {
            EventHandle = CreateEventA(NULL, ManualReset, 0, Name);
            CHECK(EventHandle);
        }

        ~SyncEventWin()
        {
            CloseHandle(EventHandle);
        }

        void Trigger()
        {
            SetEvent(EventHandle);
        }

        void Reset()
        {
            ResetEvent(EventHandle);
        }

        bool Wait(uint32 WaitTime)
        {
            return WaitForSingleObject(EventHandle, WaitTime) == WAIT_OBJECT_0;
        }

        void Lock()
        {
            WaitForSingleObject(EventHandle, INFINITE);
        }

        void Unlock()
        {
            PulseEvent(EventHandle);
        }
    };

    class CriticalSectionWin: public CriticalSection
    {
    protected:
        CRITICAL_SECTION CritSec;

    public:
        CriticalSectionWin()
        {
            InitializeCriticalSection(&CritSec);
        }

        ~CriticalSectionWin()
        {
            DeleteCriticalSection(&CritSec);
        }

        void Lock()
        {
            EnterCriticalSection(&CritSec);
        }

        void Unlock()
        {
            LeaveCriticalSection(&CritSec);
        }
    };

    SyncEvent* CreateSyncEvent(bool ManualReset, char* Name)
    {
        return new SyncEventWin(ManualReset, Name);
    }

    CriticalSection* CreateCriticalSection()
    {
        return new CriticalSectionWin;
    }
#endif
}