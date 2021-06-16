//--------------------------------------------------------------------------------
// CVector4f
//
// Copyright (C) 2003-2009 Jason Zink. All rights reserved.
//--------------------------------------------------------------------------------
#include "CVector4f.h"
#include <math.h>
#include <memory.h>
//--------------------------------------------------------------------------------
CVector4f::CVector4f( )
{
}
//--------------------------------------------------------------------------------
CVector4f::CVector4f( float x, float y, float z, float w )
{
	m_afEntry[0] = x;
	m_afEntry[1] = y;
	m_afEntry[2] = z;
	m_afEntry[3] = w;
}
//--------------------------------------------------------------------------------
CVector4f::CVector4f( const CVector4f& Vector )
{
    memcpy( m_afEntry, (void*)&Vector, 4*sizeof(float) );
}
//--------------------------------------------------------------------------------
CVector4f::~CVector4f( )
{
}
//--------------------------------------------------------------------------------
CVector4f& CVector4f::operator= ( const CVector4f& Vector )
{
    memcpy( m_afEntry, Vector.m_afEntry, 4*sizeof(float) );
    return *this;
}
//--------------------------------------------------------------------------------
float CVector4f::x( ) const
{
	return( m_afEntry[0] );
}
//--------------------------------------------------------------------------------
float& CVector4f::x( )
{
	return( m_afEntry[0] );
}
//--------------------------------------------------------------------------------
float CVector4f::y( ) const
{
	return( m_afEntry[1] );
}
//--------------------------------------------------------------------------------
float& CVector4f::y( )
{
	return( m_afEntry[1] );
}
//--------------------------------------------------------------------------------
float CVector4f::z( ) const
{
	return( m_afEntry[2] );
}
//--------------------------------------------------------------------------------
float& CVector4f::z( )
{
	return( m_afEntry[2] );
}
//--------------------------------------------------------------------------------
float CVector4f::w( ) const
{
	return( m_afEntry[3] );
}
//--------------------------------------------------------------------------------
float& CVector4f::w( )
{
	return( m_afEntry[3] );
}
//--------------------------------------------------------------------------------
void CVector4f::MakeZero( )
{
	memset( m_afEntry, 0, 4*sizeof(float) );
}
//--------------------------------------------------------------------------------
void CVector4f::Normalize( )
{
	float fInvMagnitude = ( 1 / Magnitude() );

	for ( int i = 0; i < 4; i++ )
		m_afEntry[i] *= fInvMagnitude;
}
//--------------------------------------------------------------------------------
float CVector4f::Magnitude( )
{
	float fLength = 0.0f;

	for ( int i = 0; i < 4; i++ )
        fLength += m_afEntry[i] * m_afEntry[i];

	return( sqrt(fLength) );
}
//--------------------------------------------------------------------------------
float CVector4f::Dot( CVector4f& vector )
{
	float ret = 0.0f;
	
	ret  = m_afEntry[0]*vector.x();
	ret += m_afEntry[1]*vector.y();
	ret += m_afEntry[2]*vector.z();
	ret += m_afEntry[3]*vector.w();

	return ret;
}
//--------------------------------------------------------------------------------
float CVector4f::operator[] ( int iPos ) const
{
    return m_afEntry[iPos];
}
//----------------------------------------------------------------------------
float& CVector4f::operator[] ( int iPos )
{
    return m_afEntry[iPos];
}
//----------------------------------------------------------------------------
bool CVector4f::operator== ( const CVector4f& Vector ) const
{
	for ( int i = 0; i < 4; i++ )
	{
		if ( ( m_afEntry[i]-Vector.m_afEntry[i] ) * ( m_afEntry[i]-Vector.m_afEntry[i] ) > 0.01f )
			return false;
	}

	return true;
}
//--------------------------------------------------------------------------------
bool CVector4f::operator!= ( const CVector4f& Vector ) const
{
    return memcmp( m_afEntry, Vector.m_afEntry, 4*sizeof(float) ) != 0;
}
//--------------------------------------------------------------------------------
CVector4f CVector4f::operator+ ( const CVector4f& Vector ) const
{
	CVector4f sum;

	for ( int i = 0; i < 4; i++ )
		sum.m_afEntry[i] = m_afEntry[i] + Vector.m_afEntry[i];

	return( sum );
}
//--------------------------------------------------------------------------------
CVector4f CVector4f::operator- ( const CVector4f& Vector ) const
{
	CVector4f diff;

	for ( int i = 0; i < 4; i++ )
		diff.m_afEntry[i] = m_afEntry[i] - Vector.m_afEntry[i];

	return( diff );
}
//--------------------------------------------------------------------------------
CVector4f CVector4f::operator* ( float fScalar ) const
{
	CVector4f prod;

	for ( int i = 0; i < 4; i++ )
		prod.m_afEntry[i] = m_afEntry[i] * fScalar;

	return( prod );
}
//--------------------------------------------------------------------------------
CVector4f CVector4f::operator/ ( float fScalar ) const
{
	CVector4f quot;
	if ( fScalar != 0.0f )
	{
		float fInvScalar = 1.0f / fScalar;
		for ( int i = 0; i < 4; i++ )
			quot.m_afEntry[i] = m_afEntry[i] * fInvScalar;
	}
	else
	{
		for ( int i = 0; i < 4; i++ )
			quot.m_afEntry[i] = 0;
	}

	return( quot );
}
//--------------------------------------------------------------------------------
CVector4f CVector4f::operator- ( ) const
{
	CVector4f neg;

	for ( int i = 0; i < 4; i++ )
		neg.m_afEntry[i] = -m_afEntry[i];

	return( neg );
}
//--------------------------------------------------------------------------------
CVector4f& CVector4f::operator+= ( const CVector4f& Vector )
{
	for ( int i = 0; i < 4; i++ )
		m_afEntry[i] += Vector.m_afEntry[i];

	return( *this );
}
//--------------------------------------------------------------------------------
CVector4f& CVector4f::operator-= ( const CVector4f& Vector )
{
	for ( int i = 0; i < 4; i++ )
		m_afEntry[i] -= Vector.m_afEntry[i];

	return( *this );
}
//--------------------------------------------------------------------------------
CVector4f& CVector4f::operator*= ( float fScalar )
{
	for ( int i = 0; i < 4; i++ )
		m_afEntry[i] *= fScalar;

	return( *this );
}
//--------------------------------------------------------------------------------
CVector4f& CVector4f::operator/= ( float fScalar )
{
	if ( fScalar != 0.0f )
	{
		float fInvScalar = 1.0f / fScalar;	
		for ( int i = 0; i < 4; i++ )
			m_afEntry[i] *= fInvScalar;
	}
	else
	{
		for ( int i = 0; i < 4; i++ )
			m_afEntry[i] = 0;
	}

	return( *this );
}
//--------------------------------------------------------------------------------
void CVector4f::Clamp()
{
	for ( int i = 0; i < 4; i++ )
	{
		if ( m_afEntry[i] > 1.0f )
			m_afEntry[i] = 1.0f;
		if ( m_afEntry[i] < 0.0f )
			m_afEntry[i] = 0.0f;
	}
}
//--------------------------------------------------------------------------------
unsigned int CVector4f::toARGB( )
{
	unsigned int result = 0;

	Clamp();

	result += (unsigned int)(255*z());
	result += ((unsigned int)(255*y()) << 8);
	result += ((unsigned int)(255*x()) << 16);
	result += ((unsigned int)(255*w()) << 24);

	return( result );
}
//--------------------------------------------------------------------------------
unsigned int CVector4f::toRGBA( )
{
	unsigned int result = 0;

	Clamp();

	result += (unsigned int)(255*w());
	result += ((unsigned int)(255*z()) << 8);
	result += ((unsigned int)(255*y()) << 16);
	result += ((unsigned int)(255*x()) << 24);
	
	return( result );
}
//--------------------------------------------------------------------------------
void CVector4f::fromARGB( unsigned int color )
{
	m_afEntry[0] = (float)((color & 0x00ff0000) >> 16)/(255.0f);	// red channel
	m_afEntry[1] = (float)((color & 0x0000ff00) >> 8)/(255.0f);	// green channel
	m_afEntry[2] = (float)((color & 0x000000ff))/(255.0f);		// blue channel
	m_afEntry[3] = (float)((color & 0xff000000) >> 24)/(255.0f);	// alpha channel
}
//--------------------------------------------------------------------------------