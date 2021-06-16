//-------------------------------------------------------------------
// In Game Memory Profiler
// by Ricky Lung Man Tat (mtlung@gmail.com)


#include <crtdbg.h>

extern void testCpuProfiler();
extern void testMemoryProfiler();
extern void testMemoryProfilerTcpServer();

int main(int argc, char* argv[])
{
#ifdef _MSC_VER
	// Tell the c-run time to do memory check at program shut down
	_CrtSetDbgFlag(_CRTDBG_ALLOC_MEM_DF | _CRTDBG_LEAK_CHECK_DF);
	_CrtSetBreakAlloc(-1);
#endif

	// Choose the test you want to run
//	testCpuProfiler();
//	testMemoryProfiler();
	testMemoryProfilerTcpServer();

	return 0;
}
