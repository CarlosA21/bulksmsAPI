#!/bin/bash

# ====================================================================
# ====================================================================

set -e  # Detener el script si hay errores

echo "=========================================="
echo "  INSTALACIÓN COMPLETA BULKSMS API"
echo "=========================================="

log_info() {
}

log_success() {
}

log_warning() {
}

# ====================================================================
# ====================================================================
sudo yum update -y
log_success "Sistema actualizado"

# ====================================================================
# PASO 2: INSTALAR DOCKER
# ====================================================================

if ! command -v docker &> /dev/null; then
    sudo yum install -y docker
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -a -G docker ec2-user

    if ! groups $USER | grep -q docker; then
    fi
else
fi

sudo systemctl start docker

# ====================================================================
# ====================================================================
log_success "Limpieza completada"

# ====================================================================
# ====================================================================


# ====================================================================
# ====================================================================
log_info "Creando red Docker..."

# ====================================================================
# ====================================================================
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


# ====================================================================
# ====================================================================

# ====================================================================
# ====================================================================
  --name bulksms-api \
  --network bulksms-network \
  -p 8080:8080 \
  --restart unless-stopped \
  carlosa21/bulksms-api:latest


# ====================================================================
# ====================================================================
sleep 30

echo ""
echo ""

echo ""

# ====================================================================
# ====================================================================

echo ""
echo "=========================================="
echo "=========================================="
echo ""
    echo "   http://$PUBLIC_IP:8080"
echo ""
    echo "   Host: $PUBLIC_IP:3306"
echo "   Database: bulksmsdb"
echo "   User: bulksmsuser"
echo "   Password: bulksmspass"
echo ""
echo ""



echo ""

