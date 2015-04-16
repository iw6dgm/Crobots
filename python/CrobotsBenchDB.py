#!/usr/bin/python -O
# -*- coding: UTF-8 -*-

"""
"CROBOTS" Crobots Batch Bench Manager to test one single robot with DataBase
  support

Version:        Python/1.3

                Derived from CrobotsDB.py 1.1 and CrobotsBench.py 1.0

Author:         Maurizio Camangi

Version History:
                Version 1.3 Save match list into dbase using a custom key
                Version 1.2 Count Python support - polish code
                Patch 1.1.1 Use os.devnull
                Version 1.1 Use shutil and glob to build log files
                Version 1.0 is the first stable version

"""

import sys
import imp
import shlex
import subprocess
import time
import shelve
import os.path
from itertools import combinations
from random import shuffle
from shutil import copyfileobj
from glob import iglob
from Count import parse_log_file
from CrobotsLibs import available_cpu_count

# Global configuration variables
# databases
STATUS_KEY = '__STATUS__'
dbase = None
matches = {'f2f': 1, '3vs3': 2, '4vs4': 3}

# default stdin and stderr for crobots executable
devNull = open(os.devnull)

# command line strings
robotPath = "%s/%s.ro"
crobotsCmdLine = "crobots -m%s -l200000 %s"

# if True overrides the Configuration class parameters
overrideConfiguration = False

# number of CPUs / cores
CPUs = available_cpu_count()
print "Detected %s CPU(s)" % CPUs
spawnList = []


def run_crobots(tmppath, logpath, logfile, logtype):
    "spawn crobots command lines in subprocesses"
    global spawnList
    procs = []
    # spawn processes
    for i, s in enumerate(spawnList):
        try:
            with open("%s/tmp_%s_%s_%s.log" % (tmppath, logfile, i, logtype), 'w') as tmpfile:
                procs.append(subprocess.Popen(shlex.split(s), stderr=devNull, stdout=tmpfile))
        except OSError, e:
            print e
            raise SystemExit
    # wait
    for proc in procs:
        proc.wait()
    # check for errors
    if any(proc.returncode != 0 for proc in procs):
        print 'Something failed!'
        raise SystemExit
    # aggregate log files
    try:
        with open('%s/%s_%s.log' % (logpath, logfile, logtype), 'a') as destination:
            logfiles = 'tmp_%s_*_%s.log' % (logfile, logtype)
            for filename in iglob(os.path.join(tmppath, logfiles)):
                copyfileobj(open(filename, 'r'), destination)
                clean_up_log_file(filename)
    except OSError, e:
        print e
        raise SystemExit
    update_db(logpath, logfile, logtype)
    spawnList = []


def spawn_crobots_run(cmdLine, tmppath, logpath, logfile, logtype):
    "put command lines into the buffer and run"
    global spawnList, CPUs
    spawnList.append(cmdLine)
    if len(spawnList) == CPUs:
        run_crobots(tmppath, logpath, logfile, logtype)



def check_stop_file_exist():
    "check the stop file existance"
    if os.path.exists('Crobots.stop'):
        return True
    return False


def clean_up_log_file(filepath):
    "remove log file"
    try:
        os.remove(filepath)
    except:
        pass


def build_crobots_cmdline(paramCmdLine, robotList, tmppath, logpath, logfile, logtype):
    "build and run crobots command lines"
    shuffle(robotList)
    spawn_crobots_run(" ".join([paramCmdLine] + robotList), tmppath, logpath, logfile, logtype)


def load_from_file(filepath):
    "Load configuration py file with tournament parameters"
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


# initialize database
def init_db(logfile, logtype):
    global configuration, startStatus, dbase
    dbfile = 'db/%s_%s.db' % (logfile, logtype)
    if not os.path.exists(dbfile):
        dbase = shelve.open(dbfile, 'c')
        dbase[os.path.basename(robotTest)[:-3]] = [0, 0, 0, 0]
        for s in configuration.listRobots:
            key = os.path.basename(s)
            dbase[key] = [0, 0, 0, 0]
        dbase.sync()
    else:
        dbase = shelve.open(dbfile, 'w')


# update database
def update_db(logpath, logfile, logtype):
    global dbase
    log = '%s/%s_%s.log' % (logpath, logfile, logtype)
    if not os.path.exists(log):
        print log + ' does not exists!'
        close_db()
        raise SystemExit
    txt = open(log, 'r')
    lines = txt.readlines()
    txt.close()
    robots = parse_log_file(lines)
    for r in robots.values():
        name = r[0]
        values = dbase[name]
        values[0] += r[1]
        values[1] += r[2]
        values[2] += r[3]
        values[3] += r[4]
        dbase[name] = values
    dbase.sync()
    clean_up_log_file(log)


