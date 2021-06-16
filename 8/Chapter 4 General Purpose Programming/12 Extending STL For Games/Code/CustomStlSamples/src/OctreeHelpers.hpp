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
#ifndef GPG8_OCTREE_HELPERS_HPP
#define GPG8_OCTREE_HELPERS_HPP 1

#include "FixedArray.hpp"
#include <vector>
#include <iterator>

//---------------------------------------------------------------------------
// namespace for the octree and related components.
//---------------------------------------------------------------------------
namespace spatialdb 
{
    //-----------------------------------------------------------------------    
    /// A functor for getting the location of an object of type T. This is 
    /// the default implementation for the octree, which works for objects 
    /// with a "location" member with x,y,z data members. The LocationType is
    /// required to have x,y,z data members.
    //-----------------------------------------------------------------------
    template <typename T, typename LocationType>
    class DefaultLocationFunctor
    {
    public:        
        //-------------------------------------------------------------------
        /// Must return the same type as you are using for the octree's Key.
        //-------------------------------------------------------------------
        LocationType operator() ( const T& target ) const {
            LocationType result;
            result.x = target.location.x;
            result.y = target.location.y;
            result.z = target.location.z;

            return result;
        }
    };


    //-----------------------------------------------------------------------    
    /// Generic bounding box class, including a location test member 
    /// function that works for any LocationType with x,y,z members. This 
    /// version favours compactness at the expense of a little speed in the 
    /// test function.
    //-----------------------------------------------------------------------
    template <typename LocationType>
    class BoundingBox {
    public:
        LocationType location;
        float halfSize;

        //-------------------------------------------------------------------
        /// location is the center of the bounding box, size is the 
        /// side-length
        //-------------------------------------------------------------------
        BoundingBox( const LocationType& initLocation, float size )
            : location( initLocation ) {             
            halfSize = size / 2.0f;
        }

        //-------------------------------------------------------------------
        /// Returns true if this bounding box contains the supplied point,
        /// false otherwise.
        ///
        /// Note: this implementation does not allow for floating point
        ///       precision errors, so could produce erroneous results near
        ///       the test boundaries.
        //-------------------------------------------------------------------
        bool containsPoint( LocationType testLocation ) const {
            return testLocation.x < location.x + halfSize 
                && testLocation.x >= location.x - halfSize
                && testLocation.y < location.y + halfSize
                && testLocation.y >= location.y - halfSize
                && testLocation.z < location.z + halfSize
                && testLocation.z >= location.z - halfSize;
        }

        //-------------------------------------------------------------------
        /// Returns true if this bounding box intersects the sphere with the 
        /// specified location and radius, false otherwise.
        ///
        /// Algorithm from James Arvo's article in Graphics Gems, pp.335-338.
        //-------------------------------------------------------------------
        bool intersectsSphere( LocationType center, float radius ) const {
            float distSqr = 0;

            float xMin = location.x - halfSize;
            float xMax = location.x + halfSize;            
            if ( center.x < xMin ) {
                distSqr += (center.x - xMin) * (center.x - xMin);
            }
            else if ( center.x > xMax ) {
                distSqr += (center.x - xMax) * (center.x - xMax);
            }

            float yMin = location.y - halfSize;
            float yMax = location.y + halfSize;            
            if ( center.y < yMin ) {
                distSqr += (center.y - yMin) * (center.y - yMin);
            }
            else if ( center.y > yMax ) {
                distSqr += (center.y - yMax) * (center.y - yMax);
            }

            float zMin = location.z - halfSize;
            float zMax = location.z + halfSize;            
            if ( center.z < zMin ) {
                distSqr += (center.z - zMin) * (center.z - zMin);
            }
            else if ( center.z > zMax ) {
                distSqr += (center.z - zMax) * (center.z - zMax);
            }

            return distSqr < (radius * radius);
        }
    };


    //-----------------------------------------------------------------------
    /// Functor for testing if an object's location (determined generically) 
    /// is contained within the volume of a bounding box.
    //-----------------------------------------------------------------------
    template <typename T, typename LocationType, typename LocationFunctor>
    class ObjectContainedIn {
    public:
        //-------------------------------------------------------------------
        /// Returns true if the supplied object is contained within the
        /// given bounding box, false otherwise.
        //-------------------------------------------------------------------
        bool operator() (const T& object, 
                         const BoundingBox<LocationType>& bounds ) {            
            LocationFunctor getLocation;
            LocationType location = getLocation( object );
            return bounds.containsPoint( location );
        }
    };


