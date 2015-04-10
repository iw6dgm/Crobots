Crobots Batch Tournament Manager in Python
==========================================

Install
-------

Clone this repository. Make sure that 'crobots' executable is runnable from your current shell (e.g. in $PATH env).

Setup
-----

These Python scripts try to auto-configure so you may not need any further change. If you do, edit your script and set the correct environment:

* If you get an error whilst a Crobots*.py script tries to detect the number of available CPUs / cores, set manually the environment variable NUMBER_OF_PROCESSORS (e.g. export NUMBER_OF_PROCESSORS=4 in your shell).

* Set to a non-null value the environment variable CROBOTS_MYSQL if you want to make active the native MySQL support (you may need to install MySQLdb Python library) and edit CrobotsLibs.py accordingly with your database credentials.

* If you don't want / don't have correctly configured a tmpfs pointing to /run/user/$UID (where $UID is your local user ID) edit your Crobots*.py scripts and change the tmpfs variable according to your current UNIX configuration. WARNING: For long lasting elaborations which have a huge number of iterations and do not use a local database (e.g. Crobots.py, CrobotsBench.py) tmpfs (/run/user) filesystem disk space may be not enough. Think about an alternative folder instead (e.g. /tmp).

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

Successfully tested with Python 2.7.x on Linux Ubuntu.