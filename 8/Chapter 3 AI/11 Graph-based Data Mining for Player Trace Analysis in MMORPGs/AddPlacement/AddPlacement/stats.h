#ifndef STATS_H
#define STATS_H

#include <cmath>
#include <vector>

using std::vector;

#define MAX_RANDOM_KEYS 5
unsigned int random_key[] =  {12345, 56789, 13579, 24689, 12987};

template<typename T>
void mean_and_dev(vector<T> &given, double &mean, double &dev)
{
	mean = 0;
	for(typename vector<T>::iterator i = given.begin();
		i != given.end();
		i++)
		mean += *i;
	mean /= given.size();

	dev = 0;
	for(typename vector<T>::iterator i = given.begin();
		i != given.end();
		i++)
		dev += ((*i - mean) * (*i - mean));
	dev /= (given.size() - 1);
	dev = sqrt(dev);
}

#endif //STATS_H