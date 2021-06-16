//---------------------------------------------------------------------------
/// \file TabletInterface.h
/// Declarations for TabletInterface class.
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
#ifndef GPG8_TABLET_INTERFACE_H
#define GPG8_TABLET_INTERFACE_H 1

// system includes
#include <windows.h>
// wintab SDK
#include "wintab.h"
// C++ includes
#include <utility>
#include <map>
#include <string>

// define these before including pktdef.h to have the PACKET struct
// automagically generated for you.
#define PACKETDATA ( PK_X | PK_Y | PK_BUTTONS | PK_CURSOR \
                     | PK_NORMAL_PRESSURE | PK_ORIENTATION \
                     | PK_SERIAL_NUMBER )
#define PACKETMODE PK_BUTTONS
#include "pktdef.h"


//---------------------------------------------------------------------------
/// The TabletInterface class wraps all of the API code for interacting with
/// a digitizer tablet. This implementation is based on Wintab, but in 
/// principle could be adapted to work with other APIs.
//---------------------------------------------------------------------------
class TabletInterface {
public:  
    //-----------------------------------------------------------------------
    /// For simplicity, using integer coordinates to represent positions.
    //-----------------------------------------------------------------------
    typedef std::pair<int,int> Coords2D;

    //-----------------------------------------------------------------------
    /// These are the different types of cursors (physical pointing devices)
    /// that this code could support.
    //-----------------------------------------------------------------------
    enum CursorType {
        unknown = 0,
        pen,
        eraser,
        airbrush,
        MAX_CURSOR_TYPES
    };
    
    //-----------------------------------------------------------------------
    /// Represents the proximity state of the cursor relative to the tablet.
    //-----------------------------------------------------------------------
    enum CursorProximityState {
        unavailable = 0, // cursor not within sensor range
        hover,           // cursor in proximity, not touching
        down,            // cursor on the tablet
        MAX_CURSOR_PROXIMITY_STATES
    };

    //-----------------------------------------------------------------------
    /// Possible states for buttons.
    //-----------------------------------------------------------------------
    enum ButtonState {
        button_up = 0,
        button_down,
        MAX_BUTTON_STATES
    };

    //-----------------------------------------------------------------------
    /// The CursorState struct gathers together all of the properties we are
    /// tracking that are relevant to the state of the cursor.
    //-----------------------------------------------------------------------
    struct CursorState {
        /// Cursor's current proximity state
        CursorProximityState proximity; 
        /// Normalized (0..1) pressure value from the cursor
        float pressure;
        /// Angle along the horizon (tablet surface) of the cursor's tilt
        float azimuthRads;
        /// Angle above the horizon (tablet surface) of the cursor's tilt
        float altitudeRads;
        /// Buttons associated with this cursor. Maps from logical button ID
        /// to the button's current state.
        std::map<DWORD,ButtonState> buttonStates;
    };

    //-----------------------------------------------------------------------
    /// Standard constructor. Supply the dimensions of your screen-space
    /// to have tablet coordinates translated into screen coordinates.
    //-----------------------------------------------------------------------
    TabletInterface( const int width, const int height );

    //-----------------------------------------------------------------------
    /// Destructor.
    //-----------------------------------------------------------------------
    virtual ~TabletInterface();

    //-----------------------------------------------------------------------
    /// Requires a handle to the application window, scans the system for
    /// tablet devices and updates internal data structures to match. This
    /// class assumes that the HWND will be valid until the instance is 
    /// reinitialized or destroyed.
    //-----------------------------------------------------------------------
    void initDevices( HWND hWnd );

    //-----------------------------------------------------------------------
    /// Clears any device state and interal data structures related to 
    /// previously initialized devices.
    //-----------------------------------------------------------------------
    void resetDevices();

    //-----------------------------------------------------------------------
    /// Update scaling factors based on current window size.
    //-----------------------------------------------------------------------
    void resize();

    //-----------------------------------------------------------------------
    /// Accessor for the CursorType that was most recently detected with the 
    /// tablet.
    //-----------------------------------------------------------------------
    CursorType getCursorType();

