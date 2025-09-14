#!/bin/bash

# Iniciar aplicación
echo "8. Iniciando aplicación..."
sudo docker run -d \
  --name bulksms-api \
  --network bulksms-network \
  -p 8080:8080 \
  --restart unless-stopped \
  carlosa21/bulksms-api:latest

# Verificar
echo "9. Verificando..."
sleep 20
sudo docker ps