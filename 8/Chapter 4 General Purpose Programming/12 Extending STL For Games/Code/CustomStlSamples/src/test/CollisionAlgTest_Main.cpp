//---------------------------------------------------------------------------
// Copyright (c) 2009, Neil Gower
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without 
// modification, are permitted provided that the following conditions are 
// met:
//
//   * Redistributions of source code must retain the above copyright 
//     notice, this list of conditions and the following disclaimer.
//
//   * Redistributions in binary form must reproduce the above copyright 
//     notice, this list of conditions and the following disclaimer in the 
//     documentation and/or other materials provided with the distribution.
//
//   * Neither the name of Vertex Blast nor the names of its contributors 
//     may be used to endorse or promote products derived from this software
//     without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
// TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
// PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
// OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
// PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
// LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
// NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//---------------------------------------------------------------------------
#include "CollisionAlgorithm.hpp"
#include "CollisionHelpers.hpp"
#include "Octree.hpp"
#include "test/TestingHelpers.hpp"
#include <vector>

#ifdef _MSC_VER
#include <crtdbg.h>
#endif 

//---------------------------------------------------------------------------
/// This is an example of a conforming, but very inefficient 
/// CandidatesFunctor. It blindly returns the entire container it is 
/// initialized with whenever asked for a range of possible colliders.
//---------------------------------------------------------------------------
template <typename StdContainer>
class StdContainerCandidatesFunctor {
public:
    StdContainer* m_pContainer;
    typedef typename StdContainer::iterator ResultsIterator;
    typedef std::pair<ResultsIterator, ResultsIterator> Results;

    /// Constructor
    StdContainerCandidatesFunctor( StdContainer& container )
        : m_pContainer( &container ) {
        assert( m_pContainer != NULL );
    }

    // call operator, naively return the whole container.
    Results operator() ( const typename StdContainer::value_type& t ) {
        assert( m_pContainer != NULL );
        (void)t;
        return Results( m_pContainer->begin(), m_pContainer->end() );
    }
};


//---------------------------------------------------------------------------
/// This is a sample CandidatesFunctor which uses the octree to efficiently
/// determine the set of objects that are approximately within the specified
/// range. 
///
/// It is an approximation because the range contains all objects found in 
/// *octants* that are within range. Filtering this list down to only objects
/// within the range would duplicate tests that are done later by the 
/// collision algorithm.
//---------------------------------------------------------------------------
template <typename Octree>
class OctreeCandidatesFunctor {
public:
    Octree* m_pOctree;
    float m_range;
    typedef typename Octree::local_iterator ResultsIterator;
    typedef std::pair<ResultsIterator, ResultsIterator> Results;

    /// Constructor takes an octree to use, and a radius to check around
    /// each collidee when searching for candidate colliders.
    OctreeCandidatesFunctor( Octree& octree, float range )
        : m_pOctree( &octree ), m_range( range ) {
        assert( m_pOctree != NULL );
    }

    /// Functor body, which delegates the search to the octree.
    Results operator() ( const typename Octree::value_type& t ) {
        assert( m_pOctree != NULL );
        return m_pOctree->find_range( t.location, m_range );           
    }
};


//---------------------------------------------------------------------------
/// Sample program that uses the generate_collisions generic algorithm, with
/// both a very bad candidates functor, and then a good one.
//---------------------------------------------------------------------------
int main( void ) {
#ifdef _MSC_VER
    _CrtSetDbgFlag( _CRTDBG_ALLOC_MEM_DF | _CRTDBG_LEAK_CHECK_DF );
#endif

    // set up some test data...
    GameObject zaboo, vork, codex;
    zaboo.location = Vector3f( 10, 1, 1 );
    vork.location  = Vector3f( -10, 1, 10 );
    codex.location = Vector3f( 10, 1, 2 );    
    typedef std::vector<GameObject> GameObjContainer; 
    GameObjContainer myGameObjects;
    myGameObjects.push_back(zaboo);
    myGameObjects.push_back(vork);
    myGameObjects.push_back(codex);

    // run the algorithm using a naive candidate functor...
    std::vector< std::pair<GameObject*,GameObject*> > results;
    assert( results.size() == 0 );
    generate_collisions(myGameObjects.begin(), myGameObjects.end(), 
                        back_inserter(results), 
                        StdContainerCandidatesFunctor<GameObjContainer>
                            (myGameObjects), 
                        SphereCollisionFunctor<GameObject>(5) );
    assert( results.size() == 1 );
    assert( (*results[0].first == zaboo && *results[0].second == codex)
            || (*results[0].first == codex && *results[0].second == zaboo) );
    
    // set up an octree...
    typedef spatialdb::Octree<GameObject, Vector3f, 2> Octree;
    Octree octree( Vector3f(0.0f, 0.0f, 0.0f), 256.0f );
    octree.insert( zaboo );
    octree.insert( vork );
    octree.insert( codex );

    // now try the same thing with the octree...    
    results.clear();
    generate_collisions( octree.begin(), octree.end(), 
                         back_inserter(results),                         
                         OctreeCandidatesFunctor<Octree>(octree,10),
                         SphereCollisionFunctor<GameObject>(5) );
    assert( results.size() == 1 );
    assert( (*results[0].first == zaboo && *results[0].second == codex)
            || (*results[0].first == codex && *results[0].second == zaboo) );
  
    return 0;
}
