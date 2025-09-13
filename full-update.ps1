# Script PowerShell para hacer update completo: rebuild, push a Docker Hub y update en EC2
# Este script reconstruye la imagen, la sube a Docker Hub y actualiza EC2

param(
    [Parameter(Mandatory=$false)]
    [string]$EC2_HOST = "",

    [Parameter(Mandatory=$false)]
    [string]$KEY_PATH = "",

    [Parameter(Mandatory=$false)]
    [string]$TAG = "latest",

    [Parameter(Mandatory=$false)]
    [switch]$SkipEC2Update
)

Write-Host "=== UPDATE COMPLETO: BUILD → DOCKER HUB → EC2 ===" -ForegroundColor Green
Write-Host ""

# Variables de configuración
$DOCKER_USERNAME = "carlosa21"
$IMAGE_NAME = "bulksms-api"
$FULL_IMAGE_NAME = "$DOCKER_USERNAME/${IMAGE_NAME}:$TAG"
$DOCKER_PATH = "C:\Program Files\Docker\Docker\resources\bin\docker.exe"

Write-Host "🔧 Configuration:" -ForegroundColor Yellow
Write-Host "Docker Image: $FULL_IMAGE_NAME"
Write-Host "Tag: $TAG"
if (-not $SkipEC2Update) {
    Write-Host "EC2 Host: $EC2_HOST"
    Write-Host "Key Path: $KEY_PATH"
}
Write-Host ""

# Verificar que Docker está funcionando
Write-Host "🐳 Verificando Docker..." -ForegroundColor Cyan
try {
    $dockerVersion = & $DOCKER_PATH --version
    Write-Host "✅ Docker disponible: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: Docker no está disponible" -ForegroundColor Red
    exit 1
}

# Verificar que Docker está corriendo
try {
    & $DOCKER_PATH info | Out-Null
    Write-Host "✅ Docker está corriendo" -ForegroundColor Green
} catch {
    Write-Host "❌ Error: Docker no está corriendo. Inicia Docker Desktop" -ForegroundColor Red
    exit 1
}

# Paso 1: Rebuild de la imagen con los cambios actuales
Write-Host "🏗️ Reconstruyendo imagen con cambios actuales..." -ForegroundColor Cyan
& $DOCKER_PATH build -t $FULL_IMAGE_NAME .

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error construyendo la imagen" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Imagen reconstruida exitosamente" -ForegroundColor Green

# Paso 2: Verificar que el usuario está logueado en Docker Hub
Write-Host "🔐 Verificando login en Docker Hub..." -ForegroundColor Cyan
$loginCheck = & $DOCKER_PATH images | Select-String $IMAGE_NAME
if (-not $loginCheck) {
    Write-Host "⚠️ No se encuentra imagen local. Verificando login..." -ForegroundColor Yellow
}

# Intentar un comando que requiera autenticación
try {
    & $DOCKER_PATH push $FULL_IMAGE_NAME 2>&1 | Out-Null
} catch {
    Write-Host "🔐 Necesitas hacer login en Docker Hub..." -ForegroundColor Yellow
    & $DOCKER_PATH login

    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Error en login de Docker Hub" -ForegroundColor Red
        exit 1
    }
}

# Paso 3: Push de la imagen actualizada a Docker Hub
Write-Host "⬆️ Subiendo imagen actualizada a Docker Hub..." -ForegroundColor Cyan
& $DOCKER_PATH push $FULL_IMAGE_NAME

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error subiendo imagen a Docker Hub" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Imagen subida exitosamente a Docker Hub" -ForegroundColor Green

# Paso 4: Crear tag 'latest' si se usó un tag diferente
if ($TAG -ne "latest") {
    Write-Host "🏷️ Creando tag 'latest'..." -ForegroundColor Cyan
    & $DOCKER_PATH tag $FULL_IMAGE_NAME "$DOCKER_USERNAME/${IMAGE_NAME}:latest"
    & $DOCKER_PATH push "$DOCKER_USERNAME/${IMAGE_NAME}:latest"
    Write-Host "✅ Tag 'latest' creado y subido" -ForegroundColor Green
}

