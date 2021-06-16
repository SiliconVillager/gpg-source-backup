// GUIModel.h: A model to cache GUI draw calls.
// Created by: Aurelio Reis

#ifndef __AR__GUI__MODEL__H__
#define __AR__GUI__MODEL__H__

#include "BitmapFont.h"


static const int MAX_CONST_ARRAY_QUADS = 82;
static const char MAX_CONST_ARRAY_QUADS_S[] = "82";
static const int MAX_BUFFER_QUADS = 4096;


class CArGuiQuad
{
public:
	// The screen position.
	D3DXVECTOR2 m_vPos;

	// The width and height dimensions.
	D3DXVECTOR2 m_vDims;

	// The texture coordinate dimensions.
	D3DXVECTOR4 m_vTexDims;

	// The color.
	D3DXCOLOR m_Color;

	// The technique to use.
	D3DXHANDLE m_hTechnique;

	// The texture to apply.
	LPDIRECT3DTEXTURE9 m_pTexture;

	// Constructor.
	CArGuiQuad()
	{
		m_Color = D3DCOLOR_RGBA( 255, 255, 255, 255 );
		//m_pEffect = NULL;
		m_hTechnique = NULL;
		m_pTexture = NULL;
	}

	// Destructor.
	~CArGuiQuad() {}
};


class CArGuiModel
{
public:
	enum EGeomTech
	{
		GTECH_BUFFERS,
		GTECH_CONSTARRAYS,
		GTECH_CONSTARRAYS_INST,
		GTECH_STREAMS,
		GTECH_INSTANCING
	};

private:
	// The static GUI geometry.
	IDirect3DVertexBuffer9 *m_pVertexBuffer;
	IDirect3DIndexBuffer9 *m_pIndexBuffer;
	IDirect3DVertexDeclaration9 *m_pDecl;

	// The dynamic stream buffer.
	IDirect3DVertexBuffer9 *m_pAttributesStreamBuffer;

	// The base buffer to be instanced (a single quad).
	IDirect3DVertexBuffer9 *m_pBaseInstanceBuffer;

	// The buffer used for instance data.
	IDirect3DVertexBuffer9 *m_pAttributesInstanceBuffer;

	// The buffer used for constant array instance data.
	IDirect3DVertexBuffer9 *m_pConstArrayInstanceBuffer;

	// The attribute arrays.
	D3DXVECTOR4 m_vPositionArray[ MAX_BUFFER_QUADS ];
	D3DXVECTOR4 m_vTCArray[ MAX_BUFFER_QUADS ];
	D3DXVECTOR4 m_vColorsArray[ MAX_BUFFER_QUADS ];

	// All the GPU quads to draw.
	vector< CArGuiQuad > m_QuadList;

	// The main effect.
	LPD3DXEFFECT	m_pEffect;

	// The standard font rendering techniques.
	static D3DXHANDLE		s_hRenderFontTech;
	static D3DXHANDLE		s_hRenderQuadTech;

	// Used during Render().
	D3DXHANDLE m_hCurTechnique;
	LPDIRECT3DTEXTURE9 m_pCurTexture;

	// The geometric technique to use.
	EGeomTech m_GeomTechnique;

	// Use font anti-aliasing.
	bool m_bUseAA;

	// Show batch colors (Debug Option).
	bool m_bDebugShowBatchColors;

	struct SParamHandles
	{
		D3DXHANDLE g_f4PositionArray;
		D3DXHANDLE g_f4TCArray;
		D3DXHANDLE g_f4ColorsArray;
		D3DXHANDLE g_Texture;
		D3DXHANDLE g_f4DebugBatchColor;
		D3DXHANDLE g_iConstArrays;
		D3DXHANDLE g_bAA;
		D3DXHANDLE g_f2TexelSize;
		D3DXHANDLE g_f2InvScreenSize;
	} m_Handles;

	void UpdateBuffers( int iNumElements );
	void UpdateStreams( int iNumElements );
	void UpdateInstancing( int iNumElements );

	void BindStreams();
	void ResetStreams();

	// Apply the new states for the current batch.
	bool ApplyBatchStates( D3DXHANDLE hTechnique, LPDIRECT3DTEXTURE9 pTexture );

	// Render a single batch.
	void RenderBatch( int iNumElements );

public:
	// Constructor.
	CArGuiModel();

	// Destructor.
	~CArGuiModel();

	// Initialize the Model.
	void Initialize();

	// Destroy the Model.
	void Destroy();

	// Reset to default values.
	void Reset();

	// Render the model.
	void Render();

	// Draw a 2D rectangle on the screen.
	void DrawRect( int iPosX, int iPosY, int iWidth, int iHeight, const D3DXCOLOR &Color, LPDIRECT3DTEXTURE9 pTexture, D3DXHANDLE hTechnique = s_hRenderQuadTech );
	void DrawRectTc( int iPosX, int iPosY, int iWidth, int iHeight, const D3DXVECTOR4 &vTexcoords, const D3DXCOLOR &Color, LPDIRECT3DTEXTURE9 pTexture, D3DXHANDLE hTechnique = s_hRenderQuadTech );

	// Draw some 2D text.
	void DrawText( CArBitmapFont *pFont, const char *strText, int iPosX, int iPosY, const D3DXCOLOR &Color, float fFontSize );

	D3DXHANDLE FindTechnique( const char *strName  ) { return m_pEffect->GetTechniqueByName( strName ); }

	void SetGeomTechnique( EGeomTech GeomTechnique ) { m_GeomTechnique = GeomTechnique; }
	void SetUseAA( bool bUseAA ) { m_bUseAA = bUseAA; }
	void SetDebugShowBatchColors( bool bDebugShowBatchColors ) { m_bDebugShowBatchColors = bDebugShowBatchColors; }
	bool GetDebugShowBatchColors() const { return m_bDebugShowBatchColors; }
};


#endif // __AR__GUI__MODEL__H__