//---------------------------------------------------------------------------
/// \file SocketImpl.h
/// Declarations for Winsock2 version of SocketImpl class
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
#ifndef GPG8_SOCKET_IMPL_WIN32_H
#define GPG8_SOCKET_IMPL_WIN32_H 1

// application headers
#include "../Socket.h"
// Winsock
#include <winsock2.h>

//---------------------------------------------------------------------------
/// This is the Winsock2 version of SocketImpl.
//---------------------------------------------------------------------------
class SocketImpl
{
public:
    typedef SOCKET SocketHandle;

    /// A write request holds the control struct (WSAOVERLAPPED for Winsock2)
    /// and related bits of data required by either the synchronous or 
    /// asynchronous write functions.
    struct WriteRequest {
        char data[Socket::MAX_WRITE_SIZE];
        WSAOVERLAPPED overlapped;
        WSABUF winsockBuf;
        int bytesSent;
        int bytesToSend;
    };

    /// A read request holds the control struct (WSAOVERLAPPED for Winsock2)
    /// and related bits of data required by either the synchronous or
    /// asynchronous read functions.
    struct ReadRequest {
        char data[Socket::MAX_READ_SIZE];
        WSAOVERLAPPED overlapped;
        WSABUF winsockBuf;
        int bytesExpected;
        int bytesRead;
    };

    /// Default constructor
    SocketImpl(void);

    /// Destructor
    ~SocketImpl(void);

    /// See Socket::connect()
    bool connect( const std::string& host, const std::string& port );

    /// See Socket::listen()
    bool listen( const std::string& port );

    /// See Socket::accept()
    Socket::SocketPtr accept();

    /// See Socket::close()
    void close();

    /// See Socket::write()
    size_t write( void* pData, size_t numBytes );

    /// See Socket::asyncWrite()
    void asyncWrite( void* pData, size_t numBytes );

    /// See Socket::read()
    size_t read( void* pData, size_t maxBytes );

    /// See Socket::asyncRead()
    void asyncRead( void* pData, size_t numBytes );

    //-----------------------------------------------------------------------
    /// For this callback to happen, our app must explicitly yield to the
    /// OS, with something like SleepEx()
    //-----------------------------------------------------------------------
    static void CALLBACK asyncReadCompletionHandler(
        IN DWORD dwError,
        IN DWORD cbTransferred,
        IN LPWSAOVERLAPPED lpOverlapped,
        IN DWORD dwFlags );

    //-----------------------------------------------------------------------
    /// For this callback to happen, our app must explicitly yield to the
    /// OS, with something like SleepEx()
    //-----------------------------------------------------------------------
    static void CALLBACK asyncWriteCompletionHandler(
        IN DWORD dwError,
        IN DWORD cbTransferred,
        IN LPWSAOVERLAPPED lpOverlapped,
        IN DWORD dwFlags );

private:
    /// On completion of a write operation, this function updates metrics
    /// and if the write was not complete (the expected number of bytes
    /// was not sent), it initiates another write request on the remaining
    /// data.
    void notifyWriteComplete( size_t bytesSent );

    /// On completion of a read operation, this function updates metrics and
    /// if the read was not complete (the expected number of bytes were
    /// not received), it initiates another read request for the remaining
    /// data.
    void notifyReadComplete( size_t bytesRead );

    /// This is an internal constructor, used in accept() to create a new
    /// SocketImpl given a new client socket.
    explicit SocketImpl( SocketHandle rawSocket );

    /// This function assumes that the readRequest has been configured with
    /// the right bytesExpected/Read values. It then configures the WSABUF
    /// and WSAOVERLAPPED struct and makes the actual overlapped I/O call.
    void continueAsyncRead( ReadRequest& readRequest );

    /// This function assumes that the writeRequest has been configured with
    /// the right bytesToSend/Sent values. It then configures the WSABUF
    /// and WSAOVERLAPPED struct and makes the actual overlapped I/O call.
    void continueAsyncWrite( WriteRequest& writeRequest );

    /// Sets socket flags if bSynchronous does match the current state of
    /// the socket.
    inline void setSocketFlags( bool bSyncronous );

    /// The socket handle.
    SocketHandle m_hSocket;

    /// Data for the current write operation
    WriteRequest m_activeWrite;
    /// Data for the current read operation
    ReadRequest  m_activeRead;
    
    // Various socket state flags.
    bool m_bIsConnected;
    bool m_bIsListening;
    bool m_bWriteInProgress;
    bool m_bReadInProgress;
    bool m_bIsSyncronous;

    // Instance specific stats...
    int m_bytesRead;
    int m_bytesSent;

    // Global read stats...
    static int g_readCalls;    
    static int g_logicalReadsCompleted;
    static int g_readRetries;
    static int g_bytesRead;

    // Global write stats...
    static int g_writeCalls;
    static int g_logicalWritesCompleted;
    static int g_writeRetries;
    static int g_bytesWritten;
    static int g_writePushBacks;

    // Some aspects of the implementation are simplified by having a close
    // relationship with the interface class.
    friend class Socket;
};

#endif
