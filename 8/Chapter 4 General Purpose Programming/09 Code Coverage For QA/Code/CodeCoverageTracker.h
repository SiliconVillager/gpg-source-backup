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

// Code Coverage Tracker class
//
// A singleton, with an emphasis on minimalism to avoid skewing profiling results etc.
// Markers register with the Tracker when they are first hit and each is added to a vector
// Note that the Tracker works with Markers, but only hands out Labels (strings) in it's API

#ifndef CODECOVERAGETRACKER_H
#define CODECOVERAGETRACKER_H

#include <vector>

// Forward declarations
class CCodeCoverageMarker;


class CCodeCoverageTracker
{
public:
  // Get the singleton instance
  static CCodeCoverageTracker &GetInstance();

  // Register a Marker that has been hit with the Tracker
  void Register( CCodeCoverageMarker * pMarker );

  // Get number of markers registered so far
  int GetMarkerCount() const;

  // Input vector is cleared, then populated with the Labels of Markers hit so far
  // Optionally, skip the first n Markers - so we can collect just those newly registered
  void GetLabels( std::vector <const char *> &labels, int nFirst = 0) const;

  // Reset state of Tracker and Markers as if no Markers had been hit
  void Reset();

protected:
  // Singleton prescription: Meyers, as described in Alexandrescu
  CCodeCoverageTracker();
  ~CCodeCoverageTracker() {};
  CCodeCoverageTracker(const CCodeCoverageTracker&);
  CCodeCoverageTracker& operator=(const CCodeCoverageTracker&);

  // Vector of all Markers hit
  std::vector <CCodeCoverageMarker*> m_registeredMarkers;
};


#endif //CODECOVERAGETRACKER_H