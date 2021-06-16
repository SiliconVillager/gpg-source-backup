// Renderer_Win32.cpp:
// Date: 11/24/08
// Author: Aurelio Reis

#include "Engine/Win32/Renderer_Win32.h"
#include "Engine/Shared/Font.h"
#include "Engine/Shared/Texture.h"
#include "Game/Shared/ViewInterface.h"

#include "IL/il.h"
#include "IL/ilut.h"

//const uint32 SCREENWIDTH = 320;
//const uint32 SCREENHEIGHT = 480;

class IArRenderer::CArPrivate
{
public:
	CArSemaphore m_RenderSemaphore;
	CArGuiModel m_GuiModel;

	CArPrivate() {}
	~CArPrivate() {}
};


CArRenderer_Win32 g_RendererInstance_Win32;
CArRenderer_Win32 *CArRenderer_Win32::s_pInstance = &g_RendererInstance_Win32;

CArRenderer_Win32::CArRenderer_Win32() : d( new IArRenderer::CArPrivate() )
{

}

CArRenderer_Win32::~CArRenderer_Win32()
{
	delete d;
}

bool IsExtensionSupported( char* szTargetExtension )
{
	const unsigned char *pszExtensions = NULL;
	const unsigned char *pszStart;
	unsigned char *pszWhere, *pszTerminator;

	// Extension names should not have spaces
	pszWhere = (unsigned char *) strchr( szTargetExtension, ' ' );
	if( pszWhere || *szTargetExtension == '\0' )
		return false;

	// Get Extensions String
	pszExtensions = glGetString( GL_EXTENSIONS );
	// eglQueryString(EGL_EXTENSIONS)

	// Search The Extensions String For An Exact Copy
	pszStart = pszExtensions;
	for(;;)
	{
		pszWhere = (unsigned char *) strstr( (const char *) pszStart, szTargetExtension );
		if( !pszWhere )
			break;
		pszTerminator = pszWhere + strlen( szTargetExtension );
		if( pszWhere == pszStart || *( pszWhere - 1 ) == ' ' )
			if( *pszTerminator == ' ' || *pszTerminator == '\0' )
				return true;
		pszStart = pszTerminator;
	}
	return false;
}

#ifndef _IPHONE
PFNGLGENBUFFERSARBPROC glGenBuffers = NULL;
PFNGLBINDBUFFERARBPROC glBindBuffer = NULL;
PFNGLBUFFERDATAARBPROC glBufferData = NULL;
PFNGLDELETEBUFFERSARBPROC glDeleteBuffers = NULL;
#endif

PFNGLGENPROGRAMSARBPROC glGenProgramsARB = NULL;
PFNGLDELETEPROGRAMSARBPROC glDeleteProgramsARB = NULL;
PFNGLBINDPROGRAMARBPROC glBindProgramARB = NULL;
PFNGLPROGRAMSTRINGARBPROC glProgramStringARB = NULL;
PFNGLVERTEXATTRIB4FARBPROC glVertexAttrib4fARB = NULL;
PFNGLVERTEXATTRIBPOINTERARBPROC glVertexAttribPointerARB = NULL;
PFNGLPROGRAMLOCALPARAMETER4FARBPROC glProgramLocalParameter4fARB = NULL;
PFNGLPROGRAMENVPARAMETER4FARBPROC glProgramEnvParameter4fARB = NULL;
#if 0
PFNGLGENRENDERBUFFERSEXTPROC glGenRenderbuffersEXT = NULL;
PFNGLBINDRENDERBUFFEREXTPROC glBindRenderbufferEXT = NULL;
PFNGLRENDERBUFFERSTORAGEEXTPROC glRenderbufferStorageEXT = NULL;
PFNGLGENFRAMEBUFFERSEXTPROC glGenFramebuffersEXT = NULL;
PFNGLBINDFRAMEBUFFEREXTPROC glBindFramebufferEXT = NULL;
PFNGLFRAMEBUFFERTEXTURE2DEXTPROC glFramebufferTexture2DEXT = NULL;
PFNGLFRAMEBUFFERRENDERBUFFEREXTPROC glFramebufferRenderbufferEXT = NULL;
PFNGLDELETERENDERBUFFERSEXTPROC glDeleteRenderbuffersEXT = NULL;
PFNGLDELETEFRAMEBUFFERSEXTPROC glDeleteFramebuffersEXT = NULL;
#endif

#ifdef _IPHONE
#include <UIKit/UIKit.h>
#include <OpenGLES/EAGL.h>
#include <OpenGLES/EAGLDrawable.h>
#include <OpenGLES/ES1/gl.h>
#include <OpenGLES/ES1/glext.h>
#include <QuartzCore/QuartzCore.h>
#endif

