#ifndef RANDOM_PLACEMENT_H
#define RANDOM_PLACEMENT_H

#include <map>
#include <set>
#include <vector>
#include <gsl/gsl_rng.h>

#include "add_placement.h"
#include "gsl/gsl_randist.h"
#include "stats.h"

using std::map;
using std::set;
using std::vector;

void random_compute_placement(int placement_count, WalkDb &db, set<int> &training, set<int> testing, double &training_coverage, double &testing_coverage, unsigned int key)
{
	set<int> vertices_left;
	db.vertices_in_walks(training,vertices_left);

	int *vertices_left_array = new int[vertices_left.size()];
	int counter = 0;
	for(set<int>::iterator i = vertices_left.begin(); 
		i != vertices_left.end();
		i++, counter ++)
		vertices_left_array[counter] = *i;

	const gsl_rng_type * T;
	gsl_rng * r;
	gsl_rng_env_setup();

	T = gsl_rng_default;
	r = gsl_rng_alloc (T);
	gsl_rng_set(r,key);

	int *vertices_chosen_array = new  int[placement_count];

	 gsl_ran_choose(r, vertices_chosen_array, placement_count, vertices_left_array, vertices_left.size(), sizeof (int));

	set<int> best_set;
	for(int i = 0; i < placement_count; i++)
		best_set.insert(vertices_chosen_array[i]);

	delete[] vertices_left_array;
	delete[] vertices_chosen_array;

	//cerr << "Random:";
	//for(set<int>::iterator i = best_set.begin();
	//	i != best_set.end();
	//	i++)
	//	cerr << *i << " ";
	//cerr << "\n";

	training_coverage = db.coverage(training, best_set);
	testing_coverage = db.coverage(testing, best_set);
}

void random_compute_placement_stat(int placement_count, 
								   WalkDb &db,  
								   double training_percent, 
								   double &mean_training_coverage,
								   double &dev_training_coverage,
								   double &mean_testing_coverage,
								   double &dev_testing_coverage)
{
	vector<double> training_coverage_set;
	vector<double> testing_coverage_set;

	for(int j = 0; j < MAX_RANDOM_KEYS; j++)
	{
		double curr_training_coverage;
		double curr_testing_coverage;
		set<int> training, testing;

		
		get_training_testing_sets_percent(training_percent, db.get_walk_count(), training, testing, random_key[j]);

		//cerr << "Training:";
		//for(set<int>::iterator i = training.begin();
		//	i != training.end();
		//	i++)
		//	cerr << *i << " ";
		//cerr << "\n";


		random_compute_placement(placement_count, db, training, testing, curr_training_coverage, curr_testing_coverage, random_key[j]);
		training_coverage_set.push_back(curr_training_coverage);
		testing_coverage_set.push_back(curr_testing_coverage);
	}

	

	mean_and_dev(training_coverage_set, mean_training_coverage, dev_training_coverage);
	mean_and_dev(testing_coverage_set, mean_testing_coverage, dev_testing_coverage);

}
#endif // RANDOM_PLACEMENT_H