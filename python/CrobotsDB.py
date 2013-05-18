#!/usr/bin/python -O
# -*- coding: UTF-8 -*-

"""
"CROBOTS" Crobots Batch Tournament Manager with DataBase support

Version:        Python/1.1

                Derived from Crobots.py 1.3

Author:         Maurizio Camangi

Version History:
                Version 1.1 more compact iterations / combinations with
                start / end offset
                
                Version 1.0 is the first stable version

"""

import sys, imp, shlex, subprocess, time, shelve, os.path
from random import shuffle
from itertools import combinations, islice

# Global configuration variables

#startStatus for f2f, 3vs3, 4vs4
startStatus = {'f2f':0, '3vs3':0, '4vs4':0}
#endStatus for f2f, 3vs3, 4vs4
endStatus = {'f2f':None, '3vs3':None, '4vs4':None}

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
  run_count(logfile, logtype)
  update_db(logfile, logtype)
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
    return True
  return False

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

#initialize database
def init_db(logfile, logtype):
  global configuration, startStatus
  dbfile = 'db/%s_%s.dat' % (logfile, logtype)
  statusfile = 'db/status_%s_%s.txt' % (logfile, logtype)
  start = startStatus[logtype]
  if not os.path.exists(dbfile):
    dbase = shelve.open(dbfile, 'c')
    for s in configuration.listRobots:
      key = os.path.basename(s)
      dbase[key] = [0,0,0,0]
    dbase.sync()
    dbase.close()
  if not os.path.exists(statusfile):
    status = open(statusfile, 'w')
    print >>status, start
  else:
    status = open(statusfile, 'r')
    l = status.readline().rstrip('\r\n')
    if l != 'COMPLETED':
      startStatus[logtype] = int(l)
    else:
      startStatus[logtype] = l
  status.close()

#update database
def update_db(logfile, logtype):
  dbfile = 'db/%s_%s.dat' % (logfile, logtype)
  txtfile = 'log/%s_%s.txt' % (logfile, logtype)
  if not os.path.exists(txtfile):
    print txtfile + ' does not exists!'
    raise SystemExit 
  if not os.path.exists(dbfile):
    print dbfile + ' does not exists!'
    raise SystemExit
  txt = open(txtfile, 'r') 
  lines = txt.readlines()
  txt.close() 
  dbase = shelve.open(dbfile, 'w')
  for r in lines:
    if (len(r) > 60) and ('.' in r):
      name = r[4:17].strip()
      values = dbase[name]
      values[0]+=int(r[18:28].strip())
      values[1]+=int(r[28:37].strip())
      values[2]+=int(r[38:47].strip())
      values[3]+=int(r[58:68].strip())
      dbase[name] = values
  dbase.sync()
  dbase.close()
  #clean_up_log_file(txtfile)

#save current status
def save_status(counter, logfile, logtype):
  statusfile = 'db/status_%s_%s.txt' % (logfile, logtype)
  status = open(statusfile, 'w')
  print >>status, counter
  status.close()

#show unordered results from database  
def show_results(logfile, logtype):
  dbfile = 'db/%s_%s.dat' % (logfile, logtype)
  if not os.path.exists(dbfile):
    print dbfile + ' does not exists!'
    raise SystemExit
  dbase = shelve.open(dbfile, 'r')
  print 'Results %s for %s' % (logtype, logfile)
  for r in dbase:
    values = dbase[r]
    print '%s\t\t%i\t\t%i\t\t%i\t\t%i' % (r, values[0], values[1], values[2], values[3])
  dbase.close()

#clean up database and status files
def cleanup(logfile, logtype):
  for s in ['db/%s_%s.dat','db/status_%s_%s.txt']:
    clean_up_log_file(s % (logfile, logtype))
  print 'Clean up done %s %s!' % (logfile, logtype)
  
if len(sys.argv) <> 3:
  print "Usage : CrobotsDB.py <conf.py> [f2f|3vs3|4vs4|all|test|show|clean]"
  raise SystemExit

confFile = sys.argv[1]
action = sys.argv[2]

if not os.path.exists(confFile):
  print 'Configuration file %s does not exist' % confFile
  raise SystemExit

