//--------------------------------------------------------------------------------------
// SSAO_CS50_11
//
// This sample demonstrates how to utilize the compute shader to implement the screen
// space ambient occlusion algorithm.
//
// Copyright (C) 2003-2009 Jason Zink. All rights reserved.
//--------------------------------------------------------------------------------
#include "DXUT.h"
#include "CLog.h"
#include "GeometryLoaderDX11.h"

ID3D11Texture2D*			g_pDepthNormalBuffer = 0;
ID3D11RenderTargetView*		g_pDepthNormalBufferRTV = 0;
ID3D11ShaderResourceView *	g_pDepthNormalBufferSRV = 0;

ID3D11Texture2D*			g_pOcclusionBuffer = 0;
ID3D11UnorderedAccessView*	g_pOcclusionBufferUAV = 0;
ID3D11ShaderResourceView *	g_pOcclusionBufferSRV = 0;


GeometryDX11*				g_pModel = 0;
ID3D11InputLayout*			g_pInputLayout = 0;

ID3D11VertexShader*			g_pDepthVS = 0;
ID3D11PixelShader*			g_pDepthPS = 0;

ID3D11ComputeShader*		g_pAmbientOcclusionCS = 0;
ID3D11ComputeShader*		g_pHorizontalBilateralCS = 0;
ID3D11ComputeShader*		g_pVerticalBilateralCS = 0;

ID3D11VertexShader*			g_pFinalVS = 0;
ID3D11PixelShader*			g_pFinalPS = 0;

ID3D11Buffer*				g_pConstantBuffer = 0;

D3DXMATRIXA16				g_mWorld;
D3DXMATRIXA16				g_mView;
D3DXMATRIXA16				g_mProj;


//--------------------------------------------------------------------------------------
// Reject any D3D11 devices that aren't acceptable by returning false
//--------------------------------------------------------------------------------------
bool CALLBACK IsD3D11DeviceAcceptable( const CD3D11EnumAdapterInfo *AdapterInfo, UINT Output, const CD3D11EnumDeviceInfo *DeviceInfo,
                                       DXGI_FORMAT BackBufferFormat, bool bWindowed, void* pUserContext )
{
    return true;
}


//--------------------------------------------------------------------------------------
// Called right before creating a D3D9 or D3D11 device, allowing the app to modify the device settings as needed
//--------------------------------------------------------------------------------------
bool CALLBACK ModifyDeviceSettings( DXUTDeviceSettings* pDeviceSettings, void* pUserContext )
{
    return true;
}


