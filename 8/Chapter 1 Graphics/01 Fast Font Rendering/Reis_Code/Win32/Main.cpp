//--------------------------------------------------------------------------------------
// File: Main.cpp
//
// Copyright (c) Microsoft Corporation. All rights reserved.
//--------------------------------------------------------------------------------------
#include "DXUT.h"
#include "resource.h"
#include "DXUT/Optional/DXUTgui.h"

#include "TileMap.h"
#include "StrangeAttractor.h"
#include "GuiModel.h"
#include <time.h>

#include <string.h>


//////////////////////////////////////////////////////////////////////////
// Global variable definitions
//////////////////////////////////////////////////////////////////////////

IDirect3DDevice9 *g_pd3dDevice = NULL;
DXUTDeviceSettings *g_pDeviceSettings = NULL;
CArGuiModel *g_pGuiModel = NULL;
CArPerfCounters g_PC;
double g_fTime;
float g_fElapsedTime;

bool g_bDrawStats = true;

enum ESceneState
{
	SCENE_CITY, SCENE_STRANGE
} g_Scene = SCENE_CITY;

CArGuiModel::EGeomTech g_GeomTech = CArGuiModel::GTECH_CONSTARRAYS;

CDXUTDialogResourceManager g_DialogResourceManager;
CDXUTDialog g_SampleUI;

D3DXVECTOR4 g_vTexelSize;
D3DXVECTOR4 g_vInvScreenSize;

const D3DXVECTOR2 VEC2_ZERO = D3DXVECTOR2( 0.0f, 0.0f );
const D3DXVECTOR2 VEC2_ONE = D3DXVECTOR2( 1.0f, 1.0f );

const D3DXVECTOR4 VEC4_ZERO = D3DXVECTOR4( 0.0f, 0.0f, 0.0f, 0.0f );
const D3DXVECTOR4 VEC4_ONE = D3DXVECTOR4( 1.0f, 1.0f, 1.0f, 1.0f );

bool g_bCrazyText = false;	// Draw a ton of text.
char g_strLorem[][ 128 ];


//////////////////////////////////////////////////////////////////////////
// Global function declarations
//////////////////////////////////////////////////////////////////////////

void Initialize();
void Shutdown();
void Render();


//////////////////////////////////////////////////////////////////////////
// Main
//////////////////////////////////////////////////////////////////////////

//--------------------------------------------------------------------------------------
// Rejects any D3D9 devices that aren't acceptable to the app by returning false
//--------------------------------------------------------------------------------------
bool CALLBACK IsD3D9DeviceAcceptable( D3DCAPS9* pCaps, D3DFORMAT AdapterFormat, D3DFORMAT BackBufferFormat,
                                      bool bWindowed, void* pUserContext )
{
    // Typically want to skip back buffer formats that don't support alpha blending
    IDirect3D9* pD3D = DXUTGetD3D9Object();
    if( FAILED( pD3D->CheckDeviceFormat( pCaps->AdapterOrdinal, pCaps->DeviceType,
                                         AdapterFormat, D3DUSAGE_QUERY_POSTPIXELSHADER_BLENDING,
                                         D3DRTYPE_TEXTURE, BackBufferFormat ) ) )
        return false;

    return true;
}


//--------------------------------------------------------------------------------------
// Before a device is created, modify the device settings as needed
//--------------------------------------------------------------------------------------
bool CALLBACK ModifyDeviceSettings( DXUTDeviceSettings* pDeviceSettings, void* pUserContext )
{
	g_pDeviceSettings = pDeviceSettings;

	g_vTexelSize.x = 0.5f / (float)g_pDeviceSettings->d3d9.pp.BackBufferWidth;
	g_vTexelSize.y = 0.5f / (float)g_pDeviceSettings->d3d9.pp.BackBufferHeight;

	g_vInvScreenSize.x = 2.0f / g_pDeviceSettings->d3d9.pp.BackBufferWidth;
	g_vInvScreenSize.y = 2.0f / g_pDeviceSettings->d3d9.pp.BackBufferHeight;

    return true;
}


