#!/usr/bin/python -O
# -*- coding: UTF-8 -*-

"""
"CROBOTS" Crobots Merge DataBase support

Version:        Python/1.0

                Derived from CrobotsDB.py 1.0

Author:         Maurizio Camangi

Version History:
                Version 1.0 is the first stable version

"""

import sys, imp, shelve, os.path

def clean_up_log_file(filepath):
  "remove log file"
  try:
    os.remove(filepath)
  except: pass

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
def init_db(dbfile):
  global configuration
  dbase = shelve.open(dbfile, 'n')
  for s in configuration.listRobots:
    key = os.path.basename(s)
    dbase[key] = [0,0,0,0]
  dbase.sync()
  dbase.close()

#update database
def update_db():
  dbase = shelve.open(dbaseout, 'w')
  db1   = shelve.open(dbase1, 'r')
  db2   = shelve.open(dbase2, 'r') 
  for key in dbase:
    if db1.has_key(key):
      newvalues = db1[key]
      values = dbase[key]
      values[0]+=newvalues[0]
      values[1]+=newvalues[1]
      values[2]+=newvalues[2]
      values[3]+=newvalues[3]
      dbase[key] = values
    if db2.has_key(key):
      newvalues = db2[key]
      values = dbase[key]
      values[0]+=newvalues[0]
      values[1]+=newvalues[1]
      values[2]+=newvalues[2]
      values[3]+=newvalues[3]
      dbase[key] = values
  dbase.sync()
  dbase.close()
  db1.close()
  db2.close()
  #clean_up_log_file(txtfile)

#show unordered results from database  
def show_results(dbfile):
  if not os.path.exists(dbfile):
    print dbfile + ' does not exists!'
    raise SystemExit
  dbase = shelve.open(dbfile, 'r')
  for r in dbase:
    values = dbase[r]
    print '%s\t\t%i\t\t%i\t\t%i\t\t%i' % (r, values[0], values[1], values[2], values[3])
  dbase.close()

#clean up database and status files
def cleanup(filepath):
  try:
    os.remove(filepath)
  except: pass
  print 'Clean up done %s!' % filepath
  
if len(sys.argv) <> 6:
  print "Usage : CrobotsMergeDB.py <conf.py> <db1> <db2> <dbout> [merge|show|clean]"
  raise SystemExit

confFile = sys.argv[1]
dbase1 = sys.argv[2]
dbase2 = sys.argv[3]
dbaseout = sys.argv[4] 
action = sys.argv[5]

if not os.path.exists(confFile):
  print 'Configuration file %s does not exist' % confFile
  raise SystemExit

if any(not os.path.exists(db) for db in (dbase1, dbase2)):
  print 'Source databasas do not exist'
  raise SystemExit

if not action in ['merge', 'show', 'clean']:
  print 'Invalid parameter %s. Valid values are merge, show, clean' % action
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
  
print 'List size = %d' % len(configuration.listRobots)

if action == 'clean':
  cleanup(dbaseout)
  raise SystemExit
  
if action == 'show':
  show_results(dbaseout)
  raise SystemExit

if action == 'merge':
  init_db(dbaseout)
  update_db()
  print 'Merge done!'

