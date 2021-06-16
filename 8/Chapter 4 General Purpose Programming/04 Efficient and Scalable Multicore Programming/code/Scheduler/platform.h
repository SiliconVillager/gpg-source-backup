#ifndef __platform_h__
#define __platform_h__

#include "types.h"

#ifdef _MSC_VER
    #pragma warning(disable:4065) // Switch statement contains 'default' but no 'case' labels
    #pragma warning(disable:4244) // '=' : conversion from '' to '', possible loss of data.
    #pragma warning(disable:4267) // 'argument' : conversion from '' to '', possible loss of data.
    #pragma warning(disable:4291) // No matching operator delete found; memory will not be freed if initialization throws an exception.
    #pragma warning(disable:4311) // 'type cast' : pointer truncation from '' to ''.
    #pragma warning(disable:4312) // 'type cast' : conversion from '' to '' of greater size.
    #pragma warning(disable:4996) // Function  was declared deprecated.

    #define WIN32_LEAN_AND_MEAN
    #include <windows.h>
    #include <malloc.h>
    #include <stddef.h>
    #include <stdio.h>
    #include <tchar.h>
    #include <math.h>

    #define FORCEINLINE         __forceinline
    #define THREADLS            __declspec(thread)

    struct ThreadNameInfo
    {
        DWORD dwType;     // must be 0x1000
        LPCSTR szName;    // pointer to name (in user address space)
        DWORD dwThreadID; // thread ID (-1 = caller thread)
        DWORD dwFlags;    // reserved for future use, must be zero
    };

    FORCEINLINE void SetThreadName(DWORD ThreadID, const char* ThreadName)
    {
        ThreadNameInfo Info;

        Info.dwType = 0x1000;
        Info.szName = ThreadName;
        Info.dwThreadID = ThreadID;
        Info.dwFlags = 0;

        __try
        {
            RaiseException(0x406D1388, 0, sizeof(Info) / sizeof(DWORD), (DWORD*)&Info);
        }
        __except(EXCEPTION_CONTINUE_EXECUTION)
        {
        }
    }

    FORCEINLINE uint64 GetClockCycle()
    {
        LARGE_INTEGER Cycles;
        QueryPerformanceCounter(&Cycles);
        return Cycles.QuadPart;
    }

    FORCEINLINE double CyclesToMilliSeconds(uint64 Cycles)
    {
        static double SecondsPerCycle = 0;

        if(SecondsPerCycle == 0)
        {
            LARGE_INTEGER Frequency;
            QueryPerformanceFrequency(&Frequency);
            SecondsPerCycle = 1.0 / Frequency.QuadPart;
        }

        return (double)Cycles * SecondsPerCycle * 1000;
    }

    FORCEINLINE void BreakDebugger()
    {
        DebugBreak();
    }
#endif

#ifndef RETAIL
#define ASSERTS
#endif

#ifdef ASSERTS
#define CHECK(exp)          if(!(exp)) { BreakDebugger(); } else {}
#define VERIFY(exp)         if(!(exp)) { BreakDebugger(); } else {}
#else
#define CHECK(exp)          if(1) {} else {}
#define VERIFY(exp)         if(1) {(exp);} else {}
#endif

// Memory
void* Malloc(uint32 Size);
void* Realloc(void* Ptr, uint32 Size);
void Free(void* Ptr);
void Memcpy(void* Dst, const void* Src, uint32 Size);
void Memmove(void* Dst, void* Src, uint32 Size);
void Memset(void* Dst, uint8 Value, uint32 Size);
sint Memcmp(const void* BufA, const void* BufB, uint32 Size);

// Timing
#define TIMER_START(Timer)    (Timer) -= GetClockCycle();
#define TIMER_END(Timer)      (Timer) += GetClockCycle();

// Misc
FORCEINLINE sint Floor(float Value)
{
    return floorf(Value);
}

template<class Type> FORCEINLINE Type Min(const Type A, const Type B)
{
    return (A < B) ? A : B;
}

template<class Type> FORCEINLINE Type Max(const Type A, const Type B)
{
    return (A > B) ? A : B;
}

template<class Type> FORCEINLINE Type Clamp(const Type A, const Type _Min, const Type _Max)
{
    return Max(Min(A, _Max), _Min);
}

FORCEINLINE uint32 GetNextPowerOfTwo(uint32 Value)
{
    if(Value)
    {
        Value--;
        Value = (Value >> 1) | Value;
        Value = (Value >> 2) | Value;
        Value = (Value >> 4) | Value;
        Value = (Value >> 8) | Value;
        Value = (Value >> 16) | Value;
    }
    return Value + 1;
}

#define ARRAYCOUNT(A)       (sizeof(A) / sizeof(A[0]))

sint GetNumProcessors();

#endif//__platform_h__