# Paso 5: Actualizar EC2 (opcional)
if (-not $SkipEC2Update) {
    if ([string]::IsNullOrEmpty($EC2_HOST) -or [string]::IsNullOrEmpty($KEY_PATH)) {
        Write-Host "⚠️ Parámetros de EC2 no proporcionados. Saltando actualización de EC2." -ForegroundColor Yellow
        Write-Host "Para actualizar EC2 manualmente:" -ForegroundColor Yellow
        Write-Host ".\update-ec2-dockerhub-only.ps1 -EC2_HOST 'tu-ip' -KEY_PATH 'tu-key.pem'" -ForegroundColor White
    } else {
        Write-Host "🚀 Actualizando EC2 con la nueva imagen..." -ForegroundColor Cyan

        # Verificar archivos necesarios para EC2
        if (-not (Test-Path $KEY_PATH)) {
            Write-Host "❌ Error: Key file no encontrado en $KEY_PATH" -ForegroundColor Red
            exit 1
        }

        # Verificar conectividad SSH
        Write-Host "🔍 Verificando conectividad con EC2..." -ForegroundColor Cyan
        $sshTest = ssh -i $KEY_PATH -o ConnectTimeout=10 -o StrictHostKeyChecking=no ec2-user@$EC2_HOST "echo 'Connection OK'" 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Host "❌ Error conectando a EC2: $sshTest" -ForegroundColor Red
            exit 1
        }
        Write-Host "✅ Conectividad EC2 verificada" -ForegroundColor Green

        # Ejecutar update en EC2
        $ec2UpdateCommands = @"
cd /home/ec2-user/bulksms-api

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

echo "🏥 Verificando salud de la API..."
for i in {1..10}; do
    if curl -f http://localhost:8080/actuator/health &>/dev/null; then
        echo "✅ API actualizada y funcionando!"
        break
    else
        echo "⏳ Esperando que la API esté lista... (intento `$i/10)"
        sleep 10
    fi
done

echo "📋 Logs recientes:"
sudo docker-compose -f docker-compose.dockerhub.yml logs --tail=10 bulksms-api
"@

        ssh -i $KEY_PATH ec2-user@$EC2_HOST $ec2UpdateCommands

        if ($LASTEXITCODE -ne 0) {
            Write-Host "❌ Error actualizando EC2" -ForegroundColor Red
            exit 1
        }

        Write-Host "✅ EC2 actualizado exitosamente" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "=== UPDATE COMPLETADO ===" -ForegroundColor Green
Write-Host "✅ Imagen reconstruida con cambios actuales" -ForegroundColor Green
Write-Host "✅ Imagen subida a Docker Hub: $FULL_IMAGE_NAME" -ForegroundColor Green
if (-not $SkipEC2Update -and -not [string]::IsNullOrEmpty($EC2_HOST)) {
    Write-Host "✅ EC2 actualizado con nueva imagen" -ForegroundColor Green
}
Write-Host ""
Write-Host "🌐 Imagen disponible en:" -ForegroundColor Yellow
Write-Host "  - Docker Hub: https://hub.docker.com/r/$DOCKER_USERNAME/$IMAGE_NAME" -ForegroundColor White
if (-not [string]::IsNullOrEmpty($EC2_HOST)) {
    Write-Host "  - API Health: http://${EC2_HOST}:8080/actuator/health" -ForegroundColor White
    Write-Host "  - API Docs: http://${EC2_HOST}:8080/swagger-ui.html" -ForegroundColor White
}
Write-Host ""
Write-Host "📝 Para usar la imagen actualizada:" -ForegroundColor Yellow
Write-Host "docker pull $FULL_IMAGE_NAME" -ForegroundColor White
Write-Host "docker run -p 8080:8080 $FULL_IMAGE_NAME" -ForegroundColor White
Write-Host ""
Write-Host "🎉 ¡Update completado exitosamente!" -ForegroundColor Green
