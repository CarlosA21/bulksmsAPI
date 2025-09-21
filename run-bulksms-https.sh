#!/bin/bash

# Script simple para ejecutar BulkSMS API con HTTPS (asume que ya tienes Docker instalado)
# Uso: ./run-bulksms-https.sh

set -e

echo "=== Ejecutando BulkSMS API con HTTPS ==="

# Variables
APP_DIR="/home/ec2-user/bulksms-api"
SSL_DIR="$APP_DIR/ssl"
KEYSTORE_FILE="$SSL_DIR/keystore.p12"
KEYSTORE_PASSWORD="changeit"

# Obtener IP pública
PUBLIC_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)

# Verificar si existe el certificado
if [ ! -f "$KEYSTORE_FILE" ]; then
    echo "❌ Certificado SSL no encontrado. Ejecuta primero: ./deploy-ec2-https.sh"
    exit 1
fi

# Detener contenedores existentes
echo "🛑 Deteniendo contenedores existentes..."
docker stop bulksms-api bulksms-mysql 2>/dev/null || true
docker rm bulksms-api bulksms-mysql 2>/dev/null || true

# Verificar si existe la red
if ! docker network ls | grep -q bulksms-network; then
    echo "🌐 Creando red Docker..."
    docker network create bulksms-network
fi

# Verificar si MySQL está corriendo
if ! docker ps | grep -q bulksms-mysql; then
    echo "🗄️  Iniciando MySQL..."
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

    echo "⏳ Esperando MySQL (60 segundos)..."
    sleep 60
fi

# Iniciar API con HTTPS
echo "🚀 Iniciando BulkSMS API con HTTPS..."
docker run -d \
  --name bulksms-api \
  --network bulksms-network \
  -p 8080:8080 \
  -p 8443:8443 \
  -v $KEYSTORE_FILE:/app/keystore.p12:ro \
  -e SSL_ENABLED=true \
  -e SSL_KEYSTORE=/app/keystore.p12 \
  -e SSL_KEYSTORE_PASSWORD=$KEYSTORE_PASSWORD \
  -e SSL_KEY_ALIAS=bulksmsapi \
  -e PORT=8443 \
  -e HTTP_PORT=8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://bulksms-mysql:3306/bulksmsdb \
  -e SPRING_DATASOURCE_USERNAME=bulksmsuser \
  -e SPRING_DATASOURCE_PASSWORD=bulksmspass \
  --restart unless-stopped \
  carlosa21/bulksms-api:latest

echo "⏳ Esperando que la API se inicie (30 segundos)..."
sleep 30

# Mostrar estado
echo ""
echo "📊 Estado de los contenedores:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo ""
echo "✅ ¡API ejecutándose!"
echo "🔒 HTTPS: https://$PUBLIC_IP:8443"
echo "🔓 HTTP:  http://$PUBLIC_IP:8080"
echo ""
echo "📋 Comandos útiles:"
echo "- Ver logs: docker logs -f bulksms-api"
echo "- Detener: docker stop bulksms-api"
echo "- Reiniciar: docker restart bulksms-api"
