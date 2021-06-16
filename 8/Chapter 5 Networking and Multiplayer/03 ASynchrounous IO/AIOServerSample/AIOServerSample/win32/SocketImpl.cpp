//---------------------------------------------------------------------------
/// \file SocketImpl.cpp
/// Definitions for Winsock2 version of SocketImpl class
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

// Application headers
#include "SocketImpl.h"
#include "../debug.h"
// c++
#include <cassert>
#include <iostream>
#include <algorithm>

using namespace std;
using gpg8_util::bDebug;
using gpg8_util::bTrace;
using gpg8_util::dlog;
using gpg8_util::tlog;

// global read stats
int SocketImpl::g_logicalReadsCompleted = 0;
int SocketImpl::g_readCalls   = 0;
int SocketImpl::g_readRetries = 0;
int SocketImpl::g_bytesRead   = 0;

// global write stats
int SocketImpl::g_logicalWritesCompleted = 0;
int SocketImpl::g_bytesWritten   = 0;
int SocketImpl::g_writeCalls     = 0;
int SocketImpl::g_writeRetries   = 0;
int SocketImpl::g_writePushBacks = 0;

/// helper for setting up an active (outgoing) socket.
static SocketImpl::SocketHandle connectSocket( const string& host, 
                                               const string& port );

/// Helper for setting up a passive (incoming) socket.
static SocketImpl::SocketHandle bindSocket( const string& port );


//---------------------------------------------------------------------------
/// Default constructor just makes sure internal fields are initialized to 
/// sane values.
//---------------------------------------------------------------------------
SocketImpl::SocketImpl()
    : m_hSocket(INVALID_SOCKET), m_bIsConnected(false), 
      m_bIsListening(false), m_bWriteInProgress(false), 
      m_bReadInProgress(false), m_bIsSyncronous(false),
      m_bytesRead(0), m_bytesSent(0) {
}


//---------------------------------------------------------------------------
/// Used by accept to create a new socket from a socket handle.
//---------------------------------------------------------------------------
SocketImpl::SocketImpl( SocketImpl::SocketHandle rawSocket ) 
    : m_hSocket(rawSocket), m_bIsConnected(rawSocket!=-1), 
      m_bIsListening(false), m_bWriteInProgress(false), 
      m_bReadInProgress(false), m_bIsSyncronous(false),
      m_bytesRead(0), m_bytesSent(0) {
}


//---------------------------------------------------------------------------
/// Handles closing if the caller forgot to.
//---------------------------------------------------------------------------
SocketImpl::~SocketImpl() {
    if ( m_bIsConnected ) close();    
}


//---------------------------------------------------------------------------
/// Make an outgoing connection to the specified host/port.
//---------------------------------------------------------------------------
bool SocketImpl::connect( const string& host, const string& port ) {
    assert( !m_bIsConnected && !m_bIsListening );

    m_hSocket = connectSocket( host, port );
    m_bIsConnected = ( m_hSocket != -1 );

    return m_bIsConnected;
}


//---------------------------------------------------------------------------
/// Put this socket into the listen state on the specified port, so that it
/// can accept incoming connections.
//---------------------------------------------------------------------------
bool SocketImpl::listen( const string& port ) {
    assert( !m_bIsConnected && !m_bIsListening );

    m_hSocket = bindSocket( port ); 
    if ( m_hSocket == -1 ) return false;

    int rc = ::listen( m_hSocket, 5 );
    m_bIsListening = ( rc != -1 );

    return m_bIsListening;
}


//---------------------------------------------------------------------------
/// Blocks on accept().
//---------------------------------------------------------------------------
Socket::SocketPtr SocketImpl::accept() {
    assert( m_bIsListening );
    
    sockaddr theirAddr;
    int addrSize = sizeof( theirAddr );
    SocketHandle clientFD = ::accept( m_hSocket, 
                                      (sockaddr*)&theirAddr, 
                                      &addrSize );
    if ( clientFD == -1 ) return NULL;

    Socket::SocketPtr pClientSocket = new Socket( new SocketImpl(clientFD) );
    return pClientSocket;
}


