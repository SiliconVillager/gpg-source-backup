#include "platform.h"
#include "threads.h"
#include "task.h"
#include "workerthread.h"
#include "scheduler.h"
#include <stdlib.h>

using namespace threads;
using namespace scheduler;

//
// Simple fixed-size allocator to avoid locking when creating sub-tasks.
// Todo: implement a lock-free memory allocator.
//
template<class Type> struct FixedSizePoolAllocator
{
    struct Node
    {
        Node* Next;
    };

    Node* FreeList;

    void AllocNewNodes()
    {
        sint Size = 655360;

        void* Buffer = Malloc(Size * sizeof(Type));

        Node* FreeNode = (Node*)Buffer;
        FreeList = FreeNode;

        for(sint i=0; i<Size-1; i++)
        {
            Node* NextNode = (Node*)((uint32)FreeNode + sizeof(Type));
            FreeNode->Next = NextNode;
            FreeNode = NextNode;
        }

        FreeNode->Next = NULL;
    }

public:
    FixedSizePoolAllocator()
    {
        AllocNewNodes();
    }

    void* Alloc()
    {
        Node* FreeNode = NULL;
        
        if(!FreeList)
        {
            AllocNewNodes();
        }

        FreeNode = FreeList;
        FreeList = FreeList->Next;

        return FreeNode;
    }

    void Free(void* Ptr)
    {
        Node* FreeNode = (Node*)Ptr;       
        FreeNode->Next = FreeList;
        FreeList = FreeNode;
    }
};

template<class Type> void* operator new(size_t Size, FixedSizePoolAllocator<Type> Allocator)
{
    return Allocator.Alloc();
}

template<class Type> void operator delete(void* Ptr, FixedSizePoolAllocator<Type> Allocator)
{
    Allocator.Free(Ptr);
}

// 
// Fibonacci task
//
class FibonacciTask: public Task
{
    static uint32 Fib(uint32 n)
    {
        if(n <= 1)
        {
            return n;
        }
        else
        {
            return Fib(n - 1) + Fib(n - 2);
        }
    }

    sint GetDependencies(Task**& Dependencies)
    {
        if(Index > 16)
        {
            Dependencies = (Task**)SubTasks;
            return 2;
        }

        Dependencies = NULL;
        return 0;
    }

public:
    sint Index;
    sint Result;
    FibonacciTask* SubTasks[2];

    FibonacciTask(sint _Index, bool WaitEvent = false)
    : Task(false, WaitEvent),
      Index(_Index),
      Result(0)
    {
        if(Index > 16)
        {
            SubTasks[0] = new FibonacciTask(Index - 1);
            SubTasks[1] = new FibonacciTask(Index - 2);
        }
    }

    ~FibonacciTask()
    {
        if(Index > 16)
        {
            delete SubTasks[0];
            delete SubTasks[1];
        }
    }

    void Execute(Scheduler* Parent)
    {
        if(Index <= 16)
        {
            Result = Fib(Index);
        }
        else
        {
            if(!Parent)
            {
                // Serial version
                SubTasks[0]->Execute(NULL);
                SubTasks[1]->Execute(NULL);
            }
            else
            {
                // Parallel version
                CHECK(SubTasks[0]->Executed);
                CHECK(SubTasks[1]->Executed);
            }

            Result = SubTasks[0]->Result + SubTasks[1]->Result;
        }
    }
};

//
// Perlin noise task
//
class PerlinTask: public Task
{
    uint8* Buffer;
    sint Index;
    sint X;
    sint Y;
    sint W;
    sint H;
    sint S;

    FORCEINLINE float fade(float t) { return t * t * t * (t * (t * 6 - 15) + 10); }
    FORCEINLINE float lerp(float t, float a, float b) { return a + t * (b - a); }
    FORCEINLINE float grad(sint hash, float x, float y, float z)
    {
        sint h = hash & 15;                      // CONVERT LO 4 BITS OF HASH CODE
        float u = h<8 ? x : y,                 // INTO 12 GRADIENT DIRECTIONS.
              v = h<4 ? y : h==12||h==14 ? x : z;
        return ((h&1) == 0 ? u : -u) + ((h&2) == 0 ? v : -v);
    }

    static sint Permutations[];

