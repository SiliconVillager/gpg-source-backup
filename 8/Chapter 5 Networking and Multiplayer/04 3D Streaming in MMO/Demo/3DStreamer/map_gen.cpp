#include "terrain.h"
#include "height_map.h"
#include "ray.h"

void InitTiles(Terrain* terrain)
{
	//Read maptile heights & types from heightmap
	for(int y = 0; y < terrain->m_size.y; y++)
		for(int x = 0; x < terrain->m_size.x; x++)
		{
			TerrainTile *tile = terrain->GetTile(x, y);
			if(terrain->m_pHeightMap != NULL)
				tile->m_height = terrain->m_pHeightMap->GetHeight(x, y);

			if(tile->m_height < 0.3f)		 tile->m_type = 0;	// Dirt
			else if(tile->m_height < 7.0f)	 tile->m_type = 1;	// Grass
			else							 tile->m_type = 2;	// Stone
		}
}

void CreatePatches(Terrain* terrain)
{
	//Clear any old patches
	for(int i=0;i<(int)terrain->m_patches.size();i++)
		if(terrain->m_patches[i] != NULL)
			terrain->m_patches[i]->Release();
	terrain->m_patches.clear();
	//Create new patches
	for(int y = 0; y < terrain->m_numPatches.y; y++)
	{
		terrain->Progress("Creating Terrain Mesh", y / (float)PATCHES_PER_TERRAIN_Y);
		for(int x = 0; x < terrain->m_numPatches.x; x++)
		{
			TerrainPatch *p = new TerrainPatch(x, y);
			p->CreateMesh(*terrain, terrain->m_pDevice);
			p->m_fileObject = NULL;
			p->m_loaded = true;
			terrain->m_patches.push_back(p);
		}
	}
}

void CalculateAlphaMaps(Terrain* terrain)
{
	terrain->Progress("Creating Alpha Map", 0.0f);

	//Clear old alpha maps
	if(terrain->m_pAlphaMap != NULL)
		terrain->m_pAlphaMap->Release();

	//Create new alpha map
	D3DXCreateTexture(terrain->m_pDevice, terrain->m_size.x, terrain->m_size.y, 1, D3DUSAGE_DYNAMIC, 
					  D3DFMT_A8R8G8B8, D3DPOOL_DEFAULT, &terrain->m_pAlphaMap);

	//Lock the texture
	D3DLOCKED_RECT sRect;
	terrain->m_pAlphaMap->LockRect(0, &sRect, NULL, NULL);
	BYTE *bytes = (BYTE*)sRect.pBits;
	memset(bytes, 0, terrain->m_size.y * sRect.Pitch);		//Clear texture to black

	for(int i = 0;i < (int)terrain->m_textures.size(); i++)
		for(int y = 0; y < terrain->m_size.y; y++)
		{
			terrain->Progress("Creating Alpha Map", 
							 (float)(i * terrain->m_size.y + y) / (terrain->m_textures.size() * terrain->m_size.y));
			for(int x = 0; x < terrain->m_size.x; x++)
			{
				TerrainTile *tile = terrain->GetTile(x, y);
				// Apply a filter to smooth the border among different tile types
				int intensity = 0;
				if(tile != NULL && tile->m_type == i) ++intensity;
				tile = terrain->GetTile(x - 1, y);
				if(tile != NULL && tile->m_type == i) ++intensity;
				tile = terrain->GetTile(x , y - 1);
				if(tile != NULL && tile->m_type == i) ++intensity;
				tile = terrain->GetTile(x + 1, y);
				if(tile != NULL && tile->m_type == i) ++intensity;
				tile = terrain->GetTile(x , y + 1);
				if(tile != NULL && tile->m_type == i) ++intensity;
				bytes[y * sRect.Pitch + x * 4 + i] = 255 * intensity / 5;
			}
		}
		//Unlock the texture
		terrain->m_pAlphaMap->UnlockRect(0);
}

