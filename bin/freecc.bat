@echo off

REM If there is a generated freecc.jar in the base directory, use that, otherwise, use
REM the bootstrap freecc.jar that is in the bin directory.

java -classpath "%~f0\..\..\freecc.jar;%~f0\..\freecc.jar;%~f0\..\freemarker.jar" org.visigoths.freecc.Main %1 %2 %3 %4 %5 %6 %7 %8 %9
