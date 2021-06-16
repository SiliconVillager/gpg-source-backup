#ifndef __scheduler_h__
#define __scheduler_h__

namespace scheduler
{
    class Scheduler
    {
        friend class Task;
        friend class WorkerThread;

        // Array of worker threads
        WorkerThread** WorkerThreads;
        sint NumWorkerThreads;

        // Lock used to check if a scheduling slice is already running (never waits)
        SpinLock<false, false> SchedulerLock;

        // Lock used to keep the pending tasks and idle threads queues consistent (rarely spins more than a couple of times)
        SpinLock<true, false> QueuesLock;

        // Hash table of active tasks
        Task* ActiveTasksMap[1024];

        // Internal queues
        LockFreeQueue<Task> PendingTasksQueue;
        LockFreeQueue<Task> ReadyTasksQueue;
        LockFreeQueue<WorkerThread> IdleThreadsQueue;

        // Used to maintain the number of scheduling slices requested
        volatile sint SchedulingSlices;

        Task* GetNextReadyTask(WorkerThread* Thread);
        WorkerThread* GetNextIdleThread(Task* Task);

        void ScheduleTask(Task* Task);
        void SchedulerSlice();

    public:
        Scheduler(sint NumThreads = -1);

        void AddTask(Task* NewTask);        
    };
}

#endif//__scheduler_h__