/*-------------------------------------------------------------------*/
/** AdaptiveTacticSelection.cpp:	This file contains the algorithms for the 
									Adaptive Tactic Selection in First Person
									Shooter (FPS) Games gem.
*/
/*-------------------------------------------------------------------*/

/* Header Files. */
#include "TacticLibraryInstance.h"
#include "TacticResult.h"
#include "AdaptiveTacticSelection.h"

#include <string>
#include <vector>
using namespace std;

/*-------------------------------------------------------------------*/
/** The fitness function for a tactic.
	@param prioritisedTacticList[][2]		Stores the prioritised list of tactics from the current tactic library instances.
											0. Tactic ID. 1. Average difference in life lost from the previous n encounters.
	@param currentTactic					Stores the current tactic in the form of its position in the prioritisedTacticList array.
	@param currentTacticLibraryInstance		Stores the k clost cases to the query point. 
*/
/*-------------------------------------------------------------------*/   
float AdaptiveTacticSelection::tacticFitnessFunction( float prioritisedTacticList[][2], int currentTactic, vector<TacticLibraryInstance>& currentTacticLibraryInstance ) {
	/* Determine the health lost from the NPC. */
    int hl_npc = botDamage();
	/* Determine the health lost from the opponent. */
    int hl_opp = playerDamage();

	/* The difference in life lost caused by the tactic. */
	float a = (hl_opp - hl_npc) / 100;

	/* Surprise. */
    float s = ( (hl_opp - hl_npc) - prioritisedTacticList[currentTactic][1] ) / 200;

    /* Add the difference in life lost to the to the average life lost for
       the current tactics. */
	for (int i = 0; i < currentTacticLibraryInstance.size(); i++) {
		/* Get the tactic. */
		Tactic* aTactic = currentTacticLibraryInstance.at(i).getTactic( prioritisedTacticList[currentTactic][0] );
		
		aTactic->addDiffLifeLost( (hl_opp - hl_npc) );
	}

	/* Determine the overal fitness of the bot. */
    float fitness = 1 / 10 * (7 * a + 3 * s);

	return fitness;
}

/*-------------------------------------------------------------------*/
/*  The weight update algorithm for the tactic adaptation system. Updates the tactic weight after a tactic has been used. 
	@param prioritisedTacticList[][2]		Stores the prioritised list of tactics from the current tactic library instances.
											0. Tactic ID. 1. Average difference in life lost from the previous n encounters.
	@param currentTactic					Stores the current tactic in the form of its position in the prioritisedTacticList array.
	@param currentTacticLibraryInstance		Stores the k clost cases to the query point. 
*/
/*-------------------------------------------------------------------*/   
TacticResult* AdaptiveTacticSelection::tacticWeightUpdate( float prioritisedTacticList[][2], int currentTactic, 
																				vector<TacticLibraryInstance>& currentTacticLibraryInstances, OasPoint& current_qp ) {
	/* Determine the fitness of the tactic. */
	float fitness = tacticFitnessFunction( prioritisedTacticList, currentTactic, currentTacticLibraryInstances );

	/* Determine the weight adjustment. */
	bool encounterWeightAdjustment = false;
    int* weight_Adjustment = weightAdjustment(fitness, currentTacticLibraryInstances, current_qp, encounterWeightAdjustment);

	/* Update the weights for the current tactic library instances. */
	Tactic* aTactic;
	for (int i = 0; i < currentTacticLibraryInstances.size(); i++) {
		/* Get the tactic instance. */
		aTactic = currentTacticLibraryInstances[i].getTactic( prioritisedTacticList[currentTactic][0] );
		
		/* Get the tactic and update it. */
		weightUpdate( aTactic, &currentTacticLibraryInstances[i], weight_Adjustment[i] );
	}

	/* Record the tactic update so that the encounter update can be performed. */
	TacticResult* aTacticResult = new TacticResult;
	aTacticResult->setQueryPoint(&current_qp);
	aTacticResult->setTacitcId(prioritisedTacticList[currentTactic][0]);
	aTacticResult->setCurrentTacticLibraryInstances( &currentTacticLibraryInstances );
	aTacticResult->setHl_npc( botDamage() );
	aTacticResult->setHl_opp( playerDamage() );

	return aTacticResult;
}

/*-------------------------------------------------------------------*/
/** The weight update algorithm for the tactic adaptation system.
 *  Updates the tactics that were used in the encounter. */
