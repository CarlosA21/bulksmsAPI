# Guía de Pruebas API - Sistema de Verificación Manual de Mensajes

## Configuración Local

### 1. Configurar Base de Datos
Asegúrate de que tu base de datos MySQL esté corriendo y configurada según `application.properties`.

### 2. Iniciar la Aplicación
```bash
mvn spring-boot:run
```
O desde tu IDE ejecutar `BulksmsApiApplication.java`

La API estará disponible en: `http://localhost:8080`

## Endpoints para Probar la Funcionalidad

### 1. AUTENTICACIÓN (Requerida para todas las operaciones)

#### Login como Usuario Normal
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "usuario@test.com",
  "password": "password123"
}
```

#### Login como Admin
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@test.com",
  "password": "adminpassword"
}
```

**Respuesta esperada:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "admin@test.com",
  "roles": ["ROLE_ADMIN"]
}
```

⚠️ **Importante:** Copia el token para usarlo en las siguientes peticiones.

---

### 2. CREAR MENSAJE (Como Usuario Normal)

```http
POST http://localhost:8080/api/message/send
Authorization: Bearer YOUR_USER_TOKEN_HERE
Content-Type: application/json

{
  "message": "Hola, este es un mensaje de prueba para verificación manual",
  "phoneNumber": "+1234567890"
}
```

**Respuesta esperada:**
```json
{
  "messageId": 1,
  "message": "Hola, este es un mensaje de prueba para verificación manual",
  "phoneNumber": "+1234567890",
  "date": "2025-01-20T10:30:00.000+00:00",
  "status": "PENDING",
  "creditvalue": 0,
  "cancellationReason": null
}
```

✅ **Verificar:** El mensaje se guarda con estado `PENDING` y no se envía inmediatamente.

---

### 3. VER MENSAJES PENDIENTES (Solo Admin)

```http
GET http://localhost:8080/api/message/pending
Authorization: Bearer YOUR_ADMIN_TOKEN_HERE
```

**Respuesta esperada:**
```json
[
  {
    "messageId": 1,
    "message": "Hola, este es un mensaje de prueba para verificación manual",
    "phoneNumber": "+1234567890",
    "date": "2025-01-20T10:30:00.000+00:00",
    "status": "PENDING",
    "creditvalue": 0,
    "cancellationReason": null
  }
]
```

---

### 4. APROBAR MENSAJE (Solo Admin)

```http
PUT http://localhost:8080/api/message/1/approve
Authorization: Bearer YOUR_ADMIN_TOKEN_HERE
```

**Respuesta esperada:**
```json
{
  "messageId": 1,
  "message": "Hola, este es un mensaje de prueba para verificación manual",
  "phoneNumber": "+1234567890",
  "date": "2025-01-20T10:30:00.000+00:00",
  "status": "SENT",
  "creditvalue": 1,
  "cancellationReason": null
}
```

✅ **Verificar:** 
- El estado cambió a `SENT`
- Se dedujo 1 crédito
- El SMS fue enviado a través de la API de Horisen

---

### 5. CANCELAR MENSAJE (Solo Admin)

```http
PUT http://localhost:8080/api/message/2/cancel
Authorization: Bearer YOUR_ADMIN_TOKEN_HERE
Content-Type: application/json

