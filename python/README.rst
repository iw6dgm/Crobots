Crobots Batch Tournament Manager in Python
==========================================

Install
-------

Uncompress CrobotsPy.tgz in your Crobots folder. Make sure that the 'crobots' and
'count' executables are reachable from your current shell (e.g. in $PATH env).

Setup
-----

According to your hardware / architecture, edit the Crobots.py and CrobotsDB.py scripts and set the correct CPUs variable with your CPU / core numbers. Unless you want to limit the CPU load you might use all of CPUs / cores available.
If necessary, change the first script line with the correct Python executable path (e.g. *#!/usr/bin/env python*).
Make sure that your Crobots folder contains the *conf* and *db* directories.

Configuration and run
---------------------

Create a .py configuration file with the following rules:

* Class name *Configuration*

* Variable *label* for the tournament name; it'll be also the name of the result files (each one with the suffix *_f2f* *_3vs3* *_4vs4*)

* Variables *matchF2F*, *match3VS3*, *match4VS4* with the match repetition factors.

* Variable *sourcePath* with the path of your .ro robot binaries (it could be '.' and you may specify the path within the robot name)

* List *listRobots* with the list of your .ro robot binaries (they may contain a full path, if you set '.' as *sourcePath*)

* Save the configuration file in your *conf* path 

Compatibility
-------------

Successfully tested with Python 2.6.6 and 2.7.3.