//---------------------------------------------------------------------------
/// \file GameInstance.cpp
/// Definitions for GameInstance class.
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
#include "GameInstance.h"
#include "debug.h"
// c++
#include <cassert>
#include <iostream>
#include <cstring>

#ifdef _MSC_VER
// disable MSVC warnings that cause /W4 to fail.
#pragma warning( disable: 4127 )
#endif

using namespace std;
using gpg8_util::dlog;

/// Defaults to true, but can be changed at runtime.
bool GameInstance::USE_ASYNC_IO = true;


//---------------------------------------------------------------------------
// Copy constructor.
//---------------------------------------------------------------------------
GameInstance::GameInstance( const GameInstance& rhs )
    : m_clientSockets(rhs.m_clientSockets), m_MaxClients(rhs.m_MaxClients) {
    dlog( "new game instance (copy)" );
}


//---------------------------------------------------------------------------
// Standard constructor.
//---------------------------------------------------------------------------
GameInstance::GameInstance( unsigned int MAX_CLIENTS ) 
    : m_MaxClients(MAX_CLIENTS) {
    dlog( "new game instance" );
    m_clientSockets.reserve( MAX_CLIENTS );    
}


//---------------------------------------------------------------------------
// Destructor. Closes each socket before destroying it.
//---------------------------------------------------------------------------
GameInstance::~GameInstance() {
    for ( SocketList::iterator i = m_clientSockets.begin();
          i != m_clientSockets.end();
          ++i ) {
        (*i)->close();
        delete *i;
    }
}


//---------------------------------------------------------------------------
// Copy assignment.
//---------------------------------------------------------------------------
GameInstance& GameInstance::operator=( const GameInstance& rhs ) {
    if ( this == &rhs ) return *this;

    m_clientSockets = rhs.m_clientSockets;
    m_MaxClients = rhs.m_MaxClients;

    return *this;
}


//---------------------------------------------------------------------------
/// Adds a new client, outputting a "progress dot" to stdout each time.
//---------------------------------------------------------------------------
bool GameInstance::addClient( Socket::SocketPtr pClientSocket ) {
    assert( pClientSocket != NULL );

    if ( m_clientSockets.size() >= m_MaxClients ) return false;
    m_clientSockets.push_back( pClientSocket );
    cout << "." << flush;

    return true;
}


//---------------------------------------------------------------------------
/// Each tick, the game instance performs I/O. Of course a real game server
/// would also update the game-state here.
//---------------------------------------------------------------------------
void GameInstance::tick( double ) {
    // look for input from remote players
    SocketIter end = m_clientSockets.end();
    for ( SocketIter i = m_clientSockets.begin(); i != end; ++i ) {
        // since we're currently ignoring the contents of the player input
        // messages, the read functions can use their own internal buffers.
        if ( USE_ASYNC_IO ) {            
            (*i)->asyncRead( NULL, sizeof(PlayerInput) );        
        }
        else {            
            (*i)->read( NULL , sizeof(PlayerInput) );                       
            // in the synchronous case, we may have to keep retrying writes
            // as well, since they don't always complete without blocking.
            if ( (*i)->isWriting() ) {
                dlog( "multi-part write" );
                (*i)->write( NULL, sizeof(GameState) );
            }        
        }
    }    

    // other per-frame game stuff would happen here...
}


//---------------------------------------------------------------------------
// The server calls this when it is this session's turn to push updates
// out to the clients.
//---------------------------------------------------------------------------
void GameInstance::updateClients() {
    // send current game state to clients
    SocketIter end = m_clientSockets.end();
    for ( SocketIter i = m_clientSockets.begin(); i != end; ++i ) {
        if ( USE_ASYNC_IO ) {
            (*i)->asyncWrite( &m_currentGameState, sizeof(GameState) );
        }
        else {        
            (*i)->write( &m_currentGameState, sizeof(GameState) );
        }
    }
}


GameInstance::GameState::GameState() {
    memset( &padding, 'X', GAME_PADDING_SIZE );
}
