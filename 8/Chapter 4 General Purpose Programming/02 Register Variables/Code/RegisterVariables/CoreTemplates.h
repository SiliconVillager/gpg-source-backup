/*==================================================================================================================================*/
/** @name CoreTemplates.h 
*
*   DESCRIPTION: Defines core template types used by the engine.
*
*   @field File         CoreTemplates.h
*   @field Created      11/8/2006 11:30:00 PM
*************************************************************************************************************************************/

#pragma once

// ***********************************************************************************************************************************
// Type information.

// ***********************************************************************************************************************************
// TTypeInfoAtomicBase:
//		Base type information for atomic types which pass by value..
template<typename T> class TTypeInfoAtomicBase
{
public:
	typedef const T ConstInitType;
	enum { NeedsConstructor = 0	};
	enum { NeedsDestructor = 0	};
	enum { NeedsDirectCopy = 0 };
};

// ***********************************************************************************************************************************
// TTypeInfoConstructedBase:
//		Base type information for constructed types which pass by reference.
template<typename T> class TTypeInfoConstructedBase
{
public:
	typedef const T& ConstInitType;
	enum { NeedsConstructor = 1	};
	enum { NeedsDestructor  = 1	};
	enum { NeedsDirectCopy  = 0 };
};

// The default behavior is for types to behave as constructed types.
template<typename T> class TTypeInfo : public TTypeInfoConstructedBase<T>		{};

// C-style pointers require no construction.
template<typename T> class TTypeInfo<T*>: public TTypeInfoAtomicBase<T*>		{};

template <> class TTypeInfo<bool>		: public TTypeInfoAtomicBase<bool>		{};
template <> class TTypeInfo<BYTE>		: public TTypeInfoAtomicBase<BYTE>		{};
template <> class TTypeInfo<char>		: public TTypeInfoAtomicBase<char>		{};
template <> class TTypeInfo<int>		: public TTypeInfoAtomicBase<int>		{};
template <> class TTypeInfo<DWORD>		: public TTypeInfoAtomicBase<DWORD>		{};
template <> class TTypeInfo<WORD>		: public TTypeInfoAtomicBase<WORD>		{};
template <> class TTypeInfo<float>		: public TTypeInfoAtomicBase<float>		{};


template< class T > inline T Min( const T A, const T B )
{
	return (A<=B) ? A : B;
}

// Case insensitive string hash function. 
inline char appToUpper( char c )
{
	return (c<'a' || c>'z') ? (c) : (c+'A'-'a');
}
inline bool appIsDigit( char c )
{
	return c>='0' && c<='9';
}
inline DWORD appStrihash( const char* Data )
{
	DWORD Hash = 0;
	while (*Data)
	{
		char  Ch = appToUpper( *Data++ );
		BYTE  B  = Ch;
		Hash    += B;
		Hash     = ((Hash >> 8) & 0x00FFFFFF);
	}
	return Hash;
}

// ***********************************************************************************************************************************
// FMemoryAllocator:
//		Abstract base class for allowing memory allocation to be overwritten when dealing with TArrays.
class FMemoryAllocator
{
public:
	virtual bool IsDataConstant() { return false; }
	virtual void Free( void *InData )=0;
	virtual int Realloc( BYTE *&InData, int InNumElements, int InElementSize )=0;
};

// ***********************************************************************************************************************************
// TStaticAllocator:
//		Basic static memory allocation for TArrays.
template<class T, int MaxNumElements> class TStaticAllocator : public FMemoryAllocator
{
public:
	void Free( void *InData )
	{
	}
	int Realloc( BYTE *&InData, int InNumElements, int InElementSize )
	{
		InData = StaticData;
		return MaxNumElements;
	}
	T& GetInternalValue( int InIndex ) { assert( InIndex >= 0 && InIndex < MaxNumElements ); return *(T*)&StaticData[InIndex*sizeof(T)]; }
private:
	BYTE StaticData[sizeof(T)*MaxNumElements];
};

// ***********************************************************************************************************************************
// FBufferAllocator:
//		Basic buffer memory allocation for TArrays.
class FBufferAllocator : public FMemoryAllocator
{
public:
	FBufferAllocator( void *InData=NULL, int InSize=0, bool InBufferIsConstant=false )
		: BufferData(InData)
		, BufferSize(InSize)
		, InBufferIsConstant(false)
	{}
	void SetBuffer( void *InData, int InSize, bool InBufferIsConstant=false )
	{
		BufferData = InData;
		BufferSize = InSize;
	}
	bool IsDataConstant() 
	{ 
		return InBufferIsConstant; 
	}
	void Free( void *InData )
	{
	}
	int Realloc( BYTE *&InData, int InNumElements, int InElementSize )
	{
		InData = (BYTE*)BufferData;
		return BufferSize/InElementSize;
	}
private:
	void *BufferData;
	int BufferSize;
	bool InBufferIsConstant;
};

