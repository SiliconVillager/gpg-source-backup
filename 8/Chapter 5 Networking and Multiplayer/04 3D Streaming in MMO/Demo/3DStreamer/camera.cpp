#include "camera.h"

Camera::Camera()
{
	Init(NULL);
}

void Camera::Init(IDirect3DDevice9* Dev)
{
	m_pDevice = Dev;
	m_alpha = 2.1f;
	m_beta = 0.6f;
	m_radius = 50.0f;
	m_height = 10.0f;
	m_fov = D3DX_PI / 2.5f;
	m_velocity = 15.0f;
	m_angularVelocity = 1.0f;

	m_eye = D3DXVECTOR3(TILES_PER_PATCH_X * PATCHES_PER_TERRAIN_X / 2, 
						m_height,  /* Eye height */
						-TILES_PER_PATCH_Y * PATCHES_PER_TERRAIN_Y / 2);
	GetViewMatrix();
}

void Camera::Scroll(D3DXVECTOR3 vec)
{
	m_eye += vec;
}

void Camera::Pitch(float f)
{
	m_beta += f;

	if(m_beta > (D3DX_PI / 2.0f) - 0.05f)
		m_beta = (D3DX_PI / 2.0f) - 0.05f;
	if(m_beta < 0.25f)
		m_beta = 0.25f;
}

void Camera::Yaw(float f)
{
	m_alpha += f * m_angularVelocity;
	if(m_alpha > D3DX_PI)
		m_alpha -= D3DX_PI * 2.0f;
	if(m_alpha < -D3DX_PI)
		m_alpha += D3DX_PI * 2.0f;
}

void Camera::Zoom(float f)
{
	m_fov += f;
	if(m_fov < 0.1f)
		m_fov = 0.1f;
	if(m_fov > D3DX_PI / 2.0f)
		m_fov = D3DX_PI / 2.0f;
}

void Camera::Update(Terrain &terrain, float timeDelta)
{
	//Restrict camera movement to the xz-plane
	m_right.y = m_look.y = 0.0f;
	D3DXVec3Normalize(&m_look, &m_look);
	D3DXVec3Normalize(&m_right, &m_right);

	//Move camera (i.e. Scroll)
	if(KEYDOWN('A')) Scroll(-m_right * timeDelta * m_velocity);
	if(KEYDOWN('D')) Scroll(m_right * timeDelta * m_velocity);
	if(KEYDOWN('W')) Scroll(m_look * timeDelta * m_velocity);
	if(KEYDOWN('S')) Scroll(-m_look * timeDelta * m_velocity);

	//Rotate Camera (i.e. Change Angle)
	if(KEYDOWN(VK_LEFT))Yaw(-timeDelta);
	if(KEYDOWN(VK_RIGHT))Yaw(timeDelta);
	if(KEYDOWN(VK_UP))Pitch(-timeDelta);
	if(KEYDOWN(VK_DOWN))Pitch(timeDelta);
	
	//Zoom (i.e. change fov)
	if(KEYDOWN(VK_ADD))Zoom(-timeDelta);
	if(KEYDOWN(VK_SUBTRACT))Zoom(timeDelta);

	// Have the eye's vertical position follow the terrain heights
	// Find patch that the eye is over
	for(int p=0;p<(int)terrain.m_patches.size();p++)
	{
		TerrainPatch* patch = terrain.m_patches[p];

		//Focus within patch maprect or not?
		if(m_eye.x >= patch->m_x * TILES_PER_PATCH_X && m_eye.x < (patch->m_x + 1) * TILES_PER_PATCH_X &&
			-m_eye.z >= patch->m_y * TILES_PER_PATCH_Y && -m_eye.z < (patch->m_y + 1) * TILES_PER_PATCH_Y)
		{			
			// Collect only the closest intersection
			BOOL hit;
			DWORD dwFace;
			float hitU, hitV, dist = 0.0;
			if (patch->m_loaded)
			{
				HRESULT result = D3DXIntersect(patch->m_pMesh, &D3DXVECTOR3(m_eye.x, 10000.0f, m_eye.z), 
											   &D3DXVECTOR3(0.0f, -1.0f, 0.0f), &hit, &dwFace, &hitU, &hitV, 
												&dist, NULL, NULL);
				ASSERT(result == D3D_OK && hit);	
			} else 
			{
				dist = 10000.0f - patch->m_BBox.max.y; 
			}
			m_eye.y = (float)(10000.0f - dist + m_height);  /* Eye Height */
			break;
		}
	}

	D3DXMATRIX view = GetViewMatrix();
	D3DXMATRIX projection = GetProjectionMatrix();

	m_pDevice->SetTransform(D3DTS_VIEW, &view);
	m_pDevice->SetTransform(D3DTS_PROJECTION, &projection);

	CalculateFrustum(view, projection);	
}

