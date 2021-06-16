//--------------------------------------------------------------------------------
// CVector3f
//
// Copyright (C) 2003-2009 Jason Zink. All rights reserved.
//--------------------------------------------------------------------------------
#include "CVector3f.h"
#include <math.h>
#include <memory.h>
//--------------------------------------------------------------------------------
CVector3f::CVector3f( )
{
}
//--------------------------------------------------------------------------------
CVector3f::CVector3f( float x, float y, float z )
{
	m_afEntry[0] = x;
	m_afEntry[1] = y;
	m_afEntry[2] = z;
}
//--------------------------------------------------------------------------------
CVector3f::CVector3f( const CVector3f& Vector )
{
    memcpy( m_afEntry, (void*)&Vector, 3*sizeof(float) );
}
//--------------------------------------------------------------------------------
CVector3f::~CVector3f( )
{
}
//--------------------------------------------------------------------------------
float CVector3f::x( ) const
{
	return( m_afEntry[0] );
}
//--------------------------------------------------------------------------------
float& CVector3f::x( )
{
	return( m_afEntry[0] );
}
//--------------------------------------------------------------------------------
float CVector3f::y( ) const
{
	return( m_afEntry[1] );
}
//--------------------------------------------------------------------------------
float& CVector3f::y( )
{
	return( m_afEntry[1] );
}
//--------------------------------------------------------------------------------
float CVector3f::z( ) const
{
	return( m_afEntry[2] );
}
//--------------------------------------------------------------------------------
float& CVector3f::z( )
{
	return( m_afEntry[2] );
}
//--------------------------------------------------------------------------------
void CVector3f::MakeZero( )
{
	memset( m_afEntry, 0, 3*sizeof(float) );
}
//--------------------------------------------------------------------------------
void CVector3f::Normalize( )
{
	float fInvMagnitude = ( 1 / Magnitude() );

	for ( int i = 0; i < 3; i++ )
		m_afEntry[i] *= fInvMagnitude;
}
//--------------------------------------------------------------------------------
float CVector3f::Magnitude( )
{
	float fLength = 0.0f;

	for ( int i = 0; i < 3; i++ )
        fLength += m_afEntry[i] * m_afEntry[i];

	return( sqrt( fLength ) );
}
//--------------------------------------------------------------------------------
CVector3f CVector3f::Cross( CVector3f& A )
{
	CVector3f vRet; 
	
	vRet.x() = m_afEntry[1]*A.z() - m_afEntry[2]*A.y();
	vRet.y() = m_afEntry[2]*A.x() - m_afEntry[0]*A.z();
	vRet.z() = m_afEntry[0]*A.y() - m_afEntry[1]*A.x();
	
	return( vRet );
}
//--------------------------------------------------------------------------------
float CVector3f::Dot( CVector3f& A )
{
	float ret = 0.0f;
	
	ret  = m_afEntry[0]*A.x();
	ret += m_afEntry[1]*A.y();
	ret += m_afEntry[2]*A.z();

	return ret;
}
//--------------------------------------------------------------------------------
void CVector3f::Clamp()
{
	for ( int i = 0; i < 3; i++ )
	{
		if ( m_afEntry[i] > 1.0f )
			m_afEntry[i] = 1.0f;
		if ( m_afEntry[i] < 0.0f )
			m_afEntry[i] = 0.0f;
	}
}
//--------------------------------------------------------------------------------
CVector3f& CVector3f::operator= ( const CVector3f& Vector )
{
    memcpy( m_afEntry, Vector.m_afEntry, 3*sizeof(float) );
    return( *this );
}
//--------------------------------------------------------------------------------
float CVector3f::operator[] ( int iPos ) const
{
    return( m_afEntry[iPos] );
}
//----------------------------------------------------------------------------
float& CVector3f::operator[] ( int iPos )
{
    return( m_afEntry[iPos] );
}
//----------------------------------------------------------------------------
bool CVector3f::operator== ( const CVector3f& Vector ) const
{
	for ( int i = 0; i < 3; i++ )
	{
		if ( ( m_afEntry[i]-Vector.m_afEntry[i] ) * ( m_afEntry[i]-Vector.m_afEntry[i] ) > 0.01f )
			return false;
	}

	return true;
}
//--------------------------------------------------------------------------------
bool CVector3f::operator!= ( const CVector3f& Vector ) const
{
    return memcmp( m_afEntry, Vector.m_afEntry, 3*sizeof(float) ) != 0;
}
//--------------------------------------------------------------------------------
CVector3f CVector3f::operator+ ( const CVector3f& Vector ) const
{
	CVector3f sum;

	for ( int i = 0; i < 3; i++ )
		sum.m_afEntry[i] = m_afEntry[i] + Vector.m_afEntry[i];

	return( sum );
}
//--------------------------------------------------------------------------------
CVector3f CVector3f::operator- ( const CVector3f& Vector ) const
{
	CVector3f diff;

	for ( int i = 0; i < 3; i++ )
		diff.m_afEntry[i] = m_afEntry[i] - Vector.m_afEntry[i];

	return( diff );
}
//--------------------------------------------------------------------------------
CVector3f CVector3f::operator* ( float fScalar ) const
{
	CVector3f prod;

	for ( int i = 0; i < 3; i++ )
		prod.m_afEntry[i] = m_afEntry[i] * fScalar;

	return( prod );
}
//--------------------------------------------------------------------------------
CVector3f CVector3f::operator/ ( float fScalar ) const
{
	CVector3f quot;
	if ( fScalar != 0.0f )
	{
		float fInvScalar = 1.0f / fScalar;
		for ( int i = 0; i < 3; i++ )
			quot.m_afEntry[i] = m_afEntry[i] * fInvScalar;
	}
	else
	{
		for ( int i = 0; i < 3; i++ )
			quot.m_afEntry[i] = 0;
	}

	return( quot );
}
//--------------------------------------------------------------------------------
CVector3f CVector3f::operator- ( ) const
{
	CVector3f neg;

	for ( int i = 0; i < 3; i++ )
		neg.m_afEntry[i] = -m_afEntry[i];

	return( neg );
}
//--------------------------------------------------------------------------------
CVector3f& CVector3f::operator+= ( const CVector3f& Vector )
{
	for ( int i = 0; i < 3; i++ )
		m_afEntry[i] += Vector.m_afEntry[i];

	return( *this );
}
//--------------------------------------------------------------------------------
CVector3f& CVector3f::operator-= ( const CVector3f& Vector )
{
	for ( int i = 0; i < 3; i++ )
		m_afEntry[i] -= Vector.m_afEntry[i];

	return( *this );
}
//--------------------------------------------------------------------------------
CVector3f& CVector3f::operator*= ( float fScalar )
{
	for ( int i = 0; i < 3; i++ )
		m_afEntry[i] *= fScalar;

	return( *this );
}
//--------------------------------------------------------------------------------
CVector3f& CVector3f::operator/= ( float fScalar )
{
	if ( fScalar != 0.0f )
	{
		float fInvScalar = 1.0f / fScalar;	
		for ( int i = 0; i < 3; i++ )
			m_afEntry[i] *= fInvScalar;
	}
	else
	{
		for ( int i = 0; i < 3; i++ )
			m_afEntry[i] = 0;
	}

	return( *this );
}
//--------------------------------------------------------------------------------
