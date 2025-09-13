# BulkSMS API - EC2 Deployment Guide

Este README te guía paso a paso para hacer el deployment de tu aplicación BulkSMS API en Amazon EC2.

## 📋 Prerequisitos

1. **Instancia EC2** con Amazon Linux 2 o Ubuntu
2. **Security Group** configurado con los siguientes puertos abiertos:
   - Puerto 22 (SSH)
   - Puerto 80 (HTTP)
   - Puerto 443 (HTTPS)
   - Puerto 8080 (API)
   - Puerto 3306 (MySQL) - opcional, solo si necesitas acceso externo a la BD

## 🚀 Pasos para el Deployment

### Paso 1: Configurar la Instancia EC2

1. **Conectar a tu instancia EC2:**
   ```bash
   ssh -i your-key.pem ec2-user@your-ec2-ip
   ```

2. **Ejecutar el setup inicial:**
   ```bash
   chmod +x ec2-setup.sh
   ./ec2-setup.sh
   ```

### Paso 2: Subir los Archivos

**Opción A: Usando SCP (desde tu máquina local):**
```bash
scp -i your-key.pem -r ./* ec2-user@your-ec2-ip:/home/ec2-user/bulksms-api/
```

**Opción B: Usando Git:**
```bash
cd /home/ec2-user/bulksms-api
git clone https://github.com/tu-usuario/bulksms-api.git .
```

### Paso 3: Configurar Variables de Entorno

1. **Copiar y editar el archivo de configuración:**
   ```bash
   cp .env.production .env
   nano .env
   ```

2. **Actualizar las siguientes variables importantes:**
   ```env
   # Contraseñas seguras
   DB_PASSWORD=tu_contraseña_segura_mysql
   DB_ROOT_PASSWORD=tu_contraseña_root_segura
   JWT_SECRET=tu_jwt_secret_minimo_32_caracteres
   
   # Configuración de PayPal (para producción)
   PAYPAL_CLIENT_ID=tu_paypal_client_id_real
   PAYPAL_CLIENT_SECRET=tu_paypal_client_secret_real
   PAYPAL_MODE=live
   
   # Configuración de email
   MAIL_USERNAME=tu_email@gmail.com
   MAIL_PASSWORD=tu_app_password
   
   # URL de tu dominio
   GOOGLE_REDIRECT_URI=https://tu-dominio.com/maindash
   ```

### Paso 4: Deploy de la Aplicación

1. **Hacer el deploy:**
   ```bash
   chmod +x deploy-to-ec2.sh
   ./deploy-to-ec2.sh
   ```

2. **Verificar que los servicios están corriendo:**
   ```bash
   sudo docker-compose -f docker-compose.prod.yml ps
   ```

### Paso 5: Configurar Dominio y SSL (Opcional pero Recomendado)

1. **Apuntar tu dominio a la IP de EC2**
2. **Obtener certificados SSL con Let's Encrypt:**
   ```bash
   sudo yum install -y certbot
   sudo certbot certonly --standalone -d tu-dominio.com
   ```

3. **Actualizar nginx.conf con tu dominio y certificados**

## 🔧 Comandos Útiles de Mantenimiento

### Ver logs de la aplicación:
```bash
sudo docker-compose -f docker-compose.prod.yml logs bulksms-api
```

### Ver logs de la base de datos:
```bash
sudo docker-compose -f docker-compose.prod.yml logs bulksms-mysql
```

### Actualizar la aplicación:
```bash
./update-app.sh
```

### Backup de la base de datos:
```bash
sudo docker exec bulksms-mysql-prod mysqldump -u root -p bulksmsdb > backup-$(date +%Y%m%d).sql
```

### Parar todos los servicios:
```bash
sudo docker-compose -f docker-compose.prod.yml down
```

### Iniciar todos los servicios:
```bash
sudo docker-compose -f docker-compose.prod.yml up -d
```

## 🌐 URLs de Acceso

Después del deployment, tu aplicación estará disponible en:

- **API**: `http://tu-ec2-ip:8080` o `https://tu-dominio.com`
- **Health Check**: `http://tu-ec2-ip:8080/actuator/health`
- **MySQL**: `tu-ec2-ip:3306` (si está expuesto)

## 🛠️ Troubleshooting

### Problema: Los contenedores no arrancan
```bash
# Verificar logs
sudo docker-compose -f docker-compose.prod.yml logs

# Verificar que Docker está corriendo
sudo systemctl status docker
```

### Problema: No se puede conectar a la base de datos
```bash
# Verificar que MySQL está corriendo
sudo docker exec -it bulksms-mysql-prod mysql -u root -p

# Verificar las variables de entorno
sudo docker-compose -f docker-compose.prod.yml config
```

### Problema: La aplicación no responde
```bash
# Verificar el estado de los contenedores
sudo docker ps

# Reiniciar la aplicación
sudo docker-compose -f docker-compose.prod.yml restart bulksms-api
```

## 📊 Monitoreo

Para monitorear tu aplicación en producción:

1. **Health Check**: `curl http://localhost:8080/actuator/health`
2. **Logs en tiempo real**: `sudo docker-compose -f docker-compose.prod.yml logs -f`
3. **Uso de recursos**: `sudo docker stats`

## 🔒 Seguridad

- Cambia todas las contraseñas por defecto
- Usa HTTPS en producción
- Mantén actualizados Docker y el sistema operativo
- Configura un firewall apropiado
- Realiza backups regulares de la base de datos

## 💡 Notas Importantes

- El primer arranque puede tomar varios minutos mientras se crean las tablas de la base de datos
- Asegúrate de que tu instancia EC2 tenga al menos 2GB de RAM
- Para producción, considera usar RDS para la base de datos
- Configura alertas de CloudWatch para monitoreo
