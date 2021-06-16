// GUIModel.cpp: A model to cache GUI draw calls.
// Created by: Aurelio Reis

#include "DXUT.h"

#include "GUIModel.h"


// The custom GUI vertex.
struct GPU_QUAD_VERTEX
{
	BYTE OffsetXY_IndexZ[ 4 ];
};

struct GPU_QUAD_VERTEX_POS_TC_COLOR
{
	D3DXVECTOR4 Position;
	D3DXVECTOR4 Texcoord;
	D3DCOLOR Color;
};


static const int NUM_DEBUG_COLORS = 6;
static D3DXVECTOR4 g_vDebugColors[ NUM_DEBUG_COLORS ] =
{
	D3DXVECTOR4( 1.0f, 0.0f, 0.0f, 1.0f ),	// red
	D3DXVECTOR4( 0.0f, 1.0f, 0.0f, 1.0f ),	// green
	D3DXVECTOR4( 0.0f, 0.0f, 1.0f, 1.0f ),	// blue
	D3DXVECTOR4( 1.0f, 1.0f, 0.0f, 1.0f ),	// yellow
	D3DXVECTOR4( 1.0f, 0.0f, 1.0f, 1.0f ),	// pink
	D3DXVECTOR4( 0.0f, 1.0f, 1.0f, 1.0f ),	// cyan
};


D3DXHANDLE	CArGuiModel::s_hRenderFontTech = NULL;
D3DXHANDLE	CArGuiModel::s_hRenderQuadTech = NULL;

// Constructor.
CArGuiModel::CArGuiModel() :
	m_pEffect( NULL ),
	m_pAttributesStreamBuffer( NULL ), m_pBaseInstanceBuffer( NULL ),
	m_pAttributesInstanceBuffer( NULL ), m_pConstArrayInstanceBuffer( NULL ),
	m_GeomTechnique( GTECH_CONSTARRAYS ), m_bUseAA( true ), m_bDebugShowBatchColors( false )
{
}

// Destructor.
CArGuiModel::~CArGuiModel()
{
}

