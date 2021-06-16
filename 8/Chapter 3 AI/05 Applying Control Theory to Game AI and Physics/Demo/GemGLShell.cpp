/* GemGLShell.cpp 

Control law steering demonstration. 
Author:  Brian Pickrell

Instructions:  Type a number 1-5 to select a lane.  The cars will steer to the selected lane,
each according to its own internal control law.  Hit <ESC> to end the simulation.

To add more cars or modify their control laws in source code, see instructions in the main() 
routine.  Control laws are represented as a vector of oscillatory modes containing frequency 
and other parameters.  Each mode represents a single term in Equation 1.1 in the article.

Discussion:  This is a side-scrolling demo.  Several cars are driving down a road and changing lanes.
Each car has its own control law that governs how it steers to the new lane.  The control law consists
of a set of oscillatory modes as discussed in the article; therefore, each car moves in a set of
sinusoidal-type motions.  By selecting modes with various frequencies and damping coefficients, the
user can model cars with different steering performance and different driver skills.

The internal
model for this is a 1-dimensional motion with Y value as the controlled variable.  The 
extremely simple navigation loop computes car position directly from the values of
its modes.  For simplicity, there is not a real steering model; each lane command is 
represented as an abrupt step input followed by harmonic settling.

*/

#include "stdafx.h"

#include <math.h>
#include <time.h>
#include <Vector>

using namespace std;

#define GLOBAL_SCALE     1.0f
#define NUM_INTERVALS    20
#define INTERVAL         GLOBAL_SCALE
#define START_HOR_OFFSET (-10.0f*GLOBAL_SCALE)
#define END_HOR_OFFSET   (START_HOR_OFFSET + (NUM_INTERVALS*INTERVAL))
#define START_VER_OFFSET (-10.0f*GLOBAL_SCALE)
#define END_VER_OFFSET   (START_VER_OFFSET + (NUM_INTERVALS*INTERVAL))
#define SPEED_FACTOR     (5.0f*GLOBAL_SCALE)

#define LANE_WIDTH       0.3f
// origin offset for Y axis calculations.  This puts Y = 6 in center of screen
#define Y_BASE ((START_VER_OFFSET+ END_VER_OFFSET)/2 - 6)
#define PI 3.14159

static const int CAR_SPEED=5;

typedef struct vec2_ {
	float x, y;
} vec2;

typedef struct rgbColor_
{
	GLubyte r, g, b;
} rgbColor;

/*  The mode structure stores the parameters for a single oscillatory mode of
    a car's mostion (corresponding also to a root of the characteristic edquation).
	Each driver can have any number of modes.

*/
struct mode {
  float zeta;  // damping coefficient
  float omega; // frequency
  float A;   // Initial amplitude of wave
  /*  Phase offset theta.  In real controlled motions, not all sine waves start at the same place;
      actually, each starts at a different point; the offset is called a phase angle.  Phase angles 
	  are extremely important but are not included in this demo.
	  */
  //float theta;  
};

typedef std::vector<mode> modeList;

// A car.  These are all NPC-driven.
class driver
{
public:
	// 2-dimensional position (x value is fixed).
	vec2 driverPos;

	// 2-dimensional velocity vector.  Used only for determining heading when rendering.
	vec2 driverSpeed;

	rgbColor color;

	// The modes of the control law.
	modeList modes;

	// Constructor only sets a car's color.  Give theRGB values as arguments.
	driver( GLubyte r, GLubyte g, GLubyte b ){ color.r=r; color.g=g; color.b=b; };
};

std::vector<driver> driverList;

float gameTime;
float resetTime;
int targetLane;

