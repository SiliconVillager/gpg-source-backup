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
#ifndef GPG8_OCTREE_HPP
#define GPG8_OCTREE_HPP 1

#ifdef _MSC_VER
#pragma warning( disable: 4127 )
#endif

#include "OctreeHelpers.hpp" 
#include <vector>
#include <deque>
#include <cassert>
#include <limits>

//---------------------------------------------------------------------------
/// Namespace for the octree and related components.
//---------------------------------------------------------------------------
namespace spatialdb 
{    
    //-----------------------------------------------------------------------
    /// This is a basic octree implementation with an STL container 
    /// interface. The interface is based on the TR1 unordered associative
    /// container requirements, with some modifications to suit the
    /// functionality of an octree.
    ///
    /// The purpose of this example is to look at what's involved in building
    /// an STL compatible container. The actual octree functionality is 
    /// limited.
    //-----------------------------------------------------------------------
    template <// element type stored in the container
              typename T, 
              // Key should be your own 3D vector type, and must have a 
              // constructor Key(x,y,z) and data members x, y, and z.
              typename Key,
              // when a node's elements exceeds this limit, it is subdivided
              int MaxObjectsPerNode = 8,
              // the location functor is analogous to the hash function
              typename LocationFunctor = DefaultLocationFunctor<T, Key>, 
              // the containment functor allows you to specify how to check 
              // if a location is within a bounding box.
              typename ContainmentFunctor 
                  = ObjectContainedIn<T, Key, LocationFunctor>, 
              // allocator class, which will be passed down to nested 
              // containers so that all allocations go through this allocator
              typename Allocator = std::allocator<T> >
    class Octree {
    private:
        // internal types we will be using
        typedef BoundingBox<Key> OctreeBoundingBox;        
        typedef int ContentID;
        typedef GenericOctNode<ContentID, Key, 
                               MaxObjectsPerNode, Allocator> OctNode;
        typedef std::vector<T, Allocator> RawContentsContainer;

        // Stores the element values. They are referenced from the OctNodes
        // by index.
        RawContentsContainer m_rawContents;

        // The root of the octree.
        mutable OctNode m_rootNode;

        // location functor instance
        LocationFunctor m_GetLocation;

        // containment functor instance
        ContainmentFunctor m_ObjectContainedIn;    

    public:
        ///////////////////////// constants /////////////////////////////////
        static const unsigned int MAX_OBJECTS_PER_NODE = MaxObjectsPerNode;
        static const int DEFAULT_EXTENT                = 65536;
        static const unsigned int NUM_CHILDREN         = 8;
#ifdef NDEBUG  
        static const bool bDebug = false;
#else
        static const bool bDebug = true;       
#endif


        ///////////////// STL BASIC CONTAINER REQUIREMENTS  /////////////////
        
        // container typedefs...
        typedef T         value_type;
        typedef T&        reference;
        typedef const T&  const_reference;
        typedef typename  RawContentsContainer::iterator iterator;
        typedef typename  RawContentsContainer::const_iterator 
                          const_iterator;
        typedef ptrdiff_t difference_type;
        typedef size_t    size_type;
     
        // unordered associative container typedefs...
        typedef Key key_type;
        // an element's location determines which OctNode it is in, which is
        // roughly analogous to mapping to a bucket number in a hash table.
        typedef LocationFunctor hasher;

        //-------------------------------------------------------------------
        /// default constructor
        //-------------------------------------------------------------------
        Octree( key_type location = Key(0.0f,0.0f,0.0f), 
                float extent = (float)DEFAULT_EXTENT ) 
            : m_rootNode(location, extent) {} 
        
        // The compiler generated copy constructor and assignment operator
        // are fine for this class, since everything behaves nicely with
        // member-wise copying.

        //-------------------------------------------------------------------
        /// Destructor.
        //-------------------------------------------------------------------
        ~Octree() {
            // nothing fancy needed here, the containers will clean 
            // themselves up.
        }

