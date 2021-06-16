//        File: terrain.cpp
// Description: Terrain streaming
// 
// Copyright (C) 2009 Kevin Kaichuan He.  All rights reserved. 
//
#include <list>
#include "terrain.h"
#include "camera.h"
#include "map_gen.h"
#include "file_object.h"
#include "disk_object.h"
#include "http_object.h"

const DWORD TerrainVertex::FVF = D3DFVF_XYZ | D3DFVF_NORMAL | D3DFVF_TEX2;

TerrainPatch::TerrainPatch(int x, int y) : m_x(x), m_y(y), m_pDevice(NULL), m_pMesh(NULL), m_fileObject(NULL), m_loaded(FALSE)
{}

TerrainPatch::~TerrainPatch()
{
	Release();
}

void TerrainPatch::Release()
{
	if (m_pMesh != NULL)
		m_pMesh->Release();
	m_pMesh = NULL;
}

HRESULT TerrainPatch::CreateMesh(Terrain &ter, IDirect3DDevice9* Dev)
{
	if (m_pMesh != NULL)
	{
		m_pMesh->Release();
		m_pMesh = NULL;
	}
	
	m_pDevice = Dev;
	
	int nrVert = (TILES_PER_PATCH_X + 1) * (TILES_PER_PATCH_Y + 1);
	int nrTri = TILES_PER_PATCH_X * TILES_PER_PATCH_Y * 2;

	if (FAILED(D3DXCreateMeshFVF(nrTri, nrVert, D3DXMESH_MANAGED, TerrainVertex::FVF, m_pDevice, &m_pMesh)))
	{
		s_log.Print("Couldn't create mesh for PATCH");
		return E_FAIL;
	}

	m_BBox.max = D3DXVECTOR3(-10000.0f, -10000.0f, -10000.0f);
	m_BBox.min = D3DXVECTOR3(10000.0f, 10000.0f, 10000.0f);

	//Create vertices
	TerrainVertex* ver = 0;
	m_pMesh->LockVertexBuffer(0,(void**)&ver);
	for(int y = m_y * TILES_PER_PATCH_Y, y0 = 0; y <= (m_y + 1) * TILES_PER_PATCH_Y; y++, y0++)
		for(int x = m_x * TILES_PER_PATCH_X, x0 = 0; x <= (m_x + 1) * TILES_PER_PATCH_X; x++, x0++)
		{
			TerrainTile *tile = ter.GetTile(x, y);
			D3DXVECTOR3 pos = D3DXVECTOR3((float)x, tile->m_height, (float)-y);
			D3DXVECTOR2 alphaUV = D3DXVECTOR2(x / (float)ter.m_size.x, y / (float)ter.m_size.y); //Alpha UV : [0, 1)
			D3DXVECTOR2 colorUV = alphaUV * TILES_PER_TERRAIN_X / 10.0f;   //Color UV : Repeat once every 10 tiles 
			ver[y0 * (TILES_PER_PATCH_X + 1) + x0] = TerrainVertex(pos, ter.GetNormal(x, y), alphaUV, colorUV);

			//Calculate bounding box bounds...
			if (pos.x < m_BBox.min.x) m_BBox.min.x = pos.x;
			if (pos.x > m_BBox.max.x) m_BBox.max.x = pos.x;
			if (pos.y < m_BBox.min.y) m_BBox.min.y = pos.y;
			if (pos.y > m_BBox.max.y) m_BBox.max.y = pos.y;
			if (pos.z < m_BBox.min.z) m_BBox.min.z = pos.z;
			if (pos.z > m_BBox.max.z) m_BBox.max.z = pos.z;
		}
	m_pMesh->UnlockVertexBuffer();
	//Calculate Indices
	WORD* ind = 0;
	m_pMesh->LockIndexBuffer(0,(void**)&ind);	
	int index = 0;

	for(int y = m_y * TILES_PER_PATCH_Y, y0 = 0; y < (m_y + 1) * TILES_PER_PATCH_Y; y++, y0++)
		for(int x = m_x * TILES_PER_PATCH_X, x0 = 0; x < (m_x + 1) * TILES_PER_PATCH_X; x++, x0++)
		{
			//Triangle 1
			ind[index++] =   y0   * (TILES_PER_PATCH_X + 1) + x0;
			ind[index++] =   y0   * (TILES_PER_PATCH_X + 1) + x0 + 1;
			ind[index++] = (y0+1) * (TILES_PER_PATCH_X + 1) + x0;		
			//Triangle 2
			ind[index++] = (y0+1) * (TILES_PER_PATCH_X + 1) + x0;
			ind[index++] =   y0   * (TILES_PER_PATCH_X + 1) + x0 + 1;
			ind[index++] = (y0+1) * (TILES_PER_PATCH_X + 1) + x0 +1;
		}

	m_pMesh->UnlockIndexBuffer();

	//Set Attributes
	DWORD *att = 0, a = 0;
	m_pMesh->LockAttributeBuffer(0,&att);
	memset(att, 0, sizeof(DWORD)*nrTri);
	m_pMesh->UnlockAttributeBuffer();
	
	return S_OK;
}


void TerrainPatch::Render()
{
	//Draw mesh
	if(m_pMesh != NULL)
		m_pMesh->DrawSubset(0);
}

