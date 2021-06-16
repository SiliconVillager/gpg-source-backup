/*==================================================================================================================================*/
/** @name FName.h 
*
*   DESCRIPTION: Defines the engines name table, allows for the use of strings with the comparison speed of integers.
*
*   @field File         FName.h
*   @field Created      11/8/2006 11:30:00 PM
*************************************************************************************************************************************/

#pragma once

// Maximum size of name. 
enum {NAME_SIZE	= 512};

// Name index. 
typedef WORD NAME_INDEX;
#define MAX_NUMBER_OF_NAMES		MAX_WORD
#define FNAME_HASH_TABLE_SIZE	4096

// ***********************************************************************************************************************************
// FNameEntry:
//		A global name, as stored in the global name table.
struct FNameEntry
{
	int m_iIndex;
	FNameEntry*	m_pHashNext;
	char m_Name[NAME_SIZE]; // Variable-Sized, allocates memory only as needed.  NAME_SIZE is the worst case.

	friend FNameEntry* CreateNameEntry( const char* InName, FNameEntry* InHashNext );

	class FNameIndex
	{
	public:
		int GetIndex( FNameEntry* InEntry ) const
		{
			return InEntry->m_iIndex;
		}
		void SetIndex( FNameEntry* InEntry, int InIndex ) const
		{
			InEntry->m_iIndex = InIndex;
		}
	};
};


// Defined at the top level game library to finish auto-generating all required FNames.
extern inline void appAutogenerateFNames();

enum EName 
{
	NAME_None = 0,
};

// ***********************************************************************************************************************************
// FName:
//		Public name, available to the world.  Names are stored as WORD indices into the name table and every name is stored once
//		and only once in that table.  Names are case-insensitive..
class FName 
{
public:
	FName()
	{}
	FName( enum EName InEnumName, NAME_INDEX InID=0 )
		: m_iIndex(InEnumName)
		, m_iID(InID)
	{
		m_pBaseName = m_Names[m_iIndex]->m_Name;
	}
	FName( const char* InName );
	FName( FName InName, NAME_INDEX InID )
	{
		m_iIndex = InName.m_iIndex;
		m_iID = InID;
		m_pBaseName = InName.m_pBaseName;
	}

	const char* operator*() const
	{
		assert( IsValid() );
		if (m_iID == 0)
		{
			return m_Names[m_iIndex]->m_Name;
		}
		else
		{
			const int iMaxNumTempNames = 10;
			static int TempNameIndex = 0;
			static char TempName[iMaxNumTempNames][NAME_SIZE];
			char* Temp = TempName[TempNameIndex++];
			if (TempNameIndex == iMaxNumTempNames)
			{
				TempNameIndex = 0;
			}
			if (m_iID == 0)
			{
				sprintf( Temp, "%s", m_Names[m_iIndex]->m_Name );
			}
			else
			{
				sprintf( Temp, "%s(%d)", m_Names[m_iIndex]->m_Name, m_iID );
			}
			return Temp;
		}
	}
	NAME_INDEX GetIndex() const
	{
		return m_iIndex;
	}
	NAME_INDEX GetID() const
	{
		return m_iID;
	}
	FName GetBaseFName() 
	{
		return FName( *this, 0 );
	}
	bool operator==( const FName& Other ) const
	{
		return m_iIndexID == Other.m_iIndexID;
	}
	bool operator!=( const FName& Other ) const
	{
		return m_iIndexID != Other.m_iIndexID;
	}
	bool IsValid() const
	{
		return m_iIndex >= 0 && m_iIndex < m_Names.Num() && m_Names[m_iIndex] != NULL;
	}

	// Copy operator.
	FName& operator=( const FName& Other )
	{
		m_iIndex = Other.m_iIndex;
		m_iID = Other.m_iID;
		m_pBaseName = Other.m_pBaseName;
		return *this;
	}

	// Static Operations.
	static void StaticInit();
	static void StaticExit();
	static bool IsInitialized() { return m_bInitialized; }

private:

	// Name index.
	union
	{
		DWORD m_iIndexID;
		struct 
		{
			NAME_INDEX m_iIndex;
			NAME_INDEX m_iID;
		};
	};
	const char* m_pBaseName;

	// Static subsystem variables.
	static TSparsePtrArray<FNameEntry, FNameEntry::FNameIndex>	m_Names;								// Table of all names.
	static FNameEntry*											m_NameHash[FNAME_HASH_TABLE_SIZE];		// Hashed names.
	static bool													m_bInitialized;							// Subsystem initialized.
	static bool													m_bDisableFNameCreation;				// Whether to allow new names or not.
};

// Type information for FName objects.  No construction or destruction necessary. 
template <> class TTypeInfo<FName>	: public TTypeInfoAtomicBase<FName> {};


// EOF
/*==================================================================================================================================*/
