#ifndef MARKOV_H
#define MARKOV_H

#define EPS 0.000001

#include <map>
#include <set>
#include <vector>

#include <gsl/gsl_linalg.h>
#include <gsl/gsl_blas.h>
#include <cmath>

#include <boost/numeric/ublas/matrix.hpp>
#include <boost/numeric/ublas/io.hpp>

#include "add_placement.h"
#include "stats.h"

using std::pair;
using std::map;
using std::set;
using std::vector;

using boost::numeric::ublas::matrix;

bool solve_with_Power(double *a_data, double *b_data, int size, double *result)
{

	gsl_vector *y = gsl_vector_alloc (size);
	for(int i = 0; i < size; i++)
		gsl_vector_set(y, i, 0);

	gsl_vector *x = gsl_vector_alloc (size);
	for(int i = 0; i < size; i++)
	{
		//cerr << b_data[i] << " ";
		gsl_vector_set(x, i, b_data[i]);
	}
	//cerr << "\n";

	gsl_matrix *m = gsl_matrix_alloc(size, size);
	for(int i = 0; i < size; i++)
		for(int j = 0; j < size; j++)
			gsl_matrix_set(m, i, j, a_data[i * size + j]);

	for(int i = 0; i < 50; i++)
	{
		gsl_blas_dgemv (CblasTrans, 1, m, x, 1, y);
		for(int j = 0; j < size; j++)
			gsl_vector_set(x,j,gsl_vector_get(y,j));

		for(int j = 0; j < size; j++)
			gsl_vector_set(y, j, 0);
	}

	for(int i = 0; i < size; i++)
		result[i] = gsl_vector_get (x, i);

	gsl_vector_free(x);
	gsl_vector_free(y);
	gsl_matrix_free(m);
	return true;
}

void markov_compute_placement(int placement_count, WalkDb &db, set<int> &training, set<int> testing, double &training_coverage, double &testing_coverage)
{
	set<int> vertices_left;
	db.vertices_in_walks(training,vertices_left);
	int matrix_size = vertices_left.size();
	matrix<double> transitions(matrix_size, matrix_size);
	for(int i = 0; i < matrix_size; i++)
		for(int j = 0; j < matrix_size; j++)
			transitions(i, j) = 0;

	map<int, int> vertex_mappings;
	map<int, int> vertex_back_mappings;
	int counter = 0;
	for(set<int>::iterator i = vertices_left.begin();
		i != vertices_left.end(); 
		i++, counter++)
	{
		vertex_mappings.insert(pair<int, int>(*i, counter));
		vertex_back_mappings.insert(pair<int, int>(counter, *i));
	}

	db.get_transition_counts(training, transitions, vertex_mappings);

	for(int i = 0; i < matrix_size; i++)
	{
		double total = 0;
		for(int j = 0; j < matrix_size; j++)
			total += transitions(i, j);

		if (total <= 0)
			cerr << "Problem";

		for(int j = 0; j < matrix_size; j++)
			transitions(i, j) /= total;
	}

	vector<double> start_probabilities;
	for(int i = 0; i < matrix_size; i++)
		start_probabilities.push_back(0.0);

	db.get_start_counts(training, start_probabilities, vertex_mappings);

	double *start_probabilities_matrix = new double[matrix_size];

	for(int i = 0; i < matrix_size; i++)
		start_probabilities_matrix[i] = start_probabilities[i]/training.size();


	double *transition_probabilities = new double[matrix_size * matrix_size];
	double *steady_state_probabilities = new double[matrix_size];

	for(int i = 0; i < matrix_size; i++)
		for(int j = 0; j < matrix_size; j++)
			transition_probabilities[i * matrix_size + j] = transitions(i,j);


	solve_with_Power(transition_probabilities, start_probabilities_matrix, matrix_size, steady_state_probabilities);

	set<int> best_set;
	for(int i = 0; i < placement_count; i++)
	{
		double best = 0;
		set<int>::iterator to_erase;
		for(set<int>::iterator i = vertices_left.begin();
			i != vertices_left.end();
			i++)
			{
				if(steady_state_probabilities[vertex_mappings[*i]] > best)
				{
					best = steady_state_probabilities[vertex_mappings[*i]];
					to_erase = i;
				}
			}

	//	cerr << "Probability of selected: " << *to_erase << "=>" << steady_state_probabilities[vertex_mappings[*to_erase]] << "\n";
		best_set.insert(*to_erase);
		vertices_left.erase(to_erase);
	}
	
	delete[] transition_probabilities;
	delete[] steady_state_probabilities;
	delete[] start_probabilities_matrix;

	//cerr << "Markov:";
	//for(set<int>::iterator i = best_set.begin();
	//	i != best_set.end();
	//	i++)
	//	cerr << *i << " ";
	//cerr << "\n";

	training_coverage = db.coverage(training, best_set);
	testing_coverage = db.coverage(testing, best_set);
}

void markov_compute_placement_stat(int placement_count, 
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
		markov_compute_placement(placement_count, db, training, testing, curr_training_coverage, curr_testing_coverage);

		training_coverage_set.push_back(curr_training_coverage);
		testing_coverage_set.push_back(curr_testing_coverage);
	}

	mean_and_dev(training_coverage_set, mean_training_coverage, dev_training_coverage);
	mean_and_dev(testing_coverage_set, mean_testing_coverage, dev_testing_coverage);

}
#endif // MARKOV_H