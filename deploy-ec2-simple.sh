#!/bin/bash

# Simple deployment script for EC2 using Docker commands only
# This avoids docker-compose issues

set -e

echo "=== Starting BulkSMS API deployment on EC2 ==="

# Stop and remove existing containers if they exist
echo "Cleaning up existing containers..."
docker stop bulksms-api bulksms-mysql 2>/dev/null || true
docker rm bulksms-api bulksms-mysql 2>/dev/null || true
docker network rm bulksms-network 2>/dev/null || true

# Create Docker network
echo "Creating Docker network..."
docker network create bulksms-network

# Pull the latest image from Docker Hub
echo "Pulling latest image from Docker Hub..."
docker pull carlosa21/bulksms-api:latest

# Start MySQL container
echo "Starting MySQL container..."
docker run -d \
  --name bulksms-mysql \
  --network bulksms-network \
  -p 3306:3306 \
  -e MYSQL_DATABASE=bulksmsdb \
  -e MYSQL_USER=bulksmsuser \
  -e MYSQL_PASSWORD=bulksmspass \
  -e MYSQL_ROOT_PASSWORD=rootpass \
  -v mysql_data:/var/lib/mysql \
  --restart unless-stopped \
  mysql:8.0 \
  --default-authentication-plugin=mysql_native_password

# Wait for MySQL to be ready
echo "Waiting for MySQL to initialize (60 seconds)..."
sleep 60

# Start the BulkSMS API container
echo "Starting BulkSMS API container..."
docker run -d \
  --name bulksms-api \
  --network bulksms-network \
  -p 8080:8080 \
  --restart unless-stopped \
  carlosa21/bulksms-api:latest

# Wait a bit for the application to start
echo "Waiting for application to start (30 seconds)..."
sleep 30

# Check container status
echo "Checking container status..."
docker ps

# Show recent logs
echo "=== Recent application logs ==="
docker logs --tail=20 bulksms-api

echo ""
echo "=== Deployment completed! ==="
echo "API should be available at: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):8080"
echo ""
echo "Useful commands:"
echo "- View logs: docker logs -f bulksms-api"
echo "- Check status: docker ps"
echo "- Stop all: docker stop bulksms-api bulksms-mysql"
echo "- Restart API: docker restart bulksms-api"
