#!/usr/bin/python -O
# -*- coding: UTF-8 -*-

"""
"CROBOTS" Crobots Batch Tournament Manager

Version:        Python/1.5

                Translated from 'run2012.sh' UNIX/bash to Python 2.7

Author:         Maurizio Camangi

Version History:
                Version 1.5 Count Python support
                Version 1.4.2 Polish code - use os.devnull - no more </dev/null needed
                Patch 1.3.1 Fix import
                Version 1.3 Use more compact build_crobots_cmdline function
                
                Version 1.2 Use more compact combinations from itertools

                Version 1.1 is the first stable version
                (corresponds to bash/v.1.2c)
                
                Add multiple CPUs / cores management

"""

import sys
import imp
import os.path
import shlex
import subprocess
import time
from random import shuffle
from itertools import combinations
from Count import parse_log_file, show_report

# Global configuration variables

# default stdin and stderr for crobots executable
devNull = open(os.devnull)

# command line strings
robotPath = "%s/%s.ro"
crobotsCmdLine = "crobots -m%s -l200000"
tmpLogFiles = "/tmp/tmp_%s_%s_%s.log"
catTmpLogFiles = "cat /tmp/tmp_%s_*_%s.log >>log/%s_%s.log"
logFilePath = "log/%s_%s.log"

# if True overrides the Configuration class parameters
overrideConfiguration = False

# number of CPUs / cores
CPUs = int(os.getenv('NUMBER_OF_PROCESSORS', '2'))
print "Detected %s CPU(s)" % CPUs
spawnList = []


def run_crobots(logfile, logtype):
    """spawn crobots command lines in subprocesses"""
    global spawnList
    procs = []
    # spawn processes
    for i, s in enumerate(spawnList):
        try:
            tmpfile = open(os.path.normpath(tmpLogFiles % (logfile, i, logtype)), 'w')
            procs.append(subprocess.Popen(shlex.split(s), stderr=devNull, stdout=tmpfile))
        finally:
            tmpfile.close()
    # wait
    for proc in procs:
        proc.wait()
    # check for errors
    if any(proc.returncode != 0 for proc in procs):
        print 'Something failed!'
        raise SystemExit
    # aggregate log files
    try:
        os.system(catTmpLogFiles % (logfile, logtype, logfile, logtype))
    except OSError, e:
        print e
        raise SystemExit
    # clean up temporary log files
    for i in xrange(len(spawnList)):
        clean_up_log_file(tmpLogFiles % (logfile, i, logtype))
    spawnList = []


def spawn_crobots_run(cmdLine, logfile, logtype):
    """put command lines into the buffer and run"""
    global spawnList, CPUs
    spawnList.append(cmdLine)
    if len(spawnList) == CPUs:
        run_crobots(logfile, logtype)


def run_count(logfile, logtype):
    """run the count log parser"""
    try:
        logFile = logFilePath % (logfile, logtype)
        txt = open(logFile, 'r')
        lines = txt.readlines()
        txt.close()

        robots = parse_log_file(lines)
        show_report(robots)

        clean_up_log_file(logFile)
    except OSError, e:
        print e


def check_stop_file_exist():
    """check the stop file existance"""
    if os.path.exists('Crobots.stop'):
        print 'Crobots.stop found! Exit application.'
        raise SystemExit


def clean_up_log_file(filepath):
    """remove log file"""
    try:
        os.remove(os.path.normpath(filepath))
    except:
        pass


def build_crobots_cmdline(paramCmdLine, robotList, logfile, logtype):
    """build and run crobots command lines"""
    shuffle(robotList)
    spawn_crobots_run(" ".join([paramCmdLine] + robotList), logfile, logtype)


def load_from_file(filepath):
    """Load configuration py file with tournament parameters"""
    class_inst = None
    expected_class = 'Configuration'

    mod_name, file_ext = os.path.splitext(os.path.split(filepath)[-1])

    if file_ext.lower() == '.py':
        py_mod = imp.load_source(mod_name, filepath)
    elif file_ext.lower() == '.pyc' or file_ext.lower() == '.pyo':
        py_mod = imp.load_compiled(mod_name, filepath)
    else:
        return class_inst

    if hasattr(py_mod, expected_class):
        class_inst = py_mod.Configuration()

    return class_inst


if len(sys.argv) <> 3:
    print "Usage : Crobots.py <conf.py> [f2f|3vs3|4vs4|all|test]"
    raise SystemExit

confFile = sys.argv[1]
action = sys.argv[2]

if not os.path.exists(confFile):
    print 'Configuration file %s does not exist' % confFile
    raise SystemExit

if not action in ['f2f', '3vs3', '4vs4', 'all', 'test']:
    print 'Invalid parameter %s. Valid values are f2f, 3vs3, 4vs4, all, test' % action
    raise SystemExit

try:
    configuration = load_from_file(confFile)
except Exception, e:
    print e
    print 'Invalid configuration py file %s' % confFile
    raise SystemExit

if configuration is None:
    print 'Invalid configuration py file %s' % confFile
    raise SystemExit

if len(configuration.listRobots) == 0:
    print 'List of robots empty!'
    raise SystemExit

if overrideConfiguration:
    print 'Override configuration...'
    configuration.label = 'test'
    configuration.matchF2F = 500
    configuration.match3VS3 = 8
    configuration.match4VS4 = 1
    configuration.sourcePath = 'test'

print 'List size = %d' % len(configuration.listRobots)
print 'Test opponents... ',

for r in configuration.listRobots:
    robot = robotPath % (configuration.sourcePath, r)
    if not os.path.exists(os.path.normpath(robot)):
        print 'Robot file %s does not exist.' % robot
        sys.exit(1)

print 'OK!'

if action == 'test':
    print 'Test completed!'
    raise SystemExit


def run_tournament(ptype, num, matchParam):
    print '%s Starting %s... ' % (time.ctime(), ptype.upper())
    clean_up_log_file(logFilePath % (configuration.label, ptype))
    param = crobotsCmdLine % matchParam
    for r in combinations(configuration.listRobots, num):
        check_stop_file_exist()
        build_crobots_cmdline(param, [robotPath % (configuration.sourcePath, s) for s in r], configuration.label, ptype)
    if len(spawnList) > 0: run_crobots(configuration.label, ptype)
    run_count(configuration.label, ptype)
    print '%s %s completed!' % (time.ctime(), ptype.upper())


if action in ['f2f', 'all']:
    run_tournament('f2f', 2, configuration.matchF2F)

if action in ['3vs3', 'all']:
    run_tournament('3vs3', 3, configuration.match3VS3)

if action in ['4vs4', 'all']:
    run_tournament('4vs4', 4, configuration.match4VS4)

devNull.close()
