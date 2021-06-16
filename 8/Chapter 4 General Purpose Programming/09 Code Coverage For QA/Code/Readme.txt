-----------------------------------------------
Code Coverage for QA: An example implementation
-----------------------------------------------

* Notes

This implementation is intended to be efficient and practical as-is, with a couple of caveats:
 - it is not thread-safe
 - you may wish to add a line calling your logger in the "Found duplicate Label" check in CodeCoverageManager.cpp

This implementation provides reset functionality, such that all Marker state can be reverted to appear as it was when the program was started and thus, in principle, QA can rerun the testing of a level or load a new level, without quitting and restarting. However, note that in many codebases there will be code that is hit only when initialising for the first time, and run again - and that may affect subsequent Code Coverage results. For simplicity, I recommend beginning with instructions for QA to quit and restart after each level.
  If you do decide not to use the Reset functionality, consider removing it to reduce the footprint of Markers - see "Advanced" comment in CodeCoverageMarker.h


* Glossary

Rough definitions of some key terms I use repeatedly in the source code comments:
  Marker     - Static object embedded in the source code via a macro. We track when it is first hit during execution.
  Label      - The text that uniquely identifies each Marker
  Expected   - A marker/label that was present in the input file
  Hit        - An expected marker/label that we have hit
  Unexpected - A marker/label that was not present in the input file for this test run, but has been hit
  Remaining  - An expected marker/label that has not yet been hit


* Implementing a GUI

Including a GUI with this example did not seem practical, but I argue in the article that real-time feedback to QA is essential to get the full potential out of this Gem. Hence, I strongly recommend writing a simple GUI yourself, which is a simple job given the debug-drawing facilities in most engines. A few tips to the implementer:
 - The Manager API is simple but should provide all the data required for a GUI
 - When new Markers are hit, put their Labels into your own datastructures to keep them around for display
 - Label pointers from a Manager are valid until the Manager instance is destroyed
 - For examples of features and overall presentation, see the article and associated images