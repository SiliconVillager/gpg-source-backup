#ifndef ADD_PLACEMENT_H
#define	ADD_PLACEMENT_H

#include <map>
#include <vector>
#include <set>
#include <algorithm>
#include <string>
#include <fstream>
#include <sstream>
#include <iostream>

#include <boost/tokenizer.hpp>
#include <boost/random/linear_congruential.hpp>
#include <boost/random/uniform_int.hpp>
#include <boost/random/variate_generator.hpp>
#include <boost/numeric/ublas/matrix.hpp>
#include <boost/numeric/ublas/io.hpp>

#include <gsl/gsl_rng.h>

using std::map;
using std::pair;
using std::vector;
using std::set;
using std::string;
using std::ifstream;
using std::istringstream;
using std::cerr;

using boost::numeric::ublas::matrix;

// Houses a single walk in the map
class Walk
{
public:
	void add_vertex(int given); // Add a vertex to the walk
	bool check_covered_single(int given); // Check if the given vertex is in the walk
	bool check_covered_set(set<int> &given); // Check if any member of the set is in the walk
	void accumulate_vertices(set<int> &result); // Put vertices in this walk in some given set, use for accumulation
	void erase(); // Erase all information in the walk
	void accumulate_transition_counts(matrix<double> &result, map<int,int> &vertex_mappings); //Accumulate transitions
	void accumulate_start_counts(vector<double> &result, map<int,int> &vertex_mappings); // Accumulate start vertices
private:
	vector<int> ordered; // Stores movement from vertex to vertex ordered[i] = x, ordered[i+1] = y implies movement from x to y
	set<int> elements; // Set of vertices in the walk
};

// Put vertices in this walk in some given set, use for accumulation
void Walk::accumulate_vertices(set<int> &result)
{
	int counter = 0;
	int prev = -1;
	for(vector<int>::iterator i = ordered.begin();
		i != ordered.end();
		i++)
	{
		int next = *i;
		if (prev == -1)
			prev = next;
		else
		{
			counter++;
			if(counter > 0)
				result.insert(prev);
			prev = next;
		}
	}
}

//Accumulate transitions
void Walk::accumulate_start_counts(vector<double> &result, map<int,int> &vertex_mappings)
{
	result[vertex_mappings[ordered[0]]]++;
}

//Accumulate transitions
void Walk::accumulate_transition_counts(matrix<double> &result, map<int,int> &vertex_mappings)
{
	int prev = -1;
	for(vector<int>::iterator i = ordered.begin();
		i != ordered.end();
		i++)
	{
		int next = *i;
		if (prev == -1)
			prev = next;
		else
		{
			int row = vertex_mappings[prev];
			int column = vertex_mappings[next];
			result(row,column) += 1;
			prev = next;
		}
	}
}

// Add a vertex to the walk
void Walk::add_vertex(int given)
{
	ordered.push_back(given);
	elements.insert(given);
}

// Check if the given vertex is in the walk
bool Walk::check_covered_single(int given)
{
	return !(elements.find(given) == elements.end());
}

// Check if any member of the set is in the walk
bool Walk::check_covered_set(set<int> &given)
{
	for(set<int>::iterator i = given.begin();
		i != given.end();
		i++)
	{
		if (check_covered_single(*i))
			return true;
	}
	return false;
}

// Erase all information in the walk
void Walk::erase()
{
	ordered.erase(ordered.begin(), ordered.end());
	elements.erase(elements.begin(), elements.end());
}

// Houses the entire dataset of walks
class WalkDb
{
public:
	void add_walk(Walk given); // Add a walk to the database
	double coverage(set<int> &walks, set<int> &placement); // Compute the coverage for a placement on a given set of walks
	void add_vertex(int given); // Add vertex to set of vertices
	void vertices_in_walks(set<int> &walks, set<int> &result); // Get all vertices in a set of walks
	int get_walk_count(); // Return the number of walks in the db
	void read_db(string filename); // Read db from file
	void get_transition_counts(set<int> &walks, matrix<double> &result, map<int,int> &vertex_mappings); // Get transition matrix
	void get_start_counts(set<int> &walks, vector<double> &result, map<int,int> &vertex_mappings);
private:
	vector<Walk> db;
	set<int> vertices;
};