//--------------------------------------------------------------------------------------
// Create any D3D11 resources that aren't dependant on the back buffer
//--------------------------------------------------------------------------------------
HRESULT CALLBACK OnD3D11CreateDevice( ID3D11Device* pd3dDevice, const DXGI_SURFACE_DESC* pBackBufferSurfaceDesc,
                                      void* pUserContext )
{

	// Depth/Normal buffer creation

	D3D11_TEXTURE2D_DESC desc;
	desc.Width = 640;
	desc.Height = 480;
	desc.MipLevels = 1;
	desc.ArraySize = 1;
	desc.Format = DXGI_FORMAT_R16G16B16A16_FLOAT;
	desc.SampleDesc.Count = 1;
	desc.SampleDesc.Quality = 0;
	desc.Usage = D3D11_USAGE_DEFAULT;
	desc.BindFlags = D3D11_BIND_SHADER_RESOURCE | D3D11_BIND_RENDER_TARGET;
	desc.CPUAccessFlags = 0;
	desc.MiscFlags = 0;

	pd3dDevice->CreateTexture2D( &desc, 0, &g_pDepthNormalBuffer );								// Create the texture
	pd3dDevice->CreateRenderTargetView( g_pDepthNormalBuffer, 0, &g_pDepthNormalBufferRTV );	// Create the RTV
	pd3dDevice->CreateShaderResourceView( g_pDepthNormalBuffer, 0, &g_pDepthNormalBufferSRV );	// Create the SRV

	// Create the occlusion buffer

	desc.Format = DXGI_FORMAT_R32_FLOAT;
	desc.BindFlags = D3D11_BIND_SHADER_RESOURCE | D3D11_BIND_UNORDERED_ACCESS;

	pd3dDevice->CreateTexture2D( &desc, 0, &g_pOcclusionBuffer );								// Create the texture
	pd3dDevice->CreateUnorderedAccessView( g_pOcclusionBuffer, 0, &g_pOcclusionBufferUAV );		// Create the UAV
	pd3dDevice->CreateShaderResourceView( g_pOcclusionBuffer, 0, &g_pOcclusionBufferSRV );		// Create the SRV


	// Create the constant buffer for world/view/proj. matrices

	// Create the world matrix
	D3DXMatrixIdentity( &g_mWorld );

	// Create the view matrix
	D3DXVECTOR3 vLookAt = D3DXVECTOR3( 0.0f, 0.75f, 0.0f );
	D3DXVECTOR3 vLookFrom = D3DXVECTOR3( 3.0f, 3.5f, -3.0f );
	D3DXVECTOR3 vLookUp = D3DXVECTOR3( 0.0f, 1.0f, 0.0f );
	D3DXMatrixLookAtLH( &g_mView, &vLookFrom, &vLookAt, &vLookUp );

	// Create the projection matrix
	D3DXMatrixPerspectiveFovLH( &g_mProj, static_cast< float >(D3DX_PI) / 2.0f, 640.0f /  480.0f, 1.0f, 25.0f );

	// Composite together for the final transform
	D3DXMATRIXA16 mWorldView = g_mWorld * g_mView;
	D3DXMATRIXA16 mWorldViewProj = g_mWorld * g_mView * g_mProj;
	D3DXMATRIXA16 mFinal;
	D3DXMatrixTranspose( &mFinal, &mWorldViewProj );

	D3DXMATRIXA16 identity;
	D3DXMatrixIdentity( &identity );
	
	D3D11_SUBRESOURCE_DATA data;
	data.pSysMem = &identity;
	data.SysMemPitch = 0;
	data.SysMemSlicePitch = 0;

	D3D11_BUFFER_DESC cbuffer;
	cbuffer.ByteWidth = 2*sizeof( D3DXMATRIX );
    cbuffer.BindFlags = D3D11_BIND_CONSTANT_BUFFER;
    cbuffer.MiscFlags = 0;
    cbuffer.StructureByteStride = 0;
	cbuffer.Usage = D3D11_USAGE_DYNAMIC;
	cbuffer.CPUAccessFlags = D3D11_CPU_ACCESS_WRITE;

	pd3dDevice->CreateBuffer( &cbuffer, &data, &g_pConstantBuffer );


	// Create the shaders that will be used during depth buffer rendering

	HRESULT hr = S_OK;

	ID3DBlob* pCompiledShader = NULL;
	ID3DBlob* pErrorMessages = NULL;

	// Compile the depth pass vertex shader
	hr = D3DX11CompileFromFile( L"DepthVS.hlsl", 0, 0, "VSMAIN", "vs_4_0", 0, 0, 0, &pCompiledShader, &pErrorMessages, &hr );
	hr = pd3dDevice->CreateVertexShader( pCompiledShader->GetBufferPointer(), pCompiledShader->GetBufferSize(), 0, &g_pDepthVS );
	if ( FAILED( hr ) )
	{
		if ( pErrorMessages != 0 )
		{
			LPVOID pCompileErrors = pErrorMessages->GetBufferPointer();
			const char* pMessage = (const char*)pCompileErrors;
			CLog::Get().Write( (const char*)pCompileErrors );
		}
		return( E_FAIL );
	}

	// Get the input layout with the compiled vertex shader
	D3D11_INPUT_ELEMENT_DESC layout[] =
    {
        { "POSITION", 0, DXGI_FORMAT_R32G32B32_FLOAT, 0, 0, D3D11_INPUT_PER_VERTEX_DATA, 0 },  
		{ "TEXCOORDS", 0, DXGI_FORMAT_R32G32_FLOAT, 0, D3D11_APPEND_ALIGNED_ELEMENT, D3D11_INPUT_PER_VERTEX_DATA, 0 },
		{ "NORMAL", 0, DXGI_FORMAT_R32G32B32_FLOAT, 0, D3D11_APPEND_ALIGNED_ELEMENT, D3D11_INPUT_PER_VERTEX_DATA, 0 }
    };
    UINT numElements = sizeof(layout) / sizeof(layout[0]);
    pd3dDevice->CreateInputLayout( layout, numElements, pCompiledShader->GetBufferPointer(), pCompiledShader->GetBufferSize(), &g_pInputLayout );
	SAFE_RELEASE( pCompiledShader );

	// Compile the depth pass pixel shader
	hr = D3DX11CompileFromFile( L"DepthPS.hlsl", 0, 0, "PSMAIN", "ps_4_0", 0, 0, 0, &pCompiledShader, &pErrorMessages, &hr );
	if ( FAILED( hr ) )
	{
		if ( pErrorMessages != 0 )
		{
			LPVOID pCompileErrors = pErrorMessages->GetBufferPointer();
			const char* pMessage = (const char*)pCompileErrors;
			CLog::Get().Write( (const char*)pCompileErrors );
		}
		return( E_FAIL );
	}
	hr = pd3dDevice->CreatePixelShader( pCompiledShader->GetBufferPointer(), pCompiledShader->GetBufferSize(), 0, &g_pDepthPS );
	SAFE_RELEASE( pCompiledShader );

	// Compile the ambient occlusion compute shader.  Uncomment the define below to utilize 16x16 thread groups, or comment it
	// to utilize 32x32 thread groups.

//#define USE_16x16
#ifdef USE_16x16
	hr = D3DX11CompileFromFile( L"AmbientOcclusionTraditionalCS_randomized_viewspace_16.hlsl", 0, 0, "CSMAIN", "cs_5_0", 0, 0, 0, &pCompiledShader, &pErrorMessages, &hr );
#else
	hr = D3DX11CompileFromFile( L"AmbientOcclusionTraditionalCS_randomized_viewspace_32.hlsl", 0, 0, "CSMAIN", "cs_5_0", 0, 0, 0, &pCompiledShader, &pErrorMessages, &hr );
#endif
	if ( FAILED( hr ) )
	{
		if ( pErrorMessages != 0 )
		{
			LPVOID pCompileErrors = pErrorMessages->GetBufferPointer();
			const char* pMessage = (const char*)pCompileErrors;
			CLog::Get().Write( (const char*)pCompileErrors );
		}
		return( E_FAIL );
	}
	hr = pd3dDevice->CreateComputeShader( pCompiledShader->GetBufferPointer(), pCompiledShader->GetBufferSize(), 0, &g_pAmbientOcclusionCS );
	SAFE_RELEASE( pCompiledShader );

	// Compile the horizontal bilateral filter
	hr = D3DX11CompileFromFile( L"SeparableBilateralCS.hlsl", 0, 0, "CS_Horizontal", "cs_5_0", 0, 0, 0, &pCompiledShader, &pErrorMessages, &hr );
	if ( FAILED( hr ) )
	{
		if ( pErrorMessages != 0 )
		{
			LPVOID pCompileErrors = pErrorMessages->GetBufferPointer();
			const char* pMessage = (const char*)pCompileErrors;
			CLog::Get().Write( (const char*)pCompileErrors );
		}
		return( E_FAIL );
	}
	hr = pd3dDevice->CreateComputeShader( pCompiledShader->GetBufferPointer(), pCompiledShader->GetBufferSize(), 0, &g_pHorizontalBilateralCS );
	SAFE_RELEASE( pCompiledShader );

	// Compile the vertical bilateral filter
	hr = D3DX11CompileFromFile( L"SeparableBilateralCS.hlsl", 0, 0, "CS_Vertical", "cs_5_0", 0, 0, 0, &pCompiledShader, &pErrorMessages, &hr );
	if ( FAILED( hr ) )
	{
		if ( pErrorMessages != 0 )
		{
			LPVOID pCompileErrors = pErrorMessages->GetBufferPointer();
			const char* pMessage = (const char*)pCompileErrors;
			CLog::Get().Write( (const char*)pCompileErrors );
		}
		return( E_FAIL );
	}
	hr = pd3dDevice->CreateComputeShader( pCompiledShader->GetBufferPointer(), pCompiledShader->GetBufferSize(), 0, &g_pVerticalBilateralCS );
	SAFE_RELEASE( pCompiledShader );

	// Compile the final rendering vertex shader
	hr = D3DX11CompileFromFile( L"FinalVS.hlsl", 0, 0, "VSMAIN", "vs_4_0", 0, 0, 0, &pCompiledShader, &pErrorMessages, &hr );
	if ( FAILED( hr ) )
	{
		if ( pErrorMessages != 0 )
		{
			LPVOID pCompileErrors = pErrorMessages->GetBufferPointer();
			const char* pMessage = (const char*)pCompileErrors;
			CLog::Get().Write( (const char*)pCompileErrors );
		}
		return( E_FAIL );
	}
	hr = pd3dDevice->CreateVertexShader( pCompiledShader->GetBufferPointer(), pCompiledShader->GetBufferSize(), 0, &g_pFinalVS );
	SAFE_RELEASE( pCompiledShader );

	// Compile the final rendering pixel shader
	hr = D3DX11CompileFromFile( L"FinalPS.hlsl", 0, 0, "PSMAIN", "ps_4_0", 0, 0, 0, &pCompiledShader, &pErrorMessages, &hr );
	if ( FAILED( hr ) )
	{
		if ( pErrorMessages != 0 )
		{
			LPVOID pCompileErrors = pErrorMessages->GetBufferPointer();
			const char* pMessage = (const char*)pCompileErrors;
			CLog::Get().Write( (const char*)pCompileErrors );
		}
		return( E_FAIL );
	}
	hr = pd3dDevice->CreatePixelShader( pCompiledShader->GetBufferPointer(), pCompiledShader->GetBufferSize(), 0, &g_pFinalPS );
	SAFE_RELEASE( pCompiledShader );

	// Load the model for rendering
	g_pModel = GeometryLoaderDX11::loadMS3DFile2( std::string( "SSAO_Demo_Scene_4.ms3d" ) );
	g_pModel->LoadToBuffers( pd3dDevice );

    return S_OK;
}


