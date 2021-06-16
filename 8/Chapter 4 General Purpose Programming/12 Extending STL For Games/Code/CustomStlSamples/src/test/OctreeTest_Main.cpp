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
#include "Octree.hpp"
#include "test/TestingHelpers.hpp"
#include <list>

#ifdef _MSC_VER
#include <crtdbg.h>
#endif 

/// helper function that runs tests on the contents of OctreeHelpers.hpp
static void testOctreeHelpers();

//---------------------------------------------------------------------------
/// This is a basic test harness which exercises the octree, using assert to
/// check for correctness.
//---------------------------------------------------------------------------
int main( void ) {
#ifdef _MSC_VER
    _CrtSetDbgFlag( _CRTDBG_ALLOC_MEM_DF | _CRTDBG_LEAK_CHECK_DF );
#endif

    testOctreeHelpers();

    // create an empty tree
    typedef spatialdb::Octree<GameObject, Vector3f, 2 > Octree;
    Octree octree( Vector3f(0.0f, 0.0f, 0.0f), 256.0f );
    const Octree& constOctree = octree;
    assert( octree.empty() );

    // create some test objects
    GameObject zaboo, vork;
    zaboo.location = Vector3f( 10, 1, 1 );
    vork.location = Vector3f( -10, 1, 10 );
    // illegal (will assert), locations must be unique in this implementation
    //vork.location = zaboo.location;

    // insert test objects
    octree.insert( zaboo );
    assert( octree.size() == 1 );
    assert( ++octree.begin(zaboo.location) == octree.end(zaboo.location) );
    octree.insert( vork );
    assert( octree.size() == 2 );
    Octree::local_iterator localIter = octree.begin(zaboo.location);
    std::advance( localIter, 2 );
    assert( localIter == octree.end(zaboo.location) );

    // find    
    Octree::iterator iter = octree.find( zaboo.location );
    Octree::const_iterator constIter = constOctree.find( vork.location );
    assert( *iter == zaboo );
    assert( *constIter == vork );
    iter = octree.find( Vector3f(99,99,99) );
    assert( iter == octree.end() );    

    // another test object, which triggers subdivision.
    GameObject codex;
    codex.location = Vector3f( 10, 1, 2 );
    octree.insert( codex );
    assert( octree.size() == 3 );
    localIter = octree.begin(zaboo.location);
    std::advance( localIter, 2 );
    assert( localIter == octree.end(zaboo.location) );
    localIter = octree.begin(vork.location);
    assert( ++localIter == octree.end(vork.location));

    Octree octreeB(Vector3f(0,0,0), 256);
    octreeB.insert( codex );

    // swap
    octreeB.swap( octree );
    assert( octreeB.size() == 3 );
    assert( octree.size() == 1 );

    // copy construct and equality
    Octree octreeC( octreeB );
    assert( octreeC == octreeB );
    assert( octreeB != octree );

    // clear
    octreeC.clear();
    assert( octreeC.size() == 0 );
    assert( octreeC.begin() == octreeC.end() );

    // exercise extra insert methods
    octree.insert( octree.begin(), zaboo );
    assert( octree.size() == 2 );
    octreeC.insert( octreeB.begin(), octreeB.end() );
    assert( octreeC == octreeB );

    octree.clear();
    octree.insert( zaboo );
    octree.insert( codex );

    // erase by key
    octreeB = octree;
    size_t n = octreeB.erase( codex.location );
    (void)n;
    assert( n == 1 );
    assert( octreeB.size() == 1 );
    assert( *octreeB.begin() == zaboo );

    // erase by iterator
    octreeB = octree;
    octreeB.erase( octreeB.begin() );
    assert( octreeB.size() == 1 );
    assert( *octreeB.begin() == codex );
    assert( *octreeB.find(codex.location) == codex );
    localIter = octreeB.begin(codex.location);
    assert( *localIter == codex );
    assert( ++localIter == octreeB.end(codex.location) );
    
    // erase last element w/ const_iterator
    constIter = octreeB.begin();
    octreeB.erase( constIter );
    assert( octreeB.size() == 0 );
    assert( octreeB.end(codex.location) == octreeB.begin(codex.location) );

    // exercise insert with hint     
    octree.clear();
    octree.insert( octree.begin(), zaboo );
    octree.insert( constIter, vork );
    octree.insert( codex );    
    octreeB = octree;
    // test erase from sub-nodes
    // zaboo and codex are in one octant, vork in another
    localIter = octreeB.begin(zaboo.location);
    advance( localIter, 2 );
    assert( localIter == octreeB.end(zaboo.location) );
    localIter = octreeB.begin(vork.location);
    assert( ++localIter == octreeB.end(vork.location) );

    octreeB.erase( zaboo.location );
    // now just codex in one octant, vork in another
    localIter = octreeB.begin(zaboo.location);
    assert( ++localIter == octreeB.end(zaboo.location) );
    localIter = octreeB.begin(vork.location);
    assert( ++localIter == octreeB.end(vork.location) );

    octreeB.erase( vork.location );
    // now vork's octant is empty
    localIter = octreeB.begin(zaboo.location);
    assert( ++localIter == octreeB.end(zaboo.location) );
    localIter = octreeB.begin(vork.location);
    assert( localIter == octreeB.end(vork.location) );
    assert( octreeB.find(vork.location) == octreeB.end() );

    // range erase
    octreeB = octree;
    iter = octreeB.erase( octreeB.begin(), octreeB.end() );
    assert( octreeB.empty() );
    assert( iter == octreeB.end() );
    // range erase with const_iterators
    octreeB = octree;
    Octree::const_iterator constBegin( ++octreeB.begin() );
    Octree::const_iterator constEnd( octreeB.end() );
    constIter = octreeB.erase( constBegin, constEnd );
    assert( octreeB.size() == 1 );
    assert( *octreeB.begin() == zaboo );
    assert( constIter == octreeB.end() );
    // erase up to the last elem
    octreeB = octree;
    octreeB.erase( octreeB.begin(), --octreeB.end() );
    assert( octreeB.size() == 1 );
    assert( *octreeB.begin() == codex );
    // NOTE: the iterator returned by the range form of erase is not correct,
    //       because of limitations of the octree implementation.

    // test count 
    assert( octree.count(vork.location) == 1 );
    assert( octree.count(Vector3f(99,99,99)) == 0 );

    // test equal_range
    std::pair<Octree::iterator, Octree::iterator> resultRange;
    resultRange = octree.equal_range( zaboo.location );
    assert( *resultRange.first == zaboo );
    assert( resultRange.second - resultRange.first == 1 );
        
    std::pair<Octree::const_iterator, 
              Octree::const_iterator> constResultRange;
    constResultRange = constOctree.equal_range( Vector3f(967,11,11) );
    assert( constResultRange.first == constResultRange.second );
    assert( constResultRange.first == octree.end() );

    // test find_range
    std::pair<Octree::local_iterator, Octree::local_iterator> results;
    Octree searchTree(Vector3f(0,0,0), 256 );
    searchTree.insert( zaboo );
    searchTree.insert( vork );
    searchTree.insert( codex );

    GameObject clara;
    clara.location = Vector3f( 10, 1, 3 );
    searchTree.insert( clara );
    assert( searchTree.size() == 4 );
    localIter = searchTree.begin(codex.location);
    std::advance( localIter, 2 );
    assert( localIter == searchTree.end(codex.location) );

    // start with a massive search that should encompass all elements...
    results = searchTree.find_range( Vector3f(0,0,0), 256 );
    advance( results.first, 4 );
    assert( results.first == results.second );

    // a search that should get three elements across multiple octants
    results = searchTree.find_range( codex.location, 1.5 );
    advance( results.first, 3 );
    assert( results.first == results.second );

    // now a search that should get two elements in the same octant
    results = searchTree.find_range( clara.location, 0.5 );
    advance( results.first, 2 );
    assert( results.first == results.second );

    return 0;
}


