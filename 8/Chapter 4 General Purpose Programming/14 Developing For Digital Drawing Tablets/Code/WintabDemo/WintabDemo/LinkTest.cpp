#include "windows.h"
#include "Wintab.h"
#include "LinkWintab.h"

int WINAPI WinMain( HINSTANCE hInstance, HINSTANCE hPrevInstance, 
                   LPSTR lpCmdLine, int nShowCmd ) {
    InitWintab();
    WTInfo( 0, 0, NULL );    

    ReleaseWintab();

    return 0;
}