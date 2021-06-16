//---------------------------------------------------------------------------
/// \file TabletInterface.cpp
/// Definitions for TabletInterface class.
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
#include "TabletInterface.h"

#include <iostream>
#include <cassert>
#include <dbt.h>

/// A reasonable approximate to Pi for our needs
static const float PI = 3.14159265f;
const TabletInterface::CursorInfo TabletInterface::DeviceInfo::unknownCursor;

/// Clamp x to the range between min and max, inclusive.
static int clamp( int x, int min, int max );


//---------------------------------------------------------------------------
/// This implementation asserts on failure.
//---------------------------------------------------------------------------
TabletInterface::TabletInterface( const int width, const int height )
    : m_hContext(0), m_hHardwareNotify(0), m_hWnd( 0 ),
      m_lastCursor(), m_lastCursorLocation(0, 0) {
    // check for Wintab
    UINT result = WTInfo( 0, 0, NULL );
    assert( result && "Wintab32.dll missing" );    

    // This mapping is based on the what comes back from my Intuos3, YMMV.
    m_device.cursorNameToType["Eraser  "] = eraser;
    m_device.cursorNameToType["Pressure Stylus"] = pen;
}


//---------------------------------------------------------------------------
// Destructor.
//---------------------------------------------------------------------------
TabletInterface::~TabletInterface(void) {
    resetDevices();
    UnregisterDeviceNotification( m_hHardwareNotify );
}


//---------------------------------------------------------------------------
/// Sets up device detection if neccessary, which triggers a message when
/// the system detects a new devices has been connected.
//---------------------------------------------------------------------------
void TabletInterface::initDevices( HWND hWnd ) {
    LOGCONTEXT contextParams;
    m_hWnd = hWnd;

    // release any existing tablet resources
    resetDevices();

    // register for hardware add/remove notifications, so that we can 
    // detect if a tablet was plugged in or disconnected.
    if ( !m_hHardwareNotify ) {
        DEV_BROADCAST_DEVICEINTERFACE notificationFilter;
        ZeroMemory( &notificationFilter, sizeof(notificationFilter) );
        notificationFilter.dbcc_size = sizeof(DEV_BROADCAST_DEVICEINTERFACE);
        notificationFilter.dbcc_devicetype = DBT_DEVTYP_DEVICEINTERFACE;        
        m_hHardwareNotify 
            = RegisterDeviceNotification( hWnd, 
                                          &notificationFilter, 
                                          DEVICE_NOTIFY_WINDOW_HANDLE );
    }

    if ( !queryDevices() ) {        
        // no tablets to init
        return;
    }

    // Start with a default context config, which will have most of
    // the settings we want already
    WTInfo( WTI_DEFCONTEXT, 0, &contextParams );

    // modify the digitizing region
    strcpy( contextParams.lcName, "TabletInterface Context" );
    contextParams.lcOptions  |= CXO_MESSAGES | CXO_CSRMESSAGES | CXO_SYSTEM;
    contextParams.lcPktData   = PACKETDATA;
    // bits set in lcPktMode will cause items to use relative reporting mode
    contextParams.lcPktMode   = PACKETMODE;
    contextParams.lcMoveMask  = PACKETDATA;
    contextParams.lcBtnUpMask = contextParams.lcBtnDnMask;

    // create the context for interacting with the tablet using
    // the settings above
    m_hContext = WTOpen( hWnd, &contextParams, TRUE );
    assert( m_hContext );

    // resize will handle setting up coordinate mappings
    resize();

    // as per Wacom's developer tips, increase the queue size
    // to avoid overflow
    int qsize  = WTQueueSizeGet( m_hContext );
    int result = WTQueueSizeSet( m_hContext, MAX_QUEUED_PACKETS );
    assert( result && "Failed to increase packet queue size" );
}


//---------------------------------------------------------------------------
/// Releases the Wintab context.
//---------------------------------------------------------------------------
void TabletInterface::resetDevices() {
    if ( m_hContext ) {
        WTEnable( m_hContext, false );
        WTClose( m_hContext );
    }
}


