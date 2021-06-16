// Model.cpp:
// Date: 11/24/08
// Author: Aurelio Reis

#include "Engine/Shared/Model.h"
#include "Engine/Shared/Renderer_Platform.h"


//////////////////////////////////////////////////////////////////////////

#define VAR_OFFSET( _Obj, _Var )		(uint32)&((_Obj *)0)->_Var
uint32 VertexFormat[ 3 ] = { VAR_OFFSET( CArVertex, m_fTexCoords ), 
							 VAR_OFFSET( CArVertex, m_uiColor ),
							 VAR_OFFSET( CArVertex, m_vPos ) };
GLuint uiBuffer[ 3 ];


CArGuiModel::CArGuiModel()
{
	// One default mesh.
	m_uiNumMeshes = 1;
	m_pMeshes = new CArMesh[ m_uiNumMeshes ];

	uint32 uiMaxBoxes = 256;

	// 4 corners per-box.
	m_pMeshes[ 0 ].m_uiNumVertices = uiMaxBoxes * 4;
	m_pMeshes[ 0 ].m_pVertices = new CArVertex[ m_pMeshes[ 0 ].m_uiNumVertices ];

	// 6 indices per-box (including degenerate point).
	m_pMeshes[ 0 ].m_sNumIndices = uiMaxBoxes * 6 - 2;
	m_pMeshes[ 0 ].m_pIndices = new int16[ m_pMeshes[ 0 ].m_sNumIndices ];
}

CArGuiModel::~CArGuiModel()
{
}

void CArGuiModel::Create()
{
#if 0
	glGenBuffers( 3, uiBuffer );

	glBindBuffer( GL_ARRAY_BUFFER, uiBuffer[ 0 ] );
	glBufferData( GL_ARRAY_BUFFER, m_pMeshes[ 0 ].m_uiNumVertices * sizeof( float ) * 2, &m_pMeshes[ 0 ].m_pVertices[ 0 ].m_fTexCoords[ 0 ], GL_STATIC_DRAW );
	glTexCoordPointer( 2, GL_FLOAT, 0, 0 );

	glBindBuffer( GL_ARRAY_BUFFER, uiBuffer[ 1 ] );
	glBufferData( GL_ARRAY_BUFFER, m_pMeshes[ 0 ].m_uiNumVertices * sizeof( byte ), &m_pMeshes[ 0 ].m_pVertices[ 0 ].m_uiColor, GL_STATIC_DRAW );
	glColorPointer( 4, GL_UNSIGNED_BYTE, 0, 0 );

	glBindBuffer( GL_ARRAY_BUFFER, uiBuffer[ 0 ] );
	glBufferData( GL_ARRAY_BUFFER, m_pMeshes[ 0 ].m_uiNumVertices * sizeof( CArVector3 ), &m_pMeshes[ 0 ].m_pVertices[ 0 ].m_vPos, GL_STATIC_DRAW );
	glVertexPointer( 2, GL_FLOAT, 0, 0 );

	glEnableClientState( GL_TEXTURE_COORD_ARRAY );
	glEnableClientState( GL_COLOR_ARRAY );
	glEnableClientState( GL_VERTEX_ARRAY );
#endif

	// Delete the CPU copy, everything is in video memory now.
	//m_uiNumMeshes = 0;
	//SAFE_DELETE_ARRAY( m_pMeshes );
}

void CArGuiModel::Destroy()
{
#if 0
	glDeleteBuffers( 3, uiBuffer );
#endif
}

