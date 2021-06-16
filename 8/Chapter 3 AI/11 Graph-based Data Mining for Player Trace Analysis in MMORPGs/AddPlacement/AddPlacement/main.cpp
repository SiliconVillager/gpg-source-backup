#include <iostream>
#include "add_placement.h"
#include "greedy_placement.h"
#include "brute_force.h"
#include "top_n.h"
#include "markov.h"
#include "stats.h"
#include "random_placement.h"
using std::cerr;
using std::ofstream;
int main(int argc, char *argv[]){
	if (argc < 3){
		std::cerr << "Usage: " << argv[0] << " [Data] [% Training] [Output]\n";
		return 1;
	}
	WalkDb db;
	cerr << "Advertisement Placement\n";
	cerr << "Reading " << argv[1] << "\n";
	db.read_db(argv[1]);
	cerr << "Read " << db.get_walk_count() << " walks.\n";
	ofstream results_file(argv[3]);
	cerr << "Writing results to: " << argv[3] << "\n";
	double TRAIN = atof(argv[2]);
	cerr << "Processing...\n";
	results_file << "# Filename:" << argv[1] << "\n";
	results_file << "# Training:" << TRAIN << " %\n";
	results_file << "# (1)Placement (2)RanTrainMean (3)RanTrainDev (4)RanTestMean (5)RanTestDev (6)MarTrainMean (7)MarTrainDev (8)MarTestMean (9)MarTestDev ";
	results_file << "(10)GreTrainMean (11)GreTrainDev (12)GreTestMean (13)GreTestDev (14)TopTrainMean (15)TopTrainDev (16)TopTestMean (17)TopTestDev\n";
	for(int i = 1; i < 50; i++){
		double mean_training_coverage;
		double dev_training_coverage;
		double mean_testing_coverage;
		double dev_testing_coverage;
		random_compute_placement_stat(i,db,TRAIN,mean_training_coverage, dev_training_coverage, mean_testing_coverage, dev_testing_coverage);
		results_file << i  << " " << mean_training_coverage << " " << dev_training_coverage << " " << mean_testing_coverage << " " << dev_testing_coverage << " ";
		markov_compute_placement_stat(i,db,TRAIN,mean_training_coverage, dev_training_coverage, mean_testing_coverage, dev_testing_coverage);
		results_file << mean_training_coverage << " " << dev_training_coverage << " " << mean_testing_coverage << " " << dev_testing_coverage << " ";	
		greedy_compute_placement_stat(i,db,TRAIN,mean_training_coverage, dev_training_coverage, mean_testing_coverage, dev_testing_coverage);
		results_file << mean_training_coverage << " " << dev_training_coverage << " " << mean_testing_coverage << " " << dev_testing_coverage << " ";		
		topn_compute_placement_stat(i,db,TRAIN,mean_training_coverage, dev_training_coverage, mean_testing_coverage, dev_testing_coverage);
		results_file << mean_training_coverage << " " << dev_training_coverage << " " << mean_testing_coverage << " " << dev_testing_coverage << "\n";
		cerr << i << " ";
	}
	cerr << "\nDone.\n";
	results_file.close();
	return 0;
}