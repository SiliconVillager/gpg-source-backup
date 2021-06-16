//        File: file_object.cpp
// Description: File Object
// 
// Copyright (C) 2009 Kevin Kaichuan He.  All rights reserved. 
//
#include "log.h"
#include "file_object.h"
#include "file_queue.h"

FileObject::FileObject(const char* path, FileQueueManager* queueMgr, int bufSize): 
													   m_queueMgr(queueMgr),
													   m_readOff(0), 
													   m_loaded(FALSE), 
													   m_queue(FileQueue::QUEUE_NONE), 
													   m_bufferSize(bufSize)
{
	m_buffer = new char[bufSize];
	if (!m_buffer) 
	{
		s_log << "Error in allocating memory for FileObject:" << path << std::endl;	
		return;
	}
	strcpy_s(m_path, MAX_PATH, path);
	m_dataReady = CreateEvent(NULL,							     // default security attributes
						      FALSE,							 // automatic-reset
							  FALSE,                             // unsignaled
							  NULL);						     // name isn't necessary
}

FileObject::~FileObject()
{
	if (m_buffer)
	{
		delete m_buffer;
		m_buffer = NULL;
	}
	if (m_dataReady)
	{
		CloseHandle(m_dataReady);
		m_dataReady = NULL;
	}
}

// Enqueue the FileObject
void FileObject::Enqueue(FileQueue::QueueType priority)
{
	m_queueMgr->EnqueueFileObject(this, priority);
}

// Enqueue the FileObject
bool FileObject::Requeue(FileQueue::QueueType priority)
{
	return m_queueMgr->RequeueFileObject(this, priority);
}

// Wait until the file object is fetched
void FileObject::Wait()
{
	WaitForSingleObject( m_dataReady, INFINITE );
}

// Read data out of the file object
void FileObject::Read(char* buf, int bytesToRead)
{
	if (bytesToRead > m_bufferSize - m_readOff)
	{
		s_log << " Trying to read " << bytesToRead << "bytes although there are only " 
			  << m_bufferSize - m_readOff << " bytes left" << std::endl;
		return;
	}
	if (memcpy_s(buf, bytesToRead, &m_buffer[m_readOff], bytesToRead) == 0)
		m_readOff += bytesToRead;
}

void FileObject::SetQueue( FileQueue::QueueType priority )
{
	m_queue = priority;
}

FileQueue::QueueType FileObject::GetQueue()
{
	return m_queue;
}

void FileObject::Cancel()
{
	m_queueMgr->CancelFileObject(this);
}