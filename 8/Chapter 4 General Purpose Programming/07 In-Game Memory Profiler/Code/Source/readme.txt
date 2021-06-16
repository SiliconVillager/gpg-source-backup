Build and run the "Test" project, a console program will show
the profiling result in text mode.

There are 3 tests in Main.cpp that you can choose to run, and
if you are running testMemoryProfilerTcpServer() you can startup
the C# project "ProfilerClient" which allows you to view the
profiling result in a Gui though TCP/IP.

Instead of simply hooking malloc/realloc and free as stated in the
article, the more primitive heap functions RtlAllocateHeap/RtlReAllocateHeap
and RtlFreeHeap were hooked. It should give reliable result under Windows XP
and Windows Vista.

Enjoy.