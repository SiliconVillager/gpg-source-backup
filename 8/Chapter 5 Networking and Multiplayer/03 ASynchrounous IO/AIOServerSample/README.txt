Asynchronous I/O Sample Code
============================

Author: Neil Gower <neilg@vertexblast.com>
Date:   September 2009

This is the sample code that accompanies my article in Game Programming 
Gems 8 about asynchronous I/O for game servers. The AIOServerSample is a
model of a game server, implemented with POSIX AIO, Windows Overlapped I/O,
and synchronous non-blocking I/O for comparison. A client program is also
included, written in Python.

The included license is the formal way of saying that the code is provided 
as-is, you can do what you want with it, and any problems you encounter are 
your own. If you find the code useful, feel free to drop me a note. I'd be 
happy to hear from you.


Requirements:
-------------

Python 2.6 to run the client.

The server program has been compiled and run on Windows XP with Visual
Studio 2009 Express and OpenSolaris with GCC. When this was written, the
state of AIO on Linux was unclear. It may work with the right patches 
applied, but none of the major distributions had working AIO out of the box.
FreeBSD and OS X did not support AIO with sockets.


Building:
---------

AIOServerSample.sln is the Visual Studio solution which contains the server
project. 

There is a (GNU) Makefile for building on Unix-like systems with GCC. The 
linker flags may need to be changed to suit the particular platform you are
building for, the default values provided are for Open Solaris.


Running:
--------

See the comments in main.cpp (or the generated HTML documentation) about
variables you can adjust for running the server. Basic usage is:

On the server machine    : AioSampleServer 
                   or    : AioSampleServer syncIO
On the client machine(s) : python simulate_game_clients.py <server addr>

After the server has run for the test duration, it will close the
connections to the clients and print stats to stdout. Launching the server
with (any) command line arguments will cause it to use synchronous I/O
rather than asynchronous I/O. There are cases where the clients or server
report errors on shutdown. Any errors that occur after the run stats are
reported have no effect on the test results.
