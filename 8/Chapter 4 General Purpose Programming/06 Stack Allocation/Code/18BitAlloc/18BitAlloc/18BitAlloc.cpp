// **********************************************************************************************************************
// 
// File:	    	18BitAlloc.cpp
// Created:	        30/08/2009
// Author:    		Michael Dailly
// Project:		    18BitAlloc
// Description:   	Simple example showing how to allocate a packed 18bit number.
// 
// Date				Version		BY		Comment
// ----------------------------------------------------------------------------------------------------------------------
// 30/08/2009		V1.0		MJD		1st version.
// 
// **********************************************************************************************************************

#include <tchar.h>
#include <assert.h>

	const int			MAX_OBJ = 0x40000;
	unsigned short		Stack_short[MAX_OBJ];
	unsigned int		Stack_bits[MAX_OBJ/16];
	int					SP;
	

// #############################################################################################
/// Function:<summary>
///             Return the next free index (an 18bit number)
///          </summary>
///
/// Out:	 <returns>
///				Return the next free 18bit index, or -1 for an error.
///			 </returns>
// #############################################################################################
int	Pop( void )
{
	if( SP==0)  return -1;				// If none left, then return an error

	// Get the main 16bits
	int val = Stack_short[--SP];
	
	// Now OR in the extra bits we need to make up the 18bit index.
	val |= (  (Stack_bits[SP>>4]>>((SP&0xf)<<1)) &0x3)<<16;

	return val;
}

// #############################################################################################
/// Function:<summary>
///             Push an 18bit index onto the object stack
///          </summary>
///
/// In:		 <param name="_value">18Bit number to store</param>
// #############################################################################################
void Push( int _value)
{
	assert(_value>=0);
	assert(_value<0x40000);

	// The easy bit - store the 16bit number.
	Stack_short[SP] = (unsigned short)_value&0xffff;

	// Get the shift and reduced stack index.
	int shift = (SP&0xf)<<1;
	int index = SP>>4;

	// Clear the bits we're about to OR into.
	Stack_bits[index]&= ~(3<<shift);

	// Now OR in the new bits
	Stack_bits[index] |= ((_value>>16)&0x3) << shift;

	// And move the stack pointer....
	SP++;
}


// #############################################################################################
/// Function:<summary>
///             Initialise the free list
///          </summary>
// #############################################################################################
void	Init( void )
{
	// Create and fill our 18bit table. Use Push to build the table as it's easier in this case.
	SP = 0;
	for(int i=0;i<MAX_OBJ; i++){
		Push(i);
	}
}


// #############################################################################################
/// Function:<summary>
///             Main loop. Allocate some values, and free them in a different order.
///          </summary>
// #############################################################################################
int _tmain(int argc, _TCHAR* argv[])
{
	// Create our free list.
	Init();


	int a,b,c;
	while(true){

		a = Pop();
		b = Pop();
		c = Pop();

		Push(c);
		Push(a);
		Push(b);
	}
	return 0;
}

