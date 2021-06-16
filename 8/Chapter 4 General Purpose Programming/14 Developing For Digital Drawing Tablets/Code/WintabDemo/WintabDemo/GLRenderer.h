//---------------------------------------------------------------------------
/// \file GLRenderer.h
/// Declarations for GLRenderer class.
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
#ifndef GPG8_GL_RENDERER_H
#define GPG8_GL_RENDERER_H 1

#include <windows.h>
#include <vector>
#include <utility>


//---------------------------------------------------------------------------
/// Handles 2D rendering using basic immediate mode OpenGL commands. This 
/// is a simple class to get prototypes off the ground, not intended for 
/// production use.
//---------------------------------------------------------------------------
class GLRenderer
{
public:
    //-----------------------------------------------------------------------
    /// Creates a renderer instance with the desired dimensions. After 
    /// creation, you need to call initialize() to do all of the real work to
    /// prepare the window for rendering.
    //-----------------------------------------------------------------------
    GLRenderer( const int width, const int height );
    
    //-----------------------------------------------------------------------
    /// Handles all relevant cleanup.
    //-----------------------------------------------------------------------
    ~GLRenderer( void );

    //-----------------------------------------------------------------------
    /// You need to initialize the renderer with a handle for the window
    /// it should render to. Returns true on success, false otherwise. This 
    /// class assumes that the HWND remains valid until it is re-initialized
    /// or destroyed.
    //-----------------------------------------------------------------------
    bool initialize( HWND hWnd );

    //-----------------------------------------------------------------------
    /// Set up the new frame for rendering. Call this each frame before
    /// any of the other render methods.
    //-----------------------------------------------------------------------
    void renderStart();

    //-----------------------------------------------------------------------
    /// Draws a cross centered at the specified coordinates. Radius is the 
    /// length from the center of the cross to the tip of each arm.
    //-----------------------------------------------------------------------
    void renderCross( int x, int y, int radius );

    //-----------------------------------------------------------------------
    /// A convenience method for calling renderCross() with a coordinate 
    /// pair.
    //-----------------------------------------------------------------------
    void renderCross( const std::pair<int,int>& location, 
                      int radius );

    //-----------------------------------------------------------------------
    /// Draws a filled circle with the specified radius, centered at the 
    /// coordinates given.
    //-----------------------------------------------------------------------
    void renderCircle( int x, int y, int radius );

    //-----------------------------------------------------------------------
    /// Draws a straight line from (x1,y1) to (x2,y2).
    //-----------------------------------------------------------------------
    void renderLine( int x1, int y1, int x2, int y2 );

    //-----------------------------------------------------------------------
    /// Changes the current draw colour used by the render* functions. RGB
    /// values are in the range 0..1.
    //-----------------------------------------------------------------------
    void setDrawColour( float r, float g, float b );
    
    //-----------------------------------------------------------------------
    /// Swaps display buffers. This method may block until the next v-sync
    /// interval.
    //-----------------------------------------------------------------------
    void presentScene();

    //-----------------------------------------------------------------------
    /// Resizes the 3D view to match the current window size.
    //-----------------------------------------------------------------------
    void resize();

private:
    /// Cleanup is done automatically on destruction of the renderer, so
    /// clients don't really need access to this.
    void cleanup();

    /// Sets the view transform and projection matrices on the render 
    /// device.
    void setupCamera();

    /// Desired viewport width.
    unsigned int m_width;
    /// Desired viewport height.
    unsigned int m_height;
        
    /// OpenGL context handle.
    HGLRC m_hRC;
    /// Win32 window handle.
    HWND m_hWnd;

    /// An internal struct for representing colour values.
    struct Colour {
        float r, g, b;
        Colour() {
            r = g = b = 0.0f;
        }
        Colour( float initR, float initG, float initB ) {
            r = initR;
            g = initG;
            b = initB;
        }
    };

    /// The current draw colour used by the render* methods.
    Colour m_drawColour;
};

#endif
