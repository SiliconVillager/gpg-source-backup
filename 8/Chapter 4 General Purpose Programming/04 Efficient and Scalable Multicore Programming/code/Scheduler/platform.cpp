#include "platform.h"
#include <malloc.h>

// Memory
void* Malloc(uint32 Size)
{
    return malloc(Size);
}

void* Realloc(void* Ptr, uint32 Size)
{
    return realloc(Ptr, Size);
}

void Free(void* Ptr)
{
    free(Ptr);
}

void Memcpy(void* Dst, const void* Src, uint32 Size)
{
    memcpy(Dst, Src, Size);
}

void Memmove(void* Dst, void* Src, uint32 Size)
{
    memmove(Dst, Src, Size);
}

void Memset(void* Dst, uint8 Value, uint32 Size)
{
    memset(Dst, Value, Size);
}

sint Memcmp(const void* BufA, const void* BufB, uint32 Size)
{
    return memcmp(BufA, BufB, Size);
}

sint GetNumProcessors()
{
#if defined(_MSC_VER)
    SYSTEM_INFO SI;
    GetSystemInfo(&SI);
    return SI.dwNumberOfProcessors;
#else
    return 1;
#endif
}