    //-----------------------------------------------------------------------
    /// A generic octree node struct.
    //-----------------------------------------------------------------------
    template <typename ContentIDType, typename LocationType, 
              int MaxObjectsPerNode = 1, 
              typename Allocator = std::allocator<int> >
    struct GenericOctNode {
        static const int NODE_CAPACITY = MaxObjectsPerNode;
        static const int NUM_CHILDREN  = 8;
        BoundingBox<LocationType> bounds;

        // The allocator we are given probably came from the Octree, which
        // would be an allocator<T>. Rebind is a little trick for creating
        // an allocator for a different kind of object. This comes up all the
        // time in containers, because they may need to allocate all kinds of
        // different internal objects.
        typedef typename Allocator::template rebind<GenericOctNode>::other
                OctNodeAllocator;
        typedef typename Allocator::template rebind<ContentIDType>::other
                ContentIDAllocator;

        // uncomment to use vector instead of FixedArray
        //typedef std::vector<GenericOctNode, OctNodeAllocator> 
        //        OctNodeContainer;
        typedef gpg8_util::FixedArray<GenericOctNode, NUM_CHILDREN,
                                      OctNodeAllocator> OctNodeContainer;
        typedef typename OctNodeContainer::iterator OctNodeIterator;
        typedef typename OctNodeContainer::const_iterator 
                ConstOctNodeIterator;
        OctNodeContainer children;        
        
        // uncomment to use vector instead of FixedArray
        //typedef std::vector<ContentIDType, ContentIDAllocator> 
        //        ContentsContainer;
        typedef gpg8_util::FixedArray<ContentIDType, MaxObjectsPerNode,
                                      ContentIDAllocator> ContentsContainer;
        typedef typename ContentsContainer::iterator ContentIterator;
        typedef typename ContentsContainer::const_iterator 
                ConstContentIterator;
        ContentsContainer contentIDs;

        /// Standard constructor for an empty OctNode with a bounding box 
        /// centered at the given location, with sides of the length given by
        /// extent.
        GenericOctNode( LocationType location, float extent )
            : bounds( location, extent ) {}

        /// Default constructor, creates a zero sized bounding box, 
        /// generally not useful as-is.
        GenericOctNode() 
            : bounds( LocationType(0,0,0), 0.0f ) {}   

        // the auto-generated copy constructor and assignment operator are
        // fine for this class.

        /// This is a deep equality check, which checks the entire subtree
        /// rooted at this node.
        bool operator==( const GenericOctNode& rhs ) const {
            // for leaf nodes, compare contents
            if ( children.size() == 0 ) return contentIDs == rhs.contentIDs;
            // for non-leaf nodes, compare children (recursively)
            else return children == rhs.children;            
        }

        /// Negation of operator==()
        bool operator!=( const GenericOctNode& rhs ) const {
            return !( *this == rhs );
        }

        /// Revert to an empty node.
        void clear() {
            children.clear();
            contentIDs.clear();
        }

        /// Performs a relatively efficient swap of contents with another
        /// node.
        void swap( GenericOctNode& rhs ) {
            children.swap( rhs.children );
            contentIDs.swap( rhs.contentIDs );

            BoundingBox<LocationType> bbox( bounds );
            bounds     = rhs.bounds;
            rhs.bounds = bbox;
        }

        /// Returns true if this node is a leaf node, i.e. has no children.
        bool isLeaf() const {
            return children.empty();
        }
    }; // end GenericOctNode


    //-----------------------------------------------------------------------
    /// An iterator class that aggregates the contents of several OctNodes.
    /// This is a handy way to return the results of a search through the 
    /// octree for nodes satisfying some criteria. All end iterators of 
    /// this type are equivalent, and are created by the default constructor
    /// or by passing an empty range to the range constructor.
    //-----------------------------------------------------------------------
    template <typename OctNode, bool isConst> 
    class MultiOctNodeIterator {
    private:                
        typedef typename ChooseType<isConst, 
                                    const OctNode*, 
                                    OctNode*>::type OctNodePtr;
        typedef typename ChooseType<isConst,
                    typename OctNode::ConstContentIterator,
                    typename OctNode::ContentIterator>::type ContentIterator;

    public:
        // standard iterator typedefs
        typedef typename ContentIterator::value_type value_type;
        typedef std::ptrdiff_t difference_type;
        typedef std::forward_iterator_tag iterator_category;
        typedef typename ChooseType<isConst, const value_type&, 
                                             value_type&>::type reference;
        typedef typename ChooseType<isConst, const value_type*, 
                                             value_type*>::type pointer;

