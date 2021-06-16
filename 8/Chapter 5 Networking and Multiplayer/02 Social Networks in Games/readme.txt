=====================================================================
= Social Networks in Games: Playing with Your Facebook Friends      =
= Game Programming Gems 8                                           =
= by Claus Höfele <claus@claushoefele.com>                          =
=                                                                   =
= Facebook Demo v1.0 (October 1, 2009)                              =
=====================================================================

For up-to-date information regarding this article, please visit the publisher's web site (http://www.gameprogramminggems.com/) and my homepage (http://www.claushoefele.com).

1. Welcome
=======================

This code package is the demo application for the above mentioned article in Game Programming Gems 8. The demo contains a C++ interface to access Facebook. The requests that are implemented are: login (two methods: one to use an integrated browser and one to login with an external browser), query current user, get friends, get app users, and post message to the Facebook page.

The code was developed using Qt 4.5.2 and tested on Mac OS X 10.6.1 as well as Windows XP SP3. The main reason to use Qt was to simplify embedding a browser (WebKit) into the demo.

I hope you find this project useful.

2. Building from source
=======================

Before building this project, you will have to install the Qt SDK (http://qt.nokia.com/downloads) - the LGPL/free version is sufficient. I suggest you download the complete development environment, including the Qt Creator (Nokia's IDE), and build the demo by loading gems8_facebook.pro.
When you compile from source, you have to use your own Facebook API key and secret, which you'll get when setting up a Facebook application (see http://developers.facebook.com/get_started.php). Make sure you configure the Canvas Callback URL and set the Application Type to Desktop. A compiler error when you build the project will tell where to put the API key and the secret.

3. Login
=======================

The demo supports two login mechanisms: login with an integrated browser (WebKit) and login with an external browser (whatever is set to the default browser on your system). You can choose between the two by checking or unchecking the box next to the login button.

When using an external browser, you'll have to go back to the demo application and confirm the session by clicking the Confirm session button. The demo application will send you to the browser a second time to confirm the permission required to post a message to your Facebook page.

Logging in with an integrated browser will simply open up a dialog where you can enter you Facebook user name and password as well as confirm the permission.

Note that posting a message to your Facebook page will make that message appear on your wall.

4. License
=======================

I release the code in this demo into the public domain. I'd be happy if you credited me as the original author when using the code, but it's up to you.

