@echo off
setlocal

title jshERP

set "JAVA_CMD=%JAVA_HOME%\bin\java"
if "%JAVA_HOME%"=="" set "JAVA_CMD=java"
set "JAVA_OPTS=-Xms1000m -Xmx2000m"

%JAVA_CMD% %JAVA_OPTS% -jar .\lib\jshERP.jar
pause
