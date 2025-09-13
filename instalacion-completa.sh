#!/bin/bash

# ====================================================================
# INSTALACIÓN COMPLETA BULKSMS API EN EC2
# Este script instala Docker, descarga las imágenes y ejecuta la aplicación
# ====================================================================

set -e  # Detener el script si hay errores

echo "=========================================="
echo "  INSTALACIÓN COMPLETA BULKSMS API"
echo "=========================================="
echo ""

# Función para mostrar mensajes con formato
log_info() {
    echo "🔵 [INFO] $1"
}

log_success() {
    echo "✅ [SUCCESS] $1"
}

log_warning() {
    echo "⚠️  [WARNING] $1"
}

log_error() {
    echo "❌ [ERROR] $1"
}

# ====================================================================
# PASO 1: ACTUALIZAR EL SISTEMA
# ====================================================================
log_info "Actualizando paquetes del sistema..."
sudo yum update -y
log_success "Sistema actualizado"

# ====================================================================
# PASO 2: INSTALAR DOCKER
# ====================================================================
log_info "Verificando si Docker está instalado..."

if ! command -v docker &> /dev/null; then
    log_info "Docker no encontrado. Instalando Docker..."

    # Instalar Docker
    sudo yum install -y docker

    # Iniciar y habilitar Docker
    sudo systemctl start docker
    sudo systemctl enable docker

    # Agregar usuario ec2-user al grupo docker
    sudo usermod -a -G docker ec2-user

    log_success "Docker instalado correctamente"
    log_warning "IMPORTANTE: Cierra sesión y vuelve a conectarte para aplicar los permisos de Docker"
    log_warning "Después de reconectarte, vuelve a ejecutar este script"

    # Verificar si el usuario ya está en el grupo docker
    if ! groups $USER | grep -q docker; then
        log_warning "Necesitas cerrar sesión y volver a conectarte. Luego ejecuta:"
        echo "wget https://raw.githubusercontent.com/CarlosA21/bulksmsAPI/main/instalacion-completa.sh"
        echo "chmod +x instalacion-completa.sh"
        echo "./instalacion-completa.sh"
        exit 1
    fi
else
    log_success "Docker ya está instalado"
fi

# Verificar que Docker esté ejecutándose
log_info "Verificando que Docker esté ejecutándose..."
sudo systemctl start docker
log_success "Docker está ejecutándose"

# ====================================================================
# PASO 3: LIMPIAR INSTALACIÓN ANTERIOR (SI EXISTE)
# ====================================================================
log_info "Limpiando instalación anterior si existe..."

# Detener contenedores existentes
docker stop bulksms-api bulksms-mysql 2>/dev/null || true
log_info "Contenedores detenidos"

# Eliminar contenedores existentes
docker rm bulksms-api bulksms-mysql 2>/dev/null || true
log_info "Contenedores eliminados"

# Eliminar red existente
docker network rm bulksms-network 2>/dev/null || true
log_info "Red Docker eliminada"

# Eliminar volúmenes (opcional - descomenta si quieres limpiar datos)
# docker volume rm mysql_data 2>/dev/null || true
# log_info "Volúmenes eliminados"

log_success "Limpieza completada"

# ====================================================================
# PASO 4: DESCARGAR IMÁGENES DOCKER
# ====================================================================
log_info "Descargando imagen MySQL:8.0..."
docker pull mysql:8.0
log_success "Imagen MySQL descargada"

log_info "Descargando imagen BulkSMS API..."
docker pull carlosa21/bulksms-api:latest
log_success "Imagen BulkSMS API descargada"

# ====================================================================
# PASO 5: CREAR RED DOCKER
# ====================================================================
log_info "Creando red Docker..."
docker network create bulksms-network
log_success "Red Docker creada: bulksms-network"

# ====================================================================
# PASO 6: CREAR DIRECTORIO DE TRABAJO
# ====================================================================
APP_DIR="/home/ec2-user/bulksms-api"
log_info "Creando directorio de trabajo: $APP_DIR"
mkdir -p $APP_DIR
cd $APP_DIR
log_success "Directorio creado y configurado"

# ====================================================================
# PASO 7: INICIAR CONTENEDOR MYSQL
# ====================================================================
log_info "Iniciando contenedor MySQL..."

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

log_success "Contenedor MySQL iniciado"

# ====================================================================
# PASO 8: ESPERAR INICIALIZACIÓN DE MYSQL
# ====================================================================
log_info "Esperando que MySQL se inicialice completamente..."
log_info "Esto puede tomar entre 60-120 segundos..."

# Esperar 2 minutos para que MySQL esté completamente listo
for i in {1..120}; do
    if docker logs bulksms-mysql 2>&1 | grep -q "ready for connections"; then
        log_success "MySQL está listo para conexiones"
        break
    fi
    echo -n "."
    sleep 1
    if [ $i -eq 120 ]; then
        log_warning "MySQL está tardando más de lo esperado, pero continuaremos..."
    fi
