# BulkSMS API - Docker Deployment Guide

This Spring Boot API has been optimized for Docker deployment with production-ready configurations.

## 🚀 Quick Start

### Prerequisites
- Docker
- Docker Compose

### 1. Environment Setup
```bash
# Copy the environment template
cp .env.example .env

# Edit .env with your actual values
# Important: Replace default values with secure ones for production
```

### 2. Build and Run
```bash
# Build and start all services
docker-compose up --build

# Or run in detached mode
docker-compose up -d --build
```

The API will be available at: `http://localhost:8080`

## 📋 Configuration

### Environment Variables
All sensitive configurations are externalized through environment variables. Key variables include:

**Database:**
- `DB_NAME`: Database name (default: bulksmsdb)
- `DB_USER`: Database user (default: bulksmsuser)
- `DB_PASSWORD`: Database password
- `DB_ROOT_PASSWORD`: MySQL root password

**Security:**
- `JWT_SECRET`: JWT signing secret (change for production!)
- `PAYPAL_CLIENT_ID` & `PAYPAL_CLIENT_SECRET`: PayPal credentials
- `STRIPE_API_KEY`: Stripe API key
- `GOOGLE_CLIENT_ID` & `GOOGLE_CLIENT_SECRET`: Google OAuth credentials

**Email:**
- `MAIL_USERNAME` & `MAIL_PASSWORD`: Email service credentials

### Production Deployment
1. **Security**: Change all default passwords and secrets
2. **SSL**: Configure SSL/TLS for production
3. **Reverse Proxy**: Use nginx or similar for load balancing
4. **Monitoring**: Health checks are available at `/actuator/health`

## 🛠️ Docker Services

### bulksms-api
- **Port**: 8080
- **Health Check**: `/actuator/health`
- **Logs**: `docker-compose logs bulksms-api`

### bulksms-mysql
- **Port**: 3306
- **Data Persistence**: Named volume `db_data`
- **Health Check**: MySQL ping

## 📊 Monitoring

Health checks and metrics are available through Spring Boot Actuator:
- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Info: `http://localhost:8080/actuator/info`

## 🔧 Development

### Build Only
```bash
# Build the application
docker-compose build

# Build without cache
docker-compose build --no-cache
```

### Database Access
```bash
# Connect to MySQL container
docker-compose exec bulksms-mysql mysql -u bulksmsuser -p bulksmsdb
```

### Logs
```bash
# View all logs
docker-compose logs

# Follow logs
docker-compose logs -f

# Service-specific logs
docker-compose logs bulksms-api
docker-compose logs bulksms-mysql
```

### Cleanup
```bash
# Stop and remove containers
docker-compose down

# Remove containers and volumes (WARNING: This deletes all data)
docker-compose down -v

# Remove containers, volumes, and images
docker-compose down -v --rmi all
```

## 🏗️ Architecture

- **Multi-stage Dockerfile**: Optimized for production with minimal runtime image
- **Non-root user**: Security best practice implementation
- **Health checks**: Built-in health monitoring
- **Persistent storage**: MySQL data persisted in Docker volumes
- **Network isolation**: Services communicate through dedicated Docker network

## 🔒 Security Features

- Non-root container execution
- Environment variable configuration
- Health check endpoints
- Secure defaults with override capability
- Network isolation between services

## 📝 Notes

- The application uses MySQL 8.0 with persistent storage
- JVM is optimized for container environments
- All sensitive data should be configured through environment variables
- Default credentials are for development only - change for production!
