// Utils.h:
// Date: 04/14/08
// Author: Aurelio Reis

#ifndef __AR__UTILS__H__
#define __AR__UTILS__H__

#include <vector>
#include <map>
#include <queue>
#include <list>
#include <ctime>
using namespace std;

#include "Engine/External/TinyXML/tinyxml.h"

#define SQLITE_THREADSAFE	1
#include "Engine/External/Sqlite/sqlite3.h"

#undef min
#undef YES
#undef NO
#pragma warning(disable : 4996)
#include "Engine/External/glm/glm.h"
#include "Engine/External/glm/glmext.h"
#define YES 1
#define NO 0
namespace glm { using GLM_GTX_transform; }

#include "Engine/Shared/System.h"
#include "Engine/Shared/FileSystem.h"
#include "assert.h"

#ifdef _WIN32
	//#include <winsock2.h>
	#include "Windows.h"
	#undef DrawText
	#undef GetVersion
	#undef GetClassInfo

	#define AR_INLINE	__forceinline
	#define AR_CALLBACK WINAPI

	#include <GL/gl.h>
	#include "Engine/External/gl/glext.h"

	//#include <OpenGL/glu.h>
#elif defined( _IPHONE )
	#define AR_INLINE	inline
	#define AR_CALLBACK

	#include <OpenGLES/EAGL.h>
	#include <OpenGLES/ES1/gl.h>
	#include <OpenGLES/ES1/glext.h>
#else
	#error
#endif


#define ACCESSOR_GETSET_STRING( _Var, _Name )																	\
	void Set##_Name( const char *Value ) { AR_DeleteString( _Var ); _Var = AR_CreateString( Value ); }			\
	const char *Get##_Name() const { return _Var; }

#define ACCESSOR_GETSET_STRING_DEFINE( _Name )																	\
	void Set##_Name( const char *Value );																		\
	const char *Get##_Name() const;

#define ACCESSOR_GETSET_STRING_DECLARE( _Class, _Var, _Name )													\
	void _Class::Set##_Name( const char *Value ) { AR_DeleteString( _Var ); _Var = AR_CreateString( Value ); }	\
	const char *_Class::Get##_Name() const { return _Var; }

#define ACCESSOR_GETSET_STRING_PUREVIRT( _Name )																\
	virtual void Set##_Name( const char *Value ) = 0;															\
	virtual const char *Get##_Name() const = 0;

#define ACCESSOR_GETSET_REF( _Type, _Var, _Name )									\
	void Set##_Name( _Type &Value ) { _Var = Value; }								\
	const _Type &Get##_Name() const { return _Var; }								\
	_Type &Get##_Name() { return _Var; }

#define ACCESSOR_GETSET_PTR( _Type, _Var, _Name )									\
	void Set##_Name( _Type *Value ) { _Var = Value; }								\
	const _Type *Get##_Name() const { return _Var; }								\
	_Type *Get##_Name() { return _Var; }

#define ACCESSOR_GETSET_PTR_PUREVIRT( _Type, _Name )								\
	virtual void Set##_Name( _Type *Value ) = 0;									\
	virtual const _Type *Get##_Name() const = 0;									\
	virtual _Type *Get##_Name() = 0;

#define ACCESSOR_GET_PTR( _Type, _Var, _Name )										\
	const _Type *Get##_Name() const { return _Var; }								\
	_Type *Get##_Name() { return _Var; }

#define ACCESSOR_GET_CONSTPTR( _Type, _Var, _Name )									\
	const _Type *Get##_Name() const { return _Var; }								\

#define ACCESSOR_GETSET_VAL( _Type, _Var, _Name )									\
	void Set##_Name( const _Type &Value ) { _Var = Value; }							\
	const _Type Get##_Name() const { return _Var; }									\
	_Type Get##_Name() { return _Var; }

#define ACCESSOR_GETSET_VAL_PUREVIRT( _Type, _Name )								\
	virtual void Set##_Name( const _Type &Value ) = 0;								\
	virtual const _Type Get##_Name() const = 0;										\
	virtual _Type Get##_Name() = 0;

