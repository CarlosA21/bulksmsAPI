# Guía de Implementación - Sistema de Validación de Cuenta con Imagen

## Descripción
Se ha implementado un sistema completo para que los usuarios suban una imagen de validación durante el registro, que será revisada por un administrador para validar la cuenta.

## Cambios Realizados

### 1. Modelo User (User.java)
Se agregaron los siguientes campos:
- `validationImagePath`: Ruta donde se almacena la imagen
- `validationImageName`: Nombre del archivo de imagen
- `accountValidated`: Estado de validación de la cuenta (Boolean)

### 2. Servicio de Almacenamiento (FileStorageService.java)
Nuevo servicio creado para manejar la carga y gestión de archivos:
- **storeFile()**: Guarda la imagen en el servidor
- **deleteFile()**: Elimina imágenes antiguas
- **loadFileAsBytes()**: Recupera la imagen para mostrarla
- **Validaciones**: Solo permite imágenes (jpg, png, etc.)
- **Seguridad**: Genera nombres únicos para evitar conflictos

### 3. Servicio de Usuarios (UserService.java)
Nuevos métodos agregados:
- **uploadValidationImage()**: Sube imagen de validación
- **getValidationImage()**: Obtiene la imagen del usuario
- **validateUserAccount()**: Valida/invalida cuenta (solo admin)
- **isAccountValidated()**: Verifica estado de validación

### 4. Controlador de Autenticación (AuthController.java)
Nuevos endpoints REST:

#### **POST** `/api/auth/upload-validation-image/{userId}`
Sube imagen de validación
- **Parámetro**: `file` (MultipartFile)
- **Respuesta**:
```json
{
  "message": "Validation image uploaded successfully",
  "imageName": "user_1_uuid.jpg",
  "accountValidated": false
}
```

#### **GET** `/api/auth/validation-image/{userId}`
Obtiene la imagen de validación
- **Respuesta**: Imagen en formato byte array (JPEG/PNG)

#### **POST** `/api/auth/validate-account/{userId}` (Solo Admin)
Valida o invalida una cuenta de usuario
- **Parámetro**: `validated` (boolean)
- **Respuesta**:
```json
{
  "message": "User account validation status updated",
  "userId": 1,
  "accountValidated": true
}
```

#### **GET** `/api/auth/is-validated/{userId}`
Verifica si una cuenta está validada
- **Respuesta**:
```json
{
  "userId": 1,
  "accountValidated": true
}
```

### 5. Configuración (application.properties)
Se agregó:
```properties
# File Upload Configuration
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
file.upload-dir=uploads/validation-images
```

### 6. Script SQL (add_validation_columns.sql)
Script para agregar las columnas a la base de datos existente.

## Flujo de Uso

### Para el Usuario:
1. **Registro**: El usuario se registra normalmente
2. **Subir Imagen**: Después del registro, sube una imagen de validación usando:
   ```
   POST /api/auth/upload-validation-image/{userId}
   ```
3. **Esperar Validación**: La cuenta queda pendiente de validación
4. **Verificar Estado**: Puede consultar el estado con:
   ```
   GET /api/auth/is-validated/{userId}
   ```

### Para el Administrador:
1. **Ver Lista de Usuarios**: Usar el endpoint existente para listar usuarios
2. **Ver Imagen**: Obtener la imagen de validación:
   ```
   GET /api/auth/validation-image/{userId}
   ```
3. **Validar Cuenta**: Aprobar o rechazar:
   ```
   POST /api/auth/validate-account/{userId}?validated=true
   ```

## Ejemplos de Uso con cURL

### Subir Imagen de Validación
```bash
curl -X POST "http://localhost:8443/api/auth/upload-validation-image/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/image.jpg"
```

### Obtener Imagen
```bash
curl -X GET "http://localhost:8443/api/auth/validation-image/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  --output validation_image.jpg
```

### Validar Cuenta (Admin)
```bash
curl -X POST "http://localhost:8443/api/auth/validate-account/1?validated=true" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

### Verificar Estado de Validación
```bash
curl -X GET "http://localhost:8443/api/auth/is-validated/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Ejemplos con JavaScript/Fetch

### Subir Imagen desde Frontend
```javascript
async function uploadValidationImage(userId, imageFile) {
  const formData = new FormData();
  formData.append('file', imageFile);
  
  const response = await fetch(`/api/auth/upload-validation-image/${userId}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    },
    body: formData
  });
  
  return await response.json();
}
```

### Mostrar Imagen en Frontend
```javascript
async function showValidationImage(userId) {
  const response = await fetch(`/api/auth/validation-image/${userId}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  const blob = await response.blob();
  const imageUrl = URL.createObjectURL(blob);
  
  document.getElementById('validationImage').src = imageUrl;
}
```

### Validar Cuenta (Admin)
```javascript
async function validateAccount(userId, isValid) {
  const response = await fetch(`/api/auth/validate-account/${userId}?validated=${isValid}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${adminToken}`
    }
  });
  
  return await response.json();
}
```

## Seguridad

1. **Validación de Tipos**: Solo se permiten archivos de imagen
2. **Tamaño Máximo**: 10MB por archivo
3. **Nombres Únicos**: Se generan con UUID para evitar colisiones
4. **Permisos**: La validación de cuentas solo está disponible para administradores
5. **Autenticación**: Todos los endpoints requieren JWT token

## Integración en el Frontend

### Formulario de Registro con Imagen
```html
<form id="registrationForm">
  <input type="email" name="email" required>
  <input type="password" name="password" required>
  <input type="file" name="validationImage" accept="image/*" required>
  <button type="submit">Registrarse</button>
</form>
```

### Panel de Administración
- Listar usuarios pendientes de validación
- Mostrar imagen de validación
- Botones para aprobar/rechazar

## Notas Importantes

1. **Directorio de Almacenamiento**: Se creará automáticamente en `uploads/validation-images/`
2. **Base de Datos**: Ejecutar el script SQL si la base de datos ya existe
3. **Docker**: Asegurarse de que el volumen esté montado para persistir las imágenes
4. **Producción**: Considerar usar almacenamiento en la nube (S3, Azure Blob, etc.)

## Próximos Pasos Recomendados

1. Crear interfaz de usuario para el formulario de registro con imagen
2. Crear panel de administración para revisar y validar cuentas
3. Implementar notificaciones por email cuando se valide una cuenta
4. Agregar restricciones de acceso basadas en el estado de validación
5. Considerar implementar almacenamiento en la nube para producción