void CArGuiModel::Render()
{	
	AR_DebugMessage( "Rendering GUI Model with %d boxes\n", (int)m_Boxes.size() );

	CArMesh *pMesh = &m_pMeshes[ 0 ];

	uint32 uiCurIdx = 0;
	CArVertex *pCurVert = pMesh->m_pVertices;
	int16 *pCurIdx = pMesh->m_pIndices;

	CArTexture *pCurTexture = NULL;
	float fCurRotation = 0.0f;
	uint32 uiNumIndices = 0;

	vector< CArBoxDef >::iterator iterBox = m_Boxes.begin();
	for ( ; iterBox != m_Boxes.end(); ++iterBox )
	{
		CArBoxDef &CurBox = (*iterBox);

		if ( pCurTexture != CurBox.m_pTexture || fCurRotation != CurBox.m_fRotation )
		{
			fCurRotation = CurBox.m_fRotation;

			// Render the mesh with all the boxes baked and reset so we can start over.
			// NOTE: 6 indices per-box including degenerates. Remaining two degenerates points are not accounted for.
			if ( 0 != uiNumIndices )
			{
				RENDERER->RenderMesh( pMesh, uiNumIndices - 2 );
			}

			uiNumIndices = 0;
			pCurTexture = CurBox.m_pTexture;
			uiCurIdx = 0;
			pCurVert = pMesh->m_pVertices;
			pCurIdx = pMesh->m_pIndices;

			// Bind the image.
			glBindTexture( GL_TEXTURE_2D, pCurTexture->m_uiGlTextureID );
		}

		CArVertex NuVert;

		CArPosSize PosSize = CurBox.m_PosSize;
		glm::vec2 vOffset;

		if ( fabs( fCurRotation ) >= 0.0001f )
		{
			glm::vec2 vOrigin = -CurBox.m_vRotationOrigin;

			vOffset = glm::vec2( PosSize.x - vOrigin.x, PosSize.y - vOrigin.y );
			PosSize.x = vOrigin.x;
			PosSize.y = vOrigin.y;
		}

		// Convert the pos-size to rectangle.
		CArRect Dims = PosSize;

		glm::mat2x2 rot;
		rot = glm::rotate2D( rot, fCurRotation );
		
		glm::vec2 v0;

		// NOTE: TC y is negated because of glOrtho trick above.

		v0 = glm::vec2( Dims.left, Dims.top );
		v0 = rot * v0 + vOffset;
		NuVert.m_vPos = CArVector3( v0.x, v0.y, 0.0f );

		NuVert.m_fTexCoords[ 0 ] = CurBox.m_TcDims.left;	NuVert.m_fTexCoords[ 1 ] = -CurBox.m_TcDims.top;
		NuVert.m_uiColor = CurBox.m_uiColor;
		*pCurVert++ = NuVert;

		v0 = glm::vec2( Dims.left, Dims.bottom );
		v0 = rot * v0 + vOffset;
		NuVert.m_vPos = CArVector3( v0.x, v0.y, 0.0f );

		NuVert.m_fTexCoords[ 0 ] = CurBox.m_TcDims.left;	NuVert.m_fTexCoords[ 1 ] = -CurBox.m_TcDims.bottom;
		NuVert.m_uiColor = CurBox.m_uiColor;
		*pCurVert++ = NuVert;

		v0 = glm::vec2( Dims.right, Dims.top );
		v0 = rot * v0 + vOffset;
		NuVert.m_vPos = CArVector3( v0.x, v0.y, 0.0f );

		NuVert.m_fTexCoords[ 0 ] = CurBox.m_TcDims.right;	NuVert.m_fTexCoords[ 1 ] = -CurBox.m_TcDims.top;
		NuVert.m_uiColor = CurBox.m_uiColor;
		*pCurVert++ = NuVert;
		
		v0 = glm::vec2( Dims.right, Dims.bottom );
		v0 = rot * v0 + vOffset;
		NuVert.m_vPos = CArVector3( v0.x, v0.y, 0.0f );

		NuVert.m_fTexCoords[ 0 ] = CurBox.m_TcDims.right;	NuVert.m_fTexCoords[ 1 ] = -CurBox.m_TcDims.bottom;
		NuVert.m_uiColor = CurBox.m_uiColor;
		*pCurVert++ = NuVert;
		
		*pCurIdx++ = uiCurIdx++;
		*pCurIdx++ = uiCurIdx++;
		*pCurIdx++ = uiCurIdx++;
		*pCurIdx++ = uiCurIdx;

		// Degenerate tri.
		*pCurIdx++ = uiCurIdx;	
		*pCurIdx++ = uiCurIdx + 1;	
		uiCurIdx++;

		uiNumIndices += 6;
	}

	// Render the remaining boxes.
	if ( 0 != uiNumIndices )
	{
		RENDERER->RenderMesh( pMesh, uiNumIndices - 2 );
	}

	m_Boxes.clear();

	glBindTexture( GL_TEXTURE_2D, 0 );
}

void CArGuiModel::DrawBox( CArBoxDef &Box )
{
	CArSemaphoreGuard Guard( *RENDERER->GetRenderSemaphore() );

	AR_VerifyWithError( Box.m_pTexture, "Trying to DrawBox with a NULL Texture!" );

	m_Boxes.push_back( Box );
}

