#!/bin/bash

# ============================================================================
# PASO 3 - DEPLOYMENT FINAL DE LA APLICACIÓN
# ============================================================================

echo "=== PASO 3: DEPLOYMENT FINAL ==="
echo ""

echo "PASO 1: VERIFICAR QUE ESTÁS EN EL DIRECTORIO CORRECTO"
echo "=================================================="
echo "cd /home/ec2-user/bulksms-api"
echo "pwd"
echo ""

echo "PASO 2: VERIFICAR QUE DOCKER ESTÁ FUNCIONANDO"
echo "============================================"
echo "docker --version"
echo "docker-compose --version"
echo ""
echo "Si da error, ejecuta:"
echo "sudo systemctl start docker"
echo ""

echo "PASO 3: CONSTRUIR Y EJECUTAR LA APLICACIÓN"
echo "========================================="
echo "# Usar el docker-compose de producción"
echo "sudo docker-compose -f docker-compose.prod.yml up -d --build"
echo ""

echo "PASO 4: VERIFICAR QUE LOS CONTENEDORES ESTÁN CORRIENDO"
echo "====================================================="
echo "sudo docker-compose -f docker-compose.prod.yml ps"
echo ""

echo "PASO 5: VER LOS LOGS PARA VERIFICAR QUE TODO ESTÁ BIEN"
echo "====================================================="
echo "# Ver logs de la aplicación"
echo "sudo docker-compose -f docker-compose.prod.yml logs bulksms-api"
echo ""
echo "# Ver logs de la base de datos"
echo "sudo docker-compose -f docker-compose.prod.yml logs bulksms-mysql"
echo ""

echo "PASO 6: PROBAR LA APLICACIÓN"
echo "==========================="
echo "# Obtener la IP pública de tu EC2"
echo 'curl -s http://169.254.169.254/latest/meta-data/public-ipv4'
echo ""
echo "# Probar el health check"
echo "curl http://localhost:8080/actuator/health"
echo ""

echo "PASO 7: CONFIGURAR FIREWALL (SI ES NECESARIO)"
echo "============================================="
echo "# Abrir puertos en el firewall local (si está activo)"
echo "sudo firewall-cmd --permanent --add-port=8080/tcp"
echo "sudo firewall-cmd --permanent --add-port=3306/tcp"
echo "sudo firewall-cmd --reload"
echo ""
echo "# O deshabilitar firewall temporalmente para pruebas"
echo "sudo systemctl stop firewalld"
echo ""

echo "===================================================="
echo "¡DEPLOYMENT COMPLETADO!"
echo ""
echo "Tu aplicación debería estar disponible en:"
echo "http://TU-IP-EC2:8080"
echo ""
echo "Para obtener tu IP EC2:"
echo 'curl -s http://169.254.169.254/latest/meta-data/public-ipv4'
echo "===================================================="
