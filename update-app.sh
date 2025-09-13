#!/bin/bash

# Quick deployment script for updates
# Use this when you need to update the application

set -e

echo "Updating BulkSMS API..."

# Pull latest changes (if using git)
if [ -d ".git" ]; then
    echo "Pulling latest changes from git..."
    git pull origin main
fi

# Rebuild and restart services
echo "Rebuilding and restarting services..."
sudo docker-compose -f docker-compose.prod.yml down
sudo docker-compose -f docker-compose.prod.yml up -d --build

# Wait for services to be ready
echo "Waiting for services to start..."
sleep 60

# Check service health
echo "Checking service health..."
sudo docker-compose -f docker-compose.prod.yml ps

# Test API endpoint
echo "Testing API health..."
curl -f http://localhost:8080/actuator/health || echo "Health check failed"

echo "Update completed!"
