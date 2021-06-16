// **********************************************************************************************************************
// 
// File:	    	TStackAllocIndex.h
// Created:	        31/08/2009
// Author:    		Michael Dailly
// Project:		    TemplateExample_V2
// Description:   	Simple templated version of the stack allocator using indices
// 
// Date				Version		BY		Comment
// ----------------------------------------------------------------------------------------------------------------------
// 31/08/2009		V1.0		MJD		1st version
// 
// **********************************************************************************************************************
#ifndef	__TSTACKALLOC_H__
#define	__TSTACKALLOC_H__


// #############################################################################################
/// Template:<summary>
///				Simple template showing basic object allocation
///          </summary>
// #############################################################################################
template <class T> class StackAllocIndex
{
public:
    // #############################################################################################
    /// Constructor: <summary>
    ///                Crate a new object stack allocator
    ///              </summary>
    ///
    /// In:		 <param name="_size">Number of indcies to allocate</param>
    ///
    // #############################################################################################
	StackAllocIndex(int _size)
	{
#ifdef	_DEBUG
		m_Size = _size;
#endif
		m_SP = 0;
		int s = sizeof(T);
		m_pStack = new T[_size];			// Create the stack

		// Now pre-fill the stack with the free list
		for(int i=0;i<_size;i++){
			Push( (T)i );
		}
	}
	

    // #############################################################################################
    /// Destructor: <summary>
    ///                Free everything.
    ///             </summary>
    // #############################################################################################
	~StackAllocIndex()
	{
		delete[] m_pStack;
	}


    // #############################################################################################
    /// Function:<summary>
    ///             Allocate a free object
    ///          </summary>
    ///
    /// Out:	 <returns>
    ///				The free object, or NULL for none left.
    ///			 </returns>
    // #############################################################################################
	T	Pop( void )
	{
		// Make sure the stack pointer hasn't been messed with (as much as is possible)
		assert( m_SP>=0 );
		assert( m_SP<=m_Size );

		if( m_SP ==0 ) return NULL;

#ifdef	_DEBUG
		// Using this debug code implys that you won't use the FULL size of the datatype
		// as -1 (0xff, 0xffff, 0xffff etc.) is used to flag an empty slot.
		T index = m_pStack[--m_SP];		// Get object to return
		m_pStack[m_SP] = (T)-1;			// Writing a -1 into the slot helps to make sure theres a valid object there.
		assert( index != (T)-1);		// If we ever try and return a -1, then the stack pointer is corrupted
		return index;					// return object
#else
		return m_pStack[--m_SP];		// in release mode, just return and decrement stack pointer.
#endif
	}

    // #############################################################################################
    /// Function:<summary>
    ///             Free an object
    ///          </summary>
    ///
    /// In:		 <param name="_index">Index to free</param>
    ///
    // #############################################################################################
	void	Push( T _index )
	{
		// Make sure the freed object came from our pool by checking its bounds.
		assert(	_index < m_Size );
		assert(	_index >= 0 );

		// And make sure we've not freed something twice!
		assert( m_SP>=0 );
		assert( m_SP<m_Size );
		
		// And free it
		m_pStack[m_SP++] = _index;
	}


private:
	int	m_SP;			// Stack pointer
	T*	m_pStack;		// array of pointers pointing to FREE objects

#ifdef	_DEBUG
	// Dont really need this in release mode...
	int	m_Size;
#endif
};

#endif	//__TSTACKALLOC_H__



