#include "log.h"

std::ofstream out("log.txt");

Log::Log()
{}

Log::~Log()
{
	if(out.good())
		out.close();
}

void Log::Print(char c[])
{
	out << c << std::endl;
}
std::ofstream& Log::operator<<(char c[]){out << c; return out;}
std::ofstream& Log::operator<<(int i){out << i; return out;}
std::ofstream& Log::operator<<(float f){out << f; return out;}
std::ofstream& Log::operator<<(bool b){if(b)out << "True"; else out << "False"; return out;}
std::ofstream& Log::operator<<(D3DXVECTOR3 v){out << "x: " << v.x << ", y: " << v.y << ", z: " << v.z;return out;}