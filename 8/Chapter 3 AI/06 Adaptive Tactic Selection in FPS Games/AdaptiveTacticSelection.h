/*-------------------------------------------------------------------*/
/** AdaptiveTacticSelection.cpp:	This file contains the algorithms for the 
									Adaptive Tactic Selection in First Person
									Shooter (FPS) Games gem.
*/
/*-------------------------------------------------------------------*/

#ifndef ADAPTIVE_TACTIC
#define ADAPTIVE_TACTIC

#include "TacticLibraryInstance.h"
#include "Tactic.h"
#include "OasPoint.h"
#include "TacticResult.h"

#include <map>
#include <vector>

class AdaptiveTacticSelection {
private:
	/* Constants. */
	static const int tacticBreakEvenPoint = 0;
	static const int tacticPenaltyMax = 75;
	static const int tacticRewardMax = 75;

	static const int encounterBreakEvenPoint = 0;
	static const int encounterPenaltyMax = 25;
	static const int encounterRewardMax = 25;
public:
	float tacticFitnessFunction( float prioritisedTacticList[][2], int currentTactic, vector<TacticLibraryInstance>& currentTacticLibraryInstance );
	TacticResult* tacticWeightUpdate( float prioritisedTacticList[][2], int currentTactic, 
																				vector<TacticLibraryInstance>& currentTacticLibraryInstances, OasPoint& current_qp );
	int fitnessFunction();
	int botDamage();
	int playerDamage();
	float euclideanMetric(OasPoint&, OasPoint&);
	int* weightAdjustment(float fitness, vector<TacticLibraryInstance>& tacticInstances, OasPoint& qp, bool encounterWeightAdjustment);
	void weightUpdate(Tactic* currentTactic, TacticLibraryInstance* currentTacticLibraryInstance, int weightAdjustment);
	void encounterWeightUpdate(vector<TacticResult>& tacticResults, bool hasKilled, vector<TacticLibraryInstance>& currentTacticLibraryInstances);
};

#endif ADAPTIVE_TACTIC