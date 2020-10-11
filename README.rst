*Crobots Java Tournament Manager*
=================================

*Note*: This project is no longer maintained (with the exception of the Pairing sub-module, perhaps) in favour of the Python and Golang alternatives.

Multi-platform Client Java for setup and running of a Crobots's tournament.
It handles MySQL, MS SQL, Oracle databases.

Usage: java -jar Crobots.jar -h hostname -u username -p password -f file -P path -c script -T robot -2 -4 -S -C -t -q -?

  - OS Type : It shows the current O.S.. Default's UNIX for all
    not-Windows systems.
  - path delimitator : "/" default (UNIX). "\" for Windows systems.
  - *-h* MySQL server Hostname or IP. "localhost" is the
    default.
  - *-f fileinput* : Robot's list file. "torneo.dat" is the default.
  - *-c cmd script* : Script batch that manages Crobots and Count.
    Default is "crobots.sh" (UNIX), "crobots.bat" for Windows.
  - *-u username* : Username of db. Default is "crobots".
  - *-p password* : Password of db. Default is "" (empty).
  - *-P path* : Source robots path. Default is "torneo".
  - *-K* : Handles the presence of 'Crobots.stop' file for stopping the
    tournament.
  - *-t* : Tests only the connection (the tournament won't run).
  - *-2* : Runs the F2F only.
  - *-3* : Runs the 3VS3 only.
  - *-4* : Runs the 4VS4 only.
  - *-S* : Loads the database temporary tables. It inhibits the
    tournament's run.
  - *-C* : Cleans the database temporary tables. It inhibits the
    tournament's run.
  - *-T robot* : Configures the database temporary tables for a specified
    robot. Default is "false" (robot's name default is "test").
  - *-q* : Default is "false". Suppress info messages.

Requirements
------------

  - JRE 1.7 or above.
  - Only for server side purposes MySQL 5.0.x, MS SQL 2008+, Oracle 11+

Install
-------

  - If the database is present run the correct SQL script, according to
    your database vendor.
  - Configure (if necessary) all paths into script crobots.sh and/or
    crobots.bat
  - Customize the the configuration file 'Crobots_conf.xml'. Configuration
    parameters could be overwritten by the command line options
  - Copy and execute 'Crobots.jar' via command line java 'java -jar
    Crobots.jar'

Notes
-----

  - Requires Crobots and Count (v.8.3 or above) executables.
  - Using Crobots Java such as remote client only, it is not necessary
    to install any database locally.
  - Switches -T and -C forces the setup (-S).
  - Switches -2 or -4 may be used with -S, -C or -T, for configuring F2F
    or 4vs4 only.
  - For security reasons, the setup's options are allowed only for MySQL
    db user 'root'
  - On Windows systems it is not possibile to run more then one java
    Crobots client using the same batch script (crobots.bat), because of
    temporary log file and results corruption. To avoid this problem,
    create multiple batch scripts which use different log files. *NOTE:
    With the multithreading support you don't need anymore to launch
    more java Crobots instances*.
  - Robots list must have the robot filename without path nor ".r" nor
    ".ro" extension informations.
  - Customize the .sh and .bat batch scripts if needed. You may
    use your own executable binaries path.
  - Please don't touch any command line parameter of the batch scripts.
    You may cause an incorrect client's behavior.

Release notes
-------------

  - v.4.66 : Latest stable version (check the GitHub repository)
  - v.4.00~4.65 : A complete refactoring. Due to its poor performarce and huge
    overhead, the HTTP support is now obsolete (and deprecated).
  - v.3.00 : Extended JConfig xml configuration file support. Multithreading
    support improved.
  - v.2.00 : Multithreading support. Configuration options can be set by
    the Crobots_conf.xml file.
  - v.1.00 : First stable complete version: improvements of PHP backend
    PHP with the concurrent management using transactions. Optionally
    store procedures can be used
  - v.0.99 : HTTP error handling improvement
  - v.0.97 : Add retry management (at least 3 attempts) on HTTP connection
  - v.0.96 : Add new management of shell's process
  - v.0.95 : Add tournament's parameters management (f2f and 4vs4) via
    MySQL. Suppression of -m -n params
  - v.0.94 : Add tournament's parameters management (f2f and 4vs4) via
    HTTP.
  - v.0.93 : Minor optimization and code cleaning
  - v.0.92 : Bug fixing
  - v.0.90 : Add client's version test through the web server
  - v.0.80 : Add MD5 checksum
  - v.0.70 : Add status control of match's sequence
  - v.0.60 : Add logging info through log4j
  - v.0.57 : Bug fix : Incorrect behavior when a single match remains on
    temporary table fixed.
  - v.0.56 : Bug fix : Suppression of a Java error (noise only, not
    tournament running compromising).
  - v.0.55 : PHP db MySQL authentification changed. MySQL user is now
    unique; for any connection it uses 'users' table and 'status' field.
  - v.0.54 : Bug fix of clean option (-C).
  - v.0.51 : HTTP mode. 'kill file' (-K option). Minor bug fix and '-g'
    option deleted. Performance improvement.
  - v.0.42 : First (more or less) stable release, with a full MySQL
    support.

