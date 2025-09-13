#!/bin/bash

# Deployment script for EC2
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
fi

# Install Docker Compose if not already installed
if ! command -v docker-compose &> /dev/null; then
    echo "Installing Docker Compose..."
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
fi

# Create application directory
APP_DIR="/home/ec2-user/bulksms-api"
sudo mkdir -p $APP_DIR
cd $APP_DIR

# Stop existing containers if running
echo "Stopping existing containers..."
sudo docker-compose down 2>/dev/null || true

# Pull latest images and start services
echo "Starting BulkSMS API services..."
sudo docker-compose up -d --build

# Wait for services to be healthy
echo "Waiting for services to start..."
sleep 30

# Check service status
echo "Checking service status..."
sudo docker-compose ps

# Show logs
echo "Recent logs:"
sudo docker-compose logs --tail=50

echo "Deployment completed!"
echo "API should be available at: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):8080"
echo "Database is accessible at: $(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):3306"
