#!/usr/bin/python -O
# -*- coding: UTF-8 -*-

"""
"CROBOTS" Crobots Batch Tournament Manager with DataBase support

Version:        Python/2.1

                Derived from Crobots.py 1.3

Author:         Maurizio Camangi

Version History:
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


# Global configuration variables

# databases
dbase = None
dbstatus = None

# default stdin and stderr for crobots executable
devNull = open(os.devnull)

#command line strings
robotPath = "%s/%s.ro"
crobotsCmdLine = "crobots -m%s -l200000"
countCmdLine = "count -p -t %s/%s_%s >/dev/null 2>&1"
dbfile = "db/%s_%s.db"
matches = {'f2f': 2, '3vs3': 3, '4vs4': 4}

#if True overrides the Configuration class parameters
overrideConfiguration = False

#number of CPUs / cores
CPUs = int(os.getenv('NUMBER_OF_PROCESSORS', '2'))
print "Detected %s CPU(s)" % CPUs
spawnList = []


def run_crobots(tmppath, logpath, logfile, logtype):
    "spawn crobots command lines in subprocesses"
    global spawnList
    procs = []
    #spawn processes
    for i, s in enumerate(spawnList):
        try:
            with open("%s/tmp_%s_%s_%s.log" % (tmppath, logfile, i, logtype), 'w') as tmpfile:
                procs.append(subprocess.Popen(shlex.split(s), stdin=devNull, stderr=devNull, stdout=tmpfile))
        except OSError, e:
            print e
            raise SystemExit
    #wait
    for proc in procs:
        proc.wait()
    #check for errors
    if any(proc.returncode != 0 for proc in procs):
        print 'Something failed!'
        raise SystemExit
    #aggregate log files
    try:
        with open('%s/%s_%s.log' % (logpath, logfile, logtype), 'a') as destination:
            logfiles = 'tmp_%s_*_%s.log' % (logfile, logtype)
            for filename in iglob(os.path.join(tmppath, logfiles)):
                copyfileobj(open(filename, 'r'), destination)
                clean_up_log_file(filename)
    except OSError, e:
        print e
        raise SystemExit
    run_count(logpath, logfile, logtype)
    update_db(logpath, logfile, logtype)
    spawnList = []


def spawn_crobots_run(tmppath, cmdLine, logpath, logfile, logtype):
    "put command lines into the buffer and run"
    global spawnList, CPUs
    spawnList.append(cmdLine)
    if len(spawnList) == CPUs:
        run_crobots(tmppath, logpath, logfile, logtype)


def run_count(logpath, logfile, logtype):
    "run the count log parser"
    try:
        os.system(countCmdLine % (logpath, logfile, logtype))
        clean_up_log_file('%s/%s_%s.log' % (logpath, logfile, logtype))
    except OSError, e:
        print e


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


#initialize database
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
    global dbase, dbstatus
    if None != dbase:
        try:
            dbase.close()
        except:
            print "Error on closing local database: results may be corrupted..."
        finally:
            dbase = None
    if None != dbstatus:
        try:
            dbstatus.close()
        except:
            print "Error on closing status database: results may be incomplete..."
        finally:
            dbstatus = None
    CrobotsLibs.close_connection()


#initialize status
def init_status(logfile, logtype):
    global dbstatus, dbfile
    filename = dbfile % ('status_' + logfile, logtype)
    if not os.path.exists(filename):
        print "Init local status database for %s" % logtype.upper()
        dbstatus = shelve.open(filename, 'c')
        l = list(combinations(configuration.listRobots, matches[logtype]))
        shuffle(l)
        dbstatus[logtype] = l
        dbstatus.sync()
        CrobotsLibs.set_up(logtype)
    else:
        dbstatus = shelve.open(filename, 'w')


#update database
def update_db(logpath, logfile, logtype):
    global dbase
    txtfile = '%s/%s_%s.txt' % (logpath, logfile, logtype)
    if not os.path.exists(txtfile):
        print txtfile + ' does not exists!'
        close_db()
        raise SystemExit
    txt = open(txtfile, 'r')
    lines = txt.readlines()
    txt.close()
    for r in lines:
        if (len(r) > 60) and ('.' in r):
            name = r[4:17].strip()
            values = dbase[name]
            values[0] += int(r[18:28].strip())
            values[1] += int(r[28:37].strip())
            values[2] += int(r[38:47].strip())
            values[3] += int(r[58:68].strip())
            dbase[name] = values
            if CrobotsLibs.DATABASE_ENABLE:
                CrobotsLibs.update_results(logtype, name, values[0], values[1], values[2], values[3])
    dbase.sync()
    #clean_up_log_file(txtfile)


#save current status
def save_status(l, logtype):
    dbstatus[logtype] = l
    dbstatus.sync()


#show unordered results from database
def show_results(logfile, logtype):
    global dbfile
    filename = dbfile % (logfile, logtype)
    if not os.path.exists(filename):
        print filename + ' does not exists!'
        raise SystemExit
    dbase = shelve.open(filename, 'r')
    print 'Results %s for %s' % (logtype, logfile)
    for r in dbase:
        values = dbase[r]
        print '%s\t\t%i\t\t%i\t\t%i\t\t%i' % (r, values[0], values[1], values[2], values[3])
    dbase.close()


#clean up database and status files
def cleanup(logfile, logtype):
    global dbfile
    clean_up_log_file(dbfile % (logfile, logtype))
    clean_up_log_file(dbfile % ('status_' + logfile, logtype))
    print 'Clean up done %s %s' % (logfile, logtype.upper())


if len(sys.argv) <> 3:
    print "Usage : CrobotsDB.py <conf.py> [f2f|3vs3|4vs4|all|test|show|setup|clean]"
    raise SystemExit

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
    print e
    print 'Unable to create temp %s and %s' % (logpath, tmppath)
    raise SystemExit

if not os.path.exists(confFile):
    print 'Configuration file %s does not exist' % confFile
    raise SystemExit

if not action in ['f2f', '3vs3', '4vs4', 'all', 'test', 'show', 'setup', 'clean']:
    print 'Invalid parameter %s. Valid values are f2f, 3vs3, 4vs4, all, test, show, setup, clean' % action
    raise SystemExit

try:
    configuration = load_from_file(confFile)
except Exception, e:
    print e
    print 'Invalid configuration py file %s' % confFile
    raise SystemExit

if configuration == None:
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
        if CrobotsLibs.DATABASE_ENABLE: CrobotsLibs.clean_up(a)
    close_db()
    raise SystemExit

if action == 'setup':
    for a in ['f2f', '3vs3', '4vs4']:
        cleanup(configuration.label, a)
        init_db(configuration.label, a)
        init_status(configuration.label, a)
    close_db()
    raise SystemExit

if action == 'test':
    CrobotsLibs.test_connection()
    CrobotsLibs.close_connection()
    print 'Test completed!'
    raise SystemExit

if action == 'show':
    if os.path.exists(dbfile % (configuration.label, 'f2f')):
        show_results(configuration.label, 'f2f')
    if os.path.exists(dbfile % (configuration.label, '3vs3')):
        show_results(configuration.label, '3vs3')
    if os.path.exists(dbfile % (configuration.label, '4vs4')):
        show_results(configuration.label, '4vs4')
    raise SystemExit

if check_stop_file_exist():
    print 'Crobots.stop file found! Exit application.'
    raise SystemExit


def run_tournament(ptype, matchParam):
    global dbstatus, tmppath, logpath, robotPath, configuration, crobotsCmdLine
    print '%s Starting %s... ' % (time.ctime(), ptype.upper())
    clean_up_log_file('%s/%s_%s.log' % (logpath, configuration.label, ptype))
    param = crobotsCmdLine % matchParam
    init_db(configuration.label, ptype)
    init_status(configuration.label, ptype)
    match_list = dbstatus[ptype]
    list_length = len(match_list)
    counter = 0
    while counter < list_length:
        if check_stop_file_exist():
            break
        build_crobots_cmdline(tmppath, param, [robotPath % (configuration.sourcePath, s) for s in match_list.pop()],
                              logpath,
                              configuration.label, ptype)
        counter += 1
    if len(spawnList) > 0: run_crobots(tmppath, logpath, configuration.label, ptype)
    run_count(logpath, configuration.label, ptype)
    if check_stop_file_exist():
        save_status(match_list, ptype)
        close_db()
        print 'Crobots.stop file found! Exit application.'
        raise SystemExit
    save_status(match_list, ptype)
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

