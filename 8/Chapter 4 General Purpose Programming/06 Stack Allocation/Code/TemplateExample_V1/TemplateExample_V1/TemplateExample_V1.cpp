// **********************************************************************************************************************
// 
// File:	    	TemplateExample_V1.cpp
// Created:	        30/08/2009
// Author:    		Michael Dailly
// Project:		    TemplateExample_V1
// Description:   	
// 
// Date				Version		BY		Comment
// ----------------------------------------------------------------------------------------------------------------------
// 30/08/2009		V1.0		MJD		1st version
// 
// **********************************************************************************************************************
#include <tchar.h>
#include <assert.h>
#include "TStackAlloc.h"

// #############################################################################################
/// Class:<summary>
///          Simple object to allocate
///       </summary>
// #############################################################################################
class	Sprite
{
public:
		float		x;
		float		y;
		int			shape;
		int			frame;

		Sprite(): x(0), y(0), shape(0), frame(0) {}		
};




	// Our allocator
	StackAlloc<Sprite>*	g_Objects;
	


// #############################################################################################
/// Function:<summary>
///             Simple example core showing simple temple usage.
///          </summary>
// #############################################################################################
int _tmain(int argc, _TCHAR* argv[])
{

	Sprite*	Objects[256];
	while(true)
	{
		// Create our object list
		g_Objects = new StackAlloc<Sprite>(256);

		for(int loop=0;loop<10;loop++)
		{
			// Allocate 256 objects
			for(int i=0;i<256;i++){
				Objects[i] = g_Objects->Pop();
			}

			// And free them again - Being a stack, this reverses the order...
			for(int i=0;i<256;i++)
			{
				g_Objects->Push(Objects[i]);
			}
		}

		delete g_Objects;
	}
	return 0;
}