HRESULT TerrainPatch::InitMesh(Terrain &terrain, IDirect3DDevice9* Dev)
{
	ASSERT(m_pMesh == NULL);

	m_pDevice = Dev;

	if (!terrain.m_meshPool.empty()) 
	{
		m_pMesh = terrain.m_meshPool[terrain.m_meshPool.size() - 1];
		terrain.m_meshPool.pop_back();
	} else
	{
		int nrVert = (TILES_PER_PATCH_X + 1) * (TILES_PER_PATCH_Y + 1);
		int nrTri = TILES_PER_PATCH_X * TILES_PER_PATCH_Y * 2;

		if (FAILED(D3DXCreateMeshFVF(nrTri, nrVert, D3DXMESH_MANAGED, TerrainVertex::FVF, m_pDevice, &m_pMesh)))
		{
			s_log.Print("Couldn't create mesh for PATCH");
			return E_FAIL;
		}
	
		// Vertex array will be loaded from files later

		//Calculate Indices
		WORD* ind = 0;
		m_pMesh->LockIndexBuffer(0,(void**)&ind);	
		int index = 0;
		for(int y = m_y * TILES_PER_PATCH_Y, y0 = 0; y < (m_y + 1)* TILES_PER_PATCH_Y; y++, y0++)
		for(int x = m_x * TILES_PER_PATCH_X, x0 = 0; x < (m_x + 1) * TILES_PER_PATCH_X; x++, x0++)
		{
			//Triangle 1
			ind[index++] =   y0   * (TILES_PER_PATCH_X + 1) + x0;
			ind[index++] =   y0   * (TILES_PER_PATCH_X + 1) + x0 + 1;
			ind[index++] = (y0+1) * (TILES_PER_PATCH_X + 1) + x0;		

			//Triangle 2
			ind[index++] = (y0+1) * (TILES_PER_PATCH_X + 1) + x0;
			ind[index++] =   y0   * (TILES_PER_PATCH_X + 1) + x0 + 1;
			ind[index++] = (y0+1) * (TILES_PER_PATCH_X + 1) + x0 + 1;
		}
		m_pMesh->UnlockIndexBuffer();

		//Set Attributes
		DWORD *att = 0, a = 0;
		m_pMesh->LockAttributeBuffer(0,&att);
		memset(att, 0, sizeof(DWORD)*nrTri);
		m_pMesh->UnlockAttributeBuffer();
	}
	return S_OK;
}

void TerrainPatch::Unload(Terrain& terrain)
{
	if (!m_loaded) return;
	// Unload the mesh
	ASSERT(m_pMesh);
	terrain.m_meshPool.push_back(m_pMesh);
 	m_pMesh = NULL;
	m_loaded = false;
	terrain.m_changed = true;

	// Unload terrain objects
	for(int y = m_y * TILES_PER_PATCH_Y, y0 = 0; y <= (m_y + 1) * TILES_PER_PATCH_Y; y++, y0++)
		for(int x = m_x * TILES_PER_PATCH_X, x0 = 0; x <= (m_x + 1) * TILES_PER_PATCH_X; x++, x0++)
		{
			TerrainTile* tile = terrain.GetTile(x, y);
			if (!tile) continue;
			if (tile->m_type != -1 && tile->m_pObject != terrain.m_models.end()) 
			{
				terrain.m_objectPool.push_back(*tile->m_pObject);
				terrain.m_models.erase(tile->m_pObject);
				tile->m_pObject = terrain.m_models.end();
			}
			tile->m_type = -1;  // indicate that the tile is unloaded
		}
}

Terrain::Terrain() : m_pDevice(NULL), m_pTiles(NULL), m_pLandScape(NULL), m_queueMgr(NULL), m_changed(true)
{}

// Allocate memory for terrain data without filling it
void Terrain::InitTerrainData()
{
	//Tile already initialized
	
	//Nullify height map because it is not needed for rendering streamed terrain because it's embedded in vertex info already
	if (m_pHeightMap != NULL)
	{
		m_pHeightMap->Release();
		delete m_pHeightMap;
		m_pHeightMap = NULL;
	}

	//Initialize patches
	for(int i=0;i<(int)m_patches.size();i++)
		if(m_patches[i] != NULL)
			m_patches[i]->Release();
	m_patches.clear();

	//Create new patches
	for(int y = 0; y < m_numPatches.y; y++)
	{
		for(int x = 0; x < m_numPatches.x; x++)
		{
			TerrainPatch *p = new TerrainPatch(x, y);
			m_patches.push_back(p);
		}
	}
	m_models.clear();

	//Clear old alpha maps
	if (m_pAlphaMap != NULL)
		m_pAlphaMap->Release();
	//Create new alpha map
	D3DXCreateTexture(m_pDevice, m_size.x, m_size.y, 1, D3DUSAGE_DYNAMIC, D3DFMT_A8R8G8B8, D3DPOOL_DEFAULT, &m_pAlphaMap);
	D3DLOCKED_RECT alphaRect;
	m_pAlphaMap->LockRect(0, &alphaRect, NULL, NULL);
	BYTE *alphaBits = (BYTE*)alphaRect.pBits;
	memset(alphaBits, 0, m_size.y * alphaRect.Pitch);		//Clear texture to black
	m_pAlphaMap->UnlockRect(0);
	
	//Clear old shadow maps
	if (m_pShadowMap != NULL)
		m_pShadowMap->Release();
	//Create new shadow map
	D3DXCreateTexture(m_pDevice, m_size.x, m_size.y, 1, D3DUSAGE_DYNAMIC, D3DFMT_L8, D3DPOOL_DEFAULT, &m_pShadowMap);
	D3DLOCKED_RECT shadowRect;
	m_pShadowMap->LockRect(0, &shadowRect, NULL, NULL);
	BYTE *shadowBits = (BYTE*)shadowRect.pBits;	
	memset(shadowBits, 255, m_size.y * shadowRect.Pitch);		//Clear texture to white
	m_pShadowMap->UnlockRect(0);
}

