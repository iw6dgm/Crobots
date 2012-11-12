@echo off

rem Please, configure properly the following variables

REM set TIMESTAMP=%TIME:~0,2%%TIME:~3,2%%TIME:~6,2%
set CROBOTS_PATH=c:\crobots
set COUNT_PATH=c:\crobots\tgui
set GREP_PATH=C:\Programmi\unix
set THREAD=%1
set LOG=%TEMP%\log%THREAD%

%CROBOTS_PATH%\crobots.exe <NUL 2>NUL -m%2 -l200000 %3.ro %4.ro %5.ro %6.ro >"%LOG%.log"
%COUNT_PATH%\count.exe /p /t "%LOG%" 2>NUL >NUL
%GREP_PATH%\grep.exe \. "%LOG%.txt"
del /F/Q "%LOG%.???"
