Custom STL Sample Code
======================

Author: Neil Gower <neilg@vertexblast.com>
Date:   September 2009

This is the sample code that accompanies my article in Game Programming 
Gems 8 about writing custom STL components, such as functors, containers,
iterators, and algorithms. It illustrates the techniques described in the
gem. Due to the limited amounts of testing that have been implemented, the 
code is probably not suitable for production use, but can serve as a good 
starting point for your own custom STL components.

The included license is the formal way of saying that the code is provided 
as-is, you can do what you want with it, and any problems you encounter are 
your own. If you find the code useful, feel free to drop me a note. I'd be 
happy to hear from you.


Requirements:
-------------

There are no requirements for this code beyond standard C++ and STL. It 
should work with just about any modern C++ compiler, though only GCC and 
MSVC have been tested.

The code has been compiled and run successfully on Linux (Ubuntu 9.04), OS X
(Leopard), and Windows XP with Visual C++ 2008 Express. 

Contents:
---------

The "src" directory contains all of the source code for the examples. The
.hpp files contain template classes. In "src/test" are the test drivers, each
with a main() function.

FixedArrayTest     - Test driver and code for the FixedArray template class.
STLOctree          - Test driver and code for the Octree template class.
CollisionAlgorithm - Test driver and code for the generate_collisions alg.

The test drivers are simple console applications that exercise the code and
assert various correctness properties. When they run successfully, they
simply exit with a 0 return code.

The build systems put the intermediate files in the "build" directory, and
the final executables in the "bin" directory.

Windows users should use the CustomStlExamples.sln solution to build the 
code, which contains one project for each of the test drivers.

For Linux and OS X users, the Makefile should do the trick. Just type:

  make all
  
to build all of the test drivers.
