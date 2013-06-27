#!/usr/bin/python -O
# -*- coding: UTF-8 -*-

"""
"CROBOTS" Crobots Batch Bench Manager to test one single robot

Version:        Python/1.1

                Derived from Crobots.py 1.3.1

Author:         Maurizio Camangi

Version History:
                Version 1.1 Use shutil and glob to build log files
                Version 1.0 is the first stable version

"""

import sys, imp, os.path, shlex, subprocess, time
from random import shuffle
from itertools import combinations
from shutil import copyfileobj
from glob import iglob

# Global configuration variables

#default stdin and stderr for crobots executable
devNull = open("/dev/null",'rw')

#command line strings
robotPath = "%s/%s.ro"
crobotsCmdLine = "crobots -m%s -l200000 %s"
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
      with open("/tmp/tmp_%s_%s_%s.log" % (logfile, i, logtype),'w') as tmpfile:
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
    with open('log/%s_%s.log' % (logfile, logtype), 'a') as destination:
      logfiles = 'tmp_%s_*_%s.log' % (logfile, logtype)
      for filename in iglob(os.path.join('/tmp', logfiles)):
        copyfileobj(open(filename, 'r'), destination)
        clean_up_log_file(filename)      
  except OSError, e:
    print e
    raise SystemExit
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

if len(sys.argv) <> 4:
  print "Usage : CrobotsBench.py <conf.py> <robot.r> [f2f|3vs3|4vs4|all|test]"
  raise SystemExit

confFile = sys.argv[1]
robotTest = sys.argv[2]
action = sys.argv[3]

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

if not os.path.exists(robotTest):
  print 'Robot %s does not exist' % robotTest
  raise SystemExit
else:
  print 'Compiling %s ...' % robotTest,
  clean_up_log_file(robotTest+'o')
  os.system("crobots -c %s </dev/null >/dev/null 2>&1" % robotTest)
  if not os.path.exists(robotTest+'o'):
    print 'Robot %s does not compile' % robotTest
    raise SystemExit

print 'OK!'
robotTest+='o'

if action == 'test':
  print 'Test completed!'
  raise SystemExit

if action in ['f2f', 'all']:
  print '%s Starting F2F... ' % time.ctime()
  clean_up_log_file('log/%s_f2f.log' % configuration.label)
  paramF2F = crobotsCmdLine % (configuration.matchF2F, robotTest)
  for r in configuration.listRobots:
    check_stop_file_exist()
    build_crobots_cmdline(paramF2F, [robotPath % (configuration.sourcePath, r)], configuration.label, 'f2f')
  if len(spawnList) > 0: run_crobots(configuration.label, 'f2f') 
  run_count(configuration.label, 'f2f')
  print '%s F2F completed!' % time.ctime()

if action in ['3vs3', 'all']:
  print '%s Starting 3VS3... ' % time.ctime()
  clean_up_log_file('log/%s_3vs3.log' % configuration.label)
  param3VS3 = crobotsCmdLine % (configuration.match3VS3, robotTest)
  for r in combinations(configuration.listRobots, 2):
    check_stop_file_exist() 
    build_crobots_cmdline(param3VS3, [robotPath % (configuration.sourcePath, s) for s in r], configuration.label, '3vs3')
  if len(spawnList) > 0: run_crobots(configuration.label, '3vs3')
  run_count(configuration.label, '3vs3')
  print '%s 3VS3 completed!' % time.ctime()
  
if action in ['4vs4', 'all']:
  print '%s Starting 4VS4... ' % time.ctime()
  clean_up_log_file('log/%s_4vs4.log' % configuration.label)
  param4VS4 = crobotsCmdLine % (configuration.match4VS4, robotTest)
  for r in combinations(configuration.listRobots, 3):
    check_stop_file_exist() 
    build_crobots_cmdline(param4VS4, [robotPath % (configuration.sourcePath, s) for s in r], configuration.label, '4vs4')
  if len(spawnList) > 0: run_crobots(configuration.label, '4vs4')
  run_count(configuration.label, '4vs4')
  print '%s 4VS4 completed!' % time.ctime()

devNull.close()