    FORCEINLINE float Noise3D(float x, float y, float z)
    {
        sint floorX = Floor(x),
             floorY = Floor(y),
             floorZ = Floor(z);
        sint X = floorX & 255,                  // FIND UNIT CUBE THAT
             Y = floorY & 255,                  // CONTAINS POINT.
             Z = floorZ & 255;
        x -= floorX;                                // FIND RELATIVE X,Y,Z
        y -= floorY;                                // OF POINT IN CUBE.
        z -= floorZ;
        float  u = fade(x),                                // COMPUTE FADE CURVES
               v = fade(y),                                // FOR EACH OF X,Y,Z.
               w = fade(z);
        sint A = Permutations[X  ]+Y, AA = Permutations[A]+Z, AB = Permutations[A+1]+Z,      // HASH COORDINATES OF
             B = Permutations[X+1]+Y, BA = Permutations[B]+Z, BB = Permutations[B+1]+Z;      // THE 8 CUBE CORNERS,

        return  lerp(w, lerp(v, lerp(u, grad(Permutations[AA  ], x  , y  , z   ),  // AND ADD
                                        grad(Permutations[BA  ], x-1, y  , z   )), // BLENDED
                                lerp(u, grad(Permutations[AB  ], x  , y-1, z   ),  // RESULTS
                                        grad(Permutations[BB  ], x-1, y-1, z   ))),// FROM  8
                        lerp(v, lerp(u, grad(Permutations[AA+1], x  , y  , z-1 ),  // CORNERS
                                        grad(Permutations[BA+1], x-1, y  , z-1 )), // OF CUBE
                                lerp(u, grad(Permutations[AB+1], x  , y-1, z-1 ),
                                        grad(Permutations[BB+1], x-1, y-1, z-1 ))));
    }

public:
    PerlinTask(uint8* _Buffer, sint _X, sint _Y, sint _W, sint _H, sint _S, volatile sint* ExecCounter)
    : Task(false, !ExecCounter, ExecCounter),
      Buffer(_Buffer),
      X(_X),
      Y(_Y),
      W(_W),
      H(_H),
      S(_S)
    {
        if(ExecCounter)
        {
            InterlockedIncrement(ExecCounter);
        }
    }

    volatile sint* GetSyncCounter()
    {
        return &SyncCounter;
    }

    void Execute(Scheduler* Parent)
    {
        uint8* Ptr = Buffer;
        for(sint y=Y; y<Y+H; y++)
        {
            for(sint x=X; x<X+W; x++)
            {
                float Result = 0.0f;
                float Scale = 0.001f;
                float Weight = 1.0f;

                for(sint o=0; o<16; o++)
                {
                    Result += Noise3D(x * Scale, y * Scale, 1.0f) * Weight;
                    Scale *= 2.0f;
                    Weight *= 0.5f;
                }

                *Ptr++ = Clamp(Result * 0.5f + 0.5f, 0.0f, 1.0f) * 255.0f;
            }

            Ptr += (S - W);
        }
    }
};

sint PerlinTask::Permutations[] = { 151,160,137,91,90,15,
131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,8,99,37,240,21,10,23,
190, 6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,57,177,33,
88,237,149,56,87,174,20,125,136,171,168, 68,175,74,165,71,134,139,48,27,166,
77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,55,46,245,40,244,
102,143,54, 65,25,63,161, 1,216,80,73,209,76,132,187,208, 89,18,169,200,196,
135,130,116,188,159,86,164,100,109,198,173,186, 3,64,52,217,226,250,124,123,
5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,189,28,42,
223,183,170,213,119,248,152, 2,44,154,163, 70,221,153,101,155,167, 43,172,9,
129,22,39,253, 19,98,108,110,79,113,224,232,178,185, 112,104,218,246,97,228,
251,34,242,193,238,210,144,12,191,179,162,241, 81,51,145,235,249,14,239,107,
49,192,214, 31,181,199,106,157,184, 84,204,176,115,121,50,45,127, 4,150,254,
138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180,
151,160,137,91,90,15,
131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,8,99,37,240,21,10,23,
190, 6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,57,177,33,
88,237,149,56,87,174,20,125,136,171,168, 68,175,74,165,71,134,139,48,27,166,
77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,55,46,245,40,244,
102,143,54, 65,25,63,161, 1,216,80,73,209,76,132,187,208, 89,18,169,200,196,
135,130,116,188,159,86,164,100,109,198,173,186, 3,64,52,217,226,250,124,123,
5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,189,28,42,
223,183,170,213,119,248,152, 2,44,154,163, 70,221,153,101,155,167, 43,172,9,
129,22,39,253, 19,98,108,110,79,113,224,232,178,185, 112,104,218,246,97,228,
251,34,242,193,238,210,144,12,191,179,162,241, 81,51,145,235,249,14,239,107,
49,192,214, 31,181,199,106,157,184, 84,204,176,115,121,50,45,127, 4,150,254,
138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180};

