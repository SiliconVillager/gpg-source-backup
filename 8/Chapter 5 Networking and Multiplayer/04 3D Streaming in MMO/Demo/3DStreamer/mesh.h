#ifndef _MESH_H
#define _MESH_H

#include <d3dx9.h>
#include <vector>

struct BBOX{
	BBOX()
	{
		max = D3DXVECTOR3(-10000.0f, -10000.0f, -10000.0f);
		min = D3DXVECTOR3(10000.0f, 10000.0f, 10000.0f);
	}

	BBOX(D3DXVECTOR3 _max, D3DXVECTOR3 _min)
	{
		max = _max;
		min = _min;
	}

	D3DXVECTOR3 max, min;
};

struct BSPHERE{
	BSPHERE()
	{
		center = D3DXVECTOR3(0.0f, 0.0f, 0.0f);
		radius = 0.0f;
	}

	BSPHERE(D3DXVECTOR3 _center, float _radius)
	{
		center = _center;
		radius = _radius;
	}

	D3DXVECTOR3 center;
	float radius;
};

class ObjectVertex
{
public:
	ObjectVertex(){}
	ObjectVertex(D3DXVECTOR3 pos, D3DXVECTOR3 norm, float u, float v)
	{
		m_pos = pos;
		m_norm = norm;
		m_u = u;
		m_v = v;
	}

	D3DXVECTOR3 m_pos, m_norm;
	float m_u, m_v;

	static const DWORD FVF;
};

class Mesh
{
	friend class Model;
	friend class ModelInstance;
	friend class RAY;
	public:

		Mesh();
		Mesh(char fName[], IDirect3DDevice9* Dev);
		~Mesh();
		HRESULT Load(char fName[], IDirect3DDevice9* Dev);
		void Render();
		void Release();
		const char* GetName() {return m_name;}

	private:

		char m_name[MAX_PATH];
		IDirect3DDevice9 *m_pDevice;
		ID3DXMesh *m_pMesh;
		std::vector<IDirect3DTexture9*> m_textures;
		std::vector<D3DMATERIAL9> m_materials;
		D3DMATERIAL9 m_white;
};

#endif