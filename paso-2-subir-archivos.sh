#!/bin/bash

# ============================================================================
# PASO 2 - SUBIR ARCHIVOS Y CONFIGURAR LA APLICACIÓN
# ============================================================================

echo "=== PASO 2: SUBIR ARCHIVOS Y CONFIGURAR ==="
echo ""

echo "OPCIÓN A: SUBIR ARCHIVOS DESDE TU MÁQUINA LOCAL"
echo "=============================================="
echo "Desde tu máquina Windows (PowerShell o CMD), ejecuta:"
echo ""
echo 'scp -i "ruta\a\tu\clave.pem" -r "E:\SELLING PROJECTS\SMS Web\bulksmsAPI\*" ec2-user@TU-IP-EC2:/home/ec2-user/bulksms-api/'
echo ""
echo "Ejemplo:"
echo 'scp -i "C:\keys\mykey.pem" -r "E:\SELLING PROJECTS\SMS Web\bulksmsAPI\*" ec2-user@54.123.45.67:/home/ec2-user/bulksms-api/'
echo ""

echo "OPCIÓN B: USAR GIT (SI TIENES REPOSITORIO)"
echo "========================================="
echo "Si tienes tu código en GitHub/GitLab, desde EC2 ejecuta:"
echo ""
echo "cd /home/ec2-user/bulksms-api"
echo "git clone https://github.com/tu-usuario/bulksms-api.git ."
echo ""

echo "DESPUÉS DE SUBIR LOS ARCHIVOS, CONTINÚA EN EC2:"
echo "=============================================="
echo ""

echo "PASO 3: VERIFICAR ARCHIVOS"
echo "========================="
echo "cd /home/ec2-user/bulksms-api"
echo "ls -la"
echo ""
echo "Deberías ver archivos como: Dockerfile, docker-compose.yml, etc."
echo ""

echo "PASO 4: CONFIGURAR VARIABLES DE ENTORNO"
echo "======================================"
echo "cp .env.production .env"
echo ""
echo "Editar el archivo .env:"
echo "nano .env"
echo ""
echo "IMPORTANTE: Cambia estos valores en el archivo .env:"
echo ""
echo "DB_PASSWORD=TuPasswordMySQLSeguro123"
echo "DB_ROOT_PASSWORD=TuRootPasswordSeguro123"
echo "JWT_SECRET=TuJWTSecretMinimoTreintaYDosCaracteres12345"
echo "PAYPAL_CLIENT_ID=tu_paypal_client_id_real"
echo "PAYPAL_CLIENT_SECRET=tu_paypal_client_secret_real"
echo "MAIL_USERNAME=tu_email@gmail.com"
echo "MAIL_PASSWORD=tu_app_password_gmail"
echo ""

echo "PASO 5: DAR PERMISOS A LOS SCRIPTS"
echo "================================="
echo "chmod +x *.sh"
echo ""

echo "===================================================="
echo "CONTINÚA CON EL SIGUIENTE SCRIPT PARA HACER EL DEPLOY"
echo "===================================================="
