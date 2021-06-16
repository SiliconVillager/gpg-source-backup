#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <cmath>
#include <boost/tokenizer.hpp>
#include <boost/random/linear_congruential.hpp>
#include <boost/random/uniform_int.hpp>
#include <boost/random/variate_generator.hpp>

#include "tbb/task_scheduler_init.h"
#include "tbb/parallel_for.h"
#include "tbb/blocked_range.h"
#include "tbb/concurrent_vector.h"

#define EPS 0.00001

class Point
{
public:
	void add_value(double value);
	void set_values(const Point &given);
	double get_distance(Point &given);
	void values_addition(Point &given);
	void values_division(long given);
	bool values_equal(Point &given);
	void values_set_zero();
	void values_add_zeros(long given);
	void values_erase();
	void print_point(std::ostream &given);
private:
	std::vector<double> values;
};

void Point::print_point(std::ostream &given)
{
	for(std::vector<double>::iterator i = values.begin();
		i != values.end();
		i++)
		given << *i << " ";
}

void Point::add_value(double value)
{
	values.push_back(value);
}

double Point::get_distance(Point &given)
{
	double distance = 0;
	for(long i = 0; i < values.size(); i++)
		distance += (given.values[i] - values[i]) * (given.values[i] - values[i]);
	return std::sqrt(distance);
}

void Point::set_values(const Point &given)
{
	values = given.values;
}

void Point::values_addition(Point& given)
{
	std::vector<double>::iterator j = given.values.begin();
	for(std::vector<double>::iterator i = values.begin();
		i != values.end();
		i++, j++)
		*i = *i + *j;
}

void Point::values_division(long given)
{
	for(std::vector<double>::iterator i = values.begin();
		i != values.end();
		i++)
		*i = *i/given;
}
void Point::values_set_zero()
{
	for(std::vector<double>::iterator i = values.begin();
		i != values.end();
		i++)
		*i = 0;
}

bool Point::values_equal(Point &given)
{
	std::vector<double>::iterator j = given.values.begin();
	for(std::vector<double>::iterator i = values.begin();
		i != values.end();
		i++, j++)
		if (! std::fabs(*i  - *j) <  EPS)
			return false;
	return true;
}

void Point::values_add_zeros(long given)
{
	for(long i = 0; i < given; i++)
		values.push_back(0);
}

void Point::values_erase()
{
	values.erase(values.begin(), values.end());
}

class DataPoint: public Point
{
public:
	long get_centroid();
	void set_centroid(long given_centroid);
	void print_point(std::ostream &given);
private:
	long centroid;
};

void DataPoint::print_point(std::ostream &given)
{
	Point::print_point(given);
	given << centroid;
}

long DataPoint::get_centroid()
{
	return centroid;
}

void DataPoint::set_centroid(long given_centroid)
{
	centroid = given_centroid;
}

class KMeans
{
public:
	void read_data(std::string filename);
	void compute(long input_k, long input_max_iterations);
	void write_centroids(std::string filename);
	void write_membership(std::string filename);
private:
	friend class apply_compute_centroids;
	friend class apply_compute_membership;
	void set_random_centroids();
	void compute_membership();
	void compute_centroids();
	bool check_convergence();
	void update_old_clear_new();
	void dump_centroids();
	void dump_data();
	long k;
	long max_iterations;
	tbb::concurrent_vector<DataPoint> Data;
	tbb::concurrent_vector<Point> OldCentroids;
	tbb::concurrent_vector<Point> NewCentroids;
	tbb::concurrent_vector<long> points_in_cluster;
};

class apply_compute_centroids
{
public:
	apply_compute_centroids(KMeans *given): kmeans(given) {}

	void operator() (const tbb::blocked_range<long> &r) const
	{
		for(long i = r.begin(); i != r.end(); ++i)
		{
			for(tbb::concurrent_vector<DataPoint>::iterator j = kmeans->Data.begin();
				j != kmeans->Data.end();
				j++)
			{
				if (j->get_centroid() == i)
				{
					(kmeans->NewCentroids[j->get_centroid()]).values_addition(*j);
					kmeans->points_in_cluster[j->get_centroid()]++;
				}
			}
		}
	}
private:
	KMeans *kmeans;
};

class apply_compute_membership
{
public:
	apply_compute_membership(KMeans *given): kmeans(given) {}