uint32 progid;

#ifdef _WIN32
	#define myglGetProcAddress		wglGetProcAddress
#elif defined( _IPHONE )
	#define myglGetProcAddress		eglGetProcAddress
#endif

void CArRenderer_Win32::Initialize()
{
	// TODO: Renderer thread where bulk of logic occurs for the processing
	// necessary to render an object. It just takes the command list
	// and does the work to get it to the screen while the normal game thread
	// continues to queue up more commands (semaphore protected).

	AR_DebugMessage( "Initializing DevIL\n" );
	ilInit();
	iluInit();
	ilutRenderer( ILUT_OPENGL );
	AR_DebugMessage( "Initialization Complete\n" );

	//ilEnable( IL_CONV_PAL );
	//iluImageParameter( ILU_FILTER, ILU_BILINEAR );
#if 0
	bool bVBOSupported = IsExtensionSupported( "GL_ARB_vertex_buffer_object" );
	AR_VerifyWithError( bVBOSupported, "Vertex Buffer Objects not supported!" );
	if ( bVBOSupported )
	{
		glGenBuffers = (PFNGLGENBUFFERSARBPROC)myglGetProcAddress( "glGenBuffersARB" );
		glBindBuffer = (PFNGLBINDBUFFERARBPROC)myglGetProcAddress( "glBindBufferARB" );
		glBufferData = (PFNGLBUFFERDATAARBPROC)myglGetProcAddress( "glBufferDataARB" );
		glDeleteBuffers = (PFNGLDELETEBUFFERSARBPROC)myglGetProcAddress( "glDeleteBuffersARB" );
	}

#ifdef _IPHONE
	bool bVertexProgramSupported = IsExtensionSupported( "GL_IMG_vertex_program" );
#else
		bool bVertexProgramSupported = IsExtensionSupported( "GL_ARB_vertex_program" );
#endif
	AR_VerifyWithError( bVertexProgramSupported, "Vertex Programs not supported!" );
	if ( bVertexProgramSupported )
	{
		glGenProgramsARB = (PFNGLGENPROGRAMSARBPROC)myglGetProcAddress( "glGenProgramsARB" );
		glDeleteProgramsARB = (PFNGLDELETEPROGRAMSARBPROC)myglGetProcAddress( "glDeleteProgramsARB" );
		glBindProgramARB = (PFNGLBINDPROGRAMARBPROC)myglGetProcAddress( "glBindProgramARB" );
		glProgramStringARB = (PFNGLPROGRAMSTRINGARBPROC)myglGetProcAddress( "glProgramStringARB" );
		glVertexAttrib4fARB = (PFNGLVERTEXATTRIB4FARBPROC)myglGetProcAddress( "glVertexAttrib4fARB" );
		glVertexAttribPointerARB = (PFNGLVERTEXATTRIBPOINTERARBPROC)myglGetProcAddress( "glVertexAttribPointerARB" );
		glProgramLocalParameter4fARB = (PFNGLPROGRAMLOCALPARAMETER4FARBPROC)myglGetProcAddress( "glProgramLocalParameter4fARB" );
		glProgramEnvParameter4fARB = (PFNGLPROGRAMENVPARAMETER4FARBPROC)myglGetProcAddress( "glProgramEnvParameter4fARB" );
	}
#endif
#if 0
	bool bFramebufferObjectSupported = IsExtensionSupported( "GL_ARB_framebuffer_object" );
	AR_VerifyWithError( bFramebufferObjectSupported, "Framebuffer Objects not supported!" );
	if ( bFramebufferObjectSupported )
	{
		glGenRenderbuffersEXT = (PFNGLGENRENDERBUFFERSEXTPROC)myglGetProcAddress( "glGenRenderbuffersEXT" );
		glBindRenderbufferEXT = (PFNGLBINDRENDERBUFFEREXTPROC)myglGetProcAddress( "glBindRenderbufferEXT" );
		glRenderbufferStorageEXT = (PFNGLRENDERBUFFERSTORAGEEXTPROC)myglGetProcAddress( "glRenderbufferStorageEXT" );
		glGenFramebuffersEXT = (PFNGLGENFRAMEBUFFERSEXTPROC)myglGetProcAddress( "glGenFramebuffersEXT" );
		glBindFramebufferEXT = (PFNGLBINDFRAMEBUFFEREXTPROC)myglGetProcAddress( "glBindFramebufferEXT" );
		glFramebufferTexture2DEXT = (PFNGLFRAMEBUFFERTEXTURE2DEXTPROC)myglGetProcAddress( "glFramebufferTexture2DEXT" );
		glFramebufferRenderbufferEXT = (PFNGLFRAMEBUFFERRENDERBUFFEREXTPROC)myglGetProcAddress( "glFramebufferRenderbufferEXT" );
		glDeleteRenderbuffersEXT = (PFNGLDELETERENDERBUFFERSEXTPROC)myglGetProcAddress( "glDeleteRenderbuffersEXT" );
		glDeleteFramebuffersEXT = (PFNGLDELETEFRAMEBUFFERSEXTPROC)myglGetProcAddress( "glDeleteFramebuffersEXT" );
	}
#endif

	AR_DebugMessage( "Creating the GUI Model..." );
	d->m_GuiModel.Create();
	AR_DebugMessage( "complete!\n" );

#ifdef USE_VPROGS
#ifdef _WIN32
	CArFile *pVp = FILESYS->OpenFileForRead( "Data/Test/Test1.arbvp" );
	uint32 uiVpLen = pVp->GetSize();
#define MAX_VP_TEMP_LEN		16384
	char strVpTemp[ MAX_VP_TEMP_LEN ];
	AR_Assert( MAX_VP_TEMP_LEN > ( uiVpLen + 1 ) );
	pVp->Read( strVpTemp, uiVpLen );
	strVpTemp[ uiVpLen + 1 ] = '\0';
	FILESYS->CloseFile( pVp );

	uint32 uiErr = glGetError();

	glGenProgramsARB( 1, &progid );
	glBindProgramARB( GL_VERTEX_PROGRAM_ARB, progid );
	glProgramStringARB( GL_VERTEX_PROGRAM_ARB,
						GL_PROGRAM_FORMAT_ASCII_ARB,
						uiVpLen, strVpTemp );

	uiErr = glGetError();
#elif defined( _IPHONE )
#if 0
	// PowerVR hardware present?
	const char* pszRendererString = (const char*) glGetString(GL_RENDERER);
	AR_Assert( strstr( pszRendererString, "PowerVR" ) );
	AR_Assert( strstr( pszRendererString, "VGPLite" ) );

	CArFile *pVp = FILESYS->OpenFileForRead( "Data/Test/Test1.imgvp.bin" );
	uint32 uiVpLen = pVp->GetSize();
#define MAX_VP_TEMP_LEN		16384
	byte pVpTemp[ MAX_VP_TEMP_LEN ];
	pVp->Read( pVpTemp, uiVpLen );
	FILESYS->CloseFile( pVp );

	uint32 uiErr = glGetError();

#ifdef _IPHONE
	#define GL_VERTEX_PROGRAM_ARB                                           0x8620
#endif
//#define GL_PROGRAM_STRING_ARB                                           0x8628
#define GL_PROGRAM_FORMAT_BINARY_IMG                            0x8C0B

	glGenProgramsARB( 1, &progid );
	glBindProgramARB( GL_VERTEX_PROGRAM_ARB, progid );
	glProgramStringARB( GL_VERTEX_PROGRAM_ARB,
						GL_PROGRAM_FORMAT_BINARY_IMG,
						uiVpLen, pVpTemp );

	uiErr = glGetError();
#endif
#endif
#endif // USE_VPROGS

	// Reset glGetError() in case there were any errors.
	glGetError();
}

