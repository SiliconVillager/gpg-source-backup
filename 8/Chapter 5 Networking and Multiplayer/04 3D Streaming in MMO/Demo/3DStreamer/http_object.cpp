//        File: http_object.cpp
// Description: Http Object
// 
// Copyright (C) 2009 Kevin Kaichuan He.  All rights reserved. 
//
#include <windows.h>
#include <wininet.h>
#include "log.h"
#include "http_object.h"

HANDLE HttpObject::s_httpSession = NULL;
HANDLE HttpObject::s_httpConnect = NULL;

void HttpObject::InitHTTPSession(const char* hostName)
{
	s_httpSession = InternetOpen("3DStreamer Client", INTERNET_OPEN_TYPE_PRECONFIG, NULL, NULL, 0);
	if (s_httpSession == NULL)
	{
		s_log<<"InternetOpen fails"<<std::endl;
		return;
	}
	s_httpConnect = InternetConnect(s_httpSession, hostName, INTERNET_DEFAULT_HTTP_PORT, NULL, NULL, 
		INTERNET_SERVICE_HTTP, 0, NULL);
	if (s_httpConnect == NULL) 
	{
		s_log<<"InternetConnect fails"<<std::endl;
		return;
	}
}

void HttpObject::DestroyHTTPSession()
{
	if (s_httpConnect)
	{
		InternetCloseHandle(s_httpConnect);
		s_httpConnect = NULL;
	}

	if (s_httpSession)
	{
		InternetCloseHandle(s_httpSession);
		s_httpSession = NULL;
	}
}

void HttpObject::Load(LeakyBucket* bucket)
{
	DWORD bytesRead = 0;
	DWORD dataRead = 0;
	BOOL ret = FALSE;

	s_log << "HttpObject::Load " << m_path << std::endl;
	HANDLE hRequest = HttpOpenRequest(s_httpConnect, "GET", m_path,  NULL, NULL, NULL, 
									  INTERNET_FLAG_NO_UI | INTERNET_FLAG_RELOAD | INTERNET_FLAG_NO_CACHE_WRITE, NULL);
	if (hRequest == NULL)
	{
		s_log << "HttpOpenRequest for " << m_path << "fails" << std::endl;
		return;
	}
	if (!HttpSendRequest(hRequest, NULL, 0, NULL, 0))
	{
		s_log << "HttpSendRequest error for " << m_path << std::endl;
		return;
	}
	do {
		ret = InternetReadFile(hRequest, m_buffer + dataRead, m_bufferSize - dataRead , (LPDWORD)&bytesRead);
		if (ret == FALSE)
		{
			if (GetLastError() == ERROR_IO_PENDING)
			{
				s_log<<"InternetReadFile I/O pending"<<std::endl;
				continue;
			} else
			{
				s_log << "InternetReadFile error:" << std::endl;
				CloseHandle(hRequest);
				return;
			}
		}
		if (bytesRead > 0)
		{
			int ms = bucket->Update(bytesRead);
			if (ms)
				Sleep(ms);
			dataRead += bytesRead;
		}
	} while (bytesRead);
	m_loaded = true;
	s_log << "HttpObject::Load " << m_path << " Done!" << std::endl;
	SetEvent(m_dataReady);	
	InternetCloseHandle(hRequest);
}

HttpObject::HttpObject( const char* fileName, FileQueueManager* queueMgr, int bufSize ) : 
						FileObject(fileName, queueMgr, bufSize)
{}

HttpObject::~HttpObject()
{}