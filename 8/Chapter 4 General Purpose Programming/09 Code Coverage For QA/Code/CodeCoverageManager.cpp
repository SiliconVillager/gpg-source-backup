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

// Code Coverage Manager

#include "CodeCoverageManager.h"
#include "CodeCoverageTracker.h"
#include <assert.h>
#include <stdio.h>
#include <algorithm>

#ifdef _MSC_VER
	#pragma warning (disable: 4996) // Switch off security warnings
#endif

//--------------------------------------------------------------------------------------------------
// Set a very generous limit to data file size (not allocated, just used as sanity check)
const int MAX_DATA_SIZE = 1000000;

// Enable this to detect if two Markers hit have the same Label
// This becomes inefficient only if your test run hits a large number of Unexpected Markers (unusual behaviour)
// A source code scanner could perform a similar check offline
const bool CHECK_DUPLICATES = true;

//--------------------------------------------------------------------------------------------------
CCodeCoverageManager * CCodeCoverageManager::CreateInstance( const char * path )
{
  // Create an instance and attempt to load the data file to it
  CCodeCoverageManager *pInstance = new CCodeCoverageManager();
  if (!pInstance->LoadLabels(path))
  {
    // If loading fails, clean up and return NULL
    delete pInstance;
    pInstance = NULL;
  }

  return pInstance;
}

//--------------------------------------------------------------------------------------------------
void CCodeCoverageManager::Update()
{
  // Get the Tracker and add up how many Markers there were last time, and how many now
  const CCodeCoverageTracker &tracker = CCodeCoverageTracker::GetInstance();
  int nTotalMarkersKnownHit = m_stats.hitCount + m_stats.unexpectedCount;
  int nNewMarkers = tracker.GetMarkerCount() - nTotalMarkersKnownHit;
  assert(nNewMarkers >= 0);

  // If no Markers have been hit since last time, then nothing to do
  if (nNewMarkers == 0)
    return;

  // Fetch just the Labels of the new Markers
  tracker.GetLabels( m_tmpLabelBuffer, nTotalMarkersKnownHit );
  assert(nNewMarkers == m_tmpLabelBuffer.size());

  // For each Marker hit, examine the Label and sort into "Hit" and "Unexpected"
  for (int i=0; i<nNewMarkers; ++i)
  {
    const char * sLabel = m_tmpLabelBuffer[i];
   
    // Was this Label present in our datafile, or is it an unexpected checkpoint?
    // If it is still in the list of Remaining Labels, erase it from there
    // Note that we can assume each Marker will only ever be "hit" once (until Tracker is reset),
    // but we may want to check for duplicates - different instances with the same Label
    bool bRemaining = ( m_labelsRemaining.erase(sLabel) > 0 );

    if (CHECK_DUPLICATES && !bRemaining && IsDuplicate(sLabel))
    {
      // You will probably want to add your own error logging code here instead of an assert
      assert(false && "Found duplicate Label");
      continue;
    }

    // Add to the appropriate vector.
    // We always store pointers to the actual Marker Label strings here, not those in the block loaded from disk
    std::vector <const char *> &v = ( bRemaining ? m_labelsHit : m_labelsUnexpected );
    v.push_back(sLabel);
  }

  // Clear buffer, for debugging's sake
  m_tmpLabelBuffer.clear();

  int nHitAndRemaining = m_stats.hitCount + m_stats.remainingCount;

  // Update stats
  m_stats.hitCount = m_labelsHit.size();
  m_stats.unexpectedCount = m_labelsUnexpected.size();
  m_stats.remainingCount = m_labelsRemaining.size();

  // Sanity check - apart from Unexpected, we should always have the same number of Labels
  assert(nHitAndRemaining == m_stats.hitCount + m_stats.remainingCount);
}

//--------------------------------------------------------------------------------------------------
void CCodeCoverageManager::GetRemaining( std::vector <const char *> &labels ) const
{
  labels.clear();
  std::set<const char *, SStringCompare>::const_iterator it, itEnd;
  for (it = m_labelsRemaining.begin(), itEnd = m_labelsRemaining.end(); it != itEnd; it++)
    labels.push_back(*it);
}

//--------------------------------------------------------------------------------------------------
void CCodeCoverageManager::GetHit( std::vector <const char *> &labels, int nFirst) const
{
  assert(nFirst >= 0);

  // Clear the output container
  labels.clear();

  unsigned nSize = m_labelsHit.size();
  for (unsigned i = nFirst; i < nSize; ++i)
    labels.push_back(m_labelsHit[i]);
}

