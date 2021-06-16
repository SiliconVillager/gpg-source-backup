#include <windows.h>
#include <d3dx9.h>
#include "log.h"
#include "height_map.h"
#include "terrain.h"
#include "map_gen.h"
#include "camera.h"
#include "http_object.h"

#define SCREEN_SIZE_X 800
#define SCREEN_SIZE_Y 600

class MainWindow
{
	public:
		MainWindow(int argc, char** argv);
		HRESULT Init(HINSTANCE hInstance, int width, int height, bool windowed, FileQueueManager* queueMgr);
		HRESULT Update(float deltaTime);
		HRESULT Render();
		HRESULT Cleanup();
		HRESULT Quit();
		bool MapGenMode() {return m_mapGen;}
		void UpdateMiniMap();
		void RenderMiniMap(RECT dest);
		void ParseCmdLine(int argc, char** argv);

		int m_Mbps;  // bandwidth cap
		char m_hostName[MAX_PATH];

	private:
		IDirect3DDevice9* m_pDevice; 
		Terrain m_terrain;
		Camera m_camera;

		int m_argc;
		char** m_argv;
		bool m_wireframe;
		bool m_drawMinimap;
		bool m_mapGen; 
		FileObject::DataSource m_dataSource; 
		FileQueueManager* m_queueMgr;
		char m_dataPath[MAX_PATH];
		DWORD m_time, m_snapTime;
		int m_fps, m_lastFps;
		HWND m_mainWindow;
		ID3DXFont *m_pFont;
		ID3DXLine *m_pLine;

		ID3DXSprite *m_pSprite;
		IDirect3DTexture9 *m_pMiniMap;
		IDirect3DTexture9 *m_pMiniMapBorder;
		RECT m_miniMapRect;
};

void ParseWinMainCmdLine(PSTR cmdLine, int& argc, char**& argv)
{
	char*  arg = (char *)cmdLine;
	int    index;

	// count the arguments
	argc = 1;
	while (arg[0] != 0) {
		while (arg[0] != 0 && arg[0] == ' ') {
			arg++;
		}
		if (arg[0] != 0) {
			argc++;
			while (arg[0] != 0 && arg[0] != ' ') {
				arg++;
			}
		}
	}    
	// tokenize the arguments
	argv = (char**)malloc(argc * sizeof(char*));
	arg = cmdLine;
	index = 1;

	while (arg[0] != 0) {
		while (arg[0] != 0 && arg[0] == ' ') {
			arg++;
		}
		if (arg[0] != 0) {

			argv[index] = arg;
			index++;
			while (arg[0] != 0 && arg[0] != ' ') {
				arg++;
			}
			if (arg[0] != 0) {
				arg[0] = 0;    
				arg++;
			}
		}
	}    

	// put the program name into argv[0]
	char* filename = (char*) malloc(MAX_PATH);
	GetModuleFileName(NULL, filename, MAX_PATH);
	argv[0] = filename;
}

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE prevInstance, PSTR cmdLine, int showCmd)
{
	int argc;
	char** argv;

	ParseWinMainCmdLine(cmdLine, argc, argv);

    MainWindow mainWindow(argc, argv);

	HttpObject::InitHTTPSession(mainWindow.m_hostName);
	FileQueueManager* queueMgr = new FileQueueManager(mainWindow.m_Mbps); 
	
	if (FAILED(mainWindow.Init(hInstance, SCREEN_SIZE_X, SCREEN_SIZE_Y, true, queueMgr)))
		return 0;

	MSG msg;
	memset(&msg, 0, sizeof(MSG));
	int startTime = timeGetTime(); 

	while(msg.message != WM_QUIT)
	{
		if(::PeekMessage(&msg, 0, 0, 0, PM_REMOVE))
		{
			::TranslateMessage(&msg);
			::DispatchMessage(&msg);
		}
		else
		{	
			int t = timeGetTime();
			float deltaTime = (t - startTime) * 0.001f;
			mainWindow.Update(deltaTime);
			mainWindow.Render();
			startTime = t;
		}
	}

	mainWindow.Cleanup();
	delete queueMgr;
	HttpObject::DestroyHTTPSession();
	free(argv[0]);
	free(argv);
    return (int)msg.wParam;
}

