//-------------------------------------------------------------------
// In Game Memory Profiler
// by Ricky Lung Man Tat (mtlung@gmail.com)


#ifndef __PROFILER_TIMER__
#define __PROFILER_TIMER__

#include "SharedLib.h"

typedef unsigned __int64 uint64_t;

namespace Profiler {

/*!	Class for storing a time interval value.
	The TimeInterval stores number of CPU clock ticks using 64 bits integer as the time interval
 */
class PROFILER_API TimeInterval
{
public:
	TimeInterval() : mTicks(0)	{}

	explicit TimeInterval(uint64_t ticks) {
		set(ticks);
	}

	TimeInterval(double second) {
		set(second);
	}

	void set(const uint64_t ticks) {
		mTicks = ticks;
	}

	void set(double sec);

	//! Get the clock ticks.
	uint64_t asTicks() const {
		return mTicks;
	}

	//! Get the time in unit of second.
	double asSecond() const;

	//! Get the longest time that can represented by TimeInterval.
	static TimeInterval getMax();

	bool operator ==(const TimeInterval& rhs) const {
		return (mTicks == rhs.mTicks);
	}

	bool operator<(const TimeInterval& rhs) const {
		return (mTicks < rhs.mTicks);
	}

	TimeInterval& operator+=(const TimeInterval& rhs) {
		mTicks += rhs.mTicks;
		return *this;
	}
	TimeInterval operator+(const TimeInterval& rhs) const {
		return (TimeInterval(*this) += rhs);
	}

	/*!	Subtract from another TimeInterval.
		If rhs is larger than this, the result will be trimmed to zero.
	 */
	TimeInterval& operator-=(const TimeInterval& rhs)
	{
		mTicks = rhs.mTicks > mTicks ? 0 : mTicks - rhs.mTicks;
		return *this;
	}

	TimeInterval operator-(const TimeInterval& rhs) const {
		return (TimeInterval(*this) -= rhs);
	}

	friend TimeInterval operator*(double lhs, const TimeInterval& rhs) {
		TimeInterval ret;
		ret.mTicks = uint64_t(rhs.mTicks * lhs);
		return ret;
	}

	friend TimeInterval operator*(const TimeInterval& lhs, double rhs) {
		TimeInterval ret;
		ret.mTicks = uint64_t(lhs.mTicks * rhs);
		return ret;
	}

private:
	friend uint64_t getTimeSinceMachineStartup();
	uint64_t mTicks;
};	// TimeInterval

//! A timer to measure time interval.
class PROFILER_API Timer
{
public:
	Timer();

	//! Get the time-elapsed since last reset
	TimeInterval get() const;

	//! Reset the timer
	TimeInterval reset();

protected:
	mutable TimeInterval mLastGetTime;	//!< Record when the last get() is called (to prevent negative delta time)
	TimeInterval mStartTime;			//!< Record when the timer is created
};	// Timer

//! A timer to measure delta time between each call.
class PROFILER_API DeltaTimer
{
public:
	DeltaTimer();

	/*!	Since when the delta timer is called the first time, there is no delta (delta=0 or very very small)
	 	To prevent zero delta, the first delta can be specified in the constructor
	 */
	explicit DeltaTimer(const TimeInterval& firstDelta);

	//! Get the delta time since last getDelta()
	TimeInterval getDelta() const;

protected:
	Timer mTimer;
	mutable TimeInterval mLastTime;
};	// DeltaTimer

}	// namespace Profiler

#endif	// __PROFILER_TIMER__