void CArRenderer_Win32::Shutdown()
{
	d->m_GuiModel.Destroy();
}

extern void Window_SwapBuffers();

void CArRenderer_Win32::BeginRender()
{
	CArSemaphoreGuard Guard( d->m_RenderSemaphore );

	glClearColor( 0.5f, 0.5f, 0.7f, 1.0f );
#ifdef _IPHONE
	glClearDepthf( 1.0f );
#else
	glClearDepth( 1.0f );
#endif
	glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT );
}

void CArRenderer_Win32::EndRender()
{
	//glFlush();
#ifndef _IPHONE
	Window_SwapBuffers();
#endif
}

#ifdef _IPHONE
	#define myglOrtho	 glOrthof
#else
	#define myglOrtho	 glOrtho
#endif

void CArRenderer_Win32::Render( IViewLayer *pView )
{
	CArSemaphoreGuard Guard( d->m_RenderSemaphore );

	LOG_SCOPE( "Rendering View" );

	// Reset errors.
#ifdef _DEBUG
	glGetError();
#endif

	glMatrixMode( GL_PROJECTION );
	glLoadIdentity();

	float fY =  0.0f;

	extern bool g_bDevMode;
	if ( g_bDevMode )
	{
		glViewport( 0.0f, fY, 480, (int)pView->GetHeight() );
		myglOrtho( 0.0f, 480, pView->GetHeight(), 0.0f, 0.0f, 1.0f );
	}
	else
	{
		glViewport( 0.0f, fY, (int)pView->GetWidth(), (int)pView->GetHeight() );
		myglOrtho( 0.0f, pView->GetWidth(), pView->GetHeight(), 0.0f, 0.0f, 1.0f );
	}

	glDisable( GL_CULL_FACE );
	glDisable( GL_DEPTH_TEST );

	glMatrixMode( GL_MODELVIEW );
	glLoadIdentity();
	
	//glClientActiveTexture( GL_TEXTURE0 );
	//glActiveTexture( GL_TEXTURE0 );

	glEnable( GL_TEXTURE_2D );

	// FIXME:
	glEnable( GL_BLEND );
	glBlendFunc( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA );

	glm::mat4x4 mFinalTransform;
	pView->GetFinalTransform( mFinalTransform );

	glm::mat4x4 mBase;
	pView->GetBaseTransform( mBase );

	//mBase *= mParent;

	glLoadMatrixf( &mFinalTransform[ 0 ][ 0 ] );

	// Render the GUI model.
	d->m_GuiModel.Render();
}