#define ACCESSOR_GETSET_REF_PUREVIRT( _Type, _Name )								\
	virtual void Set##_Name( _Type &Value ) = 0;									\
	virtual const _Type &Get##_Name() const = 0;									\
	virtual _Type &Get##_Name() = 0;

// TODO: Range check.
#define ACCESSOR_GETSET_ARRAY( _Type, _Var, _Size, _Name )							\
	void Set##_Name( _Type &Value, uint32 uiIndex ) { _Var[ uiIndex ] = Value; }	\
	const _Type Get##_Name( uint32 uiIndex ) const { return _Var[ uiIndex ]; }		\
	_Type Get##_Name( uint32 uiIndex) { return _Var[ uiIndex ]; }



template< typename Type >
void SAFE_DELETE( Type *&pObject ) { delete pObject; pObject = NULL; }

template< typename Type >
void SAFE_DELETE_ARRAY( Type *&pObject ) { delete [] pObject; pObject = NULL; }


#define DEFINE_SUBCLASS( _SuperClass )												\
	typedef _SuperClass super;


#define AR_SET_BIT( _Bit )		1 << _Bit


AR_INLINE uint32 ColorRGBA( float r, float g, float b, float a )
{
	return ( ( uint32( r * 255.0f ) & 0xFF ) | ( uint32( g * 255.0f ) & 0xFF ) << 8 | ( uint32( b * 255.0f ) & 0xFF ) << 16 | ( uint32( a * 255.0f ) & 0xFF ) << 24 );
}


template< typename Type >
inline Type AR_Clamp( const Type &Val, Type Min, Type Max ) { return min( max( Val, Min ), Max ); }

template< typename Type >
inline Type AR_Interpolate( const Type &Src, const Type &Dst, float fPercent ) { return Src + fPercent * ( Dst - Src ); }

template< typename Type >
inline Type AR_Interpolate2( const Type &Src, const Type &Dst, float fPercent ) { return Src * ( 1.0f - fPercent ) + fPercent * Dst; }

// Types:
typedef unsigned char byte;
typedef unsigned int uint32;


template < typename Type >
Type AR_VerifyFunc( Type Value, const char *strMsg )
{
	if ( Value ) { return Value; }

	throw( CArError( strMsg ) );

	return Value;
}


#define AR_Assert( _Condition )							assert( ( _Condition ) )
#define AR_Error( _ErrorMsg )							AR_DebugMessage( _ErrorMsg ); throw( CArError( ( _ErrorMsg ) ) )
#define AR_Verify( _Condition )							AR_VerifyFunc( _Condition, AR_VarArg( "Verify failed at '%s'", __FUNCTION__ ) )
#define AR_VerifyWithError( _Condition, _ErrorMsg )		AR_VerifyFunc( _Condition, _ErrorMsg )


//////////////////////////////////////////////////////////////////////////
// CArError
//////////////////////////////////////////////////////////////////////////

class CArError
{
private:
	const char *m_strError;

public:
	CArError( const char *strError ) : m_strError( strError ) { AR_Assert( !"Error thrown!" ); }
	~CArError() {}

	const char *GetError() const { return m_strError; }
};


#ifdef _FINAL
	#define AR_DebugOut( _Text )
#else
	#ifdef _WIN32
		#define AR_DebugOut( _Text )	OutputDebugStringA( _Text )
	#else
		#define AR_DebugOut( _Text )
	#endif
#endif // _FINAL


inline char *AR_CreateString( const char *strString, int iLen = -1 )
{
	if ( iLen == -1 )
	{
		iLen = (int)strlen( strString );
	}

	int iNewSize = iLen + 1;
	char *strNewString = new char[ iNewSize ];
#ifdef _WIN32
	strcpy_s( strNewString, iNewSize, strString );
#elif defined (_IPHONE )
	strcpy( strNewString, strString );
#else
	#error
#endif

	return strNewString;
}