// Get all vertices in a set of walks
void WalkDb::vertices_in_walks(set<int>& walks, set<int>& result)
{
	for(set<int>::iterator i = walks.begin();
		i != walks.end();
		i++)
		(db[*i]).accumulate_vertices(result);
}

// Get transition matrix
void  WalkDb::get_start_counts(set<int> &walks, vector<double> &result, map<int,int> &vertex_mappings)
{
	set<int> given_vertices;
	vertices_in_walks(walks, given_vertices);

	for(set<int>::iterator i = walks.begin();
		i != walks.end();
		i++)
		(db[*i]).accumulate_start_counts(result, vertex_mappings);
}

// Get transition matrix
void  WalkDb::get_transition_counts(set<int> &walks, matrix<double> &result, map<int,int> &vertex_mappings)
{
	set<int> given_vertices;
	vertices_in_walks(walks, given_vertices);

	for(set<int>::iterator i = walks.begin();
		i != walks.end();
		i++)
		(db[*i]).accumulate_transition_counts(result, vertex_mappings);
}

// Add vertex to set of vertices
void WalkDb::add_vertex(int given)
{
	vertices.insert(given);
}

// Add a walk to the database
void WalkDb::add_walk(Walk given)
{
	db.push_back(given);
}

// Compute the coverage for a placement on a given set of walks
double WalkDb::coverage(set<int> &walks, set<int>& placement)
{
	double coverage = 0;
	for(set<int>::iterator i = walks.begin();
		i != walks.end();
		i++)
	{
		if ((db[*i]).check_covered_set(placement))
			coverage++;
	}
	return coverage/walks.size();
}

// Return the number of walks in the db
int WalkDb::get_walk_count()
{
	return db.size();
}

// Read db from file
void WalkDb::read_db(string filename)
{
	string line;
	ifstream input_file(filename.c_str());

	typedef boost::tokenizer<boost::char_separator<char> > tokenizer;
	boost::char_separator<char> sep(" ");

	bool first = true;
	int prev_walk;
	int prev_vertex;
	Walk temp_walk;

	if (input_file.is_open())
	{
		int line_number  = 0;
		while (! input_file.eof() )
		{
			line_number++;
			std::getline(input_file,line);
			tokenizer tokens(line, sep);

			int curr_walk;
			int curr_vertex;
			
			tokenizer::iterator i = tokens.begin();
			if (i != tokens.end())
			{
				istringstream iss(*i);
				iss >> curr_walk;
			}
			else
				continue;			
			i++;

			if (i != tokens.end())
			{
				istringstream iss(*i);
				iss >> curr_vertex;
			}
			else
				continue;
		
			if(first)
			{
				first = false;
				temp_walk.add_vertex(curr_vertex);
				this->add_vertex(curr_vertex);
				prev_walk = curr_walk;
				prev_vertex = curr_vertex;
			}
			else
			{
				if (prev_walk == curr_walk)
				{
					if(prev_vertex != curr_vertex)
					{
						temp_walk.add_vertex(curr_vertex);
						this->add_vertex(curr_vertex);
						prev_vertex = curr_vertex;
					}
				}
				else
				{
					this->add_walk(temp_walk);
					temp_walk.erase();
					temp_walk.add_vertex(curr_vertex);
					this->add_vertex(curr_vertex);
					prev_walk = curr_walk;
					prev_vertex = curr_vertex;
				}
			}
		}
		this->add_walk(temp_walk);
	}
}

void get_training_testing_sets(long training_size, int max_size, set<int> &training, set<int> &testing, unsigned int key = 12345)
{
	for(long i = 0; i < max_size; i++)
		testing.insert(i);

	const gsl_rng_type * T;
	gsl_rng * r;
	gsl_rng_env_setup();

	T = gsl_rng_default;
	r = gsl_rng_alloc (T);
	gsl_rng_set(r,key);

	for(long i = 0; i < training_size; i++)
	{
		long curr = gsl_rng_uniform_int (r, max_size);
		if (testing.find(curr) != testing.end())
			testing.erase(curr);
		training.insert(curr);
	}

	gsl_rng_free (r);
}

void get_training_testing_sets_percent(float training_percent, int max_size, set<int> &training, set<int> &testing, unsigned int key)
{
	int training_size = ceil(training_percent * max_size);
	get_training_testing_sets(training_size, max_size, training, testing, key);
}
#endif	// ADD_PLACEMENT_H

