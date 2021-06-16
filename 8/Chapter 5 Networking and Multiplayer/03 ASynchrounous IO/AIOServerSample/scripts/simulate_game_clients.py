"""
Copyright (c) 2009, Neil Gower
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

  * Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.

  * Neither the name of Vertex Blast nor the names of its contributors
    may be used to endorse or promote products derived from this software
    without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

This script runs the client portion of the AioServerSample program. It
opens many connections to the server, and then uses select() to drive
I/O over the connections. There are a few tweakable parameters
provided as constants below.

Usage:

 python simulate_game_clients.py <server address>

Requires Python 2.6.

On OSX, you may need to use ulimit -n NNNN to increase the number of file 
handles you are allowed to have open to NNNN.
"""
import socket
import select
import time
import random
import sys

from ctypes import *

## CONFIGURATION OPTIONS ####################################################
NUM_CLIENTS   = 128
RECV_SIZE     = 32 * 1024 # this should match MAX_WRITE_SIZE on the server
SEND_SIZE     = 8 * 1024  # this should match MAX_READ_SIZE on the server
SEND_SKIP_PCT = 0.7       # increasing this causes the clients to send
                          # data less frequently to the server
#############################################################################


class PlayerInput(Structure):
    _fields_ = [ ('playerID', c_int),
                 ('padding', c_char * (SEND_SIZE - 4)) ]
# end class PlayerInput


def openClientSocket( remoteAddr ):
    """
    Loops until a connection is successfully made to the remote server
    on port 1234.
    """
    connected = False
    while ( not connected ):
        try:
            print "connect attempt"
            clientSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            clientSocket.connect( (remoteAddr, 1234) )
            connected = True
        except socket.error, (value, message):
            print "connect error " + message
            clientSocket.close()
            time.sleep( 0.5 )
        # end try
    # end while
    return clientSocket
# end openClientSocket


def writePlayerInput( writeSocket, playerInput ):
    """
    currently, the protocol is:

    struct PlayerInput {
        int playerID
        char padding[X]
    }
    
    where X is the number of bytes to get to the simulated struct size.
    """
    
    bytesToSend = sizeof( playerInput )
    bytesSent   = 0
    bSendError  = False
    while bytesSent < bytesToSend:
        try:
            bytesSent += writeSocket.send( playerInput )
        except socket.error, (value, message):
            if value == 12:
                pass # ignore, buffer is probably just full
            else:
                bSendError = True
                break
            # end if
        except:
            bSendError = True
        # end try
        # HACK: only attempt to send once, to keep data flowing for others
        bytesSent = bytesToSend
    # end while

    if bSendError:
        print "closing socket due to error"
        writeSocket.close()
        try:
            g_sockets.remove( writeSocket )
        except:
            pass
        # end try
    # end if
# end writePlayerInput


def readGameState( readSocket ) :
    """
    Attempts to recv the game state from the socket.
    """
    try:
        chunk = readSocket.recv(RECV_SIZE)
    except socket.error, (value, message):
        chunk = None
    # end try
    
    return chunk
# end readGameState


def main():
    """
    Makes a bunch of client connections, and then begins sending and
    receiving data until one or more connections drops.
    """
    g_sockets = []
    
    if ( len(sys.argv) > 1 ):
        serverAddr = sys.argv[1]
    else:
        print "warning, results may be misleading running client and server" \
              + "on one computer"
        serverAddr = "127.0.0.1"
    # end-if
    
    for i in range(0,NUM_CLIENTS):
        clientSocket = openClientSocket( serverAddr );
        g_sockets.append( clientSocket );
    # end for
    print "all clients connected"
    
    playerInput = PlayerInput(playerID=13)
    while ( len(g_sockets) == NUM_CLIENTS ):
        readyToRead, readyToWrite, errorSockets \
            = select.select( g_sockets, g_sockets, g_sockets )

        for reader in readyToRead:
            chunk = readGameState( reader )
            if not chunk:
                print "connection closed"
                reader.close()
                g_sockets.remove( reader )
                break;
            # end if
        # end for

        for writer in readyToWrite:
            # don't write every time it's possible, to simulate clients
            # not always having data to send to the server.
            if ( random.random() > SEND_SKIP_PCT ):
                writePlayerInput( writer, playerInput )
            # end-if
        # end-if
    # end while
    print "done."

    for sock in g_sockets:
        sock.close();
    # end-for
# end main    


# program entry point.
if __name__ == "__main__":
    main()
# end __main__
