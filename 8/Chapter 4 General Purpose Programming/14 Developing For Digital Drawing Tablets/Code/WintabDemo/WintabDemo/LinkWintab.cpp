
#include "LinkWintab.h"

WTINFO WT_Info_dynamic;
WTOPEN WT_Open_dynamic;
WTCLOSE WT_Close_dynamic;
WTENABLE WT_Enable_dynamic;
WTOVERLAP WT_Overlap_dynamic;
WTCONFIG WT_Config_dynamic;

HINSTANCE ghWintab;

bool InitWintab() {
    ghWintab = LoadLibrary( "Wintab32.dll" );
	if ( !ghWintab )
	{
		return false;
	}

	WT_Info_dynamic = ( WTINFO )GetProcAddress( ghWintab, "WTInfoA" );
	if ( !WT_Info_dynamic )
	{
		return FALSE;
	}

    return true;
}


void ReleaseWintab() {
    if ( ghWintab )
	{
		FreeLibrary( ghWintab );
	}
}