void MainWindow::ParseCmdLine(int argc, char** argv)
{
	m_mapGen = false;
	m_dataSource = FileObject::SOURCE_DISK;
	m_Mbps = 1;
	strcpy_s(m_dataPath, MAX_PATH, "Data\\");
	strcpy_s(m_hostName, "localhost");

	for (int i = 1; i < argc; i++)
	{
		if (strcmp(argv[i], "-g") == 0)
			m_mapGen = true;
		else if (strcmp(argv[i], "-h") == 0)
			m_dataSource = FileObject::SOURCE_HTTP;
		else if (strncmp(argv[i], "-p=", 3) == 0)
			strcpy_s(m_dataPath, MAX_PATH, argv[i] + 3);
		else if (strncmp(argv[i], "-s=", 3) == 0)
			strcpy_s(m_hostName, MAX_PATH, argv[i] + 3);
		else if (strncmp(argv[i], "-b=", 3) == 0)
			m_Mbps = atoi(argv[i] + 3);

	}	
}

MainWindow::MainWindow(int argc, char** argv)
{
	m_argc = argc;
	m_argv = argv;
	m_pDevice = NULL; 
	m_mainWindow = 0;
	m_wireframe = false;
	m_drawMinimap = true;
	srand(GetTickCount());
	m_fps = m_lastFps = 0;
	m_time = m_snapTime = GetTickCount();
	m_pLine = NULL;
	m_pMiniMapBorder = NULL;
	ParseCmdLine(argc, argv);
}

HRESULT MainWindow::Init(HINSTANCE hInstance, int width, int height, bool windowed, FileQueueManager* queueMgr)
{
	s_log.Print("Application initiated");
	
	m_queueMgr = queueMgr;

	//Create Window Class
	WNDCLASS wc;
	memset(&wc, 0, sizeof(WNDCLASS));
	wc.style         = CS_HREDRAW | CS_VREDRAW;
	wc.lpfnWndProc   = (WNDPROC)::DefWindowProc; 
	wc.hInstance     = hInstance;
	wc.lpszClassName = "D3DWND";

	//Register Class and Create new Window
	RegisterClass(&wc);
	m_mainWindow = CreateWindow("D3DWND", "3D Streamer", WS_EX_TOPMOST, 0, 0, width, height, 0, 0, hInstance, 0); 
	SetCursor(NULL);
	ShowWindow(m_mainWindow, SW_SHOW);
	UpdateWindow(m_mainWindow);

	//Create IDirect3D9 Interface
	IDirect3D9* d3d9 = Direct3DCreate9(D3D_SDK_VERSION);

    if(d3d9 == NULL)
	{
		s_log.Print("Direct3DCreate9() - FAILED");
		return E_FAIL;
	}

	//Check that the Device supports what we need from it
	D3DCAPS9 caps;
	d3d9->GetDeviceCaps(D3DADAPTER_DEFAULT, D3DDEVTYPE_HAL, &caps);

	//Hardware Vertex Processing or not?
	int vp = 0;
	if(caps.DevCaps & D3DDEVCAPS_HWTRANSFORMANDLIGHT)
		vp = D3DCREATE_HARDWARE_VERTEXPROCESSING;
	else vp = D3DCREATE_SOFTWARE_VERTEXPROCESSING;

	//Check vertex & pixelshader versions
	if(caps.VertexShaderVersion < D3DVS_VERSION(2, 0) || caps.PixelShaderVersion < D3DPS_VERSION(2, 0))
	{
		s_log.Print("Warning - Your graphic card does not support vertex and pixel shaders version 2.0");
	}

	//Set D3DPRESENT_PARAMETERS
	D3DPRESENT_PARAMETERS d3dpp;
	d3dpp.BackBufferWidth            = width;
	d3dpp.BackBufferHeight           = height;
	d3dpp.BackBufferFormat           = D3DFMT_A8R8G8B8;
	d3dpp.BackBufferCount            = 1;
	d3dpp.MultiSampleType            = D3DMULTISAMPLE_NONE;
	d3dpp.MultiSampleQuality         = 0;
	d3dpp.SwapEffect                 = D3DSWAPEFFECT_DISCARD; 
	d3dpp.hDeviceWindow              = m_mainWindow;
	d3dpp.Windowed                   = windowed;
	d3dpp.EnableAutoDepthStencil     = true; 
	d3dpp.AutoDepthStencilFormat     = D3DFMT_D24S8;
	d3dpp.Flags                      = 0;
	d3dpp.FullScreen_RefreshRateInHz = D3DPRESENT_RATE_DEFAULT;
	d3dpp.PresentationInterval       = D3DPRESENT_INTERVAL_IMMEDIATE;

	//Create the IDirect3DDevice9
	if(FAILED(d3d9->CreateDevice(D3DADAPTER_DEFAULT, D3DDEVTYPE_HAL, m_mainWindow,
								 vp, &d3dpp, &m_pDevice)))
	{
		s_log.Print("Failed to create IDirect3DDevice9");
		return E_FAIL;
	}

	//Release IDirect3D9 interface
	d3d9->Release();

	D3DXCreateFont(m_pDevice, 18, 0, FW_HEAVY, 0, false,  
				   DEFAULT_CHARSET, OUT_DEFAULT_PRECIS, DEFAULT_QUALITY,
				   DEFAULT_PITCH | FF_DONTCARE, TEXT("Arial"), &m_pFont);

	LoadModel(m_pDevice);
	
	m_camera.Init(m_pDevice);
	
	D3DXCreateLine(m_pDevice, &m_pLine);

	//Set sampler state
	for(int i=0;i<8;i++)
	{
		m_pDevice->SetSamplerState(i, D3DSAMP_MAGFILTER, D3DTEXF_LINEAR);
		m_pDevice->SetSamplerState(i, D3DSAMP_MINFILTER, D3DTEXF_LINEAR);
		m_pDevice->SetSamplerState(i, D3DSAMP_MIPFILTER, D3DTEXF_POINT);
	}

	//Create Minimap
	if(FAILED(m_pDevice->CreateTexture(256, 256, 1, D3DUSAGE_RENDERTARGET, D3DFMT_A8R8G8B8, D3DPOOL_DEFAULT, &m_pMiniMap, NULL)))
		s_log.Print("Failed to create texture: miniMap");
	if(FAILED(D3DXCreateTextureFromFile(m_pDevice, "textures/minimap.dds", &m_pMiniMapBorder)))
		s_log.Print("Could not load minimap.dds");
	RECT r = {608, 0, 799, 191 };
	m_miniMapRect = r;

	D3DXCreateSprite(m_pDevice, &m_pSprite);

	m_terrain.Init(m_pDevice, 
				   INTPOINT( VERTICES_PER_TERRAIN_X, VERTICES_PER_TERRAIN_Y), 
				   INTPOINT( PATCHES_PER_TERRAIN_X, PATCHES_PER_TERRAIN_Y),
				   m_mapGen, m_dataSource,
				   m_dataPath, queueMgr);

	return S_OK;
}