// 
// QuickSort task
//
class QuickSortTask: public Task
{
    sint* Data;
    sint Start;
    sint End;

    static FORCEINLINE void swap(int *a, int *b)
    {
      int t = *a;
      *a = *b;
      *b = t;
    }

    static void qsort(sint* Data, sint Start, sint End)
    {
        if(End > Start + 1)
        {
            int piv = Data[Start], l = Start + 1, r = End;
            while (l < r)
            {
                if (Data[l] <= piv)
                    l++;
                else
                    swap(&Data[l], &Data[--r]);
            }

            swap(&Data[--l], &Data[Start]);

            qsort(Data, Start, l);
            qsort(Data, r, End);
        }
    }

public:
    QuickSortTask(sint* _Data, sint _Start, sint _End, volatile sint* ExecCounter)
    : Task(!!ExecCounter, !ExecCounter, ExecCounter),
      Data(_Data),
      Start(_Start),
      End(_End)
    {
        if(ExecCounter)
        {
            InterlockedIncrement(ExecCounter);
        }
    }

    ~QuickSortTask()
    {}

    void Execute(Scheduler* Parent)
    {
        if((End - Start) < 65536)
        {
            qsort(Data, Start, End);
        }
        else
        {
            int piv = Data[Start], l = Start + 1, r = End;
            while (l < r)
            {
                if (Data[l] <= piv)
                    l++;
                else
                    swap(&Data[l], &Data[--r]);
            }

            swap(&Data[--l], &Data[Start]);

            if(l > Start + 1)
            {
                QuickSortTask* SubTask0 = new QuickSortTask(Data, Start, l, ExecCounter ? ExecCounter : &SyncCounter);
                
                if(Parent)
                {
                    // Parallel version
                    Parent->AddTask(SubTask0);
                }
                else
                {
                    // Serial version
                    SubTask0->Execute(NULL);
                }
            }

            if(End > r + 1)
            {
                QuickSortTask* SubTask1 = new QuickSortTask(Data, r, End, ExecCounter ? ExecCounter : &SyncCounter);

                if(Parent)
                {
                    // Parallel version
                    Parent->AddTask(SubTask1);
                }
                else
                {
                    // Serial version
                    SubTask1->Execute(NULL);
                }
            }
        }
    }

    void* operator new(size_t Size);
    void operator delete(void* Ptr);
};

// Make sure we allocate sub-tasks in a lock-free manner by using a task allocator per-thread.
THREADLS FixedSizePoolAllocator<QuickSortTask>* GFixedSizePoolAllocator = NULL;

void* QuickSortTask::operator new(size_t Size)
{
    if(!GFixedSizePoolAllocator)
    {
        GFixedSizePoolAllocator = new FixedSizePoolAllocator<QuickSortTask>;
    }
    return GFixedSizePoolAllocator->Alloc();
}

void QuickSortTask::operator delete(void* Ptr)
{
    if(!GFixedSizePoolAllocator)
    {
        GFixedSizePoolAllocator = new FixedSizePoolAllocator<QuickSortTask>;
    }
    GFixedSizePoolAllocator->Free(Ptr);
}

