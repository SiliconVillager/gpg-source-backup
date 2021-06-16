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
#ifndef GPG8_FIXED_ARRAY_HPP
#define GPG8_FIXED_ARRAY_HPP 1

#include "CompileTimeChecker.hpp"
#include "ChooseType.hpp"
#include <iterator>
#include <stdexcept>
#include <memory>
#include <cassert>

namespace gpg8_util {
    //-----------------------------------------------------------------------
    /// A dynamically allocated, fixed-sized array. Use reserve() to force 
    /// allocation of storage, and clear() to force deallocation. Designed
    /// to minimize storage overhead when empty. This container implements
    /// the most commonly useful functions from vector, which it is intended
    /// to replace in circumstances where minimal empty size and fixed max
    /// size are reasonable tradeoffs.
    ///
    /// Note: FixedArray does not implement the full STL container or 
    /// vector requirements, just enough to support the OctNodes.
    //-----------------------------------------------------------------------
    template <typename T, size_t MAX_SIZE_PARAM, 
              typename Allocator = std::allocator<T>,
              unsigned int SENTINEL_VALUE = 0xDEADBEEF>
    class FixedArray {
    public:       
        static const size_t MAX_SIZE = MAX_SIZE_PARAM;

        // standard container typedefs
        typedef T         value_type;
        typedef T&        reference;
        typedef const T&  const_reference;
        typedef size_t    size_type;
        typedef ptrdiff_t difference_type;

        //-------------------------------------------------------------------
        /// Custom iterator for this class. Since the const and non-const 
        /// iterator is almost the same, we can make the iterator a template 
        /// class and typedef it later to generate both types.
        //-------------------------------------------------------------------
        template <bool isConst = false>
        class FixedArrayIterator 
        {
        private:
            // although technically an unrelated class, being friendly with
            // the opposite iterator type makes conversions simpler.
            friend class FixedArrayIterator<!isConst>;
            // also be friendly with the container
            friend class FixedArray;
    
            // Strictly speaking, the container does not have to be const for
            // a const_iterator, just the elements that the iterator returns
            // need to be const.  In practice however, the container 
            // functions that return const_iterators are const themselves, so
            // they pass the container in as a const.
            typedef typename ChooseType<isConst, const FixedArray*, 
                                        FixedArray*>::type ContainerPtr;
            typedef typename ChooseType<isConst, const FixedArray&, 
                                        FixedArray&>::type ContainerRef;

            // we *require* a pointer rather than a reference to support
            // iterator assignment, which can change the container after
            // initialization.
            ContainerPtr m_pContainer;
            size_t m_pos;

        public: 
            // standard library iterator traits...
            typedef typename FixedArray::value_type value_type;
            typedef std::ptrdiff_t                  difference_type;
            typedef std::forward_iterator_tag       iterator_category;
            typedef typename ChooseType<isConst, 
                                        const T*, T*>::type pointer;
            typedef typename ChooseType<isConst, 
                                        const T&, T&>::type reference;

            //---------------------------------------------------------------
            /// Default constructor, does not produce a valid iterator
            //---------------------------------------------------------------
            FixedArrayIterator() 
                : m_pContainer(NULL), m_pos(0) {};

            //---------------------------------------------------------------
            /// This is the standard constructor. Supply a container, and the
            /// initial position in the container. The resulting iterator 
            /// will point to the first element in the container at or after
            /// the position supplied.
            //---------------------------------------------------------------
            FixedArrayIterator( ContainerRef c, size_t initPos )
                : m_pContainer(&c), m_pos(initPos) {
                assert( m_pContainer != NULL );   
                if ( m_pContainer->m_pElements == NULL ) {
                    m_pos = MAX_SIZE;
                }
                else if ( m_pos < MAX_SIZE && 
                            _isSentinel((*m_pContainer)[m_pos]) ) {
                    // advance to first real element, or end()                  
                    ++(*this);
                }
            }
    
            //---------------------------------------------------------------
            /// Convenience constructor, initializes itself to the front of
            /// the container.
            //---------------------------------------------------------------
            explicit FixedArrayIterator( ContainerRef c ) {
                FixedArrayIterator( c, 0 );
            }
        
            //---------------------------------------------------------------
            /// Conversion from a non-const iterator, which is the copy
            /// constructor if this is a non-const iterator too.
            //---------------------------------------------------------------
            FixedArrayIterator( const FixedArrayIterator<false>& rhs )
                : m_pContainer(rhs.m_pContainer), m_pos(rhs.m_pos) {
            }

