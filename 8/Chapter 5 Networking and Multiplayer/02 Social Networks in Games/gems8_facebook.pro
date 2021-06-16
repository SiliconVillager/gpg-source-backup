# -------------------------------------------------
# Project created by QtCreator 2009-08-28T12:48:07
# -------------------------------------------------
QT += network \
    opengl \
    webkit \
    xml
TARGET = gems8_facebook
TEMPLATE = app
VPATH += facebook
release:OBJECTS_DIR = ./release
release: DESTDIR = ./release
debug:OBJECTS_DIR = ./debug
debug: DESTDIR = ./debug
SOURCES += main.cpp \
    mainwindow.cpp \
    FacebookLoginDialog.cpp \
    FacebookService.cpp \
    FacebookServiceUtils.cpp \
    FacebookCreateToken.cpp \
    FacebookGetSession.cpp \
    FacebookSendMessage.cpp \
    FacebookGetLoggedInUser.cpp \
    FacebookGetAppUsers.cpp \
    FacebookGetFriends.cpp
HEADERS += mainwindow.h \
    FacebookLoginDialog.h \
    FacebookServiceStates.h \
    FacebookService.h \
    FacebookServiceUtils.h \
    FacebookCreateToken.h \
    FacebookGetSession.h \
    FacebookSendMessage.h \
    FacebookGetLoggedInUser.h \
    FacebookGetAppUsers.h \
    FacebookGetFriends.h
FORMS += mainwindow.ui \
    FacebookLoginDialog.ui

CONFIG += x86
