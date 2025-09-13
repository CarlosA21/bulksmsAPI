#!/bin/bash

# Script para actualizar la configuración en EC2 y reiniciar servicios
# Este script copia el .env.production actualizado y reinicia los contenedores

set -e

echo "=== ACTUALIZANDO BULKSMS API EN EC2 ==="
echo ""

# Variables de configuración
EC2_HOST="your-ec2-public-ip"  # ACTUALIZA ESTA IP
EC2_USER="ec2-user"
KEY_PATH="~/.ssh/your-key.pem"  # ACTUALIZA LA RUTA DE TU KEY
APP_DIR="/home/ec2-user/bulksms-api"

echo "🔧 Configuration:"
echo "EC2 Host: $EC2_HOST"
echo "EC2 User: $EC2_USER"
echo "App Directory: $APP_DIR"
echo ""

# Verificar que los archivos necesarios existen
if [ ! -f ".env.production" ]; then
    echo "❌ Error: .env.production no encontrado"
    exit 1
fi

if [ ! -f "docker-compose.dockerhub.yml" ]; then
    echo "❌ Error: docker-compose.dockerhub.yml no encontrado"
    exit 1
fi

# Copiar archivos actualizados a EC2
echo "📤 Copiando archivos actualizados a EC2..."
scp -i $KEY_PATH .env.production $EC2_USER@$EC2_HOST:$APP_DIR/.env
scp -i $KEY_PATH docker-compose.dockerhub.yml $EC2_USER@$EC2_HOST:$APP_DIR/
scp -i $KEY_PATH deploy-dockerhub-to-ec2.sh $EC2_USER@$EC2_HOST:$APP_DIR/

echo "✅ Archivos copiados exitosamente"

# Ejecutar comandos en EC2 para actualizar y reiniciar
echo "🔄 Actualizando servicios en EC2..."
ssh -i $KEY_PATH $EC2_USER@$EC2_HOST << 'EOF'
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
            echo "⏳ Esperando que la API esté lista... (intento $i/10)"
            sleep 10
        fi
    done

    echo "📋 Mostrando logs recientes..."
    sudo docker-compose -f docker-compose.dockerhub.yml logs --tail=20
EOF

echo ""
echo "=== ACTUALIZACIÓN COMPLETADA ==="
echo "✅ Configuración actualizada"
echo "✅ Servicios reiniciados"
echo ""
echo "🌐 Tu API debería estar disponible en:"
echo "  - Health Check: http://$EC2_HOST:8080/actuator/health"
echo "  - API Documentation: http://$EC2_HOST:8080/swagger-ui.html"
echo ""
echo "📝 Para verificar logs en tiempo real:"
echo "ssh -i $KEY_PATH $EC2_USER@$EC2_HOST 'cd $APP_DIR && sudo docker-compose -f docker-compose.dockerhub.yml logs -f'"
echo ""
echo "🎉 ¡Actualización completada!"
