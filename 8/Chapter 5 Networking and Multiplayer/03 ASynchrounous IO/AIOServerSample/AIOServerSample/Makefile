#
# This is the Makefile for building AioServerSample on POSIX.
#
OBJS = main.o Socket.o GameInstance.o debug.o \
       posix/SocketImpl.o posix/platform.o

# optimized:
CFLAGS   = -Wall -O2 -pedantic -Werror
CPPFLAGS = -I posix -D NDEBUG

# debug:
#CFLAGS   = -Wall -g -pedantic -Werror
#CPPFLAGS = -I posix

# OpenSolaris:
LIBS     = -lrt -lsocket
# Linux:
#LIBS = -lrt

ALL: AioServerSample

AioServerSample : depend ${OBJS} 
	${CXX} -o $@ ${OBJS} ${LIBS}

posix/%.o :
	cd posix; make $(@F) "CFLAGS=${CFLAGS}" "CPPFLAGS=${CPPFLAGS}"

depend:
	${CXX} -MM ${CPPFLAGS} -U SYNCH_IO_LEAN_AND_MEAN \
	  *.cpp > depend;
	${CXX} -MM ${CPPFLAGS} -MT posix/platform.o \
	  posix/platform.cpp >> depend;
	${CXX} -MM ${CPPFLAGS} -MT posix/SocketImpl.o \
	  posix/SocketImpl.cpp >> depend;

clean:
	rm -f *.o depend AioServerSample; 
	cd posix; make clean

.cpp.o :
	${CXX} ${CFLAGS} ${CPPFLAGS} -c $<

-include depend
