#ifndef FILE_OBJECT_H
#define FILE_OBJECT_H
#include <windows.h>
#include "file_queue.h"
#include "leaky_bucket.h"

// Asynchronous File Read Interface for local disk read and remote HTTP read
class FileObject
{	
	friend FileQueueManager;
public:
	typedef enum {
		SOURCE_DISK,
		SOURCE_HTTP
	} DataSource;

	FileObject(const char* path, FileQueueManager* queueMgr, int bufSize);
	~FileObject();
	// Schedule the file object to be loaded
	void Enqueue(FileQueue::QueueType priority);
	bool Requeue(FileQueue::QueueType priority);
	void Cancel();
	void SetQueue( FileQueue::QueueType priority );
	FileQueue::QueueType GetQueue();

	// Wait until the file object is loaded
	void Wait();
	// Read data sequentially out of an object after it is loaded
	void Read(char* buf, int bytesToRead);
	char* Path() const { return (char*) m_path; }
	FileQueue::iterator m_pos;  // position of file object in file queue
	bool m_loaded;

protected:
	// Load the file object from data source (HTTP or Disk)
	virtual void Load(LeakyBucket* bucket) = 0; 
	HANDLE m_dataReady; 
	FileQueue::QueueType m_queue;    // queue id
	FileQueueManager* m_queueMgr;
	char* m_buffer;
	int m_bufferSize; 
	int m_readOff;				// file buffer offset for reading
	char m_path[MAX_PATH];
};
#endif