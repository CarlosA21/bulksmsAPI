#!/usr/bin/env pwsh
# Script para acceder al contenedor MySQL y corregir el rol del usuario admin

Write-Host "=== Corrigiendo rol de usuario admin en MySQL ===" -ForegroundColor Green

# Verificar que Docker esté corriendo
$dockerRunning = docker ps 2>$null
if (-not $?) {
    Write-Host "Error: Docker no está corriendo o no está disponible" -ForegroundColor Red
    exit 1
}

# Buscar el contenedor MySQL
$mysqlContainer = docker ps --filter "name=mysql" --format "{{.Names}}" | Select-Object -First 1
if (-not $mysqlContainer) {
    $mysqlContainer = docker ps --filter "ancestor=mysql" --format "{{.Names}}" | Select-Object -First 1
}

if (-not $mysqlContainer) {
    Write-Host "No se encontró contenedor MySQL corriendo" -ForegroundColor Red
    Write-Host "Contenedores disponibles:" -ForegroundColor Yellow
    docker ps --format "table {{.Names}}\t{{.Image}}\t{{.Status}}"
    exit 1
}

Write-Host "Contenedor MySQL encontrado: $mysqlContainer" -ForegroundColor Green

# Ejecutar script SQL para corregir roles
Write-Host "Ejecutando corrección de roles..." -ForegroundColor Yellow

$sqlCommands = @"
USE bulksmsdb;
SELECT 'Usuarios antes de la corrección:' as mensaje;
SELECT user_id, username, email, roles FROM User WHERE username LIKE '%admin%' OR email LIKE '%admin%';
UPDATE User SET roles = 'ADMIN' WHERE (username LIKE '%admin%' OR email LIKE '%admin%') AND roles != 'ADMIN';
SELECT 'Usuarios después de la corrección:' as mensaje;
SELECT user_id, username, email, roles FROM User WHERE username LIKE '%admin%' OR email LIKE '%admin%';
"@

# Ejecutar comandos SQL en el contenedor
docker exec -i $mysqlContainer mysql -u bulksmsuser -pbulksmspass bulksmsdb -e $sqlCommands

if ($?) {
    Write-Host "✅ Corrección completada exitosamente" -ForegroundColor Green
    Write-Host "Ahora reinicia tu aplicación Spring Boot para que los cambios surtan efecto" -ForegroundColor Yellow
} else {
    Write-Host "❌ Error al ejecutar la corrección" -ForegroundColor Red
}