    private:
        // internal iterator state
        typedef std::vector<OctNodePtr> OctNodeList;
        OctNodeList m_octNodePtrs;
        // when m_currentNodePtr == m_endNodePtr, m_currentElement and 
        // m_endElement are undefined
        typename OctNodeList::iterator m_currentNodePtr, m_endNodePtr;
        typename MultiOctNodeIterator::ContentIterator m_currentElement, 
                                                       m_endElement;

    public:
        /// Default constructor, creates an end iterator.
        MultiOctNodeIterator() 
            : m_octNodePtrs(), m_currentNodePtr( m_octNodePtrs.end() ), 
                               m_endNodePtr( m_octNodePtrs.end() ) {}

        /// Create an iterator for a single node.
        MultiOctNodeIterator( OctNodePtr pNode ) 
            : m_octNodePtrs() {
            if ( pNode != NULL && !(pNode->contentIDs.empty()) ) {
                m_octNodePtrs.push_back( pNode );
                initIterators();
            }
            else {
                m_currentNodePtr = m_octNodePtrs.end();
                m_endNodePtr = m_currentNodePtr;
            }
        }

        /// Range constructor. Given a range of pointers to OctNodes, 
        /// initializes this iterator instance with a list of those pointers.
        template<typename iterator>
        MultiOctNodeIterator( iterator srcBegin, iterator srcEnd ) 
            : m_octNodePtrs( srcBegin, srcEnd ) {
            initIterators();
        }

        /// Copy constructor. Have to be a little careful to correctly
        /// transfer iterators over m_octNodePtrs, since *this will contain
        /// a new vector instance, requiring new iterators.
        ///
        /// rhs is const because a non-const iterator can be converted to 
        /// const, but the reverse is not true.
        MultiOctNodeIterator( const MultiOctNodeIterator& rhs ) 
            : m_octNodePtrs(rhs.m_octNodePtrs), 
              m_currentElement(rhs.m_currentElement),
              m_endElement(rhs.m_endElement) {
            initNodePtrIterators( rhs );
        }

        /// As in the copy constructor, we have to be careful to correctly
        /// copy the iterators from m_octNodePtrs during assignment.
        MultiOctNodeIterator& operator=( const MultiOctNodeIterator& rhs ) {
            if ( &rhs == this ) return *this;

            m_octNodePtrs    = rhs.m_octNodePtrs;
            m_currentElement = rhs.m_currentElement;
            m_endElement     = rhs.m_endElement;
            initNodePtrIterators( rhs ); 

            return *this;
        }

        /// Advance the iterator. Seamlessly iterates over the contentID 
        /// elements of the OctNodes the iterator was created with. This 
        /// makes them appear as a single range to the user of the iterator,
        /// as if they were all in the same container.
        MultiOctNodeIterator& operator++() {      
            // preconditions:
            assert( m_currentNodePtr != m_endNodePtr );
            assert( m_currentElement != m_endElement );

            ++m_currentElement;
             // current node is exhausted, increment node iterator
            if ( m_currentElement == m_endElement ) {                
                ++m_currentNodePtr;
                if ( m_currentNodePtr != m_endNodePtr ) {
                    // more sanity checks:
                    assert( *m_currentNodePtr != NULL );
                    assert( !(*m_currentNodePtr)->contentIDs.empty() );
                    m_currentElement 
                        = (*m_currentNodePtr)->contentIDs.begin();
                    m_endElement     
                        = (*m_currentNodePtr)->contentIDs.end();
                }
            }

            return *this;
        }

        /// See prefix operator++ for details.
        MultiOctNodeIterator operator++( int ) {
            MultiOctNodeIterator result = *this;
            ++(*this);
            return result;
        }

        /// Elements access
        reference operator*() {
            return *m_currentElement;
        }

        /// Pointer access
        pointer operator->() {
            return &(*m_currentElement);
        }

        /// Equality test. Note that all end iterators are equivalent.
        bool operator==( const MultiOctNodeIterator& rhs ) const {            
            if ( m_currentNodePtr != m_endNodePtr ) {
                if ( rhs.m_currentNodePtr != rhs.m_endNodePtr ) {
                    return ( m_currentElement == rhs.m_currentElement );
                }
                else return false;
            }
            else {
                // *this is an end iterator, check if rhs is.
                return ( rhs.m_currentNodePtr == rhs.m_endNodePtr );
            }
        }

        /// Negation of operator==
        bool operator!=( const MultiOctNodeIterator& rhs ) const {
            return !(*this == rhs);
        }

