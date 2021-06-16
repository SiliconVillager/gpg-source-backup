#
# Quick and dirty makefile for CustomStlExamples. This makefile handles 
# building all of the test drivers.
#

# Build options
CC = g++
CFLAGS = -g -Wall -pedantic -Werror

# Project structure
BINARIES = FixedArrayTest OctreeTest CollisionAlgTest
OCTREE_TEMPLATES = ChooseType.hpp CollisionAlgorithm.hpp \
		   CollisionHelpers.hpp CompileTimeChecker.hpp FixedArray.hpp \
		   Octree.hpp OctreeHelpers.hpp

# Build Targets
ALL: $(BINARIES)
	

clean :
	cd build; rm -f *.o; cd ../bin; rm -f $(BINARIES);

# Internal targets
FixedArrayTest_Main.o   : test/FixedArrayTest_Main.cpp FixedArray.hpp
OctreeTest_Main.o       : test/OctreeTest_Main.cpp $(OCTREE_TEMPLATES)
CollisionAlgTest_Main.o : test/CollisionAlgTest_Main.cpp $(OCTREE_TEMPLATES) \
		          CollisionAlgorithm.hpp CollisionHelpers.hpp	

# General purpose build rules
% : %_Main.o
	cd build; $(CC) -o ../bin/$@ $(CFLAGS) $<

%.o : test/%.cpp
	cd src; $(CC) -o ../build/$@ -c $(CFLAGS) $< -I .

%.cpp :
	
%.hpp :
	
