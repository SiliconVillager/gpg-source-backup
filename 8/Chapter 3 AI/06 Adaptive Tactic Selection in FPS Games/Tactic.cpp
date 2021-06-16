/*-------------------------------------------------------------------*/
/** Stores a single tactic. */
/*-------------------------------------------------------------------*/

#include "Tactic.h"

/*-------------------------------------------------------------------*/
/** Default constructor. */
/*-------------------------------------------------------------------*/    
Tactic::Tactic() { 
	tacitcId = -1;
	tacitcName = "";
    weight = 0;
	tacticVariant = "";

	for(int i = 0; i < diffLiftLostSize; i++){
		diffLifeLost[i] = 0;
	}
}

/*-------------------------------------------------------------------*/
/** Set the tactic properties. 
 *  @param id the tactic ID.
 *  @param name the name of the tactic.
 *  @param w the tactic weight.
 *  @param variant the tactic type or variant. */
/*-------------------------------------------------------------------*/  
void Tactic::setTactic(int id, string name, int w, string variant){
	tacitcId = id;
    tacitcName = name;
    weight = w;
    tacticVariant = variant;        
}

/*-------------------------------------------------------------------*/
/** Set the tactic weight.
 *  @param w the tactic weight. */
/*-------------------------------------------------------------------*/  
void Tactic::setTacticWeight(int w){
	weight = w;      
}

/*-------------------------------------------------------------------*/
/** Get the tactic ID. 
 *  @return the tactic ID. */
/*-------------------------------------------------------------------*/  
int Tactic::getTacticId(){   
	return tacitcId;
} 
    
/*-------------------------------------------------------------------*/
/** Get the tactic name. 
 *  @return the tactic name. */
/*-------------------------------------------------------------------*/  
string Tactic::getTacticName(){   
	return tacitcName;
}     
    
/*-------------------------------------------------------------------*/
/** Get the tactic weight. 
 *  @return the tactic weight. */
/*-------------------------------------------------------------------*/  
int Tactic::getTacticWeight(){   
	return weight;
}     
    
/*-------------------------------------------------------------------*/
/** Get the tactic tactic variant. 
 *  @return the tactic variant. */
/*-------------------------------------------------------------------*/  
string Tactic::getTacticVariant(){   
	return tacticVariant;
}

/*-------------------------------------------------------------------*/
/** Clear the difference in life lost from the tactic. */
/*-------------------------------------------------------------------*/  
void Tactic::clearDiffLifeLost(){ 
	for(int i = 0; i < diffLiftLostSize; i++){
		diffLifeLost[i] = 0;
	}
}    
 
/*-------------------------------------------------------------------*/
/** Add the difference in life lost during the performance of the tactic. 
 *  @param tmpDiffLifeLost the difference in life lost during the performance of the tactic. */
/*-------------------------------------------------------------------*/  
void Tactic::addDiffLifeLost(float tmpDiffLifeLost){
	for(int i = (diffLiftLostSize - 1); i > 0; i--){
		diffLifeLost[i] = diffLifeLost[ (i - 1) ];
	}

	diffLifeLost[0] = tmpDiffLifeLost;       
}
    
/*-------------------------------------------------------------------*/
/** Get the average difference in life lost. 
 *  @return the average difference in life lost. */
/*-------------------------------------------------------------------*/  
float Tactic::getAvgDiffLifeLost(){ 
	float avgDiffLifeLost = 0;
        
	if(diffLifeLost[0] == 0){
		return avgDiffLifeLost;
	}          
        
	for(int i = 0; i < diffLiftLostSize; i++) {
		avgDiffLifeLost = avgDiffLifeLost + diffLifeLost[i];
    }
        
	avgDiffLifeLost = avgDiffLifeLost / diffLiftLostSize;
        
	return avgDiffLifeLost;
}
