#include "Assert.h"

#if DEBUG

#include <iostream>
using namespace std;

Assert AssertWrapper::a;

const size_t OUTPUTSTRINGSIZE = 32;

Assert::Assert()
{
	m_dllHandle = NULL;

	m_pSymInitialize        = NULL;
	m_pStackWalk64          = NULL;
	m_pSymGetLineFromAddr64 = NULL;
    m_pSymGetSymFromAddr64  = NULL;
    m_pSymGetOptions        = NULL;
	m_pSymSetOptions        = NULL;

	LoadDLL();
}

Assert::~Assert()
{
	ReleaseDLL();
}

void Assert::PrintStack()
{
	//get the process and thread of this program (for single thread only)
	HANDLE process = GetCurrentProcess();
	HANDLE thread  = GetCurrentThread();

	//get the context for the current process (don't put this in a seperate function or you will corrupt the context )
	CONTEXT context;
	memset( &context, 0, sizeof(CONTEXT));	
 	context.ContextFlags = CONTEXT_FULL;

	//next we have to get the Instruction pointer, Frame pointer, and the Stack pointer so we know where to the 
	//RTSI from.
	//the following assembly will differ depending on which processor type you are using.  Again this is x86 specific.
	//AMD processors will use the RIP(program counter), RBP, RSP registers.
	//(x86 processors can't access the PC directly so we have to do a round about method, which is the pop EAX.)
	__asm    call x
	__asm    x: pop eax
	__asm    mov context.Eip, eax //copy the instruction pointer using an indirect method
    __asm    mov context.Ebp, ebp //copy the frame pointer
    __asm    mov context.Esp, esp //copy the stack pointer

	STACKFRAME64 frame;
	GetStackFrame( frame, context );

	//Init the symbol loading structure and pass in true so it loads the modules itself.
	//In this example we aren't concerned about performance so we let the function 
	//call SymLoadModule64 by passing in true for the third parameter.  If you are
	//concerned about memory/speed, you'll need to handle this yourself.
	this->m_pSymInitialize( process, NULL , true );

	//setup the options - the important one here is to get the line numbers for the function calls.
	DWORD options = this->m_pSymGetOptions();
	options |= SYMOPT_LOAD_LINES;
    options |= SYMOPT_FAIL_CRITICAL_ERRORS;
	this->m_pSymSetOptions( options );
	
	unsigned int i = 0;
	bool success = false;
	do 
	{
		//get the next stack frame info
		success = this->m_pStackWalk64(	IMAGE_FILE_MACHINE_I386,
								process,
								thread,
								&frame,
								(void*)&context,
								NULL,
								SymFunctionTableAccess64,
								SymGetModuleBase64,
								NULL );

		//now we try to print the current frame's info
		if ( success && frame.AddrReturn.Offset != 0 && i != 0 )
		{
			char line1[OUTPUTSTRINGSIZE];
			char line2[OUTPUTSTRINGSIZE];
			memset( line1, '\0', OUTPUTSTRINGSIZE );
			memset( line2, '\0', OUTPUTSTRINGSIZE );
			PrintLineNumber( process, frame.AddrPC.Offset, line2 );
			PrintFuncName( process, frame.AddrPC.Offset, line1 );
			printf( "%-32s || %s\n", line1, line2 );
		}
		i++;
	} while( success && frame.AddrReturn.Offset != 0 );
}

//This gets the program counter, stack pointer and frame pointer.
//The pointers are specific to the processor you are using. This is for
//x86 processors. If you need the x64 variables then look up STACKFRAME64
//on msdn.
void Assert::GetStackFrame( STACKFRAME64& stackframe, CONTEXT& context )
{
	memset( &stackframe, 0, sizeof(STACKFRAME64));
	stackframe.AddrPC.Offset    = context.Eip;
	stackframe.AddrFrame.Offset = context.Ebp;
	stackframe.AddrStack.Offset = context.Esp;

	stackframe.AddrPC.Mode      = AddrModeFlat;
	stackframe.AddrFrame.Mode   = AddrModeFlat;
	stackframe.AddrStack.Mode   = AddrModeFlat;
}

//Prints out the actual line in code that the function was called from.
void Assert::PrintLineNumber( HANDLE& process, DWORD64& pcOffset, char* line )
{
	DWORD lineOffset = 0;
	IMAGEHLP_LINE64 stackLine;
	memset( &stackLine, 0, sizeof(IMAGEHLP_LINE64) );
	stackLine.SizeOfStruct = sizeof(IMAGEHLP_LINE64);
	stackLine.FileName = (char*)malloc( 128*sizeof(char) );

	bool success = this->m_pSymGetLineFromAddr64( process, pcOffset, &lineOffset, &stackLine ); 
	unsigned int lastSlashIndex = 0;
	unsigned int i = 0;
	while( success && stackLine.FileName[i] != '\0' )
	{
		if ( stackLine.FileName[i] == '\\' )
			lastSlashIndex = i;
		i++;
	}
	if( success )
	{
		sprintf_s( line, OUTPUTSTRINGSIZE, "%s(%d)", &stackLine.FileName[lastSlashIndex+1], stackLine.LineNumber );
	}
	else
	{
		line[0] = 0;
	}

}

//Print the function name.  Doesn't print the arguments.
void Assert::PrintFuncName( HANDLE& process, DWORD64& pcOffset, char* line )
{
	DWORD64 lineOffset = 0;
	IMAGEHLP_SYMBOL64 *pSym = NULL;
	pSym = (IMAGEHLP_SYMBOL64 *) malloc(sizeof(IMAGEHLP_SYMBOL64) + 1024);
	memset(pSym, 0, sizeof(IMAGEHLP_SYMBOL64) + 1024);
	pSym->SizeOfStruct = sizeof(IMAGEHLP_SYMBOL64);
	pSym->MaxNameLength = 1024;

	bool success = this->m_pSymGetSymFromAddr64( process, pcOffset, &lineOffset, pSym );
	if( success )
	{
		sprintf_s( line, OUTPUTSTRINGSIZE, "%s", pSym->Name );
	}
	else
	{
		line[0] = 0;
	}
}

void Assert::LoadDLL()
{
	//load the dll
	m_dllHandle = LoadLibrary( "dbghelp.dll" );
	//get pointers to all the functions you need
	m_pSymInitialize        = (SymInitialize)        GetProcAddress( m_dllHandle, "SymInitialize" );
	m_pStackWalk64          = (StackWalk64)          GetProcAddress( m_dllHandle, "StackWalk64" );
	m_pSymGetLineFromAddr64 = (SymGetLineFromAddr64) GetProcAddress( m_dllHandle, "SymGetLineFromAddr64" );
    m_pSymGetSymFromAddr64  = (SymGetSymFromAddr64)  GetProcAddress( m_dllHandle, "SymGetSymFromAddr64" );
    m_pSymGetOptions        = (SymGetOptions)        GetProcAddress( m_dllHandle, "SymGetOptions" );
	m_pSymSetOptions        = (SymSetOptions)        GetProcAddress( m_dllHandle, "SymSetOptions" );
}

void Assert::ReleaseDLL()
{
	FreeLibrary( m_dllHandle );

	m_pSymInitialize        = NULL;
	m_pStackWalk64          = NULL;
	m_pSymGetLineFromAddr64 = NULL;
    m_pSymGetSymFromAddr64  = NULL;
    m_pSymGetOptions        = NULL;
	m_pSymSetOptions        = NULL;
}

#endif