// Initialize the Model.
void CArGuiModel::Initialize()
{
	int i;
	BYTE byCurIdx = 0;
	GPU_QUAD_VERTEX	*pVertices = NULL;
	WORD			*pIndices = NULL;

	//////////////////////////////////////////////////////////////////////////
	// Create the vertex decls.
	//////////////////////////////////////////////////////////////////////////

	static const D3DVERTEXELEMENT9 g_VertexDecl[] =
	{
		{ 0, 0,  D3DDECLTYPE_UBYTE4, D3DDECLMETHOD_DEFAULT, D3DDECLUSAGE_POSITION, 0 },

		{ 1, 0,  D3DDECLTYPE_FLOAT4, D3DDECLMETHOD_DEFAULT, D3DDECLUSAGE_POSITION, 1 },
		{ 1, 16,  D3DDECLTYPE_FLOAT4, D3DDECLMETHOD_DEFAULT, D3DDECLUSAGE_TEXCOORD, 0 },
		{ 1, 32,  D3DDECLTYPE_D3DCOLOR, D3DDECLMETHOD_DEFAULT, D3DDECLUSAGE_COLOR, 0 },

		// Used just for constant array instancing.
		{ 2, 0,  D3DDECLTYPE_UBYTE4, D3DDECLMETHOD_DEFAULT, D3DDECLUSAGE_POSITION, 2 },
		D3DDECL_END()
	};

	g_pd3dDevice->CreateVertexDeclaration( g_VertexDecl, &m_pDecl );

	//////////////////////////////////////////////////////////////////////////
	// Create the stream technique vertex buffer.
	//////////////////////////////////////////////////////////////////////////

	// NOTE: By specifying the "dynamic" flag this buffer is created in AGP memory as opposed to Video Memory (much faster to update).
	g_pd3dDevice->CreateVertexBuffer( sizeof( GPU_QUAD_VERTEX_POS_TC_COLOR ) * 4 * MAX_BUFFER_QUADS, D3DUSAGE_DYNAMIC | D3DUSAGE_WRITEONLY, 0, D3DPOOL_DEFAULT, &m_pAttributesStreamBuffer, NULL );

	//////////////////////////////////////////////////////////////////////////
	// Create the standard instancing technique vertex buffer.
	//////////////////////////////////////////////////////////////////////////

	g_pd3dDevice->CreateVertexBuffer( sizeof( GPU_QUAD_VERTEX ) * 4, D3DUSAGE_WRITEONLY, 0, D3DPOOL_MANAGED, &m_pBaseInstanceBuffer, NULL );

	// Create the initial geometry to be instanced.
	m_pBaseInstanceBuffer->Lock( 0, 0, (void**)&pVertices, NULL );
		// Left Top.
		pVertices[ 0 ].OffsetXY_IndexZ[ 0 ] = 0;
		pVertices[ 0 ].OffsetXY_IndexZ[ 1 ] = 0;

		// Right Top.
		pVertices[ 1 ].OffsetXY_IndexZ[ 0 ] = 1;
		pVertices[ 1 ].OffsetXY_IndexZ[ 1 ] = 0;

		// Right Bottom.
		pVertices[ 2 ].OffsetXY_IndexZ[ 0 ] = 1;
		pVertices[ 2 ].OffsetXY_IndexZ[ 1 ] = 1;

		// Left Bottom.
		pVertices[ 3 ].OffsetXY_IndexZ[ 0 ] = 0;
		pVertices[ 3 ].OffsetXY_IndexZ[ 1 ] = 1;
	m_pBaseInstanceBuffer->Unlock();

	// NOTE: By specifying the "dynamic" flag this buffer is created in AGP memory as opposed to Video Memory (much faster to update).
	g_pd3dDevice->CreateVertexBuffer( sizeof( GPU_QUAD_VERTEX_POS_TC_COLOR ) * MAX_BUFFER_QUADS, D3DUSAGE_DYNAMIC | D3DUSAGE_WRITEONLY, 0, D3DPOOL_DEFAULT, &m_pAttributesInstanceBuffer, NULL );

	//////////////////////////////////////////////////////////////////////////
	// Create the constant array instancing technique vertex buffer.
	// This buffer just holds the quad vertex indices for each instance so they
	// can grab their position from the constant arrays.
	//////////////////////////////////////////////////////////////////////////

	g_pd3dDevice->CreateVertexBuffer( sizeof( GPU_QUAD_VERTEX ) * MAX_CONST_ARRAY_QUADS, D3DUSAGE_WRITEONLY, 0, D3DPOOL_MANAGED, &m_pConstArrayInstanceBuffer, NULL );

	m_pConstArrayInstanceBuffer->Lock( 0, 0, (void**)&pVertices, NULL );
	for ( byCurIdx = 0, i = 0; i < MAX_CONST_ARRAY_QUADS; ++i, pVertices++, byCurIdx++ )
	{
		pVertices->OffsetXY_IndexZ[ 2 ] = byCurIdx;
	}
	m_pConstArrayInstanceBuffer->Unlock();

	//////////////////////////////////////////////////////////////////////////
	// Create the base const array geometry buffer.
	//////////////////////////////////////////////////////////////////////////

	// Create the vertex buffer.
	g_pd3dDevice->CreateVertexBuffer( sizeof( GPU_QUAD_VERTEX ) * 4 * MAX_BUFFER_QUADS, D3DUSAGE_WRITEONLY, 0, D3DPOOL_MANAGED, &m_pVertexBuffer, NULL );

	m_pVertexBuffer->Lock( 0, 0, (void**)&pVertices, NULL );
	
	// Advance through every quad.
	for ( byCurIdx = 0, i = 0; i < MAX_BUFFER_QUADS; ++i, pVertices += 4, byCurIdx++ )
	{
		// Left Top.
		pVertices[ 0 ].OffsetXY_IndexZ[ 0 ] = 0;
		pVertices[ 0 ].OffsetXY_IndexZ[ 1 ] = 0;
		pVertices[ 0 ].OffsetXY_IndexZ[ 2 ] = byCurIdx;

		// Right Top.
		pVertices[ 1 ].OffsetXY_IndexZ[ 0 ] = 1;
		pVertices[ 1 ].OffsetXY_IndexZ[ 1 ] = 0;
		pVertices[ 1 ].OffsetXY_IndexZ[ 2 ] = byCurIdx;

		// Right Bottom.
		pVertices[ 2 ].OffsetXY_IndexZ[ 0 ] = 1;
		pVertices[ 2 ].OffsetXY_IndexZ[ 1 ] = 1;
		pVertices[ 2 ].OffsetXY_IndexZ[ 2 ] = byCurIdx;

		// Left Bottom.
		pVertices[ 3 ].OffsetXY_IndexZ[ 0 ] = 0;
		pVertices[ 3 ].OffsetXY_IndexZ[ 1 ] = 1;
		pVertices[ 3 ].OffsetXY_IndexZ[ 2 ] = byCurIdx;
	}

	//////////////////////////////////////////////////////////////////////////
	// Create the index buffer.
	//////////////////////////////////////////////////////////////////////////

	// Create the index buffer.
	int iNumIndices = MAX_BUFFER_QUADS * 3 * 2;
	g_pd3dDevice->CreateIndexBuffer( iNumIndices * sizeof( WORD ), D3DUSAGE_WRITEONLY,
		D3DFMT_INDEX16, D3DPOOL_MANAGED, &m_pIndexBuffer, NULL );

	m_pIndexBuffer->Lock( 0, 0, (void**)&pIndices, NULL );

	for ( WORD iVertexIndex = 0, i = 0; i < MAX_BUFFER_QUADS; ++i, iVertexIndex += 4 )
	{
		// Setup the indices.
		(*pIndices++) = iVertexIndex;
		(*pIndices++) = iVertexIndex + 1;
		(*pIndices++) = iVertexIndex + 2;

		(*pIndices++) = iVertexIndex;
		(*pIndices++) = iVertexIndex + 2;
		(*pIndices++) = iVertexIndex + 3;
	}

	// Unlock the buffers.
	m_pVertexBuffer->Unlock();
	m_pIndexBuffer->Unlock();

	//////////////////////////////////////////////////////////////////////////
	// Load the effect file and needed techniques.
	//////////////////////////////////////////////////////////////////////////

	LPD3DXBUFFER pCompilationErrors;
	DWORD g_dwShaderFlags = 0;

	D3DXMACRO Defines[ 8 ];
	int iCurDefine = 0;

	// Required!
	{
		D3DXMACRO Macro = { "MAX_CONST_ARRAY_QUADS", MAX_CONST_ARRAY_QUADS_S };
		Defines[ iCurDefine++ ] = Macro;
	}

	// Required!
	{
		D3DXMACRO Macro = { NULL, NULL };
		Defines[ iCurDefine++ ] = Macro;
	}

	HRESULT hr = D3DXCreateEffectFromFile( g_pd3dDevice, L"Data/Font.fx", Defines, NULL, g_dwShaderFlags, NULL, &m_pEffect, &pCompilationErrors );
	if ( FAILED( hr ) )
	{
		assert( D3DERR_INVALIDCALL != hr );
		assert( D3DXERR_INVALIDDATA != hr );
		assert( E_OUTOFMEMORY != hr );

		WCHAR wsz[ 256 ];
		MultiByteToWideChar( CP_ACP, 0, (LPSTR)pCompilationErrors->GetBufferPointer(), -1, wsz, 256 );
		wsz[ 255 ] = 0;
		DXUTTrace( __FILE__, (DWORD)__LINE__, E_FAIL, wsz, true );

		exit( 1 );

		return;
	}

	s_hRenderFontTech = FindTechnique( "RenderGPUFont" );
	s_hRenderQuadTech = FindTechnique( "RenderGPUQuad" );

	// Cache handles for efficiency.
	m_Handles.g_f4PositionArray = m_pEffect->GetParameterByName( NULL, "g_f4PositionArray" );
	m_Handles.g_f4TCArray = m_pEffect->GetParameterByName( NULL, "g_f4TCArray" );
	m_Handles.g_f4ColorsArray = m_pEffect->GetParameterByName( NULL, "g_f4ColorsArray" );
	m_Handles.g_Texture = m_pEffect->GetParameterByName( NULL, "g_Texture" );
	m_Handles.g_f4DebugBatchColor = m_pEffect->GetParameterByName( NULL, "g_f4DebugBatchColor" );
	m_Handles.g_iConstArrays = m_pEffect->GetParameterByName( NULL, "g_iConstArrays" );
	m_Handles.g_bAA = m_pEffect->GetParameterByName( NULL, "g_bAA" );
	m_Handles.g_f2TexelSize = m_pEffect->GetParameterByName( NULL, "g_f2TexelSize" );
	m_Handles.g_f2InvScreenSize = m_pEffect->GetParameterByName( NULL, "g_f2InvScreenSize" );
}