HRESULT MainWindow::Update(float deltaTime)
{
	//Control camera
	m_camera.Update(m_terrain, deltaTime);
	if (!m_mapGen)
		m_terrain.Update(m_camera, deltaTime);
	UpdateMiniMap();

	if(KEYDOWN('F'))
	{
		m_wireframe = !m_wireframe;
		Sleep(300);
	} else if(KEYDOWN('M'))
	{
		m_drawMinimap = !m_drawMinimap;
		Sleep(300);
	} else if(m_mapGen && KEYDOWN(VK_SPACE))
	{		
		GenerateRandomTerrain(&m_terrain);
	}
	else if(KEYDOWN(VK_ESCAPE))
	{
		Quit();
	}

	return S_OK;
}	

HRESULT MainWindow::Render()
{
    // Clear the viewport
    m_pDevice->Clear( 0L, NULL, D3DCLEAR_TARGET | D3DCLEAR_ZBUFFER, 0xffffffff, 1.0f, 0L );

	//FPS Calculation
	m_fps++;
	if(GetTickCount() - m_time > 1000)
	{
		m_lastFps = m_fps;
		m_fps = 0;
		m_time = GetTickCount();
	}

    // Begin the scene 
    if(SUCCEEDED(m_pDevice->BeginScene()))
    {
		if(m_wireframe)m_pDevice->SetRenderState(D3DRS_FILLMODE, D3DFILL_WIREFRAME);	
		else m_pDevice->SetRenderState(D3DRS_FILLMODE, D3DFILL_SOLID);

		m_terrain.Render(m_camera);
		if (m_drawMinimap)
		{
			//Render Minimap
			if (m_terrain.m_changed)
			{
				m_terrain.RenderLandscape(m_camera);
				m_terrain.m_changed = false;
			}
			RenderMiniMap(m_miniMapRect);
		}

		char txt[256];
		RECT r[] = {{10, 10, 0, 0}, {10, 35, 0, 0}, {10, 60, 0, 0}, {10, 85, 0, 0 }, {10, 110, 0, 0}, {10, 135, 0, 0}, 
					{400, 10, 0, 0}, {400, 30, 0, 0}};
		m_pFont->DrawText(NULL, "Move Arround: A,D,S,W", -1, &r[0], DT_LEFT| DT_TOP | DT_NOCLIP, 0xff30e0a0);
		m_pFont->DrawText(NULL, "Rotate Camera: Arrow Keys", -1, &r[1], DT_LEFT| DT_TOP | DT_NOCLIP, 0xff30e0a0);
		m_pFont->DrawText(NULL, "Toggle Wireframe/Minimap: F, M", -1, &r[2], DT_LEFT| DT_TOP | DT_NOCLIP, 0xff30e0a0);
		m_pFont->DrawText(NULL, "Zoom In/Out: +, -", -1, &r[3], DT_LEFT| DT_TOP | DT_NOCLIP, 0xff30e0a0);
		sprintf_s(txt, 256, "Camera: pos(%3.1f, %3.1f, %3.1f), angle(%3.1f, %3.1f)", m_camera.m_eye.x, m_camera.m_eye.y, m_camera.m_eye.z,
				 m_camera.m_alpha, m_camera.m_beta);
		m_pFont->DrawText(NULL, txt, -1, &r[4], DT_LEFT| DT_TOP | DT_NOCLIP, 0xff30e0a0);
		int patch_x = (int)m_camera.m_eye.x / TILES_PER_PATCH_X;
		int patch_y = (int)-m_camera.m_eye.z / TILES_PER_PATCH_Y;
		
		if (m_mapGen)
		{
			m_pFont->DrawText(NULL, "Space: Re-generate randomized terrain", -1, &r[5], DT_LEFT| DT_TOP | DT_NOCLIP, 0xff30e0a0);
		} else 
		{
			sprintf_s(txt, 256, "Prefetch Queues : %d,%d,%d,%d", m_queueMgr->GetQueueLength(FileQueue::QUEUE_CRITICAL),
															     m_queueMgr->GetQueueLength(FileQueue::QUEUE_HIGH),
															     m_queueMgr->GetQueueLength(FileQueue::QUEUE_MEDIUM),
															     m_queueMgr->GetQueueLength(FileQueue::QUEUE_LOW));
			m_pFont->DrawText(NULL, txt, -1, &r[5], DT_LEFT| DT_TOP | DT_NOCLIP, 0xff30e0a0);
		}

		sprintf_s(txt, "FPS: %d", m_lastFps);
		m_pFont->DrawText(NULL, txt, -1, &r[6], DT_LEFT| DT_TOP | DT_NOCLIP, 0xff30e0a0);
		if (!m_mapGen)
		{
			sprintf_s(txt, 256, "Bandwidth: %4.1f Mbps", m_queueMgr->GetDownloadRate());
			m_pFont->DrawText(NULL, txt, -1, &r[7], DT_LEFT| DT_TOP | DT_NOCLIP, 0xff30e0a0);
		}

        // End the scene.
		m_pDevice->EndScene();
		m_pDevice->Present(0, 0, 0, 0);
    }

	return S_OK;
}

