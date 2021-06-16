#ifndef _HEIGHTMAP_
#define _HEIGHTMAP_

#include <d3dx9.h>
#include "intpoint.h"

class Terrain;
class HeightMap
{
public:
	HeightMap(INTPOINT _size, float _maxHeight);
	~HeightMap();
	void Release();
	void operator*=(const HeightMap &rhs);

	HRESULT CreateRandomHeightMap(Terrain* terrain, const char* msg, int seed, float noiseSize, float persistence, int octaves);
	void Cap(float capHeight);
	float GetHeight(int x, int y);
	float GetHeight(INTPOINT p);

	INTPOINT m_size;
	float m_maxHeight;
	float *m_pHeightMap;
};

#endif