// Destroy the Model.
void CArGuiModel::Destroy()
{
	ResetStreams();

	SAFE_RELEASE( m_pVertexBuffer );
	SAFE_RELEASE( m_pIndexBuffer );

	SAFE_RELEASE( m_pAttributesStreamBuffer );

	SAFE_RELEASE( m_pBaseInstanceBuffer );
	SAFE_RELEASE( m_pAttributesInstanceBuffer );

	SAFE_RELEASE( m_pConstArrayInstanceBuffer );

	SAFE_RELEASE( m_pDecl );

	SAFE_RELEASE( m_pEffect );
}

// Reset to default values.
void CArGuiModel::Reset()
{
	m_QuadList.clear();
}

void CArGuiModel::UpdateBuffers( int iNumElements )
{
	int iBytesToLock = iNumElements * sizeof( GPU_QUAD_VERTEX_POS_TC_COLOR ) * 4;

	PERF_BEGIN( "Updating Vertex Buffer" );
	GPU_QUAD_VERTEX_POS_TC_COLOR *v;
	// NOTE: Discard here is VERY important!
	m_pAttributesStreamBuffer->Lock( 0, iBytesToLock, (void**)&v, D3DLOCK_DISCARD );
	for ( int i = 0; i < iNumElements; ++i, v += 4 )
	{
		D3DCOLOR Color = D3DCOLOR_COLORVALUE( m_vColorsArray[ i ].x, m_vColorsArray[ i ].y, m_vColorsArray[ i ].z, m_vColorsArray[ i ].w );

		//////////////////////////////////////////////////////////////////////////
		// In-place expand the vertices here.
		//////////////////////////////////////////////////////////////////////////

		// Left Top.
		v[ 0 ].Position.x = m_vPositionArray[ i ].x;
		v[ 0 ].Position.y = m_vPositionArray[ i ].y;
		v[ 0 ].Position.z = 0.0f;
		v[ 0 ].Position.w = 0.0f;
		v[ 0 ].Texcoord.x = m_vTCArray[ i ].x;
		v[ 0 ].Texcoord.y = m_vTCArray[ i ].y;
		v[ 0 ].Texcoord.z = 0.0f;
		v[ 0 ].Texcoord.w = 0.0f;
		v[ 0 ].Color = Color;

		// Right Top.
		v[ 1 ].Position.x = m_vPositionArray[ i ].x + m_vPositionArray[ i ].z;
		v[ 1 ].Position.y = m_vPositionArray[ i ].y;
		v[ 1 ].Position.z = 0.0f;
		v[ 1 ].Position.w = 0.0f;
		v[ 1 ].Texcoord.x = m_vTCArray[ i ].x + m_vTCArray[ i ].z;
		v[ 1 ].Texcoord.y = m_vTCArray[ i ].y;
		v[ 1 ].Texcoord.z = 0.0f;
		v[ 1 ].Texcoord.w = 0.0f;
		v[ 1 ].Color = Color;

		// Right Bottom.
		v[ 2 ].Position.x = m_vPositionArray[ i ].x + m_vPositionArray[ i ].z;
		v[ 2 ].Position.y = m_vPositionArray[ i ].y + m_vPositionArray[ i ].w;
		v[ 2 ].Position.z = 0.0f;
		v[ 2 ].Position.w = 0.0f;
		v[ 2 ].Texcoord.x = m_vTCArray[ i ].x + m_vTCArray[ i ].z;
		v[ 2 ].Texcoord.y = m_vTCArray[ i ].y + m_vTCArray[ i ].w;
		v[ 2 ].Texcoord.z = 0.0f;
		v[ 2 ].Texcoord.w = 0.0f;
		v[ 2 ].Color = Color;

		// Left Bottom.
		v[ 3 ].Position.x = m_vPositionArray[ i ].x;
		v[ 3 ].Position.y = m_vPositionArray[ i ].y + m_vPositionArray[ i ].w;
		v[ 3 ].Position.z = 0.0f;
		v[ 3 ].Position.w = 0.0f;
		v[ 3 ].Texcoord.x = m_vTCArray[ i ].x;
		v[ 3 ].Texcoord.y = m_vTCArray[ i ].y + m_vTCArray[ i ].w;
		v[ 3 ].Texcoord.z = 0.0f;
		v[ 3 ].Texcoord.w = 0.0f;
		v[ 3 ].Color = Color;
	}
	m_pAttributesStreamBuffer->Unlock();
	PERF_END();
}

