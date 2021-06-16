//        File: file_queue.cpp
// Description: File Queue
// 
// Copyright (C) 2009 Kevin Kaichuan He.  All rights reserved. 
//
#include <windows.h>
#include <vector>
#include <list>
#include "file_queue.h"
#include "file_object.h"
#include "log.h"

FileQueue::FileQueue() 
{
	m_mutex = CreateMutex(NULL, FALSE, "FileQueueLock");
	if (m_mutex == NULL)
		s_log << "Error in CreateMutex" << std::endl;
}

FileQueue::~FileQueue() 
{
	CloseHandle(m_mutex);
}

void FileQueue::Lock()
{
	WaitForSingleObject(m_mutex, INFINITE);
}

void FileQueue::Unlock()
{
	ReleaseMutex(m_mutex);
}

FileQueueManager::FileQueueManager(int Mbps): m_fileQueues(FileQueue::NUM_OF_QUEUES), 
											  m_fileQueueThread(NULL), m_fileQueueThreadStop(NULL), m_fileQueueThreadKick(NULL),
											  m_leakyBucket(Mbps, 256 * 1024 /* 256KB */)
{
	for (int i = 0; i < FileQueue::NUM_OF_QUEUES; i++)
		m_fileQueues[i] = new FileQueue();
	m_fileQueueThreadKick = CreateEvent(NULL,				// default security attributes
										FALSE,				// automatic-reset
										FALSE,				// unsignaled
										"g_fileQueueThreadKick");
	if (m_fileQueueThreadKick == NULL)
		s_log << "CreateEvent error for g_fileQueueThreadKick" << std::endl;

	m_fileQueueThreadStop = CreateEvent(NULL,				// default security attributes
										FALSE,				// automatic-reset
										FALSE,				// unsignaled
										"g_fileQueueThreadStop");
	if (m_fileQueueThreadStop == NULL)
		s_log << "CreateEvent error for g_fileQueueThreadStop" << std::endl;
	m_handles[0] = m_fileQueueThreadStop;
	m_handles[1] = m_fileQueueThreadKick;

	m_fileQueueThread = CreateThread(NULL,      // default security
 									0,			// default stack size
									&FileQueueManager::FileQueueReadThread,  // name of the thread function
									this,		// thread parameter
									0,			// default startup flags
									NULL);      // no thread id needed
}

FileQueueManager::~FileQueueManager()
{
	DWORD dwWaitResult;
	SetEvent(m_fileQueueThreadStop);
	dwWaitResult = WaitForSingleObject(m_fileQueueThread, INFINITE);
	if (dwWaitResult == WAIT_OBJECT_0)
	{
		s_log << "g_fileQueueThread ended" << std::endl;
		CloseHandle(m_fileQueueThread);	
	} else
	{
		s_log << "WaitForSingleObject for thread error: " << GetLastError() << std::endl;
	} 
	CloseHandle(m_fileQueueThreadKick);
	CloseHandle(m_fileQueueThreadStop);

	for (int i = 0; i < FileQueue::NUM_OF_QUEUES; i++)
		delete m_fileQueues[i];

}

// Called by main thread
bool FileQueueManager::CancelFileObject( FileObject* obj )
{
	FileQueue::QueueType oldQueueId = obj->GetQueue();
	if (oldQueueId != FileQueue::QUEUE_NONE)
	{
		// Remove from the old queue
		FileQueue* oldQueue = m_fileQueues[oldQueueId];
		oldQueue->Lock();
		if (obj->GetQueue() == FileQueue::QUEUE_NONE)
		{
			// The object was just dequeued by the download thread,  no need to dequeue it again
			return FALSE;
		}
		// We locked the queue before the download thread tries to dequeue it
		oldQueue->erase(obj->m_pos);
		obj->SetQueue(FileQueue::QUEUE_NONE);
		// Download thread can not find it from the old queue any more
		oldQueue->Unlock();
		return TRUE;
	}
	return FALSE;
}

// Enqueue a newly created FileObject
// Called by main thread 
bool FileQueueManager::EnqueueFileObject( FileObject* obj, FileQueue::QueueType priority )
{
	// Add to the new queue
	FileQueue* newQueue = m_fileQueues[priority];
	newQueue->Lock();
	newQueue->push_back(obj);
	obj->SetQueue(priority);
	obj->m_pos = newQueue->end();
	obj->m_pos--;
	newQueue->Unlock();
	// Kick the download thread
	SetEvent(m_fileQueueThreadKick);
	return TRUE;
}

// Requeue an existing FileObject to a different queue
// Called by main thread 
bool FileQueueManager::RequeueFileObject( FileObject* obj, FileQueue::QueueType priority )
{
		if (!CancelFileObject(obj))
			return FALSE;
		return EnqueueFileObject(obj, priority);
}

int FileQueueManager::GetQueueLength( FileQueue::QueueType priority )
{
	return (int)m_fileQueues[priority]->size();
}

double FileQueueManager::GetDownloadRate()
{
	return m_leakyBucket.GetRate();
}

DWORD WINAPI FileQueueManager::FileQueueReadThread( LPVOID lpParameter )
{
	DWORD dwWaitResult = 0;
	int i;
	FileQueueManager* mgr = (FileQueueManager*)lpParameter;

	while (1)
	{
		for (i = 0; i < FileQueue::NUM_OF_QUEUES; i++)
		{
			FileQueue* queue = mgr->m_fileQueues[i];
			queue->Lock();
			if (!queue->empty())
			{
				FileObject* obj = queue->front();
				queue->pop_front();
				obj->SetQueue(FileQueue::QUEUE_NONE);
				s_log << "FileQueueReadThread just dequeued " << obj->Path() << "from queue:"<< i << std::endl;
				queue->Unlock();
				obj->Load(&mgr->m_leakyBucket);
				s_log << "FileQueueReadThread just loaded " << obj->Path() << "from queue:"<< i << std::endl;
				break;
			}
			queue->Unlock();
		}
		if (i < FileQueue::NUM_OF_QUEUES) 
			continue;
		dwWaitResult = WaitForMultipleObjects(2,			// number of handles in array
											  mgr->m_handles,	// array of thread handles
											  FALSE,        // wait until one of the handles is signaled
											  INFINITE
											  );
		if (dwWaitResult == WAIT_OBJECT_0)
		{
			s_log << "Received STOP command" << std::endl;
			break;
		}
	}
	return 1;
}