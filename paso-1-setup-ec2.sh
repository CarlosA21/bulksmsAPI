#!/bin/bash

# ============================================================================
# PASO A PASO - DEPLOYMENT COMPLETO EN EC2
# ============================================================================

echo "=== GUÍA PASO A PASO PARA DEPLOYMENT EN EC2 ==="
echo ""

echo "PASO 1: CONECTAR A TU INSTANCIA EC2"
echo "=================================="
echo "Desde tu máquina local, ejecuta:"
echo ""
echo "ssh -i tu-clave.pem ec2-user@tu-ip-ec2"
echo ""
echo "Ejemplo:"
echo "ssh -i mykey.pem ec2-user@54.123.45.67"
echo ""
echo "Presiona ENTER para continuar..."
read -p ""

echo "PASO 2: ACTUALIZAR EL SISTEMA"
echo "============================"
echo "Una vez conectado a EC2, ejecuta estos comandos:"
echo ""
echo "sudo yum update -y"
echo ""

echo "PASO 3: INSTALAR DOCKER"
echo "======================"
echo "sudo yum install -y docker"
echo "sudo systemctl start docker"
echo "sudo systemctl enable docker"
echo "sudo usermod -a -G docker ec2-user"
echo ""

echo "PASO 4: INSTALAR DOCKER COMPOSE"
echo "==============================="
echo 'sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose'
echo "sudo chmod +x /usr/local/bin/docker-compose"
echo ""

echo "PASO 5: INSTALAR GIT"
echo "==================="
echo "sudo yum install -y git"
echo ""

echo "PASO 6: CREAR DIRECTORIO DE LA APLICACIÓN"
echo "========================================"
echo "mkdir -p /home/ec2-user/bulksms-api"
echo "cd /home/ec2-user/bulksms-api"
echo ""

echo "PASO 7: CERRAR SESIÓN Y RECONECTAR"
echo "================================="
echo "exit"
echo ""
echo "Luego reconectar para que los cambios de Docker tomen efecto:"
echo "ssh -i tu-clave.pem ec2-user@tu-ip-ec2"
echo ""

echo "===================================================="
echo "DESPUÉS DE RECONECTAR, CONTINÚA CON EL SIGUIENTE SCRIPT"
echo "===================================================="
