#ifndef ASSERT_H
#define ASSERT_H

//This class is designed to provide the functions required to print the stack out at runtime
//without having to use a debugger.  Useful for catching bad data being sent to a function.
//Even if you handle the bad data, you may still want to know why it's in your program.

//Things to note if you are compiling this source code directly:
// 1) Make sure you are using MultiByte chars and not Unicode
// 2) Make sure to include dbghelp.lib
#if DEBUG


#include <Windows.h>
#include <DbgHelp.h>
#include <string>
class Assert
{
public:
	Assert();
	~Assert();
	void PrintStack();
private:
	//dissallow copying
	Assert* operator=( const Assert& );

	void GetContext( CONTEXT& context );
	void GetStackFrame( STACKFRAME64& stackframe, CONTEXT& context );
	void PrintLineNumber( HANDLE& process, DWORD64& pcOffset, char* line);
	void PrintFuncName( HANDLE& process, DWORD64& pcOffset, char* line );
	void LoadDLL();
	void ReleaseDLL();

	HMODULE m_dllHandle;

	//function declarations
	typedef bool (__stdcall *SymInitialize) ( 
							HANDLE hProcess, 
							PCTSTR UserSearchPath, 
							bool fInvadeProcess );

	typedef bool (__stdcall *StackWalk64) (	
							DWORD MachineType, 
							HANDLE hProcess, 
							HANDLE hThread, 
							LPSTACKFRAME64 StackFrame, 
							PVOID ContextRecord, 
							PREAD_PROCESS_MEMORY_ROUTINE64 ReadMemoryRoutine, 
							PFUNCTION_TABLE_ACCESS_ROUTINE64 FunctionTableAccessRoutine, 
							PGET_MODULE_BASE_ROUTINE64 GetModuleBaseRoutine, 
							PTRANSLATE_ADDRESS_ROUTINE64 TranslateAddress );

	typedef bool (__stdcall *SymGetLineFromAddr64) (
									HANDLE hProcess,
									DWORD64 dwAddr,
									PDWORD pdwDisplacement,
									PIMAGEHLP_LINE64 Line);

	typedef bool (__stdcall *SymGetSymFromAddr64)(	
									HANDLE hProcess,
									DWORD64 Address,
									PDWORD64 Displacement,
									PIMAGEHLP_SYMBOL64 Symbol );

	typedef DWORD (__stdcall *SymGetOptions) ( void );

	typedef DWORD (__stdcall *SymSetOptions) ( DWORD SymOptions );

	//function pointers
	SymInitialize        m_pSymInitialize;
	StackWalk64          m_pStackWalk64;
	SymGetLineFromAddr64 m_pSymGetLineFromAddr64;
    SymGetSymFromAddr64  m_pSymGetSymFromAddr64;
    SymGetOptions        m_pSymGetOptions;
	SymSetOptions        m_pSymSetOptions;
};

// One way of creating a singleton.  This is not good if you are trying to 
// use Asserts in a multithreaded program since it doesn't lock out
// access to the variable.
class AssertWrapper
{
	public:
		static Assert a;
};


	#define ASSERT( expression )               \
		if ( !expression )                     \
		{                                      \
			printf( "ASSERT[" #expression "]\n" ); \
			AssertWrapper::a.PrintStack();         \
			printf( "\n\nPress enter to continue:\n" );\
			getchar();\
		}

#else
	//If we aren't in a debug configuration then just compile out Asserts
	#define ASSERT( expression )

#endif //DEBUG

#endif //ASSERT_H