inline void AR_DeleteString( char *&strString ) { SAFE_DELETE_ARRAY( strString ); }

#ifdef _WIN32
	#pragma warning( disable : 4996 )
#endif

#ifdef _IPHONE
// TODO: Implement properly (i.e. toupper).
inline int stricmp( const char *a, const char *b ) { return strcmp( a, b ); }
#endif

inline int AR_StringCompare( const char *str0, const char *str1 ) { return strcmp( str0, str1 ); }
inline int AR_StringCompareI( const char *str0, const char *str1 ) { return stricmp( str0, str1 ); }

inline void AR_StringCopy( char *str0, const char *str1 ) { strcpy( str0, str1 ); }
inline void AR_StringCopyN( char *str0, const char *str1, int iNumCharsToCopy ) { strncpy( str0, str1, iNumCharsToCopy ); }

// Format and copy a string with variable argument into a temp buffer.
const char *AR_VarArg( const char *strFormat, ... );

// Print a message to the console and log it.
extern void AR_MessageFunc( const char *strMsg );

#define AR_DebugMessage( _Format, ... )		AR_MessageFunc( AR_VarArg( _Format, __VA_ARGS__ ) )


//////////////////////////////////////////////////////////////////////////
// CArProfileScopeGuard
// Desc: Used to profile functions.
//////////////////////////////////////////////////////////////////////////

class CArProfiler
{
private:
	float m_fInitialTime;

public:
	CArProfiler() {}
	~CArProfiler() {}

	void Begin()
	{
		m_fInitialTime = CArSystem::GetAbsTime();
	}

	void End( const char *strFuncName )
	{
		float fFinalTime = CArSystem::GetAbsTime() - m_fInitialTime;
		AR_DebugMessage( "Profile '%s' Result: Time was %2.2f seconds\n", strFuncName, fFinalTime );
	}
};

//////////////////////////////////////////////////////////////////////////
// CArProfileScopeGuard
// Desc: Used to profile functions.
//////////////////////////////////////////////////////////////////////////

class CArProfileScopeGuard
{
private:
	const char *m_strFuncName;
	CArProfiler m_Profiler;

public:
	CArProfileScopeGuard( const char *strFuncName ) : m_strFuncName( strFuncName )
	{
		m_Profiler.Begin();
	}

	~CArProfileScopeGuard() 
	{
		m_Profiler.End( m_strFuncName );
	}
};

#if 1
	#define PROFILE_SCOPE()		CArProfileScopeGuard ProfileGuard( __FUNCTION__ )
#else
	#define PROFILE_SCOPE()
#endif


// TODO: ArMath.h
const float AR_PI = 3.14159265f;
const float AR_PI_DIV_180 = 0.01745329f;
const float AR_180_DIV_PI = 57.29577957f;
inline float AR_DegToRad( float fDegrees ) { return fDegrees * AR_PI_DIV_180; }
inline float AR_RadToDeg( float fRadians ) { return fRadians * AR_180_DIV_PI; }

// Please define singleton in public scope.
#define DEFINE_SINGLETON( _Class )																				\
	private:																									\
		static _Class *s_pInstance;																				\
		_Class();																								\
	public:																										\
		static void CreateInstance() { AR_Assert( !s_pInstance ); s_pInstance = new _Class(); }					\
		static void DestroyInstance() { SAFE_DELETE( s_pInstance ); }											\
		static _Class *GetInstance() { return s_pInstance; }

#define DECLARE_SINGLETON( _Class )																				\
	_Class *_Class::s_pInstance = NULL;


#ifdef _DEBUG
	#define DERIVE_INTERFACE( _Class )	: public _Class
#else
	#define DERIVE_INTERFACE( _Class )
#endif


extern void AREngine_Main( int argc, const char *argv[] );
extern void AREngine_Run();
extern void AREngine_PlatformInit();


#endif // __AR__UTILS__H__
