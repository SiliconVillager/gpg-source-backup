//        File: terrain.h
// Description: Terrain streaming
// 
// Copyright (C) 2009 Kevin Kaichuan He.  All rights reserved. 
//
#ifndef _TERRAIN_
#define _TERRAIN_

#include <d3dx9.h>
#include <vector>
#include <list>
#include <fstream>
#include "height_map.h"
#include "log.h"
#include "shader.h"
#include "model.h"
#include "file_object.h"

#define TILES_PER_PATCH_X 32
#define TILES_PER_PATCH_Y 32
#define PATCHES_PER_TERRAIN_X 32
#define PATCHES_PER_TERRAIN_Y 32
#define TILES_PER_TERRAIN_X (TILES_PER_PATCH_X * PATCHES_PER_TERRAIN_X)
#define TILES_PER_TERRAIN_Y (TILES_PER_PATCH_Y * PATCHES_PER_TERRAIN_Y)
#define VERTICES_PER_TERRAIN_X (TILES_PER_TERRAIN_X + 1)
#define VERTICES_PER_TERRAIN_Y (TILES_PER_TERRAIN_Y + 1)

class Camera;

class TerrainVertex
{
public:
	TerrainVertex(){}
	TerrainVertex(D3DXVECTOR3 pos, D3DXVECTOR3 norm, D3DXVECTOR2 _uv1, D3DXVECTOR2 _uv2)
	{
		position = pos;
		normal = norm;
		uv1 = _uv1;
		uv2 = _uv2;
	}

	D3DXVECTOR3 position, normal;
	D3DXVECTOR2 uv1, uv2;

	static const DWORD FVF;
};

class TerrainPatch{
public:
	TerrainPatch(int x, int y);
	~TerrainPatch();
	void Release();
	HRESULT InitMesh(Terrain &terran, IDirect3DDevice9* Dev);
	HRESULT CreateMesh(Terrain &terran, IDirect3DDevice9* Dev);
	void Unload(Terrain& terrain);
	void Render();

	IDirect3DDevice9* m_pDevice;
	ID3DXMesh *m_pMesh;
	BBOX m_BBox;
	bool m_loaded;
	int m_x;
	int m_y;
	FileObject* m_fileObject;
};

class TerrainTile{
public:
	TerrainTile() : m_type(-1), m_height(0.0f) {}
	int m_type;
	float m_height;
	std::list<Model*>::iterator m_pObject;
};

class Terrain{
	friend class TerrainPatch;
	friend class Camera;
	friend class MainWindow;
	public:
		Terrain();	
		~Terrain();
		void Init(IDirect3DDevice9* Dev, INTPOINT size, INTPOINT numPatches, bool mapGen, 
				  FileObject::DataSource dataSource, const char* dataPath, FileQueueManager* queueMgr);
		void Release();
		D3DXVECTOR3 GetNormal(int x, int y);
		void AddObject(MODELTYPE type, INTPOINT mappos);
		void Update(const Camera &camera, float timeDelta);
		void Render(Camera &camera);		
		void Progress(std::string text, float prc);
		void RenderLandscape(Camera& camera);
		TerrainTile* GetTile(int x, int y);
		TerrainTile* GetTile(INTPOINT p){return GetTile(p.x, p.y);}
		D3DXVECTOR3 GetWorldPos(INTPOINT mappos);
		bool Within(INTPOINT p);	//Test if a point is within the bounds of the terrain

		bool SaveTerrainPatches(char fileName[]);
		void InitTerrainData();
		void PrefetchTerrainPatch(const Camera& camera);
		void RequestTerrainPatch(int patch_x, int patch_y, FileQueue::QueueType priority);
		void CancelTerrainPatch(TerrainPatch* patch);
		bool PuntPatchToGPU(int x, int y);
		bool LoadTerrainPatchBBs();
		
		
		INTPOINT m_size;
		INTPOINT m_numPatches;
		std::vector<IDirect3DTexture9*> m_textures;
		HeightMap *m_pHeightMap;
		TerrainTile *m_pTiles;
		IDirect3DTexture9* m_pAlphaMap;
		IDirect3DTexture9* m_pShadowMap;
		IDirect3DTexture9* m_pLandScape;
		
		std::vector<TerrainPatch*> m_patches;
		std::list<Model*> m_models;
		std::vector<ID3DXMesh *> m_meshPool;
		std::vector<Model *> m_objectPool;
		bool m_changed;   //whether new patch has been loaded or old patch has been unloaded

		D3DXVECTOR3 m_dirToSun;
		IDirect3DDevice9* m_pDevice; 

	private:
		ID3DXFont *m_pProgressFont;
	
		SHADER m_terrainPS, m_terrainVS;
		SHADER m_modelPS, m_modelVS;

		D3DXHANDLE m_vsMatrixWorld, m_vsMatrixViewProjection, m_vsDirToSun;
		D3DXHANDLE m_modelMatrixWorld, m_modelMatrixViewProjection, m_modelDirToSun, m_modelMapSize;

		D3DMATERIAL9 m_material;

		FileQueueManager *m_queueMgr;
		FileObject::DataSource m_dataSource;
		const char* m_dataPath;
};

#endif