//---------------------------------------------------------------------------
// Reconfigure scaling parameters to match the current application window.
//---------------------------------------------------------------------------
void TabletInterface::resize() {
    if ( !m_hWnd ) return;

    LONG sysX, sysY;
    WTInfo( WTI_DEFCONTEXT, CTX_SYSEXTX, &sysX );
    WTInfo( WTI_DEFCONTEXT, CTX_SYSEXTY, &sysY );

    // get new window dimensions, converted to screen coords
    GetClientRect( m_hWnd, &m_appArea ); 
    MapWindowPoints( m_hWnd, NULL, (LPPOINT)&m_appArea, 
                     sizeof(RECT)/sizeof(POINT) );

    LOGCONTEXT contextParams;
    WTGet( m_hContext, &contextParams );
    // configure tablet mapping to match current app and screen 
    // dimensions
    contextParams.lcOutOrgX = -1 * m_appArea.left; 
    contextParams.lcOutOrgY = -1 * (sysY - m_appArea.bottom);
    contextParams.lcOutExtX = sysX;
    contextParams.lcOutExtY = sysY;
    WTSet( m_hContext, &contextParams );
}


//---------------------------------------------------------------------------
// Simple accessor.
//---------------------------------------------------------------------------
TabletInterface::CursorState TabletInterface::getCursorState() {
    return m_cursorState;
}


//---------------------------------------------------------------------------
// Simple accessor.
//---------------------------------------------------------------------------
TabletInterface::CursorType TabletInterface::getCursorType() {
    return m_lastCursor.type;
}


//---------------------------------------------------------------------------
// Simple accessor.
//---------------------------------------------------------------------------
TabletInterface::Coords2D TabletInterface::getCursorLocation() {
    return m_lastCursorLocation;
}


//---------------------------------------------------------------------------
// The app should be calling this from its WindowProc.
//---------------------------------------------------------------------------
bool TabletInterface::processWinMessage( HWND hWnd, UINT msg,
                                         WPARAM wParam, LPARAM lParam ) {
    assert( hWnd == m_hWnd || m_hWnd == NULL );
    PACKET pkt;    

    switch ( msg ) 
    {
    ////////// app level messages that affect tablet operations /////////////

    case WM_ACTIVATE:
    {
        // when deactivated, push contexts to bottom of overlap order
        // so as not to conflict with other apps using the tablet        
        WORD isMinimized = HIWORD( wParam );
        WORD isActivated = !( LOWORD(wParam) & WA_INACTIVE );
        WTOverlap( m_hContext, !isMinimized && isActivated );
        return true;
    }

    case WM_MOVE:
        resize();
        return true;

    case WM_SIZE:
        resize();
        return true;

    case WM_DEVICECHANGE:
        // A tablet may have been connected/disconnected
        //
        // NOTE: This simple implementation assumes every device change
        //       should trigger a reinit(). Could be made less disruptive by
        //       examining the device change event in more detail.
        resetDevices();
        initDevices( hWnd );
        return true;

    case WT_INFOCHANGE:
        // device added/removed
        resetDevices();
        initDevices( hWnd );
        return true;

    ////////////////////// Tablet related messages //////////////////////////

    case WT_PROXIMITY:
        // Cursor moved in to or out of range of the tablet  
        // lParam - low word is true when entering context,
        //          high word is true when entering hardware proximity
        if ( LOWORD(lParam) ) {
            m_cursorState.proximity = hover;
        }
        else m_cursorState.proximity = unavailable;
        return true;
    
    case WT_CSRCHANGE:
        // Cursor detected
        if ( dequeueWintabPacket(wParam, lParam, pkt) ) {
            processCursorChange( pkt );
            processWintabPacket( pkt );
        }
        return true;

    case WT_PACKET:
        processPacketQueue();
        return true;    

    default:
        return false;
    }
}


///////////////////////// INTERNAL HELPERS //////////////////////////////////