//--------------------------------------------------------------------------------------------------
void CCodeCoverageManager::GetUnexpected( std::vector <const char *> &labels, int nFirst) const
{
  assert(nFirst >= 0);

  // Clear the output container
  labels.clear();

  unsigned nSize = m_labelsUnexpected.size();
  for (unsigned i = nFirst; i < nSize; ++i)
    labels.push_back(m_labelsUnexpected[i]);
}

//--------------------------------------------------------------------------------------------------
void CCodeCoverageManager::Reset()
{
  // Clear and repopulate Remaining labels from the original text read from the data file
  m_labelsRemaining.clear();
  PopulateRemainingLabels();
  assert( m_stats.hitCount + m_stats.remainingCount == m_labelsRemaining.size());

  // Clear the Hit and Unexpected vectors
  m_labelsHit.clear();
  m_labelsUnexpected.clear();

  // Reset the count stats
  m_stats.hitCount = 0;
  m_stats.unexpectedCount = 0;
  m_stats.remainingCount = m_labelsRemaining.size();
}

//--------------------------------------------------------------------------------------------------
bool CCodeCoverageManager::LoadLabels( const char * path )
{
  // Check a path is given
  assert(path && path[0]!=0);                              
  // Could support loading different files with the same Manager instance, but for now we do not
  assert(m_loadedLabelsBlock.empty());                     
  
  // Open the datafile
  // Even in Windows, just read it as binary
  FILE * fp = fopen(path, "rb");
  if (!fp) 
    return false;

  // Find out file length
  fseek(fp,0,SEEK_END);
  size_t nBytes = (size_t) ftell(fp);
  fseek(fp,0,SEEK_SET);

  // Sanity check the data file size
  if (nBytes <=0 || nBytes > MAX_DATA_SIZE)
  {
    fclose(fp);  
    return false;
  }

  // Allocate a single block of memory for all the Labels and read to it
  // Note that resize() fills with 0, which includes providing a final null terminator
  m_loadedLabelsBlock.resize(nBytes+1);
  int bBytesRead = fread( &m_loadedLabelsBlock[0], 1, nBytes, fp);
  fclose(fp);

  // If anything strange happens, abort
  // Note that if you were to open the file in Windows as text mode, not binary, this test may fail
  if (bBytesRead != nBytes)
  {
    m_loadedLabelsBlock.clear();
    return false;
  }

  // Scan the block and populate our Remaining Labels set
  PopulateRemainingLabels();

  // We hope to hit all labels eventually, so reserve that space in the vector
  int labelsCount = m_labelsRemaining.size();
  m_labelsHit.reserve(labelsCount);

  // Update stats
  m_stats.remainingCount = labelsCount;

  return true;
}

//--------------------------------------------------------------------------------------------------
// Scan a block of text read from disk to:
// - insert each Label into the set of Remaining markers
// - overwrite all newline characters with null-terminators
// Note that we perform this after reading the data file from disk and to reconstruct on Reset
void CCodeCoverageManager::PopulateRemainingLabels()
{
  bool bInLabel = false;
  unsigned nBytes = m_loadedLabelsBlock.size();
  for (unsigned i = 0; i < nBytes; ++i)
  {
    char &c = m_loadedLabelsBlock[i];
    if (c == '\r' || c == '\n' || c == '\0')
    {
      bInLabel = false;
      c = '\0';  // Replace newlines with null terminators, delimiting the C strings
    }
    else if (!bInLabel)
    {
      // This is the start of a new Label
      bInLabel = true;
      m_labelsRemaining.insert(&c);
    }
  }
}

//--------------------------------------------------------------------------------------------------
// Check if we have already hit an identical Label
// Only for checking consistency - see CHECK_DUPLICATES
bool CCodeCoverageManager::IsDuplicate( const char * sLabel ) const
{
  // String equality predicate function object
  // Since our loaded strings are duplicates of the Labels in the binary, we must compare content, not pointers
  struct SStringEquality
  {
    const char * a;
    SStringEquality (const char * _a) : a(_a) {}
    inline bool operator() (const char * b) const { return strcmp(a,b)==0; }
  } eq(sLabel);
  
  // Obviously these linear searches are inefficient, but as noted above:
  // This becomes inefficient only if your test run hits a large number of Unexpected Markers (unusual behaviour)
  // Rather than optimise the uncommon case, I've opted for simplicity
  std::vector<const char *>::const_iterator it;
  it = std::find_if( m_labelsHit.begin(), m_labelsHit.end(), eq);
  if (it != m_labelsHit.end())
    return true;
    
  it = std::find_if( m_labelsUnexpected.begin(), m_labelsUnexpected.end(), eq);
  if (it != m_labelsUnexpected.end())
    return true;

  return false;
}
