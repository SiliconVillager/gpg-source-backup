#include "platform.h"
#include "threads.h"
#include "task.h"

using namespace threads;
using namespace scheduler;

#include "scheduler.h"

Task::Task(bool _AutoDestroy, bool _WaitEvent, volatile sint* _ExecCounter)
: Scheduled(false),
  Executed(false),
  AutoDestroy(_AutoDestroy),
  IsDependency(false),
  IsHashed(false),
  ExecCounter(_ExecCounter),
  SyncCounter(0),
  HashNext(NULL)
{
    CHECK(!_WaitEvent || !_AutoDestroy);

    static uint32 UniqueTaskID = 0;
    TaskID = InterlockedIncrement((sint*)&UniqueTaskID);

    WaitEvent = _WaitEvent ? CreateSyncEvent() : NULL;
}

Task::~Task()
{
    if(WaitEvent)
    {
        delete WaitEvent;
    }
}

void Task::OnExecuted()
{
    if(ExecCounter)
    {
        InterlockedDecrement(ExecCounter);
    }

    if(WaitEvent)
    {
        WaitEvent->Trigger();
    }
}

void Task::Trigger(Scheduler* Scheduler)
{
    Execute(Scheduler);

    Executed = true;

    if(!IsHashed && IsExecuted())
    {
        OnExecuted();

        if(AutoDestroy)
        {
            delete this;
        }
    }
    else
    {
        VERIFY(Scheduler->PendingTasksQueue.Enqueue(this));
    }
}
