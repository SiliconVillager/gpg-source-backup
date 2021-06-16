// BitmapFont.cpp:
// Created by: Aurelio Reis

#include "DXUT.h"

#include "BitmapFont.h"


// Constructor.
CArBitmapFont::CArBitmapFont() :
	m_iNumFontPages( 0 ),
	m_strFaceName( NULL ),
	m_Flags( FF_NONE ),
	m_iSize( 0 ),
	m_iWidthScale( 0 ), m_iHeightScale( 0 )
{
	memset( m_CharDescs, 0, sizeof( m_CharDescs ) );
	memset( m_pFontPages, 0, sizeof( LPDIRECT3DTEXTURE9 ) * MAX_FONT_PAGES );
}

// Destructor.
CArBitmapFont::~CArBitmapFont()
{
	for ( int i = 0; i < 256; ++i )
	{
		SAFE_DELETE( m_CharDescs[ i ] );
	}
	m_iNumChars = 0;
}

// Create a new font with the specified face.
void CArBitmapFont::Create( const char *strFace )
{
	// Go through the fnt file and grab all the characters.
	char strFontFile[ MAX_PATH ];
	_snprintf( strFontFile, MAX_PATH, "%s.fnt", strFace );
	FILE *pFile = fopen( strFontFile, "rb" );

	if ( !pFile )
	{
		assert( 0 );

		return;
	}

	fseek( pFile, 0, SEEK_END );
	unsigned long ulSize = ftell( pFile );
	fseek( pFile, 0, SEEK_SET );

	char *pData = new char[ ulSize ];
	fread( pData, 1, ulSize, pFile );

	fclose( pFile );

	// Parse the data.
	const char *strSeps = ", \n\r\t=";
	const char *pTok;

	pTok = strtok( pData, strSeps );
	for ( ; pTok; pTok = strtok( NULL, strSeps ) )
	{
		if ( strcmp( pTok, "info" ) == 0 )
		{
			// Get the face name.
			// FIXME!
			strtok( NULL, strSeps );

			while ( strcmp( pTok, "size" ) != 0 )
			{
				pTok = strtok( NULL, strSeps );
			}

			// Get the size.
			m_iSize = atoi( strtok( NULL, strSeps ) );
		}
		else if ( strcmp( pTok, "common" ) == 0 )
		{
			strtok( NULL, strSeps );
			strtok( NULL, strSeps );

			strtok( NULL, strSeps );
			strtok( NULL, strSeps );

			// Get the width scale.
			pTok = strtok( NULL, strSeps );
			m_iWidthScale = atoi( strtok( NULL, strSeps ) );

			// Get the height scale.
			pTok = strtok( NULL, strSeps );
			m_iHeightScale = atoi( strtok( NULL, strSeps ) );

			// Get the number of pages.
			pTok = strtok( NULL, strSeps );
			m_iNumFontPages = atoi( strtok( NULL, strSeps ) );
		}
		else if ( strcmp( pTok, "page" ) == 0 )
		{
			// Ignore.
			pTok = strtok( NULL, strSeps );
			pTok = strtok( NULL, strSeps );
		}
		else if ( strcmp( pTok, "chars" ) == 0 )
		{
			// Ignore.
			pTok = strtok( NULL, strSeps );
		}
		else if ( strcmp( pTok, "char" ) == 0 )
		{
			// New character.
			CArCharDesc *pNewChar = new CArCharDesc();

			pTok = strtok( NULL, strSeps );
			pNewChar->m_iASCII = atoi( strtok( NULL, strSeps ) );

			assert( pNewChar->m_iASCII >= 0 && pNewChar->m_iASCII < 256 );

			// Set the character to it's proper ASCII slot.
			m_CharDescs[ pNewChar->m_iASCII ] = pNewChar;
			m_iNumChars++;

			pTok = strtok( NULL, strSeps );
			pNewChar->m_fU0 = (float)atof( strtok( NULL, strSeps ) ) / m_iWidthScale;

			pTok = strtok( NULL, strSeps );
			pNewChar->m_fV0 = (float)atof( strtok( NULL, strSeps ) ) / m_iHeightScale;

			pTok = strtok( NULL, strSeps );
			pNewChar->m_fWidth = (float)atof( strtok( NULL, strSeps ) );

			pTok = strtok( NULL, strSeps );
			pNewChar->m_fHeight = (float)atof( strtok( NULL, strSeps ) );

			pNewChar->m_fU1 = pNewChar->m_fWidth / m_iWidthScale;
			pNewChar->m_fV1 = pNewChar->m_fHeight / m_iHeightScale;

			pTok = strtok( NULL, strSeps );
			pNewChar->m_fOffsetX = (float)atof( strtok( NULL, strSeps ) );

			pTok = strtok( NULL, strSeps );
			pNewChar->m_fOffsetY = (float)atof( strtok( NULL, strSeps ) );

			pTok = strtok( NULL, strSeps );
			pNewChar->m_fAdvance = (float)atof( strtok( NULL, strSeps ) );

			pTok = strtok( NULL, strSeps );
			pNewChar->m_iFontPage = atoi( strtok( NULL, strSeps ) );

			// Make virtual.
			assert( m_iSize != 0.0f );
			pNewChar->m_fWidth /= m_iSize;
			pNewChar->m_fHeight /= m_iSize;
			pNewChar->m_fOffsetX /= m_iSize;
			pNewChar->m_fOffsetY /= m_iSize;
			pNewChar->m_fAdvance /= m_iSize;

		}
		else if ( strcmp( pTok, "kerning" ) == 0 )
		{
			CArCharKern Kern;

			pTok = strtok( NULL, strSeps );
			int iFirstCharId = atoi( strtok( NULL, strSeps ) );

			assert( iFirstCharId >= 0 && iFirstCharId < 256 );

			pTok = strtok( NULL, strSeps );
			Kern.m_iId = atoi( strtok( NULL, strSeps ) );

			pTok = strtok( NULL, strSeps );
			Kern.m_fOffset = (float)atof( strtok( NULL, strSeps ) );

			// Make virtual.
			assert( m_iSize != 0.0f );
			Kern.m_fOffset /= m_iSize;

			assert( m_CharDescs[ iFirstCharId ] );
			m_CharDescs[ iFirstCharId ]->m_KernList.push_back( Kern );
		}

		// TODO: Return SOMETHING if that char is NULL, OR, fill all NULL chars with something
		// to get rid of any run time checks.
	}

	SAFE_DELETE_ARRAY( pData );

	if ( m_iNumChars <= 0 )
	{
		assert( 0 );

		return;
	}

	// Load the font texture pages.
	HRESULT hr;
	WCHAR str[MAX_PATH];
	WCHAR strTxtrName[MAX_PATH];

	mbstowcs( str, strFace, MAX_PATH );
	_snwprintf( strTxtrName, MAX_PATH, L"%s_%02d.tga", str, 0 );

	V( D3DXCreateTextureFromFile( g_pd3dDevice, strTxtrName, &m_pFontPages[ 0 ] ) );
}

void CArBitmapFont::Destroy()
{
	for ( int i = 0; i < MAX_FONT_PAGES; ++i )
	{
		SAFE_RELEASE( m_pFontPages[ i ] );
	}
}