void CArGuiModel::UpdateStreams( int iNumElements )
{
	int iBytesToLock = iNumElements * sizeof( GPU_QUAD_VERTEX_POS_TC_COLOR ) * 4;

	PERF_BEGIN( "Updating Vertex Buffer" );
	GPU_QUAD_VERTEX_POS_TC_COLOR *v;
	// NOTE: Discard here is VERY important!
	m_pAttributesStreamBuffer->Lock( 0, iBytesToLock, (void**)&v, D3DLOCK_DISCARD );
	for ( int i = 0; i < iNumElements; ++i )
	{
		D3DCOLOR Color = D3DCOLOR_COLORVALUE( m_vColorsArray[ i ].x, m_vColorsArray[ i ].y, m_vColorsArray[ i ].z, m_vColorsArray[ i ].w );

		//////////////////////////////////////////////////////////////////////////
		// All vertices of the additional stream buffer must mirror that of the original buffer.
		//////////////////////////////////////////////////////////////////////////

		for ( int j = 0; j < 4; ++j, v++ )
		{
			v->Position = m_vPositionArray[ i ];
			v->Texcoord = m_vTCArray[ i ];
			v->Color = Color;
		}
	}
	m_pAttributesStreamBuffer->Unlock();
	PERF_END();
}

void CArGuiModel::UpdateInstancing( int iNumElements )
{
	//////////////////////////////////////////////////////////////////////////
	// Only modify a single copy of the instanced data (if instancing is supported).
	//////////////////////////////////////////////////////////////////////////

	int iBytesToLock = iNumElements * sizeof( GPU_QUAD_VERTEX_POS_TC_COLOR );

	PERF_BEGIN( "Updating Vertex Buffer" );
	GPU_QUAD_VERTEX_POS_TC_COLOR *v;
	// NOTE: Discard here is VERY important!
	m_pAttributesInstanceBuffer->Lock( 0, iBytesToLock, (void**)&v, D3DLOCK_DISCARD );
	for ( int i = 0; i < iNumElements; ++i, v++ )
	{
		v->Position = m_vPositionArray[ i ];
		v->Texcoord = m_vTCArray[ i ];
		v->Color = D3DCOLOR_COLORVALUE( m_vColorsArray[ i ].x, m_vColorsArray[ i ].y, m_vColorsArray[ i ].z, m_vColorsArray[ i ].w );
	}
	m_pAttributesInstanceBuffer->Unlock();
	PERF_END();
}