void Terrain::Init(IDirect3DDevice9* Dev, INTPOINT size, INTPOINT numPatches, bool mapGen, 
				   FileObject::DataSource dataSource, 
				   const char* dataPath, FileQueueManager* queueMgr)
{
	m_queueMgr = queueMgr;
	m_pDevice = Dev;
	m_size = size;
	m_numPatches = numPatches; 
	m_pHeightMap = NULL;
	m_dataSource = dataSource;
	m_dataPath = dataPath;

	if (m_pTiles != NULL)
		delete [] m_pTiles;

	m_pTiles = new TerrainTile[m_size.x * m_size.y];

	for (int i =0 ; i <( int)m_textures.size(); i++)
		m_textures[i]->Release();
	m_textures.clear();

	//Load textures
	IDirect3DTexture9* dirt = NULL, *grass = NULL, *stone = NULL;
	if (FAILED(D3DXCreateTextureFromFile(Dev, "textures/dirt.dds", &dirt))) 
		s_log.Print("Could not load grass.dds");
	if (FAILED(D3DXCreateTextureFromFile(Dev, "textures/grass.dds", &grass))) 
		s_log.Print("Could not load stone.jpg");
	if (FAILED(D3DXCreateTextureFromFile(Dev, "textures/stone.dds", &stone))) 
		s_log.Print("Could not load snow.dds");

	m_textures.push_back(dirt);
	m_textures.push_back(grass);
	m_textures.push_back(stone);
	m_pAlphaMap = NULL;
	m_pShadowMap = NULL;

	//Terrain Texture
	if(FAILED(m_pDevice->CreateTexture(256, 256, 1, D3DUSAGE_RENDERTARGET, D3DFMT_A8R8G8B8, D3DPOOL_DEFAULT, &m_pLandScape, NULL)))
		s_log.Print("Failed to create texture: m_pLandScape");

	// Init font
	D3DXCreateFont(m_pDevice, 40, 0, 0, 1, false,  
				   DEFAULT_CHARSET, OUT_DEFAULT_PRECIS, DEFAULT_QUALITY,
				   DEFAULT_PITCH | FF_DONTCARE, "Arial Black", &m_pProgressFont);

	//Load pixel & vertex shaders
	m_dirToSun = D3DXVECTOR3(1.0f, 0.6f, 0.5f);
	D3DXVec3Normalize(&m_dirToSun, &m_dirToSun);

	m_terrainPS.Init(Dev, "shaders/terrain.ps", PIXEL_SHADER);
	m_terrainVS.Init(Dev, "shaders/terrain.vs", VERTEX_SHADER);
	m_vsMatrixWorld = m_terrainVS.GetConstant("matrixWorld");
	m_vsMatrixViewProjection = m_terrainVS.GetConstant("matrixViewProjection");
	m_vsDirToSun = m_terrainVS.GetConstant("DirToSun");

	m_modelPS.Init(Dev, "shaders/model.ps", PIXEL_SHADER);
	m_modelVS.Init(Dev, "shaders/model.vs", VERTEX_SHADER);
	m_modelMatrixWorld = m_modelVS.GetConstant("matrixWorld");
	m_modelMatrixViewProjection = m_modelVS.GetConstant("matrixViewProjection");
	m_modelDirToSun = m_modelVS.GetConstant("DirToSun");
	m_modelMapSize = m_modelVS.GetConstant("mapSize");

	//Create white material	
	m_material.Ambient = m_material.Specular = m_material.Diffuse  = D3DXCOLOR(0.5f, 0.5f, 0.5f, 1.0f);
	m_material.Emissive = D3DXCOLOR(0.0f, 0.0f, 0.0f, 1.0f);

	if (mapGen)
	{
		GenerateRandomTerrain(this);
		SaveTerrainPatches("Terrain");
	} else
	{
		InitTerrainData();
		LoadTerrainPatchBBs();
	}
}

void Terrain::Release()
{
	for (int i = 0; i < (int)m_patches.size(); i++)
		if (m_patches[i] != NULL)
			m_patches[i]->Release();

	m_patches.clear();

	if (m_pHeightMap != NULL)
	{
		m_pHeightMap->Release();
		delete m_pHeightMap;
		m_pHeightMap = NULL;
	}

	m_models.clear();
}

