#include <iostream>
#include<fstream>
#include <string>
#include <vector>
#include "lcs.h"
#include "walkdb.h"

using std::vector;
using std::cerr;
using std::string;
using std::ofstream;

int main(int argc, char *argv[])
{
	if (argc != 3) {
		cerr << "Usage " << argv[0] << " [Input File] [Output File]\n";
		return 0;
	}

	cerr << "Computing LCS\n";
	WalkDb::WalkDb db;
	db.read_walks(argv[1]);
	cerr << "Read " << db.get_walk_count() << " walks.\n";
	cerr << "Processing: ";
	ofstream output_file(argv[2]);
	for(int i = 0; i < db.get_walk_count(); i++) {
		for(int j = i; j < db.get_walk_count(); j++) {
			LCS::lcs< vector<int> > l;
			l.init(db.get_data(i),db.get_data(j));
			float lcs_length = l.compute(db.get_data(i),db.get_data(j));
			float i_length = db.get_length(i);
			float j_length = db.get_length(j);
			output_file << db.get_id(i) << " " << db.get_id(j) << " ";
			output_file << i_length << " " <<  j_length << " " << lcs_length << "\n";
			l.finalize();
		}
		cerr << i+1 << " ";
	}
	cerr << "\nDone.\n";
	output_file.close();
	return 0;
}