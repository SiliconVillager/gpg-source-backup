//        File: leaky_bucket.cpp
// Description: Leaky Bucket
// 
// Copyright (C) 2009 Kevin Kaichuan He.  All rights reserved. 
//
#include <windows.h>
#include "leaky_bucket.h"

LeakyBucket::LeakyBucket( double fillRate, int burstBytes ) : m_fillRate(fillRate), m_burstSize(burstBytes),
														      m_credits(0), m_tick(GetTickCount()), m_rate(0)
{}

LeakyBucket::~LeakyBucket()
{}

// Return: 0 - accepted,  >0 - Out of credits, number of ms to hold off before next update
int LeakyBucket::Update( int bytesRcvd )
{
	ULONGLONG tick = GetTickCount();
	int deltaMs = (int)(tick - m_tick);
	if (deltaMs > 0)
	{
		// Update the running average of the rate
		m_rate = 0.5 * m_rate + 0.5 * (double)bytesRcvd * 8 / 1024 / (double)deltaMs;
		m_tick = tick;
	}

	// Refill the bucket
	m_credits += (int)(m_fillRate * deltaMs * 1024 * 1024 / 1000 / 8);
	if (m_credits > m_burstSize)
		m_credits = m_burstSize;
	// Leak the bucket
	m_credits -= bytesRcvd;
	if (m_credits >= 0)
		return 0;
	else 
	{
		return (int)((double)(-m_credits) * 8 * 1000 / (1024 * 1024) / m_fillRate);
	}
}

double LeakyBucket::GetRate()
{
	return m_rate;
}