//--------------------------------------------------------------------------------------
// Create any D3D11 resources that depend on the back buffer
//--------------------------------------------------------------------------------------
HRESULT CALLBACK OnD3D11ResizedSwapChain( ID3D11Device* pd3dDevice, IDXGISwapChain* pSwapChain,
                                          const DXGI_SURFACE_DESC* pBackBufferSurfaceDesc, void* pUserContext )
{
    return S_OK;
}


//--------------------------------------------------------------------------------------
// Handle updates to the scene.  This is called regardless of which D3D API is used
//--------------------------------------------------------------------------------------
void CALLBACK OnFrameMove( double fTime, float fElapsedTime, void* pUserContext )
{
}


//--------------------------------------------------------------------------------------
// Render the scene using the D3D11 device
//--------------------------------------------------------------------------------------
void CALLBACK OnD3D11FrameRender( ID3D11Device* pd3dDevice, ID3D11DeviceContext* pd3dImmediateContext,
                                  double fTime, float fElapsedTime, void* pUserContext )
{
    // Clear render target and the depth stencil 
    float ClearColor[4] = { 0.176f, 0.196f, 0.667f, 0.0f };

    ID3D11RenderTargetView* pRTV = DXUTGetD3D11RenderTargetView();
    ID3D11DepthStencilView* pDSV = DXUTGetD3D11DepthStencilView();

	// Update the constant buffer with a structure
	struct transforms
	{
		D3DXMATRIXA16 wvp;
		D3DXMATRIXA16 wv;
	};

	transforms xforms;
	D3DXMATRIXA16 mFinal;

	// Calculate the world * view matrix
	D3DXMatrixRotationY( &g_mWorld, fTime * 0.125f );
	D3DXMATRIXA16 mWorldView = g_mWorld * g_mView;
	D3DXMatrixTranspose( &mFinal, &mWorldView );
	xforms.wv = mFinal;
	
	// Calculate the world * view * projection matrix
	D3DXMATRIXA16 mWorldViewProj = g_mWorld * g_mView * g_mProj;
	D3DXMatrixTranspose( &mFinal, &mWorldViewProj );
	xforms.wvp = mFinal;		

	ID3D11Resource* pResource = g_pConstantBuffer;
	D3D11_MAPPED_SUBRESOURCE Data;

	// Map the resource
	HRESULT hr = pd3dImmediateContext->Map( pResource, 0, D3D11_MAP_WRITE_DISCARD, 0, &Data );
	
	// Copy the data into the buffer
	memcpy( Data.pData, &xforms, sizeof(D3DXMATRIXA16)*2 );

	// Unmap the resource
	pd3dImmediateContext->Unmap( pResource, 0 );


	// Set the depth/normal buffer and depth stencil view
	ID3D11RenderTargetView* pRenderTarget = { g_pDepthNormalBufferRTV };
	ID3D11DepthStencilView* pDepthStencilView = pDSV;
	pd3dImmediateContext->OMSetRenderTargets( 1, &pRenderTarget, pDepthStencilView );

	// Clear the depth/normal buffer and depth stencil view
	float clearColours[] = { 0.0f, 0.0f, 0.0f, 0.0f }; // RGBA
	pd3dImmediateContext->ClearRenderTargetView( g_pDepthNormalBufferRTV, clearColours );
	pd3dImmediateContext->ClearDepthStencilView( pDSV, D3D11_CLEAR_DEPTH, 1.0f, 0 );


	// Specify the TRIANGLE LIST as the topology for rasterization.
	pd3dImmediateContext->IASetPrimitiveTopology( D3D11_PRIMITIVE_TOPOLOGY_TRIANGLELIST );

	ID3D11Buffer* Buffers = { g_pModel->m_pVertexBuffer };
	UINT Strides = { g_pModel->GetVertexSize() };
	UINT Offsets = { 0 };

	// Configure the Input Assembler for rendering the model
	pd3dImmediateContext->IASetVertexBuffers( 0, 1, &Buffers, &Strides, &Offsets );
	pd3dImmediateContext->IASetIndexBuffer( g_pModel->m_pIndexBuffer, DXGI_FORMAT_R32_UINT, 0 );
	pd3dImmediateContext->IASetInputLayout( g_pInputLayout );

	// Set the vertex/pixel shaders and their resources for final rendering
	pd3dImmediateContext->VSSetShader( g_pDepthVS, 0, 0 );
	pd3dImmediateContext->PSSetShader( g_pDepthPS, 0, 0 );
	pd3dImmediateContext->VSSetConstantBuffers( 0, 1, &g_pConstantBuffer );

	// Draw the model
	pd3dImmediateContext->DrawIndexed( g_pModel->GetIndexCount(), 0, 0 );



	// Clear the render targets from the Output Merger so that it can be bound to the CS next.
	ID3D11RenderTargetView* pNullRT = { 0 };
	ID3D11DepthStencilView* pNullDSV = 0;
	pd3dImmediateContext->OMSetRenderTargets( 1, &pNullRT, pNullDSV );

	// Set the compute shader and its required resources
	pd3dImmediateContext->CSSetShader( g_pAmbientOcclusionCS, 0, 0 );
	pd3dImmediateContext->CSSetShaderResources( 0, 1, &g_pDepthNormalBufferSRV );									// register( t0 )
	pd3dImmediateContext->CSSetUnorderedAccessViews( 0, 1, &g_pOcclusionBufferUAV, (UINT*)&g_pOcclusionBufferUAV );	// register( u0 )
	
	// Dispatch appropriate number of thread groups depending on the thread group size.
#ifdef USE_16x16
	pd3dImmediateContext->Dispatch( 40, 30, 1 );
#else
	pd3dImmediateContext->Dispatch( 20, 15, 1 );
#endif


	// Unbind the depth/normal buffer SRV and occlusion buffer UAV from the CS
	ID3D11ShaderResourceView* pNullSRV = 0;
	ID3D11UnorderedAccessView* pNullUAV = 0;
	pd3dImmediateContext->CSSetShaderResources( 0, 1, &pNullSRV );
	pd3dImmediateContext->CSSetUnorderedAccessViews( 0, 1, &pNullUAV, 0 );

	// This loop performs the separable bilateral filtering.  Increasing the number of
	// iterations increases the blurring effect while mostly preserving the edges of the
	// objects within the image.
	for ( int i = 0; i < 1; i++ )
	{
		pd3dImmediateContext->CSSetShader( g_pHorizontalBilateralCS, 0, 0 );
		pd3dImmediateContext->CSSetShaderResources( 0, 1, &g_pDepthNormalBufferSRV );									// register( t0 )
		pd3dImmediateContext->CSSetUnorderedAccessViews( 0, 1, &g_pOcclusionBufferUAV, (UINT*)&g_pOcclusionBufferUAV );	// register( u0 )
		pd3dImmediateContext->Dispatch( 1, 480, 1 );

		pd3dImmediateContext->CSSetShader( g_pVerticalBilateralCS, 0, 0 );
		pd3dImmediateContext->CSSetShaderResources( 0, 1, &g_pDepthNormalBufferSRV );									// register( t0 )
		pd3dImmediateContext->CSSetUnorderedAccessViews( 0, 1, &g_pOcclusionBufferUAV, (UINT*)&g_pOcclusionBufferUAV );	// register( u0 )
		pd3dImmediateContext->Dispatch( 640, 1, 1 );
	}

	// Unbind resources from the CS
	pd3dImmediateContext->CSSetShaderResources( 0, 1, &pNullSRV );
	pd3dImmediateContext->CSSetUnorderedAccessViews( 0, 1, &pNullUAV, 0 );

	// Clear the final render target
	pd3dImmediateContext->ClearRenderTargetView( pRTV, ClearColor );
    pd3dImmediateContext->ClearDepthStencilView( pDSV, D3D11_CLEAR_DEPTH, 1.0, 0 );

	// Set the render target and depth buffer
	pd3dImmediateContext->OMSetRenderTargets( 1, &pRTV, pDSV );

	// Configure the Input Assembler for rendering the model
	pd3dImmediateContext->IASetVertexBuffers( 0, 1, &Buffers, &Strides, &Offsets );
	pd3dImmediateContext->IASetIndexBuffer( g_pModel->m_pIndexBuffer, DXGI_FORMAT_R32_UINT, 0 );
	pd3dImmediateContext->IASetInputLayout( g_pInputLayout );

	// Set the vertex/pixel shaders and their resources for final rendering
	pd3dImmediateContext->VSSetShader( g_pFinalVS, 0, 0 );
	pd3dImmediateContext->PSSetShader( g_pFinalPS, 0, 0 );
	pd3dImmediateContext->VSSetConstantBuffers( 0, 1, &g_pConstantBuffer );
	pd3dImmediateContext->PSSetShaderResources( 0, 1, &g_pOcclusionBufferSRV );

	// Draw the model
	pd3dImmediateContext->DrawIndexed( g_pModel->GetIndexCount(), 0, 0 );

	// Unbind the occlusion buffer from the PS
	pd3dImmediateContext->PSSetShaderResources( 0, 1, &pNullSRV );

}


