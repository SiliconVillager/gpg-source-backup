Wintab Demo
===========

Author: Neil Gower <neilg@vertexblast.com>
Date:   September 2009

This is the sample code that accompanies my article in Game Programming 
Gems 8 about developing software for use with digitizer tablets. It is 
intended to help you get started writing your own tablet code, though isn't 
really meant for production use itself. 

The included license is the formal way of saying that the code is provided 
as-is, you can do what you want with it, and any problems you encounter are 
your own. If you find the code useful, feel free to drop me a note. I'd be 
happy to hear from you.


Requirements:
-------------

The code has been developed for Windows XP and Visual C++ 2008 (Express 
Edition). Before you start, you'll need to get the Wintab SDK. At the time of
writing, it could be found on Wacom's developer site at: 

http://www.wacomeng.com/devsupport/index.html

The project files assume you've put the SDK in $(SolutionDir)/Wintab_SDK/,
with a directory layout like:

Wintab_SDK/
    include/
    lib/i386/

Of course you can change this in the project settings if you like.

To run the application, you'll need a tablet driver installed. To do anything
interesting, you'll also need a tablet. I've tested it with several Wacom 
Intuos tablets. In principle, other tablets from other manufacturers should
work. If not, TabletInterface.cpp is the place to start looking to add 
support for your tablet.


Gotchas:
--------
* I had problems with the 6.10 and 6.11 Wacom drivers not sending 
  WT_CSRCHANGE messages. Rolling back to the 6.08 drivers solved this for me.

* The project currently links to WINTAB32.LIB, which implicitly loads 
  WINTAB32.dll. Recommended best practice is to explicitly load the DLL,
  but this requires quite a bit of boilerplate code to redefine the Wintab
  API using function pointers.