void key(unsigned char key, int x, int y)
{
	switch(key) {

	case '1':
	case '2':
	case '3':
	case '4':
	case '5':
		// Anonymous scope to allow variable initialization inside a case statement
		{
		/*  Process a lane command.  
			When the player enters a number key 1-5, all of the cars start steering to the 
			numbered lane.  This loop sets the target lane and initializes the amplitude
			parameter for the navigation loop.

			The choice of
			amplitudes A is a mostly arbitrary shortcut; the value used makes sure
			there are no skips or discontinuities but does not represent real steering.
			*/

			float oldLane = targetLane;
			targetLane = 2 * ( key - '0' );

			std::vector<driver>::iterator driverIt;
			for( driverIt = driverList.begin(); driverIt != driverList.end(); driverIt++ )
			{
				/* Initialize each driver's navigation by setting his amplitude.
				   Amplitudes chosen to match driver's current position, which is a cheat.
				   For cars with more than one mode, this loop sets all A's equal which 
				   is also a major cheat; in real life the oscillatory modes always come in
				   a variety of different sizes.
				 
				*/
				for ( modeList::iterator modeIt = driverIt->modes.begin(); modeIt != driverIt->modes.end();
					modeIt++ )
				{
					// Amplitude = distance from current position to commanded lane
					modeIt->A = ( driverIt->driverPos.y - Y_BASE - targetLane ) / driverIt->modes.size();
				}
			}
		}
		resetTime = 0.f;   // restart oscillations
		break;

	case VK_ESCAPE:
		exit(0);
	}
}


void drawarena(void)
{
	int r, c;

	// draw the drivers' lane first
	glBegin(GL_QUADS);
		glColor4ub(99, 99, 99, 255);
		glVertex3f(START_HOR_OFFSET, targetLane + Y_BASE-LANE_WIDTH,  -1.5f);
		glVertex3f(END_HOR_OFFSET, targetLane + Y_BASE-LANE_WIDTH,  -1.5f);
		glVertex3f(END_HOR_OFFSET, targetLane + Y_BASE+LANE_WIDTH,  -1.5f);
		glVertex3f(START_HOR_OFFSET, targetLane + Y_BASE+LANE_WIDTH,  -1.5f);
	glEnd();

	// draw unused lanes 2, 4, 6, 8, 10
	for( int iLane = 2; iLane <= 10; iLane+=2 )
	{
		if( iLane != targetLane )
		{
	glBegin(GL_QUADS);
		glColor4ub(212, 212, 212, 255);
		glVertex3f(START_HOR_OFFSET, iLane + Y_BASE-LANE_WIDTH,  -1.5f);
		glVertex3f(END_HOR_OFFSET, iLane + Y_BASE-LANE_WIDTH,  -1.5f);
		glVertex3f(END_HOR_OFFSET, iLane + Y_BASE+LANE_WIDTH,  -1.5f);
		glVertex3f(START_HOR_OFFSET, iLane + Y_BASE+LANE_WIDTH,  -1.5f);
	glEnd();
		}

		// Write lane number
		glColor4ub(212, 150, 150, 255);
		glRasterPos2i(END_HOR_OFFSET-1, iLane + Y_BASE);
		char s[3];
		sprintf(  s, "%1d", iLane/2 );
			  glutBitmapCharacter(GLUT_BITMAP_HELVETICA_18, *s);

	}

	// draw background quad	
	glBegin(GL_QUADS);
		glColor4ub(255, 255, 255, 255);
		glVertex3f(START_HOR_OFFSET, START_VER_OFFSET,  -1.5f);
		glVertex3f(END_HOR_OFFSET, START_VER_OFFSET,  -1.5f);
		glVertex3f(END_HOR_OFFSET, END_VER_OFFSET,  -1.5f);
		glVertex3f(START_HOR_OFFSET, END_VER_OFFSET,  -1.5f);
	glEnd();
	
	glBegin(GL_LINES);
		glColor4ub(0, 255, 0, 255);

		// draw grid to provide a sense of scale and motion
		for (r = 0; r <= NUM_INTERVALS; r++)
		{
			glVertex3f(START_HOR_OFFSET, INTERVAL*r + START_VER_OFFSET,  -1.4f);
			glVertex3f(END_HOR_OFFSET, INTERVAL*r + START_VER_OFFSET,  -1.4f);
		}

		// vertical grid bars scroll right to left
		//int speed=5;

		for (c = CAR_SPEED*(int)gameTime; c <= NUM_INTERVALS + CAR_SPEED* ( 1 + (int)gameTime ); c++)
		{
			glVertex3f(INTERVAL*c - CAR_SPEED*gameTime + START_HOR_OFFSET, START_VER_OFFSET, -1.4f);
			glVertex3f(INTERVAL*c- CAR_SPEED*gameTime + START_HOR_OFFSET, END_VER_OFFSET, -1.4f);
		}
	glEnd();

	// text label
	glColor4ub(99, 45, 112, 255);
	glRasterPos2i(START_HOR_OFFSET+3, -9.5);
	for(char *p = "Type 1-5 to change lanes", lines = 0; *p; p++) {
		glutBitmapCharacter(GLUT_BITMAP_HELVETICA_18, *p);
	 }
}

