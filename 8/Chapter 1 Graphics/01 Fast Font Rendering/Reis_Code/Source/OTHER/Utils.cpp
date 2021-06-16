// Utils.cpp:
// Date: 04/14/08
// Author: Aurelio Reis

#include "Engine/Shared/Utils.h"
#include "Engine/Shared/LogFile.h"


static const uint32 g_iVaBufferSize = 32000;
static uint32 g_iVAIndex = 0;
static char g_strVAString[ 2 ][ g_iVaBufferSize ];

// Format and copy a string with variable argument into a temp buffer.
const char *AR_VarArg( const char *strFormat, ... )
{
	char *strBuffer = g_strVAString[ g_iVAIndex++ & 1 ];
	va_list arglist;

	va_start( arglist, strFormat );
#ifdef _WIN32
	_vsnprintf_s( strBuffer, g_iVaBufferSize, g_iVaBufferSize, strFormat, arglist );
#elif _IPHONE
	vsnprintf( strBuffer, g_iVaBufferSize, strFormat, arglist );
#else
	#error "Unknown platform!"
#endif
	
	va_end( arglist );

	return strBuffer;
}

void AR_MessageFunc( const char *strMsg )
{
#ifdef LOG_ENABLED
	AR_LOG->Append( strMsg );
#endif

	AR_DebugOut( strMsg );
}