#ifndef __task_h__
#define __task_h__

using namespace threads;

namespace scheduler
{
    class Task
    {
        friend class Scheduler;
        friend class WorkerThread;

    protected:
        void Trigger(Scheduler* Scheduler);

        virtual sint GetDependencies(Task**& Dependencies)
        {
            Dependencies = NULL;
            return 0;
        }

        // Called by the scheduler when the task is fully executed.
        virtual void OnExecuted();

        uint32 TaskID;              // Internal task-id, used for task hashing.
        Task* HashNext;             // Internal hashmap next element pointer.
        volatile sint* ExecCounter; // Pointer to a variable that gets decremented when execution is done.
        volatile sint SyncCounter;  // Used to wait for subtasks to complete.
        SyncEvent* WaitEvent;       // Event used to wait for a task to complete.
        bool Scheduled : 1;         // Is this task scheduled?
        bool Executed : 1;          // Is this task executed?
        bool AutoDestroy : 1;       // Is this task automatically destroyed after execution?
        bool IsDependency : 1;      // Is this task a dependency for another task?
        bool IsHashed : 1;          // Is this task in the dependencies hashmap?

    public:
        Task(bool _AutoDestroy = true, bool _WaitEvent = false, volatile sint* _ExecCounter = NULL);
        virtual ~Task();

        virtual void Execute(Scheduler* Scheduler)=0;

        FORCEINLINE bool IsExecuted() const
        {
            return Executed && !SyncCounter;
        }

        FORCEINLINE void Sync()
        {
            CHECK(WaitEvent);
            WaitEvent->Wait();
        }
    };
}

#endif//__task_h__