# 3. Crear script simple y funcional
#!/bin/bash

echo "=== INICIANDO INSTALACIÓN BULKSMS API ==="

# Actualizar sistema
echo "1. Actualizando sistema..."
sudo yum update -y
sudo yum install -y docker

# Configurar Docker
echo "2. Configurando Docker..."
sudo systemctl start docker
sudo systemctl enable docker

# Limpiar contenedores anteriores
echo "3. Limpiando instalación anterior..."
sudo docker stop bulksms-api bulksms-mysql 2>/dev/null || true
sudo docker rm bulksms-api bulksms-mysql 2>/dev/null || true
sudo docker network rm bulksms-network 2>/dev/null || true

# Descargar imágenes
echo "4. Descargando imágenes..."
sudo docker pull mysql:8.0
sudo docker pull carlosa21/bulksms-api:latest

# Crear red
echo "5. Creando red..."
sudo docker network create bulksms-network

# Iniciar MySQL
echo "6. Iniciando MySQL..."
sudo docker run -d \
  --name bulksms-mysql \
  --network bulksms-network \
  -p 3306:3306 \
  -e MYSQL_DATABASE=bulksmsdb \
  -e MYSQL_USER=bulksmsuser \
  -e MYSQL_PASSWORD=bulksmspass \
  -e MYSQL_ROOT_PASSWORD=rootpass \
  -v mysql_data:/var/lib/mysql \
  --restart unless-stopped \
  mysql:8.0

# Esperar MySQL
echo "7. Esperando MySQL (60 segundos)..."
sleep 60

# Iniciar aplicación
echo "8. Iniciando aplicación..."
sudo docker run -d \
  --name bulksms-api \
  --network bulksms-network \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://bulksms-mysql:3306/bulksmsdb \
  -e SPRING_DATASOURCE_USERNAME=bulksmsuser \
  -e SPRING_DATASOURCE_PASSWORD=bulksmspass \
  -e SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver \
  --restart unless-stopped \
  carlosa21/bulksms-api:latest

# Verificar
echo "9. Verificando..."
sleep 20
sudo docker ps

echo ""
echo "=== INSTALACIÓN COMPLETADA ==="
echo "Aplicación disponible en puerto 8080"
echo "Para ver logs: sudo docker logs -f bulksms-api"


