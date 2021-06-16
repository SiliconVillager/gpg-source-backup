Control Law Racers
The demo program on the book CD shows one way to apply the ideas behind a root-locus analysis in a game.  Rather than modeling a real system and then analyzing it, the demo goes the other way by asking what behavior a system ought to have and then displaying it, without actually doing a model.

The demo shows several slot cars moving down a track.  The simulation uses a side-scrolling format, so there is just one degree of freedom:  y position.  When the player tells them to change lanes, the cars swerve abruptly and then maneuver into the new lane.  Their steering is a bit weak compared to the weight of the cars, so they weave back and forth before finding their target.  What is interesting is that every car is a little bit different and steers differently.  The steering behavior of the cars is encapsulated in the mode structure, which contains the frequencies and damping rates of their motions.

    struct mode {
      float zeta;  // damping coefficient
      float omega; // frequency
      float A;     // Initial amplitude of wave
    };

Each mode supplies one of the terms in Equation (1.1).  Each car can have any number of modes.  There is no steering or simulation loop in this demo; the control law models both the physics of the car itself and the actions of its driver.  Since a car with only one mode is no more complex than the weight and spring of Example 1, this may be a simple autopilot indeed.

To design your car's control law, add modes.  Imagine a root-locus plot like Figure 7.  But instead of plotting a real function, just draw one or more X's for the roots.  Think about how well you want your car to handle and how quickly you want it to sway.  A real car weaving back and forth on the road may move with a period of 1-5 seconds or so; that is, an omega of 0.2 - 1.0.  As for damping, if you want your car to keep shimmying for a long time then give it a zeta close to 0; if you want it to handle well and accurately then give it a large zeta.  A critical damping ratio of zeta = 2 * omega will produce fastest settling.

The in-code comments explain how to add modes to your car.  The step after that, giving an actual steering command by setting the A for each mode, is handled arbitrarily by the demo.  The next step of interest is the navigation step.  This is where the values in the car's modes are applied to Equation (1.1).

    vector<driver>::iterator drivIt;

    // <for-each> car
    for( drivIt = driverList.begin();
         drivIt != driverList.end();
         drivIt++ )
    {
        // Sum of all the oscillatory modes

        // measuring distance from target lane implicitly commands the 
        // car to go there when all oscillations have settled
        drivIt->driverPos.y = targetLane + Y_BASE;

        modeList::iterator theModeIter;
        // <for-each> mode
        for( theModeIter = drivIt->modes.begin();
             theModeIter != drivIt->modes.end();
             theModeIter++ )
        {
            // Add this mode's contribution to position
            float zeta = theModeIter->zeta;
            float omega = theModeIter->omega;
            float A = theModeIter->A;
            // Time conversion from dimensionless units (radians and
            // rad/sec) to full cycles per second
            float t = 2.0 * PI * resetTime;
            drivIt->driverPos.y += A * exp(-zeta * t) * cos(omega * t);
        }
    }

The last statement computes the output position driverPos.y directly, without going through a simulation step.

You will have to go into source code to change the cars.  Try adding more cars and changing modes.  See what happens when a car's modes include both large and small damping coefficients.
Conclusion
The control theory presented in this article is the so-called classical control theory, based on linear systems, that was current practice in the aerospace and electronics industries from roughly the 1930s through the 1960s, and has been eclipsed by the rise of computers but has certainly not gone away.  It’s a bit surprising that such a well-established body of knowledge is so forgotten in the game industry. This article is meant to introduce programmers and designers to the concepts of control theory as well as to present one way to simulate and direct physical objects.  Other applications of these ideas are a virtually unexplored field for the industry.
