#include "ray.h"

RAY::RAY()
{
	org = dir = D3DXVECTOR3(0.0f, 0.0f, 0.0f);
}

RAY::RAY(D3DXVECTOR3 o, D3DXVECTOR3 d)
{
	org = o;
	dir = d;
}

float RAY::Intersect(ModelInstance iMesh)
{
	if (iMesh.m_pMesh == NULL) 
		return -1.0f;
	return Intersect(iMesh.m_pMesh->m_pMesh);
}

float RAY::Intersect(BBOX bBox)
{
	if (D3DXBoxBoundProbe(&bBox.min, &bBox.max, &org, &dir))
		return D3DXVec3Length(&(((bBox.min + bBox.max) / 2.0f) - org));
	else return -1.0f;
}

float RAY::Intersect(BSPHERE bSphere)
{
	if (D3DXSphereBoundProbe(&bSphere.center, bSphere.radius, &org, &dir))
		return D3DXVec3Length(&(bSphere.center - org));
	else return -1.0f;
}

float RAY::Intersect(ID3DXMesh* mesh)
{
	if (mesh == NULL) 
		return -1.0f;

	// Collect only the closest intersection
	BOOL hit;
	DWORD dwFace;
	float hitU, hitV, dist;
	D3DXIntersect(mesh, &org, &dir, &hit, &dwFace, &hitU, &hitV, &dist, NULL, NULL);

	if(hit) return dist;
	else return -1.0f;
}