int _tmain(int argc, char* argv[])
{
    // Register the main-thread
    Thread::RegisterThread("MainThread");

    // Allow overriding the scheduler's default number of worker threads
    sint NumThreads = -1;

    if(argc > 1)
    {
        NumThreads = atoi(argv[1]);
    }

    // Instanciate our scheduler
    Scheduler MyScheduler(NumThreads);

    // 
    // Fibonacci sequence test
    //
    // This test is essentially there to see how the scheduler handle tasks dependencies.
    // It also verify that the results are the same on both versions (single and multi threads).
    //
    uint64 Fib_SingleThread = 0;
    uint64 Fib_MultiThreads = 0;

    printf("\nRunning fibonacci sequence tests...\n");

    // Single-threaded version
    FibonacciTask* FibTaskST = new FibonacciTask(24);

    TIMER_START(Fib_SingleThread);
    FibTaskST->Execute(NULL);
    TIMER_END(Fib_SingleThread);

    printf("  Single-thread: %.2fms\n", CyclesToMilliSeconds(Fib_SingleThread));

    // Multi-threaded version
    FibonacciTask* FibTaskMT = new FibonacciTask(24, true);
    MyScheduler.AddTask(FibTaskMT);

    TIMER_START(Fib_MultiThreads);
    FibTaskMT->Sync();
    TIMER_END(Fib_MultiThreads);

    printf("  Multi-threads: %.2fms\n", CyclesToMilliSeconds(Fib_MultiThreads));

    CHECK(FibTaskST->Result == FibTaskMT->Result);

    delete FibTaskST;
    FibTaskST = NULL;

    delete FibTaskMT;
    FibTaskMT = NULL;

    // 
    // Perlin noise test
    //
    // This test computes a 2048x2048 perlin noise with 16 octaves, and compares the results of the
    // single-threaded and multi-threaded versions to ensure they gives the exact same results.
    //
    uint64 Perlin_SingleThread = 0;
    uint64 Perlin_MultiThreads = 0;

    printf("\nRunning perlin noise tests...\n");

    // Single-threaded version
    uint8* ImageST = (uint8*)Malloc(2048*2048);
    Memset(ImageST, 0, 2048*2048);

    PerlinTask* PerlinTaskST = new PerlinTask(ImageST, 0, 0, 2048, 2048, 2048, NULL);
    
    TIMER_START(Perlin_SingleThread);
    PerlinTaskST->Execute(NULL);
    TIMER_END(Perlin_SingleThread);

    printf("  Single-thread: %.2fs\n", CyclesToMilliSeconds(Perlin_SingleThread) / 1000.0);
    
    delete PerlinTaskST;    

    // Multi-threaded version
    uint8* ImageMT = (uint8*)Malloc(2048*2048);
    Memset(ImageMT, 0, 2048*2048);

    PerlinTask* Tasks[32*32] = { 0 };
    for(sint y=0; y<32; y++)
    {
        for(sint x=0; x<32; x++)
        {
            Tasks[y*32+x] = new PerlinTask(ImageMT + y * 64 * 2048 + x * 64, x * 64, y * 64, 64, 64, 2048, Tasks[0] ? Tasks[0]->GetSyncCounter() : NULL);
        }
    }

    for(sint i=0; i<32*32; i++)
    {
        MyScheduler.AddTask(Tasks[i]);
    }

    TIMER_START(Perlin_MultiThreads);
    Tasks[0]->Sync();
    TIMER_END(Perlin_MultiThreads);

    printf("  Multi-threads: %.2fs\n", CyclesToMilliSeconds(Perlin_MultiThreads) / 1000.0);

    VERIFY(!Memcmp(ImageST, ImageMT, 2048*2048));

    Free(ImageST);
    Free(ImageMT);

    //
    // QuickSort test
    //
    // This test sorts an array of integers.
    //
    const uint32 RandomSeed = 0;

    sint64 QuickSort_SingleThread = 0;
    sint64 QuickSort_MultiThreads = 0;

    printf("\nRunning quick-sort tests...\n");

    // Single-threaded version
    sint ArrayToSortSize = 65536000;
    sint* ArrayToSort = new sint[ArrayToSortSize];

    uint32 Random = RandomSeed;
    for(sint i=0; i<ArrayToSortSize; i++)
    {
        ArrayToSort[i] = Random & 0x7fffffff;
        Random = (Random * 196314165) + 907633515;
    }

    QuickSortTask* QuickSortTaskST = new QuickSortTask(ArrayToSort, 0, ArrayToSortSize, NULL);

    TIMER_START(QuickSort_SingleThread);
    QuickSortTaskST->Execute(NULL);
    TIMER_END(QuickSort_SingleThread);

    printf("  Single-thread: %.2fs\n", CyclesToMilliSeconds(QuickSort_SingleThread) / 1000.0);

    delete QuickSortTaskST;
    QuickSortTaskST = NULL;

    for(sint i=0; i<ArrayToSortSize-1; i++)
    {
        CHECK(ArrayToSort[i] <= ArrayToSort[i+1]);
    }
    
    // Multi-threaded version
    Random = RandomSeed;
    for(sint i=0; i<ArrayToSortSize; i++)
    {
        ArrayToSort[i] = Random & 0x7fffffff;
        Random = (Random * 196314165) + 907633515;
    }

    QuickSortTask* QuickSortTaskMT = new QuickSortTask(ArrayToSort, 0, ArrayToSortSize, NULL);

    MyScheduler.AddTask(QuickSortTaskMT);

    TIMER_START(QuickSort_MultiThreads);
    QuickSortTaskMT->Sync();
    TIMER_END(QuickSort_MultiThreads);

    printf("  Multi-threads: %.2fs\n", CyclesToMilliSeconds(QuickSort_MultiThreads) / 1000.0);

    for(sint i=0; i<ArrayToSortSize-1; i++)
    {
        CHECK(ArrayToSort[i] <= ArrayToSort[i+1]);
    }

    delete QuickSortTaskMT;
    delete[] ArrayToSort;

	return 0;
}


