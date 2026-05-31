@echo off
REM Diagnostic batch file to debug launch issues
REM This script builds the classpath and shows what command will be executed

setlocal enabledelayedexpansion

echo ==================== SC Trade Companion Debug Launcher ====================
echo.

REM Change to script directory to ensure relative paths work
cd /d "%~dp0"
echo Current directory: %cd%
echo.

REM Build classpath
echo Building CLASSPATH...
set CLASSPATH=bin\sc-trade-companion.jar
set JAR_COUNT=0

for %%f in (bin\dependencies\*.jar) do (
    set CLASSPATH=!CLASSPATH!;%%f
    set /a JAR_COUNT+=1
)

echo Found !JAR_COUNT! dependency JARs
echo.

REM Show full classpath
echo Full CLASSPATH (first 500 chars):
echo !CLASSPATH:~0,500!
echo.

REM Show the command that will be executed
echo Executing command:
echo bin\jre\bin\java.exe -Xmx2024m -Djava.net.preferIPv4Stack=true -cp !CLASSPATH! tools.sctrade.companion.CompanionApplication
echo.

REM Run with java.exe (console) instead of javaw.exe to capture errors
bin\jre\bin\java.exe -Xmx2024m -Djava.net.preferIPv4Stack=true -cp !CLASSPATH! tools.sctrade.companion.CompanionApplication

pause
