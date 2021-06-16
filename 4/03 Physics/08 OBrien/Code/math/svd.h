/*********************************************************************
 * svd.h
 * Authored by Kris Hauser 2002-2003
 *
 * Interfaces to singular value decomposition routines.
 *
 * Copyright 2003, Regents of the University of California 
 *
 *********************************************************************/

/**************************************************************************
 * Copyright (c) 2003, University of California at Berkeley
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 * 
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 * 
 *     * Neither the name of the University of California nor the names
 *       of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written
 *       permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *************************************************************************/



//computes the SVD of a = u*w*vt where u is returned in a
void svdecomp(matrix_type a, int m, int n, vector_type w, matrix_type v);

//x is size m, b is size n
//solves Ax = b where A = U.W.Vt are the matrices output by svdecomp
void svdbksub(const matrix_type u, const vector_type w, const matrix_type v, int m, int n,
			  vector_type x, const vector_type b);

//solves for the pseudo inverse of A, where A is size m by n
void svd_minv_solve(matrix_type A, matrix_type Ainv, int m, int n);

//solves for the pseudo least-squares inverse of Ax = b, where A is size m by n
void svd_ls_solve(matrix_type A, int m, int n, vector_type x, const vector_type b);

//solves for the k solutions of the pseudo least-squares inverse of Ax = b, where A is size m by n
void svd_ls_solve(matrix_type A, int m, int n, vector_type* x, const vector_type* b, int k);






//the following declares a function in Numerical Recipes in C
//DO NOT USE IT DIRECTLY!!!

//Given a matrix a[1..m][1..n], this routine computes its singular value decomposition, A =
//U.W.Vt. The matrix U[1..m][1..m] replaces a on output. The diagonal matrix of singular values W is output
//as a vector w[1..n]. The matrix V (not the transpose V T ) is output as v[1..n][1..n].
extern "C" void svdcmp(matrix_type a, int m, int n, vector_type w, matrix_type v);