        /////////////// iterator access to the entire container /////////////
        //                      (no specific ordering)

        // begin iterators
        iterator begin() { return m_rawContents.begin(); }
        const_iterator begin() const { return m_rawContents.begin(); }

        // end iterators
        iterator end() { return m_rawContents.end(); }
        iterator end() const { return m_rawContents.end(); }

        // As per the unordered associative container requirements draft, 
        // this container doesn't need to implement <, >, <=, >=. However, 
        // equality is useful for testing, so it is implemented.

        //-------------------------------------------------------------------
        /// Two octrees are equal if they have the same contents and 
        /// equivalent indexes built on top of them.
        //-------------------------------------------------------------------
        bool operator==( const Octree& rhs ) const {
            return m_rawContents == rhs.m_rawContents
                && m_rootNode == rhs.m_rootNode;
        }

        //-------------------------------------------------------------------
        /// Negates operator==.
        //-------------------------------------------------------------------
        bool operator!=( const Octree& rhs ) const {
            return !(*this == rhs);
        }

        //-------------------------------------------------------------------
        /// Swapping means swapping both the trees and the raw elements. The
        /// hard work is really implemented in the swap functions of the 
        /// octree members.
        //-------------------------------------------------------------------
        void swap( Octree& rhs ) {
            m_rootNode.swap( rhs.m_rootNode );
            m_rawContents.swap( rhs.m_rawContents );
        }

        //-------------------------------------------------------------------
        /// Number of elements stored in the octree.
        //-------------------------------------------------------------------
        size_type size() const { 
            return m_rawContents.size();
        }

        //-------------------------------------------------------------------
        /// A reasonable upper bound on the number of elements this octree
        /// could store.
        //-------------------------------------------------------------------
        size_type max_size() const {
            return m_rawContents.max_size();
        }

        //-------------------------------------------------------------------
        /// Sometimes faster than checking size() == 0
        //-------------------------------------------------------------------
        bool empty() const {
            return m_rawContents.empty();
        }

        //////////////// END OF BASIC CONTAINER REQUIREMENTS ////////////////


        /////// TR1 UNORDERED ASSOCIATIVE CONTAINER REQUIREMENTS ////////////
        //             (with some modifications for octrees)               //

        typedef MultiOctNodeIterator<OctNode, true> ConstContentIterator;
        typedef MultiOctNodeIterator<OctNode, false> ContentIterator;

        typedef IndexIteratorAdaptor< 
                    typename Octree::ContentIterator,
                    typename Octree::RawContentsContainer, 
                    false> local_iterator;
        typedef IndexIteratorAdaptor< 
                    typename Octree::ConstContentIterator,
                    typename Octree::RawContentsContainer, 
                    true> const_local_iterator;

        // Not implementing the hash table constructors, since they are 
        // designed around managing buckets, which is not relevant to octrees
      
        hasher hash_function() {
            return m_GetLocation;    
        }

        // FIXME: explain?
        // key_eq() from hash table requirements is not relevant to octrees.

        //-------------------------------------------------------------------
        /// Add t to the container. Returns an iterator pointing to the 
        /// element.
        //-------------------------------------------------------------------
        iterator insert( const T& t ) {
            m_rawContents.push_back( t );
            _octreeInsert( m_rawContents.size() - 1, m_rootNode );
            return m_rawContents.end() - 1;
        }

        //-------------------------------------------------------------------
        /// Add t to the container. q is a hint, which the implementation can
        /// ignore.
        //-------------------------------------------------------------------
        iterator insert( const iterator& q, const T& t ) { 
            (void)q;
            return insert(t); 
        }
        
        /// See non-const version for notes.
        const_iterator insert( const const_iterator& r, const T& t ) {
            (void)r;
            return insert(t);
        }

