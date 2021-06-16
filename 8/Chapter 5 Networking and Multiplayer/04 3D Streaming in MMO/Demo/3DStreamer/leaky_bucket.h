//        File: leaky_bucket.h
// Description: Leaky Bucket
// 
// Copyright (C) 2009 Kevin Kaichuan He.  All rights reserved. 
//
#ifndef LEAKY_BUCKET_H
#define LEAKY_BUCKET_H
#include <Windows.h>
class LeakyBucket 
{
public:
	LeakyBucket(double fillRate, int burstBytes);
	~LeakyBucket();
	// Return: 0 - accepted,  >0 - Out of credits, number of ms to wait
	int Update(int bytesRcvd);
	// Get the current running average of bit rate in unit of Mbps
	double GetRate();
private:
	double m_fillRate;
	int m_burstSize;
	DWORD m_tick;
	double m_rate;
	int m_credits;
};
#endif