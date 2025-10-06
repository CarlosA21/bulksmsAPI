
# Deployment script para EC2 usando Docker Hub
# Este script descarga la imagen desde Docker Hub en lugar de compilar localmente

set -e

echo "=== DEPLOYING BULKSMS API FROM DOCKER HUB TO EC2 ==="
echo ""

# Variables
DOCKER_USERNAME="carlosa21"
IMAGE_NAME="bulksms-api"
IMAGE_TAG="latest"
APP_DIR="/home/ec2-user/bulksms-api"

echo "Configuration:"
echo "Docker Hub Image: $DOCKER_USERNAME/$IMAGE_NAME:$IMAGE_TAG"
echo "App Directory: $APP_DIR"
echo ""

# Update system packages
echo "📦 Updating system packages..."
sudo yum update -y

# Install Docker if not already installed
if ! command -v docker &> /dev/null; then
    echo "🐳 Installing Docker..."
    sudo yum install -y docker
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -a -G docker ec2-user
    echo "✅ Docker installed successfully"
else
    echo "✅ Docker already installed"
    sudo systemctl start docker
fi

# Install Docker Compose if not already installed
if ! command -v docker-compose &> /dev/null; then
    echo "🔧 Installing Docker Compose..."
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    echo "✅ Docker Compose installed successfully"
else
    echo "✅ Docker Compose already installed"
fi

# Create application directory
echo "📁 Setting up application directory..."
sudo mkdir -p $APP_DIR
sudo chown ec2-user:ec2-user $APP_DIR
cd $APP_DIR

# Stop existing containers if running
echo "🛑 Stopping existing containers..."
sudo docker-compose -f docker-compose.dockerhub.yml down 2>/dev/null || true
sudo docker stop $(sudo docker ps -q) 2>/dev/null || true

# Pull latest image from Docker Hub
echo "⬇️ Pulling latest image from Docker Hub..."
sudo docker pull $DOCKER_USERNAME/$IMAGE_NAME:$IMAGE_TAG

# Verify image was pulled successfully
echo "🔍 Verifying image..."
sudo docker images | grep $IMAGE_NAME

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    echo "📝 Creating .env file..."
    cat > .env << EOF
# Database Configuration
DB_NAME=bulksmsdb
DB_USER=bulksmsuser
DB_PASSWORD=SecurePassword123!
DB_ROOT_PASSWORD=RootPassword123!

# JWT Configuration
JWT_SECRET=YourSuperSecretJWTKeyHere123456789

# Application Configuration
SPRING_PROFILES_ACTIVE=production
DDL_AUTO=update
SHOW_SQL=false
LOG_LEVEL=INFO

# Docker Configuration
DOCKER_USERNAME=$DOCKER_USERNAME
IMAGE_TAG=$IMAGE_TAG

# PayPal Configuration (Optional)
# PAYPAL_CLIENT_ID=your_paypal_client_id
# PAYPAL_CLIENT_SECRET=your_paypal_client_secret
# PAYPAL_MODE=live

# Stripe Configuration (Optional)
# STRIPE_API_KEY=your_stripe_api_key

# Email Configuration (Optional)
# MAIL_USERNAME=your_email@gmail.com
# MAIL_PASSWORD=your_app_password
EOF
    echo "⚠️  Please edit .env file with your actual configuration values"
fi

# Start services using Docker Hub image
echo "🚀 Starting BulkSMS API services from Docker Hub..."
sudo docker-compose -f docker-compose.dockerhub.yml up -d

# Wait for services to be healthy
echo "⏳ Waiting for services to start..."
sleep 30

# Check service status
echo "📊 Checking service status..."
sudo docker-compose -f docker-compose.dockerhub.yml ps

# Test API health
echo "🏥 Testing API health..."
for i in {1..10}; do
    if curl -f http://localhost:8080/actuator/health &>/dev/null; then
        echo "✅ API is healthy and responding!"
        break
    else
        echo "⏳ Waiting for API to be ready... (attempt $i/10)"
        sleep 10
    fi
done

# Show final status
echo ""
echo "=== DEPLOYMENT SUMMARY ==="
echo "✅ Image pulled from Docker Hub: $DOCKER_USERNAME/$IMAGE_NAME:$IMAGE_TAG"
echo "✅ Services started successfully"
echo ""
echo "🌐 API Endpoints:"
echo "  - Health Check: http://$(curl -s ifconfig.me):8080/actuator/health"
echo "  - API Documentation: http://$(curl -s ifconfig.me):8080/swagger-ui.html"
echo ""
echo "📝 Useful Commands:"
echo "  - View logs: sudo docker-compose -f docker-compose.dockerhub.yml logs -f"
echo "  - Restart services: sudo docker-compose -f docker-compose.dockerhub.yml restart"
echo "  - Stop services: sudo docker-compose -f docker-compose.dockerhub.yml down"
echo ""
echo "🎉 Deployment completed!"