        //-------------------------------------------------------------------
        /// insert a range of items from another octree
        //-------------------------------------------------------------------
        void insert( const_iterator i, const const_iterator& j ) {
            // This would be a good place to use the insert-hint form of 
            // insert(), for example to start the tree search at the last 
            // leaf node. This would let us short-circuit the tree search
            // when sequential elements are spatially close to each other.
            while ( i != j ) {
                insert( *i );
                ++i;
            }
        }

        //-------------------------------------------------------------------
        /// erase by key
        //-------------------------------------------------------------------
        size_type erase( const key_type& k ) {
            typedef typename OctNode::ConstContentIterator IDIterator;

            OctNode* pLeaf = _octreeFindLeafRecursive( k, m_rootNode );
            if ( pLeaf == NULL ) return 0;

            size_type numErased = 0;
            IDIterator i( pLeaf->contentIDs.begin() );
            while ( i != IDIterator(pLeaf->contentIDs.end()) ) {
                if ( m_GetLocation(m_rawContents[*i]) == k ) {
                    i = _octreeRemove( m_rawContents[*i] ).second;
                    ++numErased;
                }
                else ++i;
            }

            return numErased;
        }

        //-------------------------------------------------------------------
        /// erase by iterator
        //-------------------------------------------------------------------
        iterator erase( const iterator& q ) {
            return _octreeRemove( *q ).first;
        }

        /// See non-const version for details.
        const_iterator erase( const const_iterator& r ) {
            return _octreeRemove( *r ).first;
        }

        //-------------------------------------------------------------------
        /// erase range
        //-------------------------------------------------------------------
        iterator erase( const iterator& q1, const iterator& q2 ) {
            typedef std::vector<typename iterator::value_type> TmpVector;
            TmpVector eraseTargets(q1, q2);
            iterator nextIter = q2;
            for ( typename TmpVector::iterator i = eraseTargets.begin();
                  i != eraseTargets.end();
                  ++i ) {
                nextIter = _octreeRemove( *i ).first;
            }

            // FIXME: it is very difficult to provide an iterator to the 
            //        element after the last one that was deleted, because 
            //        _octreeRmove() swap elements around, constantly
            //        changing their ordering. The solution is probably to 
            //        have a different remove function for ranges that
            //        moves the raw contents in a batch at the end.
            return nextIter;
        }

        /// See non-const version for details.
        const_iterator erase( const const_iterator& r1, 
                              const const_iterator& r2 ) {
            typedef std::vector<typename iterator::value_type> TmpVector;
            TmpVector eraseTargets(r1, r2);
            const_iterator nextIter = r2;
            for ( typename TmpVector::iterator i = eraseTargets.begin();
                  i != eraseTargets.end();
                  ++i ) {
                nextIter = _octreeRemove( *i ).first;
            }

            // FIXME: wrong, see note above.
            return nextIter;
        }

        //-------------------------------------------------------------------
        /// Wipes out the contents of the octree.
        //-------------------------------------------------------------------
        void clear() {
            m_rawContents.clear();
            m_rootNode.clear();
        }
        
        //-------------------------------------------------------------------
        /// Returns an iterator to the element at the specified location. If
        /// there are multiple elements with this same location, this will
        /// be the first one according to the (arbitrary) ordering in the 
        /// OctNode.
        //-------------------------------------------------------------------
        iterator find( const key_type& k ) {
            OctNode* pNode = _octreeFindLeafRecursive( k, m_rootNode );
            typename OctNode::ConstContentIterator idIter;
            if ( pNode != NULL ) {
                idIter = _findInOctNode(k, *pNode );
            }
            if ( pNode == NULL || idIter == pNode->contentIDs.end() ) {
                return m_rawContents.end();
            }
            else {
                return m_rawContents.begin() + *idIter;
            }
        }

