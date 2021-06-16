/*-------------------------------------------------------------------*/
/** Manages a tactic Library instance. Stores a single instances of a tactic library. */
/*-------------------------------------------------------------------*/

#include "TacticLibraryInstance.h"

/*-------------------------------------------------------------------*/
/** Default constructor.
/*-------------------------------------------------------------------*/    
TacticLibraryInstance::TacticLibraryInstance() {
	numTactics = 0;
}  

/*-------------------------------------------------------------------*/
/** Add a tactic.
 *  @param t the tactic to be added. 
 *  @return true if the tactic is added. False if not. A tactic is not
 *          added if a tactic with the same id already exists. */
/*-------------------------------------------------------------------*/  
bool TacticLibraryInstance::setTactic(Tactic& t){
	/* Check if the Variant exists. */
	int tacticId = t.getTacticId();
        
    /* Add the tactic. */
	if( tactics.find(tacticId) == tactics.end() ){
		tactics[tacticId] = t;
		numTactics++;
		return true;
	}

    return false;
}

/*-------------------------------------------------------------------*/
/** Add a list of tactics. Does not return an error if the tactic was
 *  not added. A manual check needs to be performed.
 *  @param t a list of tactics to be added. */
/*-------------------------------------------------------------------*/  
void TacticLibraryInstance::setTactics(vector<Tactic>& t){
	for(int i = 0; i < t.size(); i++){
		setTactic( t[i] );
    }
}

/*-------------------------------------------------------------------*/
/** Get the tactics for a variant.
 *  @param id the id for the required tactic. */
/*-------------------------------------------------------------------*/  
Tactic* TacticLibraryInstance::getTactic(int id){          
	if( tactics.find(id) == tactics.end() ){
		Tactic* aTactic = NULL;
		return aTactic;
	} else {
		Tactic* aTactic = &tactics[id];
		return aTactic;
	}
} 

/*-------------------------------------------------------------------*/
/** Get all the tactics.
 *  @return all the tactics. */
/*-------------------------------------------------------------------*/  
vector<Tactic>* TacticLibraryInstance::getTactics(){
	vector<Tactic>* tmpTactics = new vector<Tactic>;

	for (int i = 0; i < tactics.size(); i++) {
		tmpTactics->push_back( tactics[i] );
	}

	return tmpTactics;
} 

/*-------------------------------------------------------------------*/
/** Set to library instances position.
 *  @param  the position. */
/*-------------------------------------------------------------------*/  
void TacticLibraryInstance::setPosition(OasPoint* p){ 
	position = p;
}   
    
/*-------------------------------------------------------------------*/
/** Get the library instances position.
 *  @return  the position. */
/*-------------------------------------------------------------------*/  
OasPoint* TacticLibraryInstance::getPosition(){ 
	return position;
}     
    
/*-------------------------------------------------------------------*/
/** Get the number of tactics in the tactic libraries.
 *  @return  the number of tactics in the library. */
/*-------------------------------------------------------------------*/  
int TacticLibraryInstance::getNumTactics(){ 
	return numTactics;
}