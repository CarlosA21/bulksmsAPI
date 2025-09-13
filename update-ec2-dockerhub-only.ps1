# Script PowerShell para actualizar solo EC2 con la imagen de Docker Hub
# Este script solo actualiza EC2 asumiendo que la imagen ya está en Docker Hub

param(
    [Parameter(Mandatory=$true)]
    [string]$EC2_HOST,

    [Parameter(Mandatory=$true)]
    [string]$KEY_PATH,

    [Parameter(Mandatory=$false)]
    [string]$TAG = "latest"
)

Write-Host "=== ACTUALIZANDO EC2 CON IMAGEN DE DOCKER HUB ===" -ForegroundColor Green
Write-Host ""

# Variables de configuración
$EC2_USER = "ec2-user"
$APP_DIR = "/home/ec2-user/bulksms-api"
$DOCKER_USERNAME = "carlosa21"
$IMAGE_NAME = "bulksms-api"
$FULL_IMAGE_NAME = "$DOCKER_USERNAME/${IMAGE_NAME}:$TAG"

Write-Host "🔧 Configuration:" -ForegroundColor Yellow
Write-Host "EC2 Host: $EC2_HOST"
Write-Host "EC2 User: $EC2_USER"
Write-Host "Docker Image: $FULL_IMAGE_NAME"
Write-Host "App Directory: $APP_DIR"
Write-Host ""

# Verificar archivos necesarios
if (-not (Test-Path $KEY_PATH)) {
    Write-Host "❌ Error: Key file no encontrado en $KEY_PATH" -ForegroundColor Red
    exit 1
}

# Verificar conectividad SSH
Write-Host "🔍 Verificando conectividad con EC2..." -ForegroundColor Cyan
try {
    $sshTest = ssh -i $KEY_PATH -o ConnectTimeout=10 -o StrictHostKeyChecking=no $EC2_USER@$EC2_HOST "echo 'Connection OK'" 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "SSH connection failed: $sshTest"
    }
    Write-Host "✅ Conectividad EC2 verificada" -ForegroundColor Green
} catch {
    Write-Host "❌ Error conectando a EC2: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Ejecutar update en EC2
Write-Host "🔄 Actualizando servicios en EC2..." -ForegroundColor Cyan

$updateCommands = @"
cd $APP_DIR

echo "🛑 Deteniendo servicios actuales..."
sudo docker-compose -f docker-compose.dockerhub.yml down

echo "⬇️ Pulling imagen actualizada desde Docker Hub..."
sudo docker pull $FULL_IMAGE_NAME

echo "🧹 Limpiando imágenes antiguas..."
sudo docker image prune -f

echo "🚀 Iniciando servicios con imagen actualizada..."
sudo docker-compose -f docker-compose.dockerhub.yml up -d

echo "⏳ Esperando que los servicios inicien..."
sleep 30

echo "📊 Verificando estado de los servicios..."
sudo docker-compose -f docker-compose.dockerhub.yml ps

echo "🔍 Verificando imágenes Docker..."
sudo docker images | grep $IMAGE_NAME

echo "🏥 Verificando salud de la API..."
for i in {1..10}; do
    if curl -f http://localhost:8080/actuator/health &>/dev/null; then
        echo "✅ API actualizada y funcionando correctamente!"
        curl -s http://localhost:8080/actuator/health | head -5
        break
    else
        echo "⏳ Esperando que la API esté lista... (intento \$i/10)"
        sleep 10
    fi
done

echo ""
echo "📋 Logs recientes de la aplicación:"
sudo docker-compose -f docker-compose.dockerhub.yml logs --tail=15 bulksms-api

echo ""
echo "💾 Estado del sistema:"
echo "CPU y Memoria:"
top -bn1 | head -5

echo ""
echo "🌐 Puertos abiertos:"
sudo netstat -tlnp | grep :8080
"@

ssh -i $KEY_PATH $EC2_USER@$EC2_HOST $updateCommands

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error ejecutando comandos en EC2" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=== ACTUALIZACIÓN EC2 COMPLETADA ===" -ForegroundColor Green
Write-Host "✅ Servicios actualizados en EC2" -ForegroundColor Green
Write-Host "✅ Imagen Docker Hub: $FULL_IMAGE_NAME" -ForegroundColor Green
Write-Host ""
Write-Host "🌐 Tu API debería estar disponible en:" -ForegroundColor Yellow
Write-Host "  - Health Check: http://${EC2_HOST}:8080/actuator/health" -ForegroundColor White
Write-Host "  - API Documentation: http://${EC2_HOST}:8080/swagger-ui.html" -ForegroundColor White
Write-Host ""
Write-Host "📝 Comandos útiles para EC2:" -ForegroundColor Yellow
Write-Host "  - Ver logs: ssh -i $KEY_PATH $EC2_USER@$EC2_HOST 'cd $APP_DIR && sudo docker-compose -f docker-compose.dockerhub.yml logs -f'" -ForegroundColor White
Write-Host "  - Reiniciar: ssh -i $KEY_PATH $EC2_USER@$EC2_HOST 'cd $APP_DIR && sudo docker-compose -f docker-compose.dockerhub.yml restart'" -ForegroundColor White
Write-Host "  - Estado: ssh -i $KEY_PATH $EC2_USER@$EC2_HOST 'cd $APP_DIR && sudo docker-compose -f docker-compose.dockerhub.yml ps'" -ForegroundColor White
Write-Host ""
Write-Host "🎉 ¡Actualización completada!" -ForegroundColor Green
