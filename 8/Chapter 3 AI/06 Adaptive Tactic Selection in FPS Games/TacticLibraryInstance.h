/*-------------------------------------------------------------------*/
/** Manages a tactic Library instance. Stores a single instances of a tactic library. */
/*-------------------------------------------------------------------*/

#ifndef TACTIC_LIB_INSTANCE
#define TACTIC_LIB_INSTANCE

#include <map>
#include <vector>

#include "Tactic.h"
#include "OasPoint.h"

class TacticLibraryInstance {
private:
	/** Stores the tactics for the library instance. */
    map<int,Tactic> tactics;

    /** Stores the library instances position in the lower state space. */
    OasPoint* position;
    
    /** The number of tactics in the tactic library instance. */
    int numTactics;

public:
	/** Constructor. */
	TacticLibraryInstance();
	/** Add a tactic. */
	bool setTactic(Tactic& t);	
	/** Add a list of tactics. */
	void setTactics(vector<Tactic>& t);
	/** Get the tactics for a variant. */
	Tactic* getTactic(int id);

	/** Get all tactics. */
	vector<Tactic>* getTactics();
	/** Set to library instances position. */
	void setPosition(OasPoint* p);
	/** Get the library instances position. */
	OasPoint* getPosition();
	/** Get the number of tactics in the tactic libraries. */
	int getNumTactics();
};

#endif TACTIC_LIB_INSTANCE