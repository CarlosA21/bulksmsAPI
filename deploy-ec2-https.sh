#!/bin/bash

# Script para ejecutar BulkSMS API con HTTPS en EC2
# Este script configura SSL y ejecuta la aplicación con certificado auto-firmado

set -e

echo "=== Configurando BulkSMS API con HTTPS en EC2 ==="

# Variables de configuración
APP_DIR="/home/ec2-user/bulksms-api"
SSL_DIR="$APP_DIR/ssl"
KEYSTORE_FILE="$SSL_DIR/keystore.p12"
KEYSTORE_PASSWORD="changeit"
SSL_ALIAS="bulksmsapi"

# Obtener IP pública de EC2
PUBLIC_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)
echo "IP pública de EC2: $PUBLIC_IP"

# Crear directorio de la aplicación
mkdir -p $APP_DIR
mkdir -p $SSL_DIR
cd $APP_DIR

# Verificar si Java está instalado (necesario para keytool)
if ! command -v keytool &> /dev/null; then
    echo "Instalando OpenJDK..."
    sudo yum update -y
    sudo yum install -y java-17-amazon-corretto java-17-amazon-corretto-devel
fi

# Generar certificado SSL auto-firmado
echo "Generando certificado SSL auto-firmado..."
keytool -genkeypair \
    -alias $SSL_ALIAS \
    -keyalg RSA \
    -keysize 2048 \
    -storetype PKCS12 \
    -keystore $KEYSTORE_FILE \
    -validity 365 \
    -dname "CN=$PUBLIC_IP, OU=BulkSMS, O=BulkSMS API, L=City, S=State, C=US" \
    -storepass $KEYSTORE_PASSWORD \
    -keypass $KEYSTORE_PASSWORD

echo "Certificado SSL generado en: $KEYSTORE_FILE"

# Verificar si Docker está instalado
if ! command -v docker &> /dev/null; then
    echo "Instalando Docker..."
    sudo yum install -y docker
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -a -G docker ec2-user
    echo "Docker instalado. Por favor, cierra sesión y vuelve a entrar, luego ejecuta este script nuevamente."
    exit 1
fi

# Iniciar servicio Docker
sudo systemctl start docker

# Detener contenedores existentes
echo "Deteniendo contenedores existentes..."
docker stop bulksms-api bulksms-mysql 2>/dev/null || true
docker rm bulksms-api bulksms-mysql 2>/dev/null || true
docker network rm bulksms-network 2>/dev/null || true

# Crear red Docker
echo "Creando red Docker..."
docker network create bulksms-network

# Iniciar contenedor MySQL
echo "Iniciando contenedor MySQL..."
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

# Esperar a que MySQL se inicialice
echo "Esperando a que MySQL se inicialice (90 segundos)..."
sleep 90

# Iniciar contenedor BulkSMS API con HTTPS habilitado
echo "Iniciando BulkSMS API con HTTPS..."
docker run -d \
  --name bulksms-api \
  --network bulksms-network \
  -p 8080:8080 \
  -p 8443:8443 \
  -v $KEYSTORE_FILE:/app/keystore.p12:ro \
  -e SSL_ENABLED=true \
  -e SSL_KEYSTORE=/app/keystore.p12 \
  -e SSL_KEYSTORE_PASSWORD=$KEYSTORE_PASSWORD \
  -e SSL_KEY_ALIAS=$SSL_ALIAS \
  -e PORT=8443 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://bulksms-mysql:3306/bulksmsdb \
  -e SPRING_DATASOURCE_USERNAME=bulksmsuser \
  -e SPRING_DATASOURCE_PASSWORD=bulksmspass \
  --restart unless-stopped \
  carlosa21/bulksms-api:latest

# Esperar a que los servicios se inicien
echo "Esperando a que los servicios se inicien (60 segundos)..."
sleep 60

# Verificar estado de los servicios
echo "Verificando estado de los servicios..."
docker ps

echo ""
echo "=== Logs de MySQL ==="
docker logs --tail=10 bulksms-mysql

echo ""
echo "=== Logs de BulkSMS API ==="
docker logs --tail=20 bulksms-api

echo ""
echo "=== ¡Despliegue completado! ==="
echo "🔒 API HTTPS disponible en: https://$PUBLIC_IP:8443"
echo "🔓 API HTTP disponible en: http://$PUBLIC_IP:8080"
echo "🗄️  Base de datos disponible en: $PUBLIC_IP:3306"
echo ""
echo "📋 Comandos útiles:"
echo "- Ver logs de API: docker logs -f bulksms-api"
echo "- Ver logs de MySQL: docker logs -f bulksms-mysql"
echo "- Verificar estado: docker ps"
echo "- Detener todo: docker stop bulksms-api bulksms-mysql"
echo "- Reiniciar API: docker restart bulksms-api"
echo ""
echo "⚠️  IMPORTANTE:"
echo "- El certificado es auto-firmado, el navegador mostrará advertencia de seguridad"
echo "- Para producción, reemplaza con un certificado válido de una CA"
echo "- Asegúrate de que el Security Group de EC2 permita tráfico en los puertos 8080 y 8443"
echo ""
echo "🔧 Configuración de Security Group necesaria:"
echo "- Puerto 22 (SSH): 0.0.0.0/0"
echo "- Puerto 8080 (HTTP): 0.0.0.0/0"
echo "- Puerto 8443 (HTTPS): 0.0.0.0/0"
echo "- Puerto 3306 (MySQL): solo si necesitas acceso externo"
