# Script PowerShell para actualizar la configuración en EC2 desde Windows
# Este script copia el .env.production actualizado y reinicia los servicios

param(
    [Parameter(Mandatory=$true)]
    [string]$EC2_HOST,

    [Parameter(Mandatory=$true)]
    [string]$KEY_PATH
)

Write-Host "=== ACTUALIZANDO BULKSMS API EN EC2 ===" -ForegroundColor Green
Write-Host ""

# Variables de configuración
$EC2_USER = "ec2-user"
$APP_DIR = "/home/ec2-user/bulksms-api"

Write-Host "🔧 Configuration:" -ForegroundColor Yellow
Write-Host "EC2 Host: $EC2_HOST"
Write-Host "EC2 User: $EC2_USER"
Write-Host "App Directory: $APP_DIR"
Write-Host ""

# Verificar que los archivos necesarios existen
if (-not (Test-Path ".env.production")) {
    Write-Host "❌ Error: .env.production no encontrado" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path "docker-compose.dockerhub.yml")) {
    Write-Host "❌ Error: docker-compose.dockerhub.yml no encontrado" -ForegroundColor Red
    exit 1
}

# Verificar que scp está disponible (OpenSSH)
try {
    scp 2>$null
} catch {
    Write-Host "❌ Error: scp no está disponible. Instala OpenSSH Client desde Windows Features" -ForegroundColor Red
    exit 1
}

# Copiar archivos actualizados a EC2
Write-Host "📤 Copiando archivos actualizados a EC2..." -ForegroundColor Cyan
scp -i $KEY_PATH .env.production ${EC2_USER}@${EC2_HOST}:${APP_DIR}/.env
scp -i $KEY_PATH docker-compose.dockerhub.yml ${EC2_USER}@${EC2_HOST}:${APP_DIR}/
scp -i $KEY_PATH deploy-dockerhub-to-ec2.sh ${EC2_USER}@${EC2_HOST}:${APP_DIR}/

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error copiando archivos" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Archivos copiados exitosamente" -ForegroundColor Green

# Ejecutar comandos en EC2 para actualizar y reiniciar
Write-Host "🔄 Actualizando servicios en EC2..." -ForegroundColor Cyan

$updateCommands = @"
cd /home/ec2-user/bulksms-api

echo "🛑 Deteniendo servicios actuales..."
sudo docker-compose -f docker-compose.dockerhub.yml down

echo "⬇️ Pulling latest image..."
sudo docker pull carlosa21/bulksms-api:latest

echo "🧹 Limpiando contenedores e imágenes antiguas..."
sudo docker system prune -f

echo "🚀 Iniciando servicios con nueva configuración..."
sudo docker-compose -f docker-compose.dockerhub.yml up -d

echo "⏳ Esperando que los servicios inicien..."
sleep 30

echo "📊 Verificando estado de los servicios..."
sudo docker-compose -f docker-compose.dockerhub.yml ps

echo "🏥 Verificando salud de la API..."
for i in {1..10}; do
    if curl -f http://localhost:8080/actuator/health &>/dev/null; then
        echo "✅ API está funcionando correctamente!"
        break
    else
        echo "⏳ Esperando que la API esté lista... (intento `$i/10)"
        sleep 10
    fi
done

echo "📋 Mostrando logs recientes..."
sudo docker-compose -f docker-compose.dockerhub.yml logs --tail=20
"@

ssh -i $KEY_PATH ${EC2_USER}@${EC2_HOST} $updateCommands

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error ejecutando comandos en EC2" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=== ACTUALIZACIÓN COMPLETADA ===" -ForegroundColor Green
Write-Host "✅ Configuración actualizada" -ForegroundColor Green
Write-Host "✅ Servicios reiniciados" -ForegroundColor Green
Write-Host ""
Write-Host "🌐 Tu API debería estar disponible en:" -ForegroundColor Yellow
Write-Host "  - Health Check: http://${EC2_HOST}:8080/actuator/health" -ForegroundColor White
Write-Host "  - API Documentation: http://${EC2_HOST}:8080/swagger-ui.html" -ForegroundColor White
Write-Host ""
Write-Host "📝 Para verificar logs en tiempo real:" -ForegroundColor Yellow
Write-Host "ssh -i $KEY_PATH ${EC2_USER}@${EC2_HOST} 'cd $APP_DIR && sudo docker-compose -f docker-compose.dockerhub.yml logs -f'" -ForegroundColor White
Write-Host ""
Write-Host "🎉 ¡Actualización completada!" -ForegroundColor Green