            //---------------------------------------------------------------
            /// Assignment from another iterator
            //---------------------------------------------------------------
            FixedArrayIterator& operator=( const FixedArrayIterator& rhs ) {
                if ( this == &rhs ) return *this;

                m_pContainer = rhs.m_pContainer;
                m_pos = rhs.m_pos;

                return *this;
            }

            //---------------------------------------------------------------
            /// Current element accessor (unchecked)
            //---------------------------------------------------------------
            reference operator*() const {                
                return (*m_pContainer)[m_pos];
            }
    
            //---------------------------------------------------------------
            /// Address of current element accessor (unchecked)
            //---------------------------------------------------------------
            pointer operator->() const {
                return &((*m_pContainer)[m_pos]);
            }
            
            //---------------------------------------------------------------
            /// Prefix increment
            //---------------------------------------------------------------
            FixedArrayIterator& operator++() {                
                do {
                    assert( m_pos < MAX_SIZE );
                    // don't strictly need to check if m_pos == MAX_SIZE
                    // because incrementing end() is undefined.
                    ++m_pos;
                    if ( !_isSentinel((*m_pContainer)[m_pos]) ) break;
                } while ( m_pos < FixedArray::MAX_SIZE );
                return *this;
            }
            
            //---------------------------------------------------------------
            /// Postfix increment
            //---------------------------------------------------------------
            FixedArrayIterator operator++( int ) {
                FixedArrayIterator preIncr( *this );
                ++(*this);
                return preIncr;
            }
    
            //---------------------------------------------------------------
            /// Prefix decrement
            //---------------------------------------------------------------
            FixedArrayIterator& operator--() {
                do {
                    --m_pos;
                    if ( !_isSentinel((*m_pContainer)[m_pos]) ) break;
                } while ( m_pos > 0 );
                return *this;
            }
    
            //---------------------------------------------------------------
            /// Postfix decrement
            //---------------------------------------------------------------
            FixedArrayIterator operator--( int ) {
                FixedArrayIterator preDecr;
                --(*this);
                return preDecr;
            }
            
            //---------------------------------------------------------------
            /// Equality test - if both iterators point to the same position
            /// in the same underlying container, they are equivalent.
            //---------------------------------------------------------------
            bool operator==( const FixedArrayIterator& rhs ) const {
                return rhs.m_pContainer == m_pContainer 
                           && rhs.m_pos == m_pos;
            }
            
            //---------------------------------------------------------------
            /// Negates the equality test (above)
            //---------------------------------------------------------------
            bool operator!=( const FixedArrayIterator& rhs ) const {
                return !((*this) == rhs);
            }
            
            //---------------------------------------------------------------
            /// Return the distance in elements between two iterators. This 
            /// is part of the Random Access Iterator spec, which this
            /// class doesn't fully implement. Nonetheless, it's handy to 
            /// have.
            //---------------------------------------------------------------
            difference_type operator-( const FixedArrayIterator& rhs ) const{
                assert( m_pContainer == rhs.m_pContainer );
                difference_type distance = 0;
                for ( size_t i = rhs.m_pos; i < m_pos; ++i ) {
                    if ( !_isSentinel((*m_pContainer)[i]) ) ++distance;
                }
                return distance;
            }

        }; // end class FixedArrayIterator

        // iterator types
        typedef FixedArrayIterator<false>             iterator;
        typedef FixedArrayIterator<true>              const_iterator;
        typedef std::reverse_iterator<iterator>       reverse_iterator;
        typedef std::reverse_iterator<const_iterator> const_reverse_iterator;

        //-------------------------------------------------------------------
        /// default constructor
        //-------------------------------------------------------------------
        FixedArray()
            : m_pElements( NULL ) {}
        
        //-------------------------------------------------------------------
        /// copy constructor
        //-------------------------------------------------------------------
        FixedArray( const FixedArray& rhs )
            : m_pElements( NULL ), alloc( rhs.alloc ) { 
            if ( rhs.m_pElements ) {
                reserve();
                size_t idx = 0;
                for ( const_iterator i = rhs.begin(); i != rhs.end(); ++i ) {
                    _assign( *i, idx++ );
                }
            }
        }
 
        //-------------------------------------------------------------------
        /// Constructor for user-specified allocator. Useful when working 
        /// with allocators that need some configuration beyond template
        /// params before use.
        //-------------------------------------------------------------------
        explicit FixedArray( const Allocator& userAlloc )
            : m_pElements( NULL ), alloc( userAlloc ) {}

        //-------------------------------------------------------------------
        // destructor
        //-------------------------------------------------------------------
        ~FixedArray() {
            clear();
        }