void CalculateShadowMap(Terrain* terrain)
{
	//Clear old shadow maps
	if(terrain->m_pShadowMap != NULL)
		terrain->m_pShadowMap->Release();

	//Create new shadow map
	D3DXCreateTexture(terrain->m_pDevice, terrain->m_size.x, terrain->m_size.y, 1, 
					  D3DUSAGE_DYNAMIC, D3DFMT_L8, D3DPOOL_DEFAULT, &terrain->m_pShadowMap);

	//Lock the texture
	D3DLOCKED_RECT sRect;
	terrain->m_pShadowMap->LockRect(0, &sRect, NULL, NULL);
	BYTE *bytes = (BYTE*)sRect.pBits;
	memset(bytes, 255, terrain->m_size.y * sRect.Pitch);		//Clear texture to white

	for(int y = 0; y < terrain->m_size.y; y++)
	{
		terrain->Progress("Calculating Shadow Map", y / (float)terrain->m_size.y);
		for(int x = 0; x < sRect.Pitch; x++)
		{
			//Find patch that the terrain_x, terrain_z is over
			bool done = false;
			for(int p = 0; p<(int)terrain->m_patches.size() && !done; p++)
			{
				TerrainPatch* patch = terrain->m_patches[p];

				if(x >= patch->m_x * TILES_PER_PATCH_X && x < (patch->m_x + 1) * TILES_PER_PATCH_X &&
					y >= patch->m_y * TILES_PER_PATCH_Y && y < (patch->m_y + 1) * TILES_PER_PATCH_Y)
				{			
					// Collect only the closest intersection
					RAY rayTop(D3DXVECTOR3((float)x, 10000.0f, (float)-y), D3DXVECTOR3(0.0f, -1.0f, 0.0f));
					float dist = rayTop.Intersect(terrain->m_patches[p]->m_pMesh);

					if(dist >= 0.0f)
					{
						RAY ray(D3DXVECTOR3((float)x, 10000.0f - dist + 0.01f, (float)-y), terrain->m_dirToSun);
						for(int p2 = 0; p2<(int)terrain->m_patches.size() && !done; p2++)
							if(ray.Intersect(terrain->m_patches[p2]->m_BBox) >= 0)
							{
								if(ray.Intersect(terrain->m_patches[p2]->m_pMesh) >= 0)	//In shadow
								{
									done = true;
									bytes[y * sRect.Pitch + x] = 128;
								}
							}

							done = true;
					}
				}
			}						
		}
	}

	//Smooth shadow map
	for(int i = 0; i < 3; i++)
	{
		terrain->Progress("Smoothing the Shadow Map", i / 3.0f);

		BYTE* tmpBytes = new BYTE[terrain->m_size.y * sRect.Pitch];
		memcpy(tmpBytes, sRect.pBits, terrain->m_size.y * sRect.Pitch);

		for(int y = 1; y < terrain->m_size.y - 1; y++)
			for(int x = 1; x < sRect.Pitch - 1 ; x++)
			{
				long index = y * sRect.Pitch + x;
				BYTE b1 = bytes[index];
				BYTE b2 = bytes[index - 1];
				BYTE b3 = bytes[index - sRect.Pitch];
				BYTE b4 = bytes[index + 1];
				BYTE b5 = bytes[index + sRect.Pitch];

				tmpBytes[index] = (BYTE)((b1 + b2 + b3 + b4 + b5) / 5);
			}

			memcpy(sRect.pBits, tmpBytes, terrain->m_size.y * sRect.Pitch);
			delete [] tmpBytes;
	}

	//Unlock the texture
	terrain->m_pShadowMap->UnlockRect(0);
}


void GenerateRandomHeightMap(Terrain* terrain)
{
	terrain->Release();

	//Create two height maps and multiply them
	terrain->m_pHeightMap = new HeightMap(terrain->m_size, 20.0f);
	HeightMap hm2(terrain->m_size, 2.0f);

	terrain->m_pHeightMap->CreateRandomHeightMap(terrain, "Create Random Height Map 1", rand()%2000, 
												 1.0f * terrain->m_numPatches.x / 3, 0.7f, 7);
	hm2.CreateRandomHeightMap(terrain, "Create Random Height Map 2", rand()%2000, 2.5f * terrain->m_numPatches.x / 3, 0.8f, 3);

	hm2.Cap(hm2.m_maxHeight * 0.4f);

	*(terrain->m_pHeightMap) *= hm2;
	hm2.Release();

	//Add objects
	HeightMap hm3(terrain->m_size, 1.0f);
	hm3.CreateRandomHeightMap(terrain, "Create Random Height Map Objects", rand()%1000, 5.5f * terrain->m_numPatches.x / 3, 0.9f, 7);

	for(int y = 0;y < terrain->m_size.y; y++)
		for(int x = 0; x< terrain->m_size.x; x++)
		{
			if(terrain->m_pHeightMap->GetHeight(x, y) == 0.0f && hm3.GetHeight(x, y) > 0.7f && rand()%6 == 0)
				terrain->AddObject(MODEL_TREE, INTPOINT(x, y));	//Tree
			else if(terrain->m_pHeightMap->GetHeight(x, y) >= 1.0f && hm3.GetHeight(x, y) > 0.9f && rand()%20 == 0)
				terrain->AddObject(MODEL_STONE, INTPOINT(x, y));	//Stone
			else 
			{
				TerrainTile *tile = terrain->GetTile(x, y);
				tile->m_pObject = terrain->m_models.end();
			}
		}
	hm3.Release();			
}


void GenerateRandomTerrain(Terrain* terrain)
{
	GenerateRandomHeightMap(terrain);
	InitTiles(terrain);
	CreatePatches(terrain);
	CalculateAlphaMaps(terrain);
	CalculateShadowMap(terrain);
}