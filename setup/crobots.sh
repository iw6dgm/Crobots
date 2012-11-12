#!/bin/sh

THREADS=$1
LOG=/tmp/${THREADS}$$TMP
M=$2
R1=$3.ro
R2=$4.ro
R3=$5.ro
R4=$6.ro

$HOME/bin/crobots </dev/null 2>/dev/null -m$M -l200000 $R1 $R2 $R3 $R4 >$LOG.log
$HOME/bin/count -p -t $LOG 2>&1 >/dev/null
grep \\. $LOG.txt
rm -f $LOG.*
