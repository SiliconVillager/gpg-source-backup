//        File: http_object.h
// Description: Http Object
// 
// Copyright (C) 2009 Kevin Kaichuan He.  All rights reserved. 
//
#ifndef HTTP_OBJECT_H
#define HTTP_OBJECT_H
#include <windows.h>
#include <WinInet.h>
#include "file_object.h"
#include "leaky_bucket.h"

class HttpObject: public FileObject
{
public:
	HttpObject( const char* fileName, FileQueueManager* queueMgr, int bufSize = 1024 * 1024);
	~HttpObject();
	static void InitHTTPSession(const char* hostName);
	static void DestroyHTTPSession();
	virtual void Load(LeakyBucket* bucket);
private:
	static HINTERNET s_httpSession;
	static HINTERNET s_httpConnect;
};

#endif // HTTP_OBJECT_H