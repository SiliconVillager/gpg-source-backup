//---------------------------------------------------------------------------
/// \file platform.cpp
/// Win32 definitions for platform features.
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

// application headers
#include "../platform.h"
// Windows headers
#include <winsock2.h>
#include <crtdbg.h>

/// A global variable for the number of PerformanceCounter ticks per
/// second.
static LARGE_INTEGER frequency;


//---------------------------------------------------------------------------
/// Initializes various Windows systems used in the server.
//---------------------------------------------------------------------------
void gpg8_util::init() {
#ifndef NDEBUG
    // enable basic memory leak checking
    _CrtSetDbgFlag( _CRTDBG_ALLOC_MEM_DF | _CRTDBG_LEAK_CHECK_DF );
#endif
    // initialize Winsock
    WSADATA info; 
    WSAStartup( MAKEWORD(1,1), &info );
    // get the PerformanceCounter frequency.
    QueryPerformanceFrequency( &frequency );
}

//---------------------------------------------------------------------------
/// Shutdown Winsock.
//---------------------------------------------------------------------------
void gpg8_util::cleanup() {
    WSACleanup();
}

//---------------------------------------------------------------------------
/// Uses SleepEx() to yeild for event notifications, and then Sleep() to get
/// an uninterrupted sleep for the specified duration.
//---------------------------------------------------------------------------
void gpg8_util::sleep( double seconds ) {
    seconds = max( seconds, 0 );
    // allow windows to dispatch completion handlers
    SleepEx(1, true);
    Sleep( (DWORD)(seconds * 1000) );
}

//---------------------------------------------------------------------------
/// Use high resolution PerformanceCounters to generate a precise timestamp.
//---------------------------------------------------------------------------
double gpg8_util::getTime() {
    LARGE_INTEGER rawTime;
    QueryPerformanceCounter( &rawTime );

    return (double)rawTime.QuadPart / (double)frequency.QuadPart;
}
