/*-------------------------------------------------------------------*/
/** Stores an n dimensional point in space. */
/*-------------------------------------------------------------------*/

#ifndef OAS_POINT
#define OAS_POINT

class OasPoint {
private:
	/** Array storing state information. */
    double* nDimensionPoint;  
	/** numDim the number of dimensions to be stored. */
	int numDim;
public:
	OasPoint(int numDim);
	~OasPoint();
	
	int getNumOfDimension();
	void setDimension(int dim, double stateVal);
	double getDimension(int dim);
};

#endif OAS_POINT