// ***********************************************************************************************************************************
// FArrayBase:
//		Base class for TArray, non-templated to allow for easy access when the array type isn't known.
class FArrayBase
{
public:
	FArrayBase()
		: Data(NULL)
		, ArrayNum(0)
		, ArrayMax(0)
		, Allocator(NULL)
		, IsDataConstantBuffer(false)
	{}
	~FArrayBase()
	{
		Empty( 0, 0 );
	}
	BYTE* GetByteData() const
	{
		return Data;
	}
	int Num() const
	{
		assert( ArrayNum >= 0 );
		assert( ArrayMax >= ArrayNum );
		return ArrayNum;
	}
	int Max() const
	{
		assert( ArrayNum >= 0 );
		assert( ArrayMax >= ArrayNum );
		return ArrayMax;
	}
	void SetMemoryAllocator( FMemoryAllocator* InAllocator, bool InIgnorePreviouslyAllocations=false )
	{
		assert( InIgnorePreviouslyAllocations || Data == NULL || Allocator == InAllocator );
		Allocator = InAllocator;
	}

	bool IsDataConstant()
	{
		return Allocator && Allocator->IsDataConstant();
	}
	void SetStaticData( BYTE* InData, int InNumElements, int InMaxElements, bool bEmptyFirst=true )
	{
		assert( InNumElements <= InMaxElements );
		if (bEmptyFirst)
		{
			Empty( 0, 0 );
		}
		IsDataConstantBuffer = true;
		ArrayNum = InNumElements;
		ArrayMax = InMaxElements;
		Data = InData;
	}
	void ClearStaticData( int InElementSize=0, bool InPreserveData=false )
	{
		if (IsDataConstantBuffer)
		{
			IsDataConstantBuffer = false;
			ArrayNum = 0;
			ArrayMax = 0;
			Data = NULL;
		}
	}
	
	void Empty( int Slack, int ElementSize )
	{
		ArrayNum = 0;
		if (Slack != ArrayMax)
		{
			Realloc( Slack, ElementSize, false );
		}
	}
	void Shrink( int ElementSize )
	{
		assert( ArrayNum >= 0);
		assert( ArrayMax >= ArrayNum );
		Realloc( ArrayNum, ElementSize, false );
	}
	bool Reserve( int Number, bool bRelative, int ElementSize )
	{
		if (bRelative)
		{
			Number += ArrayNum;
		}
		if (Number > ArrayMax)
		{
			return Realloc( Number, ElementSize, false );
		}
		return true;
	}
	int Add( int Count, int ElementSize )
	{
		assert( Count >= 0 );
		assert( ArrayNum >= 0 );
		assert( ArrayMax >= ArrayNum );

		// WARNING.  This routine can be dangerous if you have an Array of elements that require a constructor.
		int RequiredNum = ArrayNum+Count;
		if (RequiredNum > ArrayMax && !Realloc( RequiredNum, ElementSize, true ))
		{
			return INDEX_NONE;
		}
		const int Index = ArrayNum;
		ArrayNum = RequiredNum;
		return Index;
	}
	int AddZeroed( int Count, int ElementSize )
	{
		int Index = Add( Count, ElementSize );
		if (Index != INDEX_NONE)
		{
			memset( &Data[Index*ElementSize], 0, Count*ElementSize );
		}
		return Index;
	}
	int Insert( int Index, int ElementSize, int Count=1 )
	{
		assert( Count >= 0 );
		assert( ArrayNum >= 0 );
		assert( ArrayMax >= ArrayNum );
		assert( Index >= 0 );
		assert( Index <= ArrayNum );

		int FinalIndex = Add( Count, ElementSize );
		if (FinalIndex != INDEX_NONE)
		{
			memmove( &Data[(Count+Index)*ElementSize], &Data[Index*ElementSize], (FinalIndex-Index)*ElementSize );
			FinalIndex = Index;
		}
		return FinalIndex;
	}
	int InsertZeroed( int Index, int ElementSize, int Count=1 )
	{
		int FinalIndex = Insert( Index, ElementSize, Count );
		if (FinalIndex != INDEX_NONE)
		{
			memset( &Data[FinalIndex*ElementSize], 0, Count*ElementSize );
		}
		return FinalIndex;
	}
	void Remove( int Index, int Count, int ElementSize )
	{
		assert( Count >= 0 );
		assert( Index >= 0 );
		assert( Index <= ArrayNum );
		assert( Index+Count <= ArrayNum );

		if (ArrayNum - Index - Count > 0)
		{
			memmove( &Data[Index*ElementSize], &Data[(Index+Count)*ElementSize], (ArrayNum - Index - Count)*ElementSize );
		}
		ArrayNum -= Count;
	}

