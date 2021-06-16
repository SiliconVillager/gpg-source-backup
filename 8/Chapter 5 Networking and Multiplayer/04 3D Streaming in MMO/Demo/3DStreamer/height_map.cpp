#include "height_map.h"
#include "terrain.h"
#include "log.h"

float Noise2D(int seed, int x, int y)
{
    int n = x + y * 57 + seed;
	n = (n << 13) ^ n;
	return ( 1.0f - ( (n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff) / 1073741824.0f);    
}

float CosInterpolate(float v1, float v2, float a)
{
	float angle = a * D3DX_PI;
	float prc = (1.0f - cos(angle)) * 0.5f;
	return  v1*(1.0f - prc) + v2*prc;
}

float InterploatedNoise(int seed, float x, float y)
{
	int x_int = int(x);
	float x_frac = x - x_int;

	int y_int = int(y);
	float y_frac = y - y_int;
	
	float v1 = Noise2D(seed, x_int, y_int);
	float v2 = Noise2D(seed, x_int + 1, y_int);
	float v3 = Noise2D(seed, x_int, y_int + 1);
	float v4 = Noise2D(seed, x_int + 1, y_int + 1);

	float i1 = CosInterpolate(v1 , v2 , x_frac);
	float i2 = CosInterpolate(v3 , v4 , x_frac);

	return CosInterpolate(i1 , i2 , y_frac);
}

float PerlinNoise2D(int seed, float x, float y, float persistance, int nOctaves)
{
	float total = 0.0f;
	for (int i = 0; i < nOctaves; i++)
	{
		float frequency = pow(2.0f, i);
		float amplitude = pow(persistance, i);
		total += InterploatedNoise(seed, x * frequency, y * frequency) * amplitude;
	}
	return total;
}

HeightMap::HeightMap(INTPOINT _size, float _maxHeight)
{
	m_size = _size;
	m_maxHeight = _maxHeight;
	m_pHeightMap = new float[m_size.x * m_size.y];
	memset(m_pHeightMap, 0, sizeof(float)*m_size.x*m_size.y);
}

HeightMap::~HeightMap()
{
	Release();
}

void HeightMap::operator*=(const HeightMap &rhs)
{
	for(int y = 0; y < m_size.y; y++)
		for(int x = 0; x < m_size.x; x++)
		{
			float a = m_pHeightMap[x + y * m_size.x] / m_maxHeight;
			float b = 1.0f;
			if(x <= rhs.m_size.x && y <= rhs.m_size.y)
				b = rhs.m_pHeightMap[x + y * m_size.x] / rhs.m_maxHeight;

			m_pHeightMap[x + y * m_size.x] = a * b * m_maxHeight;
		}
}

void HeightMap::Release()
{
	if(m_pHeightMap != NULL)
		delete [] m_pHeightMap;
	m_pHeightMap = NULL;
}

HRESULT HeightMap::CreateRandomHeightMap(Terrain* terrain, const char* msg, int seed, float noiseSize, float persistence, int octaves)
{
	//For each map node
	for (int y = 0; y < m_size.y; y++)
	{
		terrain->Progress(msg, y / (float)m_size.y);
		for (int x = 0; x < m_size.x; x++)
		{
			//Scale x & y to the range of 0.0 - noiseSize
 			float xf = ((float)x / (float)m_size.x) * noiseSize;
 			float yf = ((float)y / (float)m_size.y) * noiseSize;

			// Get perlin noise in (-1.0, 1.0) range
			float perlinNoise = PerlinNoise2D(seed, xf, yf, persistence, octaves);

			int b = (int)(128 + perlinNoise * 128.0f);
			if (b < 0) b = 0;
			if (b > 255) b = 255;

			m_pHeightMap[x + y * m_size.x] = (b / 255.0f) * m_maxHeight;
		}
	}
	return S_OK;
}

void HeightMap::Cap(float capHeight)
{
	m_maxHeight = 0.0f;

	for(int y = 0; y < m_size.y; y++)
		for(int x = 0; x < m_size.x; x++)
		{
			m_pHeightMap[x + y * m_size.x] -= capHeight;
			if(m_pHeightMap[x + y * m_size.x] < 0.0f)
				m_pHeightMap[x + y * m_size.x] = 0.0f;

			if(m_pHeightMap[x + y * m_size.x] > m_maxHeight)
				m_maxHeight = m_pHeightMap[x + y * m_size.x];
		}
}

float HeightMap::GetHeight(int x, int y)
{
	if(x < 0 || x > m_size.x || y < 0 || y > m_size.y)
		return 0.0f;
	else
		return m_pHeightMap[x + y * m_size.x];
}

float HeightMap::GetHeight(INTPOINT p)
{
	return GetHeight(p.x, p.y);
}