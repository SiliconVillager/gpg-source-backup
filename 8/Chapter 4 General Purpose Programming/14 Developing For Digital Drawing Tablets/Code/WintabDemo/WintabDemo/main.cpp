//---------------------------------------------------------------------------
/// \file main.cpp
/// Program entry point and application level code.
///
/// \mainpage Wintab Demo
/// \verbinclude README.txt
/// \verbinclude License.txt
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
#define WIN32_LEAN_AND_MEAN 1

// application includes
#include "TabletInterface.h"
#include "GLRenderer.h"
// system includes
#include <windows.h>
#include <windowsx.h>
// C++ includes
#include <cassert>
#include <cstdio>
#include <cmath>

static const TCHAR* WINDOW_CLASS_NAME   = TEXT("VXB Main Window"); 
static const TCHAR* WINDOW_TITLE        = TEXT("Wintab Demo");
static const int DEFAULT_DISPLAY_WIDTH  = 1200;
static const int DEFAULT_DISPLAY_HEIGHT = 800;
static const int TILT_INDICATOR_LENGTH  = 100;

/// The tablet interface for this application.
static TabletInterface g_tablet( DEFAULT_DISPLAY_WIDTH,
                                 DEFAULT_DISPLAY_HEIGHT );

/// The rendering interface for this application.
static GLRenderer g_renderer( DEFAULT_DISPLAY_WIDTH,
                              DEFAULT_DISPLAY_HEIGHT );

//---------------------------------------------------------------------------
/// Standard Win32 callback for processing system messages.
//---------------------------------------------------------------------------
LRESULT CALLBACK WindowProc( HWND hWnd, UINT msg, WPARAM wparam, 
                             LPARAM lparam );


//---------------------------------------------------------------------------
/// Contains the Win32 logic for creating the main window for this
/// application. Returns the handle to the window.
//---------------------------------------------------------------------------
HWND CreateAppWindow( HINSTANCE hInstance ) {
    WNDCLASSEX winClass;
    HWND hWnd;

    winClass.cbSize        = sizeof( WNDCLASSEX );
    winClass.style         = CS_DBLCLKS | CS_OWNDC | CS_HREDRAW | CS_VREDRAW;
    winClass.lpfnWndProc   = WindowProc;
    winClass.cbClsExtra    = 0;
    winClass.cbWndExtra    = 0;
    winClass.hInstance     = hInstance;
    winClass.hIcon         = LoadIcon( NULL, IDI_APPLICATION );
    winClass.hCursor       = LoadCursor( NULL, IDC_ARROW );
    winClass.hbrBackground = (HBRUSH)GetStockObject( BLACK_BRUSH );
    winClass.lpszMenuName  = NULL;
    winClass.lpszClassName = WINDOW_CLASS_NAME;
    winClass.hIconSm       = LoadIcon( NULL, IDI_APPLICATION );
    // now we can register the window-class we just defined.
    RegisterClassEx( &winClass );

    hWnd = CreateWindowEx( NULL, WINDOW_CLASS_NAME, WINDOW_TITLE, 
                           WS_OVERLAPPEDWINDOW | WS_VISIBLE,
                           0, 0, 
                           DEFAULT_DISPLAY_WIDTH, DEFAULT_DISPLAY_HEIGHT,
                           NULL, NULL, hInstance, NULL );    
    assert( hWnd );
    // resize the window so that the rendering area matches the
    // desired display size, accounting for title bar etc.
    RECT clientRect = { 0, 0, 
                        DEFAULT_DISPLAY_WIDTH, 
                        DEFAULT_DISPLAY_HEIGHT };
    AdjustWindowRectEx( &clientRect, GetWindowStyle(hWnd), 
                        GetMenu(hWnd) != NULL, 
                        GetWindowExStyle(hWnd) );
    MoveWindow( hWnd, 10, 10, 
                clientRect.right - clientRect.left,
                clientRect.bottom - clientRect.top, true );

    return hWnd;
}


