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
#ifndef GPG8_COLLISION_HELPERS_H_ 
#define GPG8_COLLISION_HELPERS_H_ 1


//---------------------------------------------------------------------------
/// A functor for testing whether fixed-sized bounding spheres around two
/// objects touch. The sphere radius is specified on creation of the 
/// functor, and used for all subsequent tests.
//---------------------------------------------------------------------------
template <typename T>
class SphereCollisionFunctor {
public:
    float m_collideDistSqr;

    /// Constructor, requires a radius for the tests.
    SphereCollisionFunctor( float radius ) 
        : m_collideDistSqr( (radius + radius) * (radius + radius) ) {}

    //-----------------------------------------------------------------------
    /// If the bounding spheres around t1 and t2 touch, returns true. 
    /// Otherwise, returns false. Assumes both bounding spheres are of 
    /// the same radius (specified when the functor is instantiated).
    //-----------------------------------------------------------------------
    bool operator() ( const T& t1, const T& t2 ) {
        return m_collideDistSqr >=
            (t1.location.x - t2.location.x) * (t1.location.x - t2.location.x) 
                + (t1.location.y - t2.location.y) 
                    * (t1.location.y - t2.location.y)
                + (t1.location.z - t2.location.z) 
                    * (t1.location.z - t2.location.z);
    }
};

#endif 
