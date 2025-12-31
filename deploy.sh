#!/usr/bin/env bash
# MonitorX Docker Compose Deployment Script
# This script automates the deployment process

set -e  # Exit on error

echo "🚀 MonitorX Deployment Script"
echo "================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker is not installed. Please install Docker first.${NC}"
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}❌ Docker Compose is not installed. Please install Docker Compose first.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Docker and Docker Compose are installed${NC}"
echo ""

# Check if .env file exists
if [ ! -f .env ]; then
    echo -e "${YELLOW}⚠️  .env file not found. Creating from .env.example...${NC}"
    if [ -f .env.example ]; then
        cp .env.example .env
        echo -e "${GREEN}✅ Created .env file${NC}"
        echo -e "${YELLOW}⚠️  Please review and update the .env file with production values before continuing!${NC}"
        read -p "Press ENTER when ready to continue..."
    else
        echo -e "${RED}❌ .env.example not found. Cannot create .env file.${NC}"
        exit 1
    fi
fi

echo ""
echo "📦 Building Docker images..."
docker-compose build

echo ""
echo "🚀 Starting services..."
docker-compose up -d

echo ""
echo "⏳ Waiting for services to become healthy..."
sleep 10

# Wait for backend health check
echo "Checking backend health..."
MAX_RETRIES=30
RETRY_COUNT=0
until curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1 || [ $RETRY_COUNT -eq $MAX_RETRIES ]; do
    echo -n "."
    sleep 2
    RETRY_COUNT=$((RETRY_COUNT + 1))
done

echo ""
if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo -e "${YELLOW}⚠️  Backend health check timeout. Check logs with: docker-compose logs collector-service${NC}"
else
    echo -e "${GREEN}✅ Backend is healthy${NC}"
fi

# Wait for frontend
echo "Checking frontend..."
RETRY_COUNT=0
until curl -sf http://localhost:3000 > /dev/null 2>&1 || [ $RETRY_COUNT -eq $MAX_RETRIES ]; do
    echo -n "."
    sleep 2
    RETRY_COUNT=$((RETRY_COUNT + 1))
done

echo ""
if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo -e "${YELLOW}⚠️  Frontend timeout. Check logs with: docker-compose logs frontend-dashboard${NC}"
else
    echo -e "${GREEN}✅ Frontend is ready${NC}"
fi

echo ""
echo "================================"
echo -e "${GREEN}🎉 MonitorX is now running!${NC}"
echo ""
echo "Access the application at:"
echo "  🌐 Frontend:  http://localhost:3000"
echo "  🔧 Backend:   http://localhost:8080"
echo "  📚 API Docs:  http://localhost:8080/swagger-ui.html"
echo ""
echo "Service status:"
docker-compose ps
echo ""
echo "Useful commands:"
echo "  📊 View logs:    docker-compose logs -f"
echo "  🛑 Stop:         docker-compose stop"
echo "  🔄 Restart:      docker-compose restart"
echo "  🗑️  Clean up:     docker-compose down"
echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo "  1. Create your first user via signup"
echo "  2. Login to the dashboard"
echo "  3. Start sending API logs to MonitorX"
echo ""