//---------------------------------------------------------------------------
/// Program entry point. This is a simple Win32 application that uses the
/// TabletInterface class to get input from a digitizer tablet like a Wacom
/// Intuos, and draw some simple indicators in the app window to represent
/// that input.
//---------------------------------------------------------------------------
int WINAPI WinMain( HINSTANCE hInstance, HINSTANCE hPrevInstance, 
                    LPSTR lpCmdLine, int nShowCmd ) {
    HWND hWnd = CreateAppWindow( hInstance );

    g_tablet.initDevices( hWnd );
    g_renderer.initialize( hWnd );

    // application main loop
    bool bKeepRunning = true;
    while ( bKeepRunning ) {
        MSG msg;
        // process the Windows message queue...
        while ( PeekMessage(&msg, NULL, 0, 0, PM_REMOVE) ) {
            TranslateMessage( &msg );
            DispatchMessage( &msg );
            if ( msg.message == WM_QUIT ) bKeepRunning = false;
        }

        if ( bKeepRunning ) {
            g_renderer.renderStart();

            // draw stylus location
            TabletInterface::Coords2D cursorCoords
                = g_tablet.getCursorLocation();
            TabletInterface::CursorState cursorState
                = g_tablet.getCursorState();
            // set colour based on button states
            if ( cursorState.buttonStates[1] 
                     == TabletInterface::button_down ) {
                g_renderer.setDrawColour( 1, 1, 0 );
            }
            else if ( cursorState.buttonStates[2] 
                          == TabletInterface::button_down ) {
                g_renderer.setDrawColour( 0, 1, 1 );
            }
            else {
                g_renderer.setDrawColour( 0.5, 0.5, 0.5 );    
            }
            // override colour when pen is down
            if ( cursorState.proximity 
                    == TabletInterface::down ) {
                g_renderer.setDrawColour( 0, 
                                          cursorState.pressure, 
                                          0 );
            }

            TabletInterface::CursorType cursorType
                = g_tablet.getCursorType();
            if ( cursorState.proximity != TabletInterface::unavailable ) {
                if ( cursorType == TabletInterface::pen ) {
                    g_renderer.renderCross( cursorCoords, 10 );
                }
                else if ( cursorType == TabletInterface::eraser ) {
                    g_renderer.renderCircle( cursorCoords.first,
                                             cursorCoords.second,
                                             10 );
                }
            }

            // draw a line representing tilt orientation
            if ( cursorState.proximity != TabletInterface::unavailable ) {
                g_renderer.setDrawColour( 1, 0, 1 );
                float length = TILT_INDICATOR_LENGTH 
                                   * cos( cursorState.altitudeRads );
                float tiltX = sin( cursorState.azimuthRads ) * length;
                float tiltY = cos( cursorState.azimuthRads ) * length;
                g_renderer.renderLine( cursorCoords.first,
                                       cursorCoords.second,
                                       (int)(cursorCoords.first + tiltX),
                                       (int)(cursorCoords.second + tiltY) );
            }

            g_renderer.presentScene();
        }
    }
                                               
    return 0;
}


//--------------------------------------------------------------------------
// Standard Windows message handler.
//--------------------------------------------------------------------------
LRESULT CALLBACK WindowProc( HWND hWnd, UINT msg, 
                             WPARAM wParam, LPARAM lParam )
{
    PAINTSTRUCT ps;
    HDC hdc;

    bool bMsgHandled 
        = g_tablet.processWinMessage(hWnd, msg, wParam, lParam);

    switch( msg )
    {
    case WM_CREATE:        
        // init goes here
        return 0;

    case WM_PAINT:
        hdc = BeginPaint( hWnd, &ps );
        EndPaint( hWnd, &ps );
        return 0;

    case WM_DESTROY:
        PostQuitMessage( 0 );
        return 0;

    case WM_SIZE:
        g_renderer.resize();
        InvalidateRect( hWnd, NULL, TRUE );
        return 0;
        
    case WM_MOVE:
        InvalidateRect( hWnd, NULL, TRUE );
        return 0;

    case WM_CHAR:
        switch ( wParam ) {
        case 27:                // ESC key
            PostQuitMessage(0);
            break;
        }
        return 0;

    default:
        // oops!
        break;
    }

    return bMsgHandled 
            ? 0
            : DefWindowProc( hWnd, msg, wParam, lParam );
}
