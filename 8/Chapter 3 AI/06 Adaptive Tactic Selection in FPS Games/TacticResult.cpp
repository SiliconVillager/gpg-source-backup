/*-------------------------------------------------------------------*/
/** Manages a result from the tactic. Stores the result. */
/*-------------------------------------------------------------------*/

#include <vector>

#include "TacticLibraryInstance.h"
#include "TacticResult.h"

/*-------------------------------------------------------------------*/
/** Constructor. */
/*-------------------------------------------------------------------*/    
TacticResult::TacticResult() {
	tacitcId = -1;
	hl_npc = -1;
	hl_opp = -1;
}

vector<TacticLibraryInstance>* TacticResult::getCurrentTacticLibraryInstances() {
	return currentTacticLibraryInstances;
}

void TacticResult::setCurrentTacticLibraryInstances(vector<TacticLibraryInstance>* tacticLibraryInstances) {
	currentTacticLibraryInstances = tacticLibraryInstances;
}

OasPoint* TacticResult::getCurrentQueryPoint() {
	return qp;
}

void TacticResult::setQueryPoint(OasPoint* tmpQp) {
	qp = tmpQp;
}

float TacticResult::getTacitcId() {
	return tacitcId;
}

void TacticResult::setTacitcId(float tId) {
	tacitcId = tId;
}

int TacticResult::getHl_npc() {
	return hl_npc;
}

void TacticResult::setHl_npc(float thl_npc) {
	hl_npc = thl_npc;
}

int TacticResult::getHl_opp() {
	return hl_opp;
}

void TacticResult::setHl_opp(float thl_opp) {
	hl_opp = thl_opp;
}