void CArGuiModel::BindStreams()
{
	PERF_SCOPE( "Set Streams" );

	switch ( m_GeomTechnique )
	{
		case GTECH_BUFFERS:
		{
			//g_pd3dDevice->SetStreamSource( 0, NULL, 0, 0 );
			g_pd3dDevice->SetStreamSource( 1, m_pAttributesStreamBuffer, 0, sizeof( GPU_QUAD_VERTEX_POS_TC_COLOR ) );
		}
		break;

		case GTECH_CONSTARRAYS_INST:
		{
			g_pd3dDevice->SetStreamSource( 0, m_pBaseInstanceBuffer, 0, sizeof( GPU_QUAD_VERTEX ) );
			g_pd3dDevice->SetStreamSource( 2, m_pConstArrayInstanceBuffer, 0, sizeof( GPU_QUAD_VERTEX ) );
		}
		break;

		case GTECH_CONSTARRAYS:
		{
			g_pd3dDevice->SetStreamSource( 0, m_pVertexBuffer, 0, sizeof( GPU_QUAD_VERTEX ) );
			//g_pd3dDevice->SetStreamSource( 2, NULL, 0, 0 );
		}
		break;

		case GTECH_STREAMS:
		{
			g_pd3dDevice->SetStreamSource( 0, m_pVertexBuffer, 0, sizeof( GPU_QUAD_VERTEX ) );
			g_pd3dDevice->SetStreamSource( 1, m_pAttributesStreamBuffer, 0, sizeof( GPU_QUAD_VERTEX_POS_TC_COLOR ) );
		}
		break;

		case GTECH_INSTANCING:
		{
			g_pd3dDevice->SetStreamSource( 0, m_pBaseInstanceBuffer, 0, sizeof( GPU_QUAD_VERTEX ) );
			g_pd3dDevice->SetStreamSource( 1, m_pAttributesInstanceBuffer, 0, sizeof( GPU_QUAD_VERTEX_POS_TC_COLOR ) );
		}
		break;
	}
}

void CArGuiModel::ResetStreams()
{
	// Reset instance stream sources.
	if ( GTECH_INSTANCING == m_GeomTechnique || GTECH_CONSTARRAYS_INST == m_GeomTechnique )
	{	
		g_pd3dDevice->SetStreamSourceFreq( 0, 1 );
		g_pd3dDevice->SetStreamSourceFreq( 1, 1 );
		g_pd3dDevice->SetStreamSourceFreq( 2, 1 );
	}

	g_pd3dDevice->SetStreamSource( 0, NULL, 0, 0 );
	g_pd3dDevice->SetStreamSource( 1, NULL, 0, 0 );
	g_pd3dDevice->SetStreamSource( 2, NULL, 0, 0 );
}

// Apply the new states for the current batch.
bool CArGuiModel::ApplyBatchStates( D3DXHANDLE hTechnique, LPDIRECT3DTEXTURE9 pTexture )
{
	//////////////////////////////////////////////////////////////////////////
	// Apply the states that broke the batch here individually so as to not have
	// to change every state.
	//////////////////////////////////////////////////////////////////////////

	bool bNewTechinque = m_hCurTechnique != hTechnique;
	bool bNewTexture = m_pCurTexture != pTexture;

	if ( bNewTexture )
	{
		m_pCurTexture = pTexture;

		m_pEffect->SetTexture( m_Handles.g_Texture, m_pCurTexture );
	}

	if ( bNewTechinque )
	{
		m_hCurTechnique = hTechnique;

		m_pEffect->EndPass();
		m_pEffect->End();

		m_pEffect->SetTechnique( m_hCurTechnique );

		// Changing the technique requires a new effect begin.
		UINT cPasses;
		m_pEffect->Begin( &cPasses, D3DXFX_DONOTSAVESTATE );
		m_pEffect->BeginPass( 0 );
	}

	return bNewTechinque || bNewTexture;
}

