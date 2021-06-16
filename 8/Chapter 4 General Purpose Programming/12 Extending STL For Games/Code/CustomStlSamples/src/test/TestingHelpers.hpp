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
#ifndef GPG8_TESTING_HELPERS_HPP
#define GPG8_TESTING_HELPERS_HPP 1


//---------------------------------------------------------------------------
/// A basic 3D vector implementation.
//---------------------------------------------------------------------------
struct Vector3f {
    float x, y, z;

    Vector3f() {        
        x = 0.0f;
        y = 0.0f;
        z = 0.0f;
    }   

    Vector3f( float initX, float initY, float initZ ) {
        x = initX;
        y = initY;
        z = initZ;
    }

    bool operator==( const Vector3f& rhs ) {
        return x == rhs.x && y == rhs.y && z == rhs.z;
    }

    bool operator!=( const Vector3f& rhs ) {
        return !(*this == rhs);
    }
};


//---------------------------------------------------------------------------
/// A sample of a game entity class, which from the Octree's perspective is
/// just something with a location it can index.
//---------------------------------------------------------------------------
struct GameObject {
    Vector3f location;

    bool operator==( const GameObject& rhs ) const {
        return location.x == rhs.location.x 
                && location.y == rhs.location.y
                && location.z == rhs.location.z;
    }

    bool operator !=( const GameObject& rhs ) const {
        return !(*this == rhs);
    }
};

#endif
