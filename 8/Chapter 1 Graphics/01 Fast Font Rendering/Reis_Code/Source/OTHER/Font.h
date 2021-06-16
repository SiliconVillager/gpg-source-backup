// Font.h:
// Date: 04/24/08
// Author: Aurelio Reis

#ifndef __AR__FONT__H__
#define __AR__FONT__H__

#include "Engine/Shared/Utils.h"
#include "Engine/Shared/Resource.h"


// Forward decls.
class CArTexture;


class CArFontCharacter
{
public:
	int id;
	int x, y;
	int width, height;
	int xoffset, yoffset;
	int xadvance;
	int page;

	CArFontCharacter() {}
	~CArFontCharacter() {}
};


class CArFont : public CArResource
{
private:
	CArFontCharacter *m_Chars[ 256 ];

	uint32 m_uiNumPages;
	CArTexture **m_ppTexturePages;

	uint32 m_uiSize;
	uint32 m_uiTexPageWidth, m_uiTexPageHeight;

public:
	CArFont();
	~CArFont();

	void CreateFromFile( const char *strFileName );

	glm::vec2 CalcTextArea( const char *strText );

	CArFontCharacter *GetCharForChar( char chVal ) { return m_Chars[ (uint32)chVal ]; }
	CArTexture *GetTexturePage( uint32 uiPage ) { return m_ppTexturePages[ uiPage ]; }

	ACCESSOR_GETSET_VAL( uint32, m_uiSize, Size )
	ACCESSOR_GETSET_VAL( uint32, m_uiTexPageWidth, TexPageWidth )
	ACCESSOR_GETSET_VAL( uint32, m_uiTexPageHeight, TexPageHeight )
};


#endif // __AR__FONT__H__