/*-------------------------------------------------------------------*/     
void AdaptiveTacticSelection::encounterWeightUpdate(vector<TacticResult>& tacticResults, bool hasKilled, vector<TacticLibraryInstance>& currentTacticLibraryInstances) {
	float fitness = 0;
	int* weight_Adjustment;

	/* Loop through the list of tactics and results used in the encounter. */
	for (int i = 0; i < tacticResults.size(); i++) {
        /* Determine the fitness of the tactic. */
        if(hasKilled == true){
			/* The bot won. */
            fitness = 1 - (tacticResults[i].getHl_npc() / 125);
		} else {
            /* The bot lost. */
            fitness = -1 + (tacticResults[i].getHl_opp() / 125);
		}

		/* Determine the weight adjustment. */
        weight_Adjustment = weightAdjustment(fitness, currentTacticLibraryInstances, *tacticResults[i].getCurrentQueryPoint(), true); 

		/* Update the weights for the current tactic library instances. */
		Tactic* aTactic;
		for (int i = 0; i < currentTacticLibraryInstances.size(); i++) {
			aTactic = currentTacticLibraryInstances[i].getTactic( tacticResults[i].getTacitcId() );

			/* Get the tactic and update it. */
			weightUpdate( aTactic, &currentTacticLibraryInstances[i], weight_Adjustment[i] );
		}
	}

}


/*-------------------------------------------------------------------*/
/** Weight adjustment for the tactic instances. 
 @param fitness the fitness of a tactic.
 @param tacticInstances the k tactic instances retrieved from tactic library. 
 @param qp the query point. 
 @param encounterWeightAdjustment a flag which indicates an encounter weight adjustment. False = tactic update.
 @return a list of weight adjustments. 
*/
/*-------------------------------------------------------------------*/  
int* AdaptiveTacticSelection::weightAdjustment(float fitness, vector<TacticLibraryInstance>& tacticInstances, OasPoint& qp, bool encounterWeightAdjustment) {
	/* Determine the total weight adjustment. */
    int tmpWeightAdjustment = 0;
    
	if(encounterWeightAdjustment == false){
		/* Tactic update weight adjustment. */
        if(fitness < tacticBreakEvenPoint){
			tmpWeightAdjustment = tacticPenaltyMax * fitness;
		} else {
			tmpWeightAdjustment = tacticRewardMax * fitness;
        }
	} else {
		/* Encounter update weight adjustment. */            
		if(fitness < encounterBreakEvenPoint){
			tmpWeightAdjustment = encounterPenaltyMax * fitness;
        } else {
			tmpWeightAdjustment = encounterRewardMax * fitness;
        }                    
	}	

	/* Determine the number of instances. */
    int numOfInstances = tacticInstances.size();

	/* Share the weight adjustment between the tactic instances */
	int* weightAdjustment = new int[numOfInstances];

	/* Check if the number of instances equals 1. */
    if(numOfInstances == 1){
		weightAdjustment[0] = tmpWeightAdjustment;
	} else {
        /* First determine the distance. */
        float* tmpDistanceSqr = new float[numOfInstances];
        float tmpDistanceSqrTotal = 0;

        for(int i = 0; i < numOfInstances; i++){
            /* Get the distance between the query point and the library instance.
               A utility method is called to determine the Euclidean metric. */
			
			// A tactic instance - tacticInstances.operator[i]
            tmpDistanceSqr[i] = euclideanMetric( qp, *tacticInstances.at(i).getPosition() );             
			tmpDistanceSqr[i] = tmpDistanceSqr[i] * tmpDistanceSqr[i];
            
            tmpDistanceSqrTotal = tmpDistanceSqrTotal + (1  / tmpDistanceSqr[i]);
        }
        
        /* Determine d. */
        float d = 1 / tmpDistanceSqrTotal;
        
        /* Determine the weight adjustment for each library instance. */
        for(int i = 0; i < numOfInstances; i++){
            weightAdjustment[i] = (d * tmpWeightAdjustment) / tmpDistanceSqr[i];
        }

		delete [] tmpDistanceSqr;
	}

	return weightAdjustment;
}

/*-------------------------------------------------------------------*/
/** The weight update algorithm for the tactic adaptation system. 
 *  @param currentTactic the current tactic for the bot.
 *  @param weightAdjustment the weight adjustment for the tactic library instance. */
/*-------------------------------------------------------------------*/     
void AdaptiveTacticSelection::weightUpdate(Tactic* currentTactic, TacticLibraryInstance* currentTacticLibraryInstance, int weightAdjustment) { 
	// Weight update algorithm here.
	// Use the standard dynamic scripting weight update algorithm to update the tactic weights in the library instance.
	// See: Dynamic Scripting. AI Game Programming Wisdom 3 (ed. S. Rabin), pp. 661 - 675. Charles River Media. 2006.
}

/*-------------------------------------------------------------------*/
/* A function that returns the bot damage. */
/*-------------------------------------------------------------------*/   
int AdaptiveTacticSelection::botDamage() {
	// Code here to determine and return the bot damage.
	return 0;
}

/*-------------------------------------------------------------------*/
/* A function that returns the player damage. */
/*-------------------------------------------------------------------*/  
int AdaptiveTacticSelection::playerDamage() {
	// Code here to determine and return the player damage.
	return 0;
}

/*-------------------------------------------------------------------*/
/* A function that returns the distance between 2 points (i.e. euclidean Metric). */
/*-------------------------------------------------------------------*/   
float AdaptiveTacticSelection::euclideanMetric(OasPoint& i , OasPoint& j) {
	// Code here to determine the distance between 2 points (i.e. euclidean Metric).
	return 0;
}