#include <iostream>
#include <vector>
#include <string>
#include <cmath>
#include <fstream>
#include <sstream>
#include <map>

#include <boost/tokenizer.hpp>

using std::vector;
using std::cerr;
using std::ifstream;
using std::ofstream;
using std::istringstream;
using std::string;
using std::map;
using std::pair;

class IdManager
{
public:
	IdManager() {counter = 0;}
	long get_id(long given) 
	{
		map<long,long>::iterator curr = mapping.find(given);
		if (curr == mapping.end())
		{
			long return_value = counter;
			mapping.insert(pair<long,long>(given, return_value));
			counter++;
			return return_value;
		}
		else
			return curr->second;
	}
	long get_max() {return counter;}
private:
	long counter;
	map<long,long> mapping;
};

class Ranges
{
public:
	void init_ranges(double given_start, double given_end, long given_ranges);
	long get_range(double given);
private:
	vector<double> start_range;
	vector<double> end_range;
};

void Ranges::init_ranges(double given_start, double given_end, long given_ranges)
{
	double increment = (given_end - given_start)/given_ranges;
	double curr_start = given_start;
	while(curr_start <= given_end)
	{
		start_range.push_back(curr_start);
		end_range.push_back(curr_start + increment);
		curr_start += increment;
	}
}

long Ranges::get_range(double given)
{
	long counter = 1;
	vector<double>::iterator curr_end = end_range.begin();
	for(vector<double>::iterator curr_start = start_range.begin();
		curr_start != start_range.end();
		curr_start++, curr_end++, counter++)
	{
		if((given >= *curr_start) && (given <= *curr_end))
			return counter;
	}
	return 0;
}

class Partition
{
public:
	Partition(double xstart, double xend, double xranges, double ystart, double yend, double yranges, double zstart, double zend, double zranges);
	long get_partition(double given_x, double given_y, double given_z);
private:
	double _xdigits;
	double _ydigits;
	double _zdigits;
	Ranges x;
	Ranges y;
	Ranges z;
};

Partition::Partition(double xstart, double xend, double xranges, double ystart, double yend, double yranges, double zstart, double zend, double zranges)
{
	_zdigits = floor(log10(zranges)) + 1;
	_ydigits = _zdigits + floor(log10(yranges)) + 1;
	_xdigits = _ydigits + floor(log10(xranges)) + 1;

	_xdigits = pow(10, _xdigits);
	_ydigits = pow(10, _ydigits);
	_zdigits = pow(10, _zdigits);

	x.init_ranges(xstart, xend, xranges);
	y.init_ranges(ystart, yend, zranges);
	z.init_ranges(zstart, yend, zranges);
}

long Partition::get_partition(double given_x, double given_y, double given_z)
{
	long xpartition = x.get_range(given_x);
	long ypartition = y.get_range(given_y);
	long zpartition = z.get_range(given_z);
	return  (xpartition * _ydigits) + (ypartition * _zdigits) + zpartition ;
}
int main(int argc, char *argv[])
{
	cerr << "Partition (Points into Clusters)\n";
	if (argc < 5)
	{
		std::cerr << "Usage: " << argv[0] << " [Data] [Start] [End] [Ranges] [Output]\n";
		return 1;
	}

	cerr << "Reading from: " << argv[1] << "\n";
	cerr << "Start: " << atoi(argv[2]) << "\n";
	cerr << "End: " << atoi(argv[3]) << "\n";
	cerr << "Ranges: " << atoi(argv[4]) << "\n";
	cerr << "Writing result to: " << argv[5] << "\n";

	Partition P(atoi(argv[2]), atoi(argv[3]), atoi(argv[4]),
				atoi(argv[2]), atoi(argv[3]), atoi(argv[4]),
				atoi(argv[2]), atoi(argv[3]), atoi(argv[4]));

	IdManager ids;

	ifstream input_file(argv[1]);
	ofstream output_file(argv[5]);
	cerr << "Progress: ";
	typedef boost::tokenizer<boost::char_separator<char> > tokenizer;
	boost::char_separator<char> sep(" ");
	long line_counter = 0;
	if (input_file.is_open())
	{
		while (! input_file.eof() )
		{
			float x_val,y_val,z_val;
			string line;
			std::getline(input_file,line);
			tokenizer tokens(line, sep);
			if(tokens.begin() != tokens.end())
			{
				tokenizer::iterator i = tokens.begin();
				{
					std::istringstream iss(*i);
					iss >> x_val;
					i++;
				}
				
				{
					std::istringstream iss(*i);
					iss >> y_val;
					i++;
				}
				
				{
					std::istringstream iss(*i);
					iss >> z_val;
				}
			
				long membership = P.get_partition(x_val, y_val, z_val);
				output_file << x_val << " " << y_val << " " << z_val << " " << ids.get_id(membership) << "\n";
				line_counter++;
				if (line_counter % 10000 == 0)
					cerr << line_counter << " ";
			}
		}

		input_file.close();
		output_file.close();
	}
	cerr << "\nTotal number of (non-empty) clusters: " << ids.get_max() << "\n";
	cerr << "Done.\n";
	return 0;
}