        /// See non-const version for details.
        const_iterator find( const key_type& k ) const {
            const OctNode* pNode = _octreeFindLeafRecursive( k, m_rootNode );
            typename OctNode::ConstContentIterator idIter;
            if ( pNode != NULL ) {
                idIter = _findInOctNode(k, *pNode );
            }
            if ( pNode == NULL || idIter == pNode->contentIDs.end() ) {
                return m_rawContents.end();
            }
            else {
                return m_rawContents.begin() + *idIter;
            }
        }

        //-------------------------------------------------------------------
        /// For simplicity, we've assumed that no two elements in the octree
        /// will occupy exactly the same location, so this returns 0 or 1.
        //-------------------------------------------------------------------
        size_type count( const key_type& k ) const {
            const OctNode* pNode = _octreeFindLeafRecursive( k, m_rootNode );
            if (pNode == NULL 
                    || _findInOctNode(k,*pNode) == pNode->contentIDs.end()) {
                return 0;
            }
            else return 1;
        }

        //-------------------------------------------------------------------
        /// See note in count() about uniqueness of keys.
        //-------------------------------------------------------------------
        std::pair<iterator, iterator> equal_range( const key_type& k ) {
            OctNode* pNode = _octreeFindLeafRecursive( k, m_rootNode );
            if ( pNode == NULL ) return make_pair( m_rawContents.end(),
                                                   m_rawContents.end() );
            typename OctNode::ConstContentIterator result 
                = _findInOctNode( k, *pNode );
            if ( result == pNode->contentIDs.end() ) {
                return std::make_pair( m_rawContents.end(), 
                                       m_rawContents.end() );
            }
            else {
                return std::make_pair( m_rawContents.begin() + *result,
                                       m_rawContents.begin() 
                                           + ((*result) + 1) );
            }
        }

        /// See non-const version for details.    
        std::pair<const_iterator, 
                  const_iterator> equal_range( const key_type& k ) const {
            const OctNode* pNode = _octreeFindLeafRecursive( k, m_rootNode );
            if ( pNode == NULL ) return make_pair( m_rawContents.end(),
                                                   m_rawContents.end() );
            typename OctNode::ConstContentIterator result 
                = _findInOctNode( k, *pNode );
            if ( result == pNode->contentIDs.end() ) {
                return std::make_pair( m_rawContents.end(), 
                                       m_rawContents.end() );
            }
            else {
                return std::make_pair( m_rawContents.begin() + *result,
                                       m_rawContents.begin() 
                                           + ((*result) + 1) );
            }
        }

        // The bucket related functions are omitted, since they don't apply
        // to octrees. Similar functionality for accessing elements by octant
        // are provided in the extended octree interface (below).

        ////// END OF TR1 UNORDERED ASSOCIATIVE CONTAINER REQUIREMENTS //////


        //////////////////// EXTENDED OCTREE INTERFACE //////////////////////
        //                                                                 //

        //-------------------------------------------------------------------
        /// Returns an iterator at the start of the range of elements in the 
        /// octant containing k.
        //-------------------------------------------------------------------
        local_iterator begin( const key_type& k ) {
            OctNode* leaf = _octreeFindLeafRecursive( k, m_rootNode );
            return local_iterator( leaf, m_rawContents );
        }
        
        /// See non-const version for details.
        const_local_iterator begin( const key_type& k ) const {
            const OctNode* leaf = _octreeFindLeafRecursive( k, m_rootNode );
            return const_local_iterator( leaf, m_rawContents );
        }
        
        //-------------------------------------------------------------------
        /// Returns an iterator at the end of the range of elements in the
        /// octant containing k.
        //-------------------------------------------------------------------
        local_iterator end( const key_type& k ) {
            (void)k;
            return local_iterator( NULL, m_rawContents );
        }
        
        /// See non-const version for details.
        const_local_iterator end( const key_type& k ) const {
            (void)k;
            return const_local_iterator( NULL, m_rawContents );
        }
                
        //-------------------------------------------------------------------
        /// Convenience version of begin(Key) that extracts the location from
        /// the element supplied.
        //-------------------------------------------------------------------
        const_local_iterator begin( const T& t ) const {  
            return begin( m_GetLocation(t) );
        }

