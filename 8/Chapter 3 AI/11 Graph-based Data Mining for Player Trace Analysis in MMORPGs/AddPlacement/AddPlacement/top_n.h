#ifndef TOP_N_H
#define TOP_N_H

#include <map>
#include <set>
#include <vector>

#include "add_placement.h"
#include "stats.h"

using std::map;
using std::set;
using std::vector;

void topn_compute_placement(int placement_count, WalkDb &db, set<int> &training, set<int> testing, double &training_coverage, double &testing_coverage)
{
	set<int> vertices_left;
	db.vertices_in_walks(training,vertices_left);

	set<int> best_set;

	for(int i = 0; i < placement_count; i++)
	{
		double best_coverage = 0;
		int best_vertex;

		set<int>::iterator to_remove;

		bool found = false;
		for(set<int>::iterator i = vertices_left.begin();
			i != vertices_left.end();
			i++)
		{
			set<int> set_to_try;
			set_to_try.insert(*i);			
			training_coverage = db.coverage(training, set_to_try);
			if (training_coverage > best_coverage)
			{
				best_coverage = training_coverage;
				to_remove = i;
				found = true;
			}
		}
		if(!found)
			to_remove = vertices_left.begin();

		best_set.insert(*to_remove);
		vertices_left.erase(to_remove);
	}

	training_coverage = db.coverage(training, best_set);
	testing_coverage = db.coverage(testing, best_set);
}

void topn_compute_placement_stat(int placement_count, 
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
		topn_compute_placement(placement_count, db, training, testing, curr_training_coverage, curr_testing_coverage);
		training_coverage_set.push_back(curr_training_coverage);
		testing_coverage_set.push_back(curr_testing_coverage);
	}

	mean_and_dev(training_coverage_set, mean_training_coverage, dev_training_coverage);
	mean_and_dev(testing_coverage_set, mean_testing_coverage, dev_testing_coverage);


}
#endif // TOP_N_H