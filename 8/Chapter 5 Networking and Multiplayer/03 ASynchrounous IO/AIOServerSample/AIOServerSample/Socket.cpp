//---------------------------------------------------------------------------
/// \file Socket.cpp
/// Definitions for Socket class.
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

// application
#include "Socket.h"
#include "SocketImpl.h"
#include "debug.h"
// c++
#include <cassert>
#include <iostream>
#include <algorithm>


using namespace std;
using gpg8_util::bDebug;


//---------------------------------------------------------------------------
// default constructor
//---------------------------------------------------------------------------
Socket::Socket() {
    m_pImpl = new SocketImpl();
}


//---------------------------------------------------------------------------
// create from an existing SocketImpl
//---------------------------------------------------------------------------
Socket::Socket( SocketImpl* pSocketImpl ) {
    m_pImpl = pSocketImpl;
}


//---------------------------------------------------------------------------
// Destructor
//---------------------------------------------------------------------------
Socket::~Socket() {
    delete m_pImpl;
    m_pImpl = NULL;
}


//---------------------------------------------------------------------------
// Prints a variety of global socket stats to stdout.
//---------------------------------------------------------------------------
void Socket::reportStats( double workingTime ) {
    cout << "\nTotal read calls     : " << SocketImpl::g_readCalls
         << "\nTotal write calls    : " << SocketImpl::g_writeCalls
         << "\nTotal write pushbacks: " << SocketImpl::g_writePushBacks
         << "\nTotal data read      : " 
         << std::fixed << SocketImpl::g_bytesRead / (1000 * 1000.0) 
         << " MB"
         << "\nTotal data written   : " 
         << std::fixed << SocketImpl::g_bytesWritten / (1000 * 1000.0) 
         << " MB"
         << "\nExecution throughput : " 
         << ( (SocketImpl::g_bytesWritten + SocketImpl::g_bytesRead)
                   / (1000 * 1000) ) / workingTime
         << " MB/s\n"
         << "\nLogical read-op pct  : " 
         << 100 * ( SocketImpl::g_logicalReadsCompleted 
                       / (double)(SocketImpl::g_readCalls 
                                      - SocketImpl::g_readRetries) )
         << "\nAvg bytes per read   : "
         << (int)( SocketImpl::g_bytesRead 
                       / (double)(SocketImpl::g_readCalls 
                                      - SocketImpl::g_readRetries) )
         << "\nLogical write-op pct : " 
         << 100 * ( SocketImpl::g_logicalWritesCompleted 
                       / (double)(SocketImpl::g_writeCalls 
                                      - SocketImpl::g_writeRetries) )
         << "\nAvg bytes per write  : "
         << (int)( SocketImpl::g_bytesWritten 
                       / (double)(SocketImpl::g_writeCalls 
                                      - SocketImpl::g_writeRetries) )
         << "\nRead poll pct        : " 
         << (SocketImpl::g_readRetries / (double)SocketImpl::g_readCalls)
            * 100
         << "\nWrite poll pct       : " 
         << (SocketImpl::g_writeRetries / (double)SocketImpl::g_writeCalls)
            * 100
         << endl;

    cout << "\nNOTES:\n"
         << "  - Running the test client on the same machine as the server\n"
         << "    can noticeably distort the results.\n"
         << "  - Read/write calls count the calls to OS I/O functions.\n"
         << "  - Write pushbacks are I/O calls that failed because a prior\n"
         << "    send operation was still in progress.\n"
         << "  - Execution throughput is average amount of data transfered\n"
         << "    per second of CPU time consumed.\n"
         << "  - Logical ops are complete transfers of the expected amount\n"
         << "    of data, which can require more than one OS call.\n"
         << "  - Poll pct is the percentage of OS calls that returned with\n"
         << "    would-block status.\n"
         << endl;
}


///////////////////////// PIMPL METHODS /////////////////////////////////////

// these are simple wrapper functions that delegate to the SocketImpl.

bool Socket::connect( const std::string& host, const std::string& port ) {
    return m_pImpl->connect( host, port );
}

bool Socket::listen( const std::string& port ) {
    return m_pImpl->listen( port );
}

Socket::SocketPtr Socket::accept() {
    return m_pImpl->accept();
}

void Socket::close() {
    m_pImpl->close();
}

size_t Socket::write( void* pData, size_t numBytes ) {
    return m_pImpl->write( pData, numBytes );
}

/// This could be made more efficient by checking m_bWriteInProgress to 
/// early out, but arguably that gives an unfair advantage to the async
/// implementation.
void Socket::asyncWrite( void* pData, size_t numBytes ) {
    m_pImpl->asyncWrite( pData, numBytes );
}

bool Socket::isWriting() {
    return m_pImpl->m_bWriteInProgress;
}

size_t Socket::read( void* pData, size_t maxBytes ) {
    return m_pImpl->read( pData, maxBytes );
}

/// See note in asyncWrite() about early out, which also applies here with
/// m_bReadInProgress.
void Socket::asyncRead( void* pData, size_t numBytes ) {
    m_pImpl->asyncRead( pData, numBytes );
}

bool Socket::isConnected() {
    return m_pImpl->m_bIsConnected;
}

bool Socket::isListening() {
    return m_pImpl->m_bIsListening;
}

int Socket::getBytesRead() {
    return m_pImpl->m_bytesRead;
}

int Socket::getBytesSent() {
    return m_pImpl->m_bytesSent;
}

int Socket::getRawHandle() {
    return (int)m_pImpl->m_hSocket; 
}
