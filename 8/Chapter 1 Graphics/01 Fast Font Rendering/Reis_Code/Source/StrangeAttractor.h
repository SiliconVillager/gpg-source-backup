// StrangeAttractor.h:
// Created by: Aurelio Reis

#ifndef __AR__STRANGE__ATTRACTOR__H__
#define __AR__STRANGE__ATTRACTOR__H__

#include "TextureManager.h"


//////////////////////////////////////////////////////////////////////////
// CArStrangeAttractor
//////////////////////////////////////////////////////////////////////////

class CArStrangeAttractor
{
private:
	CArBaseTexture *m_pTex;
	D3DXHANDLE m_hTq;
	int m_iNumIters;

public:
	CArStrangeAttractor();
	~CArStrangeAttractor();

	void Create();
	void Destroy();
	void Draw();

	void SetNumIters( int iNumIters ) { m_iNumIters = iNumIters; }
};


extern CArStrangeAttractor g_StrangeAttractor;




#endif // __AR__STRANGE__ATTRACTOR__H__
