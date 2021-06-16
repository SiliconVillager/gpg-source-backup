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

// Code Coverage Marker class
//
// Each Marker instance registers with the Tracker singleton when first hit
// Usage should always be via the macro defined below
//
// Advanced: The Reset functionality adds an extra static variable and an extra check to the assembler.
//           While included here for flexibility, you might choose to drop the Reset feture, roll Hit() into the
//           constructor, and thus reduce the footprint of your Markers

#ifndef CODECOVERAGEMARKER_H
#define CODECOVERAGEMARKER_H

#include "CodeCoverageTracker.h"

// Enable Code Coverage in this project
// This implementation aims to be efficient enough to be enabled for all internal builds, not just debug
#define CODE_COVERAGE_ENABLED


// Marker class, written for fast common case with minimal code bloat
class CCodeCoverageMarker
{
  friend class CCodeCoverageTracker;
public:

  // Constructor assigning text Label
  CCodeCoverageMarker( const char * sLabel )
  : m_sLabel(sLabel), m_bHit(false) {};

  // Called every time we execute code containing a Marker
  // Registers with the Tracker only on first Hit()
  // By separating this from the constructor, we allow Reset functionality
  inline void Hit()
  {
    if (!m_bHit)
    {
      m_bHit = true;
      CCodeCoverageTracker::GetInstance().Register(this);
    }
  }

  // Fetch label - useful to Manager class
  const char * GetLabel() const 
  {
    return m_sLabel; 
  }

protected:
  const char * m_sLabel;               // The text label of this marker
  bool m_bHit;                         // Cache whether marker is already registered
};

#ifdef CODE_COVERAGE_ENABLED
  #define CCMARKER( label ) \
    do { \
        static CCodeCoverageMarker ccMarker_##label( #label ); \
        ccMarker_##label.Hit(); \
    }while(0)
#else
  #define CCMARKER( label ) ((void)0)
#endif

#endif //CODECOVERAGEMARKER_H
