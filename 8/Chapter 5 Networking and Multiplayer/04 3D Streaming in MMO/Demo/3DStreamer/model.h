#ifndef _MODEL_H
#define _MODEL_H

#include <vector>
#include "log.h"
#include "mesh.h"
#include "intpoint.h"

HRESULT LoadModel(IDirect3DDevice9* Device);
void UnloadModel();

enum MODELTYPE {
 MODEL_TREE = 0,
 MODEL_STONE,
 MODEL_NONE
};

class ModelInstance{
	friend class Model;
	friend class RAY;
public:
	ModelInstance();
	ModelInstance(Mesh *meshPtr);
	void Render();

	void SetMesh(Mesh *m)			{m_pMesh = m;}
	void SetPosition(D3DXVECTOR3 p)	{m_pos = p;}
	void SetRotation(D3DXVECTOR3 r)	{m_rotate = r;}
	void SetScale(D3DXVECTOR3 s)	{m_scale = s;}

	D3DXMATRIX GetWorldMatrix();
	BBOX GetBoundingBox();

	Mesh *m_pMesh;
	D3DXVECTOR3 m_pos, m_rotate, m_scale;
};

class Model{
	friend class Terrain;
	public:
		Model(MODELTYPE modelType, INTPOINT mapPosition, D3DXVECTOR3 position, D3DXVECTOR3 rotation, D3DXVECTOR3 scale);
		void init(MODELTYPE modelType, INTPOINT mapPosition, D3DXVECTOR3 position, D3DXVECTOR3 rotation, D3DXVECTOR3 scale);
		void Render();

	private:
		INTPOINT m_mappos;
		ModelInstance m_instance;
		MODELTYPE m_type;
		BBOX m_BBox;
};

#endif