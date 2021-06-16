/*==================================================================================================================================*/
/** @name FRegisteredVar.h 
*
*   DESCRIPTION: Registered Variables are used to pass values between objects without the object be required to know about one another.
*
*   @field File         FRegisteredVar.h
*   @field Created      9/2/2008 5:57:29 PM
*************************************************************************************************************************************/

#pragma once

#define DECLARE_REGISTERED_VARIABLE( InClass, InBaseClass )				\
	DECLARE_FNAME_CLASS_TYPESAFETY( InClass, InBaseClass )		
#define DECLARE_BASEREGISTERED_VARIABLE( InClass )						\
	DECLARE_FNAME_BASECLASS_TYPESAFETY( InClass )		


// ***********************************************************************************************************************************
// FRegisteredVar:
//		Base class for all registered variables.
class FRegisteredVar
{
	DECLARE_BASEREGISTERED_VARIABLE( FRegisteredVar );

	FRegisteredVar();
	virtual ~FRegisteredVar();

	void SetDirtyCallback( void(*InCallback)( FRegisteredVar& ) );
	bool IsDirty( bool InCheckParent=false ) const;
	void SetDirty( bool InDirty, bool InRecurse=false );
	void SetRedirector( FRegisteredVar* InRedir, bool InAllowCopy=true );
	bool IsRegistered( FRegisteredVar* InRedir );
	bool IsRedirected() const;
	void SetFName( FName InName );
	FName GetFName() const;
	
protected:
	template<class T> T* GetBaseVariable() const
	{
		if (m_pRedirector)
		{
			return m_pRedirector->GetBaseVariable<T>();
		}
		return (T*)this;
	}
	virtual FName GetBaseVariableType() const ;
	virtual void SetRedirectorInternal( FRegisteredVar* InRedir, bool InAllowCopy );
	void AddReference( FRegisteredVar* InParent );
	virtual void RemoveReference( FRegisteredVar* InParent );

	FName m_Name;
	bool m_bDirty;
	FRegisteredVar* m_pRedirector;
	TArray<FRegisteredVar*> m_References;
	void(*m_DirtyCallback)( FRegisteredVar& );
};

template<class T, class RegVarType> class TRegisteredVarType : public FRegisteredVar
{
public:
	TRegisteredVarType()
	{}

	T Get()											{ return GetBaseVariable<RegVarType>()->m_Value; }
	const T& Get() const							{ return GetBaseVariable<RegVarType>()->m_Value; }
	void Set( const T& InValue )					{ GetBaseVariable<RegVarType>()->SetDirectly( InValue ); }

	T& LocalValue()									{ return m_Value; }

	operator T() const								{ return GetBaseVariable<RegVarType>()->m_Value; }
	operator T&() 									{ return GetBaseVariable<RegVarType>()->m_Value; }

	void operator=( const RegVarType& InStruct )	{ Set( InStruct.Get() ); }

	bool operator>( const T& InValue ) const 		{ return Get() > InValue; }
	bool operator<( const T& InValue ) const 		{ return Get() < InValue; }
	bool operator>=( const T& InValue ) const 		{ return Get() >= InValue; }
	bool operator<=( const T& InValue ) const 		{ return Get() <= InValue; }
	bool operator==( const T& InValue ) const 		{ return Get() == InValue; }
	bool operator!=( const T& InValue ) const 		{ return Get() != InValue; }

	T operator/( const T& InValue ) const 			{ return Get() / InValue; }
	T operator*( const T& InValue ) const 			{ return Get() * InValue; }
	T operator+( const T& InValue ) const 			{ return Get() + InValue; }
	T operator-( const T& InValue ) const 			{ return Get() - InValue; }

	RegVarType& operator/=( const T& InValue )		{ Set( Get() / InValue ); return *(RegVarType*)this; }
	RegVarType& operator*=( const T& InValue )		{ Set( Get() * InValue ); return *(RegVarType*)this; }
	RegVarType& operator+=( const T& InValue )		{ Set( Get() + InValue ); return *(RegVarType*)this; }
	RegVarType& operator-=( const T& InValue )		{ Set( Get() - InValue ); return *(RegVarType*)this; }

protected:
	virtual FName GetBaseVariableType() const
	{ 
		return RegVarType::StaticGetClassType();
	}
	void SetDirectly( const T& InValue )
	{
		if (m_Value != InValue)
		{
			m_Value = InValue;
			SetDirty( true );
		}
		for (int ii = 0; ii < m_References.Num(); ++ii)
		{
			((RegVarType*)m_References[ii])->SetDirectly( InValue );
		}
	}
	virtual void SetRedirectorInternal( FRegisteredVar* InRedir, bool InAllowCopy )
	{
		FRegisteredVar::SetRedirectorInternal( InRedir, InAllowCopy );
		m_Value = Get();
	}
	T m_Value;
};

