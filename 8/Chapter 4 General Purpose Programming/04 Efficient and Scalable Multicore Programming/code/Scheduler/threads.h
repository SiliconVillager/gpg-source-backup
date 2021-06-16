#ifndef __threads_h__
#define __threads_h__

struct ThreadInfo
{
    char ThreadName[128];
    uint32 ThreadID;

    ThreadInfo()
    : ThreadID(0xffffffff)
    {}
};

extern THREADLS ThreadInfo* GThreadDesc;

namespace threads
{
#ifdef _MSC_VER
    extern "C"
    {
        // Make sure that the InterlockedXXX functions are generated as intrinsics:
        LONG _InterlockedIncrement(LONG volatile *Addend);
        LONG _InterlockedDecrement(LONG volatile *Addend);
        LONG _InterlockedCompareExchange(LPLONG volatile Dest, LONG Exchange, LONG Comperand);
        LONGLONG _InterlockedCompareExchange64(LONGLONG volatile *Dest, LONGLONG Exchange, LONGLONG Comperand);
        LONG _InterlockedExchange(LPLONG volatile Target, LONG Value);
        LONG _InterlockedExchangeAdd(LPLONG volatile Addend, LONG Value);
    }

    FORCEINLINE uint32 GetCurrentThreadId()
    {
        return ::GetCurrentThreadId();
    }

    FORCEINLINE sint InterlockedIncrement(volatile sint* Value)
    {
        return (sint)_InterlockedIncrement((LPLONG)Value);
    }

    FORCEINLINE sint InterlockedDecrement(volatile sint* Value)
    {
        return (sint)_InterlockedDecrement((LPLONG)Value);
    }

    FORCEINLINE sint InterlockedAdd(volatile sint* Value, sint Amount)
    {
        return (sint)_InterlockedExchangeAdd((LPLONG)Value, (LONG)Amount);
    }

    FORCEINLINE sint InterlockedSub(volatile sint* Value, sint Amount)
    {
        return (sint)_InterlockedExchangeAdd((LPLONG)Value, (LONG)-Amount);
    }

    FORCEINLINE sint InterlockedExchange(volatile sint* Value, sint Exchange)
    {
        return (sint)_InterlockedExchange((LPLONG)Value, (LONG)Exchange);
    }

    FORCEINLINE sint InterlockedCompareExchange(volatile sint* Dest, sint Exchange, sint Comperand)
    {
        return (sint)_InterlockedCompareExchange((LPLONG)Dest, (LONG)Exchange, (LONG)Comperand);
    }

    FORCEINLINE sint64 InterlockedCompareExchange64(volatile sint64* Dest, sint64 Exchange, sint64 Comperand)
    {
        return (sint64)_InterlockedCompareExchange64((LONGLONG*)Dest, (LONGLONG)Exchange, (LONGLONG)Comperand);
    }
#endif

    FORCEINLINE void Sleep(float Milliseconds)
    {
        ::Sleep(Milliseconds);
    }

    FORCEINLINE void SwitchThread()
    {
        Sleep(0);
    }

    // Full memory barrier
    FORCEINLINE void MemoryBarrier()
    {
#ifdef POWERPC
        __lwsync();
#else
        ::MemoryBarrier();
#endif
    }

    // SpinLocks are efficient if threads are only likely to be blocked for a short period of time, as they avoid overhead from operating
    // system process re-scheduling or context switching. However, spinlocks become wasteful if held for longer durations, both preventing 
    // other threads from running and requiring re-scheduling. The longer a lock is held by a thread, the greater the risk that it will be 
    // interrupted by the O/S scheduler while holding the lock. If this happens, other threads will be left "spinning" (repeatedly trying 
    // to acquire the lock), while the thread holding the lock is not making progress towards releasing it. The result is a semi-deadlock 
    // until the thread holding the lock can finish and release it. This is especially true on a single-processor system, where each waiting 
    // thread of the same priority is likely to waste its quantum (allocated time where a thread can run) spinning until the thread that 
    // holds the lock is finally finished.
    template<bool Wait, bool Sleep> class SpinLock
    {
        volatile sint LockSem;

    public:
        FORCEINLINE SpinLock()
        : LockSem(0)
        {}

        FORCEINLINE bool Lock()
        {
            do
            {
                // Atomically swap the lock variable with 1 if it's currently equal to 0
                if(!InterlockedCompareExchange(&LockSem, 1, 0))
                {
                    // We successfully acquired the lock
                    MemoryBarrier();
                    return true;
                }

                // To reduce inter-CPU bus traffic, when the lock is not acquired, loop reading without trying to write anything, until 
                // the value changes. This optimization should be effective on all CPU architectures that have a cache per CPU.
                while(Wait && LockSem)
                {
                    if(Sleep)
                    {
                        SwitchThread();
                    }
                }
            }
            while(Wait);

            return false;
        }

        FORCEINLINE void Unlock()
        {
            MemoryBarrier();
            LockSem = 0;
        }
    };

