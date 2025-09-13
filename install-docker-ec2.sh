#!/bin/bash

# Script para instalar Docker y Docker Compose en EC2 Amazon Linux
# Y ejecutar la aplicación BulkSMS API desde Docker Hub

set -e

echo "=== INSTALANDO DOCKER Y DOCKER COMPOSE EN EC2 ==="
echo ""

# Actualizar el sistema
echo "📦 Actualizando paquetes del sistema..."
sudo yum update -y

# Instalar Docker si no está instalado
if ! command -v docker &> /dev/null; then
    echo "🐳 Instalando Docker..."
    sudo yum install -y docker
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -a -G docker ec2-user
    echo "✅ Docker instalado exitosamente"
else
    echo "✅ Docker ya está instalado"
    sudo systemctl start docker
fi

# Instalar Docker Compose
echo "🔧 Instalando Docker Compose..."
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Crear enlace simbólico para que funcione el comando
sudo ln -sf /usr/local/bin/docker-compose /usr/bin/docker-compose

# Verificar instalación
echo "🔍 Verificando instalaciones..."
docker --version
docker-compose --version

echo ""
echo "✅ Docker y Docker Compose instalados correctamente"
echo ""
echo "🔄 Nota: Es posible que necesites cerrar y volver a abrir tu sesión SSH"
echo "para que los cambios de grupo surtan efecto."
echo ""
echo "Para continuar sin reiniciar la sesión, usa 'sudo' con docker-compose:"
echo "sudo docker-compose -f docker-compose.dockerhub.yml up -d"