//---------------------------------------------------------------------------
/// Uses the default device.
//---------------------------------------------------------------------------
bool TabletInterface::queryDevices() {
    if ( !WTInfo(0, 0, NULL) ) {
        // Wintab not available, or no tablets present
        return false;
    }

    // Query Wintab version
    WORD thisVersion;
    WTInfo(WTI_INTERFACE, IFC_SPECVERSION, &thisVersion); 
    BYTE majorVersion = HIBYTE(thisVersion);
    BYTE minorVersion = LOBYTE(thisVersion);

    UINT numDevices;
    WTInfo( WTI_INTERFACE, IFC_NDEVICES, &numDevices );
    if ( numDevices < 1 ) return false;

    // NOTE: Use the default device. This means the code probably won't
    //       handle multiple simultaneous tablets correctly.
    UINT deviceIdx;
    WTInfo( WTI_DEFCONTEXT, CTX_DEVICE, &deviceIdx );
    m_device.deviceIdx = deviceIdx;

    // NOTE: Strictly speaking, you're supposed to call WTInfo() with a NULL
    //       lpOutput param to get the size required for the info you're
    //       requesting. Instead, we'll just use a really big static buffer.
    TCHAR resultString[1024];    
    WTInfo( WTI_DEVICES + deviceIdx, DVC_NAME, resultString );

    // get pressure scales
    AXIS axisData;
    WTInfo( WTI_DEVICES + deviceIdx, DVC_NPRESSURE, &axisData );
    m_device.pressureScale = 1.0f / axisData.axMax;

    // check for tilt support
    AXIS tiltOrient[3];
    BOOL tiltSupport = FALSE;
    tiltSupport = WTInfo( WTI_DEVICES + deviceIdx, DVC_ORIENTATION, 
                          &tiltOrient );
    if ( tiltSupport ) {
        // Does the tablet support azimuth and altitude
        if ( tiltOrient[0].axResolution 
                && tiltOrient[1].axResolution ) {
            m_device.azimuthScale 
                = 1.0f / (tiltOrient[0].axMax - tiltOrient[0].axMin);
            m_device.altitudeScale 
                = 1.0f / (tiltOrient[1].axMax - tiltOrient[1].axMin);
        }
        else tiltSupport = FALSE;                
    }
    m_device.bCanTilt = tiltSupport != 0;

    // cursor properties
    UINT numCursors, firstCursor;
    WTInfo( WTI_DEVICES + deviceIdx, DVC_NCSRTYPES, &numCursors );
    WTInfo( WTI_DEVICES + deviceIdx, DVC_FIRSTCSR, &firstCursor );

    m_device.cursorIDToInfo.clear();
    for ( UINT i = firstCursor; i < firstCursor + numCursors; ++i ) {
        // add to cursor id mapping
        if ( WTInfo( WTI_CURSORS + i, CSR_NAME, resultString ) ) {
            DeviceInfo::CursorNameTable::iterator pos 
                = m_device.cursorNameToType.find( resultString );
            // skip unrecognized cursor types
            if ( pos == m_device.cursorNameToType.end() ) continue;
            CursorInfo cursorInfo;
            cursorInfo.type = pos->second;

            // get buttons for the cursor
            BYTE numButtons = 0;
            WTInfo( WTI_CURSORS + i, CSR_BUTTONS, &numButtons );
            if ( numButtons > 0 ) {
                BYTE buttonMap[32];
                BYTE pressureButton;
                WTInfo( WTI_CURSORS + i, CSR_BUTTONMAP, buttonMap );
                WTInfo( WTI_CURSORS + i, CSR_NPBUTTON, &pressureButton );
                cursorInfo.pressureButton = buttonMap[pressureButton];
            }          

            // save the cursor info
            m_device.cursorIDToInfo[i] = cursorInfo;
        }
    }
    return true;
}


