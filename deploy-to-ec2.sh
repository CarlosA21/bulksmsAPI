#!/bin/bash

# Deployment script for EC2 - Using direct Docker commands instead of docker-compose
# This script will be run on your EC2 instance

set -e

echo "Starting BulkSMS API deployment on EC2..."

# Update system packages
sudo yum update -y

# Install Docker if not already installed
if ! command -v docker &> /dev/null; then
    echo "Installing Docker..."
    sudo yum install -y docker
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -a -G docker ec2-user
    echo "Docker installed. Please logout and login again, then re-run this script."
    exit 1
fi

# Start Docker service if not running
sudo systemctl start docker

# Create application directory
APP_DIR="/home/ec2-user/bulksms-api"
mkdir -p $APP_DIR
cd $APP_DIR

# Stop and remove existing containers if running
echo "Stopping existing containers..."
docker stop bulksms-api bulksms-mysql 2>/dev/null || true
docker rm bulksms-api bulksms-mysql 2>/dev/null || true
docker network rm bulksms-network 2>/dev/null || true

# Pull required images
echo "Downloading MySQL image..."
docker pull mysql:8.0

echo "Downloading BulkSMS API image..."
docker pull carlosa21/bulksms-api:latest

# Create Docker network
echo "Creating Docker network..."
docker network create bulksms-network

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

# Wait for MySQL to initialize
echo "Waiting for MySQL to initialize (90 seconds)..."
sleep 90

# Start BulkSMS API container
echo "Starting BulkSMS API container..."
docker run -d \
  --name bulksms-api \
  --network bulksms-network \
  -p 8080:8080 \
  --restart unless-stopped \
  carlosa21/bulksms-api:latest

# Wait for services to start
echo "Waiting for services to start (30 seconds)..."
sleep 30

# Check service status
echo "Checking service status..."
docker ps

# Show recent logs
echo "=== MySQL logs ==="
docker logs --tail=10 bulksms-mysql

echo "=== BulkSMS API logs ==="
docker logs --tail=20 bulksms-api

echo ""
echo "=== Deployment completed! ==="
echo "API should be available at: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):8080"
echo "Database is accessible at: $(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):3306"
echo ""
echo "Useful commands:"
echo "- View API logs: docker logs -f bulksms-api"
echo "- View MySQL logs: docker logs -f bulksms-mysql"
echo "- Check status: docker ps"
echo "- Stop all: docker stop bulksms-api bulksms-mysql"
echo "- Restart API: docker restart bulksms-api"
