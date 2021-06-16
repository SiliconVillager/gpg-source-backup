//---------------------------------------------------------------------------
/// \file SocketImpl.cpp
/// Definitions for POSIX version of SocketImpl
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

// application includes
#include "SocketImpl.h"
#include "../Socket.h"
#include "../debug.h"
// sockets
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <string.h>
#include <strings.h>
#include <stdlib.h>
// aio
#include <aio.h>
#include <errno.h>
#include <fcntl.h>
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


/// low level helper for making a socket connection
static SocketImpl::SocketHandle connectSocket( const string& host, 
                                               const string& port );

/// low level helper for making a listen socket
static SocketImpl::SocketHandle bindSocket( const string& port );

/// prints some extra diagnostics related to errno.
void reportAioError( int errorCode );


//---------------------------------------------------------------------------
// Initialize member variables to sane values.
//---------------------------------------------------------------------------
SocketImpl::SocketImpl()
    : m_hSocket(-1), m_bIsConnected(false), 
      m_bIsListening(false), m_bWriteInProgress(false), 
      m_bReadInProgress(false), m_bIsSynchronous(false),
      m_bytesRead(0), m_bytesSent(0) {
}


//---------------------------------------------------------------------------
// Initialize member variables to sane values.
//---------------------------------------------------------------------------
SocketImpl::SocketImpl( SocketHandle rawSocket ) 
    : m_hSocket(rawSocket), m_bIsConnected(rawSocket!=-1), 
      m_bIsListening(false), m_bWriteInProgress(false), 
      m_bReadInProgress(false), m_bIsSynchronous(false),
      m_bytesRead(0), m_bytesSent(0) {

}

//---------------------------------------------------------------------------
/// Calls close, which may block on active asynchronous I/O.
//---------------------------------------------------------------------------
SocketImpl::~SocketImpl() {
    if ( m_bIsConnected ) close();
}


//---------------------------------------------------------------------------
// See connectSocket() for implementation details.
//---------------------------------------------------------------------------
bool SocketImpl::connect( const string& host, const string& port ) {
    assert( !m_bIsConnected && !m_bIsListening );

    m_hSocket = connectSocket( host, port );
    m_bIsConnected = ( m_hSocket != -1 );

    return m_bIsConnected;
}


//---------------------------------------------------------------------------
/// Binds the socket to the port and starts listening.
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
    
    sockaddr_storage theirAddr;
    socklen_t addrSize = sizeof( theirAddr );
    SocketHandle clientFD 
        = ::accept( m_hSocket, (sockaddr*)&theirAddr, &addrSize );
    if ( clientFD == -1 ) return NULL;

    Socket::SocketPtr pClientSocket 
        = new Socket( new SocketImpl(clientFD) );
    return pClientSocket;
}


//---------------------------------------------------------------------------
/// In asynchronous mode, cancels outstanding AIO requests and waits for them
/// to complete before closing the connection.
//---------------------------------------------------------------------------
void SocketImpl::close() {
    dlog( "closing socket" );
    if ( !m_bIsSynchronous ) {
        aiocb* suspendList[1];
        if ( m_bReadInProgress ) {
            aio_cancel( m_hSocket, &m_activeRead.m_aiocb );
            suspendList[0] = &m_activeRead.m_aiocb;
            aio_suspend( suspendList, 1, NULL );
        }
        if ( m_bWriteInProgress ) {
            aio_cancel( m_hSocket, &m_activeWrite.m_aiocb );
            suspendList[0] = &m_activeWrite.m_aiocb;
            aio_suspend( suspendList, 1, NULL );
        }
    }

    ::close( m_hSocket );

    m_bIsConnected = false;
    m_bIsListening = false;
}


//---------------------------------------------------------------------------
// If m_bIsSyncronous doesn't match bSyncronous, set the appropriate options
// on the socket for the specified mode.
//---------------------------------------------------------------------------
void SocketImpl::setSocketFlags( bool bSyncronous ) {
    if ( bSyncronous && !m_bIsSynchronous ) {
        fcntl( m_hSocket, F_SETFL, O_NONBLOCK );
        m_bIsSynchronous = true;
    }
    else if ( !bSyncronous && m_bIsSynchronous ) {
        int flags = fcntl( m_hSocket, F_GETFL );
        fcntl( m_hSocket, F_SETFL, flags & ~O_NONBLOCK );
        m_bIsSynchronous = false;
    }
}


