//---------------------------------------------------------------------------
/// \file GLRenderer.cpp
/// Definitions for GLRenderer class.
//
// Copyright (c) 2009, Neil Gower
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without 
// modification, are permitted provided that the following conditions are 
// met:
//
//   * Redistributions of source code must retain the above copyright 
//     notice, this list of conditions and the following disclaimer.
//
//   * Redistributions in binary form must reproduce the above copyright 
//     notice, this list of conditions and the following disclaimer in the 
//     documentation and/or other materials provided with the distribution.
//
//   * Neither the name of Vertex Blast nor the names of its contributors 
//     may be used to endorse or promote products derived from this software
//     without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
// TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
// PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
// OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
// PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
// LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
// NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//---------------------------------------------------------------------------

// app includes
#include "GLRenderer.h"
// OpenGL includes
#include "GL/gl.h"
#include "GL/glu.h"
// C++ includes
#include <cassert>
#include <iostream>
#include <cmath>

static const float PI = 3.14159265f;


//---------------------------------------------------------------------------
/// Internal helper for selecting an appropriate Windows pixel format.
//---------------------------------------------------------------------------
static void setupPixelFormat( HWND hWnd );


//---------------------------------------------------------------------------
// Set up a sane initial state.
//---------------------------------------------------------------------------
GLRenderer::GLRenderer( const int width, const int height )
{
    m_width  = width;
    m_height = height;
}


//---------------------------------------------------------------------------
/// Uses cleanup() to do the real work.
//---------------------------------------------------------------------------
GLRenderer::~GLRenderer( void )
{
    cleanup();
}


//---------------------------------------------------------------------------
/// If a renderer gets initialized more than once, it will discard the
/// previous state and resources. To modify present params, you will want
/// to tweak this method.
//---------------------------------------------------------------------------
bool GLRenderer::initialize( HWND hWnd )
{
    assert( hWnd != NULL );
    m_hWnd = hWnd;

    // start from a clean state
    cleanup();

    // initialize OpenGL context
    HDC hDC = GetDC( m_hWnd );
    setupPixelFormat( m_hWnd );
    m_hRC = wglCreateContext( hDC );
    wglMakeCurrent( hDC, m_hRC );

    // set viewport size
    resize();

    // set global rendering params
    glShadeModel( GL_SMOOTH );
    glClearColor( 0.0f, 0.0f, 0.0f, 0.5f );
    glClearDepth( 1.0f );
    glDisable( GL_DEPTH_TEST );
    glDepthFunc( GL_LEQUAL );
    glHint( GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST );

    return true;
}


//---------------------------------------------------------------------------
// Set up the a new frame for rendering.
//---------------------------------------------------------------------------
void GLRenderer::renderStart() 
{
    glClear( GL_COLOR_BUFFER_BIT );
    setupCamera();
    glMatrixMode( GL_MODELVIEW );							
    glLoadIdentity();		
    
    assert( glGetError() == GL_NO_ERROR );
}


//---------------------------------------------------------------------------
// See renderCross(int,int,int).
//---------------------------------------------------------------------------
void GLRenderer::renderCross( const std::pair<int,int>& location, 
                              int radius )
{
    renderCross( location.first, location.second, radius );
}


//---------------------------------------------------------------------------
/// Since the center of the cross occupies one pixel, the actual radius will 
/// be (radius + 0.5) when drawn.
//---------------------------------------------------------------------------
void GLRenderer::renderCross( int x, int y, int radius ) 
{
    // draw a cross
    glBegin( GL_LINES );    
    glColor3f( m_drawColour.r, m_drawColour.g, m_drawColour.b );
    glVertex2i( x - radius, y );
    glVertex2i( x + radius + 1 , y );
    glVertex2i( x, y - radius );
    glVertex2i( x, y + radius + 1 );
    glEnd();

    assert( glGetError() == GL_NO_ERROR );
}


