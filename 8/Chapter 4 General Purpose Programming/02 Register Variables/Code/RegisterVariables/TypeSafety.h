/*==================================================================================================================================*/
/** @name TypeSafety.h 
*
*   DESCRIPTION: Defines some standard types and type safety for structs.
*
*   @field File         TypeSafety.h
*   @field Created      9/21/2009 10:24:16 AM
*************************************************************************************************************************************/

#pragma once

// Defines.
#define STRING2(x)	(#x)			// Helper for converting a macro to a string.
#define STRING(x)	STRING2(x)		// Converts a macro to a string.


#define INDEX_NONE  -1
#define MAX_INT		2147483647  
#define MAX_WORD	0xffff

#define ARRAY_COUNT( array )					((int)(sizeof(array) / sizeof((array)[0])))
#define ARRAY_SIZE( array )						((int)sizeof(array))


typedef unsigned char			BYTE;		// 8-bit  unsigned.
typedef unsigned short  		WORD;		// 16-bit unsigned.
typedef unsigned int  			DWORD;		// 32-bit unsigned.


// ***********************************************************************************************************************************
// FName TypeSafety.
#define DECLARE_FNAME_CLASS_TYPESAFETY( InClass, InBaseClass )										\
	protected:																						\
		typedef InClass		ThisClass;																\
		typedef InBaseClass Super;																	\
	public:																							\
		virtual FName GetSuperClassType( FName InComponentType ) const								\
		{																							\
			FName SuperType = NAME_None;															\
			if (InClass::StaticGetClassType() == InComponentType)									\
			{																						\
				SuperType = Super::StaticGetClassType();											\
			}																						\
			else if (InComponentType != NAME_None)													\
			{																						\
				SuperType = Super::GetSuperClassType( InComponentType );							\
			}																						\
			return SuperType == InComponentType ? NAME_None : SuperType;							\
		}																							\
		virtual FName GetClassType() const															\
		{																							\
			return InClass::StaticGetClassType();													\
		}																							\
		static FName StaticGetClassType()															\
		{																							\
			static FName TypeName = FName( STRING( InClass ) );										\
			return TypeName;																		\
		}	
#define DECLARE_FNAME_BASECLASS_TYPESAFETY( InClass )												\
		DECLARE_FNAME_CLASS_TYPESAFETY( InClass, InClass )											\
		template<class T> bool IsA() const															\
		{																							\
			return IsA( T::StaticGetClassType() );													\
		}																							\
		bool IsA( const FName& InTypeName ) const													\
		{																							\
			for (FName Type = GetClassType(); Type != NAME_None; Type = GetSuperClassType( Type ))	\
			{																						\
				if (Type == InTypeName)																\
				{																					\
					return true;																	\
				}																					\
			}																						\
			return false;																			\
		}				


// EOF
/*==================================================================================================================================*/