        //-------------------------------------------------------------------
        /// Assignment operator. 
        //-------------------------------------------------------------------
        FixedArray& operator=( const FixedArray& rhs ) { 
            clear();
            if ( rhs.m_pElements != NULL ) reserve();
            int i = 0;
            for (const_iterator pos = rhs.begin(); pos != rhs.end(); ++pos) {
                _assign( *pos, i++ );
            }
            return *this;
        }

        //-------------------------------------------------------------------
        /// Can perform an efficient swap by simply swapping internal 
        /// pointers.
        //-------------------------------------------------------------------
        void swap( FixedArray& rhs ) {
            T* pTemp = m_pElements;
            m_pElements = rhs.m_pElements;
            rhs.m_pElements = pTemp;
        }

        // begin iterators
        iterator begin() { return iterator( *this, 0 ); } 
        const_iterator begin() const { return const_iterator( *this, 0 ); }
        reverse_iterator rbegin() { return reverse_iterator( end() ); }
        const_reverse_iterator rbegin() const { 
            return const_reverse_iterator( end() );
        }

        // end iterators
        iterator end() { return iterator( *this, MAX_SIZE ); }
        const_iterator end() const { return const_iterator(*this,MAX_SIZE); }
        reverse_iterator rend() { return reverse_iterator( begin() ); }
        const_reverse_iterator rend() const {
            return const_reverse_iterator( begin() );
        }


        ///////////////////////// ELEMENT ACCESS ////////////////////////////
        
        // access the first element in the container
        reference front() { return *begin(); }
        const_reference front() const { return front(); }

        // access the last element in the container
        reference back() { return *( --end() ); }
        const_reference back() const { return back(); }

        // unchecked random element access
        reference operator[]( size_t i ) { return m_pElements[i]; }
        const_reference operator[](size_t i) const { return m_pElements[i]; }

        //-------------------------------------------------------------------
        /// checked random element access
        //-------------------------------------------------------------------
        reference at( size_t i ) {
            if ( m_pElements == NULL 
                    || i < 0 || i >= MAX_SIZE
                || _isSentinel(m_pElements[i]) ) {
                throw std::range_error("invalid FixedArray element access");
            }
            // else the element is valid.
            return m_pElements[i];
        }

        // insert is really inefficient in this kind of container, so we're 
        // not implementing it.

        //-------------------------------------------------------------------
        /// Random element access is actually a more efficient way to add 
        /// elements to this container, if you know the index you want to 
        /// store at.
        //-------------------------------------------------------------------
        void push_back( const T& t ) {
            reserve();      
            int i = size(); 
            _assign( t, i );
        }


        //////////////////////////// DELETION ///////////////////////////////

        //-------------------------------------------------------------------
        /// Remove the elements at p, returns the next element in the 
        /// container. (unchecked)
        //-------------------------------------------------------------------
        iterator erase( iterator p ) {
            return _erase( p.m_pos );
        }

        //-------------------------------------------------------------------
        /// Removes the elements in the range [p,q). (unchecked)
        //-------------------------------------------------------------------
        iterator erase( iterator p, iterator q ) {
            while ( p != q ) {
                erase( p );
                ++p;
            }
            return p;
        }
            
        //-------------------------------------------------------------------
        /// Removes the last element in the container. (unchecked)
        //-------------------------------------------------------------------
        void pop_back() {            
            erase( --end() );
        }
    

        ////////////////////// CONTAINER SIZING /////////////////////////////

        //-------------------------------------------------------------------
        /// Actual number of elements in the container. This is expensive,
        /// because we have to actually count the number of elements in the
        /// storage array.
        //-------------------------------------------------------------------
        size_type size() const { return end() - begin(); }        

        //-------------------------------------------------------------------
        /// Faster than size() == 0 for cleared containers.
        //-------------------------------------------------------------------
        bool empty() const { 
            return m_pElements == NULL || size() == 0;                 
        }
        
        //-------------------------------------------------------------------
        /// Max number of elements this container can hold
        //-------------------------------------------------------------------
        size_type max_size() const { return MAX_SIZE; }

        //-------------------------------------------------------------------
        /// Max elements that won't trigger an allocation. Note that since
        /// the size of the container is fixed, the only allocation happens
        /// going from empty to non-empty.
        //-------------------------------------------------------------------
        size_type capacity() const { 
            return m_pElements == NULL ? 0 : MAX_SIZE; 
        }
        
