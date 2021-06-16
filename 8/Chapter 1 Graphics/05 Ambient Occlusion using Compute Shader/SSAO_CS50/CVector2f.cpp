//--------------------------------------------------------------------------------
// CVector2f
//
// Copyright (C) 2003-2009 Jason Zink. All rights reserved.
//--------------------------------------------------------------------------------
#include "CVector2f.h"
#include <math.h>
#include <memory.h>
//--------------------------------------------------------------------------------
CVector2f::CVector2f( )
{
}
//--------------------------------------------------------------------------------
CVector2f::CVector2f( float x, float y )
{
	m_afEntry[0] = x;
	m_afEntry[1] = y;
}
//--------------------------------------------------------------------------------
CVector2f::CVector2f( const CVector2f& Vector )
{
    memcpy( m_afEntry, (void*)&Vector, 2*sizeof(float) );
}
//--------------------------------------------------------------------------------
CVector2f::~CVector2f( )
{
}
//--------------------------------------------------------------------------------
CVector2f& CVector2f::operator= ( const CVector2f& Vector )
{
    memcpy( m_afEntry, Vector.m_afEntry, 2*sizeof(float) );
    return( *this );
}
//--------------------------------------------------------------------------------
float CVector2f::x( ) const
{
	return( m_afEntry[0] );
}
//--------------------------------------------------------------------------------
float& CVector2f::x( )
{
	return( m_afEntry[0] );
}
//--------------------------------------------------------------------------------
float CVector2f::y( ) const
{
	return( m_afEntry[1] );
}
//--------------------------------------------------------------------------------
float& CVector2f::y( )
{
	return( m_afEntry[1] );
}
//--------------------------------------------------------------------------------
void CVector2f::MakeZero( )
{
	memset( m_afEntry, 0, 2*sizeof(float) );
}
//--------------------------------------------------------------------------------
void CVector2f::Normalize( )
{
	float fInvMagnitude = ( 1.0f / Magnitude() );

	for ( int i = 0; i < 2; i++ )
		m_afEntry[i] *= fInvMagnitude;
}
//--------------------------------------------------------------------------------
float CVector2f::Magnitude( )
{
	float fLength = 0.0f;

	for ( int i = 0; i < 2; i++ )
        fLength += m_afEntry[i] * m_afEntry[i];

	return( sqrt( fLength ) );
}
//--------------------------------------------------------------------------------
void CVector2f::Clamp()
{
	for ( int i = 0; i < 2; i++ )
	{
		if ( m_afEntry[i] > 1.0f )
			m_afEntry[i] = 1.0f;
		if ( m_afEntry[i] < 0.0f )
			m_afEntry[i] = 0.0f;
	}
}
//--------------------------------------------------------------------------------
float CVector2f::operator[] ( int iPos ) const
{
    return( m_afEntry[iPos] );
}
//----------------------------------------------------------------------------
float& CVector2f::operator[] ( int iPos )
{
    return( m_afEntry[iPos] );
}
//----------------------------------------------------------------------------
bool CVector2f::operator== ( const CVector2f& Vector ) const
{
	for (int i = 0; i < 2; i++)
	{
		if ( ( m_afEntry[i]-Vector.m_afEntry[i] ) * ( m_afEntry[i]-Vector.m_afEntry[i] ) > 0.01f )
			return( false );
	}

	return( true );
}
//--------------------------------------------------------------------------------
bool CVector2f::operator!= ( const CVector2f& Vector ) const
{
    return memcmp( m_afEntry, Vector.m_afEntry, 2*sizeof(float) ) != 0;
}
//--------------------------------------------------------------------------------
CVector2f CVector2f::operator+ ( const CVector2f& Vector ) const
{
	CVector2f sum;

	for ( int i = 0; i < 2; i++ )
		sum.m_afEntry[i] = m_afEntry[i] + Vector.m_afEntry[i];

	return( sum );
}
//--------------------------------------------------------------------------------
CVector2f CVector2f::operator- ( const CVector2f& Vector ) const
{
	CVector2f diff;

	for ( int i = 0; i < 2; i++ )
		diff.m_afEntry[i] = m_afEntry[i] - Vector.m_afEntry[i];

	return( diff );
}
//--------------------------------------------------------------------------------
CVector2f CVector2f::operator* ( float fScalar ) const
{
	CVector2f prod;

	for ( int i = 0; i < 2; i++ )
		prod.m_afEntry[i] = m_afEntry[i] * fScalar;

	return( prod );
}
//--------------------------------------------------------------------------------
CVector2f CVector2f::operator/ ( float fScalar ) const
{
	CVector2f quot;
	if ( fScalar != 0.0f )
	{
		float fInvScalar = 1.0f / fScalar;
		for ( int i = 0; i < 2; i++ )
			quot.m_afEntry[i] = m_afEntry[i] * fInvScalar;
	}
	else
	{
		for ( int i = 0; i < 2; i++ )
			quot.m_afEntry[i] = 0;
	}

	return( quot );
}
//--------------------------------------------------------------------------------
CVector2f CVector2f::operator- ( ) const
{
	CVector2f neg;

	for ( int i = 0; i < 2; i++ )
		neg.m_afEntry[i] = -m_afEntry[i];

	return( neg );
}
//--------------------------------------------------------------------------------
CVector2f& CVector2f::operator+= ( const CVector2f& Vector )
{
	for ( int i = 0; i < 2; i++ )
		m_afEntry[i] += Vector.m_afEntry[i];

	return( *this );
}
//--------------------------------------------------------------------------------
CVector2f& CVector2f::operator-= ( const CVector2f& Vector )
{
	for ( int i = 0; i < 2; i++ )
		m_afEntry[i] -= Vector.m_afEntry[i];

	return( *this );
}
//--------------------------------------------------------------------------------
CVector2f& CVector2f::operator*= ( float fScalar )
{
	for ( int i = 0; i < 2; i++ )
		m_afEntry[i] *= fScalar;

	return( *this );
}
//--------------------------------------------------------------------------------
CVector2f& CVector2f::operator/= ( float fScalar )
{
	if ( fScalar != 0.0f )
	{
		float fInvScalar = 1.0f / fScalar;	
		for ( int i = 0; i < 2; i++ )
			m_afEntry[i] *= fInvScalar;
	}
	else
	{
		for ( int i = 0; i < 2; i++ )
			m_afEntry[i] = 0;
	}

	return( *this );
}
//--------------------------------------------------------------------------------
