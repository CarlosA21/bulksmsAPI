#!/bin/bash

# Script simple para deployment en EC2
# Ejecutar este script línea por línea en la consola EC2

echo "=== Iniciando deployment BulkSMS API ==="

# Paso 1: Limpiar contenedores existentes
echo "1. Limpiando contenedores existentes..."
docker stop bulksms-api bulksms-mysql 2>/dev/null || true
docker rm bulksms-api bulksms-mysql 2>/dev/null || true
docker network rm bulksms-network 2>/dev/null || true

# Paso 2: Descargar imágenes necesarias
echo "2. Descargando imagen MySQL..."
docker pull mysql:8.0

echo "3. Descargando imagen BulkSMS API..."
docker pull carlosa21/bulksms-api:latest

# Paso 3: Crear red Docker
echo "4. Creando red Docker..."
docker network create bulksms-network

# Paso 4: Ejecutar MySQL
echo "5. Iniciando contenedor MySQL..."
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

# Paso 5: Esperar que MySQL inicie
echo "6. Esperando que MySQL inicie (90 segundos)..."
sleep 90

# Paso 6: Ejecutar aplicación
echo "7. Iniciando aplicación BulkSMS API..."
docker run -d \
  --name bulksms-api \
  --network bulksms-network \
  -p 8080:8080 \
  --restart unless-stopped \
  carlosa21/bulksms-api:latest

# Paso 7: Verificar estado
echo "8. Verificando estado..."
sleep 30
docker ps

echo ""
echo "=== Deployment completado ==="
echo "API disponible en: http://TU_IP_EC2:8080"
echo ""
echo "Para ver logs: docker logs -f bulksms-api"
echo "Para ver estado: docker ps"