	// Swap two arrays by manipulating their data pointers.
	void Swap( FArrayBase& InArray )
	{
		FMemoryAllocator* TempAllocator = InArray.Allocator;
		BYTE* TempData = InArray.Data;
		int TempNum = InArray.ArrayNum;
		int TempMax = InArray.ArrayMax;

		InArray.Allocator = Allocator;
		InArray.Data = Data;
		InArray.ArrayNum = ArrayNum;
		InArray.ArrayMax = ArrayMax;

		Allocator = TempAllocator;
		Data = TempData;
		ArrayNum = TempNum;
		ArrayMax = TempMax;
	}
protected:
	bool Realloc( int RequiredSize, int ElementSize, bool bEvaluateMemUsage )
	{
		int NewMaxNumElements = ArrayMax;
		if (RequiredSize > ArrayMax)
		{
			NewMaxNumElements = bEvaluateMemUsage ? RequiredSize + 3*RequiredSize/8 + 32 : RequiredSize;
		}
		else if (RequiredSize < ArrayMax)
		{
			if (!bEvaluateMemUsage ||
				(3*ArrayNum < 2*ArrayMax || (ArrayMax-ArrayNum)*ElementSize >= 16384) && 
				(ArrayMax-ArrayNum > 64 || ArrayNum == 0))
			{
				NewMaxNumElements = RequiredSize;
			}
		}

		if (!IsDataConstantBuffer)
		{
			if (NewMaxNumElements == 0)
			{
				if (Allocator)
				{
					Allocator->Free( Data );
				}
				else
				{
					free( Data );
				}
				Data = NULL;
				ArrayMax = NewMaxNumElements;
			}
			else if (NewMaxNumElements != ArrayMax)
			{
				if (Allocator)
				{
					int NumElements = Allocator->Realloc( Data, NewMaxNumElements, ElementSize );
					if (NumElements != 0)
					{
						ArrayMax = NumElements;
					}
				}
				else
				{
					BYTE* NewMem = (BYTE*)realloc( Data, NewMaxNumElements*ElementSize );
					if (NewMem)
					{
						Data = NewMem;
						ArrayMax = NewMaxNumElements;
					}
				}
			}
		}
		if (ArrayNum > ArrayMax)
		{
			ArrayNum = ArrayMax;
		}
		return ArrayMax >= RequiredSize;
	}

	int	  ArrayNum;
	int	  ArrayMax;
	BYTE* Data;
	FMemoryAllocator* Allocator;
	bool IsDataConstantBuffer;
};

