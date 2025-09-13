# PowerShell script to start BulkSMS API with Docker
Write-Host "Starting BulkSMS API with Docker..." -ForegroundColor Green
Write-Host ""

# Check if Docker is running
try {
    docker --version | Out-Null
    Write-Host "Docker found, proceeding with deployment..." -ForegroundColor Green
}
catch {
    Write-Host "Error: Docker is not accessible from this terminal." -ForegroundColor Red
    Write-Host "Please run this script from a regular PowerShell where Docker works." -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

# Navigate to the project directory
Set-Location "E:\SELLING PROJECTS\SMS Web\bulksmsAPI"

Write-Host "Building and starting services..." -ForegroundColor Cyan
docker compose up --build

Read-Host "Press Enter to exit"
