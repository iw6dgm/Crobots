#!/usr/bin/python -O
# -*- coding: UTF-8 -*-

"""
"CROBOTS" Crobots Batch Tournament Manager

Version:        Python/1.3.1

                Translated from 'run2012.sh' UNIX/bash to Python 2.7

Author:         Maurizio Camangi

Version History:
                Patch 1.3.1 Fix import
                Version 1.3 Use more compact build_crobots_cmdline function
                
                Version 1.2 Use more compact combinations from itertools

                Version 1.1 is the first stable version
                (corresponds to bash/v.1.2c)
                
                Add multiple CPUs / cores management

"""

import sys, imp, os.path, shlex, subprocess, time
from random import shuffle
from itertools import combinations

# Global configuration variables

#default stdin and stderr for crobots executable
devNull = open("/dev/null",'rw')

#command line strings
robotPath = "%s/%s.ro"
crobotsCmdLine = "crobots -m%s -l200000"
countCmdLine = "count -p -t log/%s_%s >/dev/null 2>&1"

#if True overrides the Configuration class parameters
overrideConfiguration = False

#number of CPUs / cores
CPUs = 2
spawnList = []

def run_crobots(logfile, logtype):
  "spawn crobots command lines in subprocesses"
  global spawnList
  procs = []
  #spawn processes
  for i,s in enumerate(spawnList):
    try:
      tmpfile = open("/tmp/tmp_%s_%s_%s.log" % (logfile, i, logtype),'w')
      procs.append(subprocess.Popen(shlex.split(s), stdin=devNull, stderr=devNull, stdout=tmpfile))
    finally:
      tmpfile.close()
  #wait
  for proc in procs:
    proc.wait()
  #check for errors
  if any(proc.returncode != 0 for proc in procs):
    print 'Something failed!'
    raise SystemExit
  #aggregate log files
  try:
    os.system("cat /tmp/tmp_%s_*_%s.log >>log/%s_%s.log" % (logfile, logtype, logfile, logtype))
  except OSError, e:
    print e
    raise SystemExit
  #clean up temporary log files
  for i in xrange(len(spawnList)):
    clean_up_log_file("/tmp/tmp_%s_%s_%s.log" % (logfile, i, logtype))
  spawnList = []
    

def spawn_crobots_run(cmdLine, logfile, logtype):
  "put command lines into the buffer and run"
  global spawnList, CPUs
  spawnList.append(cmdLine)
  if len(spawnList) == CPUs:
    run_crobots(logfile, logtype)
    

def run_count(logfile, logtype):
  "run the count log parser"
  try:
    os.system(countCmdLine % (logfile, logtype))
    clean_up_log_file('log/%s_%s.log' % (logfile, logtype))
  except OSError, e:
    print e 

def check_stop_file_exist():
  "check the stop file existance"
  if os.path.exists('Crobots.stop'):
    print 'Crobots.stop found! Exit application.'
    raise SystemExit

def clean_up_log_file(filepath):
  "remove log file"
  try:
    os.remove(filepath)
  except: pass

def build_crobots_cmdline(paramCmdLine, robotList, logfile, logtype):
  "build and run crobots command lines"
  shuffle(robotList)
  spawn_crobots_run(" ".join([paramCmdLine] + robotList), logfile, logtype)

def load_from_file(filepath):
  "Load configuration py file with tournament parameters"
  class_inst = None
  expected_class = 'Configuration'

  mod_name,file_ext = os.path.splitext(os.path.split(filepath)[-1])

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

if configuration == None:
  print 'Invalid configuration py file %s' % confFile
  raise SystemExit
  
if len(configuration.listRobots)==0:
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
  robot =  robotPath % (configuration.sourcePath, r)
  if not os.path.exists(robot):
    print 'Robot file %s does not exist.' % robot
    sys.exit(1)

print 'OK!'

if action == 'test':
  print 'Test completed!'
  raise SystemExit

if action in ['f2f', 'all']:
  print '%s Starting F2F... ' % time.ctime()
  clean_up_log_file('log/%s_f2f.log' % configuration.label)
  paramF2F = crobotsCmdLine % configuration.matchF2F
  for r in combinations(configuration.listRobots, 2):
    check_stop_file_exist()
    build_crobots_cmdline(paramF2F, [robotPath % (configuration.sourcePath, s) for s in r], configuration.label, 'f2f')
  if len(spawnList) > 0: run_crobots(configuration.label, 'f2f') 
  run_count(configuration.label, 'f2f')
  print '%s F2F completed!' % time.ctime()

if action in ['3vs3', 'all']:
  print '%s Starting 3VS3... ' % time.ctime()
  clean_up_log_file('log/%s_3vs3.log' % configuration.label)
  param3VS3 = crobotsCmdLine % configuration.match3VS3
  for r in combinations(configuration.listRobots, 3):
    check_stop_file_exist() 
    build_crobots_cmdline(param3VS3, [robotPath % (configuration.sourcePath, s) for s in r], configuration.label, '3vs3')
  if len(spawnList) > 0: run_crobots(configuration.label, '3vs3')
  run_count(configuration.label, '3vs3')
  print '%s 3VS3 completed!' % time.ctime()
  
if action in ['4vs4', 'all']:
  print '%s Starting 4VS4... ' % time.ctime()
  clean_up_log_file('log/%s_4vs4.log' % configuration.label)
  param4VS4 = crobotsCmdLine % configuration.match4VS4
  for r in combinations(configuration.listRobots, 4):
    check_stop_file_exist() 
    build_crobots_cmdline(param4VS4, [robotPath % (configuration.sourcePath, s) for s in r], configuration.label, '4vs4')
  if len(spawnList) > 0: run_crobots(configuration.label, '4vs4')
  run_count(configuration.label, '4vs4')
  print '%s 4VS4 completed!' % time.ctime()

devNull.close()

