@echo off
title SLCAS - Compile
setlocal EnableDelayedExpansion

echo ============================================================
echo  Smart Library Circulation ^& Automation System  (SLCAS)
echo  Compiling...
echo ============================================================

set JAVAC="C:\Program Files\Java\jdk-12.0.1\bin\javac.exe"

if not exist bin mkdir bin

if exist sources.txt del sources.txt
for /r src %%f in (*.java) do echo "%%f" >> sources.txt

%JAVAC% -encoding UTF-8 -d bin @sources.txt

if %ERRORLEVEL% EQU 0 (
    echo.
    echo  Compilation successful!
    echo  Run run.bat to start the application.
    del sources.txt
) else (
    echo.
    echo  Compilation FAILED. Check errors above.
    if exist sources.txt del sources.txt
)

pause
endlocal
