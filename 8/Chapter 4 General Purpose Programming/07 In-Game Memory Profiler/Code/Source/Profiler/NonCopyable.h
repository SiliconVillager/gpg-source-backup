//-------------------------------------------------------------------
// In Game Memory Profiler
// by Ricky Lung Man Tat (mtlung@gmail.com)


#ifndef __PROFILER_NONCOPYABLE__
#define __PROFILER_NONCOPYABLE__

#include "SharedLib.h"

namespace Profiler {

#define sal_in
#define sal_in_opt
#define sal_in_z
#define sal_in_z_opt
#define sal_out_opt
#define sal_out_z
#define sal_inout
#define sal_notnull
#define sal_maybenull
#define sal_override
#define sal_checkreturn
#define sal_in_ecount(count)
#define sal_in_ecount_opt(count)
#define sal_out_ecount(count)
#define sal_out_ecount_opt(count)
#define nullptr 0
#define ABSTRACT_CLASS __declspec(novtable)

class PROFILER_API Noncopyable
{
protected:
	Noncopyable() {}
	~Noncopyable() throw() {}
private:	// Emphasize the following members are private
	Noncopyable(const Noncopyable&);
	const Noncopyable& operator=(const Noncopyable&);
};	// Noncopyable

}	// namespace Profiler

#endif	// __PROFILER_NONCOPYABLE__