// ***********************************************************************************************************************************
// TArray:
//		Templated dynamic array.
template< class T > class TArray : public FArrayBase
{
public:

	TArray( int InNum=0 )
		: FArrayBase()
	{
		if (InNum > 0)
		{
			Reserve( InNum );
			Add( InNum );
		}
	}
	TArray( const TArray& Other )
		: FArrayBase()
	{
		Copy( Other );
	}
	~TArray()
	{
		assert( ArrayNum >= 0 );
		assert( ArrayMax >= ArrayNum );
		Empty();
	}
	bool IsValidIndex( int Index ) const
	{
		return Index >= 0 && Index < ArrayNum;
	}

	// Helper function returning the size of the inner type.
	DWORD GetTypeSize() const
	{
		return sizeof(T);
	}
	T* GetData()
	{
		return (T*)GetByteData();
	}
	const T* GetData() const
	{
		return (T*)GetByteData();
	}
	T& operator[]( int Index ) 
	{
		assert( Index >= 0 );
		assert( Index < ArrayNum ); 
		assert( ArrayMax >= ArrayNum );
		return *(T*)&FArrayBase::GetByteData()[Index*sizeof(T)];
	}
	const T& operator[]( int Index ) const
	{
		assert( Index >= 0 );
		assert( Index < ArrayNum ); 
		assert( ArrayMax >= ArrayNum );
		return *(T*)&FArrayBase::GetByteData()[Index*sizeof(T)];
	}
	void SetMemoryAllocator( FMemoryAllocator* InAllocator, bool InIgnorePreviouslyAllocations=false )
	{
		FArrayBase::SetMemoryAllocator( InAllocator, InIgnorePreviouslyAllocations );
	}
	T Pop()
	{
		assert( ArrayNum > 0 );
		assert( ArrayMax >= ArrayNum );
		T Result = (*this)[ArrayNum-1];
		Remove( ArrayNum-1 );
		return Result;
	}
	bool Push( const T& Item )
	{
		return AddItem( Item ) != INDEX_NONE;
	}
	T& Top()
	{
		return Last();
	}
	const T& Top() const
	{
		return Last();
	}
	T& Last( int Offset=0 )
	{
		assert( Offset >= 0 );
		assert( Offset < ArrayNum );
		assert( ArrayMax >= ArrayNum );
		return *(T*)&FArrayBase::GetByteData()[(ArrayNum-Offset-1)*sizeof(T)];
	}
	const T& Last( int Offset=0 ) const
	{
		assert( Offset >= 0 );
		assert( Offset < ArrayNum );
		assert( ArrayMax >= ArrayNum );
		return *(T*)&FArrayBase::GetByteData()[(ArrayNum-Offset-1)*sizeof(T)];
	}
	bool FindItem( const T& Item, int& Index ) const
	{
		Index = FindItemIndex( Item );
		return Index != INDEX_NONE;
	}
	int FindItemIndex( const T& Item ) const
	{
		for (int Index = 0; Index < ArrayNum; ++Index)
		{
			if ((*this)[Index] == Item)
			{
				return Index;
			}
		}
		return INDEX_NONE;
	}
	bool ContainsItem( const T& Item ) const
	{
		return FindItemIndex( Item ) != INDEX_NONE;
	}
	bool operator==( const TArray<T>& OtherArray ) const
	{
		if (Num() != OtherArray.Num())
		{
			return false;
		}
		for (int Index = 0; Index < Num(); ++Index)
		{
			if (!((*this)[Index] == OtherArray[Index]))
			{
				return false;
			}
		}
		return true;
	}
	bool operator!=( const TArray<T>& OtherArray ) const
	{
		return !((*this) == OtherArray);
	}

	// Add, Insert, Remove, Empty interface.
	int Add( int Count=1 )
	{
		return FArrayBase::Add( Count, sizeof(T) );
	}
	int AddZeroed( int Count=1 )
	{
		return FArrayBase::AddZeroed( Count, sizeof(T) );
	}
	int AddUsingConstructor( int Count=1 )
	{
		int iIndex = Add( Count );
		if (iIndex != INDEX_NONE && TTypeInfo<T>::NeedsConstructor)
		{
			for (int ii = 0; ii < Count; ++ii)
			{
				new( &(*this)[ii+iIndex] ) T;
			}
		}
		return iIndex;
	}
	int AddItem( const T& Item, int Count=1 )
	{
		int iStartingIndex = Add( Count );
		if (iStartingIndex != INDEX_NONE)
		{
			if (TTypeInfo<T>::NeedsConstructor)
			{
				// Construct each element.
				ArrayNum = iStartingIndex;
				for (int Index = 0; Index < Count; ++Index)
				{
					new(*this) T(Item);
				}
			}
			else
			{
				for (int Index = iStartingIndex; Index < ArrayNum; ++Index)
				{
					(*this)[Index] = Item;
				}
			}
		}
		return iStartingIndex;
	}
	int AddUniqueItem( const T& Item )
	{
		int Index = FindItemIndex( Item );
		return Index != INDEX_NONE ? Index : AddItem( Item );
	}
	int Insert( int Index, int Count=1 )
	{
		assert( Count >= 0 );
		assert( ArrayNum >= 0 );
		assert( ArrayMax >= ArrayNum );
		assert( Index >= 0 );
		assert( Index <= ArrayNum );

		int FinalIndex = Add( Count );
		if (FinalIndex != INDEX_NONE)
		{
			if (TTypeInfo<T>::NeedsDirectCopy)
			{
				for (int ii = ArrayNum-1; ii >= Index+Count; --ii)
				{
					(*this)[ii] = (*this)[ii-1]; 
				}
			}
			else if (FinalIndex-Index > 0)
			{
				memmove( &(*this)[Index+Count], &(*this)[Index], (FinalIndex-Index)*sizeof(T) );
			}
			FinalIndex = Index;
		}
		return FinalIndex;
	}
	int InsertZeroed( int Index, int Count=1 )
	{
		Index = Insert( Index, Count );
		if (Index != INDEX_NONE)
		{
			memset( &(*this)[Index], 0, Count*sizeof(T) );
		}
		return Index;
	}
	int InsertItem( const T& Item, int Index )
	{
		T* NewItem = new(*this, Index)T(Item);
		return NewItem ? Index : INDEX_NONE;
	}
	void Remove( int Index, int Count=1, bool InMaintainOrder=true )
	{
		assert( Count >= 0 );
		assert( Index >= 0 );
		assert( Index <= ArrayNum );
		assert( Index+Count <= ArrayNum );
		if (TTypeInfo<T>::NeedsDestructor)
		{
			for (int ii = Index; ii < Index+Count; ++ii)
			{
				(&(*this)[ii])->~T();
			}
		}

		if (TTypeInfo<T>::NeedsDirectCopy)
		{
			if (InMaintainOrder)
			{
				for (int ii = Index; ii < ArrayNum-1; ++ii)
				{
					(*this)[ii] = (*this)[ii+1]; 
				}
			}
			else
			{
				int iNumToMove = Min( Count, ArrayNum - Index - Count );
				for (int ii = 0; ii < iNumToMove; ++ii)
				{
					(*this)[Index++] = (*this)[ArrayNum-1-ii];
				}
			}
		}
		else if (ArrayNum - Index - Count > 0)
		{
			if (InMaintainOrder)
			{
				memmove( &(*this)[Index], &(*this)[Index+Count], (ArrayNum - Index - Count)*sizeof(T) );
			}
			else
			{
				int iNumToMove = Min( Count, ArrayNum - Index - Count );
				memmove( &(*this)[Index], &(*this)[ArrayNum-iNumToMove], iNumToMove*sizeof(T) );
			}
		}
		ArrayNum -= Count;
	}
	int RemoveItem( const T& Item, int InNumToRemove=MAX_INT, bool InMaintainOrder=true )
	{
		int OriginalNum = ArrayNum;
		for (int Index = 0; Index < ArrayNum && InNumToRemove > 0; ++Index)
		{
			if ((*this)[Index] == Item)
			{
				Remove( Index--, 1, InMaintainOrder );
				--InNumToRemove;
			}
		}
		return OriginalNum - ArrayNum;
	}
	void Empty( int Slack=0 )
	{
		if (TTypeInfo<T>::NeedsDestructor)
		{
			for (int ii = 0; ii < ArrayNum; ++ii)
			{
				(&(*this)[ii])->~T();
			}
		}
		FArrayBase::Empty( Slack, sizeof(T) );
	}
	void Reset()
	{
		Empty( ArrayMax );
	}
	void Shrink()
	{
		FArrayBase::Shrink( sizeof(T) );
	}

	// Appends the specified array to this array.  Cannot append to self.
	void Append( const TArray<T>& Source )
	{
		// Do nothing if the source and target match, or the source is empty.
		if (this != &Source && Source.Num() > 0)
		{
			// Allocate memory for the new elements.
			Reserve( ArrayNum + Source.ArrayNum );

			if (TTypeInfo<T>::NeedsConstructor || TTypeInfo<T>::NeedsDirectCopy)
			{
				// Construct each element.
				for (int Index = 0 ; Index < Source.ArrayNum ; ++Index)
				{
					::new(*this) T(Source[Index]) != NULL;
				}
			}
			else
			{
				// Do a bulk copy.
				memcpy( GetByteData(), Source.GetByteData(), sizeof(T) * Source.ArrayNum );
				ArrayNum += Source.ArrayNum;
			}
		}
	}

	// Swap two arrays by manipulating their data pointers.
	void Swap( TArray& InArray )
	{
		FArrayBase::Swap( InArray );
	}

	// Appends the specified array to this array.  Cannot append to self.
	TArray& operator+=( const TArray& Other )
	{
		Append( Other );
		return *this;
	}

	// Copies the source array into this one. Uses the common copy method.
	TArray& operator=( const TArray& Other )
	{
		Copy( Other );
		return *this;
	}

	// Reserves memory such that the array can contain at least Number elements.
	bool Reserve( int Number, bool bRelative=false )
	{
		return FArrayBase::Reserve( Number, bRelative, sizeof(T) );
	}

	// Iterator.
	class TIterator
	{
	public:
		TIterator() : Index(-1)							{ }
		TIterator( TArray<T>& InArray,bool InStartAtEnd=false ) : Array(InArray), Index(InStartAtEnd?Array.Num()-1:0), StartAtEnd(InStartAtEnd) {}
		void operator++()      							{ ++Index;												}
		void operator--()      							{ --Index;												}
		void RemoveCurrent( bool InMaintainOrder=true ){ Array.Remove(Index, 1, InMaintainOrder); if (StartAtEnd) ++Index; else --Index; }
		int GetIndex()   const 							{ return Index;											}
		operator bool() const 							{ return Index != -1 && Array.IsValidIndex(Index);		}
		T& operator*()   const 							{ return Array[Index];									}
		T* operator->()  const 							{ return &Array[Index];									}
		TIterator& operator=( const TIterator& It )		{ Array = It.Array; Index = It.Index; return *this;		}
		bool operator!=( const TIterator& It ) const	{ return &Array != &It.Array || Index != It.Index;		}
		bool operator==( const TIterator& It )	const	{ return &Array == &It.Array && Index == It.Index;		}
		T& GetPrev()     const { return Array[Index ? Index-1 : Array.Num()-1];		}
		T& GetNext()     const { return Array[Index<Array.Num()-1 ? Index+1 : 0];	}
	private:
		TArray<T>& Array;
		int Index;
		bool StartAtEnd;
	};

	// Iterator.
	class TConstIterator
	{
	public:
		TConstIterator() : Index(-1)							{ }
		TConstIterator( const TArray<T>& InArray,bool InStartAtEnd=false ) : Array(InArray), Index(InStartAtEnd?Array.Num()-1:0) {}
		void operator++()      									{ ++Index;												}
		void operator--()      									{ --Index;												}
		int GetIndex()   const 									{ return Index;											}
		operator bool() const 									{ return Index != -1 && Array.IsValidIndex(Index);		}
		const T& operator*()   const 							{ return Array[Index];									}
		const T* operator->()  const 							{ return &Array[Index];									}
		TConstIterator& operator=( const TConstIterator& It )	{ Array = It.Array; Index = It.Index; return *this;		}
		bool operator!=( const TConstIterator& It ) const		{ return &Array != &It.Array || Index != It.Index;		}
		bool operator==( const TConstIterator& It ) const		{ return &Array == &It.Array && Index == It.Index;		}
		const T& GetPrev()     const 							{ return Array[Index ? Index-1 : Array.Num()-1];		}
		const T& GetNext()     const 							{ return Array[Index<Array.Num()-1 ? Index+1 : 0];	}
	private:
		const TArray<T>& Array;
		int Index;
	};

protected:

	// Copies data from one array into this array. Uses the fast path if the data in question does not need a constructor.
	void Copy( const TArray<T>& Source )
	{
		if (this != &Source)
		{
			// Just empty our array if there is nothing to copy.
			if (Source.ArrayNum > 0)
			{
				// Pre-size the array so there are no extra allocs/memcpys.
				Empty( Source.ArrayNum );
				if (ArrayMax >= Source.ArrayNum)
				{
					// Determine whether we need per element construction or bulk.
					// copy is fine
					if (TTypeInfo<T>::NeedsConstructor || TTypeInfo<T>::NeedsDirectCopy)
					{
						// Use the in place new to copy the element to an array element.
						for (int Index = 0; Index < Source.ArrayNum; Index++)
						{
							new(*this) T(Source[Index]);
						}
					}
					else
					{
						// Use the much faster path for types that allow it.
						memcpy( GetByteData(), &Source[0], sizeof(T) * Source.ArrayNum );
						ArrayNum = Source.ArrayNum;
					}
				}
			}
			else
			{
				Empty();
			}
		}
	}
};

