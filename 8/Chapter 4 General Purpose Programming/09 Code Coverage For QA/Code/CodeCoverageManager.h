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

// Code Coverage Manager class
//
// Handles loading of Expected Labels and sorting Labels from the Tracker into Hit and Unexpected categories
// Presents useful interface for implementing feedback (ideally through a GUI)
// Notes:
//  * The Manager deals only with the Labels (uniquely identifying strings) not the Markers themselves
//  * A Manager instance has no side-effects upon the Tracker
//  * The input file format read by this class consists:
//      Plain text, listing one Label on each line, in any order
//      DOS, Unix and Mac line-endings are all tolerated
//      See LabelFile.txt as an example

#ifndef CODECOVERAGEMANAGER_H
#define CODECOVERAGEMANAGER_H

#include <vector>
#include <set>

class CCodeCoverageManager
{
public:
  // Simple struct for returning hit counts of the different categories of Markers
  struct SStats
  {
    int remainingCount, hitCount, unexpectedCount;
    SStats() : remainingCount(0), hitCount(0), unexpectedCount(0) {};
  };

  // Create a Manager instance from a data file containing a list of expected Labels
  // If the file is invalid then NULL is returned
  // One instance is sufficient - multiple instances sharing the Tracker might be useful in extended implementations
  static CCodeCoverageManager * CreateInstance( const char * path );

  // Destroy an instance
  ~CCodeCoverageManager() {};

  // Queries the Tracker and updates the Manager with any Markers hit since the last call
  // Note that loading can be delayed to late in a run, in which case many Markers will be found on first Update
  void Update();

  // Get basic data such as the count of each category of Marker
  void GetStats( SStats &stats ) const { stats = m_stats; }

  // Get the Labels of all remaining Markers
  // Their number will decrease as we hit Expected Markers
  // Pointers are valid until instance is destroyed
  void GetRemaining( std::vector <const char *> &labels ) const;

  // Get Labels of Hit Markers, in the order that they were hit
  // Optionally, skip the first n Markers - so we can collect just those recently hit
  // Pointers are valid until instance is destroyed
  void GetHit( std::vector <const char *> &labels, int nFirst = 0) const;

  // Get Labels of Enexpected Markers, in the order that they were hit
  // Optionally, skip the first n Markers - so we can collect just those recently hit
  // Pointers are valid until instance is destroyed
  void GetUnexpected( std::vector <const char *> &labels, int nFirst = 0) const;

  // Reset the Manager to the same state it had immediately after reading the input data file
  void Reset();

protected:
  // Created instances are only available with a valid datafile
  CCodeCoverageManager() {};

  // Shouldn't be needed, not implemented
  CCodeCoverageManager(const CCodeCoverageManager&);
  CCodeCoverageManager& operator=(const CCodeCoverageManager&);

  // Attempt to load expected Labels from a file
  bool LoadLabels( const char * path );

  // Scan the block of loaded Labels text and use it to populate the set of Remaining Labels
  void PopulateRemainingLabels();

  // Check if we have already hit an identical Label
  // Only for checking consistency - see CHECK_DUPLICATES
  bool IsDuplicate( const char * sLabel ) const;

  // Current count values for each category of Label
  SStats m_stats;

  // Single block of text loaded from disk, storing the Labels of the expected Markers
  // Hence they should in fact be duplicates of strings already present somewhere in the binary
  std::vector <char> m_loadedLabelsBlock;

  // These two vectors will grow as we hit checkpoints. 
  // The only time we want to search them is when an Unexpected checkpoint is found (if CHECK_DUPLICATES is enabled)
  std::vector <const char *> m_labelsHit, m_labelsUnexpected;

  // String compare used in the set of Remaining labels
  // Since we load Labels from out input file we must compare content, not pointers
  struct SStringCompare : public std::binary_function<const char*, const char*, bool>
  {
    inline bool operator() ( const char * a, const char * b ) const { return (strcmp(a,b) < 0); }
  };

  // This set is populated when we load a Marker data file for a level.
  // It will then shrink as we hit Markers and move the Labels into the two vectors. We must search it on each new hit.
  std::set <const char *, SStringCompare> m_labelsRemaining;

  // Buffer for querying Tracker (simple way to avoid allocations)
  std::vector <const char *> m_tmpLabelBuffer;
};


#endif //CODECOVERAGEMANAGER_H