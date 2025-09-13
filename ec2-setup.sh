#!/bin/bash

# EC2 Initial Setup Script
# Run this script when you first set up your EC2 instance

echo "Setting up EC2 instance for BulkSMS API..."

# Update system
sudo yum update -y

# Install Git
sudo yum install -y git

# Install Docker
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -a -G docker ec2-user

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Create application directory
mkdir -p /home/ec2-user/bulksms-api
cd /home/ec2-user/bulksms-api

# Create necessary directories
mkdir -p logs
mkdir -p data/mysql

# Set up firewall rules (if using Amazon Linux)
# Allow HTTP, HTTPS, SSH, and custom ports
sudo iptables -A INPUT -p tcp --dport 22 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 80 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 443 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 8080 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 3306 -j ACCEPT

echo "EC2 setup completed!"
echo "Next steps:"
echo "1. Clone your repository or upload your files"
echo "2. Copy .env.production to .env and update values"
echo "3. Run ./deploy-to-ec2.sh to start the application"
