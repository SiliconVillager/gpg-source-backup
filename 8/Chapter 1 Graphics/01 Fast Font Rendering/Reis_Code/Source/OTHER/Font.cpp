// Font.cpp:
// Date: 11/26/08
// Author: Aurelio Reis

#include "Engine/Shared/Font.h"
#include "Engine/Shared/ResourceManager.h"

#include "Engine/Shared/RendererInterface.h"
#include "Engine/Shared/Thread_Platform.h"


CArFont::CArFont()
	:	m_uiNumPages( 0 ), m_ppTexturePages( NULL ),
		m_uiSize( 0 ), m_uiTexPageWidth( 0 ), m_uiTexPageHeight( 0 )
{
	memset( m_Chars, 0, 256 * sizeof( CArFontCharacter * ) );
}

CArFont::~CArFont()
{
	for ( uint32 i = 0; i < m_uiNumPages; ++i )
	{
		SAFE_DELETE( m_ppTexturePages[ i ] );
	}

	SAFE_DELETE_ARRAY( m_ppTexturePages );
}

void CArFont::CreateFromFile( const char *strFileName )
{
	TiXmlDocument *pXmlDoc = AR_Verify( RESOURCEMGR->CreateXmlFromFile( strFileName ) );

	TiXmlElement *pElem = pXmlDoc->FirstChildElement( "font" );
	AR_Assert( pElem );

	TiXmlElement *pNxtElem = NULL;

	const char *strVal = NULL;

	pNxtElem = pElem->FirstChildElement( "info" );
	strVal = pNxtElem->Attribute( "face" );
	m_uiSize = (uint32)atoi( AR_Verify( strVal = pNxtElem->Attribute( "size" ) ) );

	pNxtElem = pElem->FirstChildElement( "common" );
	//strVal = pNxtElem->Attribute( "lineHeight" );

	m_uiTexPageWidth = (uint32)atoi( AR_Verify( strVal = pNxtElem->Attribute( "scaleW" ) ) );
	m_uiTexPageHeight = (uint32)atoi( AR_Verify( strVal = pNxtElem->Attribute( "scaleH" ) ) );

	m_uiNumPages = (uint32)atoi( AR_Verify( strVal = pNxtElem->Attribute( "pages" ) ) );

	m_ppTexturePages = new CArTexture*[ m_uiNumPages ];

	pNxtElem = pElem->FirstChildElement( "pages" );
	pNxtElem = pNxtElem->FirstChildElement( "page" );
	for ( ; pNxtElem; pNxtElem = pNxtElem->NextSiblingElement( "page" ) )
	{
		uint32 uiCurPage = (int)atoi( AR_Verify( pNxtElem->Attribute( "id" ) ) );
		const char *strPageFileName = pNxtElem->Attribute( "file" );

		m_ppTexturePages[ uiCurPage ] = AR_VerifyWithError( RESOURCEMGR->CreateTextureFromFile( strPageFileName ), AR_VarArg( "Cannot open font texture page file '%s'\n", strPageFileName ) );
	}

	pNxtElem = pElem->FirstChildElement( "chars" );
	const uint32 uiCount = atoi( pNxtElem->Attribute( "count" ) );

	pNxtElem = pNxtElem->FirstChildElement( "char" );
	for ( ; pNxtElem; pNxtElem = pNxtElem->NextSiblingElement( "char" ) )
	{
		CArFontCharacter *pChar = new CArFontCharacter();

		pChar->id = (int)atoi( AR_Verify( strVal = pNxtElem->Attribute( "id" ) ) );
		pChar->x = (int)atoi( AR_Verify( strVal = pNxtElem->Attribute( "x" ) ) );
		pChar->y = (int)atoi( AR_Verify( strVal = pNxtElem->Attribute( "y" ) ) );

		pChar->width = (int)atoi( AR_Verify( strVal = pNxtElem->Attribute( "width" ) ) );
		pChar->height = (int)atoi( AR_Verify( strVal = pNxtElem->Attribute( "height" ) ) );
		pChar->xoffset = (int)atoi( AR_Verify( strVal = pNxtElem->Attribute( "xoffset" ) ) );
		pChar->yoffset = (int)atoi( AR_Verify( strVal = pNxtElem->Attribute( "yoffset" ) ) );
		pChar->xadvance = (int)atoi( AR_Verify( strVal = pNxtElem->Attribute( "xadvance" ) ) );
		pChar->page = (int)atoi( AR_Verify( strVal = pNxtElem->Attribute( "page" ) ) );

		m_Chars[ pChar->id ] = pChar;
	}

	SAFE_DELETE( pXmlDoc );
}

glm::vec2 CArFont::CalcTextArea( const char *strText )
{
	glm::vec2 vArea;
	const char *pTxt = strText;
	int iCurAdvance = 0;

	for ( ; '\0' != *pTxt; ++pTxt )
	{
		char chChar = *pTxt;

		CArFontCharacter *pChar = GetCharForChar( chChar );

		// TODO: Make more apparent than this (like error character).
		if ( !pChar ) continue;
#if 0
		CArBoxDef Box;

		int iWidth = pChar->width, iHeight = pChar->height;

		Box.m_PosSize.x = x + iCurAdvance + pChar->xoffset;
		Box.m_PosSize.y = y + pChar->yoffset;
		Box.m_PosSize.width = (float)iWidth;
		Box.m_PosSize.height = (float)iHeight;

		float width = (float)pChar->width;
		float height = (float)pChar->height;
		float xoffset = (float)pChar->xoffset;
		float yoffset= (float)pChar->yoffset;
#endif
		iCurAdvance += pChar->xadvance;
	}

	vArea.x = (float)iCurAdvance;
	vArea.y = (float)GetSize();

	return vArea;
}