D3DXVECTOR3 Terrain::GetNormal(int x, int y)
{
	//Neighboring map nodes (D, B, C, F, H, G)
	INTPOINT mp[] = {INTPOINT(x-1, y),   INTPOINT(x, y-1), 
					 INTPOINT(x+1, y-1), INTPOINT(x+1, y),
				  	 INTPOINT(x, y+1),   INTPOINT(x-1, y+1)};

	//if there's an invalid map node return (0, 1, 0)
	if(!Within(mp[0]) || !Within(mp[1]) || !Within(mp[2]) || 
	   !Within(mp[3]) || !Within(mp[4]) || !Within(mp[5]))
		return D3DXVECTOR3(0.0f, 1.0f, 0.0f);

	//Calculate the normals of the 6 neighboring planes
	D3DXVECTOR3 normal = D3DXVECTOR3(0.0f, 0.0f, 0.0f);

	for(int i=0;i<6;i++)
	{
		D3DXPLANE plane;
		D3DXPlaneFromPoints(&plane, 
							&GetWorldPos(INTPOINT(x, y)),
							&GetWorldPos(mp[i]), 
							&GetWorldPos(mp[(i + 1) % 6]));

		normal +=  D3DXVECTOR3(plane.a, plane.b, plane.c);
	}

	D3DXVec3Normalize(&normal, &normal);
	return normal;
}

void Terrain::AddObject(MODELTYPE type, INTPOINT mappos)
{
	D3DXVECTOR3 pos = D3DXVECTOR3((float)mappos.x, m_pHeightMap->GetHeight(mappos), (float)-mappos.y);	
	D3DXVECTOR3 rot = D3DXVECTOR3((rand()%1000 / 1000.0f) * 0.13f, (rand()%1000 / 1000.0f) * 3.0f, (rand()%1000 / 1000.0f) * 0.13f);

	float sca_xz = (rand()%1000 / 1000.0f) * 0.6f + 0.5f;
	float sca_y = (rand()%1000 / 1000.0f) * 1.0f + 0.5f;
	D3DXVECTOR3 sca = D3DXVECTOR3(sca_xz, sca_y, sca_xz);

	Model* object = new Model(type, mappos, pos, rot, sca);
	m_models.push_back(object);

	TerrainTile *tile = GetTile(mappos.x, mappos.y);
	if (tile)
	{
		tile->m_pObject = m_models.end();
		--(tile->m_pObject);
	}
}

void Terrain::Render(Camera &camera)
{
	//Set render states		
	m_pDevice->SetRenderState(D3DRS_LIGHTING, false);
	m_pDevice->SetRenderState(D3DRS_ZWRITEENABLE, true);	
	
	m_pDevice->SetTexture(0, m_pAlphaMap);
	m_pDevice->SetTexture(1, m_textures[0]);		//Dirt
	m_pDevice->SetTexture(2, m_textures[1]);		//Grass
	m_pDevice->SetTexture(3, m_textures[2]);		//Stone
	m_pDevice->SetTexture(4, m_pShadowMap);			//Shadow Map
	m_pDevice->SetMaterial(&m_material);

	D3DXMATRIX world, vp = camera.GetViewMatrix() * camera.GetProjectionMatrix();
	D3DXMatrixIdentity(&world);
	m_pDevice->SetTransform(D3DTS_WORLD, &world);
	
	//Set vertex shader variables
	m_terrainVS.SetMatrix(m_vsMatrixWorld, world);
	m_terrainVS.SetMatrix(m_vsMatrixViewProjection, vp);
	m_terrainVS.SetVector3(m_vsDirToSun, m_dirToSun);

	m_terrainVS.Begin();
	m_terrainPS.Begin();
		
	for (int y = 0; y < m_numPatches.y; y++)
		for (int x = 0; x < m_numPatches.x; x++)
		{
			TerrainPatch* p = m_patches[y * m_numPatches.x + x];

			if((p->m_loaded || (p->m_fileObject && p->m_fileObject->m_loaded)) &&
				!camera.Cull(p->m_BBox))
			{
				if (p->m_loaded || PuntPatchToGPU(x, y))
					p->Render();
			}
		}
	m_terrainPS.End();
	m_terrainVS.End();

	m_pDevice->SetTexture(1, NULL);
	m_pDevice->SetTexture(2, NULL);
	m_pDevice->SetTexture(3, NULL);
	m_pDevice->SetTexture(4, NULL);

	//Render Objects
	m_modelVS.SetMatrix(m_modelMatrixWorld, world);
	m_modelVS.SetMatrix(m_modelMatrixViewProjection, vp);
	m_modelVS.SetVector3(m_modelDirToSun, m_dirToSun);
	m_modelVS.SetVector3(m_modelMapSize, D3DXVECTOR3((float)m_size.x, (float)m_size.y, 0.0f));

	m_pDevice->SetTexture(1, m_pShadowMap);		// Shadow Map
	
	m_modelVS.Begin();
	m_modelPS.Begin();

	for (std::list<Model*>::iterator it = m_models.begin(); it != m_models.end(); ++it)
		if(!camera.Cull((*it)->m_BBox))
		{
			D3DXMATRIX m = (*it)->m_instance.GetWorldMatrix();
			m_modelVS.SetMatrix(m_modelMatrixWorld, m);
			(*it)->Render();
		}

	m_modelVS.End();
	m_modelPS.End();
}

