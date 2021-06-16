#ifndef _LOG_H
#define _LOG_H

#include <d3dx9.h>
#include <fstream>

#define KEYDOWN(vk_code) ((GetAsyncKeyState(vk_code) & 0x8000) ? 1 : 0)
#define KEYUP(vk_code)   ((GetAsyncKeyState(vk_code) & 0x8000) ? 0 : 1)
#ifdef _DEBUG 
 #define ASSERT(x) if (!(x)) *(int*)0 = 1
#else
 #define ASSERT(x)
#endif

class Log{
	public:
		Log();
		~Log();
		void Print(char c[]);
		std::ofstream& operator<<(char c[]);
		std::ofstream& operator<<(int i);
		std::ofstream& operator<<(float f);
		std::ofstream& operator<<(bool b);
		std::ofstream& operator<<(D3DXVECTOR3 v);
		void Endl(int nr);
};

static Log s_log;

#endif