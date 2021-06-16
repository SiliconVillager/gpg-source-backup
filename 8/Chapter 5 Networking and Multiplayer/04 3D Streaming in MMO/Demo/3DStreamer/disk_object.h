//        File: disk_object.h
// Description: Define disk object
// 
// Copyright (C) 2009 Kevin Kaichuan He.  All rights reserved. 
//
#ifndef DISK_OBJECT_H
#define DISK_OBJECT_H
#include "file_object.h"
#include "leaky_bucket.h"

class DiskObject: public FileObject 
{
public:
	virtual void Load(LeakyBucket* bucket);
	DiskObject(const char* fileName, FileQueueManager* queueMgr, int bufSize = 1024 * 1024);
	~DiskObject();
};

#endif // DISK_OBJECT_H