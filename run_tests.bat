@echo off
REM ============================================================
REM TextHack - Test Runner for Windows CMD
REM ============================================================

REM Check if java is on PATH, otherwise use installed Temurin JRE 21
where java >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    set JAVA_CMD=java
) else (
    set JAVA_CMD="C:\Users\Lenovo\AppData\Local\Programs\STM32CubeMX\jre\bin\java.exe"
)

chcp 65001 >nul
%JAVA_CMD% -Dfile.encoding=UTF-8 -cp bin TestPhase1