void Terrain::Progress(std::string text, float prc)
{
	m_pDevice->Clear(0, 0, D3DCLEAR_TARGET | D3DCLEAR_ZBUFFER, 0xffffffff, 1.0f, 0L);
	m_pDevice->BeginScene();
	
	RECT rc = {200, 250, 600, 300};
	m_pProgressFont->DrawText(NULL, text.c_str(), -1, &rc, DT_CENTER | DT_VCENTER | DT_NOCLIP, 0xff000000);
	
	//Progress bar
	D3DRECT r;
	r.x1 = 200; r.x2 = 600;
	r.y1 = 300; r.y2 = 340;
	m_pDevice->Clear(1, &r, D3DCLEAR_TARGET | D3DCLEAR_ZBUFFER, 0xff000000, 1.0f, 0L);
	r.x1 = 202; r.x2 = 598;
	r.y1 = 302; r.y2 = 338;
	m_pDevice->Clear(1, &r, D3DCLEAR_TARGET | D3DCLEAR_ZBUFFER, 0xffffffff, 1.0f, 0L);
	r.x1 = 202; r.x2 = (int)(202 + 396 * prc);
	r.y1 = 302; r.y2 = 338;
	m_pDevice->Clear(1, &r, D3DCLEAR_TARGET | D3DCLEAR_ZBUFFER, 0xff00ff00, 1.0f, 0L);

	m_pDevice->EndScene();
	m_pDevice->Present(0, 0, 0, 0);
}

bool Terrain::Within(INTPOINT p)
{
	return p.x >= 0 && p.y >= 0 && p.x < m_size.x && p.y < m_size.y;
}

TerrainTile* Terrain::GetTile(int x, int y)
{
	if(m_pTiles == NULL)
		return NULL;
	if (x < 0 || x >= m_size.x || y < 0 || y >= m_size.y)
		return NULL;
	return &m_pTiles[x + y * m_size.x];
}

bool Terrain::SaveTerrainPatches(char fileName[])
{
	char name[MAX_PATH];

	sprintf_s(name, MAX_PATH, "%s_BB.dat", fileName);
	std::ofstream outBB(name, std::ios::binary);		//Binary format
	if (!outBB.good())
		return false;	
		
	D3DLOCKED_RECT alphaRect, shadowRect;
	m_pAlphaMap->LockRect(0, &alphaRect, NULL, NULL);
	BYTE *alphaBits = (BYTE*)alphaRect.pBits;
	m_pShadowMap->LockRect(0, &shadowRect, NULL, NULL);
	BYTE *shadowBits = (BYTE*)shadowRect.pBits;

	for (int i = 0; i < m_numPatches.y; i++)
		for (int j = 0; j < m_numPatches.x; j++)
		{
			// save patch (i, j)
			sprintf_s(name, MAX_PATH, "%s_%d_%d.dat", fileName, i, j);
			std::ofstream out(name, std::ios::binary);		//Binary format
			if (!out.good())
			{
				outBB.close();
				return false;				
			}
			TerrainPatch* patch = m_patches[i * m_numPatches.x + j];
			
			outBB.write((char *)&patch->m_BBox, sizeof(patch->m_BBox)); // Save bounding box

			int width = TILES_PER_PATCH_X + 1;

			TerrainVertex* ver = 0;
			patch->m_pMesh->LockVertexBuffer(0,(void**)&ver);
				
			for(int y = patch->m_y * TILES_PER_PATCH_Y, y0 = 0; y <= (patch->m_y + 1) * TILES_PER_PATCH_Y; y++, y0++)
				for(int x = patch->m_x * TILES_PER_PATCH_X, x0 = 0;x <= (patch->m_x + 1) * TILES_PER_PATCH_X; x++, x0++)
				{
					if ( y < (patch->m_y + 1) * TILES_PER_PATCH_Y &&
						 x < (patch->m_x + 1) * TILES_PER_PATCH_X ) 
					{
						// Save all information associated with tile (x,y)
						TerrainTile *tile = GetTile(x, y);
						ASSERT(tile);
						// Save tile
						ASSERT(tile->m_type != -1);
						out.write((char*)&tile->m_type, sizeof(tile->m_type));		
						out.write((char*)&tile->m_height, sizeof(tile->m_height));	
					
						// Save Object Information
						MODELTYPE otype = MODEL_NONE;
						if (tile->m_pObject != m_models.end())
						{
							Model *object = *tile->m_pObject;
							// Assumption: at most one object per tile
							otype = object->m_type;
							ASSERT(otype >= 0 && otype < MODEL_NONE);
							out.write((char*)&object->m_type, sizeof(object->m_type));				
							out.write((char*)&object->m_instance.m_pos, sizeof(object->m_instance.m_pos));	
							out.write((char*)&object->m_instance.m_rotate, sizeof(object->m_instance.m_rotate));	
							out.write((char*)&object->m_instance.m_scale, sizeof(object->m_instance.m_scale));	
						} else
						{
							out.write((char*)&otype, sizeof(otype));
						}
					}
					// Save Alpha Texture
					out.write((char *)&alphaBits[y * alphaRect.Pitch + x * 4], (int)m_textures.size());
					// Save Shadow Texture
					out.write((char*)&shadowBits[y * shadowRect.Pitch + x], 1);
					// save vertex information
					out.write((char *)&ver[y0 * width + x0], sizeof(*ver));
				}
				patch->m_pMesh->UnlockVertexBuffer();
				out.close();
		}				
	m_pAlphaMap->UnlockRect(0);
	m_pShadowMap->UnlockRect(0);
	outBB.close();
	return true;
}