//////////////////// ASYNCHRONOUS I/O FUNCTIONS /////////////////////////////

//---------------------------------------------------------------------------
/// This is the public facing interface for starting a new logical write
/// request.
//---------------------------------------------------------------------------
void SocketImpl::asyncWrite( void* pData, size_t numBytes ) {
    assert ( pData != NULL );
    assert( numBytes <= MAX_WRITE_SIZE );

    if ( m_bWriteInProgress ) {
        if ( bDebug ) dlog( "Oops, flooding client with data" );
        ++g_writePushBacks;;
        return;
    }
    if ( bTrace ) cerr << "queue write for " << this << endl;

    // set up the write request
    memcpy( m_activeWrite.m_pData, pData, numBytes );
    m_activeWrite.bytesSent   = 0;
    m_activeWrite.bytesToSend = numBytes;
    
    continueAsyncWrite( m_activeWrite );
}


//---------------------------------------------------------------------------
/// This is the internal function that actually calls aio_write.
//---------------------------------------------------------------------------
void SocketImpl::continueAsyncWrite( WriteRequest& writeRequest ) {
    assert( m_bIsConnected );
    assert( !m_bWriteInProgress );

    m_bWriteInProgress = true;
    setSocketFlags( false );

    // configure the aiocb...
    // good practice to zero out the aiocb so that we start with sane values
    aiocb& writeAiocb = writeRequest.m_aiocb;
    bzero( (char*)&writeAiocb, sizeof(aiocb) );
    writeAiocb.aio_fildes = m_hSocket;
    writeAiocb.aio_nbytes = writeRequest.bytesToSend 
                               - writeRequest.bytesSent;
    writeAiocb.aio_offset = 0;
    writeAiocb.aio_buf    = writeRequest.m_pData 
                                + writeRequest.bytesSent;
    // set up the callback
    writeRequest.m_pSocketImpl = this;
    sigevent& writeSigEvent = writeAiocb.aio_sigevent;
    writeSigEvent.sigev_notify            = SIGEV_THREAD;
    writeSigEvent.sigev_notify_function   = asyncWriteCompletionHandler;
    writeSigEvent.sigev_notify_attributes = NULL;
    writeSigEvent.sigev_value.sival_ptr   = &writeRequest;

    // be aware, this write could immediately call the callback
    ++g_writeCalls;
    int rc = aio_write( &writeAiocb );
    if ( rc == -1 ) {        
        perror("aio_write");
    }
}


//---------------------------------------------------------------------------
/// This is the public facing function for starting a logical read operation.
//---------------------------------------------------------------------------
void SocketImpl::asyncRead( void* pData, size_t numBytes ) {
    assert( m_bIsConnected );
    assert( numBytes <= MAX_READ_SIZE );
    if ( m_bReadInProgress ) {
        if ( bTrace ) dlog( "read in progress" );
        return;
    }
    m_activeRead.bytesExpected = numBytes;
    m_activeRead.bytesRead = 0;
    continueAsyncRead( m_activeRead );
}


//---------------------------------------------------------------------------
/// This is the internal function that actually calls aio_read().
//---------------------------------------------------------------------------
void SocketImpl::continueAsyncRead( ReadRequest& readRequest ) {
    assert( m_bIsConnected );
    assert( !m_bReadInProgress );
    if ( bDebug ) cerr << "queue read for " << this << endl;
    ++g_readCalls;
    setSocketFlags( false );
    m_bReadInProgress = true;

    aiocb& readAiocb = readRequest.m_aiocb;    
    // good practice to zero out the aiocb so that we start with sane values
    bzero( (char*)&readAiocb, sizeof(aiocb) );

    // configure the read request...
    readAiocb.aio_fildes = m_hSocket;
    readAiocb.aio_nbytes = readRequest.bytesExpected - readRequest.bytesRead;
    readAiocb.aio_buf    = m_activeRead.m_pBuffer + readRequest.bytesRead;
    // set up the callback
    m_activeRead.m_pSocketImpl = this;
    sigevent& readSigEvent = readAiocb.aio_sigevent;
    readSigEvent.sigev_notify            = SIGEV_THREAD;
    readSigEvent.sigev_notify_function   = asyncReadCompletionHandler;
    readSigEvent.sigev_notify_attributes = NULL;
    readSigEvent.sigev_value.sival_ptr   = &m_activeRead;

    // be aware, the read could immediately call the callback
    int rc = aio_read( &readAiocb );
    if ( rc == -1 ) {
        perror("aio_read");
        close();
    }
}


