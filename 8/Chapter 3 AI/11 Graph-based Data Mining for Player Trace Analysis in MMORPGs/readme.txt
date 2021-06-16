The source code in this folder accompanies the chapter "Graph-based Data Mining for Player Trace Analysis in MMORPGs" in GPG 8. Each of the folders contain code for performing specific tasks. 



1) AddPlacement - Placement of Advertisements in the World

----------------------------------------------------------

Usage: [Input File] [% Samples to use for Training] [# Advertisements to Place] [Output File]



Format of the Input File: Each line has the walk id and the discrete location (in sequence) as follows.
W1 L1
W1 L2
W1 L3
...
W2 L2
W2 L3
...

So, W1 starts with L1 then goes to L2 then to L3 and so on. W2 starts with L2 goes to W3 and so on.
An example can be found in AddPlacementTest.dat

Format of the output file: Contains the mean coverage and the standard deviation for each method, for each add placed.


Here is an example snippet 

# Filename:
25-walks.dat
# Training:0.005 %
# (1)Placement (2)RanTrainMean (3)RanTrainDev (4)RanTestMean (5)RanTestDev (6)MarTrainMean (7)MarTrainDev (8)MarTestMean (9)MarTestDev (10)GreTrainMean (11)GreTrainDev (12)GreTestMean (13)GreTestDev (14)TopTrainMean (15)TopTrainDev (16)TopTestMean (17)TopTestDev
1 0.215385 0.166765 0.22047 0.116324 0.230769 0.121626 0.251424 0.197523 0.538462 0.121626 0.401073 0.0682307 0.538462 0.121626 0.401073 0.0682307
2 0.338462 0.11666 0.33735 0.161257 0.4 0.183651 0.388857 0.188517 0.753846 0.100295 0.563681 0.0655279 0.630769 0.114095 0.508956 0.0499316
3 0.415385 0.159511 0.402476 0.197175 0.446154 0.19911 0.43343 0.152554 0.876923 0.0688021 0.656624 0.0506081 0.723077 0.0877058 0.603962 0.0645947



2) Dendrogram - Produces a Dendrogram for cluster analysis

-----------------------------------------------------------

Usage: Dendrogram.exe [Input File] [Output File]



Format of input file: Each line encodes the distance/similarity between two examples as follows.
e1 e2 value
e1 e3 value
e2 e3 value
...


An example is provided in DendrogramTest.dat



Format of output file: Specifies a tree where each vertex is an example and edges connect 2 vertices as follows. 
e1 e2
e1 e3
e3 e4
...



3) Partition - Preprocessing to superimpose a grid on the world

----------------------------------------------------------------

Usage:  [Input File] [Start] [End] [Ranges] [Output File]



Format of input file: One point on each line X, Y and Z locations as follows.
X1 Y1 Z1
X2 Y2 Z2
...

Example can be found in PartitionTest.dat



Format of output file: Same as the input file except that a column with the membership id is added to the end.



4) KMeans - Alternative to grid-based preprocessing

---------------------------------------------------

Usage: KMeans.exe [Data File (Input)] [Number of Clusters] [Max Iterations] [Centroids File (Output)] [Membership File (Output)]



Format of Input File: Each line has one sample point with all the features, as follows.
E1-F1 E1-F2 E1-F3
E2-F1 E2-F2 E2-F3
E3-F1 E3-F2 E3-F3
Where, E is the example, F is the feature. All values are floats. An example can be found in KMeansTest.dat



Format of Centroid file (output): Each line has one centroid with all the feature values

Format of Membership file (output) : Exactly like input file except there is one column added at the ending which indicates the cluster identifier



5) WalkKernel - Computation of LCS measure

------------------------------------------

Usage: WalkKernel [Input File] [Output File]



Format of input file: Same as the input file of AddPlacement


Format of output file: As follows.
walk1 walk2 len(walk1) len(walk2) lcs(walk1, walk2)
walk1 walk3 len(walk1) len(walk3) lcs(walk1, walk3)
walk1 walk4 len(walk1) len(walk4) lcs(walk1, walk4)
...



6) SVM - Use of the LCS measure with support vector machines

------------------------------------------------------------

Refer to http://svmlight.joachims.org




All source code is complete except for the following libraries (due to licence concernes these must be downloaded separately):

* SVMLight which can be downloaded from http://svmlight.joachims.org. You must replace the kernel.h file by the file we have provided and recompile.

* Boost, a utility library which can be downloaded at http://www.boost.org/users/download/

* TBB, Intel's open source threaded building block library, which can be downloaded at http://www.threadingbuildingblocks.org/download.php

* GSL, the GNU Scientific Library, which can be downloaded at http://www.gnu.org/software/gsl/



Our datasets are too large to be provided on disk. They can be downloaded from 
http://gameintelligencegroup.org/projects/data