//---------------------------------------------------------------------------
/// Close the socket properly.
//---------------------------------------------------------------------------
void SocketImpl::close() {
    // With async IO, we have to remember to cancel any active io requests
    // before freeing memory and resources...
    dlog( "closing socket" );
    ::shutdown( m_hSocket, SD_BOTH );

    if ( !m_bIsSyncronous ) {        
        DWORD xfer, flags;
        if ( m_bReadInProgress ) {            
            // block until read completes
            while ( !WSAGetOverlappedResult( m_hSocket, 
                                            &m_activeRead.overlapped,
                                            &xfer, false, &flags ) ) {
                int error = WSAGetLastError();
                if ( error != WSA_IO_INCOMPLETE ) {
                    break;
                }
                SleepEx(100,true);
            }
        }   
        if ( m_bWriteInProgress ) {            
            // block until write completes
            while ( !WSAGetOverlappedResult( m_hSocket, 
                                             &m_activeWrite.overlapped,
                                             &xfer, false, &flags ) ) {
                int error = WSAGetLastError();
                if ( error != WSA_IO_INCOMPLETE ) {
                    break;
                }
                SleepEx(100,true);            
            }
        }        
    }

    ::closesocket( m_hSocket );
    m_bIsConnected = false;
    m_bIsListening = false;
}


//---------------------------------------------------------------------------
/// If m_bIsSyncronous doesn't match bSyncronous, set the appropriate options
/// on the socket for the specified mode.
//---------------------------------------------------------------------------
void SocketImpl::setSocketFlags( bool bSyncronous ) {
    u_long mode;
    if ( bSyncronous && !m_bIsSyncronous ) {
        mode = 1;
        ioctlsocket( m_hSocket, FIONBIO, &mode );
        m_bIsSyncronous = true;
    }
    else if ( !bSyncronous && m_bIsSyncronous ) {
        mode = 0;
        ioctlsocket( m_hSocket, FIONBIO, &mode );
        m_bIsSyncronous = false;
    }
}


//////////////////// ASYNCHRONOUS I/O FUNCTIONS /////////////////////////////

//---------------------------------------------------------------------------
/// This is the public facing interface for starting a new logical write
/// request.
//---------------------------------------------------------------------------
void SocketImpl::asyncWrite( void* pData, size_t numBytes ) {
    assert ( pData != NULL );
    assert( numBytes <= Socket::MAX_WRITE_SIZE );
    
    if ( m_bWriteInProgress ) {
        if ( bDebug ) dlog( "Oops, flooding client with data" );
        ++g_writePushBacks;
        return;
    }

    memcpy( m_activeWrite.data, pData, numBytes );
    m_activeWrite.bytesSent = 0;
    m_activeWrite.bytesToSend = numBytes;

    continueAsyncWrite( m_activeWrite );
}

//---------------------------------------------------------------------------
/// This is the internal function that actually calls WSASend()
//---------------------------------------------------------------------------
void SocketImpl::continueAsyncWrite( WriteRequest& writeRequest ) {
    assert( m_bIsConnected );
    assert( !m_bWriteInProgress );

    m_bWriteInProgress = true;   
    setSocketFlags( false );
    
    // set up the write request
    memset( &writeRequest.overlapped, 0, sizeof(WSAOVERLAPPED) );
    writeRequest.winsockBuf.len 
        = writeRequest.bytesToSend - writeRequest.bytesSent;   
    writeRequest.winsockBuf.buf 
        = writeRequest.data + writeRequest.bytesSent;
    // when using callback functions, hEvent is for application use
    writeRequest.overlapped.hEvent = this;

    DWORD bytesSent = 0;
    ++g_writeCalls;
    int rc = WSASend( m_hSocket, &writeRequest.winsockBuf, 
                      1, &bytesSent, NULL, 
                      &writeRequest.overlapped,
                      asyncWriteCompletionHandler );
    if ( rc ) {
        rc = WSAGetLastError();
        if ( rc == WSA_IO_PENDING ) {
            // false alarm, everything is fine.
            return;
        }
        else {
            cerr << "error in async write: " << rc << endl;
            assert( rc );
        }
    }
    else {  
        // Send was immediate. A completion callback will still be scheduled.
    }
}

