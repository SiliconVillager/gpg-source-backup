//--------------------------------------------------------------------------------
// CVector4f
//
// Copyright (C) 2003-2009 Jason Zink. All rights reserved.
//--------------------------------------------------------------------------------
#ifndef CVector4f_h
#define CVector4f_h
//--------------------------------------------------------------------------------

//--------------------------------------------------------------------------------
class CVector4f
{
public:
	CVector4f( );
	CVector4f( float x, float y, float z, float w );
	CVector4f( const CVector4f& Vector );
	~CVector4f( );

	// member access
	float x() const;
	float& x();
	float y() const;
	float& y();
	float z() const;
	float& z();
	float w() const;
	float& w();

	// vector operations
	void Clamp( );
	float Dot( CVector4f& vector );
	void MakeZero( );
	void Normalize( );
	float Magnitude( );

	// Operators
	CVector4f& operator= ( const CVector4f& Vector );

	// member access
	float operator[] ( int iPos ) const;
	float& operator[] ( int iPos );

	// comparison
	bool operator== ( const CVector4f& Vector ) const;
	bool operator!= ( const CVector4f& Vector ) const;

	// arithmetic operations
	CVector4f operator+ ( const CVector4f& Vector ) const;
	CVector4f operator- ( const CVector4f& Vector ) const;
	CVector4f operator* ( float fScalar) const;
	CVector4f operator/ ( float fScalar) const;
	CVector4f operator- ( ) const;

	// arithmetic updates
	CVector4f& operator+= ( const CVector4f& Vector );
	CVector4f& operator-= ( const CVector4f& Vector );
	CVector4f& operator*= ( float fScalar );
	CVector4f& operator/= ( float fScalar );

	unsigned int toARGB( );
	unsigned int toRGBA( );
	void fromARGB( unsigned int color );

protected:
	float m_afEntry[4];
};
//--------------------------------------------------------------------------------
#endif // CVector4f_h
