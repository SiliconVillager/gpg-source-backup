#include "assert.h"
#include <Windows.h>

void f1();
void f2(int dummy);
void f3(int* dummy);

int main( int argc, char* argv[] )
{
	f1();
}

void f1()
{
	f2(5);
}

void f2( int dummy )
{
	f3(NULL);
}

void f3( int* dummy )
{
	ASSERT( dummy );
}