#ifndef __workerthread_h__
#define __workerthread_h__

using namespace threads;

namespace scheduler
{
    class WorkerThread: public Thread
    {
    protected:
        Task* CurrentTask;          // Current executing task.
        SyncEvent* TasksEvent;      // Event used when waiting for a task.
        Scheduler* Parent;          // Scheduler owning this worker thread.

        bool Run();

    public:
        WorkerThread(Scheduler* _Parent);

        void SetTask(Task* Task);
    };
}

#endif//__workerthread_h__