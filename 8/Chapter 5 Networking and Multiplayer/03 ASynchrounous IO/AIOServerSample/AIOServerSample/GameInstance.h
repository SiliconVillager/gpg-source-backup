//---------------------------------------------------------------------------
/// \file GameInstance.h
/// Declarations for GameInstance class.
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
#ifndef GPG8_GAME_INSTANCE_H
#define GPG8_GAME_INSTANCE_H 1

#include "Socket.h"
#include <vector>

//---------------------------------------------------------------------------
/// The GameInstance class models one game session, which consists mainly
/// of a collection of connections from the players and a shared gamestate.
/// In this model, the game state is just a struct padded out with an array
/// to simulate whatever amount of data is specified by the socket's 
/// MAX_WRITE_SIZE. Similarly, this class models input from the players as 
/// another padded struct sized to match the MAX_READ_SIZE from the socket.
//---------------------------------------------------------------------------
class GameInstance {
public:
    /// Ends the game instance and closes down all connections.
    ~GameInstance();

    /// Creates a game instance for up to the specified number of players.
    GameInstance( unsigned int MAX_CLIENTS );

    /// Copy constructor.
    GameInstance( const GameInstance& );

    /// Copy assignment.
    GameInstance& operator=( const GameInstance& );
     
    /// Adds a player to the game, associated with their socket connection.
    bool addClient( Socket::SocketPtr pClientSocket );

    /// Executes one time-step/frame of the game.
    void tick( double dt );

    /// Pushes a game-state update out to the clients. This allows the
    /// server to control scheduling of updates.
    void updateClients();
    
    /// A global configuration variable that indicates whether the game
    /// instances should use asychnronous I/O to communicate with clients.
    /// Otherwise, non-blocking sychronous I/O is used.
    static bool USE_ASYNC_IO;

private:
    /// A collection of sockets.
    typedef std::vector<Socket::SocketPtr> SocketList;
    /// Iterator over a collection of sockets.
    typedef GameInstance::SocketList::iterator SocketIter;

    /// These are the sockets for all of the connected players.
    SocketList m_clientSockets;

    /// Maximum number of players allowed in this session.
    unsigned int m_MaxClients;
    
    /// This struct contains the actual data the server is using in the
    /// GameState struct. The rest is padding.
    struct ActiveGameState {
        int numPlayers;
        // some other game info would go here...
    };
    
    /// The number of bytes required to pad after ActiveGameState to 
    /// reach the MAX_WRITE_SIZE.
    static const size_t GAME_PADDING_SIZE 
        = Socket::MAX_WRITE_SIZE - sizeof(ActiveGameState);

    /// The GameState struct represent a block of data to be replicated to
    /// the clients. It is filled with extra padding to make the network 
    /// transfers more substantial.
    struct GameState {
        /// Real data.
        ActiveGameState stateData;        
        /// Dummy data.
        char padding[GAME_PADDING_SIZE];        

        /// Constructor, fills the padding with a printable character.
        GameState();
    };

    /// This session's GameState.
    GameState m_currentGameState;
    
    /// This struct contains the actual data the server uses in the 
    /// PlayerInput struct. The rest is padding.
    struct ActivePlayerInput {
        // actually, the server completely ignores the contents of the
        // data from the clients, so even this is not strictly neccessary.
        int playerID;
    };
    
    /// Bytes needed to pad ActivePlayerInput out to the MAX_READ_SIZE.
    static const size_t INPUT_PADDING_SIZE
        = Socket::MAX_READ_SIZE - sizeof(ActivePlayerInput);
    
    /// The actual struct we receive from the clients, which is padded out
    /// to make the network transfers more substantial.
    struct PlayerInput {
        /// Real data.
        ActivePlayerInput inputData;
        /// Dummy data.
        char padding[INPUT_PADDING_SIZE];            
    };
};

#endif
