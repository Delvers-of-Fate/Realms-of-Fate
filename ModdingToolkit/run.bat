@echo off
setlocal
java -jar "%~dp0ModdingToolkit.jar"
if errorlevel 1 pause
