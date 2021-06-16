//---------------------------------------------------------------------------
/// \file main.cpp
/// Program entry point and application level code.
///
/// \mainpage AIO Server Sample
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

// application headers
#include "Socket.h"
#include "GameInstance.h"
#include "platform.h"
#include "debug.h"
// c++
#include <string>
#include <vector>
#include <iostream>
#include <cassert>

using gpg8_util::dlog;
using std::string;
using std::vector;
using std::cout;
using std::endl;
using std::flush;

/// How long to run the main loop, in seconds.
static const double RUN_DURATION = 20.0;
/// Number of client connections to accept before starting the main loop.
static const unsigned int MAX_CLIENTS = 128;
/// Time (in seconds) between sending game state updates to clients.
static const double UPDATE_INTERVAL   = 0.5;

#ifdef SYNCH_IO_LEAN_AND_MEAN
#error This server will not work with the simplified read/write functions.
#endif


//---------------------------------------------------------------------------
/// Program entry point for AioExampleServer. Uses asynchronous I/O by 
/// default. If (any) command line arguments are provided, uses non-blocking
/// synchronous I/O instead. Use simulate_game_clients.py to initiate
/// client connections, preferably from one or more physically separate 
/// machines.
///
/// The server waits for MAX_CLIENTS client connections, and then runs the
/// main loop for RUN_DURATION seconds while recording metrics. After the
/// main loop completes, prints statistics from the run to stdout.
///
/// The main experimental variables for this program are:
///
/// - MAX_CLIENTS. More clients increases the load on the server. Large
///   values may cause failures due to the OS running out of socket handles 
///   or memory.
///
/// - UPDATE_INTERVAL. Sending more frequent updates of the game state to the
///   clients causes more write activity on the sockets. If this interval is
///   too small for clients to finish receiving the previous update before 
///   the next one starts, this will cause "push-backs" in the socket stats.
///
/// - GameInstance::USE_ASYNC_IO. Controlled from the command line. Chooses
///   between asynchronous and synchronous I/O operations.
///
/// - Socket::MAX_READ_SIZE. The expected size of the updates sent from the
///   clients. Should match SEND_SIZE in the client script. Increase this
///   variable to increase read traffic and potentially cause partial reads.
///
/// - Socket::MAX_WRITE_SIZE. The size of the game state updates that will
///   be sent out to the clients. Should match RECV_SIZE in the client 
///   script. Increase this variable to increase write traffic and 
///   potentially trigger partial writes, and would-block scenarios for the
///   synchronous I/O code (waiting for clients to finish receiving the
///   larger data transfers). The clients read as fast as they can.
///
/// - SEND_SKIP_PCT. In simulate_game_clients.py. Controls the percentage of
///   possible client writes that are skipped. This causes the client to 
///   leave the socket unused for portions of time, which means the server
///   has to wait on the idle socket. Should be greater than zero to 
///   simulate clients that don't send a continuous stream of data, i.e. 
///   most real-world game clients.
//---------------------------------------------------------------------------
int main( int argc, char* [] ) {    
    gpg8_util::init();

    if ( argc > 1 ) GameInstance::USE_ASYNC_IO = false;

    // initialization...
    typedef vector<GameInstance> GameInstanceList;
    typedef GameInstanceList::iterator GameIter;
    GameInstanceList gameInstances;
    gameInstances.reserve( MAX_CLIENTS );
    GameInstance protoGame( 16 );
    gameInstances.push_back( protoGame );

    // wait for clients to connect...
    Socket serverSocket;
    bool success = serverSocket.listen( "1234" );
    assert( success ); (void)success;    
    unsigned int numClients = 0;
    cout << "accepting clients" << flush;
    while ( numClients < MAX_CLIENTS ) {
        Socket::SocketPtr pClient = serverSocket.accept();
        assert( pClient->isConnected() );
        ++numClients;

        GameInstance& lastGame = gameInstances.back();
        if ( !lastGame.addClient(pClient) ) {
            gameInstances.push_back(protoGame);
            gameInstances.back().addClient(pClient);
        }
    }
    cout << "done." << endl;

    // set up for the main loop...
    double tickStart, workTime;
    double dt          = 1.0 / 60.0;
    double totalWorkingTime = 0;
    double totalElapsedTime = 0;
    const int ticksPerClientUpdate 
        = int((UPDATE_INTERVAL / dt) / gameInstances.size());
    int ticksToNextUpdate = ticksPerClientUpdate;
    GameIter end       = gameInstances.end();
    GameIter clientUpdateIter = gameInstances.begin();
    cout << "starting main loop" << endl; 

    // run the main game loop(s)...
    while ( totalElapsedTime < RUN_DURATION ) {

        tickStart = gpg8_util::getTime();	        
        // tick each game instance
        for ( GameIter gameInstance = gameInstances.begin(); 
              gameInstance != end; 
              ++gameInstance ) {
            gameInstance->tick(dt);
        }

        // update one instance's clients
        --ticksToNextUpdate;
        if ( ticksToNextUpdate <= 0 ) {
            dlog( "updating clients" );
            clientUpdateIter->updateClients();
            ++clientUpdateIter;
            if ( clientUpdateIter == end ) {
                clientUpdateIter = gameInstances.begin();
            }
            ticksToNextUpdate = ticksPerClientUpdate;
        }      
        workTime  = gpg8_util::getTime() - tickStart;

        totalWorkingTime += workTime;
        // sync to frame-rate
        gpg8_util::sleep( dt - workTime );
        totalElapsedTime += gpg8_util::getTime() - tickStart;
    }

    // output stats and exit...
    std::cout.precision(3);
    std::cout.width(5);
    std::cout << "\nIdle pct             : " << std::fixed 
              << ( (totalElapsedTime - totalWorkingTime) 
                       / totalElapsedTime ) * 100 
              << "% (i.e. time available for game code)" << std::endl;    
    Socket::reportStats( totalWorkingTime );

    gpg8_util::cleanup();

    return 0;
}