void Terrain::RequestTerrainPatch(int patch_x, int patch_y, FileQueue::QueueType priority)
{
	TerrainPatch* patch = m_patches[patch_y * m_numPatches.x + patch_x];
	if (patch->m_loaded)
		return;
	if (!patch->m_fileObject)
	{
		// new object
		char fileName[MAX_PATH];
		sprintf_s(fileName, MAX_PATH, "%sTerrain_%d_%d.dat", m_dataPath, patch_y, patch_x);
		if (m_dataSource == FileObject::SOURCE_HTTP)
			patch->m_fileObject =  new HttpObject((const char*)fileName, m_queueMgr);
		else
			patch->m_fileObject =  new DiskObject((const char*)fileName, m_queueMgr);
		patch->m_fileObject->Enqueue(priority);
		s_log << "Enqueue New Patch (" << patch_x << "," << patch_y << ") for queue:" << priority << std::endl;
	} else 
	{
		FileQueue::QueueType oldQueue = patch->m_fileObject->GetQueue();
		if (oldQueue == FileQueue::QUEUE_NONE || oldQueue == priority)
			return;   // either just dequeued or no change in queue
		if (patch->m_fileObject->Requeue(priority))
			s_log << "[SUCCESS] Requeue Existing Patch (" << patch_x << "," << patch_y << ") to queue:" 
				  << priority << " from queue:" << oldQueue << std::endl;
		else
			s_log << "[FAIL] Requeue Existing Patch (" << patch_x << "," << patch_y << ") to queue:" 
				   << priority << " from queue:" << oldQueue << std::endl;
	}
}

bool Terrain::PuntPatchToGPU(int patch_x, int patch_y)
{
	
	TerrainPatch* patch = m_patches[patch_y * m_numPatches.x + patch_x];
	if (patch->m_loaded)
		return TRUE;
	if (!patch->m_fileObject || !patch->m_fileObject->m_loaded)
		return FALSE;

	patch->InitMesh(*this, m_pDevice);
	
	D3DLOCKED_RECT alphaRect;
	m_pAlphaMap->LockRect(0, &alphaRect, NULL, NULL);
	BYTE *alphaBits = (BYTE*)alphaRect.pBits;
	D3DLOCKED_RECT shadowRect;
	m_pShadowMap->LockRect(0, &shadowRect, NULL, NULL);
	BYTE *shadowBits = (BYTE*)shadowRect.pBits;	

	int width = TILES_PER_PATCH_X + 1;

	TerrainVertex* ver = 0;
	patch->m_pMesh->LockVertexBuffer(0,(void**)&ver);

	for(int y = patch->m_y * TILES_PER_PATCH_Y, y0 = 0; y <= (patch->m_y + 1) * TILES_PER_PATCH_Y; y++, y0++)
		for(int x = patch->m_x * TILES_PER_PATCH_X, x0 = 0; x <= (patch->m_x + 1) * TILES_PER_PATCH_X; x++, x0++)
		{
			if ( y < (patch->m_y + 1) * TILES_PER_PATCH_Y &&
				x < (patch->m_x + 1) * TILES_PER_PATCH_X ) 
			{
				// Load all information associated with tile (x,y)
				TerrainTile *tile = GetTile(x, y);
				ASSERT( tile->m_type == -1);
				// Load tile
				
				patch->m_fileObject->Read((char*)&tile->m_type, sizeof(tile->m_type));		//type
				patch->m_fileObject->Read((char*)&tile->m_height, sizeof(tile->m_height));	//Height
				ASSERT(tile->m_type != -1);

				// Load Object Information
				MODELTYPE otype = MODEL_NONE;
				patch->m_fileObject->Read((char*)&otype, sizeof(otype));
				ASSERT(otype >= 0 && otype <= MODEL_NONE);

				if (otype != MODEL_NONE)
				{
					D3DXVECTOR3 m_pos, m_rot, m_sca;
					patch->m_fileObject->Read((char*)&m_pos, sizeof(m_pos));
					patch->m_fileObject->Read((char*)&m_rot, sizeof(m_rot));
					patch->m_fileObject->Read((char*)&m_sca, sizeof(m_sca));
					Model* obj;
					if (m_objectPool.size())
					{
						obj = m_objectPool[m_objectPool.size() - 1];
						m_objectPool.pop_back();
						obj->init(otype, INTPOINT(x, y), m_pos, m_rot, m_sca );
					} else 
					{					
						obj = new Model(otype, INTPOINT(x, y), m_pos, m_rot, m_sca );
					}
					m_models.push_back(obj);
					tile->m_pObject = m_models.end();
					--(tile->m_pObject);
				} else 
				{
					tile->m_pObject = m_models.end();
				}
			}

			// Load Alpha Texture
			patch->m_fileObject->Read((char *)&alphaBits[y * alphaRect.Pitch + x * 4], (int)m_textures.size());

			// Load Shadow Texture
			patch->m_fileObject->Read((char*)&shadowBits[y * shadowRect.Pitch + x], 1);

			// Load vertex information
			TerrainVertex* vertex = &ver[y0 * width + x0];
			patch->m_fileObject->Read((char *)vertex, sizeof(*ver));
			//Calculate bounding box bounds...
			if(vertex->position.x < patch->m_BBox.min.x) patch->m_BBox.min.x = vertex->position.x;
			if(vertex->position.x > patch->m_BBox.max.x) patch->m_BBox.max.x = vertex->position.x;
			if(vertex->position.y < patch->m_BBox.min.y) patch->m_BBox.min.y = vertex->position.y;
			if(vertex->position.y > patch->m_BBox.max.y) patch->m_BBox.max.y = vertex->position.y;
			if(vertex->position.z < patch->m_BBox.min.z) patch->m_BBox.min.z = vertex->position.z;
			if(vertex->position.z > patch->m_BBox.max.z) patch->m_BBox.max.z = vertex->position.z;
		}
	patch->m_pMesh->UnlockVertexBuffer();

	m_pAlphaMap->UnlockRect(0);
	m_pShadowMap->UnlockRect(0);

	patch->m_loaded = true;
	m_changed = true;
	s_log << "LoadTerrainPatch loaded patch (" << patch_x << "," << patch_y << ") and deleting object";
	delete(patch->m_fileObject);
	patch->m_fileObject = NULL;

	return true;
}

