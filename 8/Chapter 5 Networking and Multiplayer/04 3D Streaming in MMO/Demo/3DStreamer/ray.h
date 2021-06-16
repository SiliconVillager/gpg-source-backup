#ifndef _RAY
#define _RAY
#include "model.h"

class RAY{
public:
	RAY();
	RAY(D3DXVECTOR3 o, D3DXVECTOR3 d);

	//Our different intersection tests
	float Intersect(ModelInstance iMesh);
	float Intersect(BBOX bBox);
	float Intersect(BSPHERE bSphere);
	float Intersect(ID3DXMesh* mesh);

	D3DXVECTOR3 org, dir;
};
#endif