void CArGuiModel::DrawText( CArGuiTextDesc *pTextDesc, const CArPosSize &PosSize )
{
	glm::vec2 vArea = pTextDesc->m_pFont->CalcTextArea( pTextDesc->m_strText );
	glm::vec2 vPos( PosSize.x, PosSize.y );

	if ( pTextDesc->m_Alignment & TALIGN_VERT_RIGHT )
	{
		vPos.x += PosSize.width;
		vPos.x -= vArea.x;
	}
	else if ( pTextDesc->m_Alignment & TALIGN_VERT_CENTER )
	{
		vPos.x += PosSize.width * 0.5f;
		vPos.x -= vArea.x * 0.5f;
	}

	if ( TALIGN_HORZ_BOTTOM & pTextDesc->m_Alignment )
	{
		vPos.y += PosSize.height;
		vPos.y -= vArea.y;
	}
	else if ( TALIGN_HORZ_CENTER & pTextDesc->m_Alignment )
	{
		vPos.y += PosSize.height * 0.5f;
		vPos.y -= vArea.y * 0.5f;
	}

	vPos += pTextDesc->m_vOffset;

	DrawText( pTextDesc->m_pFont, vPos.x, vPos.y, pTextDesc->m_strText, pTextDesc->m_uiColor );
}

void CArGuiModel::DrawText( CArFont *pFont, float x, float y, const char *strText, uint32 uiColor )
{
	CArSemaphoreGuard Guard( *RENDERER->GetRenderSemaphore() );

#if 0
	CArTextDef *pTextDef = new CArTextDef();
	pTextDef->x = x;
	pTextDef->y = y;
	pTextDef->m_Dims = CArRect( x, y, x + 50, y + 50 );
	pTextDef->m_uiColor = uiColor;
	pTextDef->m_strText = AR_CreateString( strText );
	pTextDef->m_pTexture = pFontTxtr;

	m_Text.push_back( pTextDef );
#endif

	// TODO: This will happen a lot from the main game thread and requires a bit
	// of calculation. To increase efficiency, just clone this data and send it to
	// some queue in the renderer. When I get around to doing the renderer thread,
	// this data will be processed there while the main thread continues on it's
	// merry way... concurrency. TODO: PREREQ: Thread safe memory allocator.

	const char *pTxt = strText;

	int iCurAdvance = 0;

	for ( ; '\0' != *pTxt; ++pTxt )
	{
		char chChar = *pTxt;

		CArFontCharacter *pChar = pFont->GetCharForChar( chChar );

		// TODO: Make more apparent than this (like error character).
		if ( !pChar ) continue;

		CArBoxDef Box;

		int iWidth = pChar->width, iHeight = pChar->height;

		Box.m_PosSize.x = x + iCurAdvance + pChar->xoffset;
		Box.m_PosSize.y = y + pChar->yoffset;
		Box.m_PosSize.width = (float)iWidth;
		Box.m_PosSize.height = (float)iHeight;

		float tc_x = (float)pChar->x;
		float tc_y = (float)pChar->y;
		float width = (float)pChar->width;
		float height = (float)pChar->height;
		float xoffset = (float)pChar->xoffset;
		float yoffset= (float)pChar->yoffset;

		Box.m_TcDims.left = (float)tc_x / pFont->GetTexPageWidth();
		Box.m_TcDims.top = (float)tc_y / pFont->GetTexPageHeight();
		Box.m_TcDims.right = (float)( tc_x + width ) / pFont->GetTexPageWidth();
		Box.m_TcDims.bottom = (float)( tc_y + height ) / pFont->GetTexPageHeight();

		Box.m_uiColor = uiColor;
		Box.m_pTexture = pFont->GetTexturePage( pChar->page );

#if 0
		CArBoxDef OldBox = Box;

		// FIXME: Temporarily draw a shadow first.
		Box.m_Dims.left += 2;
		Box.m_Dims.top += 2;
		Box.m_Dims.right += 2;
		Box.m_Dims.bottom += 2;
		Box.m_uiColor = 0xFF000000;
#endif
		m_Boxes.push_back( Box );

		//	m_Boxes.push_back( OldBox );

		iCurAdvance += pChar->xadvance;
	}
}