bool Terrain::LoadTerrainPatchBBs()
{
	char name[MAX_PATH];

	sprintf_s(name, MAX_PATH, "%sTerrain_BB.dat", m_dataPath);

	
	FileObject* fileObject;

	if (m_dataSource == FileObject::SOURCE_HTTP)
		fileObject =  new HttpObject((const char*)name, m_queueMgr);
	else
		fileObject =  new DiskObject((const char*)name, m_queueMgr);

	fileObject->Enqueue(FileQueue::QUEUE_CRITICAL);
	fileObject->Wait();

	for (int y = 0; y < m_numPatches.y; y++)
		for (int x = 0; x < m_numPatches.x; x++)
		{
			TerrainPatch* p = m_patches[y * m_numPatches.x + x];
			fileObject->Read((char *)&p->m_BBox, sizeof(p->m_BBox));
		}				
	delete fileObject;
	return true;
}

D3DXVECTOR3 Terrain::GetWorldPos(INTPOINT mappos)
{
	if(!Within(mappos))return D3DXVECTOR3(0, 0, 0);
	TerrainTile *tile = GetTile(mappos);
	return D3DXVECTOR3((float)mappos.x, tile->m_height, (float)-mappos.y);
}

void Terrain::PrefetchTerrainPatch( const Camera& camera )
{
	// camera position
	D3DXVECTOR2 cameraPos = D3DXVECTOR2(camera.Eye().x, -camera.Eye().z);
	
	// For each patch
	for (int patch_y = 0; patch_y < m_numPatches.y; patch_y++)
		for (int patch_x = 0; patch_x < m_numPatches.x; patch_x++)
		{
			TerrainPatch* patch = m_patches[patch_y * m_numPatches.x + patch_x];
			
			D3DXVECTOR2 patchPos(((float)patch_x + 0.5f) * TILES_PER_PATCH_X, ((float)patch_y  + 0.5f) * TILES_PER_PATCH_Y);
			D3DXVECTOR2 eyePos(camera.Eye().x, - camera.Eye().z);
			D3DXVECTOR2 eyeToPatch = patchPos - eyePos;
			float patchAngle = atan2f(eyeToPatch.y, eyeToPatch.x);  //  [-Pi, +Pi]

			// Calculate rotation distance and rotation time
			float angleDelta = abs(patchAngle - camera.Alpha());
			if (angleDelta > D3DX_PI)
				angleDelta = 2 * D3DX_PI - angleDelta;
			float rotationTime = angleDelta / camera.AngularVelocity();
	
			// Calculate linear distance and movement time
			float distance = D3DXVec2Length(&eyeToPatch);
			float linearTime = distance / camera.Velocity();
	
			float totalTime = rotationTime + linearTime;
			float patchTraverseTime = TILES_PER_PATCH_X / camera.Velocity();
			if (totalTime < 2 * patchTraverseTime)
				RequestTerrainPatch(patch_x, patch_y, FileQueue::QUEUE_CRITICAL);
			else if (totalTime < 4 * patchTraverseTime)
				RequestTerrainPatch(patch_x, patch_y, FileQueue::QUEUE_HIGH);
			else if (totalTime < 6 * patchTraverseTime)
				RequestTerrainPatch(patch_x, patch_y, FileQueue::QUEUE_MEDIUM);
			else if (totalTime < 8 * patchTraverseTime)
				RequestTerrainPatch(patch_x, patch_y, FileQueue::QUEUE_LOW);
			else 
				CancelTerrainPatch(patch);
		}
}

void Terrain::Update( const Camera &camera, float timeDelta )
{
	PrefetchTerrainPatch(camera);
}

void Terrain::CancelTerrainPatch( TerrainPatch* patch )
{
	FileObject* obj = patch->m_fileObject;
	if (!obj || obj->m_loaded || obj->GetQueue() == FileQueue::QUEUE_NONE)
	{
		// Unload the mesh
		patch->Unload(*this);
		return;
	}
	s_log << "Cancel patch:" << patch->m_fileObject->Path() << std::endl;
	obj->Cancel();
	delete obj;
	patch->m_fileObject = NULL;
}

