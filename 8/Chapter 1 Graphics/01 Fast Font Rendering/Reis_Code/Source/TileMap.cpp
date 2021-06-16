// TileMap.h:
// Created by: Aurelio Reis

#include "DXUT.h"

#include "TileMap.h"
#include "GuiModel.h"


CArTileMap g_Map;


//////////////////////////////////////////////////////////////////////////
// CArTileMap
//////////////////////////////////////////////////////////////////////////

CArTileMap::CArTileMap()
{
}

CArTileMap::~CArTileMap()
{
}

void CArTileMap::CreateDefaultMap()
{
	m_iWidth = 32;
	m_iHeight = 32;

	m_pTileGrid = new ETileSprite[ m_iWidth * m_iHeight ];

	const char CityLayout[ 32 ][ 32 ] =
	{
		{ 'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b' },
		{ 'r',	'r',	'r',	'r',	'r',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b' },
		{ 'b',	'r',	'b',	'r',	'b',	'r',	'b',	'b',	'b',	'r',	'r',	'r',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b' },
		{ 'b',	'r',	'b',	'r',	'b',	'r',	'b',	'b',	'b',	'r',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b' },
		{ 'b',	'r',	'r',	'r',	'b',	'r',	'b',	'b',	'b',	'r',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b' },
		{ 'b',	'b',	'b',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r' },
		{ 'b',	'b',	'b',	'r',	'b',	'b',	'r',	'b',	'r',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b' },
		{ 'b',	'b',	'r',	'r',	'b',	'b',	'r',	'b',	'r',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b' },
		{ 'b',	'b',	'r',	'b',	'b',	'b',	'r',	'b',	'r',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b' },
		{ 'b',	'b',	'r',	'b',	'b',	'b',	'r',	'b',	'r',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b' },
		{ 'b',	'b',	'r',	'b',	'b',	'b',	'r',	'b',	'r',	'b',	'b',	'r',	'r',	'r',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b' },
		{ 'b',	'b',	'r',	'b',	'b',	'b',	'r',	'b',	'r',	'r',	'r',	'r',	'b',	'r',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b' },
		{ 'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'b',	'b',	'b',	'b',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'b',	'b',	'b' },
		{ 'b',	'b',	'r',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b' },
		{ 'b',	'b',	'r',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b' },
		{ 'b',	'b',	'r',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b' },
		//////////////////////////////////////////////////////////////////////////
		{ 'b',	'b',	'r',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b' },
		{ 'b',	'b',	'r',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b' },
		{ 'b',	'b',	'r',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b' },
		{ 'b',	'b',	'r',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b' },
		{ 'b',	'b',	'r',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b' },
		{ 'b',	'b',	'r',	'b',	'b',	'b',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r' },
		{ 'b',	'b',	'r',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b' },
		{ 'r',	'r',	'r',	'r',	'r',	'r',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b' },
		{ 'b',	'b',	'r',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b' },
		{ 'b',	'b',	'r',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b' },
		{ 'b',	'b',	'r',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b' },
		{ 'b',	'b',	'r',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b' },
		{ 'b',	'b',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r',	'r' },
		{ 'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b' },
		{ 'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b' },
		{ 'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'b',	'r',	'b',	'b',	'b',	'b',	'b' },
	};

	// Analyze the city layout and compute the proper tiles.
	// NOTE: This code is pretty nasty.
	for ( int r = 0; r < m_iHeight; ++r )
	{
		for ( int c = 0; c < m_iWidth; ++c )
		{
			// Building?
			if ( CityLayout[ r ][ c ] == 'b' )
			{
				// Randomly pick one of any of the building types.
				m_pTileGrid[ c + r * m_iWidth ] = ETileSprite( TSPRITE_BUILDING_0 + RandInt( 0, 2 ) );
			}
			// Road?
			else if ( CityLayout[ r ][ c ] == 'r' )
			{
				int nextr = r + 1;
				int prevr = r - 1;
				int nextc = c + 1;
				int prevc = c - 1;

				// Clamp.
				if ( nextr >= m_iHeight ) { nextr = -1; }
				if ( nextc >= m_iWidth ) { nextc = -1; }
				if ( prevr < 0 ) { prevr = -1; }
				if ( prevc < 0 ) { prevc = -1; }

				int RoadMask = 0;

				// West.
				if ( prevc != -1 && CityLayout[ r ][ prevc ] == 'r' )
				{
					RoadMask |= RD_W;
				}

				// East.
				if ( nextc != -1 && CityLayout[ r ][ nextc ] == 'r' )
				{
					RoadMask |= RD_E;
				}

				// North.
				if ( prevr != -1 && CityLayout[ prevr ][ c ] == 'r' )
				{
					RoadMask |= RD_N;
				}

				// South.
				if ( nextr != -1 && CityLayout[ nextr ][ c ] == 'r' )
				{
					RoadMask |= RD_S;
				}

				//////////////////////////////////////////////////////////////////////////
				// Handle corner cases.
				//////////////////////////////////////////////////////////////////////////

				// FIXME: These don't work if the road has multiple connections.

				// Horizontal edge.
				if ( prevc == -1 )
				{
					RoadMask |= RD_W | RD_E;
				}

				// Vertical edge.
				if ( prevr == -1 )
				{
					RoadMask |= RD_N | RD_S;
				}

				//////////////////////////////////////////////////////////////////////////
				// Figure out the tile sprite to place here.
				//////////////////////////////////////////////////////////////////////////

				ETileSprite Tile = TSPRITE_ROAD_NS;

				if ( RoadMask & RD_N )
				{
					if ( RoadMask & RD_S )
					{
						Tile = TSPRITE_ROAD_NS;
					}

					if ( RoadMask & RD_W )
					{
						Tile = TSPRITE_ROAD_NW;
					}

					if ( RoadMask & RD_E )
					{
						Tile = TSPRITE_ROAD_NE;
					}

					if ( RoadMask & RD_W && RoadMask & RD_S )
					{
						Tile = TSPRITE_ROAD_NWS;
					}

					if ( RoadMask & RD_E && RoadMask & RD_S )
					{
						Tile = TSPRITE_ROAD_NES;
					}

					if ( RoadMask & RD_W && RoadMask & RD_E )
					{
						Tile = TSPRITE_ROAD_WNE;
					}

					if ( RoadMask & RD_W && RoadMask & RD_E && RoadMask & RD_S )
					{
						Tile = TSPRITE_ROAD_NESW;
					}
				}
				else if ( RoadMask & RD_W && RoadMask & RD_S && RoadMask & RD_E )
				{
					Tile = TSPRITE_ROAD_WSE;
				}
				else if ( RoadMask & RD_W && RoadMask & RD_E )
				{
					Tile = TSPRITE_ROAD_WE;
				}
				else if ( RoadMask & RD_S && RoadMask & RD_E )
				{
					Tile = TSPRITE_ROAD_SE;
				}
				else if ( RoadMask & RD_S && RoadMask & RD_W )
				{
					Tile = TSPRITE_ROAD_SW;
				}
				else if ( RoadMask & RD_W )
				{
					Tile = TSPRITE_ROAD_WE;
				}

				m_pTileGrid[ c + r * m_iWidth ] = Tile;
			}
			else
			{
				assert( 0 );
			}
		}
	}
}

void CArTileMap::Create()
{
	m_pTileSprites[ 0 ] = g_TextureManager.CreateTexture( "Data/Roads/road_ns_0.tga" );
	m_pTileSprites[ 1 ] = g_TextureManager.CreateTexture( "Data/Roads/road_we_1.tga" );
	m_pTileSprites[ 2 ] = g_TextureManager.CreateTexture( "Data/Roads/road_ne_2.tga" );
	m_pTileSprites[ 3 ] = g_TextureManager.CreateTexture( "Data/Roads/road_nw_3.tga" );
	m_pTileSprites[ 4 ] = g_TextureManager.CreateTexture( "Data/Roads/road_se_4.tga" );
	m_pTileSprites[ 5 ] = g_TextureManager.CreateTexture( "Data/Roads/road_sw_5.tga" );
	m_pTileSprites[ 6 ] = g_TextureManager.CreateTexture( "Data/Roads/road_nws_6.tga" );
	m_pTileSprites[ 7 ] = g_TextureManager.CreateTexture( "Data/Roads/road_nes_7.tga" );
	m_pTileSprites[ 8 ] = g_TextureManager.CreateTexture( "Data/Roads/road_wne_8.tga" );
	m_pTileSprites[ 9 ] = g_TextureManager.CreateTexture( "Data/Roads/road_wse_9.tga" );
	m_pTileSprites[ 10 ] = g_TextureManager.CreateTexture( "Data/Roads/road_nesw_10.tga" );

	m_pTileSprites[ 11 ] = g_TextureManager.CreateTexture( "Data/Buildings/building_0.tga" );
	m_pTileSprites[ 12 ] = g_TextureManager.CreateTexture( "Data/Buildings/building_1.tga" );
	m_pTileSprites[ 13 ] = g_TextureManager.CreateTexture( "Data/Buildings/building_2.tga" );

	CreateDefaultMap();

	m_Font.Create( "Data/Fonts/Times42" );
}

void CArTileMap::Destroy()
{
	for ( int i = 0; i < NUM_TILE_SPRITES; ++i )
	{
		g_TextureManager.DestroyTexture( m_pTileSprites[ i ] );
	}

	SAFE_DELETE_ARRAY( m_pTileGrid );

	m_Font.Destroy();
}

void CArTileMap::Draw()
{
	const int iScreenWidth = g_pDeviceSettings->d3d9.pp.BackBufferWidth;
	const int iScreenHeight = g_pDeviceSettings->d3d9.pp.BackBufferHeight;

	const int	iTileWidth = iScreenWidth / m_iWidth,
				iTileHeight = iScreenHeight / m_iHeight;

	for ( int r = 0; r < m_iHeight; ++r )
	{
		for ( int c = 0; c < m_iWidth; ++c )
		{
			ETileSprite Sprite = m_pTileGrid[ c + r * m_iWidth ];
			CArBaseTexture *SpriteTex = m_pTileSprites[ Sprite ];

			// NOTE: TC not rectangular but rather dims (tc x, y, width, height).
			D3DXVECTOR4 vTexcoords = D3DXVECTOR4( SpriteTex->GetOffset().x, SpriteTex->GetOffset().y, SpriteTex->GetSize().x, SpriteTex->GetSize().y );
			g_pGuiModel->DrawRectTc( c * iTileWidth, r * iTileHeight, iTileWidth, iTileHeight, vTexcoords, D3DXCOLOR( 1.0f, 1.0f, 1.0f, 1.0f ), SpriteTex->GetD3dTexture() );
		}
	}
}
