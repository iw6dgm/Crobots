#!/usr/bin/python -O
# -*- coding: UTF-8 -*-


import os

config = {
  'user': 'crobots',
  'passwd': '',
  'host': '127.0.0.1',
  'db': 'crobots',
  'compress': True,
}

DATABASE_ENABLE = DATABASE_ENABLE = os.environ.has_key("CROBOTS_MYSQL")
print "MySQL database enabled: %s" % DATABASE_ENABLE

if DATABASE_ENABLE:
    import MySQLdb as mysql


P_SETUPUP = "call pSetupResults%s"
P_CLEANUP = "TRUNCATE TABLE results_%s"
UPDATE_SQL = "UPDATE results_%s SET games=%s, wins=%s, ties=%s, points=%s WHERE robot='%s'"


cnx = None

def test_connection():
    global DATABASE_ENABLE, config, cnx
    if DATABASE_ENABLE:
        try:
            mysql.threadsafety = 0
            cnx = mysql.connect(**config)
        except mysql.Error as err:
            print "A database error has occurred: switching remote database enabled to False..."
            DATABASE_ENABLE = False
            print(err)
        else:
            print "Database Connection OK"

def update_results(ptype, robot, games, win, tie, points):
    global cnx, DATABASE_ENABLE
    try:
        cursor = cnx.cursor()
        cursor.execute(UPDATE_SQL % (ptype, games, win, tie, points, robot))
        cnx.commit()
        cursor.close()
    except mysql.Error as err:
        print "A database error has occurred: switching remote database enabled to False..."
        close_connection()
        DATABASE_ENABLE = False
        print(err)

def close_connection():
    global DATABASE_ENABLE, cnx
    if DATABASE_ENABLE and None != cnx:
        try:
            print "Closing remote database connection..."
            cnx.close()
        except mysql.Error as err:
            print "A database error has occurred on closing connection..."
            print(err)
        finally:
            cnx = None

def set_up(ptype):
    global DATABASE_ENABLE
    if DATABASE_ENABLE:
        try:
            print("Set up remote database for %s" % ptype.upper())
            cnx = mysql.connect(**config)
            cursor = cnx.cursor()
            cursor.execute(P_SETUPUP % ptype.upper())
            cnx.commit()
            cursor.close()
            cnx.close()
        except mysql.Error as err:
            print "A database error has occurred: switching remote database enabled to False..."
            DATABASE_ENABLE = False
            print(err)

def clean_up(ptype):
    global DATABASE_ENABLE
    if DATABASE_ENABLE:
        try:
            print("Clean up remote database for %s" % ptype.upper())
            cnx = mysql.connect(**config)
            cursor = cnx.cursor()
            cursor.execute(P_CLEANUP % ptype)
            #cnx.commit()
            cursor.close()
            cnx.close()
        except mysql.Error as err:
            print "A database error has occurred: switching remote database enabled to False..."
            DATABASE_ENABLE = False
            print(err)





