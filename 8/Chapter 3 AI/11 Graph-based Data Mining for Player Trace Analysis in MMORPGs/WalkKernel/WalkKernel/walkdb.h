#ifndef WALKDB_H
#define	WALKDB_H

#include<vector>
#include<string>
#include<fstream>

namespace WalkDb {
    using std::vector;
    using std::string;
    using std::ifstream;

    class Walk {
    public:
        void add_vertex(int given) { data.push_back(given);}
        void erase() {data.erase(data.begin(), data.end());}
        const vector<int>& get_data() {return data;}
        int get_length() {return data.size();}
        void set_id(int given) {id = given;}
        int get_id() {return id;}
    private:
        vector<int> data;
        int id;
    };

    class WalkDb {
    public:
        void add_walk(Walk given) {data.push_back(given);}
        const vector<int>& get_data(int index) { return ((data[index]).get_data());}
        int get_length(int index) {return ((data[index]).get_length());}
        int get_id(int index) {return ((data[index]).get_id());}
        int get_walk_count() {return data.size();}
        void read_walks(string filename) {
            ifstream input_file(filename.c_str());
            bool first = true;
            Walk curr_walk;
            int prev_walk_id;
            if (input_file.is_open()) {
                while(! input_file.eof()) {
                    int walk_id;
                    int vertex;
                    input_file >> walk_id;
                    input_file >> vertex;
                    if (first) {
                        first = false;
                        prev_walk_id = walk_id;
                        curr_walk.set_id(walk_id);
                        curr_walk.add_vertex(vertex);
                    }
                    else {
                        if (prev_walk_id == walk_id)
                            curr_walk.add_vertex(vertex);
                        else {
                            data.push_back(curr_walk);
                            curr_walk.erase();
                            curr_walk.set_id(walk_id);
                            curr_walk.add_vertex(vertex);
                            prev_walk_id = walk_id;
                        }
                    }
                }
                 data.push_back(curr_walk);
            }
            input_file.close();
        }
    private:
        vector<Walk> data;
    };
}
#endif	// WALKDB_H

