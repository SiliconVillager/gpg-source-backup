/*==================================================================================================================================*/
/** @name FName.cpp -> Implements FName.h
*
*   DESCRIPTION: Defines the engines name table, allows for the use of strings with the comparison speed of integers.
*
*   @field File         FName.cpp
*   @field Created      11/8/2006 11:30:00 PM
*************************************************************************************************************************************/

#include "stdafx.h"

// ***********************************************************************************************************************************
// FName -> class implementation:

// Static variables.
bool												FName::m_bInitialized = false;
bool												FName::m_bDisableFNameCreation = false;
FNameEntry*											FName::m_NameHash[FNAME_HASH_TABLE_SIZE];
TSparsePtrArray<FNameEntry, FNameEntry::FNameIndex> FName::m_Names;


/*==================================================================================================================================*/
/** FName::FName()
*
*   DESCRIPTION:
*     FName Constructor.
*
*   PARAMETERS:
*     @param   Name        The name to create within the FName Table.
*     @param   FindType    The query type.
*************************************************************************************************************************************/
FName::FName( const char* InName )
	: m_iID(0)
	, m_pBaseName(NULL)
{
	assert( InName );
	assert( m_bInitialized );

	// If empty or invalid name was specified, return NAME_None.
	if (!InName[0])
	{
		*this = NAME_None;
		return;
	}

	size_t StrLen = strlen( InName );
	if (InName[StrLen-1] == ')')
	{
		const char* IDStart = strstr( InName, "(" );
		const char* IDTemp = IDStart;
		while (IDTemp)
		{
			IDTemp = strstr( &IDTemp[1], "(" );
			if (IDTemp)
			{
				IDStart = IDTemp;
			}
		}
		char TempName[NAME_SIZE];
		if (IDStart)
		{
			size_t Index = int(IDStart - InName)+1;
			if (Index < StrLen && appIsDigit( InName[Index] ))
			{
				strncpy( TempName, InName, Index-1 );
				TempName[Index-1] = NULL;
				m_iID = atoi( &InName[Index] );
				InName = TempName;
			}
		}
	}

	// Try to find the name in the hash.
	int iHash = appStrihash( InName ) & (ARRAY_COUNT(m_NameHash)-1);
	for (FNameEntry* Hash = m_NameHash[iHash]; Hash; Hash = Hash->m_pHashNext)
	{
		if (stricmp( InName, Hash->m_Name ) == 0)
		{
			// Found it in the hash.
			m_iIndex = Hash->m_iIndex;
			m_pBaseName = Hash->m_Name;
			return;
		}
	}

	// Add the name.
	FNameEntry* NewName = CreateNameEntry( InName, m_NameHash[iHash] );
	m_Names.AddItem( NewName );
	assert( NewName->m_iIndex < MAX_NUMBER_OF_NAMES );
	m_iIndex = NewName->m_iIndex;
	m_pBaseName = NewName->m_Name;

	// Add the name to the hash table.
	m_NameHash[iHash] = NewName;
}

/*==================================================================================================================================*/
/** FName::StaticInit()
*
*   DESCRIPTION:
*     Initialize the name subsystem.
*************************************************************************************************************************************/
void FName::StaticInit()
{
	assert( m_bInitialized == false );
	assert( (ARRAY_COUNT(m_NameHash)&(ARRAY_COUNT(m_NameHash)-1)) == 0 );
	m_bInitialized = true;

	// Init the name hash.
	for (int ii = 0; ii < ARRAY_COUNT(m_NameHash); ++ii)
	{
		m_NameHash[ii] = NULL;
	}

	// Create default names.
	FName None = FName( "None" );
	assert( None.GetIndex() == NAME_None );
}

/*==================================================================================================================================*/
/** FName::StaticExit()
*
*   DESCRIPTION:
*     Shut down the name subsystem.
*************************************************************************************************************************************/
void FName::StaticExit()
{
	assert( m_bInitialized);

	// Kill all names.
	for (int ii = 0; ii < m_Names.Num(); ++ii)
	{
		if (m_Names[ii])
		{
			free( m_Names[ii] );
		}
	}

	// Empty tables.
	m_Names.Empty();
	m_bInitialized = false;
}


// ***********************************************************************************************************************************
// FNameEntry -> class implementation:

/*==================================================================================================================================*/
/** CreateNameEntry()
*
*   DESCRIPTION:
*     Allocates and initializes a new FNameEntry for the FName table.
*
*   PARAMETERS:
*     @param   Name        The name for the new FNameEntry.
*     @param   HashNext    The next hash index.
*
*     @return  FNameEntry* -> Returns the newly created FNameEntry.
*************************************************************************************************************************************/
FNameEntry* CreateNameEntry( const char* Name, FNameEntry* HashNext )
{
	assert( strlen(Name) < NAME_SIZE );
	DWORD iSize = sizeof(FNameEntry) - (NAME_SIZE - strlen(Name) - 1)*sizeof(char);
	FNameEntry* NameEntry = (FNameEntry*)malloc( iSize );
	NameEntry->m_iIndex = INDEX_NONE;
	NameEntry->m_pHashNext   = HashNext;
	strcpy( NameEntry->m_Name, Name );
	return NameEntry;
}

// EOF
/*==================================================================================================================================*/
