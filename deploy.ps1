# MonitorX Docker Compose Deployment Script (PowerShell)
# This script automates the deployment process for Windows

Write-Host "🚀 MonitorX Deployment Script" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Check if Docker is installed
try {
    docker --version | Out-Null
    Write-Host "✅ Docker is installed" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker is not installed. Please install Docker Desktop first." -ForegroundColor Red
    exit 1
}

# Check if Docker Compose is installed
try {
    docker-compose --version | Out-Null
    Write-Host "✅ Docker Compose is installed" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker Compose is not installed. Please install Docker Compose first." -ForegroundColor Red
    exit 1
}

Write-Host ""

# Check if .env file exists
if (-not (Test-Path .env)) {
    Write-Host "⚠️  .env file not found. Creating from .env.example..." -ForegroundColor Yellow
    if (Test-Path .env.example) {
        Copy-Item .env.example .env
        Write-Host "✅ Created .env file" -ForegroundColor Green
        Write-Host "⚠️  Please review and update the .env file with production values!" -ForegroundColor Yellow
        Write-Host ""
        Read-Host "Press ENTER when ready to continue"
    } else {
        Write-Host "❌ .env.example not found. Cannot create .env file." -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
Write-Host "📦 Building Docker images..." -ForegroundColor Cyan
docker-compose build

Write-Host ""
Write-Host "🚀 Starting services..." -ForegroundColor Cyan
docker-compose up -d

Write-Host ""
Write-Host "⏳ Waiting for services to become healthy..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Wait for backend health check
Write-Host "Checking backend health..." -ForegroundColor Cyan
$maxRetries = 30
$retryCount = 0
$backendHealthy = $false

while ($retryCount -lt $maxRetries) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            $backendHealthy = $true
            break
        }
    } catch {
        Write-Host "." -NoNewline
    }
    Start-Sleep -Seconds 2
    $retryCount++
}

Write-Host ""
if ($backendHealthy) {
    Write-Host "✅ Backend is healthy" -ForegroundColor Green
} else {
    Write-Host "⚠️  Backend health check timeout. Check logs with: docker-compose logs collector-service" -ForegroundColor Yellow
}

# Wait for frontend
Write-Host "Checking frontend..." -ForegroundColor Cyan
$retryCount = 0
$frontendHealthy = $false

while ($retryCount -lt $maxRetries) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:3000" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            $frontendHealthy = $true
            break
        }
    } catch {
        Write-Host "." -NoNewline
    }
    Start-Sleep -Seconds 2
    $retryCount++
}

Write-Host ""
if ($frontendHealthy) {
    Write-Host "✅ Frontend is ready" -ForegroundColor Green
} else {
    Write-Host "⚠️  Frontend timeout. Check logs with: docker-compose logs frontend-dashboard" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "🎉 MonitorX is now running!" -ForegroundColor Green
Write-Host ""
Write-Host "Access the application at:" -ForegroundColor Cyan
Write-Host "  🌐 Frontend:  http://localhost:3000" -ForegroundColor White
Write-Host "  🔧 Backend:   http://localhost:8080" -ForegroundColor White
Write-Host "  📚 API Docs:  http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host ""
Write-Host "Service status:" -ForegroundColor Cyan
docker-compose ps
Write-Host ""
Write-Host "Useful commands:" -ForegroundColor Cyan
Write-Host "  📊 View logs:    docker-compose logs -f" -ForegroundColor White
Write-Host "  🛑 Stop:         docker-compose stop" -ForegroundColor White
Write-Host "  🔄 Restart:      docker-compose restart" -ForegroundColor White
Write-Host "  🗑️  Clean up:     docker-compose down" -ForegroundColor White
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Create your first user via signup" -ForegroundColor White
Write-Host "  2. Login to the dashboard" -ForegroundColor White
Write-Host "  3. Start sending API logs to MonitorX" -ForegroundColor White
Write-Host ""