// Render a single batch.
void CArGuiModel::RenderBatch( int iNumElements )
{
	PERF_SCOPE( "CArGuiModel::RenderBatch" );

#if 0
	// Set the rest of the quads to degenerate nothingness.
	for ( int i = iNumElements; i < MAX_CONST_ARRAY_QUADS; ++i )
	{
		m_vPositionArray[ i ] = D3DXVECTOR4( 0.0f, 0.0f, 0.0f, 0.0f );
	}
#endif

#if 0
	// Flush old values for testing purposes.
	D3DXVECTOR4 ZeroArray[ MAX_CONST_ARRAY_QUADS ];
	memset( ZeroArray, 0, sizeof( D3DXVECTOR4 ) * MAX_CONST_ARRAY_QUADS );
	m_pEffect->SetVectorArray( "g_f4PositionArray", ZeroArray, MAX_CONST_ARRAY_QUADS );
	m_pEffect->SetVectorArray( "g_f4TCArray", ZeroArray, MAX_CONST_ARRAY_QUADS );
	m_pEffect->SetVectorArray( "g_f4ColorsArray", ZeroArray, MAX_CONST_ARRAY_QUADS );
#endif

	// Debug info.
	if ( m_bDebugShowBatchColors )
	{
		m_pEffect->SetVector( m_Handles.g_f4DebugBatchColor, &g_vDebugColors[ g_PC.m_iNumBatchsDrawn % NUM_DEBUG_COLORS ] );
	}

	//////////////////////////////////////////////////////////////////////////
	// Upload instance data.
	// NOTE: A number of these calls (like SetStreamSource()) should be state checked
	// in a real development environment.
	//////////////////////////////////////////////////////////////////////////

	switch ( m_GeomTechnique )
	{
		case GTECH_CONSTARRAYS_INST:
		{
			g_pd3dDevice->SetStreamSourceFreq( 0, D3DSTREAMSOURCE_INDEXEDDATA | iNumElements );
			g_pd3dDevice->SetStreamSourceFreq( 2, D3DSTREAMSOURCE_INSTANCEDATA | (UINT)1 );
		}
		case GTECH_CONSTARRAYS:
		{
			// Upload the quad attributes.
			PERF_BEGIN( "Updating Constant Arrays" );
			m_pEffect->SetVectorArray( m_Handles.g_f4PositionArray, m_vPositionArray, iNumElements );
			m_pEffect->SetVectorArray( m_Handles.g_f4TCArray, m_vTCArray, iNumElements );
			m_pEffect->SetVectorArray( m_Handles.g_f4ColorsArray, m_vColorsArray, iNumElements );
			PERF_END();
		}
		break;

		case GTECH_BUFFERS:
		{
			UpdateBuffers( iNumElements );
		}
		break;

		case GTECH_STREAMS:
		{
			UpdateStreams( iNumElements );
		}
		break;

		case GTECH_INSTANCING:
		{
			UpdateInstancing( iNumElements );

			g_pd3dDevice->SetStreamSourceFreq( 0, D3DSTREAMSOURCE_INDEXEDDATA | iNumElements );
			g_pd3dDevice->SetStreamSourceFreq( 1, UINT( D3DSTREAMSOURCE_INSTANCEDATA | 1 ) );
		}
		break;
	}

	//////////////////////////////////////////////////////////////////////////
	// Render geometry.
	//////////////////////////////////////////////////////////////////////////

	int iNumVertices = iNumElements * 4;
	int iNumPrims = iNumElements * 2;

	// Commit any pending state changes.
	PERF_BEGIN( "Commit State Changes" );
	m_pEffect->CommitChanges();
	PERF_END();

	PERF_BEGIN( "Drawing Primitive" );
	// If instancing we draw as if for the original geometry (the four vertex, two triangle quad).
	if ( GTECH_INSTANCING == m_GeomTechnique || GTECH_CONSTARRAYS_INST == m_GeomTechnique )
	{
		g_pd3dDevice->DrawIndexedPrimitive( D3DPT_TRIANGLELIST, 0, 0, 4, 0, 2 );
	}
	else
	{
		g_pd3dDevice->DrawIndexedPrimitive( D3DPT_TRIANGLELIST, 0, 0, iNumVertices, 0, iNumPrims );
	}
	PERF_END();

	// Increment performance counters.
	g_PC.m_iNumVerticesDrawn += iNumVertices;
	g_PC.m_iNumBatchsDrawn++;
	g_PC.m_iNumTrisDrawn += iNumPrims;
}