void Terrain::RenderLandscape(Camera& camera)
{
	//Retrieve the surface of the back buffer
	IDirect3DSurface9 *backSurface = NULL;
	m_pDevice->GetRenderTarget(0, &backSurface);

	//Get the surface of the minimap texture
	IDirect3DSurface9 *landScapeSurface = NULL;			
	m_pLandScape->GetSurfaceLevel(0, &landScapeSurface);			

	//Set render target to the visible surface
	m_pDevice->SetRenderTarget(0, landScapeSurface);

	//Clear render target to black
	m_pDevice->Clear(0, NULL, D3DCLEAR_TARGET | D3DCLEAR_ZBUFFER, 0x00000000, 1.0f, 0);

	m_pDevice->BeginScene();

	//Set render states		
	m_pDevice->SetRenderState(D3DRS_LIGHTING, false);
	m_pDevice->SetRenderState(D3DRS_ZWRITEENABLE, true);

	//Create white shadow map
	IDirect3DTexture9* white = NULL;
	m_pDevice->CreateTexture(m_size.x, m_size.y, 1, D3DUSAGE_DYNAMIC, D3DFMT_L8, D3DPOOL_DEFAULT, &white, NULL);
	D3DLOCKED_RECT sRect;
	white->LockRect(0, &sRect, NULL, NULL);
	memset((BYTE*)sRect.pBits, 255, m_size.y * sRect.Pitch);
	white->UnlockRect(0);
	m_pDevice->SetTexture(0, m_pAlphaMap);
	m_pDevice->SetTexture(1, m_textures[0]);		// Dirt
	m_pDevice->SetTexture(2, m_textures[1]);		// Grass
	m_pDevice->SetTexture(3, m_textures[2]);		// Stone
	m_pDevice->SetTexture(4, white);				// White texture as shadow map
	m_pDevice->SetMaterial(&m_material);
	D3DXMATRIX world, view, proj;
	D3DXMatrixIdentity(&world);
	m_pDevice->SetTransform(D3DTS_WORLD, &world);

	//View matrix (eye & focus in the center of the terrain)
	D3DXVECTOR2 center(TILES_PER_PATCH_X * PATCHES_PER_TERRAIN_X / 2, -TILES_PER_PATCH_Y * PATCHES_PER_TERRAIN_Y / 2);
	D3DXMatrixLookAtLH(&view, &D3DXVECTOR3(center.x, 1000.0f, center.y),
					   &D3DXVECTOR3(center.x, 0.0f, center.y),
					   &D3DXVECTOR3(0.0f, 0.0f, 1.0f));
	D3DXMatrixOrthoLH(&proj,TILES_PER_PATCH_X * PATCHES_PER_TERRAIN_X, TILES_PER_PATCH_Y * PATCHES_PER_TERRAIN_Y, 0.1F, 2000.0f);

	//Set vertex shader variables
	m_terrainVS.SetMatrix(m_vsMatrixWorld, world);
	m_terrainVS.SetMatrix(m_vsMatrixViewProjection, view * proj);
	m_terrainVS.SetVector3(m_vsDirToSun, m_dirToSun);

	m_terrainVS.Begin();
	m_terrainPS.Begin();
	for(int patch_y = 0; patch_y < m_numPatches.y; patch_y++)
		for(int patch_x = 0; patch_x < m_numPatches.x; patch_x++)
		{
			TerrainPatch* patch = m_patches[patch_y * m_numPatches.x + patch_x];
			if (patch->m_loaded)
				patch->Render();
		}
	m_terrainPS.End();
	m_terrainVS.End();

	m_pDevice->SetTexture(1, NULL);
	m_pDevice->SetTexture(2, NULL);
	m_pDevice->SetTexture(3, NULL);
	m_pDevice->SetTexture(4, NULL);

	//Render Objects
	m_modelVS.SetMatrix(m_modelMatrixWorld, world);
	m_modelVS.SetMatrix(m_modelMatrixViewProjection, view * proj);
	m_modelVS.SetVector3(m_modelDirToSun, m_dirToSun);
	m_modelVS.SetVector3(m_modelMapSize, D3DXVECTOR3((float)m_size.x, (float)m_size.y, 0.0f));
	m_pDevice->SetTexture(1, white);		//White Texture

	m_modelVS.Begin();
	m_modelPS.Begin();

	for(std::list<Model*>::iterator it = m_models.begin(); it != m_models.end(); ++it)
	{
		D3DXMATRIX m = (*it)->m_instance.GetWorldMatrix();
		m_modelVS.SetMatrix(m_modelMatrixWorld, m);
		(*it)->Render();
	}

	m_modelVS.End();
	m_modelPS.End();

	white->Release();

	m_pDevice->EndScene();

	//Reset render target to back buffer
	m_pDevice->SetRenderTarget(0, backSurface);

	//Release surfaces
	landScapeSurface->Release();
	backSurface->Release();
}

Terrain::~Terrain()
{
	for (std::vector<ID3DXMesh *>::iterator it = m_meshPool.begin(); it != m_meshPool.end(); ++it)
		(*it)->Release();
	m_meshPool.clear();
	for (std::vector<Model *>::iterator it = m_objectPool.begin(); it != m_objectPool.end(); ++it)
		delete (*it);
	m_objectPool.clear();
}