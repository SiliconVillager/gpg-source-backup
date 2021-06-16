//---------------------------------------------------------------------------
/// \file Socket.h
/// Declarations for Socket class.
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
#ifndef GPG8_SOCKET_H
#define GPG8_SOCKET_H 1

#include <string>

/// This is the Impl of the pImpl pattern. It must be forward declared here
/// to prevent a dependency on SocketImpl.h, which would break the 
/// encapsulation.
class SocketImpl;


//---------------------------------------------------------------------------
/// Socket is a wrapper class for the platform's socket API, which also
/// provides some higher-level functionality like managing partial sends and
/// receives. This class provides access to both asynchronous and
/// non-blocking synchronous send and receive functions. Although you may be
/// able to switch back and forth between these methods on a single socket,
/// the intended usage is really to use one method or the other for the
/// lifetime of the Socket object. 
//---------------------------------------------------------------------------
class Socket {
public:
    /// Typedef-ed so that this could be upgraded to a smart pointer class
    /// in the future.
    typedef Socket* SocketPtr;

    // This class manages buffers for send and receive internally, with
    // fixed maximum sizes.
    static const size_t MAX_WRITE_SIZE = 32 * 1024;
    static const size_t MAX_READ_SIZE  = 8 * 1024;

    //-----------------------------------------------------------------------
    /// Creates an unconnected socket instance.
    //-----------------------------------------------------------------------
    Socket();

    //-----------------------------------------------------------------------
    /// Cleanup includes disconnection, if this has not already been done
    /// with the close() function.
    //-----------------------------------------------------------------------
    ~Socket();

    //-----------------------------------------------------------------------
    /// Initiates a connection to the remote host on the given port.
    //-----------------------------------------------------------------------
    bool connect( const std::string& host, const std::string& port );

    //-----------------------------------------------------------------------
    /// Binds this socket to a particular port on the local end, suitable
    /// for accepting incoming client connections.
    //-----------------------------------------------------------------------
    bool listen( const std::string& port );

    //-----------------------------------------------------------------------
    /// Blocks until an incoming client connection is made. Returns a 
    /// new socket instance for the client connection. It is the caller's 
    /// responsibility to delete the socket when they are done with it, 
    /// since we're not using smart pointers.
    //-----------------------------------------------------------------------
    SocketPtr accept();

    //-----------------------------------------------------------------------
    /// Terminates the connection, possibly blocking until any outstanding
    /// asynchronous I/O operations are completed or cancelled.
    //-----------------------------------------------------------------------
    void close();

    //-----------------------------------------------------------------------
    /// Attempts to send numBytes of the data pointed to by pData using
    /// synchronous non-blocking I/O. Returns the total number of bytes
    /// transferred, which may be less than numBytes in the case of a 
    /// partial write or if the socket would block. In these cases calling
    /// write() again with pData = NULL will attempt to send the rest of 
    /// the data. Calling write() again with pData != NULL will be a no-op
    /// and is recorded as a "push-back", i.e. the caller is trying to 
    /// send new data before the old data could be completely sent.
    ///
    /// Since this prototype code, it will assert() on any unexpected
    /// socket errors.
    //-----------------------------------------------------------------------
    size_t write( void* pData, size_t numBytes );

    //-----------------------------------------------------------------------
    /// Attempts to send numBytes of the data in pData using asynchronous
    /// I/O. On partial write, the underlying SocketImpl will automatically
    /// initiate additional write operations to complete the transfer.
    /// Calling asyncWrite() while another write operation is still in 
    /// progress will be a no-op, and is recorded as a push-back (see
    /// docs for synchronous write() for details about push-backs).
    ///
    /// Since this prototype code, it will assert() on any unexpected
    /// socket errors.
    //-----------------------------------------------------------------------
    void asyncWrite( void* pData, size_t numBytes );

    //-----------------------------------------------------------------------
    /// Returns true is there is currently an asynchronous write operation 
    /// in progress, or an incomplete synchronous write.
    //-----------------------------------------------------------------------
    bool isWriting();

    //-----------------------------------------------------------------------
    /// Attempts to receive numBytes using synchronous non-blocking I/O. If 
    /// less than numBytes are received, the read request remains active, 
    /// and future calls will resume receiving the remaining data. Returns 
    /// the total number of bytes received so far for the currently active
    /// request.
    ///
    /// Since the model server doesn't actually use the data after it is
    /// received, all transfers use an internal buffer in the SocketImpl. 
    /// A complete implementation would of course receive into pData.
    ///
    /// Since this prototype code, it will assert() on any unexpected
    /// socket errors.
    //-----------------------------------------------------------------------
    size_t read( void* pData, size_t numBytes );

    //-----------------------------------------------------------------------
    /// Attempts to receive numBytes using asynchronous I/O. As in the 
    /// synchronous read() function, the data is transferred into an 
    /// internal buffer, since the model server doesn't actually use the
    /// data after it arrives. In the case of partial reads, the SocketImpl
    /// will initiate additional asynchronous read requests until the full
    /// numBytes of data is received. Calls to asyncRead() while a read
    /// operation is active are no-ops.
    ///
    /// Since this prototype code, it will assert() on any unexpected
    /// socket errors.
    //-----------------------------------------------------------------------
    void asyncRead( void* pData, size_t numBytes );

    //-----------------------------------------------------------------------
    /// Returns true if this socket is currently connected to the remote
    /// socket, false otherwise.
    //-----------------------------------------------------------------------
    bool isConnected();

    //-----------------------------------------------------------------------
    /// Returns true if this socket is listening for new client connections,
    /// i.e. if accept() can be called successfully. Otherwise, returns 
    /// false.
    //-----------------------------------------------------------------------
    bool isListening();

    //-----------------------------------------------------------------------
    /// Dumps global stats about socket performance to stdout, given the
    /// total amount of CPU time measured by the application that was
    /// related to I/O.
    //-----------------------------------------------------------------------
    static void reportStats( double workingTime );

    //-----------------------------------------------------------------------
    /// Returns the total number of bytes that have been successfully 
    /// received by this socket since it was created.
    //-----------------------------------------------------------------------
    int getBytesRead();

    //-----------------------------------------------------------------------
    /// Returns the total number of bytes that have been successfully sent
    /// over this socket since it was created.
    //-----------------------------------------------------------------------
    int getBytesSent();

    //-----------------------------------------------------------------------
    /// This is a hack that exposes the underlying file descriptor or
    /// handle for the connection. Use with caution.
    //-----------------------------------------------------------------------
    int getRawHandle();

private:
    /// This constructor is used internally to create new client sockets in
    /// accept()
    explicit Socket( SocketImpl* pSocketImpl );

    /// Some aspects of the implementation can be streamlined by having
    /// access to the internals of the SocketImpl.
    friend class SocketImpl;

    /// Pointer to the SocketImpl, which contains all of the real 
    /// functionality.
    SocketImpl* m_pImpl;
};

#endif