//---------------------------------------------------------------------------
/// May initiate another asynchronous write if the expected number of bytes
/// were not sent the the operation that just finished.
//---------------------------------------------------------------------------
void SocketImpl::notifyWriteComplete( WriteRequest& writeRequest,
                                      size_t bytesSent ) {
    tlog( "write complete" );
    assert( bytesSent >= 0 );

    m_bWriteInProgress = false;
    g_bytesWritten += bytesSent;
    m_bytesSent    += bytesSent;
    writeRequest.bytesSent += bytesSent;

    if ( writeRequest.bytesSent < writeRequest.bytesToSend 
             && m_bIsConnected ) {
        // start another write op for the remainder...
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
void SocketImpl::notifyReadComplete( ReadRequest& readRequest, 
                                     size_t bytesRead ) {
    m_bReadInProgress = false;
    m_bytesRead += bytesRead;
    g_bytesRead += bytesRead;
    readRequest.bytesRead += bytesRead;
    if ( readRequest.bytesRead < readRequest.bytesExpected 
             && m_bIsConnected ) {
        if ( bTrace ) {
            cerr << "continuing read " << readRequest.bytesRead << "/" 
                 << readRequest.bytesExpected << endl;
        }
        // make another read request for the remainder
        continueAsyncRead( readRequest );
    }
    else {
        ++g_logicalReadsCompleted;
        if ( bTrace ) {
            readRequest.m_pBuffer[readRequest.bytesExpected + 1] = '\0';
            cerr << "read complete [" << readRequest.m_pBuffer 
                 << "]" << endl;
        }
    }
}


//---------------------------------------------------------------------------
// Static function called by the OS when an aio_write() request is done.
// Notifies the associated Socket instance. 
//---------------------------------------------------------------------------
void SocketImpl::asyncWriteCompletionHandler( sigval sigval ) {
    WriteRequest* pWriteReq = (WriteRequest*)sigval.sival_ptr;
    int bytesSent = aio_return( &pWriteReq->m_aiocb );
    if ( bytesSent < 0 ) {
        // error!
        if ( pWriteReq->m_pSocketImpl 
                 && pWriteReq->m_pSocketImpl->m_bIsConnected ) {
            int rc = aio_error( &pWriteReq->m_aiocb );
            reportAioError( rc );
            assert( false );
        }
    }
    else {
        if ( bTrace ) cerr << "notifying " << pWriteReq->m_pSocketImpl 
                           << endl;
        pWriteReq->m_pSocketImpl->notifyWriteComplete( *pWriteReq,
                                                       bytesSent );
    }

    return;
}


//---------------------------------------------------------------------------
// Static function called by OS when aio_read() completes. Notifies the
// associated Socket instance.
//---------------------------------------------------------------------------
void SocketImpl::asyncReadCompletionHandler( sigval sigval ) {
    ReadRequest* pReadReq = (ReadRequest*)sigval.sival_ptr;
    assert( pReadReq != NULL );

    int rc = aio_error(&pReadReq->m_aiocb);
    if ( rc == ECANCELED ) {
        // bail out, don't notify the caller
        return;
    }
    else if ( rc == ECONNRESET ) {
        // connection lost
        return;
    }
    else if ( rc != 0 ) {
        perror( "aio_read error" );
        assert( false );
    }
    else {
        int bytesRead = aio_return( &pReadReq->m_aiocb );
        assert( bytesRead > 0 );
        if ( bTrace ) cout << "notifying " << pReadReq->m_pSocketImpl 
                           << endl;
        assert( pReadReq->m_pSocketImpl != NULL );
        pReadReq->m_pSocketImpl->notifyReadComplete( *pReadReq, bytesRead );
    }
    
    return;
}


//////////////// SYNCHRONOUS NON-BLOCKING I/O FUNCTIONS /////////////////////

#ifndef SYNCH_IO_LEAN_AND_MEAN
//---------------------------------------------------------------------------
/// This is the fully functional synchronous write function, which can be
/// used almost like asyncWrite(). The main difference is that on partial
/// writes, the caller must call write() again with pData = NULL to continue
/// the transfer.
//---------------------------------------------------------------------------
size_t SocketImpl::write( void* pData, size_t numBytes ) {
    assert( m_bIsConnected );
    assert( numBytes <= MAX_WRITE_SIZE );

    if ( m_bWriteInProgress && pData != NULL ) {
        if ( bDebug ) dlog( "Oops, flooding client with data" );
        ++g_writePushBacks;
        return 0;
    }
    
    // check if we're starting a new logical write op...
    if ( pData != NULL ) {
        m_activeWrite.bytesToSend = numBytes;
        m_activeWrite.bytesSent = 0;
        // this could be optimized out by requiring the caller to be
        // careful with pData, however asyncWrite() has a memcpy() too,
        // so using it here puts both implementations on equal footing.
        memcpy( m_activeWrite.m_pData, pData, numBytes );
    }
    // otherwise, we're continuing to send whatever is in the active write

    m_bWriteInProgress = true;
    setSocketFlags( true );
    ++g_writeCalls;
    int bytesSent 
        = ::send( m_hSocket, 
                  m_activeWrite.m_pData + m_activeWrite.bytesSent,
                  m_activeWrite.bytesToSend - m_activeWrite.bytesSent,
                  0 );
    if ( bytesSent == -1 ) {
        if ( errno == EWOULDBLOCK ) {
            ++g_writeRetries;
        }
        else {
            // oops, real error!
            assert( false );
        }
    }
    else {
        m_activeWrite.bytesSent += bytesSent;
        g_bytesWritten += bytesSent;
        m_bytesSent += bytesSent;
    }

    if ( m_activeWrite.bytesSent >= m_activeWrite.bytesToSend ) {
        ++g_logicalWritesCompleted;
        m_bWriteInProgress = false;
    }

    return m_activeWrite.bytesSent;
}
#else 
//---------------------------------------------------------------------------
/// This is a minimalist wrapper around the native send function. It has less
/// functionality that asyncWrite(), but is provided for benchmarking.
//---------------------------------------------------------------------------
size_t SocketImpl::write( void* pData, size_t numBytes ) {
    ++g_writeCalls;
    int bytesSent
        = ::send( m_hSocket, (char*)pData, numBytes, 0 );
    if ( bytesSent < 0 ) {
        if ( errno == EWOULDBLOCK ) {
            ++g_writeRetries;
            bytesSent = 0;
        }
        else {
            // oops, real error!
            assert( false );
        }
    }
    g_bytesWritten += bytesSent;
    m_bytesSent += bytesSent;

    return bytesSent;
}
#endif


#ifndef SYNCH_IO_LEAN_AND_MEAN
//---------------------------------------------------------------------------
/// This is the fully functional synchronous read function, which can be
/// used very similarly to asyncRead(). This implementation reads into an
/// internal buffer, so it doesn't actually matter what you pass in for
/// pData.
//---------------------------------------------------------------------------
size_t SocketImpl::read( void* pData, size_t maxBytes ) {
    assert( m_bIsConnected );
    assert( maxBytes <= MAX_READ_SIZE );

    if ( !m_bReadInProgress ) {
        m_activeRead.bytesExpected = maxBytes;
        m_activeRead.bytesRead = 0;
        m_bReadInProgress = true;
    }
    setSocketFlags( true );
    ++g_readCalls;
    int bytesRead 
        = ::recv( m_hSocket, 
                  m_activeRead.m_pBuffer + m_activeRead.bytesRead, 
                  m_activeRead.bytesExpected - m_activeRead.bytesRead,
                  0 );
    if ( bytesRead == 0 ) {
        // connection was closed by the other end
        close();
    }
    else if ( bytesRead < 0 ) {
        if ( errno == EWOULDBLOCK ) {
            ++g_readRetries;
            bytesRead = 0;
        }
        else {
            // oops, real error.
            assert( false );
        }
    }
    else {
        // read successful!
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
#else
//---------------------------------------------------------------------------
/// This is a minimalist wrapper around the native OS recv function. It is
/// not functional enough to use in place of asyncRead(), but is provided
/// for benchmarking purposes.
//---------------------------------------------------------------------------
size_t SocketImpl::read( void* buffer, size_t maxBytes ) {
    ++g_readCalls;
    int bytesRead
        = ::recv( m_hSocket, (char*)buffer, maxBytes, 0 );
    if ( bytesRead == 0 ) {
        // connection closed on other end
        close();
    }
    else if ( bytesRead < 0 ) {
        if ( errno == EWOULDBLOCK ) {
            ++g_readRetries;
            bytesRead = 0;
        }
        else {
            // oops! a read error
            assert( false );
        }
    }
    m_bytesRead += bytesRead;
    g_bytesRead += bytesRead;

    return bytesRead;
}
#endif


/////////////////////////////////// HELPERS /////////////////////////////////

//---------------------------------------------------------------------------
/// Creates a socket connected to the remote host specified.
//---------------------------------------------------------------------------
SocketImpl::SocketHandle connectSocket(const string& host, const string& port) {
    addrinfo hints, *res;
    SocketImpl::SocketHandle result;
    int rc;
    
    memset(&hints, 0, sizeof hints);
    hints.ai_family   = AF_INET;
    hints.ai_socktype = SOCK_STREAM;

    rc = getaddrinfo(host.c_str(), port.c_str(), &hints, &res);
    assert( rc == 0 );
    
    result = socket(res->ai_family, res->ai_socktype, res->ai_protocol);
    if ( result != -1 ) {
        rc = connect(result, res->ai_addr, res->ai_addrlen);
        if ( rc == -1 ) {
            // failed to connect
            close( result );
            result = -1;
        }
    }
    
    return result;
}


//---------------------------------------------------------------------------
/// Creates a sockets and handles all of the setup surrounding a bind() call 
/// to specify the local port.
//---------------------------------------------------------------------------
SocketImpl::SocketHandle bindSocket( const string& port ) {
    addrinfo hints, *res;
    SocketImpl::SocketHandle result;
    int rc;
    
    memset(&hints, 0, sizeof hints);
    hints.ai_family   = AF_INET;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags    = AI_PASSIVE;

    rc = getaddrinfo(NULL, port.c_str(), &hints, &res);
    assert( rc == 0 );
    
    result = socket(res->ai_family, res->ai_socktype, res->ai_protocol);
    if ( result != -1 ) {
        char yes = 1;
        if ( setsockopt(result, SOL_SOCKET, SO_REUSEADDR, 
                        &yes, sizeof(int) ) == -1) {
            perror("setsockopt");
            return -1;
        } 

        rc = bind(result, res->ai_addr, res->ai_addrlen);
        if ( rc == -1 ) {
            perror("bind");
            // failed to connect
            close( result );
            return -1;
        }
    }
    
    return result;
}


//---------------------------------------------------------------------------
/// Given an error code, prints an error message to stdout. Can be useful
/// for debugging, especially if you want a breakpoint on certain errors.
//---------------------------------------------------------------------------
void reportAioError( int errorCode ) {
    switch ( errorCode ) {
    case ENOSYS:
        cout << "ENOSYS - aio operation not supported" << endl;
        break;
    case EAGAIN:
        cout << "EAGAIN - insufficient resources" << endl;
        break;
    case EBADF:
        cout << "EBADF - bad file handle on write" << endl;
        break;
    case EINVAL:
        cout << "EINVAL - bad parameter value on write" << endl;
        break;
    default:
        cout << "unknown aio error" << endl;
        break;
    }
}