    //-----------------------------------------------------------------------
    /// Accessor for the last recorded CursorState values. 
    //-----------------------------------------------------------------------
    CursorState getCursorState();

    //-----------------------------------------------------------------------
    /// Accessor for the scaled coordinates of the cursor that were most
    /// recently recorded.
    //-----------------------------------------------------------------------
    Coords2D getCursorLocation();

    //-----------------------------------------------------------------------
    /// Hook this method into your WindowProc to have the TabletInterface 
    /// handle tablet related messages.  Returns true if the message was 
    /// handled, false otherwise.
    //-----------------------------------------------------------------------
    bool processWinMessage( HWND hWnd, UINT msg,
                            WPARAM wParam, LPARAM lParam );

private:
    static const int MAX_QUEUED_PACKETS = 64;

    /// WinTab context handle
    HCTX m_hContext;
    /// Windows handle for detecting when devices are added/removed.
    HDEVNOTIFY m_hHardwareNotify;
    /// Application window handle
    HWND m_hWnd;

    /// Internal struct for configuration of the current cursor.
    struct CursorInfo {
        /// The kind of cursor are we dealing with.
        CursorType type;        
        /// The ID of the pressure button/tip.
        BYTE pressureButton;    

        /// Default constructor.
        CursorInfo() {
            type = unknown;
            pressureButton = 0;
        }
    };

    /// A internal struct for storing information about the current tablet
    /// device's capabilities. This struct is not exhaustive, but covers all
    /// of the common tablet features.
    struct DeviceInfo {
        /// Default constuctor.
        DeviceInfo() 
            : deviceIdx(0) {}

        /// Wintab device index.
        UINT deviceIdx;
        
        /// Factor used to normalize pressure values from the device to 0..1.
        float pressureScale; 
        /// Does the device support tilt sensing?
        bool bCanTilt;
        /// Factor used to convert azimuth angle from device to rads.
        float azimuthScale;
        /// Factor used to convert altitude angle from device to rads.
        float altitudeScale;
       
        typedef std::map<UINT, CursorInfo> CursorInfoTable;
        /// Maps device IDs to cursor properties.
        CursorInfoTable cursorIDToInfo;
        /// Used when there isn't a CursorInfo in the table for the current
        /// cursor, or when the cursor is not available.
        static const CursorInfo unknownCursor; 
        
        typedef std::basic_string<TCHAR> WinString;
        typedef std::map<WinString, CursorType> CursorNameTable;
        /// Maps strings to CursorTypes.
        CursorNameTable cursorNameToType;
    };

    /// The current device. This implementation only supports one device
    /// at a time.
    DeviceInfo m_device;

    /// The current state of the cursor
    CursorState m_cursorState;

    /// The last cursor type detected. Not particularly useful when
    /// m_cursorState is unavailable.
    CursorInfo m_lastCursor;

    /// The last cursor coordinates detected. Not particularly useful when
    /// m_cursorState is unavailable.
    Coords2D m_lastCursorLocation;

    /// Coordinates of the application window in screen coordinates.
    RECT m_appArea;
    
    /// Interrogates the Wintab devices and stores the results. Returns true
    /// if a device was found and successfully queried, false on failure.
    bool queryDevices();

    /// Helper function for processWinMessage() that takes the latest packet
    /// from the device's queue and writes the values to pktOut. If a packet
    /// serial is not specified by WPARAM, this function uses the newest
    /// packet in the queue.
    bool dequeueWintabPacket( WPARAM, LPARAM, PACKET& pktOut );

    /// Helper function for processWinMessage() that takes a generic Wintab
    /// packet and updates the TabletInterface's state to match the packet
    /// data.
    void processWintabPacket( const PACKET& pkt );

    /// Helper function for processWinMessage() that takes a packet from a 
    /// WT_CSRCHANGE event and updates the TabetInterface's cursor state.
    void processCursorChange( const PACKET& pkt );

    /// Grabs all of the packets currently in the packet queue, and processes
    /// each one with processWintabPacket(). Packets are processed in the 
    /// order they are returned by Wintab.
    void processPacketQueue();
};

#endif
