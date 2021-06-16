#include "platform.h"
#include "threads.h"
#include "task.h"
#include "workerthread.h"
#include "scheduler.h"

using namespace scheduler;

Scheduler::Scheduler(sint NumThreads)
{
    SchedulingSlices = 0;

    for(sint i=0; i<ARRAYCOUNT(ActiveTasksMap); i++)
    {
        ActiveTasksMap[i] = NULL;
    }

    NumWorkerThreads = (NumThreads == -1) ? GetNumProcessors() : NumThreads;
    CHECK(NumWorkerThreads > 0);

    WorkerThreads = (WorkerThread**)Malloc(NumWorkerThreads * sizeof(WorkerThread**));

    PendingTasksQueue.Init(65536);
    ReadyTasksQueue.Init(65536);
    IdleThreadsQueue.Init(NumWorkerThreads);

    for(sint i=0; i<NumWorkerThreads; i++)
    {
        WorkerThreads[i] = new WorkerThread(this);
        VERIFY(IdleThreadsQueue.Enqueue(WorkerThreads[i]));
    }

    printf("Scheduler initialized using %d worker threads\n", NumWorkerThreads);
}

void Scheduler::SchedulerSlice()
{
    InterlockedIncrement(&SchedulingSlices);

    if(SchedulerLock.Lock())
    {
        while(SchedulingSlices)
        {
            // Schedule waiting tasks
            Task* CurTask;
            while(CurTask = PendingTasksQueue.Dequeue())
            {
                if(CurTask->Scheduled)
                {
                    if(!CurTask->IsHashed)
                    {
                        // Fast path: the task is fully executed and not hashed
                        if(CurTask->IsExecuted())
                        {
                            CurTask->OnExecuted();

                            if(CurTask->AutoDestroy)
                            {
                                delete CurTask;
                            }
                        }
                        else
                        {
                            // Register task in dependencies hashmap
                            CHECK(!CurTask->HashNext);
                            uint32 HashBin = (CurTask->TaskID) & (ARRAYCOUNT(ActiveTasksMap) - 1);
                            CurTask->HashNext = ActiveTasksMap[HashBin];
                            CurTask->IsHashed = true;
                            ActiveTasksMap[HashBin] = CurTask;
                        }
                    }
                }
                else
                {
                    Task** Dependencies = NULL;
                    sint NumDependencies = CurTask->GetDependencies(Dependencies);

                    if(CurTask->IsDependency || NumDependencies)
                    {
                        // Register task in dependencies hashmap
                        CHECK(!CurTask->HashNext);
                        uint32 HashBin = (CurTask->TaskID) & (ARRAYCOUNT(ActiveTasksMap) - 1);
                        CurTask->HashNext = ActiveTasksMap[HashBin];
                        CurTask->IsHashed = true;
                        ActiveTasksMap[HashBin] = CurTask;
                    }

                    if(!NumDependencies)
                    {
                        // Schedule ready tasks
                        ScheduleTask(CurTask);
                    }
                }
            }

            // Schedule ready tasks with dependencies
            for(sint i=0; i<ARRAYCOUNT(ActiveTasksMap); i++)
            {
                Task* CurTask = ActiveTasksMap[i];
                Task** PrevLink = &ActiveTasksMap[i];

                while(CurTask)
                {
                    CHECK(CurTask->IsHashed);

                    bool UnHashTask = false;
                    bool DestroyTask = false;

                    if(!CurTask->Scheduled)
                    {
                        bool TaskIsReady = true;
                        Task** Dependencies = NULL;
                        sint NumDependencies = CurTask->GetDependencies(Dependencies);

                        for(sint j=0; j<NumDependencies; j++)
                        {
                            Task* ChildTask = Dependencies[j];

                            uint32 HashBin = (ChildTask->TaskID) & (ARRAYCOUNT(ActiveTasksMap) - 1);
                            Task* HashTask = ActiveTasksMap[HashBin];

                            while(HashTask)
                            {
                                if((HashTask == ChildTask) && !ChildTask->IsExecuted())
                                {
                                    TaskIsReady = false;
                                    break;
                                }

                                HashTask = HashTask->HashNext;
                            }
                        }

                        if(TaskIsReady)
                        {
                            ScheduleTask(CurTask);
                            UnHashTask = !CurTask->IsDependency;
                        }
                    }
                    else if(CurTask->IsExecuted())
                    {
                        CurTask->OnExecuted();
                        DestroyTask = CurTask->AutoDestroy;
                        UnHashTask = true;
                    }

                    if(UnHashTask)
                    {
                        *PrevLink = CurTask->HashNext;
                        CurTask->IsHashed = false;
                    }
                    else
                    {
                        PrevLink = &CurTask->HashNext;
                    }

                    Task* NextTask = CurTask->HashNext;

                    if(DestroyTask)
                    {
                        delete CurTask;
                    }

                    CurTask = NextTask;
                }
            }

            InterlockedDecrement(&SchedulingSlices);
        }

        SchedulerLock.Unlock();
    }
}

Task* Scheduler::GetNextReadyTask(WorkerThread* Thread)
{
    QueuesLock.Lock();

    Task* ReadyTask = ReadyTasksQueue.Dequeue();

    if(ReadyTask)
    {
        QueuesLock.Unlock();
        return ReadyTask;
    }

    VERIFY(IdleThreadsQueue.Enqueue(Thread));

    QueuesLock.Unlock();
    return NULL;
}

WorkerThread* Scheduler::GetNextIdleThread(Task* Task)
{
    QueuesLock.Lock();

    WorkerThread* IdleThread = IdleThreadsQueue.Dequeue();

    if(IdleThread)
    {
        QueuesLock.Unlock();
        return IdleThread;
    }

    VERIFY(ReadyTasksQueue.Enqueue(Task));

    QueuesLock.Unlock();
    return NULL;
}

void Scheduler::ScheduleTask(Task* Task)
{
    Task->Scheduled = true;

    WorkerThread* WorkerThread = GetNextIdleThread(Task);

    if(WorkerThread)
    {
        WorkerThread->SetTask(Task);
    }
}

void Scheduler::AddTask(Task* NewTask)
{
    Task** Dependencies = NULL;
    sint NumDependencies = NewTask->GetDependencies(Dependencies);

    // Recursivly add dependant tasks
    for(sint i=0; i<NumDependencies; i++)
    {
        Dependencies[i]->IsDependency = true;
        AddTask(Dependencies[i]);
    }

    if(!NumDependencies && !NewTask->IsDependency)
    {
        // The task is ready to execute; schedule it right away
        ScheduleTask(NewTask);
    }
    else
    {
        // Since the task has dependencies, we register it internally.
        VERIFY(PendingTasksQueue.Enqueue(NewTask));

        SchedulerSlice();
    }
}
