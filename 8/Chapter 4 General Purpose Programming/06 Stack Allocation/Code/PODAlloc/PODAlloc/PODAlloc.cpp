// **********************************************************************************************************************
// 
// File:	    	PODAlloc.cpp
// Created:	        30/08/2009
// Author:    		Michael Dailly
// Project:		    PODAlloc
// Description:   	Simple example showing how to allocate POD types using the StackAllocator.
//					This is a very simple example but shows that not just pointers and indexes 
//					need be allocated. If your whole structure can fit inside a standard data type
//					then you can "pop" if from the stack and return it directly.
// 
//					This could also be used to allocate cells or SPAWN points inside an AI system,
//					or even 3D allocation using more compex packing or a U64 return type.
// 
// 
// Date				Version		BY		Comment
// ----------------------------------------------------------------------------------------------------------------------
// 30/08/2009		V1.0		MJD		1st version.
// 
// **********************************************************************************************************************
#include <tchar.h>
#include <assert.h>
#include "Structs.h"

#define	MAX_BUFFERS		(128*128)


	unsigned int*	g_TexturePage;
	STile			g_CoordinateArray[MAX_BUFFERS];
	int				g_SP;
	STile			g_Empty;


	// Example code for allocating and freeing
	STile			g_AllocatedPool[256];

// #############################################################################################
/// Function:<summary>
///             Allocate a 32x32 tile from the 4096x4096 texture.
///          </summary>
///
/// Out:	 <returns>
///				a STile struct holding the base coordinate of the tile.
///			 </returns>
// #############################################################################################
STile	Pop( void )
{
	assert(g_SP>=0);
	assert(g_SP<=MAX_BUFFERS);

	if( g_SP ==0 ) return g_Empty;
	return g_CoordinateArray[--g_SP];
}

// #############################################################################################
/// Function:<summary>
///				Free a texture slot.            
///          </summary>
///
/// In:		 <param name="_tile">
///							This POD holds the X and Y coordinate on the texture page that
///							is being freed.
///			 </param>
// #############################################################################################
void	Push( STile _tile )
{
	assert(_tile.X>=0);
	assert(_tile.Y>=0);
	assert(_tile.X<4096);
	assert(_tile.Y<4096);

	g_CoordinateArray[g_SP++] = _tile;
}



// #############################################################################################
/// Function:<summary>
///             Pre-Allocate the memory buffers
///          </summary>
// #############################################################################################
void	Init( void )
{
	g_SP = 0;
	g_Empty.X = -1;
	g_Empty.Y = -1;
	for(int y = 0;y<128;y++){
		for(int x = 0;x<128;x++)
		{
			STile	pod;
			
			pod.X = x*32;	// Create the actual coordinate
			pod.Y = y*32;	// and store it inside the POD

			Push(pod);		// use push (free) to initialise.
		}
	}

}


// #############################################################################################
/// Function:<summary>
///             Simple example showing the use of POD type allocation.
///          </summary>
// #############################################################################################
int _tmain(int argc, _TCHAR* argv[])
{
	Init();

	while(true){
		// Allocate 256 tile slots
		for(int i=0;i<256;i++){
			g_AllocatedPool[i] = Pop();
		}

		// Now free them again.
		for(int i=0;i<256;i++)
		{
			// Shuffle the bits to give a more interested FREE order.
			int index = ((i&1)<<7) | ((i&2)<<5) | ((i&0x40)>>5) | ((i&0x80)>>7) | (i&0x3c);
			Push( g_AllocatedPool[index] );
		}
	}
}