//---------------------------------------------------------------------------
/// This is the public facing function for starting a logical read operation.
//---------------------------------------------------------------------------
void SocketImpl::asyncRead( void*, size_t numBytes ) {
    assert( m_bIsConnected );
    assert( numBytes <= Socket::MAX_READ_SIZE );
    if ( m_bReadInProgress ) {
        if ( bTrace ) tlog( "waiting for read" );
        return;
    }
    m_activeRead.bytesExpected = numBytes;
    m_activeRead.bytesRead = 0;
    continueAsyncRead( m_activeRead );
}


//---------------------------------------------------------------------------
/// This is the internal function that actually calls WSARecv().
//---------------------------------------------------------------------------
void SocketImpl::continueAsyncRead( ReadRequest& readRequest ) {
    assert( m_bIsConnected );
    assert( !m_bReadInProgress );
    if ( bTrace ) cout << "queue read for " << this << endl;
    ++g_readCalls;
    setSocketFlags( false );
    m_bReadInProgress = true;

    memset( &readRequest.overlapped, 0, sizeof(WSAOVERLAPPED) );
    readRequest.winsockBuf.len 
        = Socket::MAX_READ_SIZE - readRequest.bytesRead;
    readRequest.winsockBuf.buf 
        = (char*)(readRequest.data + readRequest.bytesRead);
    readRequest.overlapped.hEvent = this;
    DWORD bytesRead = 0;
    DWORD flags = 0;
    int rc = WSARecv( m_hSocket, &readRequest.winsockBuf, 1, 
                      &bytesRead, &flags, &readRequest.overlapped, 
                      asyncReadCompletionHandler );
    if ( rc ) {
        rc = WSAGetLastError();
        if ( rc == WSA_IO_PENDING ) {
            // false alarm, everything is fine.
            return;
        }
        else {
            cerr << "error in async read: " << rc << endl;
            close();            
        }
    }
    else {
        // Recv completed immediately. A completion callback will still be
        // scheduled.
    }
}


//---------------------------------------------------------------------------
/// Initiates another write operation if the entire buffer was not sent by
/// the op that just completed.
//---------------------------------------------------------------------------
void SocketImpl::notifyWriteComplete( size_t bytesSent ) {
    tlog( "write complete" );
    assert( bytesSent >= 0 );

    m_bWriteInProgress = false;
    g_bytesWritten += bytesSent;
    m_bytesSent += bytesSent;
    m_activeWrite.bytesSent += bytesSent;

    if ( m_activeWrite.bytesSent < m_activeWrite.bytesToSend 
            && m_bIsConnected ) {
        // start another write op
        continueAsyncWrite( m_activeWrite );
    }
    else {
        ++g_logicalWritesCompleted;
    }    
}


//---------------------------------------------------------------------------
/// Updates readRequest fields and call continueAsyncRead() if neccesary to 
/// get all of the requested data.
//---------------------------------------------------------------------------
void SocketImpl::notifyReadComplete( size_t bytesRead ) {
    m_bReadInProgress = false;
    m_activeRead.bytesRead += bytesRead;
    m_bytesRead += bytesRead;
    g_bytesRead += bytesRead;
    if ( bTrace ) cerr << "read " << m_activeRead.bytesRead << "/" 
                       << m_activeRead.bytesExpected << endl;
    if ( m_activeRead.bytesRead < m_activeRead.bytesExpected ) {     
        continueAsyncRead( m_activeRead );
    }
    else {
        ++g_logicalReadsCompleted;
        // read is actually complete.
    }
}


//---------------------------------------------------------------------------
/// Static function called by the OS when an aio write() request is done.
/// Notifies the associated Socket instance. 
//---------------------------------------------------------------------------
void CALLBACK SocketImpl::asyncWriteCompletionHandler( 
                            IN DWORD dwError,
                            IN DWORD cbTransferred,
                            IN LPWSAOVERLAPPED lpOverlapped,
                            IN DWORD ) {
    if ( !dwError ) {
        SocketImpl* sockImpl = (SocketImpl*)lpOverlapped->hEvent;
        sockImpl->notifyWriteComplete( cbTransferred );
    }
}