//--------------------------------------------------------------------------------------
// Create any D3D9 resources that will live through a device reset (D3DPOOL_MANAGED)
// and aren't tied to the back buffer size
//--------------------------------------------------------------------------------------
HRESULT CALLBACK OnD3D9CreateDevice( IDirect3DDevice9* pd3dDevice, const D3DSURFACE_DESC* pBackBufferSurfaceDesc,
                                     void* pUserContext )
{
	HRESULT hr;

	g_pd3dDevice = pd3dDevice;

	V_RETURN( g_DialogResourceManager.OnD3D9CreateDevice( pd3dDevice ) );

    return S_OK;
}


//--------------------------------------------------------------------------------------
// Create any D3D9 resources that won't live through a device reset (D3DPOOL_DEFAULT) 
// or that are tied to the back buffer size 
//--------------------------------------------------------------------------------------
HRESULT CALLBACK OnD3D9ResetDevice( IDirect3DDevice9* pd3dDevice, const D3DSURFACE_DESC* pBackBufferSurfaceDesc,
                                    void* pUserContext )
{
	HRESULT hr;

	g_pd3dDevice = pd3dDevice;

	V_RETURN( g_DialogResourceManager.OnD3D9ResetDevice() );

    return S_OK;
}


//--------------------------------------------------------------------------------------
// Handle updates to the scene.  This is called regardless of which D3D API is used
//--------------------------------------------------------------------------------------
void CALLBACK OnFrameMove( double fTime, float fElapsedTime, void* pUserContext )
{
}


//--------------------------------------------------------------------------------------
// Render the scene using the D3D9 device
//--------------------------------------------------------------------------------------
void CALLBACK OnD3D9FrameRender( IDirect3DDevice9* pd3dDevice, double fTime, float fElapsedTime, void* pUserContext )
{
	g_fTime = fTime;
	g_fElapsedTime = fElapsedTime;

    HRESULT hr;

    // Clear the render target and the zbuffer 
	//V( pd3dDevice->Clear( 0, NULL, D3DCLEAR_TARGET | D3DCLEAR_ZBUFFER, D3DCOLOR_ARGB( 0, 45, 50, 170 ), 1.0f, 0 ) );
	V( pd3dDevice->Clear( 0, NULL, D3DCLEAR_TARGET | D3DCLEAR_ZBUFFER, D3DCOLOR_ARGB( 0, 0, 0, 0 ), 1.0f, 0 ) );

    // Render the scene
    if( SUCCEEDED( pd3dDevice->BeginScene() ) )
    {
		Render();

        V( pd3dDevice->EndScene() );
    }
}


//--------------------------------------------------------------------------------------
// Handle messages to the application 
//--------------------------------------------------------------------------------------
LRESULT CALLBACK MsgProc( HWND hWnd, UINT uMsg, WPARAM wParam, LPARAM lParam,
                          bool* pbNoFurtherProcessing, void* pUserContext )
{
	*pbNoFurtherProcessing = g_SampleUI.MsgProc( hWnd, uMsg, wParam, lParam );
	if( *pbNoFurtherProcessing )
		return 0;

    return 0;
}


//--------------------------------------------------------------------------------------
// Release D3D9 resources created in the OnD3D9ResetDevice callback 
//--------------------------------------------------------------------------------------
void CALLBACK OnD3D9LostDevice( void* pUserContext )
{
	g_DialogResourceManager.OnD3D9LostDevice();
}


//--------------------------------------------------------------------------------------
// Release D3D9 resources created in the OnD3D9CreateDevice callback 
//--------------------------------------------------------------------------------------
void CALLBACK OnD3D9DestroyDevice( void* pUserContext )
{
	g_DialogResourceManager.OnD3D9DestroyDevice();

//	Shutdown();
//	Initialize();
}