#ifndef _IPHONE
	#define USE_INTERLEAVED_ARRAYS
#endif

//#define USE_VBO
extern uint32 VertexFormat[ 3 ];
extern GLuint uiBuffer[ 3 ];

void CArRenderer_Win32::RenderMesh( CArMesh *pMesh, uint32 uiNumIndices )
{
#ifdef USE_VPROGS
	glEnable( GL_VERTEX_PROGRAM_ARB );
	glBindProgramARB( GL_VERTEX_PROGRAM_ARB, progid );
	AR_Assert( 0 == glGetError() );
#endif

	//glVertexAttrib4fARB( 0, 1.0f, 0.0f, 1.0f, 1.0f );

#ifdef USE_VBO
	glEnableClientState( GL_TEXTURE_COORD_ARRAY );
	glEnableClientState( GL_COLOR_ARRAY );
	glEnableClientState( GL_VERTEX_ARRAY );

#define VBO_POINTER		NULL
	glBindBuffer( GL_ARRAY_BUFFER_ARB, uiBuffer[ 0 ] );
	glTexCoordPointer( 2, GL_FLOAT, 0, VertexFormat[ 0 ] );

	glBindBuffer( GL_ARRAY_BUFFER_ARB, uiBuffer[ 1 ] );
	glVertexPointer( 4, GL_UNSIGNED_BYTE, 0, VertexFormat[ 1 ] );

	glBindBuffer( GL_ARRAY_BUFFER_ARB, uiBuffer[ 2 ] );
	glVertexPointer( 3, GL_FLOAT, 0, VertexFormat[ 2 ] );

	glDrawElements( GL_TRIANGLE_STRIP, uiNumIndices, GL_UNSIGNED_SHORT, &pMesh->m_pIndices[ 0 ] );

	glDisableClientState( GL_VERTEX_ARRAY );
	glDisableClientState( GL_COLOR_ARRAY );
	glDisableClientState( GL_TEXTURE_COORD_ARRAY );		
#else
	glEnableClientState( GL_VERTEX_ARRAY );
	glVertexPointer( 3, GL_FLOAT, sizeof( CArVertex ), &pMesh->m_pVertices[ 0 ].m_vPos );

	glEnableClientState( GL_COLOR_ARRAY );
	glColorPointer( 4, GL_UNSIGNED_BYTE, sizeof( CArVertex ), &pMesh->m_pVertices[ 0 ].m_uiColor );

	glEnableClientState( GL_TEXTURE_COORD_ARRAY );
	glTexCoordPointer( 2, GL_FLOAT, sizeof( CArVertex ), &pMesh->m_pVertices[ 0 ].m_fTexCoords );

	glDrawElements( GL_TRIANGLE_STRIP, uiNumIndices, GL_UNSIGNED_SHORT, &pMesh->m_pIndices[ 0 ] );

	glDisableClientState( GL_TEXTURE_COORD_ARRAY );
	glDisableClientState( GL_COLOR_ARRAY );
	glDisableClientState( GL_VERTEX_ARRAY );
#endif

	AR_Assert( 0 == glGetError() );

#ifdef USE_VPROGS
//	glDeleteProgramsARB( 1, &progid );
	glDisable( GL_VERTEX_PROGRAM_ARB );

	AR_Assert( 0 == glGetError() );
#endif
}

CArGuiModel *CArRenderer_Win32::GetGuiModel()
{
	return &d->m_GuiModel;
}

CArSemaphore *CArRenderer_Win32::GetRenderSemaphore()
{
	return &d->m_RenderSemaphore;
}