        //-------------------------------------------------------------------
        /// Triggers allocation of the fixed size storage array. This is a
        /// no-op if the array has already been allocated. Note that 
        /// requested size is irrelevant, since this class uses fixed sized
        /// storage.
        //-------------------------------------------------------------------
        void reserve( size_t requested = MAX_SIZE ) {
            assert( requested <= MAX_SIZE ); (void)requested;
            if ( m_pElements == NULL ) {
                // allocate raw storage
                m_pElements = alloc.allocate(MAX_SIZE);
                // NOTE: this only works correctly when sizeof(T) is
                // big enough to be overwritten with the sentinel value
                STATIC_CHECK( sizeof(T) >= sizeof(Sentinel),
                              Element_Type_Too_Small );
                for ( unsigned int i = 0; i < MAX_SIZE; ++i ) {
                    // init all unused elements with the sentinel value
                    Sentinel* pRaw = (Sentinel*)&m_pElements[i];
                    *pRaw = SENTINEL_VALUE;
                }
            }
            // else no-op
        }
        
        //-------------------------------------------------------------------
        /// Removes all elements and deallocates space, making this 
        /// container very small.
        //-------------------------------------------------------------------
        void clear() {
            if ( m_pElements ) {
                for ( unsigned int i = 0; i < MAX_SIZE; ++i ) {
                    _erase( i );
                }   
                alloc.deallocate( m_pElements, MAX_SIZE );
                m_pElements = NULL;
            }
        }

        //-------------------------------------------------------------------
        // because this container's capacity is fixed at 0 or MAX_SIZE, this
        // is a no-op
        //-------------------------------------------------------------------
        void resize( size_t, const T& ) {
            return;
        }
        
        //-------------------------------------------------------------------
        /// Container contents equivalent.
        //-------------------------------------------------------------------
        bool operator==( const FixedArray& rhs ) const {
            const_iterator lhsIter = begin();
            const_iterator lhsEnd  = end();
            const_iterator rhsIter = rhs.begin();
            const_iterator rhsEnd  = rhs.end();
            while ( lhsIter != lhsEnd && rhsIter != rhsEnd ) {
                if ( *lhsIter != *rhsIter ) return false;
                ++lhsIter;
                ++rhsIter;
            }
            // check that we've examined all of the elements...
            return lhsIter == lhsEnd && rhsIter == rhsEnd;
        }

        //-------------------------------------------------------------------
        /// Contents not equivalent.
        //-------------------------------------------------------------------
        bool operator!=( const FixedArray& rhs ) const {
            return !(*this == rhs);
        }
    
        // We're omitting < > <= >= operators because they're not terribly
        // useful for this application.

    private:
        // We do some trickery in this container by overwriting the first 
        // sizeof(Sentinel) bytes of each element in m_pElements
        // with a sentinel value, so that we can detect where the
        // end of the container is without storing an explicit counter.
        //
        // There are two notable cases where this can fail:
        //
        // 1. If the value of a legitimate object matches the sentinel 
        //    value, the container will mistakenly treat it as non-existant.
        //
        // 2. If sizeof(T) is less than sizeof(Sentinel). This is checked
        //    at compile time with STATIC_CHECK.
        typedef unsigned int Sentinel;
        T* m_pElements; 

        Allocator alloc;

        //-------------------------------------------------------------------
        /// Encapsulates the logic for testing whether an element is actually
        /// a sentinel, and therefore uninitialized.
        //-------------------------------------------------------------------
        static inline bool _isSentinel( const T& t ) {
            return *((Sentinel*)&t) == SENTINEL_VALUE;
        }

        //-------------------------------------------------------------------
        /// Overwrites the element at idx with the value of t, performing
        /// a placement new constructor call if the element at idx is 
        /// currently uninitialized.
        //-------------------------------------------------------------------
        void _assign( const T& t, size_t idx ) {
            if ( _isSentinel(m_pElements[idx]) ) {
                // copy construct uninitialized array elements
                alloc.construct( &m_pElements[idx], t );
            }
            else {
                m_pElements[idx] = t;
            }
        }                         

        //-------------------------------------------------------------------
        /// Destroys the element at the specified index, and returns an 
        /// iterator pointing to the next valid element in the container, or 
        /// the end iterator if this was the last element.
        //-------------------------------------------------------------------
        iterator _erase( int idx ) {
            if ( !_isSentinel( m_pElements[idx] ) ) {
                alloc.destroy( &m_pElements[idx] );
                Sentinel* pRaw 
                    = reinterpret_cast<Sentinel*>( &m_pElements[idx] );
                *pRaw = SENTINEL_VALUE;
            }
            return iterator(*this, idx);
        }

    }; // end class FixedArray

} // end namespace gpg8_util

#endif
