// Model.h:
// Date: 11/24/08
// Author: Aurelio Reis

#ifndef __AR__MODEL__H__
#define __AR__MODEL__H__

#include "Engine/Shared/Utils.h"


// Forward decls.
class CArTexture;
class CArFont;

#pragma pack(push, 1)
class CArVector3
{
public:
	float x, y, z;

	CArVector3() {}
	CArVector3( float _x, float _y, float _z ) : x( _x ), y( _y ), z( _z ) {}
};
#pragma pack(pop)

#pragma pack(push, 1)
class CArVertex
{
public:
	float m_fTexCoords[ 2 ];
	uint32 m_uiColor;
	CArVector3 m_vPos;
};
#pragma pack(pop)

class CArMesh
{
public:
	CArVertex *m_pVertices;
	uint32 m_uiNumVertices;

	int16 *m_pIndices;
	int16 m_sNumIndices;
	
	CArMesh() : m_pVertices( NULL ), m_uiNumVertices( 0 ), m_pIndices( NULL ), m_sNumIndices( 0 ) {}

	~CArMesh()
	{
		delete [] m_pVertices;
		delete [] m_pIndices;
	}
};

class CArModel
{
public:
	CArMesh *m_pMeshes;
	uint32 m_uiNumMeshes;

	CArModel() : m_pMeshes( NULL ), m_uiNumMeshes( 0 ) {}

	virtual ~CArModel()
	{
		delete [] m_pMeshes;
	}

	virtual void Create() {}
	virtual void Destroy() {}
};

//////////////////////////////////////////////////////////////////////////

class CArPosSize
{
public:
	float x, y, width, height;

	CArPosSize() : x( 0.0f ), y( 0.0f ), width( 0.0f ), height( 0.0f ) {}
	CArPosSize( float _x, float _y, float _width, float _height ) : x( _x ), y( _y ), width( _width ), height( _height ) {}
};

class CArRect
{
public:
	float left, top, right, bottom;

	CArRect() : left( 0.0f ), top( 0.0f ), right( 0.0f ), bottom( 0.0f ) {}
	CArRect( float _left, float _top, float _right, float _bottom ) : left( _left ), top( _top ), right( _right ), bottom( _bottom ) {}
	CArRect( const CArPosSize &_PosSize ) :
		left( _PosSize.x ), top( _PosSize.y ), right( _PosSize.x + _PosSize.width ), bottom( _PosSize.y + _PosSize.height ) {}

	bool PointInRect( const glm::vec2 &vPt )
	{
		if ( vPt.x < left || vPt.x > right || vPt.y < top || vPt.y > bottom )
		{
			return false;
		}

		return true;
	}
};

class CArBoxDef
{
public:
	CArPosSize m_PosSize;
	CArRect m_TcDims;
	uint32 m_uiColor;
	CArTexture *m_pTexture;
	float m_fRotation;
	glm::vec2 m_vRotationOrigin;

	CArBoxDef() :
		m_pTexture( NULL ), m_uiColor( 0xFFFFFFFF ),
		m_TcDims( 0.0f, 0.0f, 1.0f, 1.0f ), m_fRotation( 0.0f ),
		m_vRotationOrigin( 0.0f, 0.0f ) {}
};

class CArTextDef
{
public:
	float x, y;
	CArRect m_Dims;
	uint32 m_uiColor;
	char *m_strText;
	CArTexture *m_pTexture;

	CArTextDef() : m_strText( NULL ), m_pTexture( NULL ) {}
	~CArTextDef() { AR_DeleteString( m_strText ); }
};

typedef uint32 TTextAlign;

enum {	TALIGN_DEFAULT = 0,
		TALIGN_VERT_LEFT = AR_SET_BIT( 0 ),
		TALIGN_VERT_CENTER = AR_SET_BIT( 1 ),
		TALIGN_VERT_RIGHT = AR_SET_BIT( 2 ),
		TALIGN_HORZ_TOP = AR_SET_BIT( 3 ),
		TALIGN_HORZ_CENTER = AR_SET_BIT( 4 ),
		TALIGN_HORZ_BOTTOM = AR_SET_BIT( 5 ) };

class CArGuiTextDesc
{
public:
	CArFont *m_pFont;
	char *m_strText;
	uint32 m_uiColor;
	TTextAlign m_Alignment;
	glm::vec2 m_vOffset;

	CArGuiTextDesc() :
		m_pFont( NULL ), m_strText( NULL ), m_uiColor( 0xFFFFFFFF ),
		m_Alignment( TALIGN_DEFAULT ), m_vOffset( 0.0f, 0.0f ) {}
	~CArGuiTextDesc() { AR_DeleteString( m_strText ); }
};

//////////////////////////////////////////////////////////////////////////

class CArGuiModel : public CArModel
{
private:
	vector< CArBoxDef > m_Boxes;
	vector< CArTextDef * > m_Text;

public:
	CArGuiModel();
	~CArGuiModel();

	void Create();
	void Destroy();

	void Render();
	void DrawBox( CArBoxDef &Box );
	
	void DrawText( CArGuiTextDesc *pTextDesc, const CArPosSize &PosSize );
	void DrawText( CArFont *pFont, float x, float y, const char *strText, uint32 uiColor );

	friend class CArRenderer_Win32;
};

#define GUIMODEL CArRenderer::GetInstance()->GetGuiModel()


#endif // __AR__MODEL__H__
