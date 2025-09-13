# Crear el script corregido directamente en tu EC2
cat > instalacion-completa-fixed.sh << 'EOF'
#!/bin/bash

# ====================================================================
# INSTALACIÓN COMPLETA BULKSMS API EN EC2 - VERSIÓN CORREGIDA
# ====================================================================

set -e  # Detener el script si hay errores

echo "=========================================="
echo "  INSTALACIÓN COMPLETA BULKSMS API"
echo "=========================================="

# Función para mostrar mensajes
log_info() {
    echo "[INFO] $1"
}

log_success() {
    echo "[SUCCESS] $1"
}

log_warning() {
    echo "[WARNING] $1"
}

# ====================================================================
# PASO 1: ACTUALIZAR SISTEMA E INSTALAR HERRAMIENTAS
# ====================================================================
log_info "Actualizando sistema e instalando herramientas básicas..."
sudo yum update -y
sudo yum install -y curl wget
log_success "Sistema actualizado"

# ====================================================================
# PASO 2: INSTALAR DOCKER
# ====================================================================
log_info "Verificando Docker..."

if ! command -v docker &> /dev/null; then
    log_info "Instalando Docker..."
    sudo yum install -y docker
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -a -G docker ec2-user
    log_success "Docker instalado"

    # Verificar permisos
    if ! groups $USER | grep -q docker; then
        log_warning "Aplicando permisos de Docker..."
        sudo systemctl restart docker
        # Usar sudo para el resto del script si es necesario
        DOCKER_CMD="sudo docker"
    else
        DOCKER_CMD="docker"
    fi
else
    log_success "Docker ya instalado"
    DOCKER_CMD="docker"
fi

# Asegurar que Docker esté ejecutándose
sudo systemctl start docker

# ====================================================================
# PASO 3: LIMPIAR INSTALACIÓN ANTERIOR
# ====================================================================
log_info "Limpiando instalación anterior..."
$DOCKER_CMD stop bulksms-api bulksms-mysql 2>/dev/null || true
$DOCKER_CMD rm bulksms-api bulksms-mysql 2>/dev/null || true
$DOCKER_CMD network rm bulksms-network 2>/dev/null || true
log_success "Limpieza completada"

# ====================================================================
# PASO 4: DESCARGAR IMÁGENES
# ====================================================================
log_info "Descargando imagen MySQL..."
$DOCKER_CMD pull mysql:8.0
log_success "MySQL descargado"

log_info "Descargando BulkSMS API..."
$DOCKER_CMD pull carlosa21/bulksms-api:latest
log_success "BulkSMS API descargado"

# ====================================================================
# PASO 5: CREAR RED
# ====================================================================
log_info "Creando red Docker..."
$DOCKER_CMD network create bulksms-network
log_success "Red creada"

# ====================================================================
# PASO 6: INICIAR MYSQL
# ====================================================================
log_info "Iniciando MySQL..."
$DOCKER_CMD run -d \
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

log_success "MySQL iniciado"

# ====================================================================
# PASO 7: ESPERAR MYSQL
# ====================================================================
log_info "Esperando MySQL (90 segundos)..."
sleep 90

# ====================================================================
# PASO 8: INICIAR APLICACIÓN
# ====================================================================
log_info "Iniciando BulkSMS API..."
$DOCKER_CMD run -d \
  --name bulksms-api \
  --network bulksms-network \
  -p 8080:8080 \
  --restart unless-stopped \
  carlosa21/bulksms-api:latest

log_success "Aplicación iniciada"

# ====================================================================
# PASO 9: VERIFICAR ESTADO
# ====================================================================
log_info "Esperando aplicación (30 segundos)..."
sleep 30

echo ""
echo "=== ESTADO DE CONTENEDORES ==="
$DOCKER_CMD ps

echo ""
echo "=== LOGS DE MYSQL ==="
$DOCKER_CMD logs --tail=5 bulksms-mysql

echo ""
echo "=== LOGS DE APLICACIÓN ==="
$DOCKER_CMD logs --tail=10 bulksms-api

# ====================================================================
# PASO 10: INFORMACIÓN FINAL
# ====================================================================
# Obtener IP pública de forma más robusta
PUBLIC_IP=""
if command -v curl &> /dev/null; then
    PUBLIC_IP=$(curl -s --connect-timeout 5 http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo "")
fi

if [ -z "$PUBLIC_IP" ]; then
    if command -v wget &> /dev/null; then
        PUBLIC_IP=$(wget -qO- --timeout=5 http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo "")
    fi
fi

if [ -z "$PUBLIC_IP" ]; then
    PUBLIC_IP="TU_IP_PUBLICA_EC2"
fi

echo ""
echo "=========================================="
echo "INSTALACIÓN COMPLETADA"
echo "=========================================="
echo ""
echo "Aplicación disponible en:"
echo "  http://$PUBLIC_IP:8080"
echo ""
echo "Base de datos MySQL:"
echo "  Host: $PUBLIC_IP:3306"
echo "  Database: bulksmsdb"
echo "  User: bulksmsuser"
echo "  Password: bulksmspass"
echo ""
echo "Comandos útiles:"
echo "  Ver logs: $DOCKER_CMD logs -f bulksms-api"
echo "  Ver estado: $DOCKER_CMD ps"
echo "  Reiniciar: $DOCKER_CMD restart bulksms-api"
echo ""

# Crear archivo de información
mkdir -p /home/ec2-user/bulksms-info
cat > /home/ec2-user/bulksms-info/info.txt << EOFINFO
Instalación completada: $(date)
IP: $PUBLIC_IP
App: http://$PUBLIC_IP:8080
DB: $PUBLIC_IP:3306
User: bulksmsuser / Pass: bulksmspass

Comandos:
- Ver logs: $DOCKER_CMD logs -f bulksms-api
- Estado: $DOCKER_CMD ps
- Reiniciar: $DOCKER_CMD restart bulksms-api
EOFINFO

log_success "Información guardada en /home/ec2-user/bulksms-info/info.txt"
echo ""
echo "INSTALACIÓN FINALIZADA EXITOSAMENTE!"

EOF
