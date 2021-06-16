/* Copyright (C) Greg Snook, 2000. 
 * All rights reserved worldwide.
 *
 * This software is provided "as is" without express or implied
 * warranties. You may freely copy and compile this source into
 * applications you distribute provided that the copyright text
 * below is included in the resulting source code, for example:
 * "Portions Copyright (C) Greg Snook, 2000"
 */
// map.cpp
// Geometry Data taken from navimesh2.lwo
 
// This file contains geometry information for the Navigation Map object used in the navimesh sample program
// The navimesh.cpp file links to this data externally to create the Navigation Map object.

#include "mtxlib.h" // definition of vector3
 
unsigned long map_totalpoints = 77;
 
vector3 map_points[] = 
{
 vector3(-41.4328f, 40.0000f, 24.0671f),
 vector3(-23.1315f, 40.0000f, 42.3684f),
 vector3(-23.1315f, 40.0000f, 92.3684f),
 vector3(-16.4328f, 40.0000f, 67.3684f),
 vector3(-66.4328f, 40.0000f, 17.3684f),
 vector3(-91.4328f, 40.0000f, 24.0671f),
 vector3(-109.7341f, 40.0000f, 42.3684f),
 vector3(-116.4328f, 40.0000f, 67.3684f),
 vector3(-109.7341f, 40.0000f, 92.3684f),
 vector3(-91.4328f, 40.0000f, 110.6697f),
 vector3(-66.4328f, 40.0000f, 117.3684f),
 vector3(-41.4328f, 40.0000f, 110.6697f),
 vector3(-40.4520f, 40.0000f, 82.3684f),
 vector3(-51.4328f, 40.0000f, 93.3492f),
 vector3(-66.4328f, 40.0000f, 97.3684f),
 vector3(-81.4328f, 40.0000f, 93.3492f),
 vector3(-92.4136f, 40.0000f, 82.3684f),
 vector3(-96.4328f, 40.0000f, 67.3684f),
 vector3(-92.4136f, 40.0000f, 52.3684f),
 vector3(-81.4328f, 40.0000f, 41.3876f),
 vector3(-66.4328f, 40.0000f, 37.3684f),
 vector3(-51.4328f, 40.0000f, 41.3876f),
 vector3(-40.4520f, 40.0000f, 52.3684f),
 vector3(-36.4328f, 40.0000f, 67.3684f),
 vector3(-56.5907f, 10.0000f, 67.1996f),
 vector3(-56.5907f, 10.0000f, 67.1996f),
 vector3(-81.4328f, 40.0000f, 93.3492f),
 vector3(-67.5715f, 10.0000f, 56.2188f),
 vector3(-67.5715f, 10.0000f, 56.2188f),
 vector3(-92.4136f, 40.0000f, 82.3684f),
 vector3(65.0024f, 5.7630f, -65.5516f),
 vector3(35.0030f, 5.7630f, -84.3647f),
 vector3(0.4275f, 5.7630f, -91.4832f),
 vector3(-33.1311f, 5.7630f, -83.8563f),
 vector3(-63.6390f, 5.7630f, -65.0431f),
 vector3(-83.9775f, 5.7630f, -36.0606f),
 vector3(-93.1299f, 5.7630f, -0.4682f),
 vector3(-88.0453f, 5.7630f, 20.3789f),
 vector3(-66.1813f, 5.7630f, 14.2773f),
 vector3(-52.4528f, 5.7630f, 18.3450f),
 vector3(-44.3174f, 5.7630f, 21.3958f),
 vector3(-60.0797f, 5.7630f, 41.7344f),
 vector3(-63.4004f, 5.7630f, 51.8567f),
 vector3(-52.4528f, 5.7630f, 62.7418f),
 vector3(-39.2327f, 5.7630f, 55.4629f),
 vector3(-19.4026f, 5.7630f, 43.2598f),
 vector3(-13.8095f, 5.7630f, 64.6152f),
 vector3(-19.4026f, 5.7630f, 89.0215f),
 vector3(0.4275f, 5.7630f, 92.0723f),
 vector3(36.0200f, 5.7630f, 84.4454f),
 vector3(65.5109f, 5.7630f, 66.6491f),
 vector3(85.8495f, 5.7630f, 34.6159f),
 vector3(93.4764f, 5.7630f, 0.5488f),
 vector3(86.3579f, 5.7630f, -35.0437f),
 vector3(86.3579f, 5.7630f, -35.0437f),
 vector3(63.9855f, 5.7630f, -21.8236f),
 vector3(41.1046f, 5.7630f, -18.7729f),
 vector3(63.4770f, 5.7630f, 3.0911f),
 vector3(49.2400f, 5.7630f, 25.9720f),
 vector3(27.3761f, 5.7630f, 36.1413f),
 vector3(0.4275f, 5.7630f, 35.6328f),
 vector3(-21.9449f, 5.7630f, 22.4127f),
 vector3(-33.6396f, 5.7630f, 3.0911f),
 vector3(-36.6904f, 5.7630f, -24.3660f),
 vector3(-23.4703f, 5.7630f, -45.2130f),
 vector3(0.4275f, 5.7630f, -59.9585f),
 vector3(26.3592f, 5.7630f, -59.9585f),
 vector3(19.2407f, 5.7630f, -37.5860f),
 vector3(6.5291f, 5.7630f, -38.0945f),
 vector3(-5.1656f, 5.7630f, -30.4675f),
 vector3(-11.7756f, 5.7630f, -18.2644f),
 vector3(-11.2672f, 5.7630f, -5.0443f),
 vector3(-4.1487f, 5.7630f, 7.6673f),
 vector3(6.0206f, 5.7630f, 14.2773f),
 vector3(20.2576f, 5.7630f, 14.7858f),
 vector3(32.4607f, 5.7630f, 7.6673f),
 vector3(39.0708f, 5.7630f, -3.0105f),
};
 
 
 
