#!/usr/bin/python -O
# -*- coding: UTF-8 -*-

"""
"CROBOTS" Crobots Batch Tournament Manager with DataBase support

Version:        Python/3.2

                Derived from Crobots.py 1.3

Author:         Maurizio Camangi

Version History:
                Version 3.2 Return error code on SystemExit after Exception
                Version 3.1 Save match list into dbase using a custom key
                Version 3.0 Added support for Count Python
                Version 2.1 Added MySQL support - polish code
                Version 2.0.2 Polish code - use os.devnull
                Version 2.0 Use /run/user/{userid}/crobots as temp and log dir
                Version 1.2 Use shutil and glob to build log files
                Version 1.1 more compact iterations / combinations with
                start / end offset

                Version 1.0 is the first stable version

"""

import sys
import imp
import shlex
import subprocess
import time
import shelve
import os.path
from random import shuffle
from itertools import combinations
from shutil import copyfileobj
from glob import iglob
import CrobotsLibs
from Count import parse_log_file

# Global configuration variables

# databases
STATUS_KEY = '__STATUS__'
dbase = None

# default stdin and stderr for crobots executable
devNull = open(os.devnull)

# command line strings
robotPath = "%s/%s.ro"
crobotsCmdLine = "crobots -m%s -l200000"
# countCmdLine = "count -p -t %s/%s_%s >/dev/null 2>&1"
dbfile = "db/%s_%s.db"
matches = {'f2f': 2, '3vs3': 3, '4vs4': 4}

# if True overrides the Configuration class parameters
overrideConfiguration = False

# number of CPUs / cores
CPUs = CrobotsLibs.available_cpu_count()
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
            raise SystemExit(e)
    # wait
    for proc in procs:
        proc.wait()
    # check for errors
    if any(proc.returncode != 0 for proc in procs):
        raise SystemExit('Something failed!')
    # aggregate log files
    try:
        with open('%s/%s_%s.log' % (logpath, logfile, logtype), 'a') as destination:
            logfiles = 'tmp_%s_*_%s.log' % (logfile, logtype)
            for filename in iglob(os.path.join(tmppath, logfiles)):
                copyfileobj(open(filename, 'r'), destination)
                clean_up_log_file(filename)
    except OSError, e:
        raise SystemExit(e)
    update_db(logpath, logfile, logtype)
    spawnList = []


def spawn_crobots_run(tmppath, cmdLine, logpath, logfile, logtype):
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


def build_crobots_cmdline(tmppath, paramCmdLine, robotList, logpath, logfile, logtype):
    "build and run crobots command lines"
    shuffle(robotList)
    spawn_crobots_run(tmppath, " ".join([paramCmdLine] + robotList), logpath, logfile, logtype)


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
    global configuration, dbase, dbfile
    filename = dbfile % (logfile, logtype)
    if not os.path.exists(filename):
        print "Init local database for %s" % logtype.upper()
        dbase = shelve.open(filename, 'c')
        for s in configuration.listRobots:
            key = os.path.basename(s)
            dbase[key] = [0, 0, 0, 0]
        dbase.sync()
    else:
        dbase = shelve.open(filename, 'w')
    CrobotsLibs.test_connection()


def close_db():
    global dbase
    if dbase is not None:
        try:
            dbase.close()
        except:
            print "Error on closing local database: results may be corrupted..."
        finally:
            dbase = None
    CrobotsLibs.close_connection()


# initialize status
def init_status(logtype):
    global dbase
    if not STATUS_KEY in dbase:
        print "Init local status database for %s" % logtype.upper()
        l = list(combinations(configuration.listRobots, matches[logtype]))
        shuffle(l)
        dbase[STATUS_KEY] = l
        dbase.sync()
        CrobotsLibs.set_up(logtype)


# update database
def update_db(logpath, logfile, logtype):
    global dbase
    log = '%s/%s_%s.log' % (logpath, logfile, logtype)
    if not os.path.exists(log):
        close_db()
        raise SystemExit('%s does not exists!' % log)
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
        if CrobotsLibs.DATABASE_ENABLE:
            CrobotsLibs.update_results(logtype, name, values[0], values[1], values[2], values[3])
    dbase.sync()
    clean_up_log_file(log)


# save current status
def save_status(l):
    global dbase
    dbase[STATUS_KEY] = l
    dbase.sync()


# clean up database
def cleanup(logfile, logtype):
    global dbfile
    clean_up_log_file(dbfile % (logfile, logtype))
    print 'Clean up done %s %s' % (logfile, logtype.upper())


if len(sys.argv) <> 3:
    raise SystemExit('Usage : CrobotsDB.py <conf.py> [f2f|3vs3|4vs4|all|test|setup|clean]')

confFile = sys.argv[1]
action = sys.argv[2]

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
    raise SystemExit('Unable to create temp %s and %s: %s' % (logpath, tmppath, e))

if not os.path.exists(confFile):
    raise SystemExit('Configuration file %s does not exist' % confFile)

if not action in ['f2f', '3vs3', '4vs4', 'all', 'test', 'setup', 'clean']:
    raise SystemExit('Invalid parameter %s. Valid values are f2f, 3vs3, 4vs4, all, test, setup, clean' % action)

try:
    configuration = load_from_file(confFile)
except Exception, e:
    raise SystemExit('Invalid configuration py file %s: %s' % (confFile, e))

if configuration is None:
    raise SystemExit('Invalid configuration py file %s' % confFile)

if len(configuration.listRobots) == 0:
    raise SystemExit('List of robots empty!')

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
        raise SystemExit('Robot file %s does not exist.' % robot)

print 'OK!'

if action == 'clean':
    for a in ['f2f', '3vs3', '4vs4']:
        cleanup(configuration.label, a)
        if CrobotsLibs.DATABASE_ENABLE: CrobotsLibs.clean_up(a)
    close_db()
    raise SystemExit

if action == 'setup':
    for a in ['f2f', '3vs3', '4vs4']:
        cleanup(configuration.label, a)
        init_db(configuration.label, a)
        init_status(a)
    close_db()
    raise SystemExit

if action == 'test':
    CrobotsLibs.test_connection()
    CrobotsLibs.close_connection()
    print 'Test completed!'
    raise SystemExit

if check_stop_file_exist():
    print 'Crobots.stop file found! Exit application.'
    close_db()
    raise SystemExit


def run_tournament(ptype, matchParam):
    global tmppath, logpath, robotPath, configuration, crobotsCmdLine
    print '%s Starting %s... ' % (time.ctime(), ptype.upper())
    clean_up_log_file('%s/%s_%s.log' % (logpath, configuration.label, ptype))
    param = crobotsCmdLine % matchParam
    init_db(configuration.label, ptype)
    init_status(ptype)
    match_list = dbase[STATUS_KEY]
    list_length = len(match_list)
    counter = 0
    while counter < list_length:
        if check_stop_file_exist():
            break
        build_crobots_cmdline(tmppath, param, [robotPath % (configuration.sourcePath, s) for s in match_list.pop()],
                              logpath,
                              configuration.label, ptype)
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
    close_db()


if action in ['f2f', 'all']:
    run_tournament('f2f', configuration.matchF2F)

if action in ['3vs3', 'all']:
    run_tournament('3vs3', configuration.match3VS3)

if action in ['4vs4', 'all']:
    run_tournament('4vs4', configuration.match4VS4)

close_db()
devNull.close()