def close_db():
    global dbase, dbstatus
    if dbase is not None:
        try:
            dbase.close()
        except:
            print "Error on closing local database: results may be corrupted..."
        finally:
            dbase = None


# initialize status
def init_status(logtype):
    global dbase
    if not STATUS_KEY in dbase:
        print "Init local status database for %s" % logtype.upper()
        l = list(combinations(configuration.listRobots, matches[logtype]))
        shuffle(l)
        dbase[STATUS_KEY] = l
        dbase.sync()


# save current status
def save_status(l):
    global dbase
    dbase[STATUS_KEY] = l
    dbase.sync()


# clean up database and status files
def cleanup(logfile, logtype):
    for s in ['db/%s_%s.db', 'db/status_%s_%s.txt']:
        clean_up_log_file(s % (logfile, logtype))
    print 'Clean up done %s %s!' % (logfile, logtype)


if len(sys.argv) <> 4:
    print "Usage : CrobotsBenchDB.py <conf.py> <robot.r> [f2f|3vs3|4vs4|all|test|clean]"
    raise SystemExit

confFile = sys.argv[1]
robotTest = sys.argv[2]
action = sys.argv[3]

# Temp and Log dir: configurable if you want
uid = os.getuid()
print 'Found UID %s' % uid
tmpfs = '/run/user/%s/crobots' % uid
logpath = '%s/log' % tmpfs
tmppath = '%s/tmp' % tmpfs

print 'Setup temp directories...'
try:
    if not os.path.exists(logpath):
        os.makedirs(logpath)
    if not os.path.exists(tmppath):
        os.makedirs(tmppath)
except Exception, e:
    print e
    print 'Unable to create temp %s and %s' % (logpath, tmppath)
    raise SystemExit

if not os.path.exists(confFile):
    print 'Configuration file %s does not exist' % confFile
    raise SystemExit

if not action in ['f2f', '3vs3', '4vs4', 'all', 'test', 'clean']:
    print 'Invalid parameter %s. Valid values are f2f, 3vs3, 4vs4, all, test, clean' % action
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
    if not os.path.exists(robot):
        print 'Robot file %s does not exist.' % robot
        sys.exit(1)

print 'OK!'

if action == 'clean':
    for a in ['f2f', '3vs3', '4vs4']:
        cleanup(configuration.label, a)
    raise SystemExit

if not os.path.exists(robotTest):
    print 'Robot %s does not exist' % robotTest
    raise SystemExit
else:
    print 'Compiling %s ...' % robotTest,
    clean_up_log_file(robotTest + 'o')
    os.system("crobots -c %s </dev/null >/dev/null 2>&1" % robotTest)
    if not os.path.exists(robotTest + 'o'):
        print 'Robot %s does not compile' % robotTest
        raise SystemExit

print 'OK!'
robotTest += 'o'

if action == 'test':
    print 'Test completed!'
    raise SystemExit


if check_stop_file_exist():
    print 'Crobots.stop file found! Exit application.'
    close_db()
    raise SystemExit


def run_tournament(ptype, matchParam):
    global dbase, tmppath, logpath, robotPath, robotTest, configuration, crobotsCmdLine
    print '%s Starting %s... ' % (time.ctime(), ptype.upper())
    clean_up_log_file('%s/%s_%s.log' % (logpath, configuration.label, ptype))
    param = crobotsCmdLine % (matchParam, robotTest)
    init_db(configuration.label, ptype)
    init_status(ptype)
    match_list = dbase[STATUS_KEY]
    list_length = len(match_list)
    counter = 0
    while counter < list_length:
        if check_stop_file_exist():
            break

        build_crobots_cmdline(param, [robotPath % (configuration.sourcePath, s) for s in match_list.pop()], tmppath, logpath, configuration.label, ptype)
        counter += 1
    if len(spawnList) > 0:
        run_crobots(tmppath, logpath, configuration.label, ptype)
    if check_stop_file_exist():
        save_status(match_list)
        close_db()
        print 'Crobots.stop file found! Exit application.'
        raise SystemExit
    save_status(match_list)
    print '%s %s completed!' % (time.ctime(), ptype.upper())


if action in ['f2f', 'all']:
    run_tournament('f2f', configuration.matchF2F)

if action in ['3vs3', 'all']:
    run_tournament('3vs3', configuration.match3VS3)

if action in ['4vs4', 'all']:
    run_tournament('4vs4', configuration.match4VS4)


close_db()
devNull.close()