D3DXMATRIX Camera::GetViewMatrix()
{
	D3DXMATRIX  matView;
	
	// Calculate focus from eye position and orientation (alpha, beta)
	float sideRadius = m_radius * cos(m_beta);
	float height = m_radius * sin(m_beta);

	D3DXVECTOR3 focus = D3DXVECTOR3(m_eye.x + sideRadius * cos(m_alpha),
									m_eye.y - height,
									m_eye.z - sideRadius * sin(m_alpha));

	D3DXMatrixLookAtLH(&matView, &m_eye, &focus, &D3DXVECTOR3(0.0f, 1.0f, 0.0f));

	m_right.x = matView(0,0);
	m_right.y = matView(1,0);
	m_right.z = matView(2,0);
	D3DXVec3Normalize(&m_right, &m_right);

	m_look.x = matView(0,2);
	m_look.y = matView(1,2);
	m_look.z = matView(2,2);
	D3DXVec3Normalize(&m_look, &m_look);

	return  matView;
}

D3DXMATRIX Camera::GetProjectionMatrix()
{
	D3DXMATRIX  matProj;
	float aspect = 800.0f / 600.0f;
	D3DXMatrixPerspectiveFovLH(&matProj, m_fov, aspect, 1.0f, 1000.0f );
	return matProj;
}

void Camera::CalculateFrustum(D3DXMATRIX view, D3DXMATRIX projection)
{
	// Get combined matrix
	D3DXMATRIX matComb;
	D3DXMatrixMultiply(&matComb, &view, &projection);

	// Left clipping plane
	m_frustum[0].a = matComb._14 + matComb._11; 
	m_frustum[0].b = matComb._24 + matComb._21; 
	m_frustum[0].c = matComb._34 + matComb._31; 
	m_frustum[0].d = matComb._44 + matComb._41;

	// Right clipping plane 
	m_frustum[1].a = matComb._14 - matComb._11; 
	m_frustum[1].b = matComb._24 - matComb._21; 
	m_frustum[1].c = matComb._34 - matComb._31; 
	m_frustum[1].d = matComb._44 - matComb._41;

	// Top clipping plane 
	m_frustum[2].a = matComb._14 - matComb._12; 
	m_frustum[2].b = matComb._24 - matComb._22; 
	m_frustum[2].c = matComb._34 - matComb._32; 
	m_frustum[2].d = matComb._44 - matComb._42;

	// Bottom clipping plane 
	m_frustum[3].a = matComb._14 + matComb._12; 
	m_frustum[3].b = matComb._24 + matComb._22; 
	m_frustum[3].c = matComb._34 + matComb._32; 
	m_frustum[3].d = matComb._44 + matComb._42;

	// Near clipping plane 
	m_frustum[4].a = matComb._13; 
	m_frustum[4].b = matComb._23; 
	m_frustum[4].c = matComb._33; 
	m_frustum[4].d = matComb._43;

	// Far clipping plane 
	m_frustum[5].a = matComb._14 - matComb._13; 
	m_frustum[5].b = matComb._24 - matComb._23; 
	m_frustum[5].c = matComb._34 - matComb._33; 
	m_frustum[5].d = matComb._44 - matComb._43; 

	//Normalize planes
	for(int i=0;i<6;i++)
		D3DXPlaneNormalize(&m_frustum[i], &m_frustum[i]);
}

bool Camera::Cull(BBOX bBox)
{
	//For each plane in the view frustum
	for(int f=0;f<6;f++)
	{
		D3DXVECTOR3 c1, c2;

		//Find furthest point (n1) & nearest point (n2) to the plane
		if	(m_frustum[f].a > 0.0f)	
			{c1.x = bBox.max.x; c2.x = bBox.min.x;}
		else
			{c1.x = bBox.min.x; c2.x = bBox.max.x;}
		if (m_frustum[f].b > 0.0f)	
			{c1.y = bBox.max.y; c2.y = bBox.min.y;}
		else
			{c1.y = bBox.min.y; c2.y = bBox.max.y;}
		if (m_frustum[f].c > 0.0f)	
			{c1.z = bBox.max.z; c2.z = bBox.min.z;}
		else
			{c1.z = bBox.min.z; c2.z = bBox.max.z;}

		float distance1 = m_frustum[f].a * c1.x + m_frustum[f].b * c1.y + 
						  m_frustum[f].c * c1.z + m_frustum[f].d;
		float distance2 = m_frustum[f].a * c2.x + m_frustum[f].b * c2.y + 
						  m_frustum[f].c * c2.z + m_frustum[f].d;

		//If both points are on the negative side of the plane, Cull!
		if(distance1 < 0.0f && distance2 < 0.0f)
			return true;
	}

	//Object is inside the volume
	return false;
}

bool Camera::Cull(BSPHERE bSphere)
{
	//For each plane in the view frustum
	for(int f=0;f<6;f++)
	{
		float distance = D3DXVec3Dot(&bSphere.center, &D3DXVECTOR3(m_frustum[f].a, m_frustum[f].b, m_frustum[f].c)) + m_frustum[f].d;

		if(distance < -bSphere.radius)
			return true;
	}

	//Object is inside the volume
	return false;
}