//--------------------------------------------------------------------------------------
// Initialize everything and go into a render loop
//--------------------------------------------------------------------------------------
INT WINAPI wWinMain( HINSTANCE, HINSTANCE, LPWSTR, int )
{
    // Enable run-time memory check for debug builds.
#if defined(DEBUG) | defined(_DEBUG)
    _CrtSetDbgFlag( _CRTDBG_ALLOC_MEM_DF | _CRTDBG_LEAK_CHECK_DF );
#endif

    // Set the callback functions
    DXUTSetCallbackD3D9DeviceAcceptable( IsD3D9DeviceAcceptable );
    DXUTSetCallbackD3D9DeviceCreated( OnD3D9CreateDevice );
    DXUTSetCallbackD3D9DeviceReset( OnD3D9ResetDevice );
    DXUTSetCallbackD3D9FrameRender( OnD3D9FrameRender );
    DXUTSetCallbackD3D9DeviceLost( OnD3D9LostDevice );
    DXUTSetCallbackD3D9DeviceDestroyed( OnD3D9DestroyDevice );
    DXUTSetCallbackDeviceChanging( ModifyDeviceSettings );
    DXUTSetCallbackMsgProc( MsgProc );
    DXUTSetCallbackFrameMove( OnFrameMove );

    // Initialize DXUT and create the desired Win32 window and Direct3D device for the application
    DXUTInit( true, true ); // Parse the command line and show msgboxes
    DXUTSetHotkeyHandling( true, true, true );  // handle the default hotkeys
    DXUTSetCursorSettings( true, true ); // Show the cursor and clip it when in full screen
    DXUTCreateWindow( L"GpuFonts" );
    //DXUTCreateDevice( true, 1920, 1024 );

	int iScreenWidth = 800, iScreenHeight = 800;
	DXUTCreateDevice( true, iScreenWidth, iScreenHeight );

    Initialize();

    // Start the render loop
    DXUTMainLoop();

    Shutdown();

    return DXUTGetExitCode();
}


//////////////////////////////////////////////////////////////////////////
// Misc.
//////////////////////////////////////////////////////////////////////////

// Send a message to the debugger.
void PrintMessage( const char *strFormat, ... )
{
	static char g_strMsgString[ 2048 ];
	va_list arglist;

	va_start( arglist, strFormat );
	_vsnprintf( g_strMsgString, 2048, strFormat, arglist );
	va_end( arglist );

	// Output to the Debugger.
	OutputDebugStringA( g_strMsgString );
}

enum 
{
	IDC_DRAWLABEL = 1,
	IDC_SCENE,
	IDC_GTECH,
	IDC_COLORBATCHES,
	IDC_DRAWSTATS,
	IDC_SA_NUMITERS,
	IDC_DRAWCRAZYTEXT,
};

void CALLBACK OnGUIEvent( UINT nEvent, int nControlID, CDXUTControl *pControl, void *pUserContext )
{
	switch( nControlID )
	{
		case IDC_SCENE:
			g_Scene = (ESceneState)(UINT)((CDXUTComboBox *)pControl)->GetSelectedData();
		break;

		case IDC_GTECH:
			g_GeomTech = (CArGuiModel::EGeomTech)(UINT)((CDXUTComboBox *)pControl)->GetSelectedData();
			g_pGuiModel->SetGeomTechnique( g_GeomTech );
		break;

		case IDC_COLORBATCHES:
			g_pGuiModel->SetDebugShowBatchColors( !g_pGuiModel->GetDebugShowBatchColors() );
		break;

		case IDC_DRAWSTATS:
			g_bDrawStats = !g_bDrawStats;
		break;

		case IDC_SA_NUMITERS:
			g_StrangeAttractor.SetNumIters( (int)((CDXUTSlider *)pControl)->GetValue() );
		break;

		case IDC_DRAWCRAZYTEXT:
			g_bCrazyText = !g_bCrazyText;
		break;
	}
}

