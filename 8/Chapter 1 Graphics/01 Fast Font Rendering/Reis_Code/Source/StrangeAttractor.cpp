// StrangeAttractor.h:
// Created by: Aurelio Reis

#include "DXUT.h"

#include "StrangeAttractor.h"
#include "GuiModel.h"
#include "TileMap.h"


CArStrangeAttractor g_StrangeAttractor;


//////////////////////////////////////////////////////////////////////////
// CArStrangeAttractor
//////////////////////////////////////////////////////////////////////////

CArStrangeAttractor::CArStrangeAttractor() : m_iNumIters( 50000 )
{
}

CArStrangeAttractor::~CArStrangeAttractor()
{
}

void CArStrangeAttractor::Create()
{
	//m_pTex = g_TextureManager.CreateTexture( "Data/Misc/dot_noise.tga" );
	m_hTq = g_pGuiModel->FindTechnique( "RenderGPUQuadAdd" );
}

void CArStrangeAttractor::Destroy()
{
}

static const int NUM_COLORS = 6;
static D3DXVECTOR4 g_vColors[ NUM_COLORS ] =
{
	D3DXVECTOR4( 1.0f, 0.0f, 0.0f, 1.0f ),	// red
	D3DXVECTOR4( 0.0f, 1.0f, 0.0f, 1.0f ),	// green
	D3DXVECTOR4( 0.0f, 0.0f, 1.0f, 1.0f ),	// blue
	D3DXVECTOR4( 1.0f, 1.0f, 0.0f, 1.0f ),	// yellow
	D3DXVECTOR4( 1.0f, 0.0f, 1.0f, 1.0f ),	// pink
	D3DXVECTOR4( 0.0f, 1.0f, 1.0f, 1.0f ),	// cyan
};

void CArStrangeAttractor::Draw()
{
	// Pickover Attractor Equation:
	//xn+1 = sin(a yn) + c cos(a xn)
	//yn+1 = sin(b xn) + d cos(b yn)

	int iScreenWidth = g_pDeviceSettings->d3d9.pp.BackBufferWidth;
	int iScreenHeight = g_pDeviceSettings->d3d9.pp.BackBufferHeight;

	double a = -2.5, b = 1.7, c = 1.1, d = -1.2;

	a += cos( g_fTime * 0.2 ) * 0.5 + 0.1f;
	b += cos( g_fTime * 0.5 ) * 0.1;
	c += cos( g_fTime * 0.1 ) * 0.2 + ( int( g_fTime ) % 5 ) / 500.0f;
	d += cos( g_fTime * 0.8) * 0.3;

	double minx = -3.0;
	double miny = minx * iScreenHeight / iScreenWidth;

	double maxx = 3.0;
	double maxy = maxx * iScreenHeight / iScreenWidth;

	double x = 0.0;
	double y = 0.0;

	for ( int i = 0; i < m_iNumIters; ++i )
	{
		double xn = sin( a * y ) + c * cos( a * x );
		double yn = sin( b * x ) + d * cos( b * y );

		x = xn;
		y = yn;

		int xi = int( ( x - minx ) * iScreenWidth / ( maxx - minx ) );
		int yi = int( ( y - miny ) * iScreenHeight / ( maxy - miny ) );

		D3DXVECTOR3 vColor = D3DXVECTOR3( 1.0f, 0.0f, 0.2f );

		float exposure = 0.05f;
		vColor.x = 1.0f - expf( -exposure * vColor.x );
		vColor.y = 1.0f - expf( -exposure * vColor.y );
		vColor.z = 1.0f - expf( -exposure * vColor.z );

		g_pGuiModel->DrawRect( xi, yi, 4, 4, D3DCOLOR_COLORVALUE( vColor.x, vColor.y, vColor.z, 1.0f ), NULL, m_hTq );
	}
}
