#pragma once

#include "windows.h"
#include "WinDef.h"
#include "Wintab.h"

bool InitWintab();
void ReleaseWintab();


typedef UINT ( API * WTINFO )  ( UINT, UINT, LPVOID );
typedef HCTX ( API * WTOPEN )  (HWND, LPLOGCONTEXTA, BOOL);
typedef BOOL ( API * WTCLOSE ) (HCTX);
typedef BOOL ( API * WTENABLE ) (HCTX, BOOL);
typedef BOOL ( API * WTOVERLAP ) (HCTX, BOOL);
typedef BOOL ( API * WTCONFIG ) (HCTX, HWND);


#undef WTInfo
#undef WTOpen
#undef WTClose
#undef WTEnable
#undef WTOverlap
#undef WTConfig

extern WTINFO WT_Info_dynamic;
extern WTOPEN WT_Open_dynamic;
extern WTCLOSE WT_Close_dynamic;
extern WTENABLE WT_Enable_dynamic;
extern WTOVERLAP WT_Overlap_dynamic;
extern WTCONFIG WT_Config_dynamic;

#define WTInfo WT_Info_dynamic
#define WTOpen WT_Open_dynamic
#define WTClose WT_Close_dynamic
#define WTEnable WT_Enable_dynamic
#define WTOverlap WT_Overlap_dynamic
#define WTConfig WT_Config_dyanmic