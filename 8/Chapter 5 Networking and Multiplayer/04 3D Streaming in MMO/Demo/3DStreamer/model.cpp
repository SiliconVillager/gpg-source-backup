#include "model.h"

std::vector<Mesh*> modelMeshes;

HRESULT LoadModel(IDirect3DDevice9* Device)
{
	Mesh *tree = new Mesh("Models/tree.x", Device);
	modelMeshes.push_back(tree);

	Mesh *stone = new Mesh("Models/stone.x", Device);
	modelMeshes.push_back(stone);

	return S_OK;
}

void UnloadModel()
{
	for(int i = 0; i < (int)modelMeshes.size(); i++)
		modelMeshes[i]->Release();
	modelMeshes.clear();
}

Model::Model(MODELTYPE modelType, INTPOINT mapPosition, D3DXVECTOR3 position, D3DXVECTOR3 rotation, D3DXVECTOR3 scale)
{
	init(modelType, mapPosition, position, rotation, scale);
}

void Model::init(MODELTYPE modelType, INTPOINT mapPosition, D3DXVECTOR3 position, D3DXVECTOR3 rotation, D3DXVECTOR3 scale)
{
   m_type = modelType;
   m_mappos = mapPosition;
   m_instance.SetPosition(position);
   m_instance.SetRotation(rotation);
   m_instance.SetScale(scale);
   m_instance.SetMesh(modelMeshes[m_type]);

   m_BBox = m_instance.GetBoundingBox();
}

void Model::Render()
{
	m_instance.Render();
}

ModelInstance::ModelInstance()
{
	m_pMesh = NULL;
	m_pos = m_rotate = D3DXVECTOR3(0.0f, 0.0f, 0.0f);
	m_scale = D3DXVECTOR3(1.0f, 1.0f, 1.0f);
}

ModelInstance::ModelInstance(Mesh *meshPtr)
{
	m_pMesh = meshPtr;
	m_pos = m_rotate = D3DXVECTOR3(0.0f, 0.0f, 0.0f);
	m_scale = D3DXVECTOR3(1.0f, 1.0f, 1.0f);
}

D3DXMATRIX ModelInstance::GetWorldMatrix()
{
	D3DXMATRIX p, r, s;
	D3DXMatrixTranslation(&p, m_pos.x, m_pos.y, m_pos.z);
	D3DXMatrixRotationYawPitchRoll(&r, m_rotate.y, m_rotate.x, m_rotate.z);
	D3DXMatrixScaling(&s, m_scale.x, m_scale.y, m_scale.z);

	D3DXMATRIX world = s * r * p;
	return world;
}

void ModelInstance::Render()
{
	if(m_pMesh != NULL)
	{
		m_pMesh->m_pDevice->SetTransform(D3DTS_WORLD, &GetWorldMatrix());
		m_pMesh->Render();
	}
}

BBOX ModelInstance::GetBoundingBox()
{
	if(m_pMesh == NULL || m_pMesh->m_pMesh == NULL)return BBOX();

	if(m_pMesh->m_pMesh->GetFVF() != ObjectVertex::FVF)		// XYZ and NORMAL and UV
		return BBOX();

	BBOX bBox(D3DXVECTOR3(-10000.0f, -10000.0f, -10000.0f),
		D3DXVECTOR3(10000.0f, 10000.0f, 10000.0f));
	D3DXMATRIX World = GetWorldMatrix();

	//Lock vertex buffer of the object
	ObjectVertex* vertexBuffer = NULL;
	m_pMesh->m_pMesh->LockVertexBuffer(0,(void**)&vertexBuffer);

	//For each vertex in the m_pMesh
	for(int i=0;i<(int)m_pMesh->m_pMesh->GetNumVertices();i++)
	{
		//Transform vertex to world space using the model instance's
		//world matrix, i.e. the position, rotation and scale
		D3DXVECTOR3 pos;
		D3DXVec3TransformCoord(&pos, &vertexBuffer[i].m_pos, &World);

		// Check if the vertex is outside the bounds
		// if so, then update the bounding volume
		if(pos.x < bBox.min.x)bBox.min.x = pos.x;
		if(pos.x > bBox.max.x)bBox.max.x = pos.x;
		if(pos.y < bBox.min.y)bBox.min.y = pos.y;
		if(pos.y > bBox.max.y)bBox.max.y = pos.y;
		if(pos.z < bBox.min.z)bBox.min.z = pos.z;
		if(pos.z > bBox.max.z)bBox.max.z = pos.z;
	}

	m_pMesh->m_pMesh->UnlockVertexBuffer();

	return bBox;
}