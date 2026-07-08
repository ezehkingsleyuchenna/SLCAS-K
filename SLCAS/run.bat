@echo off
title SLCAS - Run
echo ============================================================
echo  Smart Library Circulation ^& Automation System  (SLCAS)
echo  Starting...
echo ============================================================
cd /d "%~dp0"
"C:\Program Files\Java\jdk-12.0.1\bin\java.exe" -cp bin Main
pause