{
  "reason": "Contenido inapropiado detectado por el sistema de moderación"
}
```

**Respuesta esperada:**
```json
{
  "messageId": 2,
  "message": "Mensaje a cancelar",
  "phoneNumber": "+0987654321",
  "date": "2025-01-20T10:35:00.000+00:00",
  "status": "FAILED",
  "creditvalue": 0,
  "cancellationReason": "Contenido inapropiado detectado por el sistema de moderación"
}
```

✅ **Verificar:**
- El estado cambió a `FAILED`
- Se guardó el motivo de cancelación
- Se envió un email al usuario con la notificación

---

### 6. VER TODOS LOS MENSAJES

```http
GET http://localhost:8080/api/message/all
Authorization: Bearer YOUR_TOKEN_HERE
```

**Respuesta esperada:**
```json
[
  {
    "messageId": 1,
    "status": "SENT",
    "cancellationReason": null
  },
  {
    "messageId": 2,
    "status": "FAILED",
    "cancellationReason": "Contenido inapropiado detectado por el sistema de moderación"
  }
]
```

---

## Casos de Prueba Específicos

### Caso 1: Usuario Normal Intenta Aprobar (Debe Fallar)
```http
PUT http://localhost:8080/api/message/1/approve
Authorization: Bearer USER_TOKEN_NOT_ADMIN
```

**Respuesta esperada:**
```
HTTP 403 Forbidden
Access Denied
```

### Caso 2: Cancelar Sin Motivo (Debe Fallar)
```http
PUT http://localhost:8080/api/message/1/cancel
Authorization: Bearer YOUR_ADMIN_TOKEN_HERE
Content-Type: application/json

{
  "reason": ""
}
```

**Respuesta esperada:**
```json
"Cancellation reason is required"
```

### Caso 3: Aprobar Mensaje Ya Procesado (Debe Fallar)
```http
PUT http://localhost:8080/api/message/1/approve
Authorization: Bearer YOUR_ADMIN_TOKEN_HERE
```

**Respuesta esperada:**
```json
"Error approving message: Message is not pending approval"
```

---

## Configuración de Correo para Pruebas

### application.properties (para testing local)
```properties
# Configuración SMTP para Gmail (ejemplo)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### Verificar Email de Cancelación
Cuando canceles un mensaje, revisa el email del usuario para confirmar que recibió:
- Asunto: "Message Cancelled - SMS Service"
- Contenido del mensaje cancelado
- Número de teléfono destinatario
- Motivo de la cancelación

---

## Herramientas Recomendadas para Pruebas

### 1. Postman
Importa esta colección:

```json
{
  "info": {
    "name": "SMS API Testing",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Login Admin",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"email\": \"admin@test.com\",\n  \"password\": \"adminpassword\"\n}"
        },
        "url": {
          "raw": "http://localhost:8080/api/auth/login",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "auth", "login"]
        }
      }
    }
  ]
}
```

### 2. cURL Commands

#### Crear mensaje
```bash
curl -X POST http://localhost:8080/api/message/send \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"message": "Test message", "phoneNumber": "+1234567890"}'
```

#### Aprobar mensaje
```bash
curl -X PUT http://localhost:8080/api/message/1/approve \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

#### Cancelar mensaje
```bash
curl -X PUT http://localhost:8080/api/message/1/cancel \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason": "Motivo de cancelación"}'
```

---

## Flujo de Prueba Completo

1. **Login como usuario normal** → Obtener token de usuario
2. **Crear mensaje** → Verificar que se guarda como PENDING
3. **Login como admin** → Obtener token de admin
4. **Ver mensajes pendientes** → Confirmar que aparece el mensaje
5. **Aprobar mensaje** → Verificar que cambia a SENT
6. **Crear otro mensaje como usuario**
7. **Cancelar mensaje como admin** → Verificar email de notificación
8. **Verificar que usuario normal no puede aprobar/cancelar**

---

## Troubleshooting

### Error de Autenticación
- Verificar que el token está en el header: `Authorization: Bearer TOKEN`
- Confirmar que el token no haya expirado
- Verificar que el usuario tenga el rol correcto

### Error de Base de Datos
- Confirmar que MySQL está corriendo
- Verificar conexión en `application.properties`
- Revisar que las tablas existan

### Error de Email
- Verificar configuración SMTP
- Comprobar credenciales de email
- Revisar logs de la aplicación

### Logs Útiles
```bash
# Ver logs de la aplicación
tail -f logs/application.log

# Ver logs específicos de mensajes
grep "Message" logs/application.log
```
