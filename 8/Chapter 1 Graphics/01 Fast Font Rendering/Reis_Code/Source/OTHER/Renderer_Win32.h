// Renderer_Win32.h:
// Date: 11/24/08
// Author: Aurelio Reis

#ifndef __AR__RENDERER__WIN32__H__
#define __AR__RENDERER__WIN32__H__

#include "Engine/Shared/Utils.h"
#include "Engine/Shared/RendererInterface.h"
#include "Engine/Shared/Thread_Platform.h"
#include "Engine/Shared/Model.h"
#include "Engine/Shared/Texture.h"
#include "Engine/Shared/Font.h"


// FIXME:
#ifdef _IPHONE
#define APIENTRYP *
typedef void (APIENTRYP PFNGLBINDBUFFERARBPROC) (GLenum target, GLuint buffer);
typedef void (APIENTRYP PFNGLDELETEBUFFERSARBPROC) (GLsizei n, const GLuint *buffers);
typedef void (APIENTRYP PFNGLGENBUFFERSARBPROC) (GLsizei n, GLuint *buffers);
typedef void (APIENTRYP PFNGLBUFFERDATAARBPROC) (GLenum target, int size, const GLvoid *data, GLenum usage);
#endif

// VBO Extension Function Pointers
#ifndef _IPHONE
extern PFNGLGENBUFFERSARBPROC glGenBuffers;
extern PFNGLBINDBUFFERARBPROC glBindBuffer;
extern PFNGLBUFFERDATAARBPROC glBufferData;
extern PFNGLDELETEBUFFERSARBPROC glDeleteBuffers;
#endif

#ifdef _IPHONE
typedef void (APIENTRYP PFNGLPROGRAMSTRINGARBPROC) (GLenum target, GLenum format, GLsizei len, const GLvoid *string);
typedef void (APIENTRYP PFNGLBINDPROGRAMARBPROC) (GLenum target, GLuint program);
typedef void (APIENTRYP PFNGLDELETEPROGRAMSARBPROC) (GLsizei n, const GLuint *programs);
typedef void (APIENTRYP PFNGLGENPROGRAMSARBPROC) (GLsizei n, GLuint *programs);
typedef void (APIENTRYP PFNGLVERTEXATTRIB4FARBPROC) (GLuint index, GLfloat x, GLfloat y, GLfloat z, GLfloat w);
typedef void (APIENTRYP PFNGLVERTEXATTRIBPOINTERARBPROC) (GLuint index, GLint size, GLenum type, GLboolean normalized, GLsizei stride, const GLvoid *pointer);
typedef void (APIENTRYP PFNGLPROGRAMLOCALPARAMETER4FARBPROC) (GLenum target, GLuint index, GLfloat x, GLfloat y, GLfloat z, GLfloat w);
typedef void (APIENTRYP PFNGLPROGRAMENVPARAMETER4FARBPROC) (GLenum target, GLuint index, GLfloat x, GLfloat y, GLfloat z, GLfloat w);
#endif

extern PFNGLGENPROGRAMSARBPROC glGenProgramsARB;
extern PFNGLDELETEPROGRAMSARBPROC glDeleteProgramsARB;
extern PFNGLBINDPROGRAMARBPROC glBindProgramARB;
extern PFNGLPROGRAMSTRINGARBPROC glProgramStringARB;
extern PFNGLVERTEXATTRIB4FARBPROC glVertexAttrib4fARB;
extern PFNGLVERTEXATTRIBPOINTERARBPROC glVertexAttribPointerARB;
extern PFNGLPROGRAMLOCALPARAMETER4FARBPROC glProgramLocalParameter4fARB;
extern PFNGLPROGRAMENVPARAMETER4FARBPROC glProgramEnvParameter4fARB;

#if 0
extern PFNGLGENRENDERBUFFERSEXTPROC glGenRenderbuffersEXT;
extern PFNGLBINDRENDERBUFFEREXTPROC glBindRenderbufferEXT;
extern PFNGLRENDERBUFFERSTORAGEEXTPROC glRenderbufferStorageEXT;
extern PFNGLGENFRAMEBUFFERSEXTPROC glGenFramebuffersEXT;
extern PFNGLBINDFRAMEBUFFEREXTPROC glBindFramebufferEXT;
extern PFNGLFRAMEBUFFERTEXTURE2DEXTPROC glFramebufferTexture2DEXT;
extern PFNGLFRAMEBUFFERRENDERBUFFEREXTPROC glFramebufferRenderbufferEXT;
extern PFNGLDELETERENDERBUFFERSEXTPROC glDeleteRenderbuffersEXT;
extern PFNGLDELETEFRAMEBUFFERSEXTPROC glDeleteFramebuffersEXT;
#endif


class CArRenderCall
{
public:
	// The model to render.
	CArModel *m_pModel;
};


class CArRenderer_Win32 DERIVE_INTERFACE( IArRenderer )
{
private:
	static CArRenderer_Win32 *s_pInstance;

	IArRenderer::CArPrivate *d;

public:
	static CArRenderer_Win32 *GetInstance() { return s_pInstance; }

	CArRenderer_Win32();
	~CArRenderer_Win32();

	void Initialize();
	void Shutdown();

	void BeginRender();
	void EndRender();

	void Render( IViewLayer *pView );

	void RenderMesh( CArMesh *pMesh, uint32 uiNumIndices );

	IArRenderer::CArPrivate *GetPrivate() { return d; }

	CArGuiModel *GetGuiModel();
	CArSemaphore *GetRenderSemaphore();
};


typedef CArRenderer_Win32		CArRenderer;


extern const uint32 SCREENWIDTH;
extern const uint32 SCREENHEIGHT;


#endif // __AR__RENDERER__WIN32__H__
