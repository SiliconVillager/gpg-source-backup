// TileMap.h:
// Created by: Aurelio Reis

#ifndef __AR__TILE__MAP__H__
#define __AR__TILE__MAP__H__

#include "BitmapFont.h"
#include "TextureManager.h"


//////////////////////////////////////////////////////////////////////////
// CArTileMap
//////////////////////////////////////////////////////////////////////////

class CArTileMap
{
public:
	enum ERoadDir { RD_N = 1 << 0, RD_E = 1 << 1, RD_S = 1 << 2, RD_W = 1 << 3 };

	enum ETileSprite
	{
		// Roads.
		TSPRITE_ROAD_NS = 0, TSPRITE_ROAD_WE, TSPRITE_ROAD_NE, TSPRITE_ROAD_NW, TSPRITE_ROAD_SE,
		TSPRITE_ROAD_SW, TSPRITE_ROAD_NWS, TSPRITE_ROAD_NES,
		TSPRITE_ROAD_WNE, TSPRITE_ROAD_WSE, TSPRITE_ROAD_NESW,

		// Buildings.
		TSPRITE_BUILDING_0 = 11, TSPRITE_BUILDING_1, TSPRITE_BUILDING_2,

		NUM_TILE_SPRITES
	};

private:
	int m_iWidth, m_iHeight;
	ETileSprite *m_pTileGrid;
	CArBaseTexture *m_pTileSprites[ NUM_TILE_SPRITES ];

public:
	CArBitmapFont m_Font;

	CArTileMap();
	~CArTileMap();

	void CreateDefaultMap();
	void Create();
	void Destroy();
	void Draw();
};


extern CArTileMap g_Map;


#endif // __AR__TILE__MAP__H__