void Initialize()
{
	srand( (unsigned int)time( NULL ) );
	timeBeginPeriod( 1 );

	g_pGuiModel = new CArGuiModel();

	g_TextureManager.Initialize();

	CArTextureAtlas *pAtlas = new CArTextureAtlas();
	pAtlas->LoadAtlas( "Data/Atlases/Tiles" );
	g_TextureManager.AddAtlas( pAtlas );

	pAtlas = new CArTextureAtlas();
	pAtlas->LoadAtlas( "Data/Atlases/Cars" );
	g_TextureManager.AddAtlas( pAtlas );

	g_pGuiModel->Initialize();

	// TESTING:
	//g_TextureManager.SetUseAtlasVirtualTextures( false );

	g_StrangeAttractor.Create();
	g_Map.Create();

	//////////////////////////////////////////////////////////////////////////
	// Make the real GUI.
	//////////////////////////////////////////////////////////////////////////

	g_SampleUI.Init( &g_DialogResourceManager );
	g_SampleUI.SetCallback( OnGUIEvent );

	int iScreenWidth = g_pDeviceSettings->d3d9.pp.BackBufferWidth;
	//int iScreenHeight = g_pDeviceSettings->d3d9.pp.BackBufferHeight;

	int x = iScreenWidth - 200;
	int y = 8;
	int ydelta = 34;

	g_SampleUI.AddStatic( IDC_SCENE, L"Scene", x - 50, y, 56, 22 );
	g_SampleUI.AddComboBox( IDC_SCENE, x, y, 160, 22 );
	y += ydelta;
	g_SampleUI.GetComboBox( IDC_SCENE )->AddItem( L"City", (LPVOID)SCENE_CITY );
	g_SampleUI.GetComboBox( IDC_SCENE )->AddItem( L"Strange Attractors", (LPVOID)SCENE_STRANGE );

	g_SampleUI.AddStatic( IDC_SCENE, L"Technique", x - 62, y, 56, 22 );
	g_SampleUI.AddComboBox( IDC_GTECH, x, y, 160, 22 );
	y += ydelta;
	g_SampleUI.GetComboBox( IDC_GTECH )->AddItem( L"Buffers", (LPVOID)CArGuiModel::GTECH_BUFFERS );
	g_SampleUI.GetComboBox( IDC_GTECH )->AddItem( L"Streams", (LPVOID)CArGuiModel::GTECH_STREAMS );
	g_SampleUI.GetComboBox( IDC_GTECH )->AddItem( L"Geometry Instancing", (LPVOID)CArGuiModel::GTECH_INSTANCING );
	g_SampleUI.GetComboBox( IDC_GTECH )->AddItem( L"Const Array Instancing", (LPVOID)CArGuiModel::GTECH_CONSTARRAYS_INST );
	g_SampleUI.GetComboBox( IDC_GTECH )->AddItem( L"Const Array", (LPVOID)CArGuiModel::GTECH_CONSTARRAYS );
	g_SampleUI.GetComboBox( IDC_GTECH )->SetSelectedByIndex( 4 );

	g_SampleUI.AddCheckBox( IDC_COLORBATCHES, L"Color Batches", x, y, 135, 22, g_pGuiModel->GetDebugShowBatchColors() );
	y += ydelta;

	g_SampleUI.AddCheckBox( IDC_DRAWSTATS, L"Draw Stats", x, y, 135, 22, g_bDrawStats );
	y += ydelta;

	g_SampleUI.AddStatic( IDC_SCENE, L"Strange Attractor Points", x - 170, y, 200, 22 );
	g_SampleUI.AddSlider( IDC_SA_NUMITERS, x, y, 170, 22, 100, 300000, 100000 );
	y += ydelta;

	g_SampleUI.AddCheckBox( IDC_DRAWCRAZYTEXT, L"Draw Text Stress Test", x, y, 135, 22, g_bCrazyText );
	y += ydelta;
}

void Shutdown()
{
	g_StrangeAttractor.Destroy();
	g_Map.Destroy();
	if ( g_pGuiModel )
	{
		g_pGuiModel->Destroy();
		SAFE_DELETE( g_pGuiModel );
	}
	g_TextureManager.Shutdown();

	timeEndPeriod( 1 );
}