HRESULT MainWindow::Cleanup()
{
	try
	{
		m_terrain.Release();
		UnloadModel();

		m_pFont->Release();
		m_pLine->Release();
		m_pDevice->Release();

		s_log.Print("Application terminated");
	}
	catch(...){}

	return S_OK;
}

HRESULT MainWindow::Quit()
{
	::DestroyWindow(m_mainWindow);
	::PostQuitMessage(0);
	return S_OK;
}

void MainWindow::UpdateMiniMap()
{
	//Retrieve the surface of the back buffer
	IDirect3DSurface9 *backSurface = NULL;
	m_pDevice->GetRenderTarget(0, &backSurface);

	//Get and set surface of the m_pFogOfWarTexture...
	IDirect3DSurface9 *minimapSurface = NULL;
	m_pMiniMap->GetSurfaceLevel(0, &minimapSurface);
	m_pDevice->SetRenderTarget(0, minimapSurface);

	m_pDevice->Clear(0, NULL, D3DCLEAR_TARGET | D3DCLEAR_ZBUFFER, 0x00000000, 1.0f, 0);
	m_pDevice->BeginScene();

	//Draw to the minimap texture
	m_pSprite->Begin(0);
	m_pSprite->Draw(m_terrain.m_pLandScape, NULL, NULL, &D3DXVECTOR3(0.0f, 0.0f, 0.0f), 0xffffffff);
	m_pSprite->End();

	m_pDevice->EndScene();

	//Reset render target to back buffer
	m_pDevice->SetRenderTarget(0, backSurface);

	//Release surfaces
	backSurface->Release();
	minimapSurface->Release();
}