// ***********************************************************************************************************************************
// FRegisteredVarArray:
//		First functions to standardize some general functionality utilized by all array types (remove if nothing is added to this)
template< class T, class RegVarType > class TRegisteredVarArray : public FRegisteredVar
{
	DECLARE_REGISTERED_VARIABLE( TRegisteredVarArray, FRegisteredVar );
	TRegisteredVarArray()
	{}

	int Num() const
	{
		return GetBaseVariable<TRegisteredVarArray>()->m_Array.Num();
	}
	bool IsValidIndex( const int InIndex ) const
	{
		return GetBaseVariable<TRegisteredVarArray>()->m_Array.IsValidIndex( InIndex );
	}
	const T& operator[]( const int InIndex ) const
	{ 
		return GetBaseVariable<TRegisteredVarArray>()->m_Array[InIndex];
	}
	T& operator[]( const int InIndex )
	{ 
		T& Value = GetBaseVariable<TRegisteredVarArray>()->m_Array[InIndex];
		SetDirectly( InIndex, Value, false, true );
		return Value;
	}
	TArray<T>& GetInternalArray()
	{
		return m_Array;
	}

	const T& Get( int InIndex ) const			{ return GetBaseVariable<RegVarType>()->m_Array[InIndex]; }
	void Set( int InIndex, const T& InValue )	{ GetBaseVariable<RegVarType>()->SetDirectly( InIndex, InValue, true ); }

protected:
	virtual FName GetBaseVariableType() const 
	{ 
		return RegVarType::StaticGetClassType(); 
	}
	void SetDirectly( int InIndex, const T& InValue, bool InBase, bool InDirty=false )
	{
		if (InBase && m_Array[InIndex] != InValue)
		{
			m_Array[InIndex] = InValue;
			InDirty = true;
		}
		if (InDirty)
		{
			SetDirty( true );
		}
		for (int ii = 0; ii < m_References.Num(); ++ii)
		{
			((RegVarType*)m_References[ii])->SetDirectly( InIndex, InValue, false, InDirty );
		}
	}
	virtual void SetRedirectorInternal( FRegisteredVar* InRedir, bool InAllowCopy )
	{
		if (InAllowCopy && InRedir == NULL && m_pRedirector != NULL)
		{
			m_Array = GetBaseVariable<RegVarType>()->GetInternalArray();
		}
		FRegisteredVar::SetRedirectorInternal( InRedir, InAllowCopy );
	}
	TArray<T> m_Array;
};


// ***********************************************************************************************************************************
// ***** FRegisteredVarBOOL
class FRegisteredVarBOOL : public TRegisteredVarType<bool, FRegisteredVarBOOL>
{
	DECLARE_REGISTERED_VARIABLE( FRegisteredVarBOOL, TRegisteredVarType );

	FRegisteredVarBOOL& operator=( const bool& InValue )	{ Set( InValue ); return *this; }
};

// ***** FRegisteredVarBOOLArray
class FRegisteredVarBOOLArray : public TRegisteredVarArray<bool,FRegisteredVarBOOLArray>
{
	DECLARE_REGISTERED_VARIABLE( FRegisteredVarBOOLArray, TRegisteredVarArray );
};

// ***********************************************************************************************************************************
// FRegisteredVarINT:
class FRegisteredVarINT : public TRegisteredVarType<int, FRegisteredVarINT>
{
	DECLARE_REGISTERED_VARIABLE( FRegisteredVarINT, TRegisteredVarType );

	FRegisteredVarINT& operator=( const int& InValue )		{ Set( InValue ); return *this; }
};

// ***** FRegisteredVarINTArray
class FRegisteredVarINTArray : public TRegisteredVarArray<int, FRegisteredVarINTArray >
{
	DECLARE_REGISTERED_VARIABLE( FRegisteredVarINTArray, TRegisteredVarArray );
};

// ***********************************************************************************************************************************
// FRegisteredVarFLOAT:
class FRegisteredVarFLOAT : public TRegisteredVarType<float, FRegisteredVarFLOAT>
{
	DECLARE_REGISTERED_VARIABLE( FRegisteredVarFLOAT, TRegisteredVarType );

	FRegisteredVarFLOAT& operator=( const float& InValue )	{ Set( InValue ); return *this; }
};

// ***** FRegisteredVarFLOATArray
class FRegisteredVarFLOATArray : public TRegisteredVarArray<float, FRegisteredVarFLOATArray>
{
	DECLARE_REGISTERED_VARIABLE( FRegisteredVarFLOATArray, TRegisteredVarArray );
};


// Here we create a custom type.
class FColor
{
public:

	bool operator!=( const FColor& InColor ) const
	{
		return InColor.r != r || InColor.g != g || InColor.b != b || InColor.a != a;
	}
	BYTE r, g, b, a;
};

// ***********************************************************************************************************************************
// FRegisteredVarFColor:
class FRegisteredVarFColor : public TRegisteredVarType<FColor, FRegisteredVarFColor>
{
	DECLARE_REGISTERED_VARIABLE( FRegisteredVarFColor, TRegisteredVarType );

	FRegisteredVarFColor& operator=( const FColor& InValue )	{ Set( InValue ); return *this; }
};

// ***** FRegisteredVarFColorArray
class FRegisteredVarFColorArray : public TRegisteredVarArray<FColor, FRegisteredVarFColorArray>
{
	DECLARE_REGISTERED_VARIABLE( FRegisteredVarFColorArray, TRegisteredVarArray );
};

// EOF
/*==================================================================================================================================*/