void Render()
{
	DWORD dwStartTime = timeGetTime();

	g_PC.FrameReset();

	switch ( g_Scene )
	{
		case SCENE_CITY:
			g_Map.Draw();
		break;

		case SCENE_STRANGE:
			g_StrangeAttractor.Draw();
		break;
	}

	if ( g_bCrazyText )
	{
		for ( int i = 0; i < 2; ++i )
		{
			int x = 2 - i * 5;
			int y = 2 + i * 4, iDeltaY = 22;
			int iCurTxt = 0;
			const char *strText = (const char *)g_strLorem[ iCurTxt ];

			for ( ; NULL != *strText; iCurTxt++, strText = g_strLorem[ iCurTxt ] )
			{
				int realx = 40 + int( sin( (double)x + g_fTime ) * 100.0 ) + x;
				int realy = 40 + int( cos( (double)y + g_fTime ) * 100.0 ) + y;

				g_pGuiModel->DrawText( &g_Map.m_Font, strText + i * 2, realx, realy, D3DXCOLOR( 1.0f, 1.0f, 1.0f, 1.0f ), 22.0f );

				x = ( iCurTxt % 8 ) + iCurTxt * 2 + i * 10;
				y = ( iCurTxt % 36 ) * iDeltaY + i;
			}
		}
	}
	
	g_pGuiModel->Render();

	//////////////////////////////////////////////////////////////////////////
	// Draw stats.
	//////////////////////////////////////////////////////////////////////////

	if ( g_bDrawStats )
	{
		DWORD dwTimeDif = timeGetTime() - dwStartTime;
		float fFPS = ( 1000.0f / float( dwTimeDif < 1 ? dwTimeDif = 1 : dwTimeDif ) );

		int y = 2, iDeltaY = 30;
		g_pGuiModel->DrawText( &g_Map.m_Font, VarArg( "Num Batches Drawn: %d", g_PC.m_iNumBatchsDrawn ), 0, y, D3DXCOLOR( 1.0f, 1.0f, 1.0f, 1.0f ), 32.0f );
		y += iDeltaY;
		g_pGuiModel->DrawText( &g_Map.m_Font, VarArg( "Num Tris Drawn: %d", g_PC.m_iNumTrisDrawn ), 0, y, D3DXCOLOR( 1.0f, 1.0f, 1.0f, 1.0f ), 32.0f );
		y += iDeltaY;
		g_pGuiModel->DrawText( &g_Map.m_Font, VarArg( "Num Verts Drawn: %d", g_PC.m_iNumVerticesDrawn ), 0, y, D3DXCOLOR( 1.0f, 1.0f, 1.0f, 1.0f ), 32.0f );
		y += iDeltaY;
		g_pGuiModel->DrawText( &g_Map.m_Font, VarArg( "Num Quads Drawn: %d", g_PC.m_iNumQuadsDrawn ), 0, y, D3DXCOLOR( 1.0f, 1.0f, 1.0f, 1.0f ), 32.0f );
		y += iDeltaY;
		g_pGuiModel->DrawText( &g_Map.m_Font, VarArg( "Render Time: %d ms", g_PC.m_iRenderMs ), 0, y, D3DXCOLOR( 1.0f, 1.0f, 1.0f, 1.0f ), 32.0f );
		y += iDeltaY;
		g_pGuiModel->DrawText( &g_Map.m_Font, VarArg( "Frame Rate: %2.2f fps", fFPS ), 0, y, D3DXCOLOR( 1.0f, 1.0f, 1.0f, 1.0f ), 32.0f );

		g_pGuiModel->Render();
	}

	g_SampleUI.OnRender( g_fElapsedTime );
}

void MakeFilePathCanonical( string &strFilePath )
{
	//////////////////////////////////////////////////////////////////////////
	// No backslashes and only lowercase.
	//////////////////////////////////////////////////////////////////////////

	for ( size_t i = 0; i < strFilePath.size(); ++i )
	{
		if ( strFilePath[ i ] == '\\' )
		{
			strFilePath[ i ] = '/';
		}

		strFilePath[ i ] = (char)tolower( strFilePath[ i ] );
	}
}

