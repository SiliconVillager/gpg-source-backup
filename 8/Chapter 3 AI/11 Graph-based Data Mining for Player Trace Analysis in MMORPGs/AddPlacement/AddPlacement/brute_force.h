#ifndef BRUTE_FORCE_H
#define BRUTE_FORCE_H
#include <set>
#include <stack>
#include "stats.h"

using namespace std;

class Combination
{
public:
	void add_item(int given);
	bool initiate(int given_max_elements);
	bool get_next_combination( set<int> &result);	
private:
	int max_elements;
	set<int> elements;
	stack <set<int> > done_stack;
	stack <set<int>::iterator> remaining_stack;
	stack <int> remaining_count;
};	

void Combination::add_item(int given)
{
	elements.insert(given);
}

bool Combination::initiate(int given_max_elements)
{
	if (given_max_elements <= elements.size())
	{
		max_elements = given_max_elements;
		set<int>::iterator curr_remaining = elements.begin();

		int curr_remaining_count =  elements.size() - 1;

		for(set<int>::iterator i = elements.begin();
			i != elements.end();
			i++)
		{
			set<int> curr_done;
			curr_done.insert(*i);
			done_stack.push(curr_done);

			curr_remaining++;
			remaining_stack.push(curr_remaining);

			remaining_count.push(curr_remaining_count--);
		}
	}
	else
		return false;
}

bool Combination::get_next_combination( set<int> &result)
{
	if (!done_stack.empty())
	{
		set<int> curr_done = done_stack.top();
		done_stack.pop();

		set<int>::iterator curr_remaining = remaining_stack.top();
		remaining_stack.pop();

		int curr_remaining_count = remaining_count.top();
		remaining_count.pop();

		if (curr_done.size() == max_elements)
		{
			result = curr_done;
			return true;
		}
		else 
		{
			set<int>::iterator next_remaining = curr_remaining;
			for(set<int>::iterator i = curr_remaining;
				i != elements.end();
				i++)
			{
				if (curr_remaining_count + curr_done.size() >= max_elements)
				{
					set<int> next_done = curr_done;
					next_done.insert(*i);
					done_stack.push(next_done);

					next_remaining++;
					remaining_stack.push(next_remaining);

					remaining_count.push(curr_remaining_count--);
				}
			}
			return get_next_combination(result);
		}
	}
	else
		return false;
}

void bruteforce_compute_placement(int placement_count, WalkDb &db, set<int> &training, set<int> testing, double &training_coverage, double &testing_coverage)
{
	set<int> vertices_to_consider;
	db.vertices_in_walks(training, vertices_to_consider);
	
	Combination combi;
	for(set<int>::iterator i = vertices_to_consider.begin();
		i != vertices_to_consider.end();
		i++)
		combi.add_item(*i);

	combi.initiate(placement_count);
	set<int> set_to_try;
	set<int> best_set;
	double best_coverage = 0;
	while(combi.get_next_combination(set_to_try))
	{
		training_coverage = db.coverage(training, set_to_try);
		if (training_coverage >= best_coverage)
		{
			best_coverage = training_coverage;
			best_set = set_to_try;
		}
	}
	training_coverage = db.coverage(training, best_set);
	testing_coverage = db.coverage(testing, best_set);
}

void bruteforce_compute_placement_stat(int placement_count, 
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
		bruteforce_compute_placement(placement_count, db, training, testing, curr_training_coverage, curr_testing_coverage);
		training_coverage_set.push_back(curr_training_coverage);
		testing_coverage_set.push_back(curr_testing_coverage);
	}

	mean_and_dev(training_coverage_set, mean_training_coverage, dev_training_coverage);
	mean_and_dev(testing_coverage_set, mean_testing_coverage, dev_testing_coverage);

}

#endif //BRUTE_FORCE_H