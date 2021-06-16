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

// Toy Codebase class

#include "ToyCodebase.h"
#include "CodeCoverageMarker.h"

// Some dummy classes and methods to call, annotated with Markers
class ClassA
{
public:
  void MethodA()
  {
    // Re-introduce this line and run in Debug to demonstrate duplicate checks
    // CCMARKER(potential_duplicate);
  }

  void MethodB()
  {
    CCMARKER(ClassA_MethodB);
  }

  void MethodC(int a)
  {
    CCMARKER(ClassA_MethodC);
    switch (a)
    {
    case 0: 
      CCMARKER(potential_duplicate);
      break;
    case 1:
      CCMARKER(ClassA_MethodC_A);
      break;
    case 2:
      CCMARKER(ClassA_MethodC_B);
      break;
    case 6:
      CCMARKER(ClassA_MethodC_Another);
      break;
    default:
      break;
    }

		if (a%3 == 0)
			CCMARKER(ClassA_MethodC_Modulus);
		else
			CCMARKER(ClassA_MethodC_Remainder);
  }  
};


void CToyCodebase::Update( int nFrame )
{
  ClassA classA;

  // Delberate fall-thru - use more code on each loop
  switch(nFrame)
  {
    default:
    case 2:
      classA.MethodC(nFrame-1);
    case 1:
      classA.MethodB();
    case 0:
      classA.MethodA();
  }
}