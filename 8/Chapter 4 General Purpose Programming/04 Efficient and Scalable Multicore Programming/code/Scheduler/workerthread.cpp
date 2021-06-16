#include "platform.h"
#include "threads.h"
#include "task.h"
#include "workerthread.h"
#include "scheduler.h"

using namespace scheduler;

WorkerThread::WorkerThread(Scheduler* _Parent)
: CurrentTask(NULL),
  Parent(_Parent)
{
    TasksEvent = CreateSyncEvent();

    static uint32 ThreadID = 0;
    static char ThreadName[128];
    sprintf(ThreadName, "worker_thread_%d", ThreadID++);

    CreateThread(256 * 1024, ThreadName);
}

bool WorkerThread::Run()
{
    // Wait for the task trigger event
    TasksEvent->Wait();

    // Loop executing ready tasks
    while(CurrentTask)
    {
        // Execute current task
        CurrentTask->Trigger(Parent);

        // Try to get another task
        CurrentTask = Parent->GetNextReadyTask(this);
    }

    // Run a scheduler slice
    Parent->SchedulerSlice();

    return true;
}

void WorkerThread::SetTask(Task* Task)
{
    CurrentTask = Task;
    TasksEvent->Trigger();
}
