#include <iostream>
#include <map>
#include <set>
#include <fstream>
#include <string>

using std::map;
using std::multimap;
using std::set;
using std::pair;
using std::cerr;
using std::ifstream;
using std::ofstream;
using std::string;

class SparseMatrix {
public:
	float& get(int row, int column) {
		map<pair<int,int>, float>::iterator index;
		if (row >= column)
			index = data.find(pair<int, int>(row, column));
		else
			index = data.find(pair<int, int>(column, row));
		if(index != data.end())
			return index->second;
	}

void set(int row, int column, float given) { 
		if (row >= column)
			data.insert(pair<pair<int,int>, float>(pair<int,int>(row, column), given));
		else
			data.insert(pair<pair<int,int>, float>(pair<int,int>(column, row), given));
	}
private:
	map<pair<int, int>, float> data;
};

class Dendrogram {
public:
	Dendrogram() {node_count = 0;};
	void add_orphan(int node);
	void pair_orphans(int node1, int node2);
	float get_similarity(int node1, int node2, SparseMatrix &Basesimilaritys);
	void get_children(int node, set<int>& result);
	set<int>::iterator orphans_begin();
	set<int>::iterator orphans_end();
	int orphan_count();
	void print(string path);
private:
	multimap<int, int> children; //Parent->Child
	set<int> orphans;
	int node_count;
};

void Dendrogram::add_orphan(int node) {
	orphans.insert(node);
	if (node_count < node)
		node_count = node;
}

void Dendrogram::pair_orphans(int node1, int node2) {
	int parent = node_count++;
	children.insert(pair<int,int>(parent, node1));
	children.insert(pair<int,int>(parent, node2));
	orphans.erase(node1);
	orphans.erase(node2);
	orphans.insert(parent);
}

set<int>::iterator Dendrogram::orphans_begin() {
	return orphans.begin();
}

set<int>::iterator Dendrogram::orphans_end() {
	return orphans.end();
}

int Dendrogram::orphan_count() {
	return orphans.size();
}

void Dendrogram::get_children(int node, set<int>& result) {
	pair<multimap<int,int>::iterator, multimap<int,int>::iterator> range;
	range = children.equal_range(node);
	multimap<int, int>::iterator i = range.first;
	if (i == range.second)
		result.insert(node);
	else
		while(i != range.second) { 
			get_children(i->second, result);
			i++;
		}
}

float Dendrogram::get_similarity(int node1, int node2, SparseMatrix &Basesimilaritys) {
	set<int> node1_children;
	set<int> node2_children;
	get_children(node1, node1_children);
	get_children(node2, node2_children);
	float total_similarity = 0;
	for(set<int>::iterator i = node1_children.begin();
		i != node1_children.end();
		i++)
		for(set<int>::iterator j = node2_children.begin();
			j != node2_children.end();
			j++)
			if (*i != *j)
				total_similarity += Basesimilaritys.get(*i, *j);

	return total_similarity/(node1_children.size() * node2_children.size());
}

void Dendrogram::print(string path) {
	ofstream output_file(path.c_str());		
	if(output_file.is_open()) {
		for(multimap<int, int>::iterator i = children.begin();
			i != children.end();
			i++) 
			output_file << i->first << " " << i->second << "\n";	
		output_file.close();
	}
	else 
		cerr << "Error opening file " << path << "\n";
}

int main(int argc, char *argv[]) {

	cerr << "Dendrogram Computation\n";
	if (argc != 3) {
		cerr << "Usage: " << argv[0] << " [Input File] [Output File]\n";
		return 1;
	}
	SparseMatrix m;
	Dendrogram d;

	cerr << "Reading input file.\n";

	ifstream input_file(argv[1]);		
	if(input_file.is_open()) {
		while(! input_file.eof()) {
				int example1, example2;
				float similarity;
				input_file >> example1;
				input_file >> example2;
				input_file >> similarity;
				m.set(example1, example2, similarity);
				d.add_orphan(example1);
				d.add_orphan(example2);
			}
		input_file.close();
		}
	else {
		cerr << "Error opening file " << argv[1] << "\n";
		return 1;
	}

	cerr << "Read " << d.orphan_count() << "leaf nodes.\n";

	cerr << "Progress (parents added): ";
	int parent_count = 0;

	while(d.orphan_count() > 1) {
			float highest_similarity;
			int node1;
			int node2;
			bool first = true;
			for(set<int>::iterator i = d.orphans_begin();
				i != d.orphans_end();
				i++)
				for(set<int>::iterator j = i;
					j != d.orphans_end();
					j++)
					if (*i != *j) {
						if(first) {
							first = false;
							node1 = *i;
							node2 = *j;
							highest_similarity = d.get_similarity(node1, node2, m);
						}
						else {
							float curr_similarity = d.get_similarity(*i, *j, m);
							if (curr_similarity > highest_similarity) {
								node1 = *i;
								node2 = *j;
								highest_similarity = curr_similarity;
							}
						}
					}
					d.pair_orphans(node1, node2);
					cerr << ++parent_count << " ";

		}
		cerr << "\nWriting Results.\n";
		d.print(argv[2]);
		cerr << "Done.\n";
}