    //
    // A fixed-size, lockfree queue.
    //
    template<class tType> class LockFreeQueue
    {
    private:
        struct LFQNode
        {
            union
            {
                struct
                {
                    tType* Elem;
                    uint32 Key;
                };

                sint64 Value;
            };

            LFQNode()
            {}

            LFQNode(tType* _Elem, uint32 _Key)
            : Elem(_Elem),
              Key(_Key)
            {}
        };

        sint Size;
        LFQNode* RealBuffer;
        LFQNode* Buffer;
        volatile sint ReadIndex;
        volatile sint WriteIndex;
       
    public:
        LockFreeQueue()
        : Size(0),
          Buffer(NULL),
          ReadIndex(0),
          WriteIndex(0)
        {}

        ~LockFreeQueue()
        {
            if(RealBuffer)
            {
                Free((void*)RealBuffer);
            }
        }

        void Init(sint _Size)
        {
            Size = GetNextPowerOfTwo(_Size);
            RealBuffer = (LFQNode*)Malloc(sizeof(LFQNode) * Size + 128);
            CHECK(RealBuffer);
            Buffer = (LFQNode*)(((uint32)RealBuffer + 127) & ~127);
            CHECK(!((uint32)Buffer & 127));
            
            for(sint i=0; i<Size; i++)
            {
                Buffer[i].Elem = NULL;
                Buffer[i].Key = i;
            }
        }

        FORCEINLINE bool Enqueue(tType* Elem)
        {
            CHECK(Elem);
            
            LFQNode CurNode(NULL, 0);
            LFQNode NewNode(Elem, 0);        

            while((WriteIndex - ReadIndex) < Size)
            {
                const sint CurWriteIndex = WriteIndex;
                const sint WriteBucket = (CurWriteIndex & (Size - 1));

                CurNode.Key = CurWriteIndex;
                NewNode.Key = CurWriteIndex;

                if(InterlockedCompareExchange64(&Buffer[WriteBucket].Value, NewNode.Value, CurNode.Value) == CurNode.Value)
                {
                    InterlockedIncrement(&WriteIndex);
                    return true;
                }
            }

            return false;
        }

        FORCEINLINE tType* Dequeue()
        {
            while(ReadIndex != WriteIndex)
            {
                const sint CurReadIndex = ReadIndex;
                const sint ReadBucket = (CurReadIndex & (Size - 1));
                const LFQNode CurNode((tType*)Buffer[ReadBucket].Elem, CurReadIndex);

                if(CurNode.Elem)
                {
                    const LFQNode NewNode(NULL, CurReadIndex + Size);

                    if(InterlockedCompareExchange64(&Buffer[ReadBucket].Value, NewNode.Value, CurNode.Value) == CurNode.Value)
                    {
                        InterlockedIncrement(&ReadIndex);
                        return (tType*)CurNode.Elem;
                    }
                }
            }

            return NULL;
        }
    };

    //
    // Thread-safe counter
    //
    class ThreadSafeCounter
    {
    protected:
        sint Value;

        ThreadSafeCounter(const ThreadSafeCounter& Other) {}
        void operator =(const ThreadSafeCounter& Other) {}

    public:
        ThreadSafeCounter()
        : Value(0)
        {}

        ThreadSafeCounter(sint _Value)
        : Value(_Value)
        {}

        sint operator *()
        {
            return Value;
        }

        sint operator ++()
        {
            return InterlockedIncrement(&Value);
        }

        sint operator --()
        {
            return InterlockedDecrement(&Value);
        }

        sint operator +=(sint Amount)
        {
            return InterlockedAdd(&Value, Amount);
        }

        sint operator -=(sint Amount)
        {
            return InterlockedAdd(&Value, -Amount);
        }
    };

    //
    // Synchronization base class
    //
    class SyncBase
    {
    public:
        virtual ~SyncBase() {}
        virtual void Lock()=0;
        virtual void Unlock()=0;
    };

    //
    // Event
    //
    class SyncEvent: public SyncBase
    {
    public:
        virtual void Trigger()=0;
        virtual void Reset()=0;
        virtual bool Wait(uint32 WaitTime = 0xffffffff)=0;
    };

    // 
    // Critical section
    //
    class CriticalSection: public SyncBase
    {};

    SyncEvent* CreateSyncEvent(bool ManualReset=FALSE, char* Name=NULL);
    CriticalSection* CreateCriticalSection();

    //
    // Thread wrapper
    //
    struct ThreadHandle
    {
        virtual bool IsValid()=0;
    };

    class Thread
    {
    protected:
        ThreadHandle* ThreadHandle;
        ThreadSafeCounter Running;
        ThreadSafeCounter ForcedExit;

    public:
        ThreadInfo* ThreadDesc;

        Thread()
        : ThreadDesc(NULL)
        {}

        virtual ~Thread();

        void CreateThread(sint StackSize, char* ThreadName);
        void RegisterThread();
        static void RegisterThread(char* ThreadName);

        void StartRun()
        {
            CHECK(!*Running);
            ++Running;
        }

        void EndRun()
        {
            CHECK(*Running);
            --Running;
        }

        bool IsForceExit()
        {
            return !!*ForcedExit;
        }

        void ForceExit(bool Wait)
        {
            ++ForcedExit;

            while(Wait && IsRunning())
            {
                SwitchThread();
            }
        }

        bool IsRunning()
        {
            return (*Running != 0);
        }

        // Interface
        virtual bool Init() { return true; }
        virtual bool Run() = 0;
        virtual void Exit() {}
    };
}

#endif//__threads_h__