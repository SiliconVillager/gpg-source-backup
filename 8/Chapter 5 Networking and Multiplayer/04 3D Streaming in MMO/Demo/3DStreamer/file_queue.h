//        File: file_queue.h
// Description: Define 4 priority queues for preloading file objects
// 
// Copyright (C) 2009 Kevin Kaichuan He.  All rights reserved. 
//
#ifndef FILE_QUEUE_H
#define FILE_QUEUE_H

#include <vector>
#include <list>
#include "leaky_bucket.h"
class FileObject;

typedef std::list<FileObject*> FileObjectList;

class FileQueue : public FileObjectList
{
public:
	enum QueueType {
		QUEUE_NONE = -1,
		QUEUE_CRITICAL = 0,
		QUEUE_HIGH,
		QUEUE_MEDIUM,
		QUEUE_LOW,
		NUM_OF_QUEUES
	};
	FileQueue();
	~FileQueue();
	void Lock();
	void Unlock();
private:
	HANDLE m_mutex;
};

class FileQueueManager 
{
private:
	static DWORD WINAPI FileQueueReadThread(LPVOID lpParameter);
public:
	FileQueueManager(int Mbps);
	~FileQueueManager();
	
	bool CancelFileObject( FileObject* obj );
	bool EnqueueFileObject( FileObject* obj, FileQueue::QueueType priority );
	bool RequeueFileObject( FileObject* obj, FileQueue::QueueType priority );
	int GetQueueLength(FileQueue::QueueType priority );
	double GetDownloadRate();

	std::vector<FileQueue*> m_fileQueues;
	HANDLE m_fileQueueThread;
	HANDLE m_fileQueueThreadStop;
	HANDLE m_fileQueueThreadKick;
	HANDLE m_handles[2];
	LeakyBucket m_leakyBucket; 
};

#endif