#!/usr/bin/python -O
# -*- coding: UTF-8 -*-

"""
CrobotsDBReport v.1.0.1 - Given a dbase shows an ordered ranking
"""


__author__ = 'joshua'

import sys
import os.path
import shelve
from Count import print_report

STATUS_KEY = '__STATUS__'


def show_db_report(dbfile):
    if not os.path.exists(dbfile):
        print 'DB %s does not exist. Exit.' % dbfile
        raise SystemExit
    robots = []
    dbase = shelve.open(dbfile, 'r')
    for r in dbase:
        if STATUS_KEY == r:
            continue
        values = dbase[r]
        games = values[0]
        wins = values[1]
        ties = values[2]
        points = values[3]
        eff = 0.0
        if games != 0:
            eff = 100.0 * points / (12.0 * games)
        robots.append([r, games, wins, ties, games-wins-ties, points, eff])
    dbase.close()
    print_report(robots)


def main():
    if len(sys.argv) <> 2:
        print "Usage : CrobotsDBReport.py <dbfile>"
        raise SystemExit
    dbfile = sys.argv[1]
    show_db_report(dbfile)

if __name__ == '__main__':
    main()
