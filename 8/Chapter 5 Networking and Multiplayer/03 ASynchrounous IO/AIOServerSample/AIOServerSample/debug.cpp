//---------------------------------------------------------------------------
/// \file debug.cpp
/// Definitions for debug support features.
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
#include "debug.h"
#include <iostream>
#include <string>

using namespace std;

/// Enable/disable debug output
bool gpg8_util::bDebug = false;
/// Enable/disable really verbose debug output
bool gpg8_util::bTrace = false;


//---------------------------------------------------------------------------
/// If you're in a really performance sensitive area of the code, prefer:
///
///     if (bDebug) dlog(...); 
///
/// to avoid the function call overhead.
//---------------------------------------------------------------------------
void gpg8_util::dlog( const string& msg ) {
    if ( bDebug ) cerr << msg << endl;
}


//---------------------------------------------------------------------------
/// If you're in a performance sensitive area of the code, prefer:
///
///     if ( bTrace ) tlog(...);
///
/// to avoid the function call overhead.
//---------------------------------------------------------------------------
void gpg8_util::tlog( const string& msg ) {
    if ( bTrace ) cerr << msg << endl;
}
