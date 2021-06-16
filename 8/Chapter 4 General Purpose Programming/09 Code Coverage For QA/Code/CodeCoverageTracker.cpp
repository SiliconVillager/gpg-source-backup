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

// Code Coverage Tracker

#include "CodeCoverageTracker.h"
#include "CodeCoverageMarker.h"
#include <assert.h>

// Just used to reserve space
const size_t TYPICAL_MARKER_COUNT = 256;

//--------------------------------------------------------------------------------------------------
CCodeCoverageTracker &CCodeCoverageTracker::GetInstance()
{
  static CCodeCoverageTracker instance;
  return instance;
}

//--------------------------------------------------------------------------------------------------
void CCodeCoverageTracker::Register( CCodeCoverageMarker * pMarker )
{
  assert(pMarker);
  m_registeredMarkers.push_back(pMarker);
}

//--------------------------------------------------------------------------------------------------
int CCodeCoverageTracker::GetMarkerCount() const
{
  return m_registeredMarkers.size();
}

//--------------------------------------------------------------------------------------------------
void CCodeCoverageTracker::GetLabels( std::vector <const char *> &labels, int nFirst) const
{
  assert(nFirst >= 0);

  // Clear the output container
  labels.clear();

  int nSize = m_registeredMarkers.size();
  for (int i = nFirst; i < nSize; ++i)
    labels.push_back(m_registeredMarkers[i]->GetLabel());
}

//--------------------------------------------------------------------------------------------------
void CCodeCoverageTracker::Reset()
{
  // First, reset the cached state of each Marker we have already hit
  int nSize = m_registeredMarkers.size();
  for (int i = 0; i < nSize; ++i)
    m_registeredMarkers[i]->m_bHit = false;

  // Now forget them all
  m_registeredMarkers.clear();
}

//--------------------------------------------------------------------------------------------------
CCodeCoverageTracker::CCodeCoverageTracker()
{
  // Just reserve space to reduce allocations
  m_registeredMarkers.reserve(TYPICAL_MARKER_COUNT);
}
