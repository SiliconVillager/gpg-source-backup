//---------------------------------------------------------------------------
/// \file platform.cpp
/// Defintions for interface to POSIX specific features
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
// C++
#include <algorithm>
#include <iostream>
// POSIX
#include <sys/time.h>
#include <errno.h>

using namespace std;


//---------------------------------------------------------------------------
/// This is a no-op for POSIX.
//---------------------------------------------------------------------------
void gpg8_util::init() {}


//---------------------------------------------------------------------------
/// This is a no-op for POSIX.
//---------------------------------------------------------------------------
void gpg8_util::cleanup() {}


//---------------------------------------------------------------------------
/// Uses nanosleep for a high-resolution sleep function. May call sleep()
/// more than once if interrupted by signals.
//---------------------------------------------------------------------------
void gpg8_util::sleep( double seconds ) {
    seconds = std::max( seconds, 0.0 );
    timespec sleepReq, sleepLeft;
    sleepLeft.tv_sec  = 0;
    sleepLeft.tv_nsec = (long int)(seconds * 1000000000.0);
    int rc = EINTR;
    
    // signals can interrupt the sleep, so we loop through the
    // interruptions until the full sleep duration is complete.
    while ( rc == EINTR ) {
        sleepReq = sleepLeft;
        rc = nanosleep( &sleepReq, &sleepLeft );
    }
}


#ifdef __APPLE__
//---------------------------------------------------------------------------
// OSX doesn't provide POSIX gethrtime()
//---------------------------------------------------------------------------
double gpg8_util::getTime() {
    assert( false );
    return 0;
}
#else
//---------------------------------------------------------------------------
/// Uses gethrtime(), which is in nanoseconds.
//---------------------------------------------------------------------------
double gpg8_util::getTime() {
    // For OpenSolaris:
    hrtime_t timeStamp = gethrtime();
    return timeStamp / 1000000000.0;   

    // For Linux
    //timespec timeStamp;
    //clock_gettime(CLOCK_PROCESS_CPUTIME_ID,&timeStamp);
    //return timeStamp.tv_nsec / 1000000000.0;   
}
#endif