// Render the model.
void CArGuiModel::Render()
{
	PERF_SCOPE( "CArGuiModel::Render" );

	g_PC.m_iNumQuadsDrawn = (int)m_QuadList.size();

	DWORD dwStartMS = timeGetTime();

	//////////////////////////////////////////////////////////////////////////
	// Prepare initial shared states.
	//////////////////////////////////////////////////////////////////////////

	m_pEffect->SetVector( m_Handles.g_f4DebugBatchColor, &VEC4_ONE );
	int iConstArraysTechnique = GTECH_CONSTARRAYS == m_GeomTechnique ? 1 : GTECH_CONSTARRAYS_INST == m_GeomTechnique ? 2 : 0;
	m_pEffect->SetInt( m_Handles.g_iConstArrays, iConstArraysTechnique );
	m_pEffect->SetBool( m_Handles.g_bAA, m_bUseAA );
	m_pEffect->SetVector( m_Handles.g_f2TexelSize, &g_vTexelSize );
	m_pEffect->SetVector( m_Handles.g_f2InvScreenSize, &g_vInvScreenSize );

	g_pd3dDevice->SetFVF( NULL );
	g_pd3dDevice->SetVertexDeclaration( m_pDecl );
	g_pd3dDevice->SetIndices( m_pIndexBuffer );

	m_pEffect->SetTexture( m_Handles.g_Texture, NULL );

	BindStreams();

	//////////////////////////////////////////////////////////////////////////
	// Collate and render batches.
	//////////////////////////////////////////////////////////////////////////

	const int iMaxQuads = ( GTECH_CONSTARRAYS_INST == m_GeomTechnique || GTECH_CONSTARRAYS == m_GeomTechnique ) ? MAX_CONST_ARRAY_QUADS : MAX_BUFFER_QUADS;

	m_hCurTechnique = NULL;
	m_pCurTexture = NULL;
	int iCurElem = 0;

	UINT cPasses;
	m_pEffect->Begin( &cPasses, D3DXFX_DONOTSAVESTATE );
	m_pEffect->BeginPass( 0 );

	// Prepare the attribute arrays and render each group matching it's batch parameters.
	vector< CArGuiQuad >::iterator iterQuad = m_QuadList.begin();
	for ( ; iterQuad != m_QuadList.end(); ++iterQuad, iCurElem++ )
	{
		CArGuiQuad &Quad = (*iterQuad);

		bool bNewTechinque = m_hCurTechnique != Quad.m_hTechnique;
		bool bNewTexture = m_pCurTexture != Quad.m_pTexture;

		bool bStateChanged = bNewTechinque || bNewTexture;

		// If we've broken the batch or are about to draw more quads than we can...
		if ( bStateChanged || iCurElem >= iMaxQuads )
		{
			//////////////////////////////////////////////////////////////////////////
			// Render the batch using the current parameters.
			//////////////////////////////////////////////////////////////////////////

			if ( m_hCurTechnique )
			{
				RenderBatch( iCurElem );
			}

			//////////////////////////////////////////////////////////////////////////
			// Apply new states (if there was a change).
			//////////////////////////////////////////////////////////////////////////

			if ( bStateChanged )
			{
				ApplyBatchStates( Quad.m_hTechnique, Quad.m_pTexture );
			}

			//////////////////////////////////////////////////////////////////////////
			// Restart the batch.
			//////////////////////////////////////////////////////////////////////////

			iCurElem = 0;
		}

		//////////////////////////////////////////////////////////////////////////
		// Set quad attributes for the current element.
		//////////////////////////////////////////////////////////////////////////

		// Set position/size (NOTE: screen space coordinates are transformed to clip space).
		// OPTIMIZATION: Dims.y negated for the proper math to work out in the shader.
		m_vPositionArray[ iCurElem ] = D3DXVECTOR4( -1.0f + Quad.m_vPos.x * g_vInvScreenSize.x, 1.0f - Quad.m_vPos.y * g_vInvScreenSize.y,
													Quad.m_vDims.x * g_vInvScreenSize.x, -Quad.m_vDims.y * g_vInvScreenSize.y );

		// Set tex dims.
		m_vTCArray[ iCurElem ] = Quad.m_vTexDims;

		// Set color.
		m_vColorsArray[ iCurElem ] = D3DXVECTOR4( Quad.m_Color );
	}

	//////////////////////////////////////////////////////////////////////////
	// Render the last remaining batch.
	//////////////////////////////////////////////////////////////////////////
	
	RenderBatch( iCurElem );

	//////////////////////////////////////////////////////////////////////////
	// End the technique/effect and reset states.
	//////////////////////////////////////////////////////////////////////////

	m_pEffect->EndPass();
	m_pEffect->End();

	Reset();
	ResetStreams();

	g_PC.m_iRenderMs = timeGetTime() - dwStartMS;
}

