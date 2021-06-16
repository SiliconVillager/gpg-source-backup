/*-------------------------------------------------------------------*/
/** Stores an n dimensional point in space. */
/*-------------------------------------------------------------------*/

#include "OasPoint.h";

/*-------------------------------------------------------------------*/
/** Constructor. Creates a new instance of OasPoint.
 *  @param numDim the number of dimensions to be stored. */
/*-------------------------------------------------------------------*/    
OasPoint::OasPoint(int numDim) {
	nDimensionPoint = new double[numDim];
	this->numDim = numDim;         

    for (int i = 0; i < numDim; i++) {
		nDimensionPoint[i] = -1;
	}
} 

OasPoint::~OasPoint() {
	delete [] nDimensionPoint;
}

/*-------------------------------------------------------------------*/
/** Get the number of dimensions. 
 *  @return the number of dimensions. */
/*-------------------------------------------------------------------*/
int OasPoint::getNumOfDimension() {
	return numDim;
}

/*-------------------------------------------------------------------*/
/** Set the position for a particular dimension. 
 *  @param dim the dimension in which to set the position (point). 
 *  @param stateVal the postition value. */
/*-------------------------------------------------------------------*/    
void OasPoint::setDimension(int dim, double stateVal) {
	if(dim < 0 || dim >= numDim)
		return;
	nDimensionPoint[dim] = stateVal;
}

/*-------------------------------------------------------------------*/
/** Get the position of a dimension. 
 *  @return the position of a dimension. */
/*-------------------------------------------------------------------*/    
double OasPoint::getDimension(int dim) {
	if(dim < 0 || dim >= numDim)
		return -1;
	return nDimensionPoint[dim];
}