char g_strLorem[][ 128 ] =
{
	{ "Lorem ipsum dolor sit amet, consectetur adipiscing elit." },
	{ "Ut sed velit id sapien ullamcorper viverra." },
	{ "Proin in lorem ac eros facilisis porta." },
	{ "Nam adipiscing est ac dui varius mollis." },
	{ "Aliquam ornare turpis eu sapien ullamcorper vel ultrices eros adipiscing." },
	{ "Etiam accumsan est vel enim auctor id vulputate massa euismod." },
	{ "Sed nec lectus est, a lobortis ipsum." },
	{ "Aenean dictum rutrum tortor, vitae tincidunt metus ultricies a." },
	{ "Mauris in eros metus, et fringilla dui." },
	{ "Nam id enim ac metus elementum vestibulum a sed ligula." },
	{ "Curabitur vehicula bibendum lacus, sit amet accumsan elit commodo in." },
	{ "Pellentesque eget tellus eu enim sagittis tempor." },
	{ "Proin dapibus neque vitae orci adipiscing id ultricies diam interdum." },
	{ "Vivamus quis lacus felis, eget consectetur ante." },
	{ "Quisque a libero vitae velit hendrerit posuere." },
	{ "Sed ultricies metus ut sem scelerisque non auctor velit tristique." },
	{ "Fusce nec diam a metus viverra ultricies." },
	{ "Integer vitae arcu metus, vitae pulvinar felis." },
	{ "Duis ultrices felis vel metus ullamcorper molestie at a metus." },
	{ "Fusce eleifend arcu sed odio posuere ac lacinia tellus tempor." },
	{ "Nam non felis est, non venenatis ipsum." },
	{ "Suspendisse volutpat magna mattis elit imperdiet vel tristique justo laoreet." },
	{ "Donec in enim tristique turpis egestas semper." },
	{ "Proin bibendum massa a mauris ornare ac elementum erat pellentesque." },
	{ "Mauris vulputate viverra ligula, vel gravida enim posuere vel." },
	{ "Nullam ut neque nibh, quis hendrerit urna." },
	{ "Phasellus et odio tellus, et eleifend eros." },
	{ "Ut quis ligula mi, sed consectetur magna." },
	{ "Praesent suscipit ipsum non arcu posuere ullamcorper mattis neque tempor." },
	{ "Nunc dignissim ante sit amet nulla viverra vitae volutpat odio blandit." },
	{ "Vestibulum convallis est eu arcu tempus non dignissim elit eleifend." },
	{ "Proin dignissim nulla nec neque imperdiet quis vestibulum velit blandit." },
	{ "Aliquam vitae lorem ut nisi molestie lacinia ac sit amet nibh." },
	{ "Praesent pretium aliquam sapien, nec aliquet nibh congue vitae." },
	{ "Praesent ut nunc et nulla auctor aliquet eu vel lorem." },
	{ "Pellentesque in metus quis metus tempus bibendum." },
	{ "Nam luctus augue vel orci lobortis congue." },
	{ "Donec quis ante nunc, et pharetra magna." },
	{ "Proin condimentum neque nec turpis lacinia luctus." },
	{ "Integer elementum libero a dolor malesuada ullamcorper." },
	{ "Sed porta fringilla ante, consectetur congue sapien egestas et." },
	{ "Integer in ligula sed risus vestibulum ullamcorper eu nec lorem." },
	{ "Praesent non elit id nunc vestibulum congue non id orci." },
	{ "Donec eget magna ipsum, eu convallis lacus." },
	{ "Cras et purus nec eros sollicitudin porttitor rutrum nec est." },
	{ "Proin convallis dolor eget ligula tempor nec luctus lorem tincidunt." },
	{ "Duis tincidunt eros et metus cursus ac commodo eros condimentum." },
	{ "Proin in ipsum justo, a feugiat lorem." },
	{ "Suspendisse vel metus felis, ut auctor nulla." },
	{ "Cras convallis dapibus dui, sit amet vulputate purus faucibus eu." },
	{ "Proin non nisl a erat adipiscing hendrerit quis quis diam." },
	{ "Nulla cursus sagittis ipsum, vitae congue felis mollis quis." },
	{ "Vestibulum ac eros in dolor ultrices adipiscing." },
	{ "Quisque faucibus dolor gravida augue rutrum suscipit." },
	{ "Donec elementum velit a quam euismod euismod molestie dui ultrices." },
	{ "Ut pellentesque nisi ac ipsum aliquet vel pellentesque felis fermentum." },
	{ "Ut pellentesque lacus non eros lobortis fermentum." },
	{ "Maecenas adipiscing tempor leo, porttitor iaculis ante sodales a." },
	{ "Mauris non augue turpis, in condimentum purus." },
	{ "Vivamus interdum arcu id tortor rutrum sit amet auctor erat imperdiet." },
	{ "Aenean sit amet ligula nisl, id ullamcorper orci." },
	{ "Integer rutrum risus nec purus congue auctor quis ac nulla." },
	{ "Cras eu eros non dolor scelerisque dictum." },
	{ "Maecenas tristique tincidunt massa, vel ornare purus rutrum sit amet." },
	{ "Donec porttitor dui at nibh rhoncus at placerat elit faucibus." },
	{ "Aenean a lacus eget ligula vestibulum dignissim et at justo." },
	{ "Ut vel metus vel diam pretium scelerisque." },
	{ "Duis at orci vitae magna blandit molestie a ac leo." },
	{ "Sed vulputate turpis a nisl condimentum quis posuere leo varius." },
	{ "Sed facilisis mattis dui, eget rutrum mi consectetur vitae." },
	{ "Maecenas consectetur justo et erat gravida euismod." },
	{ "Ut vel ligula lectus, quis ultrices sem." },
	{ "Aliquam nec odio nec dui vestibulum semper." },
	{ "Vestibulum eu eros nec erat tempus ultricies." },
	{ "Donec nec elit nibh, nec aliquam sem." },
	{ "Aliquam eu dolor et magna tincidunt auctor quis et velit." },
	{ "Aenean sodales sem ac eros fringilla scelerisque in ac ligula." },
	{ "Pellentesque sit amet felis eu elit bibendum sollicitudin vehicula in felis." },
	{ "Sed lobortis augue nec libero laoreet rhoncus." },
	{ "Ut fringilla placerat diam, sit amet cursus augue dictum nec." },
	{ "Quisque quis sem nec diam tempor hendrerit in vel elit." },
	{ "Nam adipiscing mi id turpis ornare blandit." },
	{ "Proin vitae erat mauris, vitae molestie massa." },
	{ "Proin id nibh vel mi ultricies tincidunt." },
	{ "Phasellus sagittis lectus diam, id consectetur nibh." },
	{ "Morbi imperdiet odio ut ligula pulvinar sit amet commodo elit consequat." },
	{ "Donec vel lorem et turpis suscipit bibendum quis vel risus." },
	{ "Proin porta suscipit mauris, ut ultrices sapien adipiscing semper." },
	{ "Cras accumsan euismod tortor, in luctus ipsum molestie sit amet." },
	{ "Donec aliquam commodo diam, a laoreet ante sollicitudin eu." },
	{ "Sed consequat varius purus, aliquam dictum nunc tempor id." },
	{ "Sed sit amet dui at sapien vestibulum dapibus vel sit amet est." },
	{ "Phasellus luctus justo sit amet justo porta gravida." },
	{ "Suspendisse pellentesque neque sed leo iaculis faucibus." },
	{ "Vestibulum et diam enim, et rhoncus lectus." },
	{ "Vivamus elementum nunc vitae leo pharetra ac sagittis orci porttitor." },
	{ "Aliquam eu orci vitae ante commodo auctor nec vitae augue." },
	{ "Vivamus cursus sapien non purus elementum a rutrum mi dictum." },
	{ "Mauris ut nulla nec nisl feugiat ornare." },
	{ "Donec non nisl purus, a fringilla turpis." },
	{ "Quisque commodo urna ut metus consectetur et fermentum neque placerat." },
	{ "Fusce hendrerit posuere dolor, sed laoreet enim sagittis sit amet." },
	{ "Mauris et tortor quis mauris feugiat luctus non nec nibh." },
	{ "Proin vel nisi eget purus porta commodo quis nec lorem." },
	{ "Maecenas semper fermentum sapien, at dictum sapien tempor eu." },
	{ "Nulla eu massa tellus, a lacinia magna." },
	{ "Maecenas eleifend elit a erat accumsan tempor." },
	{ "Donec at est et dui dictum euismod." },
	{ "Ut lacinia vulputate erat, bibendum sagittis quam eleifend quis." },
	{ "Nullam bibendum lacus vel mi cursus bibendum." },
	{ "Donec mollis est eu ipsum sollicitudin tincidunt." },
	{ "Duis sit amet tellus odio, eget fringilla libero." },
	{ "Nunc mattis consectetur lacus, quis volutpat est dignissim eget." },
	{ "Proin ullamcorper erat eu mauris mollis egestas." },
	{ "Vestibulum adipiscing placerat eros, eu mollis leo lobortis vel." },
	{ "Mauris vel eros neque, id accumsan lacus." },
	{ "Sed nec mi urna, eget mollis elit." },
	{ NULL }
};