//---------------------------------------------------------------------------
// Draw a filled circle.
//---------------------------------------------------------------------------
void GLRenderer::renderCircle( int x, int y, int radius ) 
{    
    glBegin( GL_TRIANGLE_FAN );    
    glColor3f( m_drawColour.r, m_drawColour.g, m_drawColour.b );
    const int NUM_SLICES = 16;
    for ( int i = 0; i <= NUM_SLICES; ++i  ) {
        glVertex2f( x + std::sin(i * 2*PI / NUM_SLICES) 
                        * (radius + 0.5f ), 
                    y + std::cos(i * 2*PI / NUM_SLICES) 
                        * (radius + 0.5f) );
    }
    glEnd();

    assert( glGetError() == GL_NO_ERROR );
}


//---------------------------------------------------------------------------
// Draw a line.
//---------------------------------------------------------------------------
void GLRenderer::renderLine( int x1, int y1, int x2, int y2 ) 
{
    glBegin( GL_LINES );
    glColor3f( m_drawColour.r, m_drawColour.g, m_drawColour.b );
    glVertex2i( x1, y1 );
    glVertex2i( x2, y2 );
    glEnd();

    assert( glGetError() == GL_NO_ERROR );
}


//---------------------------------------------------------------------------
// Sets the draw colour for subsequent renderX() calls
//---------------------------------------------------------------------------
void GLRenderer::setDrawColour( float r, float g, float b ) 
{
    m_drawColour = Colour( r, g, b );   
}


//---------------------------------------------------------------------------
/// SwapBuffers is a Win32 call, which is supposed to block when using 
/// vsync, much like D3D present().
//---------------------------------------------------------------------------
void GLRenderer::presentScene()
{
    HDC hDC = GetDC( m_hWnd );
    SwapBuffers( hDC );
}


//---------------------------------------------------------------------------
// Resize graphics to fit the actual window
//---------------------------------------------------------------------------
void GLRenderer::resize()
{
    if ( !m_hWnd ) return;

    // Get new window size
    RECT rect;
    GetClientRect( m_hWnd, &rect );
    m_width  = (unsigned int)rect.right;
    m_height = (unsigned int)rect.bottom;

    assert( m_height > 0 );
    glViewport( 0, 0, m_width, m_height );
}


//////////////////// INTERNAL METHODS AND HELPERS //////////////////////////

//---------------------------------------------------------------------------
/// Release OpenGL resources.
//---------------------------------------------------------------------------
void GLRenderer::cleanup()
{
    HDC hDC = GetDC( m_hWnd );
    wglDeleteContext( m_hRC );
    ReleaseDC( m_hWnd, hDC );
}


//---------------------------------------------------------------------------
/// Uses an orthographic projection to map GL coordinates to 2D screen 
/// coordinates. Offset by 0.5 so that integer pixel values are centered over
/// viewport texels.
//---------------------------------------------------------------------------
void GLRenderer::setupCamera()
{
    glMatrixMode( GL_PROJECTION );
    glLoadIdentity();	
    glOrtho( -0.5, m_width - 0.5, -0.5, m_height - 0.5, 0, 1 );    
}


//---------------------------------------------------------------------------
/// Set up OpenGL pixel format for graphics initialization
//---------------------------------------------------------------------------
void setupPixelFormat( HWND hWnd )
{
    PIXELFORMATDESCRIPTOR pfd;
    int pixelformat;
    HDC hDC = GetDC( hWnd );    

    pfd.nSize        = sizeof(PIXELFORMATDESCRIPTOR);
    pfd.nVersion     = 1;
    pfd.dwFlags      = PFD_DRAW_TO_WINDOW | PFD_SUPPORT_OPENGL 
                           | PFD_DOUBLEBUFFER;
    pfd.dwLayerMask  = PFD_MAIN_PLANE;
    pfd.iPixelType   = PFD_TYPE_RGBA;
    pfd.cColorBits   = 16;
    pfd.cDepthBits   = 16;
    pfd.cAccumBits   = 0;
    pfd.cStencilBits = 0;

    pixelformat = ChoosePixelFormat( hDC, &pfd );
    SetPixelFormat( hDC, pixelformat, &pfd );
}
