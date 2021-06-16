// **********************************************************************************************************************
// 
// File:	    	TemplateExample_V2.cpp
// Created:	        30/08/2009
// Author:    		Michael Dailly
// Project:		    TemplateExample_V2
// Description:   	
// 
// Date				Version		BY		Comment
// ----------------------------------------------------------------------------------------------------------------------
// 30/08/2009		V1.0		MJD		1st version
// 
// **********************************************************************************************************************
#include <tchar.h>
#include <assert.h>
#include "TStackAllocIndex.h"

// The current debug code inside the template requires that -1 be free, no matter what the datatype
// This means you can't use the whole space of the type if you plan keep the debug code.
#define	MAX_INDICES			16384
typedef	short				CoreType;

	// Our allocator
	StackAllocIndex<CoreType>*	g_Alloc;
	


// #############################################################################################
/// Function:<summary>
///             Simple example core showing simple temple usage.
///          </summary>
// #############################################################################################
int _tmain(int argc, _TCHAR* argv[])
{

	CoreType	indices[MAX_INDICES];

	while(true)
	{
		// Create our object list
		g_Alloc = new StackAllocIndex<CoreType>(MAX_INDICES);

		for(int loop=0;loop<10;loop++)
		{
			// Allocate 256 objects
			for(int i=0;i<MAX_INDICES;i++){
				indices[i] = g_Alloc->Pop();
			}

			// And free them again - Being a stack, this reverses the order...
			for(int i=0;i<MAX_INDICES;i++)
			{
				g_Alloc->Push(indices[i]);
			}
		}

		delete g_Alloc;
	}
	return 0;
}



