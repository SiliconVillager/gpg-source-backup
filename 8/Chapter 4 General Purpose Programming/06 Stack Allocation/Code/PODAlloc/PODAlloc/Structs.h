// **********************************************************************************************************************
// 
// File:	    	Structs.h
// Created:	        30/08/2009
// Author:    		Michael Dailly
// Project:		    PODAlloc
// Description:   	Example POD struct and possible object use.
// 
// Date				Version		BY		Comment
// ----------------------------------------------------------------------------------------------------------------------
// 30/08/2009		V1.0		MJD		1st version
// 
// **********************************************************************************************************************
#ifndef	__STRUCTS_H__
#define	__STRUCTS_H__


// #############################################################################################
/// Struct:<summary>
///           Our simple POD type
///        </summary>
// #############################################################################################
struct STile
{
	short	X;
	short	Y;
};




// #############################################################################################
/// Class:<summary>
///          An example of how you might use the allocated values.
///       </summary>
// #############################################################################################
class	TileBitmap
{
	STile	m_BaseCoordinate;		// Coordinate inside texture page
	void*	m_pTile;				// Tile Bitmap
};


#endif	// __STRUCTS_H__


