// BitmapFont.h:
// Created by: Aurelio Reis

#ifndef __AR__BITMAP_FONT__H__
#define __AR__BITMAP_FONT__H__


class CArCharKern
{
public:
	// The id for the character to kern with.
	int m_iId;

	// The offset to apply for this kern.
	float m_fOffset;

	// Constructor.
	CArCharKern() : m_iId( -1 ), m_fOffset( 0.0f ) {}

	// Destructor.
	~CArCharKern() {}
};


// A character description.
class CArCharDesc
{
public:
	// The ASCII ID for this character (0-255).
	int m_iASCII;

	// The character uv coords position and the width and height that map into
	// the font texture.
	float m_fU0, m_fV0, m_fU1, m_fV1;

	// The character physical width and height.
	float m_fWidth, m_fHeight;

	// The physical screen space amount to advance for the next character.
	float m_fAdvance;

	// The physical screen space offset to apply to the character.
	float m_fOffsetX, m_fOffsetY;

	// The font page this character belongs to (if it spanned more than 1).
	int m_iFontPage;

	// The list of characters we kern with.
	vector< CArCharKern > m_KernList;

	// Constructor.
	CArCharDesc() :
		m_iASCII( 0 ),
		m_fU0( 0.0f ), m_fV0( 0.0f ), m_fU1( 0.0f ), m_fV1( 0.0f ),
		m_fWidth( 0.0f ), m_fHeight( 0.0f ),
		m_fOffsetX( 0.0f ), m_fOffsetY( 0.0f ),
		m_fAdvance( 0.0f ),
		m_iFontPage( 0 )
	{
	}

	// Destructor.
	~CArCharDesc() {}

};


class CArBitmapFont
{
public:
	// The maximum number of font pages allowed.
	enum { MAX_FONT_PAGES = 3 };

	enum EFontFlags
	{
		FF_NONE		= 0x0,
		FF_BOLD		= 0x01,
		FF_ITALIC	= 0x02
	};

	// The font face name.
	char *m_strFaceName;

	// The font size.
	int m_iSize;

	// The width and height scale.
	int m_iWidthScale, m_iHeightScale;

	// The font flags.
	// TODO: Add style flags, like italic, bold, outline and shadowed.
	EFontFlags m_Flags;

	// The font textures/pages.
	// NOTE: Using more than one is very bad - it completely breaks a font batch.
	LPDIRECT3DTEXTURE9 m_pFontPages[ MAX_FONT_PAGES ];

	// The number of font pages.
	int m_iNumFontPages;

	// All the character descriptions.
	CArCharDesc *m_CharDescs[ 256 ];

	// The number of stored characters.
	int m_iNumChars;

	// Constructor.
	CArBitmapFont();

	// Destructor.
	~CArBitmapFont();

	// Create a new font with the specified face.
	void Create( const char *strFace );

	void Destroy();

	// Find a character description by ASCII ID.
	CArCharDesc *FindCharByASCII( int iASCII )
	{
		return m_CharDescs[ iASCII ];
	}

	// Indexing operator.
	CArCharDesc *operator[]( int iIndex )
	{
		// TODO: Return SOMETHING if that char is NULL, OR, fill all NULL chars with something
		// to get rid of any run time checks.
		return m_CharDescs[ iIndex ];
	}
};


#endif // __AR__BITMAP_FONT__H__