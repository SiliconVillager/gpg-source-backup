/*==================================================================================================================================*/
/** @name RegisterVariables.cpp -> Implements RegisterVariables.h
*
*   DESCRIPTION: Registered Variables are used to pass values between objects without the object be required to know about one another.
*
*   @field File         RegisterVariables.cpp
*   @field Created      9/21/2009 10:26:04 AM	
*************************************************************************************************************************************/

#include "stdafx.h"

class FRegisterVariableHolder
{
public:
	virtual void RegisterVariable( FRegisteredVar& InVar ) {}
};
class BaseClass : public FRegisterVariableHolder
{
public:
	virtual void RegisterVariables(FRegisterVariableHolder& InHolder) {}
};

// Simple example of setting a registered variable explicitly.
class Weapon : public BaseClass
{
public:
	Weapon()
	{
		m_Fire = false;
		m_Fire.SetFName( "Fire" );
	}
	void SetFireRegVar( FRegisteredVar* InVar )
	{
		m_Fire.SetRedirector( InVar );
	}
	virtual void RegisterVariable( FRegisteredVar& InVar )
	{
		if (InVar.IsA<FRegisteredVarBOOL>() && 
			InVar.GetFName() == m_Fire.GetFName())
		{
			m_Fire.SetRedirector( &InVar );
		}
	} 
	bool IsFiring()
	{
		return m_Fire;
	}

	FRegisteredVarBOOL m_Fire;
};
class Vehicle : public BaseClass
{
public:
	Vehicle()
		: m_pMyWeapon(NULL)
	{
		m_FireWeapon = false;
	}
	void Initialize( Weapon* InWeapon )
	{
		m_pMyWeapon = InWeapon;
		m_pMyWeapon->SetFireRegVar( &m_FireWeapon );
	}
	FRegisteredVarBOOL m_FireWeapon;
	Weapon* m_pMyWeapon;
};

// Example of setting a registered variable indirectly.
class Material : public BaseClass
{};
class DamageStateMaterial : public Material
{
public:
	DamageStateMaterial()
	{
		m_DamageState.SetFName( "DamageState" );
		m_DamageState = 0.f;
	}
	virtual void RegisterVariable( FRegisteredVar& InVar )
	{
		if (InVar.IsA<FRegisteredVarFLOAT>() && 
			InVar.GetFName() == m_DamageState.GetFName())
		{
			m_DamageState.SetRedirector( &InVar );
		}
	} 
	FRegisteredVarFLOAT m_DamageState;
};
class Vehicle2 : public BaseClass
{
public:
	Vehicle2()
	{
		m_VehicleDamageState = 0.f;
		m_VehicleDamageState.SetFName( "DamageState" );
		m_Fire = false;
		m_Fire.SetFName( "Fire" );
	}
	void Initialize( Weapon* InWeapon, Material* InMaterial )
	{
		m_pMaterial = InMaterial;
		m_pMyWeapon = InWeapon;
		RegisterVariables( *m_pMaterial );       
		RegisterVariables( *m_pMyWeapon );       
	}
	virtual void RegisterVariables( FRegisterVariableHolder& InHolder )
	{
		InHolder.RegisterVariable( m_VehicleDamageState );
		InHolder.RegisterVariable( m_Fire );
	}
	FRegisteredVarFLOAT m_VehicleDamageState;
	FRegisteredVarBOOL m_Fire;
	Material* m_pMaterial;
	Weapon* m_pMyWeapon;
};


int main()
{
	// Initialize the FName system.
	FName::StaticInit();

	// Here we will take a look at an explicitly set registered variable.  The vehicle can change its variable to affect the weapon.
	Weapon weap;
	Vehicle veh;
	veh.Initialize( &weap );
	printf( "The weapon is firing = %s\n", weap.IsFiring() ? "true" : "false" );
	veh.m_FireWeapon = true;
	printf( "The weapon is firing = %s\n", weap.IsFiring() ? "true" : "false" );
	veh.m_FireWeapon = false;


	// Now lets take a look at indirectly setting up a registered var.
	Vehicle2 veh2;
	DamageStateMaterial mat;
	veh2.Initialize( &weap, &mat );
	printf( "The material damage state = %f\n", mat.m_DamageState.Get() );
	veh2.m_VehicleDamageState = 3.f;
	printf( "The material damage state = %f\n", mat.m_DamageState.Get() );

	return 0;
}


// EOF
/*==================================================================================================================================*/
