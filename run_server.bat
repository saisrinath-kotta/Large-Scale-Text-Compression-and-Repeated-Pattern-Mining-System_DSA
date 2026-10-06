@echo off
REM ============================================================
REM TextHack - Web Server Runner for Windows CMD
REM ============================================================

where java >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    set JAVA_CMD=java
) else (
    set JAVA_CMD="C:\Users\Lenovo\AppData\Local\Programs\STM32CubeMX\jre\bin\java.exe"
)

echo Compiling TextHack...
call compile.bat

echo Starting TextHack Web Server...
echo Opening browser once server initializes...
chcp 65001 >nul
start /b "" cmd /c "timeout /t 2 >nul & start http://localhost:8081/"
%JAVA_CMD% -Dfile.encoding=UTF-8 -cp bin WebServer 8081

