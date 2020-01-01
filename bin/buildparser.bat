@echo off

REM If there is a generated freecc.jar in the base directory, use that, otherwise, use
REM the bootstrap freecc.jar that is in the bin directory.

java -classpath "%~f0\..\..\freecc.jar;%~f0\..\freecc.jar;%~f0\..\freemarker.jar" freecc.Main %~f0\..\..\src\grammars\FreeCC.freecc


