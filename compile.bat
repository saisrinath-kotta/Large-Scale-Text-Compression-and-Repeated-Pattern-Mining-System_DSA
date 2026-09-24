@echo off
REM ============================================================
REM TextHack - Build Script for Windows CMD
REM ============================================================

where javac >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    set JAVAC_CMD=javac
) else (
    set JAVAC_CMD="C:\Users\Lenovo\AppData\Local\Programs\STM32CubeMX\jre\bin\javac.exe"
)

if not exist bin mkdir bin

echo Compiling TextHack source files (Java 8+ compatible, UTF-8)...
%JAVAC_CMD% --release 8 -Xlint:-options -encoding UTF-8 -d bin -sourcepath src src\*.java

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation completed into 'bin' directory.
) else (
    echo [ERROR] Compilation failed.
)