//---------------------------------------------------------------------------
/// A test rig for the classes in OctreeHelpers.hpp. Uses assert to check for
/// correctness.
//---------------------------------------------------------------------------
void testOctreeHelpers() {
    using namespace spatialdb;

    Vector3f p0( 0, 0, 0 );
    Vector3f p1( 1, 2, 3 );
    Vector3f p2( -11, -22, -33 );
    Vector3f p3( 3, 2, 1 );
    GameObject t1;
    t1.location = p1;
    
    // location functor
    typedef DefaultLocationFunctor<GameObject, Vector3f> LocnFunctor;
    LocnFunctor getLocation;
    assert( getLocation(t1) == p1 ); (void)getLocation;

    // bounding box
    BoundingBox<Vector3f> bbox( p3, 5 );
    assert(  bbox.containsPoint(p1) );
    assert(  bbox.containsPoint(p3) );
    assert( !bbox.containsPoint(p0) );
    assert( !bbox.containsPoint(p2) );

    // containment functor
    ObjectContainedIn<GameObject, Vector3f, LocnFunctor> containmentFunctor;    
    assert( containmentFunctor(t1,bbox) ); (void)containmentFunctor;
    t1.location = p0;
    assert( !containmentFunctor(t1,bbox) );

    // GenericOctNode
    typedef GenericOctNode<int,Vector3f,4> OctNode;
    OctNode nodeA( Vector3f(256,256,256), 512 );
    nodeA.contentIDs.push_back(99);

    OctNode nodeB( Vector3f(0,0,0), 1024 );
    nodeB.children.push_back( nodeA );
    
    OctNode nodeC( nodeB );
    assert( nodeC.children.size() == 1 );
    assert( nodeC.children[0].contentIDs.size() == 1 );
    assert( nodeC.children[0].contentIDs[0] == 99 );

    // MultiOctNodeIterator
    typedef MultiOctNodeIterator<OctNode, false> OctNodeIterator;
    std::vector<OctNode*> nodes;

    // empty range
    OctNodeIterator emptyIter( nodes.begin(), nodes.end() );
    assert( emptyIter == OctNodeIterator() );

    // iterate over a single node
    nodes.push_back( &nodeA );
    OctNodeIterator current( nodes.begin(), nodes.end() );
    assert( *current == nodeA.contentIDs[0] );
    ++current;
    assert( current == emptyIter );

    nodeB.clear();
    nodeB.contentIDs.push_back( 42 );
    nodeB.contentIDs.push_back( 13 );
    nodeB.contentIDs.push_back( 666 );

    nodes.push_back( &nodeB );
    nodes.push_back( &nodeA );
    // nodes = [ A, B, A ]

    OctNodeIterator iterB( nodes.begin(), nodes.end() );
    current = iterB;
    assert( current == iterB );
    OctNodeIterator iterC( iterB );
    assert( iterC == iterB );

    assert( iterB != emptyIter );
    assert( *iterB == nodeA.contentIDs[0] );   
    ++iterB;
    assert( iterB != emptyIter );
    assert( *iterB == nodeB.contentIDs[0] );
    ++iterB;
    assert( iterB != emptyIter );
    assert( *iterB == nodeB.contentIDs[1] );
    iterB++;
    assert( iterB != emptyIter );
    assert( *iterB == nodeB.contentIDs[2] );
    ++iterB;
    assert( iterB != emptyIter );
    assert( *iterB == nodeA.contentIDs[0] );
    ++iterB;
    assert( iterB == emptyIter );

    // IndexIteratorAdaptor test setup
    // create a container...
    typedef std::vector<char> CharVector;
    CharVector testContainer;
    testContainer.push_back( 'b' );
    testContainer.push_back( 'a' );
    testContainer.push_back( 'c' );
    // and another container of indices into the previous one
    typedef std::list<size_t> IndexList;
    IndexList testIndices;
    testIndices.push_back( 1 );
    testIndices.push_back( 0 );
    testIndices.push_back( 2 );

    // test basic creation and iteration
    typedef IndexIteratorAdaptor<IndexList::iterator, CharVector, false>
        TestIndexAdaptor;
    TestIndexAdaptor idxIterA( testIndices.begin(), testContainer );
    TestIndexAdaptor idxIterEnd( testIndices.end(), testContainer );
    assert( *idxIterA++ == 'a' );
    assert( *idxIterA == 'b' );
    assert( *(++idxIterA)++ == 'c' );
    assert( idxIterA == idxIterEnd );

    // exercise assignment
    idxIterA = TestIndexAdaptor( testIndices.begin(), testContainer );
    assert( *idxIterA == 'a' );

    // test const version of iterator
    typedef IndexIteratorAdaptor<IndexList::const_reverse_iterator, 
                                 CharVector, true> TestConstIndexAdaptor;    
    TestConstIndexAdaptor constIdxIterA(testIndices.rbegin(), testContainer);
    TestConstIndexAdaptor constIdxIterEnd(testIndices.rend(), testContainer);
    assert( *constIdxIterA++ == 'c' );
    assert( *constIdxIterA++ == 'b' );
    assert( *constIdxIterA++ == 'a' );
    assert( constIdxIterA == constIdxIterEnd );
}
