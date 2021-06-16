//-------------------------------------------------------------------
// In Game Memory Profiler
// by Ricky Lung Man Tat (mtlung@gmail.com)


/*!	To reduce the code dependency, the use libdasm is removed.
	Actually only x86_disasm() is used in the getPrologueSize(),
	and since getPrologueSize() can be pre-obtained, therefore if USE_LIBDIASM
	is disabled, user must supply the pre-obtained prologue size to copyPrologue().
	If something wrong in function patching, try enable USE_LIBDIASM again.
	\ref http://bastard.sourceforge.net/libdisasm.html
 */
#define USE_LIBDIASM 0

#define VC_EXTRALEAN
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#undef VC_EXTRALEAN
#undef WIN32_LEAN_AND_MEAN

#if USE_LIBDIASM
#	include "libdasm/libdis.h"
#else
typedef int x86_insn_t;
typedef unsigned __int32 uint32_t;
// See the file header for documentation
unsigned int x86_disasm(unsigned char*, unsigned int, uint32_t, unsigned int, x86_insn_t*) {
	return 1;
}
void x86_oplist_free(void*) {}
#endif

/*
	More information on the implementation of this function hooking:
	http://read.pudn.com/downloads137/sourcecode/game/584894/HookClass.h__.htm
	http://divine.fi.muni.cz/darcs/branch-2.0/hoard/winhoard.cpp
 */
class FunctionPatcher
{
public:
	FunctionPatcher();

	~FunctionPatcher();

	/*!	Copy the function prologue of a function at funcAddress.
		An optional givenPrologueSize can be used if you know the
		prologue size in advance.
	 */
	void* copyPrologue(void* funcAddress, int givenPrologueSize=-1);

	void patch(void* originalFuncAddress, void* replacement);

	void UnpatchAll();
};	// FunctionPatcher

#define MAX_PROLOGUE_CODE_SIZE 64

struct PrologueInfo
{
	void* original;
	unsigned char ByteCodes[MAX_PROLOGUE_CODE_SIZE];
};

#define IAX86_NEARJMP_OPCODE 0xe9
#define JMP_CODE_SIZE 5
#define MakeIAX86Offset(to,from) ((unsigned)((char*)(to)-(char*)(from)) - JMP_CODE_SIZE)

struct ScopeChangeProtection
{
	ScopeChangeProtection(void* original)
	{
		// Change rights on orignal function memory to execute/read/write.
		::VirtualQuery((void*)original, &mbi,
			sizeof(MEMORY_BASIC_INFORMATION));
		::VirtualProtect(mbi.BaseAddress, mbi.RegionSize,
			PAGE_EXECUTE_READWRITE, &mbi.Protect);
	}

	~ScopeChangeProtection()
	{
		// Reset to original page protection.
		::VirtualProtect(mbi.BaseAddress, mbi.RegionSize,
			mbi.Protect, &mbi.Protect);
	}

	MEMORY_BASIC_INFORMATION mbi;
};	// ScopeChangeProtection

size_t getPrologueSize(void* original)
{
	int pos = 0;
	x86_insn_t insn;	// representation of the code instruction

	while(pos < JMP_CODE_SIZE) {
		pos += x86_disasm((unsigned char*)original, MAX_PROLOGUE_CODE_SIZE, 0, pos, &insn);
		x86_oplist_free(&insn);
	}

	return pos;
}

#define MAX_PROLOGUE_INFO 1024
static PrologueInfo gPrologueInfos[MAX_PROLOGUE_INFO];
static size_t PrologueFuncIdx = 0;

FunctionPatcher::FunctionPatcher()
{
#if USE_LIBDIASM
	x86_init(opt_none, NULL, NULL);
#endif

	// Change the page protection for the memory region occupied by gPrologueInfos
	for(size_t i=0; i<MAX_PROLOGUE_INFO; i+=MAX_PROLOGUE_INFO-1)
	{
		MEMORY_BASIC_INFORMATION mbi;
		::VirtualQuery(&gPrologueInfos[i], &mbi, sizeof(mbi));
		::VirtualProtect(mbi.BaseAddress, mbi.RegionSize,
			PAGE_EXECUTE_READWRITE, &mbi.Protect);
	}
}

FunctionPatcher::~FunctionPatcher()
{
#if USE_LIBDIASM
	x86_cleanup();
#endif
}

void* FunctionPatcher::copyPrologue(void* funcAddres, int givenPrologueSize)
{
	 void* result = NULL;

	 if(void* original = funcAddres)
	 {
		 size_t prologueSize = givenPrologueSize == -1 ? getPrologueSize(original) : givenPrologueSize;

		 PrologueInfo* prologueInfo = gPrologueInfos + PrologueFuncIdx++;
		 prologueInfo->original = original;

		 // Patch CRT library original routine:
		 // save original prologue code bytes for exit restoration
		 memcpy(prologueInfo->ByteCodes, original, prologueSize);

		 unsigned char* unprologuedAddress = ((unsigned char*)original) + prologueSize;
		 unsigned char* prologuedFromAddress = ((unsigned char*)prologueInfo->ByteCodes) + prologueSize;

		 unsigned char* trampolineloc = prologueInfo->ByteCodes + prologueSize;
		 *trampolineloc++ = IAX86_NEARJMP_OPCODE;
		 *(unsigned*)trampolineloc = MakeIAX86Offset(unprologuedAddress, prologuedFromAddress);

		 result = prologueInfo->ByteCodes;
	 }

	return result;
}

struct PatchInfo
{
	void* original;
	unsigned char ByteCodes[MAX_PROLOGUE_CODE_SIZE];
};

#define MAX_PATCH_INFO 1024
static PatchInfo PatchInfos[MAX_PATCH_INFO];
static size_t PatchFuncIdx = 0;

void FunctionPatcher::patch(void* originalFuncAddress, void* replacement)
{
	if(void* original = originalFuncAddress)
	{
		ScopeChangeProtection guard(original);

		PatchInfo* patchInfo = PatchInfos + PatchFuncIdx++;
		patchInfo->original = original;
		memcpy(patchInfo->ByteCodes, original, JMP_CODE_SIZE);

		// Patch CRT library original routine:
		// save original prologue code bytes for exit restoration
		unsigned char* patchloc = (unsigned char*)original;
		*patchloc++ = IAX86_NEARJMP_OPCODE;
		*(unsigned*)patchloc = MakeIAX86Offset(replacement, original);
	}
}

void FunctionPatcher::UnpatchAll()
{
	for(size_t i = 0; i < PatchFuncIdx; ++i) {
		PatchInfo* patchInfo = PatchInfos + i;
		ScopeChangeProtection guard(patchInfo->original);
		memcpy(patchInfo->original, patchInfo->ByteCodes, JMP_CODE_SIZE);
	}
}
