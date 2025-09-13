# BulkSMS API - Docker Hub

✅ **Imagen disponible en Docker Hub**: `carlosa21/bulksms-api:latest`

## 🚀 Uso Rápido

### Ejecutar solo la aplicación (requiere MySQL externo)
```bash
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://tu-mysql-host:3306/bulksmsdb" \
  -e SPRING_DATASOURCE_USERNAME="tu-usuario" \
  -e SPRING_DATASOURCE_PASSWORD="tu-password" \
  -e JWT_SECRET="tu-jwt-secret" \
  carlosa21/bulksms-api:latest
```

### Ejecutar con Docker Compose (aplicación + MySQL)
```bash
# Usar la configuración de Docker Hub
docker-compose -f docker-compose.dockerhub.yml up -d
```

## 📋 Variables de Entorno Requeridas

### Base de Datos
- `SPRING_DATASOURCE_URL`: URL de conexión a MySQL
- `SPRING_DATASOURCE_USERNAME`: Usuario de la base de datos
- `SPRING_DATASOURCE_PASSWORD`: Contraseña de la base de datos

### Seguridad
- `JWT_SECRET`: Clave secreta para JWT tokens

### PayPal (Opcional)
- `PAYPAL_CLIENT_ID`: ID del cliente PayPal
- `PAYPAL_CLIENT_SECRET`: Secret del cliente PayPal
- `PAYPAL_MODE`: Modo PayPal (sandbox/live)

### Stripe (Opcional)
- `STRIPE_API_KEY`: Clave API de Stripe

### Email (Opcional)
- `MAIL_USERNAME`: Usuario del servidor de email
- `MAIL_PASSWORD`: Contraseña del servidor de email

## 🌐 Puertos
- **8080**: Puerto de la aplicación Spring Boot

## 🔗 Enlaces Útiles
- **Docker Hub**: https://hub.docker.com/r/carlosa21/bulksms-api
- **Health Check**: http://localhost:8080/actuator/health
- **API Documentation**: http://localhost:8080/swagger-ui.html

## 📝 Comandos Útiles

```bash
# Pull de la imagen
docker pull carlosa21/bulksms-api:latest

# Ver logs
docker logs bulksms-api-prod

# Acceder al contenedor
docker exec -it bulksms-api-prod bash

# Parar y eliminar contenedores
docker-compose -f docker-compose.dockerhub.yml down
```

## 🎯 Características
- ✅ Multi-stage build optimizado
- ✅ Usuario no-root para seguridad  
- ✅ Health checks incluidos
- ✅ JVM optimizado para contenedores
- ✅ Tamaño: ~682MB

---
*Última actualización de la imagen: $(date)*
