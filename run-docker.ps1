# PowerShell script to run Docker commands with proper PATH setup
# This ensures Docker is always available in your project terminal

# Add Docker to PATH for this session
$dockerPath = "C:\Program Files\Docker\Docker\resources\bin"
if (Test-Path $dockerPath) {
    $env:PATH = "$env:PATH;$dockerPath"
    Write-Host "Docker added to PATH for this session" -ForegroundColor Green
} else {
    Write-Host "Docker path not found at $dockerPath" -ForegroundColor Yellow
}

# Verify Docker is working
Write-Host "Testing Docker..." -ForegroundColor Cyan
try {
    $dockerVersion = docker --version
    Write-Host "$dockerVersion" -ForegroundColor Green
    Write-Host "Docker is ready!" -ForegroundColor Green
} catch {
    Write-Host "Docker is not accessible" -ForegroundColor Red
    exit 1
}

# Start the application
Write-Host "Starting BulkSMS API with Docker..." -ForegroundColor Cyan
Write-Host ""

docker compose up --build
