#ifndef _CAMERA
#define _CAMERA

#include <d3dx9.h>
#include "log.h"
#include "terrain.h"

class Camera{
	friend class MainWindow;
	friend class Model;

public:
		//Init Camera
		Camera();
		void Init(IDirect3DDevice9* Dev);

		//Movement
		void Scroll(D3DXVECTOR3 vec);	//Move Focus
		void Pitch(float f);			//Change B-angle
		void Yaw(float f);				//Change A-angle
		void Zoom(float f);				//Change FOV

		//Calculate Eye position etc
		void Update(Terrain &terrain, float timeDelta);
		void CalculateFrustum(D3DXMATRIX view, D3DXMATRIX projection);
		bool Cull(BBOX bBox);
		bool Cull(BSPHERE bSphere);

		//Calculate Matrices
		D3DXMATRIX GetViewMatrix();
		D3DXMATRIX GetProjectionMatrix();

		float Alpha() const { return m_alpha; }
		void Alpha(float val) { m_alpha = val; }
		D3DXVECTOR3 Eye() const { return m_eye; }
		void Eye(D3DXVECTOR3 val) { m_eye = val; }
		float Velocity() const { return m_velocity; }
		void Velocity(float val) { m_velocity = val; }
		float AngularVelocity() const { return m_angularVelocity; }
		void AngularVelocity(float val) { m_angularVelocity = val; }
	
private:

		IDirect3DDevice9* m_pDevice;
		float m_alpha,    // angle from x axis to the look-at projected to (x, z) plane,  in range of (-Pi, +Pi)
			  m_beta,     // angle from (x, z) plane to look-at 
			  m_height,   // eye height from the ground
			  m_radius,   // distance from eye to focus
			  m_fov,
			  m_velocity, 
			  m_angularVelocity;
		D3DXVECTOR3 m_eye, m_right, m_look;
		//6-planes to store our view frustum
		D3DXPLANE m_frustum[6];
};

#endif