//---------------------------------------------------------------------------
/// Static function called by OS when aio read() completes. Notifies the
/// associated Socket instance.
//---------------------------------------------------------------------------
void SocketImpl::asyncReadCompletionHandler(
                            IN DWORD dwError,
                            IN DWORD cbTransferred,
                            IN LPWSAOVERLAPPED lpOverlapped,
                            IN DWORD ) {
    if ( !dwError ) {
        SocketImpl* sockImpl = (SocketImpl*)lpOverlapped->hEvent;    
        sockImpl->notifyReadComplete( cbTransferred );
    }
}

//////////////// SYNCHRONOUS NON-BLOCKING I/O FUNCTIONS /////////////////////

#ifdef SYNCH_IO_LEAN_AND_MEAN
//---------------------------------------------------------------------------
/// This implementation is the thinnest wrapper around the native send()
/// method possible. It provides less functionality than its asynchronous
/// equivalent, but is provided for benchmarking purposes.
//---------------------------------------------------------------------------
size_t SocketImpl::write( void* pData, size_t numBytes ) {
    ++g_writeCalls;
    int bytesSent 
        = ::send( m_hSocket, (char*)pData, numBytes, 0 );
    if ( bytesSent == SOCKET_ERROR ) {
        int error = WSAGetLastError();
        if ( error == WSAEWOULDBLOCK ) {
            ++g_writeRetries;
            bytesSent = 0;
        }
        else {
            // real error!
            assert( false );
        }
    }
    g_bytesWritten += bytesSent;
    m_bytesSent += bytesSent;

    return bytesSent;
}
#else
//---------------------------------------------------------------------------
/// This is the fully functional synchronous write function, which can be
/// used almost like asyncWrite(). The main difference is that on partial
/// writes, the caller must call write() again with pData = NULL to continue
/// the transfer.
//---------------------------------------------------------------------------
size_t SocketImpl::write( void* pData, size_t numBytes ) {
    assert( m_bIsConnected );
    assert( numBytes <= Socket::MAX_WRITE_SIZE );

    if ( m_bWriteInProgress && pData != NULL ) {
        dlog( "Oops, flooding client with data" );
        ++g_writePushBacks;
        return 0;
    }

    // check if this is a new request, or just a request to continue
    // sending an existing buffer...
    if ( pData != NULL ) {
        m_activeWrite.winsockBuf.buf = (char*)pData;
        m_activeWrite.winsockBuf.len = numBytes;
        m_activeWrite.bytesSent = 0;
    }
    m_bWriteInProgress = true;
    setSocketFlags( true );
    ++g_writeCalls;
    int bytesSent 
        = ::send( m_hSocket, 
                  m_activeWrite.winsockBuf.buf + m_activeWrite.bytesSent, 
                  m_activeWrite.winsockBuf.len - m_activeWrite.bytesSent, 
                  0 );
    if ( bytesSent == SOCKET_ERROR ) {
        int error = WSAGetLastError();
        if ( error == WSAEWOULDBLOCK ) {
            ++g_writeRetries;
        }
        else {
            // real error!
            assert( false );
        }
    }
    else {
        m_activeWrite.bytesSent += bytesSent;
        g_bytesWritten += bytesSent;
        m_bytesSent += bytesSent;
    }

    if ( m_activeWrite.bytesSent >= (int)m_activeWrite.winsockBuf.len ) {
        ++g_logicalWritesCompleted;
        m_bWriteInProgress = false;
    }

    return m_activeWrite.bytesSent;
}
#endif


