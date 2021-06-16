//--------------------------------------------------------------------------------
// CLog
//
// The log class is a singleton that allows the application to write messages to 
// a file.
//
// Copyright (C) 2003-2009 Jason Zink. All rights reserved.
//--------------------------------------------------------------------------------
#ifndef CLog_h
#define CLog_h
//--------------------------------------------------------------------------------
#include <fstream>
#include <string>
//--------------------------------------------------------------------------------
class CLog 
{
protected:
	CLog();

	std::ofstream	AppLog;

public:

	static CLog& Get( );

	bool Open( );
	bool Close( );

	bool Write( const char *TextString );
	bool Write( std::string& TextString );
	bool WriteSeparater( );
};
//--------------------------------------------------------------------------------
#endif // CLog_h
//--------------------------------------------------------------------------------