unsigned short map_polys[][3] = 
{
 { 67, 68, 69},
 { 67, 69, 70},
 { 67, 70, 71},
 { 67, 71, 72},
 { 67, 72, 73},
 { 67, 73, 74},
 { 67, 74, 75},
 { 67, 75, 76},
 { 67, 76, 56},
 { 60, 61, 45},
 { 60, 45, 46},
 { 36, 37, 38},
 { 35, 36, 38},
 { 46, 47, 48},
 { 46, 48, 49},
 { 60, 46, 49},
 { 42, 43, 44},
 { 60, 49, 50},
 { 41, 42, 44},
 { 40, 41, 44},
 { 40, 44, 45},
 { 40, 45, 61},
 { 40, 61, 62},
 { 39, 40, 62},
 { 39, 62, 63},
 { 34, 35, 38},
 { 38, 39, 63},
 { 34, 38, 63},
 { 33, 34, 63},
 { 32, 33, 63},
 { 66, 67, 56},
 { 66, 56, 55},
 { 59, 60, 50},
 { 59, 50, 51},
 { 58, 59, 51},
 { 57, 58, 51},
 { 57, 51, 52},
 { 55, 57, 52},
 { 55, 52, 53},
 { 66, 55, 53},
 { 54, 30, 31},
 { 54, 31, 32},
 { 32, 63, 64},
 { 32, 64, 65},
 { 32, 65, 66},
 { 66, 53, 54},
 { 54, 32, 66},
 { 28, 24, 43},
 { 28, 43, 42},
 { 11, 2, 12},
 { 12, 2, 3},
 { 12, 3, 1},
 { 10, 11, 12},
 { 1, 0, 4},
 { 1, 4, 5},
 { 6, 7, 8},
 { 6, 8, 9},
 { 10, 12, 13},
 { 9, 10, 13},
 { 23, 12, 1},
 { 22, 23, 1},
 { 21, 22, 1},
 { 21, 1, 5},
 { 20, 21, 5},
 { 20, 5, 6},
 { 19, 20, 6},
 { 18, 19, 6},
 { 17, 18, 6},
 { 17, 6, 9},
 { 16, 17, 9},
 { 15, 16, 9},
 { 9, 13, 14},
 { 9, 14, 15},
 { 25, 27, 29},
 { 25, 29, 26},
};
 
 
unsigned long map_totalpolys = 75;