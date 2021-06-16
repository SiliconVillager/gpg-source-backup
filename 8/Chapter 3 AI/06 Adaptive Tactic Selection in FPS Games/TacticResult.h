/*-------------------------------------------------------------------*/
/** Manages a result from the tactic. Stores the result. */
/*-------------------------------------------------------------------*/

#ifndef TACTICRESULT
#define TACTICRESULT

class TacticResult {
private:
	/** Stores the k closest cases to the query point. */
	vector<TacticLibraryInstance>* currentTacticLibraryInstances;

    /** Stores the current query point. */
    OasPoint* qp;    
    
    /** The tactic ID. */
    float tacitcId;  

    /** NPC health lost. */
    int hl_npc;
    
    /** Opponent health lost. */
    int hl_opp;

public:
	TacticResult();
	vector<TacticLibraryInstance>* getCurrentTacticLibraryInstances();
	void setCurrentTacticLibraryInstances(vector<TacticLibraryInstance>* tacticLibraryInstances);
	OasPoint* getCurrentQueryPoint();
	void setQueryPoint(OasPoint* tmpQp);
	float getTacitcId();
	void setTacitcId(float tId);
	int getHl_npc();
	void setHl_npc(float thl_npc);
	int getHl_opp();
	void setHl_opp(float thl_opp);
};

#endif TACTICRESULT