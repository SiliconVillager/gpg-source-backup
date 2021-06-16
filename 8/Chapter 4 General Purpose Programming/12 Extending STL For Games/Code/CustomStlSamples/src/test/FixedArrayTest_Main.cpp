//---------------------------------------------------------------------------
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
#include "FixedArray.hpp"
#include <cassert>

#ifdef _MSC_VER
#include <crtdbg.h>
#endif 

//---------------------------------------------------------------------------
/// A simple class for populating a test instance of FixedArray.
//---------------------------------------------------------------------------
struct TestObject {
    int x;
    static int g_destructorCalls;

    /// Default constructor
    TestObject() : x(-1) {}
    
    /// Constructor that specifies the X value
    explicit TestObject( int initX ) : x(initX) {}
    TestObject( const TestObject& rhs ) {
        x = rhs.x;
    }
    
    /// Desructor, instrumented for testing
    ~TestObject() {
        x = -1;
        ++g_destructorCalls;
    }
    
    /// Assignment
    TestObject& operator=( const TestObject& rhs ) {
        if ( this == &rhs ) return *this;
        x = rhs.x;
        return *this;
    }
    
    /// Value comparison
    bool operator==(const TestObject& rhs ) const {
        return x == rhs.x;
    }
    
    bool operator!=(const TestObject& rhs ) const {
        return !(*this == rhs);
    }
};

// Define and initialize TestObject's global variable for tracking destructor
// calls from outside of the class.
int TestObject::g_destructorCalls = 0;


//---------------------------------------------------------------------------
/// Program entry point. Exercises the core functionality of the FixedArray 
/// template class.
//---------------------------------------------------------------------------
int main( void ) {
#ifdef _MSC_VER
    _CrtSetDbgFlag( _CRTDBG_ALLOC_MEM_DF | _CRTDBG_LEAK_CHECK_DF );
#endif

    using namespace gpg8_util;
    const unsigned int MAX_ELEMENTS = 5;
    typedef FixedArray<TestObject, MAX_ELEMENTS> TestArrayA;

    // starts empty
    TestArrayA a;
    assert( a.size() == 0 );
    assert( a.max_size() == MAX_ELEMENTS );
    assert( a.capacity() == 0 );
    assert( a.begin() == a.end() );

    // insert with push_back correctly triggers allocation
    TestObject u(1);
    a.push_back( u );
    assert( a.size() == 1 );
    assert( a.capacity() == MAX_ELEMENTS );
    assert( a.begin() != a.end() );
    assert( a.front() == u );

    // additional push_back calls don't affect capacity.
    TestObject v(2);
    a.push_back( v );
    assert( a.size() == 2 );
    assert( a.capacity() == MAX_ELEMENTS );    
    assert( a.end() - a.begin() == 2 );
    assert( a.front() == u );
    assert( a.back() == v );
    
    // test element access
    assert( a.front() == a[0] );
    assert( a.back() == a[1] );
    assert( a.front() == a.at(0) );
    bool bExceptionThrown = false;
    try {
        a.at(5);
    }
    catch ( std::range_error ) {
        bExceptionThrown = true;
    }
    assert( bExceptionThrown );
    
    // test the iterator 
    TestArrayA::iterator i;
    TestArrayA::const_iterator iConst;
    i = a.begin();
    assert( i != a.end() );
    assert( *i == u );
    assert( i->x == u.x );
    
    // advance with prefix operator, conversion to const_iterator
    iConst = ++i;
    assert( iConst == i );
    assert( i != a.begin() );
    assert( i != a.end() );
    assert( *i == v );
           
    // advance with postfix operator
    iConst = i++;
    assert( *iConst == v );
    assert( i == a.end() );
    
    // go backwards with decrement operators
    iConst = --i;
    assert( iConst == i );
    assert( i != a.end() );
    assert( *i == v );
    iConst = i--;
    assert( iConst != i );
    assert( i == a.begin() );
    assert( *i == u );
    
    // test reverse iterators
    const TestArrayA& aConst = a;
    TestArrayA::reverse_iterator iRev;
    TestArrayA::const_reverse_iterator iRevConst;
    iRevConst = aConst.rbegin();
    assert( iRevConst != aConst.rend() );
    assert( *iRevConst == v );
    assert( iRevConst->x == v.x );
    
    // advance (in reverse) with prefix operator
    TestArrayA::const_reverse_iterator prev = ++iRevConst;
    assert( iRevConst == prev );
    assert( iRevConst != aConst.rbegin() );
    assert( iRevConst != aConst.rend() );
    assert( *iRevConst == u );
    
    // advance (in reverse) with postfix operator
    iRevConst++;
    assert( iRevConst == aConst.rend() );
    
    // copy constructor and container comparison
    TestArrayA b( a );
    assert( a == b );
    b.push_back( TestObject(3) );
    assert( a != b );
    
    // swap
    TestArrayA aCopy( a );
    TestArrayA bCopy( b );
    aCopy.swap( bCopy );
    assert( aCopy == b );
    assert( bCopy == a );
    
    // assignment
    aCopy = a;
    assert( aCopy == a );

    // clear
    TestArrayA empty;
    TestObject::g_destructorCalls = 0;
    b.clear();
    assert( b.capacity() == 0 );
    assert( b.size() == 0 );
    assert( b.begin() == b.end() );
    assert( b.empty() );
    assert( b == empty );
    assert( TestObject::g_destructorCalls == 3 ); 
    
    // reserve
    b.reserve();
    assert( b.capacity() == MAX_ELEMENTS );
    assert( b.size() == 0 );
    assert( b.empty() );
    assert( b == empty );
    
    // try the erase function
    i = a.erase( a.begin() );
    assert( *i == v );
    assert( a.size() == 1 );
    for ( iConst = a.begin(); iConst != a.end(); ++iConst ) {
        assert( *iConst != u );
    }
    
    // pop_back
    a.pop_back();
    assert( a.size() == 0 );
    
    // range erase
    a.push_back( u );
    a.push_back( u );
    a.push_back( v );
    a.erase( a.begin(), --a.end() );
    assert( a.size() == 1 );
    assert( a.front() == v );
        
    return 0;
}
