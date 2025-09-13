#!/bin/bash

# Script para subir tu aplicación BulkSMS API a Docker Hub
# Ejecuta este script desde tu directorio del proyecto

set -e

echo "=== SUBIENDO BULKSMS API A DOCKER HUB ==="
echo ""

# Variables - ACTUALIZA ESTOS VALORES
DOCKER_USERNAME="carlosa21"
IMAGE_NAME="bulksms-api"
TAG="latest"
FULL_IMAGE_NAME="$DOCKER_USERNAME/$IMAGE_NAME:$TAG"

echo "Configuración:"
echo "Usuario Docker Hub: $DOCKER_USERNAME"
echo "Nombre de imagen: $IMAGE_NAME"
echo "Tag: $TAG"
echo "Imagen completa: $FULL_IMAGE_NAME"
echo ""

# Verificar que Docker está instalado y corriendo
if ! command -v docker &> /dev/null; then
    echo "❌ Error: Docker no está instalado"
    exit 1
fi

echo "✅ Docker está instalado"

# Login a Docker Hub
echo "🔐 Iniciando sesión en Docker Hub..."
echo "Ingresa tu password de Docker Hub cuando se solicite:"
docker login

# Construir la imagen
echo "🏗️ Construyendo la imagen Docker..."
docker build -t $FULL_IMAGE_NAME .

# Verificar que la imagen se construyó correctamente
echo "📋 Verificando imagen construida..."
docker images | grep $IMAGE_NAME

# Subir la imagen a Docker Hub
echo "⬆️ Subiendo imagen a Docker Hub..."
docker push $FULL_IMAGE_NAME

# Crear tag 'latest' si no es latest
if [ "$TAG" != "latest" ]; then
    echo "🏷️ Creando tag 'latest'..."
    docker tag $FULL_IMAGE_NAME $DOCKER_USERNAME/$IMAGE_NAME:latest
    docker push $DOCKER_USERNAME/$IMAGE_NAME:latest
fi

echo ""
echo "✅ ¡SUBIDA COMPLETADA!"
echo ""
echo "Tu imagen está disponible en:"
echo "https://hub.docker.com/r/$DOCKER_USERNAME/$IMAGE_NAME"
echo ""
echo "Para usar tu imagen desde Docker Hub:"
echo "docker pull $FULL_IMAGE_NAME"
echo "docker run -p 8080:8080 $FULL_IMAGE_NAME"
echo ""
echo "Para usar en docker-compose, cambia 'build: .' por:"
echo "image: $FULL_IMAGE_NAME"
