@echo off
echo Starting BulkSMS API with Docker...
echo.

REM Check if Docker is running
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Docker is not accessible from this terminal.
    echo Please run this script from a regular Command Prompt or PowerShell where Docker works.
    pause
    exit /b 1
)

echo Docker found, proceeding with deployment...
echo.

REM Navigate to the project directory
cd /d "E:\SELLING PROJECTS\SMS Web\bulksmsAPI"

echo Building and starting services...
docker compose up --build

pause
