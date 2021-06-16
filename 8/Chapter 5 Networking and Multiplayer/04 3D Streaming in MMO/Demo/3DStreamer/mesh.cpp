#include "mesh.h"

const DWORD ObjectVertex::FVF = D3DFVF_XYZ | D3DFVF_NORMAL | D3DFVF_TEX1;

Mesh::Mesh() : m_pDevice(NULL), m_pMesh(NULL)
{}

Mesh::Mesh(char fName[], IDirect3DDevice9* Dev) : m_pDevice(Dev), m_pMesh(NULL)
{
	Load(fName, m_pDevice);
}

Mesh::~Mesh()
{
	Release();
}

HRESULT Mesh::Load(char fName[], IDirect3DDevice9* Dev)
{
	m_pDevice = Dev;

	strcpy_s(m_name, MAX_PATH, fName);

	m_white.Ambient = m_white.Specular = m_white.Diffuse  = D3DXCOLOR(1.0f, 1.0f, 1.0f, 1.0f);
	m_white.Emissive = D3DXCOLOR(0.0f, 0.0f, 0.0f, 1.0f);
	m_white.Power = 1.0f;

	Release();

	ID3DXBuffer * adjacencyBfr = NULL;
	ID3DXBuffer * materialBfr = NULL;
	DWORD noMaterials = NULL;

	if(FAILED(D3DXLoadMeshFromX(fName, D3DXMESH_MANAGED, m_pDevice,	&adjacencyBfr, &materialBfr, NULL, &noMaterials, &m_pMesh)))
		return E_FAIL;

	D3DXMATERIAL *mtrls = (D3DXMATERIAL*)materialBfr->GetBufferPointer();

	for(int i=0;i<(int)noMaterials;i++)
	{
		m_materials.push_back(mtrls[i].MatD3D);

		if(mtrls[i].pTextureFilename != NULL)
		{
			char textureFileName[MAX_PATH];
			strcpy_s(textureFileName, MAX_PATH, "Models/");
			strncat_s(textureFileName, mtrls[i].pTextureFilename, MAX_PATH);
			IDirect3DTexture9 * newTexture = NULL;
			D3DXCreateTextureFromFile(m_pDevice, textureFileName, &newTexture);			
			m_textures.push_back(newTexture);
		}
		else m_textures.push_back(NULL);
	}

	m_pMesh->OptimizeInplace(D3DXMESHOPT_ATTRSORT | D3DXMESHOPT_COMPACT | D3DXMESHOPT_VERTEXCACHE,
							(DWORD*)adjacencyBfr->GetBufferPointer(), NULL, NULL, NULL);

	adjacencyBfr->Release();
	materialBfr->Release();

	return S_OK;
}

void Mesh::Render()
{
	for(int i=0;i<(int)m_materials.size();i++)
	{	
		if(m_textures[i] != NULL) 
			m_pDevice->SetMaterial(&m_white);
		else 
			m_pDevice->SetMaterial(&m_materials[i]);
		m_pDevice->SetTexture(0,m_textures[i]);
		m_pMesh->DrawSubset(i);
	}	
}

void Mesh::Release()
{
	//Clear old mesh...
	if(m_pMesh != NULL)
	{
		m_pMesh->Release();
		m_pMesh = NULL;
	}

	//Clear textures and materials
	for(int i=0;i<(int)m_textures.size();i++)
		if(m_textures[i] != NULL)
			m_textures[i]->Release();

	m_textures.clear();
	m_materials.clear();	
}