	void operator() (const tbb::blocked_range<tbb::concurrent_vector<DataPoint>::iterator> &r) const
	{
		for(tbb::concurrent_vector<DataPoint>::iterator i = r.begin();
			i != r.end();
			i++)
		{
			double min_distance = -1;
			long centroid_id = -1;
			for(long j = 0; j < kmeans->OldCentroids.size(); j++)
			{
				if (j == 0)
				{
					centroid_id = j;
					min_distance = i->get_distance(kmeans->OldCentroids[j]);
				}
				else
				{
					double curr_distance = i->get_distance(kmeans->OldCentroids[j]);
					if (curr_distance < min_distance)
					{
						min_distance = curr_distance;
						centroid_id = j;
					}
				}
			}
			i->set_centroid(centroid_id);
		}
	}
private:
	KMeans *kmeans;
};



void KMeans::read_data(std::string filename)
{
	std::string line;
	std::ifstream input_file(filename.c_str());

	typedef boost::tokenizer<boost::char_separator<char> > tokenizer;
	boost::char_separator<char> sep(" ");
	if (input_file.is_open())
	{
		while (! input_file.eof() )
		{
			std::getline(input_file,line);
			tokenizer tokens(line, sep);
			DataPoint curr_point;
			bool empty_line = true;
			for(tokenizer::iterator i = tokens.begin();
				i != tokens.end();
				i++)
			{
				std::istringstream iss(*i);
				float curr_value;
				iss >> curr_value;
				curr_point.add_value(curr_value);
				empty_line = false;
			}
			if (! empty_line)    
				Data.push_back(curr_point);
		}
	}
}


void KMeans::write_centroids(std::string filename)
{
	std::ofstream centroid_file(filename.c_str());
	if (centroid_file.is_open())
	{
		for(tbb::concurrent_vector<Point>::iterator i = NewCentroids.begin();
			i != NewCentroids.end();
			i++)
		{
			i->print_point(centroid_file);
			centroid_file << "\n";
		}
	}
}

void KMeans::write_membership(std::string filename)
{
	std::ofstream membership_file(filename.c_str());
	if (membership_file.is_open())
	{
		for(tbb::concurrent_vector<DataPoint>::iterator i = Data.begin();
			i != Data.end();
			i++)
		{
			i->print_point(membership_file);
			membership_file << "\n";
		}
	}
}

void KMeans::compute(long input_k, long input_max_iterations)
{

	k = input_k;
	max_iterations = input_max_iterations;

	for (long i = 0; i < k; i++)
		points_in_cluster.push_back(0);

	std::cerr << "No. of datapoints: " << Data.size() << "\n";
	std::cerr << "No. of clusters: " << k << "\n";
	if (k < Data.size())
	{
		set_random_centroids();
		//    dump_data();
		//    dump_centroids();
		long iteration_count  = 0;
		std::cerr << "Iteration: ";
		while(true)
		{
			compute_membership();
			compute_centroids();
			//        dump_centroids();
			//        dump_data();
			if (check_convergence())
			{
				std::cerr << "\nConverged (before max iterations exceeded).\n";
				break;
			}
			
			iteration_count++;
			std::cerr << iteration_count << " ";
			if (iteration_count > max_iterations)
			{
				std::cerr << "\nDid not converge(before max iterations exceeded).\n";
				break;
			}

			update_old_clear_new();
		}
	}
	else
		std::cerr << "Number of clusters exceed the number of datapoints. Quiting...\n";
}


void KMeans::set_random_centroids()
{
	boost::minstd_rand rng;  
	boost::uniform_int<> six(1,Data.size());
	boost::variate_generator<boost::minstd_rand&, boost::uniform_int<> > die(rng, six);


	for(long i = 0; i < k; i++)
	{
		Point curr_point;
		int curr = die();
		//        std::cerr << curr << " ";
		curr_point.set_values(Data[curr]);
		OldCentroids.push_back(curr_point);
		curr_point.values_set_zero();
		NewCentroids.push_back(curr_point);
	}

}

void KMeans::compute_membership()
{

	tbb::parallel_for(tbb::blocked_range<tbb::concurrent_vector<DataPoint>::iterator>(Data.begin(), Data.end(), 1), apply_compute_membership(this), tbb::auto_partitioner());

	/*for(tbb::concurrent_vector<DataPoint>::iterator i = Data.begin();
		i != Data.end();
		i++)
	{
		double min_distance = -1;
		long centroid_id = -1;
		for(long j = 0; j < OldCentroids.size(); j++)
		{
			if (j == 0)
			{
				centroid_id = j;
				min_distance = i->get_distance(OldCentroids[j]);
			}
			else
			{
				double curr_distance = i->get_distance(OldCentroids[j]);
				if (curr_distance < min_distance)
				{
					min_distance = curr_distance;
					centroid_id = j;
				}
			}
		}
		i->set_centroid(centroid_id);
	}*/
}