void MainWindow::RenderMiniMap(RECT dest)
{
	float width = (float)(dest.right - dest.left);
	float height = (float)(dest.bottom - dest.top);

	D3DXVECTOR2 scale = D3DXVECTOR2(width / 256.0f, height / 256.0f);
	D3DXMATRIX scaleMatrix;
	D3DXMatrixScaling(&scaleMatrix, scale.x, scale.y, 1.0f);
	m_pSprite->SetTransform(&scaleMatrix);

	m_pSprite->Begin(0);
	m_pSprite->Draw(m_pMiniMap, NULL, NULL, &D3DXVECTOR3(dest.left / scale.x, dest.top / scale.y, 0.0f), 0xffffffff);
	m_pSprite->End();

	D3DXMatrixIdentity(&scaleMatrix);
	m_pSprite->SetTransform(&scaleMatrix);

	//Calculate m_camera frustum viewpoints
	D3DXMATRIX view, proj, viewInverse;

	view = m_camera.GetViewMatrix();
	proj = m_camera.GetProjectionMatrix();

	//fov_x & fov_y Determines the size of the frustum representation
	float screenRatio = proj(0,0) / proj(1,1);
	float fov_x = tanf(m_camera.m_fov / 2);
	float fov_y = fov_x * screenRatio;

	//Initialize the four rays
	D3DXVECTOR3 org = D3DXVECTOR3(0.0f, 0.0f, 0.0f);	//Same origin

	//Four different directions
	D3DXVECTOR3 dir[4] = {D3DXVECTOR3(-fov_x, fov_y, 1.0f),	
						  D3DXVECTOR3(fov_x, fov_y, 1.0f),
						  D3DXVECTOR3(fov_x, -fov_y, 1.0f),
						  D3DXVECTOR3(-fov_x, -fov_y, 1.0f)};

	//Our resulting minimap coordinates
	D3DXVECTOR2 points[5];

	//View matrix inverse
	D3DXMatrixInverse(&viewInverse, 0, &view);
	D3DXVec3TransformCoord(&org, &org, &viewInverse);

	//Ground plane
	D3DXPLANE plane;
	D3DXPlaneFromPointNormal(&plane, &D3DXVECTOR3(0.0f, 0.0f, 0.0f), &D3DXVECTOR3(0.0f, 1.0f, 0.0f));

	bool ok = true;

	//check where each ray intersects with the ground plane
	for(int i=0;i<4 && ok;i++)
	{
		//Transform ray direction
		D3DXVec3TransformNormal(&dir[i], &dir[i], &viewInverse);
		D3DXVec3Normalize(&dir[i], &dir[i]);
		dir[i] *= 1000.0f;

		//Find intersection point
		D3DXVECTOR3 hit;
		if(D3DXPlaneIntersectLine(&hit, &plane, &org, &dir[i]) == NULL)
			ok = false;

		//Make sure the intersection point is on the positive side of the near plane
		D3DXPLANE n = m_camera.m_frustum[4];
		float distance = n.a * hit.x + n.b * hit.y + n.c * hit.z + n.d;
		if(distance < 0.0f)ok = false;

		//Convert the intersection point to a minimap coordinate
		if(ok)
		{
			points[i].x = (hit.x / (float)m_terrain.m_size.x) * width;
			points[i].y = (-hit.z / (float)m_terrain.m_size.y) * height;
		}
	}

	//Set the end point to equal the starting point
	points[4] = points[0];

	//Set viewport to destination rectangle only...
	D3DVIEWPORT9 v1, v2;

	v1.X = dest.left;
	v1.Y = dest.top;
	v1.Width = (int)width;
	v1.Height = (int)height;
	v1.MinZ = 0.0f;
	v1.MaxZ = 1.0f;

	m_pDevice->GetViewport(&v2);
	m_pDevice->SetViewport(&v1);

	//Draw camera frustum in the minimap
	if(ok)
	{
		m_pLine->SetWidth(1.0f);
		m_pLine->SetAntialias(true);
		m_pLine->Begin();
		m_pLine->Draw(&points[0], 5, 0xffffffff);
		m_pLine->End();
	}

	//Reset viewport
	m_pDevice->SetViewport(&v2);

	//Draw minimap border
	m_pSprite->Begin(D3DXSPRITE_ALPHABLEND);
	m_pSprite->Draw(m_pMiniMapBorder, NULL, NULL, &D3DXVECTOR3(v2.Width - 256.0f, 0.0f, 0.0f), 0xffffffff);
	m_pSprite->End();
}