// ***********************************************************************************************************************************
// TArray new allocators:

template <class T> void* operator new( size_t Size, TArray<T>& Array )
{
	const int Index = Array.Add( 1 );
	return Index == INDEX_NONE ? NULL : &Array[Index];
}
template <class T> void* operator new( size_t Size, TArray<T>& Array, int Index )
{
	return Array.Insert( Index, 1 ) != INDEX_NONE ? &Array[Index] : NULL;
}
template <class T> void operator delete( void* InData, TArray<T>& Array )
{
	::delete InData;
}


// ***********************************************************************************************************************************
// TSparsePtrArray:
//		Sparse pointer container class used for quickly adding and removing pointers from a list.
template< class T, typename INDEXOR > class TSparsePtrArray : protected TArray<T*>
{
public:
	TSparsePtrArray( const INDEXOR& InIndexor=INDEXOR() )
		: m_Indexor(InIndexor)
		, iNumberOfElements(0)
	{}
	int Num() const
	{
		return TArray<T*>::Num();
	}
	int Max() const
	{
		return TArray<T*>::Max();
	}
	int NumActivePointers() const
	{
		return iNumberOfElements;
	}
	T* operator[]( int InIndex ) 
	{
		return TArray<T*>::operator[]( InIndex );
	}
	const T* operator[]( int InIndex ) const
	{
		return TArray<T*>::operator[]( InIndex );
	}
	bool IsValidIndex( int InIndex ) const
	{
		return TArray<T*>::IsValidIndex( InIndex );
	}
	bool Reserve( int InNumber, bool InRelative=false )
	{
		return TArray<T*>::Reserve( InNumber, InRelative );
	}
	void Empty( int InSlack=0, bool InClearIndexes=false )
	{
		if (InClearIndexes)
		{
			for (int ii = 0; ii < Num(); ++ii)
			{
				T* pPtr = TArray<T*>::operator[]( ii );
				if (pPtr)
				{
					m_Indexor.SetIndex( pPtr, INDEX_NONE );
				}
			}
		}
		iNumberOfElements = 0;
		TArray<T*>::Empty( InSlack );
		AvailableIndexes.Empty( Min( AvailableIndexes.Max(), InSlack ) );
	}
	void SetMemoryAllocator( FMemoryAllocator* InAllocator, bool InIgnorePreviouslyAllocations=false )
	{
		TArray<T*>::SetMemoryAllocator( InAllocator, InIgnorePreviouslyAllocations );
	}
	bool AddItem( T* InPointer, bool InUseEmptySlots=true )
	{
		assert( InPointer );
		assert( m_Indexor.GetIndex( InPointer ) == INDEX_NONE );
		assert( iNumberOfElements >= 0 && iNumberOfElements <= Num() );

		if (InUseEmptySlots && AvailableIndexes.Num() > 0)
		{
			int iIndex = AvailableIndexes.Pop();
			assert( (*this)[iIndex] == NULL );
			TArray<T*>::operator[]( iIndex ) = InPointer;
			m_Indexor.SetIndex( InPointer, iIndex );
			++iNumberOfElements;
			return true;
		}
		else
		{
			int iIndex = TArray<T*>::AddItem( InPointer );
			if (iIndex != INDEX_NONE)
			{
				m_Indexor.SetIndex( InPointer, iIndex );
				++iNumberOfElements;
				return true;
			}
		}
		return false;
	}
	bool InsertItem( T* InPointer, int InIndex, int InMaxIndex=INDEX_NONE  )
	{
		assert( InPointer );
		assert( m_Indexor.GetIndex( InPointer ) == INDEX_NONE );
		assert( iNumberOfElements >= 0 && iNumberOfElements <= Num() );
		assert( InIndex <= Num() );
		if (InMaxIndex == INDEX_NONE)
		{
			InMaxIndex = InIndex;
		}
		assert( InMaxIndex >= InIndex );

		int iIndex = INDEX_NONE;
		int iMinAvailableIndex = Num();
		for (int ii = AvailableIndexes.Num()-1; ii >= 0; --ii)
		{
			int iAvailableIndex = AvailableIndexes[ii];
			if (iAvailableIndex >= InIndex && iAvailableIndex <= InMaxIndex)
			{
				iIndex = iAvailableIndex;
				AvailableIndexes.Remove( ii );
				break;
			}
			else if (iAvailableIndex > InIndex && iAvailableIndex < iMinAvailableIndex)
			{
				iMinAvailableIndex = iAvailableIndex;
			}
		}
		if (iIndex == INDEX_NONE)
		{
			if (iMinAvailableIndex < Num())
			{
				assert( (*this)[iMinAvailableIndex] == NULL );
				iIndex = InIndex;
				AvailableIndexes.RemoveItem( iMinAvailableIndex );
				for (int ii = iMinAvailableIndex-1; ii >= iIndex; --ii)
				{
					T* pPtr = TArray<T*>::operator[]( ii );
					assert( pPtr );
					TArray<T*>::operator[]( ii+1 ) = pPtr;
					m_Indexor.SetIndex( pPtr, ii+1 );
				}
				TArray<T*>::operator[]( iIndex ) = NULL;
			}
			else
			{
				iIndex = TArray<T*>::InsertZeroed( InIndex );
				if (iIndex != INDEX_NONE)
				{
					for (int ii = iIndex+1; ii < Num(); ++ii)
					{
						T* pPtr = TArray<T*>::operator[]( ii );
						assert( pPtr );
						m_Indexor.SetIndex( pPtr, ii );
					}
				}
			}
		}
		if (iIndex != INDEX_NONE)
		{
			assert( (*this)[iIndex] == NULL );
			TArray<T*>::operator[]( iIndex ) = InPointer;
			m_Indexor.SetIndex( InPointer, iIndex );
			++iNumberOfElements;
			return true;
		}
		return false;
	}
	void RemoveItem( T* InPointer )
	{
		assert( InPointer );
		if (m_Indexor.GetIndex( InPointer ) != INDEX_NONE)
		{
			assert( (*this)[m_Indexor.GetIndex( InPointer )] == InPointer );
			assert( iNumberOfElements >= 0 && iNumberOfElements <= Num() );
			TArray<T*>::operator[]( m_Indexor.GetIndex( InPointer )) = NULL;
			AvailableIndexes.Push( m_Indexor.GetIndex( InPointer ) );
			m_Indexor.SetIndex( InPointer, INDEX_NONE );
			--iNumberOfElements;
		}
	}
	float GetDensity()
	{
		if (Num() == 0)
		{
			return 1.f;
		}
		else
		{
			return 1.f - AvailableIndexes.Num() / Num();
		}
	}
	void Collapse()
	{
		int iIndex = 0;
		AvailableIndexes.Reset();
		for (int ii = 0; ii < Num(); ++ii)
		{
			T* pPtr = TArray<T*>::operator[]( ii );
			if (pPtr)
			{
				TArray<T*>::operator[]( ii ) = NULL;
				TArray<T*>::operator[]( iIndex ) = pPtr;
				m_Indexor.SetIndex( pPtr, iIndex );
				++iIndex;
			}
		}
		for (int ii = iIndex; ii < Num(); ++ii)
		{
			AvailableIndexes.AddItem( ii );
		}
	}
	const TArray<T*>& GetArray() const
	{
		return *this;
	}

	// Iterators.
	class TIterator
	{
	public:
		TIterator() : Index(INDEX_NONE) {}
		TIterator( TSparsePtrArray<T,INDEXOR>& InContainer ) : Container(InContainer), Index(INDEX_NONE) { ++(*this); }
		void operator++()      							{ do {++Index;}while (Index < Container.Num() && Container[Index] == NULL); }
		void operator--()      							{ do {--Index;}while (Index < Container.Num() && Container[Index] == NULL); }
		void RemoveCurrent()   							{ Container.RemoveItem(Container[Index]);						}
		operator bool() const 							{ return Container.IsValidIndex(Index);							}
		T* operator*()   const 							{ return Container[Index];										}
		T* operator->()  const 							{ return Container[Index];										}
		TIterator& operator=( const TIterator& It )		{ Container = It.Container; Index = It.Index; return *this;		}
		bool operator!=( const TIterator& It ) const	{ return &Container != &It.Container || Index != It.Index;		}
		bool operator==( const TIterator& It )	const	{ return &Container == &It.Container && Index == It.Index;		}
	private:
		TSparsePtrArray<T,INDEXOR>& Container;
		int Index;
	};
	class TConstIterator
	{
	public:
		TConstIterator() : Index(INDEX_NONE) {}
		TConstIterator( const TSparsePtrArray<T,INDEXOR>& InContainer ) : Container(InContainer), Index(INDEX_NONE) { ++(*this); }
		void operator++()      							{ do {++Index;}while( Container.IsValidIndex(Index) && Container[Index] == NULL ); }
		void operator--()      							{ do {--Index;}while( Container.IsValidIndex(Index) && Container[Index] == NULL ); }
		operator bool() const 							{ return Container.IsValidIndex(Index);							}
		const T* operator*()   const 					{ return Container[Index];										}
		const T* operator->()  const 					{ return Container[Index];										}
		TConstIterator& operator=( const TConstIterator& It ){ Container = It.Container; Index = It.Index; return *this;}
		bool operator!=( const TIterator& It ) const	{ return &Container != &It.Container || Index != It.Index;		}
		bool operator==( const TIterator& It )	const	{ return &Container == &It.Container && Index == It.Index;		}
	private:
		const TSparsePtrArray<T,INDEXOR>& Container;
		int Index;
	};

protected:
	struct CompareINT
	{
		static int Compare( const int& InA, const int& InB )
		{
			return InB - InA;
		};
	};
	const INDEXOR& m_Indexor;
	TArray<int> AvailableIndexes;
	int iNumberOfElements;
};

// EOF
/*==================================================================================================================================*/