//---------------------------------------------------------------------------
/// WT_PACKET message supply:
///  - wParam - serial number of packet
///  - lParam - context of the packet    
//---------------------------------------------------------------------------
bool TabletInterface::dequeueWintabPacket( WPARAM wParam, LPARAM lParam,
                                           PACKET& pktOut ) {
    UINT oldestSerial, newestSerial;
    // assert that we're not handling multiple contexts...
    assert( m_hContext == reinterpret_cast<HCTX>(lParam) );

    WTQueuePacketsEx( m_hContext, &oldestSerial, &newestSerial );
    return WTPacket( m_hContext, wParam ? wParam 
                                        : newestSerial, &pktOut ) != 0;
}


//---------------------------------------------------------------------------
/// Updates interal representations for location, button states, cursor 
/// state.
//---------------------------------------------------------------------------
void TabletInterface::processWintabPacket( const PACKET& pkt ) {
    // update Cursor location...
    m_lastCursorLocation.first 
        = clamp( pkt.pkX, 
                 0, m_appArea.right - m_appArea.left - 1);          
    m_lastCursorLocation.second 
        = clamp( pkt.pkY, 
                 0, m_appArea.bottom - m_appArea.top - 1);

    // FIXME: doesn't handle menu bar correctly.
    if ( (pkt.pkX > 0) && (pkt.pkX < m_appArea.right - m_appArea.left)
             && (pkt.pkY > 0) 
                && (pkt.pkY < m_appArea.bottom - m_appArea.top) ) {
        ShowCursor( false );
    }
    else {
        ShowCursor( true );     
    }

    if ( m_device.bCanTilt ) {
        m_cursorState.azimuthRads 
            = 2 * PI * pkt.pkOrientation.orAzimuth 
                * m_device.azimuthScale;   
        m_cursorState.altitudeRads
            = PI * pkt.pkOrientation.orAltitude
                * m_device.altitudeScale;
    }

    DWORD buttonNum    = LOWORD( pkt.pkButtons );
    DWORD buttonChange = HIWORD( pkt.pkButtons );
    if ( buttonChange != TBN_NONE ) {
        // handle pen tip
        if ( buttonNum == m_lastCursor.pressureButton ) {
            m_cursorState.proximity 
                = (buttonChange == TBN_DOWN)
                    ? down
                    : hover;
            // WT_PROXIMITY messages will handle hover->unavailable
        }
        m_cursorState.buttonStates[buttonNum] 
            = (buttonChange == TBN_DOWN) 
                  ? button_down
                  : button_up;
    }

    if ( m_cursorState.proximity == down ) {
        m_cursorState.pressure 
            = pkt.pkNormalPressure * m_device.pressureScale;
    }
    else {
        m_cursorState.pressure = 0.0f;
    }
}


//---------------------------------------------------------------------------
// Updates the current cursor, based on packet contents.
//---------------------------------------------------------------------------
void TabletInterface::processCursorChange( const PACKET& pkt ) {
    UINT numCursors;
    WTInfo( WTI_INTERFACE, IFC_NCURSORS, &numCursors );
    UINT cursorID = pkt.pkCursor % numCursors;

    DeviceInfo::CursorInfoTable::iterator pos 
        = m_device.cursorIDToInfo.find( cursorID );
    if ( pos != m_device.cursorIDToInfo.end() ) {
        m_lastCursor = pos->second;
    }
    else {
        m_lastCursor = m_device.unknownCursor;
    } 

    return;
}


//---------------------------------------------------------------------------
// Iterate through the entire packet queue, so that it will be empty when
// we're done.
//---------------------------------------------------------------------------
void TabletInterface::processPacketQueue() {
    PACKET pktList[MAX_QUEUED_PACKETS];
    int numPackets = WTPacketsGet( m_hContext, MAX_QUEUED_PACKETS, pktList );
    for ( int i = 0; i < numPackets; ++i ) {
        processWintabPacket( pktList[i] );
    }

}


//---------------------------------------------------------------------------
// Clamps x between min and max, inclusive.
//---------------------------------------------------------------------------
int clamp( int x, int min, int max ) {
    if ( x > max ) x = max;
    if ( x < min ) x = min;
    return x;
}