//--------------------------------------------------------------------------------------
// Release D3D11 resources created in OnD3D11ResizedSwapChain 
//--------------------------------------------------------------------------------------
void CALLBACK OnD3D11ReleasingSwapChain( void* pUserContext )
{
}


//--------------------------------------------------------------------------------------
// Release D3D11 resources created in OnD3D11CreateDevice 
//--------------------------------------------------------------------------------------
void CALLBACK OnD3D11DestroyDevice( void* pUserContext )
{
	SAFE_RELEASE( g_pDepthNormalBuffer );
	SAFE_RELEASE( g_pDepthNormalBufferRTV );
	SAFE_RELEASE( g_pDepthNormalBufferSRV );

	SAFE_RELEASE( g_pOcclusionBuffer );
	SAFE_RELEASE( g_pOcclusionBufferUAV );
	SAFE_RELEASE( g_pOcclusionBufferSRV );

	SAFE_DELETE( g_pModel );

	SAFE_RELEASE( g_pInputLayout );
	
	SAFE_RELEASE( g_pDepthVS );
	SAFE_RELEASE( g_pDepthPS );

	SAFE_RELEASE( g_pAmbientOcclusionCS );
	SAFE_RELEASE( g_pHorizontalBilateralCS );
	SAFE_RELEASE( g_pVerticalBilateralCS );

	SAFE_RELEASE( g_pFinalVS );
	SAFE_RELEASE( g_pFinalPS );

	SAFE_RELEASE( g_pConstantBuffer );
}