done

# Esperar un poco más para estar seguros
sleep 30

# ====================================================================
# PASO 9: INICIAR APLICACIÓN BULKSMS API
# ====================================================================
log_info "Iniciando aplicación BulkSMS API..."

docker run -d \
  --name bulksms-api \
  --network bulksms-network \
  -p 8080:8080 \
  --restart unless-stopped \
  carlosa21/bulksms-api:latest

log_success "Aplicación BulkSMS API iniciada"

# ====================================================================
# PASO 10: VERIFICAR INSTALACIÓN
# ====================================================================
log_info "Esperando que la aplicación se inicie (30 segundos)..."
sleep 30

log_info "Verificando estado de los contenedores..."
echo ""
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo ""

# Verificar logs de MySQL
log_info "Últimas líneas del log de MySQL:"
docker logs --tail=5 bulksms-mysql

echo ""

# Verificar logs de la aplicación
log_info "Últimas líneas del log de la aplicación:"
docker logs --tail=10 bulksms-api

echo ""

# ====================================================================
# PASO 11: OBTENER IP PÚBLICA Y MOSTRAR INFORMACIÓN FINAL
# ====================================================================
log_info "Obteniendo IP pública de EC2..."
PUBLIC_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null || echo "NO_DISPONIBLE")

echo ""
echo "=========================================="
echo "✅ INSTALACIÓN COMPLETADA EXITOSAMENTE"
echo "=========================================="
echo ""
echo "🌐 Tu aplicación está disponible en:"
if [ "$PUBLIC_IP" != "NO_DISPONIBLE" ]; then
    echo "   http://$PUBLIC_IP:8080"
else
    echo "   http://TU_IP_PUBLICA_EC2:8080"
fi
echo ""
echo "🗄️  Base de datos MySQL disponible en:"
if [ "$PUBLIC_IP" != "NO_DISPONIBLE" ]; then
    echo "   Host: $PUBLIC_IP:3306"
else
    echo "   Host: TU_IP_PUBLICA_EC2:3306"
fi
echo "   Database: bulksmsdb"
echo "   User: bulksmsuser"
echo "   Password: bulksmspass"
echo ""
echo "📋 COMANDOS ÚTILES:"
echo "   Ver logs de la app:    docker logs -f bulksms-api"
echo "   Ver logs de MySQL:     docker logs -f bulksms-mysql"
echo "   Ver estado:            docker ps"
echo "   Reiniciar app:         docker restart bulksms-api"
echo "   Reiniciar MySQL:       docker restart bulksms-mysql"
echo "   Detener todo:          docker stop bulksms-api bulksms-mysql"
echo "   Ver esta info:         cat $APP_DIR/info.txt"
echo ""

# Guardar información en archivo
cat > $APP_DIR/info.txt << EOF
=== INFORMACIÓN DE LA INSTALACIÓN ===
Fecha de instalación: $(date)
IP Pública: $PUBLIC_IP

URLs:
- Aplicación: http://$PUBLIC_IP:8080
- Base de datos: $PUBLIC_IP:3306

Credenciales MySQL:
- Database: bulksmsdb
- User: bulksmsuser
- Password: bulksmspass
- Root Password: rootpass

Comandos útiles:
- Ver logs de la app: docker logs -f bulksms-api
- Ver logs de MySQL: docker logs -f bulksms-mysql
- Ver estado: docker ps
- Reiniciar app: docker restart bulksms-api
- Detener todo: docker stop bulksms-api bulksms-mysql
EOF

log_success "Información guardada en: $APP_DIR/info.txt"

# ====================================================================
# PASO 12: VERIFICACIÓN FINAL DE SALUD
# ====================================================================
log_info "Realizando verificación final..."

# Verificar que los contenedores estén ejecutándose
if docker ps | grep -q "bulksms-mysql"; then
    log_success "✅ MySQL está ejecutándose"
else
    log_error "❌ MySQL no está ejecutándose"
fi

if docker ps | grep -q "bulksms-api"; then
    log_success "✅ BulkSMS API está ejecutándose"
else
    log_error "❌ BulkSMS API no está ejecutándose"
fi

# Verificar conectividad
log_info "Verificando conectividad..."
sleep 10

if curl -s http://localhost:8080 > /dev/null 2>&1; then
    log_success "✅ La aplicación responde correctamente"
else
    log_warning "⚠️  La aplicación aún se está iniciando o hay un problema"
    log_info "Espera unos minutos más y verifica con: docker logs -f bulksms-api"
fi

echo ""
echo "🎉 ¡INSTALACIÓN FINALIZADA!"
echo "🔗 Accede a tu aplicación en el navegador usando la URL mostrada arriba"
echo ""

# Script finalizado exitosamente
exit 0
