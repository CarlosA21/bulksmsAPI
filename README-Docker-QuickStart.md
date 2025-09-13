# BulkSMS API - Docker Quick Start Guide

## 🚀 Running with Docker

Since Docker works from your regular command prompt but not from the project terminal, here are several ways to run your Spring Boot API:

### Option 1: Use the Helper Scripts (Recommended)
I've created helper scripts in your project directory:

**For Command Prompt:**
```cmd
start-docker.bat
```

**For PowerShell:**
```powershell
.\start-docker.ps1
```

**To stop the containers:**
```cmd
stop-docker.bat
```

### Option 2: Regular Command Prompt
1. Open **Regular Command Prompt** (not project terminal)
2. Navigate to your project:
   ```cmd
   cd "E:\SELLING PROJECTS\SMS Web\bulksmsAPI"
   ```
3. Start the application:
   ```cmd
   docker compose up --build
   ```

### Option 3: PowerShell
1. Open **Regular PowerShell** (not project terminal)
2. Navigate to your project:
   ```powershell
   cd "E:\SELLING PROJECTS\SMS Web\bulksmsAPI"
   ```
3. Start the application:
   ```powershell
   docker compose up --build
   ```

## 📋 What happens when you run?

1. **MySQL Database** will start on port 3306
2. **Spring Boot API** will start on port 8080
3. **All PayPal and Stripe configurations** will be loaded from your `.env` file
4. **Health checks** will ensure services are running properly

## 🔧 Managing the Application

### View Logs
```cmd
docker compose logs -f
```

### Stop Services
```cmd
docker compose down
```

### Rebuild and Restart
```cmd
docker compose down
docker compose up --build
```

### Access Your API
- **API Base URL**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **Database**: localhost:3306 (bulksmsdb)

## ⚙️ Environment Configuration

Your PayPal and Stripe settings are in the `.env` file. To change them:

1. Edit `.env` file
2. Restart Docker: `docker compose down && docker compose up`

## 🛠️ Troubleshooting

If you get "docker-compose not recognized":
- Use `docker compose` (without hyphen) - newer Docker versions
- Or use the helper scripts I created
- Make sure to run from regular command prompt, not IDE terminal

## 📝 Files Created

I've added these helper files to your project:
- `start-docker.bat` - Start services (Command Prompt)
- `start-docker.ps1` - Start services (PowerShell) 
- `stop-docker.bat` - Stop services
- `README-Docker-QuickStart.md` - This guide