//--------------------------------------------------------------------------------------
// Handle messages to the application
//--------------------------------------------------------------------------------------
LRESULT CALLBACK MsgProc( HWND hWnd, UINT uMsg, WPARAM wParam, LPARAM lParam,
                          bool* pbNoFurtherProcessing, void* pUserContext )
{
    return 0;
}


//--------------------------------------------------------------------------------------
// Handle key presses
//--------------------------------------------------------------------------------------
void CALLBACK OnKeyboard( UINT nChar, bool bKeyDown, bool bAltDown, void* pUserContext )
{
}


//--------------------------------------------------------------------------------------
// Handle mouse button presses
//--------------------------------------------------------------------------------------
void CALLBACK OnMouse( bool bLeftButtonDown, bool bRightButtonDown, bool bMiddleButtonDown,
                       bool bSideButton1Down, bool bSideButton2Down, int nMouseWheelDelta,
                       int xPos, int yPos, void* pUserContext )
{
}


//--------------------------------------------------------------------------------------
// Call if device was removed.  Return true to find a new device, false to quit
//--------------------------------------------------------------------------------------
bool CALLBACK OnDeviceRemoved( void* pUserContext )
{
    return true;
}


//--------------------------------------------------------------------------------------
// Initialize everything and go into a render loop
//--------------------------------------------------------------------------------------
int WINAPI wWinMain( HINSTANCE hInstance, HINSTANCE hPrevInstance, LPWSTR lpCmdLine, int nCmdShow )
{
    // Enable run-time memory check for debug builds.
#if defined(DEBUG) | defined(_DEBUG)
    _CrtSetDbgFlag( _CRTDBG_ALLOC_MEM_DF | _CRTDBG_LEAK_CHECK_DF );
#endif

	CLog::Get().Open();

    // DXUT will create and use the best device (either D3D9 or D3D11) 
    // that is available on the system depending on which D3D callbacks are set below

    // Set general DXUT callbacks
    DXUTSetCallbackFrameMove( OnFrameMove );
    DXUTSetCallbackKeyboard( OnKeyboard );
    DXUTSetCallbackMouse( OnMouse );
    DXUTSetCallbackMsgProc( MsgProc );
    DXUTSetCallbackDeviceChanging( ModifyDeviceSettings );
    DXUTSetCallbackDeviceRemoved( OnDeviceRemoved );

    // Set the D3D11 DXUT callbacks. Remove these sets if the app doesn't need to support D3D11
    DXUTSetCallbackD3D11DeviceAcceptable( IsD3D11DeviceAcceptable );
    DXUTSetCallbackD3D11DeviceCreated( OnD3D11CreateDevice );
    DXUTSetCallbackD3D11SwapChainResized( OnD3D11ResizedSwapChain );
    DXUTSetCallbackD3D11FrameRender( OnD3D11FrameRender );
    DXUTSetCallbackD3D11SwapChainReleasing( OnD3D11ReleasingSwapChain );
    DXUTSetCallbackD3D11DeviceDestroyed( OnD3D11DestroyDevice );

    // Perform any application-level initialization here

    DXUTInit( true, true, NULL ); // Parse the command line, show msgboxes on error, no extra command line params
    DXUTSetCursorSettings( true, true ); // Show the cursor and clip it when in full screen
    DXUTCreateWindow( L"Screen Space Ambient Occlusion in the Compute Shader" );

    // Only require 10-level hardware
    DXUTCreateDevice( D3D_FEATURE_LEVEL_11_0, true, 640, 480 );
    DXUTMainLoop(); // Enter into the DXUT ren  der loop

    // Perform any application-level cleanup here

    return DXUTGetExitCode();
}


