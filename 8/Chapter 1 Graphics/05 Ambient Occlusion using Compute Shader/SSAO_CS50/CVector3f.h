//--------------------------------------------------------------------------------
// CVector3f
//
// Copyright (C) 2003-2009 Jason Zink. All rights reserved.
//--------------------------------------------------------------------------------
#ifndef CVector3f_h
#define CVector3f_h
//--------------------------------------------------------------------------------

//--------------------------------------------------------------------------------
class CVector3f
{
public:
	CVector3f( );
	CVector3f( float x, float y, float z );
	CVector3f( const CVector3f& Vector );
	~CVector3f();

	// member access
	float x() const;
	float& x();
	float y() const;
	float& y();
	float z() const;
	float& z();

	// vector operations
	void Clamp( );
	CVector3f Cross( CVector3f& A );
	float Dot( CVector3f& A );
	void MakeZero( );
	float Magnitude( );
	void Normalize( );

	// assignment
	CVector3f& operator= (const CVector3f& Vector);

	// member access
	float operator[] (int iPos) const;
	float& operator[] (int iPos);

	// comparison
	bool operator== (const CVector3f& Vector) const;
	bool operator!= (const CVector3f& Vector) const;

	// arithmetic operations
	CVector3f operator+ (const CVector3f& Vector) const;
	CVector3f operator- (const CVector3f& Vector) const;
	CVector3f operator* (float fScalar) const;
	CVector3f operator/ (float fScalar) const;
	CVector3f operator- () const;

	// arithmetic updates
	CVector3f& operator+= (const CVector3f& Vector);
	CVector3f& operator-= (const CVector3f& Vector);
	CVector3f& operator*= (float fScalar);
	CVector3f& operator/= (float fScalar);

protected:
	float m_afEntry[3];
};
//--------------------------------------------------------------------------------
#endif // CVector3f_h
