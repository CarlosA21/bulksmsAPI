@echo off
echo Stopping BulkSMS API Docker containers...
echo.

cd /d "E:\SELLING PROJECTS\SMS Web\bulksmsAPI"

docker compose down

echo.
echo Containers stopped successfully!
pause