// Draw a 2D rectangle on the screen.
void CArGuiModel::DrawRect( int iPosX, int iPosY, int iWidth, int iHeight, const D3DXCOLOR &Color, LPDIRECT3DTEXTURE9 pTexture, D3DXHANDLE hTechnique /*= s_hRenderQuadTech*/ )
{
	DrawRectTc( iPosX, iPosY, iWidth, iHeight, D3DXVECTOR4( 0.0f, 0.0f, 1.0f, 1.0f ), Color, pTexture, hTechnique );
}

void CArGuiModel::DrawRectTc( int iPosX, int iPosY, int iWidth, int iHeight, const D3DXVECTOR4 &vTexcoords, const D3DXCOLOR &Color, LPDIRECT3DTEXTURE9 pTexture, D3DXHANDLE hTechnique /*= s_hRenderQuadTech*/ )
{
	if ( Color.a < 0.001f )
	{
		return;
	}

	CArGuiQuad Quad;

	Quad.m_vPos.x = (float)iPosX;
	Quad.m_vPos.y = (float)iPosY;

	Quad.m_vDims.x = (float)iWidth;
	Quad.m_vDims.y = (float)iHeight;

	Quad.m_vTexDims = vTexcoords;

	Quad.m_Color = Color;

	//Quad.m_pEffect = m_pEffect;
	Quad.m_hTechnique = hTechnique;

	Quad.m_pTexture = pTexture;

	m_QuadList.push_back( Quad );
}


// Draw some 2D text.
void CArGuiModel::DrawText( CArBitmapFont *pFont, const char *strText, int iPosX, int iPosY, const D3DXCOLOR &Color, float fFontSize )
{
	if ( Color.a < 0.001f )
	{
		return;
	}

	CArGuiQuad Quad;
	float fOffsetX, fOffsetY;
	float fAdvance = 0.0f;
	float fKernOffset;
	int iNumChars = (int)strlen( strText );

	// Go through all the characters and create character references.
	for ( int i = 0; i < iNumChars; i++ )
	{
		CArCharDesc *pCharDesc = pFont->FindCharByASCII( strText[ i ] );

		Quad.m_hTechnique = s_hRenderFontTech;

		fOffsetX = pCharDesc->m_fOffsetX * fFontSize;
		fOffsetY = pCharDesc->m_fOffsetY * fFontSize;

		Quad.m_Color = Color;

		Quad.m_vPos.x = iPosX + fOffsetX + fAdvance;
		Quad.m_vPos.y = iPosY + fOffsetY;

		Quad.m_vDims.x = pCharDesc->m_fWidth * fFontSize;
		Quad.m_vDims.y = pCharDesc->m_fHeight * fFontSize;

		// Setup the texcoords.
		Quad.m_vTexDims = D3DXVECTOR4( pCharDesc->m_fU0, pCharDesc->m_fV0, pCharDesc->m_fU1, pCharDesc->m_fV1 );

		// Set the texture.
		Quad.m_pTexture = pFont->m_pFontPages[ pCharDesc->m_iFontPage ];

		// Prepare kerning.
		fKernOffset = 0.0f;
		if ( ( i + 1 ) < iNumChars )
		{
			// Grab the next character.
			CArCharDesc *pNxtCharDesc = pFont->FindCharByASCII( strText[ i + 1 ] );

			// See if we can find that character in the current characters kerning list.
			vector< CArCharKern >::iterator iterKern = pCharDesc->m_KernList.begin();
			for ( ; iterKern != pCharDesc->m_KernList.end(); ++iterKern )
			{
				// If the character was found...
				if ( pNxtCharDesc->m_iASCII == (*iterKern).m_iId )
				{
					// Set the kerning offset.
					fKernOffset = (*iterKern).m_fOffset * fFontSize;
					break;
				}
			}
		}

		// Advance to next character.
		fAdvance += pCharDesc->m_fAdvance * fFontSize + fKernOffset;

		// Don't add the quad to the list if it has an invalid size (ex. whitespace).
		if ( Quad.m_vDims.y == 0.0f || Quad.m_vDims.x == 0.0f )
		{
			continue;
		}

		// Add the quad to the list.
		m_QuadList.push_back( Quad );
	}
}