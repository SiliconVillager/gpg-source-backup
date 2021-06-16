//        File: disk_object.cpp
// Description:  Disk File Object
// 
// Copyright (C) 2009 Kevin Kaichuan He.  All rights reserved. 
//
#include <windows.h>
#include <stdio.h>
#include "log.h"
#include "disk_object.h"
#include "leaky_bucket.h"

void DiskObject::Load(LeakyBucket* bucket)
{
	HANDLE hIn = CreateFile(m_path, GENERIC_READ, 0, NULL, OPEN_EXISTING, 0, NULL);
	if (hIn == INVALID_HANDLE_VALUE)
	{
		s_log << "Error in std::ifstream for file: " << m_path << std::endl;
		return ;
	}
	DWORD bytesRead = 0;
	char* buffer = m_buffer;
	DWORD bufSize = m_bufferSize;
	
	while (ReadFile(hIn, buffer, bufSize, &bytesRead, NULL) && bytesRead > 0)
	{
		if (bytesRead > 0)
		{
			int ms = bucket->Update(bytesRead);
			if (ms)
				Sleep(ms);
			buffer += bytesRead;
			bufSize -= bytesRead;
		}
	}
	CloseHandle(hIn);
	m_loaded = TRUE;
	if (!SetEvent(m_dataReady))
		s_log << "Error in SetEvent from DiskObject::Load" << std::endl;
}

DiskObject::DiskObject( const char* fileName, FileQueueManager* queueMgr, int bufSize /*= 1024 * 1024*/) : 
					  FileObject(fileName, queueMgr, bufSize)
{}

DiskObject::~DiskObject()
{}