        //-------------------------------------------------------------------
        /// Convenience version of end(Key) that extracts the location from
        /// the element supplied.
        //-------------------------------------------------------------------
        const_local_iterator end( const T& t ) const {
            return end( m_GetLocation(t) );
        }
                
        //-------------------------------------------------------------------
        /// Returns a pair of iterators defining a range of Octree elements
        /// that are in octants intersecting the sphere (centered at the 
        /// given location with the specified radius). Note that this does
        /// *not* imply that the elements themselves intersect the sphere.
        ///
        /// Naturally, the efficiency of this function is proportional to the 
        /// size of the search sphere vs the size of the octants. In the 
        /// worst case, if you specify a sphere that is as big as the root 
        /// node's bounding box, you'll get the whole octree back!
        //-------------------------------------------------------------------
        std::pair<const_local_iterator, const_local_iterator> 
        find_range( const key_type& location, float radius ) const {
            std::vector<OctNode*> overlappingNodes;
            _octreeFindOctants( location, radius, 
                                back_inserter(overlappingNodes) );

            // By now, we have a list of the OctNodes that intersect the
            // search sphere, which will get copied into the 
            // MultiOctNodeIterator so that the caller can iterate through
            // the actual objects.
            return make_pair( const_local_iterator(
                                  ConstContentIterator(
                                      overlappingNodes.begin(),
                                      overlappingNodes.end() ), 
                                  m_rawContents),
                              const_local_iterator(
                                  ConstContentIterator(),
                                  m_rawContents) );
        }

        /// See const version of this function for details.
        std::pair<local_iterator, local_iterator> 
        find_range( const key_type& location, float radius ) {
            std::vector<OctNode*> overlappingNodes;
            _octreeFindOctants( location, radius, 
                                back_inserter(overlappingNodes) );
            return make_pair( local_iterator(
                                  ContentIterator(
                                      overlappingNodes.begin(),
                                      overlappingNodes.end() ), 
                                  m_rawContents),
                              local_iterator(
                                  ContentIterator(),
                                  m_rawContents) );
        }
        
        ///////////////// END OF EXTENDED OCTREE INTERFACE //////////////////

    private:
        ////////////////////// INTERNAL HELPERS /////////////////////////////

        //-------------------------------------------------------------------
        /// Given a key (location) and an OctNode, returns a pointer to the
        /// leaf node beneath the OctNode that most tightly bounds the key.
        /// Implemented using recursion for simplicity and clarity, this is
        /// a good candidate for future optimization.
        //-------------------------------------------------------------------
        OctNode* _octreeFindLeafRecursive( const key_type& k, 
                                           OctNode& root ) const {
            if ( root.bounds.containsPoint(k) ) {
                if ( root.isLeaf() ) return &root;
                // else find the best child
                OctNode* result = NULL;
                for ( typename OctNode::OctNodeIterator i 
                          = root.children.begin();
                      i != root.children.end();
                      ++i ) {
                    result = _octreeFindLeafRecursive( k, *i );
                    if ( result != NULL ) return result;
                }
                // if k is contained in this node, it should be in one 
                // of the children too
                assert( false && "This shouldn't happen!" );
            }

            return NULL;
        }