if not action in ['f2f', '3vs3', '4vs4', 'all', 'test', 'show', 'clean']:
  print 'Invalid parameter %s. Valid values are f2f, 3vs3, 4vs4, all, test, show, clean' % action
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

if action == 'clean':
  for a in ['f2f', '3vs3', '4vs4']:
    cleanup(configuration.label, a)
  raise SystemExit

if action == 'test':
  print 'Test completed!'
  raise SystemExit
  
if action == 'show':
  if os.path.exists('db/%s_%s.dat' % (configuration.label, 'f2f')):
    show_results(configuration.label, 'f2f')
  if os.path.exists('db/%s_%s.dat' % (configuration.label, '3vs3')):
    show_results(configuration.label, '3vs3')
  if os.path.exists('db/%s_%s.dat' % (configuration.label, '4vs4')):
    show_results(configuration.label, '4vs4')
  raise SystemExit

if check_stop_file_exist():
  print 'Crobots.stop file found! Exit application.'
  raise SystemExit

if action in ['f2f', 'all']:
  print '%s Starting F2F... ' % time.ctime()
  clean_up_log_file('log/%s_f2f.log' % configuration.label)
  paramF2F = crobotsCmdLine % configuration.matchF2F
  init_db(configuration.label, 'f2f')
  start=startStatus['f2f']
  end=endStatus['f2f']
  if start != 'COMPLETED':
    counter=start
    for r in islice(iter(combinations(configuration.listRobots, 2)), start, end):
      if check_stop_file_exist(): break
      build_crobots_cmdline(paramF2F, [robotPath % (configuration.sourcePath, s) for s in r], configuration.label, 'f2f')
      counter+=1
    if len(spawnList) > 0: run_crobots(configuration.label, 'f2f') 
    run_count(configuration.label, 'f2f')
    if check_stop_file_exist():
      save_status(counter, configuration.label, 'f2f')
      print 'Crobots.stop file found! Exit application.'
      raise SystemExit
    save_status('COMPLETED', configuration.label, 'f2f')
  print '%s F2F completed!' % time.ctime()


if action in ['3vs3', 'all']:
  print '%s Starting 3VS3... ' % time.ctime()
  clean_up_log_file('log/%s_3vs3.log' % configuration.label)
  param3VS3 = crobotsCmdLine % configuration.match3VS3
  init_db(configuration.label, '3vs3')
  start=startStatus['3vs3']
  end=endStatus['3vs3']
  if start != 'COMPLETED':
    counter=start
    for r in islice(iter(combinations(configuration.listRobots, 3)), start, end):
      if check_stop_file_exist(): break
      build_crobots_cmdline(param3VS3, [robotPath % (configuration.sourcePath, s) for s in r], configuration.label, '3vs3')
      counter+=1
    if len(spawnList) > 0: run_crobots(configuration.label, '3vs3')
    run_count(configuration.label, '3vs3')
    if check_stop_file_exist():
      save_status(counter, configuration.label, '3vs3')
      print 'Crobots.stop file found! Exit application.'
      raise SystemExit
    save_status('COMPLETED', configuration.label, '3vs3')
  print '%s 3VS3 completed!' % time.ctime()
  
if action in ['4vs4', 'all']:
  print '%s Starting 4VS4... ' % time.ctime()
  clean_up_log_file('log/%s_4vs4.log' % configuration.label)
  param4VS4 = crobotsCmdLine % configuration.match4VS4
  init_db(configuration.label, '4vs4')
  start=startStatus['4vs4']
  end=endStatus['4vs4']
  if start != 'COMPLETED':
    counter=start
    for r in islice(iter(combinations(configuration.listRobots, 4)), start, end):
      if check_stop_file_exist(): break 
      build_crobots_cmdline(param4VS4, [robotPath % (configuration.sourcePath, s) for s in r], configuration.label, '4vs4')
      counter+=1
    if len(spawnList) > 0: run_crobots(configuration.label, '4vs4')
    run_count(configuration.label, '4vs4')
    if check_stop_file_exist():
      save_status(counter, configuration.label, '4vs4')
      print 'Crobots.stop file found! Exit application.'
      raise SystemExit
    save_status('COMPLETED', configuration.label, '4vs4')
  print '%s 4VS4 completed!' % time.ctime()

devNull.close()