void drawDrivers()
{
	/*
	static vec2 p1 = {0.24f*GLOBAL_SCALE, 0.0f};
	static vec2 p2 = {-0.12f*GLOBAL_SCALE, -0.12f*GLOBAL_SCALE};
	static vec2 p3 = {-0.12f*GLOBAL_SCALE, 0.12f*GLOBAL_SCALE}; */
	static vec2 p1 = {0.48f*GLOBAL_SCALE, 0.0f};
	static vec2 p2 = {-0.24f*GLOBAL_SCALE, -0.24f*GLOBAL_SCALE};
	static vec2 p3 = {-0.24f*GLOBAL_SCALE, 0.24f*GLOBAL_SCALE};
	vec2 t1, t2, t3;

	vector<driver>::iterator theIter;
	for( theIter = driverList.begin(); theIter != driverList.end(); theIter++ )
	{

		// transform points so that triangle "heads" in travel direction
		t1.x =  p1.x * theIter->driverSpeed.x + p1.y * theIter->driverSpeed.y;
		t1.y = -p1.y * theIter->driverSpeed.x + p1.x * theIter->driverSpeed.y;
		t2.x =  p2.x * theIter->driverSpeed.x + p2.y * theIter->driverSpeed.y;
		t2.y = -p2.y * theIter->driverSpeed.x + p2.x * theIter->driverSpeed.y;
		t3.x =  p3.x * theIter->driverSpeed.x + p3.y * theIter->driverSpeed.y;
		t3.y = -p3.y * theIter->driverSpeed.x + p3.x * theIter->driverSpeed.y;
		glBegin(GL_TRIANGLES);
		glColor4ub(theIter->color.r,theIter->color.g,theIter->color.b, 255);

		glVertex3f(theIter->driverPos.x + t1.x, theIter->driverPos.y + t1.y, -1.32f);
		glVertex3f(theIter->driverPos.x + t2.x, theIter->driverPos.y + t2.y, -1.32f);
		glVertex3f(theIter->driverPos.x + t3.x, theIter->driverPos.y + t3.y, -1.32f);
		glEnd();	

	}
}

void redraw()
{
	/* clear stencil each time */
	glClearColor(0.5f, 0.1f, 0.1f, 0.0f);
    glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT|GL_STENCIL_BUFFER_BIT);

	// draw play arena
	drawarena();
	// draw each car
	drawDrivers();

	// drawLanes();
	glutSwapBuffers();
}

float timedelta(void)
{
	static LARGE_INTEGER sFreq;
	static double        sInvFreq = 0;
	static __int64       sCurrentTime = 0L;

	LARGE_INTEGER now;
	if (sCurrentTime == 0L)
	{
		QueryPerformanceFrequency( &sFreq );
		sInvFreq = 1.0f / ((double)sFreq.QuadPart);
		QueryPerformanceCounter( &now );
		sCurrentTime = (__int64)((double)now.QuadPart * sInvFreq * 1000);
	}
	QueryPerformanceCounter( &now );
	__int64 newcurrentTime = (__int64)((double)now.QuadPart * sInvFreq * 1000);
	float dt = 0.001f*(newcurrentTime - sCurrentTime);
	if (dt < 1.0f/60.0f) // clamp between 60hz and 30hz
		dt = 1.0f/60.0f;
	else if (dt > 1.0f/30.0f)
		dt = 1.0f/30.0f;
	sCurrentTime = newcurrentTime;
	return dt;
}