        //-------------------------------------------------------------------
        /// Given a sphere, writes a list of OctNode pointers into results
        /// which corresponds to all of the octants that the sphere 
        /// intersects.
        //-------------------------------------------------------------------
        template <typename ResultsInsertIterator>
        void _octreeFindOctants( const key_type& location, float radius,
                                 ResultsInsertIterator results ) const {
            std::vector<OctNode*> candidates;

            candidates.reserve( 8 );            
            OctNode* pCurrentNode = &m_rootNode;            
            while ( pCurrentNode != NULL ) {
                // test this node's bounding box vs the sphere for 
                // intersection...
                if ( pCurrentNode
                        ->bounds.intersectsSphere(location, radius) ) {
                    if ( pCurrentNode->isLeaf() ) {
                        if ( !pCurrentNode->contentIDs.empty() ) {
                            *results++ = pCurrentNode;
                        }
                    }
                    else {
                        // at least some of this node's children intersect
                        // the sphere, so queue them up for testing.
                        for ( typename OctNode::OctNodeIterator i 
                                = pCurrentNode->children.begin();
                              i != pCurrentNode->children.end();
                              ++i ) {
                            candidates.push_back( &(*i) );
                        }
                    }
                }
                // else - nodes that don't intersect the sphere are 
                //        irrelevant, so they are ignored.

                // advance the search.
                if ( candidates.empty() ) pCurrentNode = NULL;
                else {
                    pCurrentNode = candidates.back();
                    candidates.pop_back();
                }
            }
        }

        //-------------------------------------------------------------------
        /// Insert an ContentID which refers to an element in m_rawContents
        /// into the octree rooted at the supplied OctNode. Does not check 
        /// for uniqueness of elements.
        //-------------------------------------------------------------------
        void _octreeInsert( ContentID objectID, OctNode& root ) {        
            // verify that the object belongs in this octant
            if ( !m_ObjectContainedIn(m_rawContents[objectID],
                                      root.bounds) ) {
                // quietly fail to insert, so that this is effectively a 
                // no-op when called on irrelevant nodes.
                return;
            }
            
            // for leaf nodes with enough capacity, just add the object and
            // we're done.
            if ( root.children.empty() 
                    && root.contentIDs.size() < MAX_OBJECTS_PER_NODE ) {
                if ( bDebug ) {
                    for ( typename OctNode::ContentIterator i 
                              = root.contentIDs.begin();                          
                          i != root.contentIDs.end();
                          ++i ) {
                              assert( m_rawContents[*i].location != 
                                        m_rawContents[objectID].location );
                    }
                }
                root.contentIDs.push_back( objectID );   
                return;
            }
            
            // if this node isn't already an internal node, subdivide it.
            if ( root.children.empty() ) {
                root.children.reserve( NUM_CHILDREN );
                float childOffset = root.bounds.halfSize / 2.0f;
                
                // init the child with the bounding boxes that subdivide the
                // root's box.
                for ( unsigned int i = 0; i < NUM_CHILDREN; ++i ) {                
                    unsigned int foo = i & 0x1;
                    (void)foo;
                    Key childPos( root.bounds.location.x 
                                    + childOffset 
                                        * (1 - 2 * int(i & 0x1)),
                                  root.bounds.location.y
                                    + childOffset 
                                        * (1 - 2 * int((i & 0x2) >> 1)),
                                  root.bounds.location.z
                                    + childOffset 
                                        * (1 - 2 * int((i & 0x4) >> 2)) );
                    root.children.push_back( OctNode(childPos, 
                                                     root.bounds.halfSize) );
                }     

                // after subdividing the node, we have to insert its contents
                // into the new children.
                for ( typename OctNode::ConstContentIterator j 
                          = root.contentIDs.begin();
                      j != root.contentIDs.end();
                      ++j ) {
                    _octreeChildInsert( root.children.begin(), 
                                        root.children.end(),
                                        *j );
                }

                // reclaim storage used for contentIDs when this node was a
                // leaf node.
                root.contentIDs.clear();
            }

            // This is an iternal node, so pass the object down to this 
            // node's children for insertion.
            _octreeChildInsert( root.children.begin(), root.children.end(),
                               objectID );
        } // end _octreeInsert

        //-------------------------------------------------------------------        
        /// Inserts the supplied objectID into the node(s) in the iterator
        /// range which contain that object's location.
        //-------------------------------------------------------------------
        void _octreeChildInsert( typename OctNode::OctNodeIterator begin, 
                                 typename OctNode::OctNodeIterator end, 
                                 size_t objectID ) {
            for ( typename OctNode::OctNodeIterator childIter = begin;
                 childIter != end;
                 ++childIter ) {
                 _octreeInsert( objectID, *childIter );
            }
        }