void KMeans::compute_centroids()
{
	for (long i = 0; i < k; i++)
		points_in_cluster[i] = 0;

	tbb::parallel_for(tbb::blocked_range<long>(0, k, 1), apply_compute_centroids(this), tbb::auto_partitioner());

	/*for(long i = 0; i < k; ++i)
	{
		for(tbb::concurrent_vector<DataPoint>::iterator j = Data.begin();
			j != Data.end();
			j++)
		{
			if (j->get_centroid() == i)
			{
				(NewCentroids[j->get_centroid()]).values_addition(*j);
				points_in_cluster[j->get_centroid()]++;
			}
		}
	}*/

	long j = 0;
	for(tbb::concurrent_vector<Point>::iterator i = NewCentroids.begin();
		i != NewCentroids.end();
		i++, j++)
		i->values_division(points_in_cluster[j]);
}

//void KMeans::compute_centroids()
//{
//	std::vector<long> points_in_cluster;
//	for (long i = 0; i < k; i++)
//		points_in_cluster.push_back(0);
//
//	for(std::vector<DataPoint>::iterator i = Data.begin();
//		i != Data.end();
//		i++)
//	{
//		(NewCentroids[i->get_centroid()]).values_addition(*i);
//		points_in_cluster[i->get_centroid()]++;
//	}
//
//	long j = 0;
//	for(std::vector<Point>::iterator i = NewCentroids.begin();
//		i != NewCentroids.end();
//		i++, j++)
//		i->values_division(points_in_cluster[j]);
//}

void KMeans::dump_data()
{
	std::cerr << "-----Data-----\n";
	for (tbb::concurrent_vector<DataPoint>::iterator j = Data.begin();
		j != Data.end();
		j++)
	{
		std::cerr << "Data point: ";
		j->print_point(std::cerr);
		std::cerr << "\n";
	}
}

void KMeans::dump_centroids()
{
	std::cerr << "-----Centroids-----\n";
	tbb::concurrent_vector<Point>::iterator i = OldCentroids.begin();
	for (tbb::concurrent_vector<Point>::iterator j = NewCentroids.begin();
		j != NewCentroids.end();
		j++, i++)
	{
		std::cerr << "Old centroids: ";
		i->print_point(std::cerr);
		std::cerr << "\n";
		std::cerr << "New centroids: ";
		j->print_point(std::cerr);
		std::cerr << "\n";
	}
}

bool KMeans::check_convergence()
{
	tbb::concurrent_vector<Point>::iterator i = OldCentroids.begin();
	for (tbb::concurrent_vector<Point>::iterator j = NewCentroids.begin();
		j != NewCentroids.end();
		j++, i++)
		if (! i->values_equal(*j))
			return false;
	return true;
}

void KMeans::update_old_clear_new()
{
	tbb::concurrent_vector<Point>::iterator i = OldCentroids.begin();
	for (tbb::concurrent_vector<Point>::iterator j = NewCentroids.begin();
		j != NewCentroids.end();
		j++, i++)
	{
		*i = *j;
		j->values_set_zero();
	}
}

int main(int argc, char *argv[])
{
	tbb::task_scheduler_init init;
	KMeans my_kmeans;
	if (argc < 5)
	{
		std::cerr << "Usage: " << argv[0] << " [Data] [Number of Clusters] [Max Iterations] [Centroids] [Membership]\n";
		return 1;
	}
	std::cerr << "Reading Input...\n";
	std::cerr << "Input file: " << argv[1] << "\n";
	my_kmeans.read_data(argv[1]);

	std::cerr << "Computing...\n";
	my_kmeans.compute(std::atoi(argv[2]), std::atoi(argv[3]));

	std::cerr << "Writing results...\n";
	std::cerr << "Centroids file: " << argv[4] << "\n";
	my_kmeans.write_centroids(argv[4]);
	std::cerr << "Membership file: " << argv[5] << "\n";
	my_kmeans.write_membership(argv[5]);
	return 0;
}