void anim()
{
    glPushMatrix();
	redraw();
    glPopMatrix();

	float dt = timedelta();
	gameTime += dt;
	resetTime += dt;

	/* Navigation loop.  This is the heart of the demo.
	   This is an implementation of the closed-form Equation 1.1 from the 
	   article, with y-coordinate of the car as the output value.  This
	   implicitly assumes that the transfer functions represented by
	   these mode lists are for the entire vehicle including steering
	   controls and driver.  Navigation, steering, and vehicle simulation
	   are all combined in this one loop.
	**/
	vector<driver>::iterator drivIt;
	for( drivIt = driverList.begin(); drivIt != driverList.end(); drivIt++ )
	{
		drivIt->driverPos.x = START_HOR_OFFSET+1;
		drivIt->driverSpeed.x = CAR_SPEED;
		drivIt->driverSpeed.y = 0.0f;
		// Sum of all the oscillatory modes
		drivIt->driverPos.y = targetLane + Y_BASE;
		modeList::iterator theModeIter;
		for( theModeIter = drivIt->modes.begin(); theModeIter != drivIt->modes.end(); theModeIter++ )
		{
			// Add this mode's contribution to position
			float zeta = theModeIter->zeta;
			float omega = theModeIter->omega;
			float A = theModeIter->A;
            // Time conversion from dimensionless units (radians and
            // rad/sec) to full cycles per second
            float t = 2 * PI * resetTime;
			drivIt->driverPos.y += A * exp( -zeta * t ) * cos( omega * t );

			// y velocity is derivative of position
			// In this demo, velocity is only used to calculate the car's heading for rendering.
			drivIt->driverSpeed.y += A * exp( -zeta * t ) *( - zeta * cos( omega * t ) - omega * ( sin( omega * t ) ) );
		}

		// normalize velocity
		float vel = sqrt( CAR_SPEED*CAR_SPEED + drivIt->driverSpeed.y*drivIt->driverSpeed.y ); 
		drivIt->driverSpeed.x /= vel;
		drivIt->driverSpeed.y /= vel;
	}
	
}

int main(int argc, char *argv[])
{
    glutInit(&argc, argv);
    glutInitWindowSize(512, 512);
    glutInitDisplayMode(GLUT_STENCIL|GLUT_DEPTH|GLUT_DOUBLE);
    (void)glutCreateWindow("Control Law Gem");
    glutDisplayFunc(redraw);
    glutKeyboardFunc(key);
    glutIdleFunc(anim);
    glEnable(GL_DEPTH_TEST);
    glMatrixMode(GL_PROJECTION);
    glOrtho(-10., 10., -10., 10., 0., 20.);
    glMatrixMode(GL_MODELVIEW);

    glEnable(GL_CULL_FACE);
    glCullFace(GL_BACK);
    glEnable(GL_COLOR_MATERIAL);

	gameTime = -1.f;

	targetLane = 6;  //center lane

	// create the drivers
	driver d1( 255, 128, 128 );
	/* The red car weaves back and forth once per 2.0 seconds (a bit quick) and is 
	   moderately underdamped  */
	mode dm0 = { 0.5, 0.5, 6. };  // zeta, omega, A

	d1.modes.push_back( dm0 );
	driverList.push_back( d1 );

    driver d2( 90, 123, 228 );   // color	
	/*  The blue car has two modes.  
	    The first mode is critically damped and a little bit faster than the red car's.
	*/
	mode dm1 = { 1.4, 0.7, 8 };         // zeta, omega, A

	/*  The second mode is a rapid oscillation that damps out very slowly.
	*/
	mode dm2 = { 0.25, 1.5, 2 };        // zeta, omega, A
	d2.modes.push_back( dm1 );
	d2.modes.push_back( dm2 );
	driverList.push_back( d2 );

	driver d3( 12, 230, 78 );
	/*  The green car has three modes which are all quickly damped and responsive.  
	    The car's motion is less obviously sine-like because it has multiple modes.		   
	*/
	mode dm3 = { 0.8, 0.75, -3. };      // zeta, omega, A
	mode dm4 = { 2.5, 1.4, 5.8 };       // zeta, omega, A
	mode dm5 = { 2.0, 0.4, -1.9 };      // zeta, omega, A

	d3.modes.push_back( dm3 );
	d3.modes.push_back( dm4 );
	d3.modes.push_back( dm5 );
	driverList.push_back( d3 );

	/** To add more cars, follow this template:

	// make a driver, passing the RGB values of its color
	driver driver_name( r, g, b );

    // make one or more modes (roots of the root locus plot), passing values taken from the
	// root locus plot.  A (amplitude) is not important as it will be overwritten by the 
	// lane-changing algorithm
	mode mode_name = { zeta, omega, A }; 
	... more modes ...

	// Add the mode(s) to the driver's list of modes.
	driver_name.push_back( mode_name );
	... more modes ...

	// Add the driver to the list of drivers.
	driverList.push_back( driver_name );

	*/
	// end creating drivers

	/* initialize random seed: readers may experiment with random parameters */
	srand ( time(NULL) );
    glutMainLoop();
	return 0;
}
/////////////  *********************  ///////////////

