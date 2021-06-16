/*==================================================================================================================================*/
/** @name FRegisteredVar.cpp -> Implements FRegisteredVar.h
*
*   DESCRIPTION: Registered Variables are used to pass values between objects without the object be required to know about one another.
*
*   @field File         FRegisteredVar.cpp
*   @field Created      9/8/2009 9:48:31 AM	
*************************************************************************************************************************************/

#include "stdafx.h"

FRegisteredVar::FRegisteredVar()
: m_bDirty(false)
, m_pRedirector(NULL)
, m_DirtyCallback(NULL)
{}
FRegisteredVar::~FRegisteredVar()
{
	if (m_pRedirector)
	{
		m_pRedirector->RemoveReference( this );
	}
	while (m_References.Num())
	{
		m_References[0]->SetRedirector( NULL, false );
	}
}
void FRegisteredVar::SetDirtyCallback( void(*InCallback)( FRegisteredVar& ) )
{
	m_DirtyCallback = InCallback;
}
bool FRegisteredVar::IsDirty( bool InCheckParent ) const 
{
	if (InCheckParent && m_pRedirector)
	{
		return m_pRedirector->IsDirty( true );
	}
	return m_bDirty; 
}
void FRegisteredVar::SetDirty( bool InDirty, bool InRecurse )
{
	m_bDirty = InDirty;
	if (m_bDirty && m_DirtyCallback)
	{
		m_DirtyCallback( *this );
	}
	if (InRecurse && m_pRedirector)
	{
		m_pRedirector->SetDirty( InDirty, InRecurse );
	}
}
void FRegisteredVar::SetRedirector( FRegisteredVar* InRedir, bool InAllowCopy )
{
	if (InRedir != this && (!InRedir || (InRedir->IsA( GetBaseVariableType() ) && !InRedir->IsRegistered( this ))))
	{
		if (m_pRedirector)
		{
			m_pRedirector->RemoveReference( this );
		}
		SetRedirectorInternal( InRedir, InAllowCopy );
		if (m_pRedirector)
		{
			m_pRedirector->AddReference( this );
		}
	} 
}
bool FRegisteredVar::IsRegistered( FRegisteredVar* InRedir )
{
	return ( m_pRedirector == InRedir ) || ( m_pRedirector && m_pRedirector->IsRegistered( InRedir ) );
}
bool FRegisteredVar::IsRedirected() const
{
	return m_pRedirector != NULL;
}
void FRegisteredVar::SetFName( FName InName )
{
	m_Name = InName;
}
FName FRegisteredVar::GetFName() const
{
	return m_Name;
}

FName FRegisteredVar::GetBaseVariableType() const 
{
	return GetClassType(); 
}
void FRegisteredVar::SetRedirectorInternal( FRegisteredVar* InRedir, bool InAllowCopy )
{
	m_pRedirector = InRedir;
	SetDirty( true );
}
void FRegisteredVar::AddReference( FRegisteredVar* InParent )
{
	m_References.AddItem( InParent );
}
void FRegisteredVar::RemoveReference( FRegisteredVar* InParent )
{
	m_References.RemoveItem( InParent );
}

// EOF
/*==================================================================================================================================*/
