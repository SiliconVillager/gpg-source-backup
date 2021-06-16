/*-------------------------------------------------------------------*/
/** Stores a single tactic. */
/*-------------------------------------------------------------------*/

#ifndef TACTIC_H
#define TACTIC_H

#include <string>
using namespace std;

class Tactic {
private:
	/** The tactic ID. */
	int tacitcId;
    /** The tactics name. */
    string tacitcName; 
    /** Tactic weight. */
    int weight;
    /** Tactic Variant. */
    string tacticVariant;
	/* static const - diffLiftLostSize. */
	static const int diffLiftLostSize = 5;
    /** A list of difference in life lost for the adapting NPC. */
    float diffLifeLost[diffLiftLostSize]; 
public:
	/** Constructor. */
	Tactic();
	/** Set the tactic properties. */
	void setTactic(int id, string name, int w, string variant);
	/** Set the tactic weight. */
	void setTacticWeight(int w);
	/** Get the tactic ID. */
	int getTacticId();
	/** Get the tactic name. */
	string getTacticName();
	/** Get the tactic weight. */
	int getTacticWeight();
	/** Get the tactic tactic variant. */
	string getTacticVariant();
	/** Clear the difference in life lost from the tactic. */
	void clearDiffLifeLost();
	/** Add the difference in life lost during the performance of the tactic. */
	void addDiffLifeLost(float tmpDiffLifeLost);
	/** Get the average difference in life lost. */
	float getAvgDiffLifeLost();
};

#endif TACTIC_H