    private:
        /// Set the initial iterator positions, assuming that m_octNodePtrs
        /// has been initialized.
        void initIterators() {
            m_currentNodePtr = m_octNodePtrs.begin();
            m_endNodePtr     = m_octNodePtrs.end();
            if ( m_currentNodePtr != m_endNodePtr ) {
                m_currentElement = (*m_currentNodePtr)->contentIDs.begin();
                m_endElement     = (*m_currentNodePtr)->contentIDs.end();
            }
        }

        /// Sets the nodePtr iterators over the vector in *this to match the 
        /// positions of the iterators in rhs over the vector in rhs.
        void initNodePtrIterators( const MultiOctNodeIterator& rhs ) {
            m_endNodePtr     = m_octNodePtrs.end();
            m_currentNodePtr = m_octNodePtrs.begin();
            typename OctNodeList::const_iterator rhsBegin 
                = rhs.m_octNodePtrs.begin();
            std::advance( m_currentNodePtr, 
                          std::distance<typename OctNodeList::const_iterator>
                            (rhsBegin, rhs.m_currentNodePtr) );            
        }
    }; // end MultiOctNodeIterator


    //-----------------------------------------------------------------------
    /// This is an adaptor for iterators over indices into another container.
    /// It is used in the Octree code to make the iterators over contentIDs 
    /// look like iterators over the actual elements. When the adaptor is 
    /// dereferenced, it returns the element in the container at the index 
    /// of the adapted iterator.
    //-----------------------------------------------------------------------
    template <typename IndexIterator, typename Container, bool isConst>
    class IndexIteratorAdaptor {
    private:
        typedef typename Container::value_type T;
        typedef typename ChooseType<isConst, 
                                    const Container*, 
                                    Container*>::type ContainerPtr;
        typedef typename ChooseType<isConst,
                                    const Container&,
                                    Container&>::type ContainerRef;
        typedef typename ChooseType<isConst, const T&, T&>::type ElementRef;

        // iterator state
        IndexIterator m_indexIter;
        ContainerPtr m_pContainer;

    public:
        // standard iterator typedefs
        typedef typename IndexIteratorAdaptor::T value_type;
        typedef std::ptrdiff_t difference_type;
        typedef std::forward_iterator_tag iterator_category;
        typedef typename ChooseType<isConst, const value_type&, 
                                             value_type&>::type reference;
        typedef typename ChooseType<isConst, const value_type*, 
                                             value_type*>::type pointer;

        /// Default constructor, does not produce a particularly useful
        /// iterator, but sometimes necessary as a placeholder.
        IndexIteratorAdaptor() 
            : m_pContainer( NULL ) {}

        /// Primary constructor, takes a iterator over container indices and
        /// the container the indices refer to.
        IndexIteratorAdaptor( const IndexIterator& local, 
                              ContainerRef c )
            : m_indexIter(local), m_pContainer(&c) {}

        /// Copy constructor.
        IndexIteratorAdaptor( const IndexIteratorAdaptor& rhs ) 
            : m_indexIter(rhs.m_indexIter), m_pContainer(rhs.m_pContainer) {}

        /// Assignment operator.
        IndexIteratorAdaptor& operator=( const IndexIteratorAdaptor& rhs ) {
            if ( &rhs == this ) return *this;

            m_pContainer = rhs.m_pContainer;
            m_indexIter  = rhs.m_indexIter;

            return *this;
        }

        /// Element access. Dereference the internal iterator to get the 
        /// index, then use it to get the element from the container.
        ElementRef operator*() {
            assert( m_pContainer != NULL );
            return (*m_pContainer)[*m_indexIter];
        }

        /// Advance the iterator, which simply advances the internal 
        /// iterator.
        IndexIteratorAdaptor& operator++() {
            ++m_indexIter;
            return *this;
        }

        /// Postfix form of advance.
        IndexIteratorAdaptor operator++( int ) {
            IndexIteratorAdaptor result( *this );
            ++(*this);
            return result;
        }

        /// Equality test passes if the adapted iterators test as equal, and
        /// the containers are the same.
        bool operator==( const IndexIteratorAdaptor& rhs ) const {
            return m_indexIter == rhs.m_indexIter
                    && m_pContainer == rhs.m_pContainer;
        }

        /// Negations of operator==.
        bool operator!=( const IndexIteratorAdaptor& rhs ) const {
            return !(*this == rhs);
        }

    }; // end class IndexIteratorAdaptor.

} // end namespace spatialdb

#endif
