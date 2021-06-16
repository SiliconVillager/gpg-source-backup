//--------------------------------------------------------------------------------
// CVector2f
//
// Copyright (C) 2003-2009 Jason Zink. All rights reserved.
//--------------------------------------------------------------------------------
#ifndef CVector2f_h
#define CVector2f_h
//--------------------------------------------------------------------------------

//--------------------------------------------------------------------------------
class CVector2f
{
public:
	CVector2f( );
	CVector2f( float x, float y );
	CVector2f( const CVector2f& Vector );
	~CVector2f( );

	// member access
	float x() const;
	float& x();
	float y() const;
	float& y();

	// vector operations
	void Clamp( );
	void MakeZero( );
	void Normalize( );
	float Magnitude( );

	// assignment
	CVector2f& operator= ( const CVector2f& Vector );

	// accessors
	float operator[] ( int iPos ) const;
	float& operator[] ( int iPos );

	// boolean comparison
	bool operator== ( const CVector2f& Vector ) const;
	bool operator!= ( const CVector2f& Vector ) const;

	// arithmetic operations
	CVector2f operator+ ( const CVector2f& Vector ) const;
	CVector2f operator- ( const CVector2f& Vector ) const;
	CVector2f operator* ( float fScalar ) const;
	CVector2f operator/ ( float fScalar ) const;
	CVector2f operator- ( ) const;

	// arithmetic updates
	CVector2f& operator+= ( const CVector2f& Vector );
	CVector2f& operator-= ( const CVector2f& Vector );
	CVector2f& operator*= ( float fScalar );
	CVector2f& operator/= ( float fScalar );

protected:
	float m_afEntry[2];
};
//--------------------------------------------------------------------------------
#endif // CVector2f_h