        //-------------------------------------------------------------------
        /// Returns a content iterator pointing to the element matching the
        /// key, from the supplied octnode's contentID list.
        //-------------------------------------------------------------------
        typename OctNode::ConstContentIterator _findInOctNode(
                              const key_type& k,
                              const OctNode& octNode ) const {
            typename OctNode::ConstContentIterator i 
                = octNode.contentIDs.begin();
            typename OctNode::ConstContentIterator rangeEnd 
                = octNode.contentIDs.end();
            while ( i != rangeEnd ) {
                if ( m_GetLocation(m_rawContents[*i]) == k ) break;
                ++i;
            }
            return i;
        }

        //-------------------------------------------------------------------
        // removing elements is done by swapping the last element of the
        // container with the target for deletion, and then popping the 
        // target off the end of the containers. This changes the ContentID
        // of the last element, so we have to go find it in the octree and
        // fix the ID.
        //-------------------------------------------------------------------
        std::pair<iterator, typename OctNode::ContentIterator> 
        _octreeRemove( const T& target ) {
            OctNode* pTargetNode 
                = _octreeFindLeafRecursive(m_GetLocation(target),m_rootNode);
            if ( pTargetNode == NULL ) {
                return std::make_pair(m_rawContents.end(),
                                      m_rootNode.contentIDs.end() );
            }

            // write the last contentID in this node over the deleted 
            // element's ID (saved in targetID) 
            ContentID targetID = -1;
            typename OctNode::ContentIterator i;
            if ( pTargetNode->contentIDs.size() > 1 ) {
                // find the element being removed...
                i = pTargetNode->contentIDs.begin();
                typename OctNode::ContentIterator contentEnd 
                    = pTargetNode->contentIDs.end();
                while ( i != contentEnd ) {
                    if ( m_rawContents[*i] == target ) {
                        // copy the last element over the one being removed
                        targetID = *i;
                        *i = pTargetNode->contentIDs.back();
                        break;
                    }
                    else {
                        ++i;
                    }
                }
                // _findLeaf should never return a node that doesn't contain
                // the target element.
                assert( i != contentEnd );
            }
            else {
                targetID = pTargetNode->contentIDs.back();
                i = --pTargetNode->contentIDs.end();
            }

            // do a similar trick with the raw contents container...
            if ( m_rawContents.size() > 1 ) {
                // overwrite the targetID with the lastID
                const ContentID lastID  = m_rawContents.size() - 1;
                m_rawContents[targetID] = m_rawContents.back();
                // find the lastID in the octree, and update it to 
                // targetID...
                OctNode* pSwapNode 
                    = _octreeFindLeafRecursive(
                          m_GetLocation(m_rawContents[targetID]), 
                          m_rootNode );
                assert( pSwapNode != NULL );
                typename OctNode::ContentIterator j 
                    = pSwapNode->contentIDs.begin();
                typename OctNode::ContentIterator contentEnd
                    = pSwapNode->contentIDs.end();
                while ( j != contentEnd ) {
                    if ( *j == lastID ) {
                        *j = targetID;
                        break;
                    }
                    ++j;
                }
                assert( j != contentEnd );
            }

            // if i is pointing to the last element, it will be invalidated
            // when we pop_back(), so we can't use it in the return pair.
            bool bDeletingLastID = (i == --pTargetNode->contentIDs.end());
            pTargetNode->contentIDs.pop_back();
            m_rawContents.pop_back();

            // special case, if we just erased the last element,
            return std::make_pair( m_rawContents.begin() + targetID, 
                                   bDeletingLastID 
                                       ? pTargetNode->contentIDs.end()
                                       : i );
        } // end _octreeRemove()

    }; // end of class OctTree

} // end namespace spatialdb

#endif
