/* Copyright (C) Matthew Jack, 2009.
 * All rights reserved worldwide.
 * matthew.jack+GPG@gmail.com
 *
 * This software is provided "as is" without express or implied
 * warranties. You may freely copy and compile this source into
 * applications you distribute provided that the copyright text
 * below is included in the resulting source code, for example:
 * "Portions Copyright (C) Matthew Jack, 2009"
 */

// Example implementation of Code Coverage for QA
// See Readme.txt
//
// This file applies the Code Coverage framework to a toy codebase over a few dummy update frames
// It only uses a text interface for feedback, as any GUI would be of limited portability
// I definately recommend writing a GUI class for real usage, as described in the article


#define WIN32_LEAN_AND_MEAN		// Exclude rarely-used stuff from Windows headers

#include "CodeCoverageMarker.h"
#include "CodeCoverageManager.h"
#include "ToyCodebase.h"
#include <stdio.h>
#include <direct.h>
#include <conio.h>

#ifdef _MSC_VER
	#pragma warning (disable: 4996) // Switch off warning about getcwd
#endif

//--------------------------------------------------------------------------------------------------
// Relative path of input data file
const char * sPath = "LabelFile.txt";
// Manager instance
CCodeCoverageManager * ccManager = NULL;
// Our running copy of the Hit marker count etc.
CCodeCoverageManager::SStats stats;

//--------------------------------------------------------------------------------------------------
// Cause the Manager to check with the Tracker for new hits
// Outputs any results to the console
void UpdateManager();

// Dump the contents of the Manager to the console, listing all known Labels of all categories
void DumpManagerStatus();

//--------------------------------------------------------------------------------------------------
int main(int argc, char* argv[])
{
  // Create Manager instance, loading a datafile of expected Markers from disk
  // You may wish to try loading this later in the run - the Tracker will still accumulate results
  ccManager = CCodeCoverageManager::CreateInstance(sPath);
  if (!ccManager)
  {
    // If this fails, it's probably looking in the wrong place
    const size_t buffSize = 256;
    char sCwd[buffSize];
    if (!getcwd(sCwd, buffSize))
      sCwd[0]='\0';

    printf("Failed to load data file with path \"%s\" with working directory \"%s\"\n", sPath, sCwd);
    return -1;
  }

  // We do two Code Coverage runs (to demonstrate the Reset functionality)
  // This would be equivalent to asking QA to play a level twice before quitting, getting independant results for each run
  for (int i=0; i<2; ++i)
  {
    ccManager->GetStats(stats);
    printf("Results from Manager at this stage are:\n");
    DumpManagerStatus();
    
    CToyCodebase toyCodebase;

    // We simulate a QA test run with an update loop of 10 "frames"
    for (int nFrame=0; nFrame<10; nFrame++)
    {
      printf("Starting frame #%i\n", nFrame);
      toyCodebase.Update( nFrame );

      // We update the Manager each time to output any points hit
      UpdateManager();
    }

    printf("After completing our test run of a few toy frames, complete results from Manager are:\n");
    DumpManagerStatus();

    // Now refresh Manager and reset the Tracker before repeating the run
    // If we were loading a new level to perform an independant run, we would instead delete the Manager and get a new
    // instance loading the new level's data file, and then reset the Tracker as below.
    printf("** Reseting Manager and Tracker ***\n");
    ccManager->Reset();
    CCodeCoverageTracker::GetInstance().Reset();
  }
    
  // Wait for key hit, then cleanup and exit
  getch();

  delete ccManager;
	return 0;
}


//--------------------------------------------------------------------------------------------------
void UpdateManager()
{
  // We might choose to instantiate the Manager during he run, so tolerate it being NULL
  if (!ccManager) 
    return;

  // Check with the Tracker and update with any new hits
  ccManager->Update();

  // Output any newly Hit Labels to console
  std::vector<const char*> labels;
  ccManager->GetHit(labels, stats.hitCount);
  for (unsigned i=0; i<labels.size(); ++i)
    printf("Hit: %s\n", labels[i]);

  // And any Unexpected Labels
  ccManager->GetUnexpected(labels, stats.unexpectedCount);
  for (unsigned i=0; i<labels.size(); ++i)
    printf("Unexpected: %s\n", labels[i]);

  // Update our copy of the Marker counts, to compare against next frame
  ccManager->GetStats(stats);
}

//--------------------------------------------------------------------------------------------------
void DumpManagerStatus()
{
   // We might choose to instantiate the Manager during he run, so tolerate it being NULL
   if (!ccManager) 
    return;
  
  // Output all known Labels of all categories to the console
  // This includes all Labels hit (Expected or Unexpected) and those present in the input file but not hit (Remaining)
  // It cannot include any Markers present in the source, but not hit and not present in the input file
  std::vector<const char*> labels;
  printf("%i expected Labels hit:\n", stats.hitCount);
  ccManager->GetHit(labels);
  for (unsigned i=0; i<labels.size(); ++i)
    printf("Hit: %s\n", labels[i]);

  printf("%i unexpected Labels found:\n", stats.unexpectedCount);
  ccManager->GetUnexpected(labels);
  for (unsigned i=0; i<labels.size(); ++i)
    printf("Unexpected: %s\n", labels[i]);

  printf("%i Labels remaining:\n", stats.remainingCount);
  ccManager->GetRemaining(labels);
  for (unsigned i=0; i<labels.size(); ++i)
    printf("Remaining: %s\n", labels[i]);
}