#ifdef SYNCH_IO_LEAN_AND_MEAN
//---------------------------------------------------------------------------
/// This implementation is the thinnest wrapper around the native recv()
/// method possible. It provides less functionality than its asynchronous
/// equivalent, but is provided for benchmarking purposes.
//---------------------------------------------------------------------------
size_t SocketImpl::read( void* buffer, size_t maxBytes ) {
    ++g_readCalls;
    int bytesRead 
        = ::recv( m_hSocket, (char*)buffer, maxBytes, 0 );
    if ( bytesRead == 0 ) {
        // connection closed on the other end.
        close();
    }
    else if( bytesRead == SOCKET_ERROR ) {
        int error = WSAGetLastError();
        if ( error == WSAEWOULDBLOCK ) {
            ++g_readRetries;
            bytesRead = 0;
        }
        else {
            assert( false );
        }
    }
    m_bytesRead += bytesRead;
    g_bytesRead += bytesRead;

    return bytesRead;
}
#else
//---------------------------------------------------------------------------
/// This is the fully functional synchronous read function, which can be
/// used very similarly to asyncRead(). This implementation reads into an
/// internal buffer, so it doesn't actually matter what you pass in for
/// pData.
//---------------------------------------------------------------------------
size_t SocketImpl::read( void*, size_t maxBytes ) {
    assert( m_bIsConnected );
    assert( maxBytes <= Socket::MAX_READ_SIZE );
    
    if ( !m_bReadInProgress ) {
        m_activeRead.bytesExpected = maxBytes;
        m_activeRead.bytesRead = 0;
        m_bReadInProgress = true;
    }
    setSocketFlags( true );
    ++g_readCalls;
    int bytesRead 
        = ::recv( m_hSocket, 
                  m_activeRead.data + m_activeRead.bytesRead, 
                  m_activeRead.bytesExpected - m_activeRead.bytesRead, 
                  0 );
    if ( bytesRead == 0 ) {
        // connection closed on the other end.
        close();
    }
    else if( bytesRead == SOCKET_ERROR ) {
        int error = WSAGetLastError();
        if ( error == WSAEWOULDBLOCK ) {
            ++g_readRetries;
            bytesRead = 0;
        }
        else {
            assert( false );
        }
    }
    else {
        g_bytesRead += bytesRead;
        m_bytesRead += bytesRead;
        m_activeRead.bytesRead += bytesRead;
        if ( m_activeRead.bytesRead >= m_activeRead.bytesExpected ) {
            ++g_logicalReadsCompleted;
            m_bReadInProgress = false;
            // since all of the test code ignores the actual data, no
            // need to copy from m_activeRead to pData.
        }   
    }

    return m_activeRead.bytesRead;
}
#endif


///////////////////////////////// HELPERS ///////////////////////////////////

//---------------------------------------------------------------------------
/// Creates a socket connected to the remote host specified.
//---------------------------------------------------------------------------
SocketImpl::SocketHandle connectSocket( const string& host, 
                                        const string& port ) {
    SocketImpl::SocketHandle result;
    int rc;
    struct sockaddr_in sa; struct hostent *hp; 
    int portnum = atoi(port.c_str());

    hp = gethostbyname( host.c_str() );
    memset( &sa, 0, sizeof(sa) );
    memcpy( (char*)&sa.sin_addr, hp->h_addr_list, hp->h_length);
    sa.sin_family = hp->h_addrtype;
    sa.sin_port = htons((u_short)portnum);

    result = socket( hp->h_addrtype, SOCK_STREAM, 0 );
    if ( result != INVALID_SOCKET ) {
        rc = connect(result, (sockaddr*)&sa, sizeof sa );
        if ( rc == SOCKET_ERROR ) {
            // failed to connect
            closesocket( result );
            result = INVALID_SOCKET;
        }
    }

    return result;
}


//---------------------------------------------------------------------------
/// Creates a sockets and handles all of the setup surrounding a bind() call 
/// to specify the local port.
//---------------------------------------------------------------------------
SocketImpl::SocketHandle bindSocket( const string& port ) {
    SocketImpl::SocketHandle result;
    char myname[256]; 
    int rc;
    struct sockaddr_in sa; struct hostent *hp;    

    memset( &sa, 0, sizeof(sockaddr) );
    gethostname(myname,sizeof(myname) );
    hp = gethostbyname( myname );
    if ( hp == NULL ) return INVALID_SOCKET;

    sa.sin_family = hp->h_addrtype;
    sa.sin_port = htons( (short)atoi(port.c_str()) );
    result = socket( AF_INET, SOCK_STREAM, 0 );
    if ( result != -1 ) {
        rc = bind(result, (sockaddr*)&sa, sizeof(sockaddr_in));
        if ( rc == SOCKET_ERROR ) {
            perror("bind");
            // failed to connect
            closesocket( result );
            return INVALID_SOCKET;
        }
    }
    
    return result;
}
