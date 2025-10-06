# Guía: Probar Registro con Imagen en Postman

## 📋 Pasos para Probar el Endpoint de Registro con Imagen

### 1. Configurar la Petición en Postman

#### **Paso 1: Crear Nueva Request**
1. Abre Postman
2. Click en "New" → "HTTP Request"
3. Nombre sugerido: "Register User with Validation Image"

#### **Paso 2: Configurar el Método y URL**
- **Método:** `POST`
- **URL:** `https://localhost:8443/api/auth/register`
  - O si estás en desarrollo local: `http://localhost:8080/api/auth/register`

### 2. Configurar el Body (Multipart Form Data)

#### **Paso 3: Seleccionar tipo de Body**
1. Ve a la pestaña **"Body"**
2. Selecciona **"form-data"** (NO selecciones "raw" ni "JSON")

#### **Paso 4: Agregar el campo 'user' (JSON)**
1. En la primera fila:
   - **KEY:** `user`
   - **Tipo:** Cambia de "Text" a **"File"** primero, luego vuelve a **"Text"**
   - Haz click en el dropdown al lado del KEY y selecciona **"JSON (application/json)"**
   - **VALUE:** Pega este JSON:

```json
{
  "email": "testuser@example.com",
  "password": "SecurePassword123",
  "driverLicense": "DL123456789",
  "billingAddress": {
    "street": "123 Main Street",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  }
}
```

#### **Paso 5: Agregar el campo 'validationImage' (Archivo)**
1. En la segunda fila:
   - **KEY:** `validationImage`
   - **Tipo:** Cambia de "Text" a **"File"**
   - **VALUE:** Click en "Select Files" y selecciona una imagen (JPG, PNG, etc.)

### 3. Configuración Visual en Postman

```
┌─────────────────────────────────────────────────────────┐
│ POST  https://localhost:8443/api/auth/register    [Send]│
├─────────────────────────────────────────────────────────┤
│ Params  Authorization  Headers  Body  Pre-request Script│
│                                   ▼                      │
├─────────────────────────────────────────────────────────┤
│ ○ none  ○ form-data  ○ x-www-form-urlencoded  ○ raw    │
│         ●                                                │
├─────────────────────────────────────────────────────────┤
│ KEY               │ VALUE                    │ TYPE     │
├───────────────────┼──────────────────────────┼──────────┤
│ ☑ user           │ {                        │ JSON     │
│   (application/   │   "email": "test@..."    │          │
│    json)          │   ...                    │          │
│                   │ }                        │          │
├───────────────────┼──────────────────────────┼──────────┤
│ ☑ validationImage│ [Select Files] image.jpg │ File     │
├───────────────────┴──────────────────────────┴──────────┤
└─────────────────────────────────────────────────────────┘
```

### 4. Pasos Detallados con Imágenes Textuales

#### **A. Configurar el campo 'user' como JSON:**

1. En la columna **KEY**, escribe: `user`
2. En la columna **VALUE**, NO hagas nada todavía
3. Haz hover sobre el KEY "user" y verás un dropdown que dice "Text"
4. Click en el dropdown y cambia a **"JSON (application/json)"**
5. AHORA sí, pega el JSON en la columna **VALUE**

**IMPORTANTE:** Si no cambias el tipo a JSON, el servidor no podrá parsear los datos correctamente.

#### **B. Configurar el campo 'validationImage' como File:**

1. Click en "Add row" (+ icon)
2. En la columna **KEY**, escribe: `validationImage`
3. Haz click en el dropdown (donde dice "Text") 
4. Selecciona **"File"**
5. Aparecerá un botón **"Select Files"** en la columna VALUE
6. Click en "Select Files" y selecciona una imagen de tu computadora

### 5. Headers Automáticos

**NO necesitas configurar headers manualmente.** Postman automáticamente agregará:
```
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary...
```

Si ves que tienes un header `Content-Type` manual, **ELIMÍNALO**.

### 6. Enviar la Petición

1. Click en el botón azul **"Send"**
2. Espera la respuesta

### 7. Respuestas Esperadas

#### ✅ **Respuesta Exitosa (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlckBleGFtcGxlLmNvbSIsImlhdCI6MTcwOTc0MjAwMCwiZXhwIjoxNzA5ODI4NDAwfQ.xyz123...",
  "username": "testuser@example.com",
  "userID": "1",
  "role": "USER",
  "secretKey": null
}
```

#### ❌ **Error: Email ya existe (400 Bad Request):**
```json
{
  "error": "Email already in use"
}
```

#### ❌ **Error: Archivo muy grande (413 Payload Too Large):**
```json
{
  "error": "Maximum upload size exceeded"
}
```

### 8. Verificar que la Imagen se Guardó

#### **Opción A: Verificar en Base de Datos**
Ejecuta esta query en MySQL:
```sql
SELECT id, email, validationImageName, validationImagePath, accountValidated 
FROM users 
WHERE email = 'testuser@example.com';
```

Deberías ver:
```
id | email                  | validationImageName        | validationImagePath              | accountValidated
1  | testuser@example.com   | user_1_validation.jpg      | uploads/.../user_1_validation.jpg| 0
```

#### **Opción B: Usar el endpoint GET para obtener la imagen**
Crea otra request en Postman:
- **Método:** `GET`
- **URL:** `https://localhost:8443/api/auth/validation-image/1`
  (Reemplaza "1" con el userId que recibiste)
- **Headers:** 
  - `Authorization: Bearer {tu_token}`

Si todo está bien, verás la imagen en la respuesta.

### 9. Colección de Postman Completa

Crea estas requests para probar todo el flujo:

#### 1️⃣ **Register with Image**
```
POST https://localhost:8443/api/auth/register
Body: form-data
  - user (JSON): { email, password, driverLicense, billingAddress }
  - validationImage (File): [image.jpg]
```

#### 2️⃣ **Register without Image** (opcional)
```
POST https://localhost:8443/api/auth/register
Body: form-data
  - user (JSON): { email, password, driverLicense, billingAddress }
  (sin validationImage)
```

#### 3️⃣ **Get Validation Image**
```
GET https://localhost:8443/api/auth/validation-image/{userId}
Headers:
  - Authorization: Bearer {token}
```

#### 4️⃣ **Upload Image Later** (si no se subió en registro)
```
POST https://localhost:8443/api/auth/upload-validation-image/{userId}
Body: form-data
  - file (File): [image.jpg]
Headers:
  - Authorization: Bearer {token}
```

#### 5️⃣ **Check Account Validation Status**
```
GET https://localhost:8443/api/auth/is-validated/{userId}
Headers:
  - Authorization: Bearer {token}
```

#### 6️⃣ **Validate Account** (Solo Admin)
```
POST https://localhost:8443/api/auth/validate-account/{userId}?validated=true
Headers:
  - Authorization: Bearer {admin_token}
```

### 10. Troubleshooting

#### ❌ Error: "Required request part 'user' is not present"
**Solución:** Asegúrate de que el KEY sea exactamente `user` (en minúsculas) y esté marcado como JSON.

#### ❌ Error: "Content type 'text/plain' not supported"
**Solución:** Cambia el tipo del campo 'user' a **JSON (application/json)** en el dropdown.

#### ❌ Error: "Cannot parse JSON"
**Solución:** Verifica que el JSON esté bien formado (usa un validador JSON online).

#### ❌ Error: SSL/Certificate
**Solución:** En Postman, ve a Settings → General → desactiva "SSL certificate verification" (solo para desarrollo).

#### ❌ La imagen no aparece en la base de datos
**Solución:** 
1. Verifica que la carpeta `uploads/validation-images` exista
2. Verifica permisos de escritura en la carpeta
3. Revisa los logs del servidor

### 11. Script de Prueba Automático (Opcional)

Puedes usar este script en la pestaña "Tests" de Postman para validar automáticamente:

```javascript
// Validar que la respuesta sea 200
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

// Validar que el token existe
pm.test("Token is present", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.token).to.exist;
    pm.expect(jsonData.token).to.be.a('string');
});

// Validar que el userID existe
pm.test("UserID is present", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.userID).to.exist;
    
    // Guardar el userId para usar en otras requests
    pm.environment.set("userId", jsonData.userID);
});

// Guardar el token para usar en otras requests
var jsonData = pm.response.json();
if (jsonData.token) {
    pm.environment.set("authToken", jsonData.token);
}
```

### 12. Variables de Entorno en Postman

Crea un Environment con estas variables:

```
baseUrl: https://localhost:8443
userId: (se llenará automáticamente)
authToken: (se llenará automáticamente)
adminToken: (pegar manualmente el token de admin)
```

Luego usa en tus requests:
```
{{baseUrl}}/api/auth/register
{{baseUrl}}/api/auth/validation-image/{{userId}}
```

### 13. Exportar/Importar Colección

Para compartir o guardar tu configuración:

1. Click derecho en la colección
2. "Export"
3. Selecciona "Collection v2.1"
4. Guarda el archivo JSON

Para importar:
1. Click en "Import"
2. Arrastra el archivo JSON o selecciónalo

---

## 📝 Checklist de Prueba

- [ ] Request configurada como POST
- [ ] URL correcta (con HTTPS si aplica)
- [ ] Body tipo "form-data" seleccionado
- [ ] Campo 'user' agregado como JSON (application/json)
- [ ] JSON de 'user' válido y completo
- [ ] Campo 'validationImage' agregado como File
- [ ] Imagen seleccionada (menor a 10MB)
- [ ] NO hay header Content-Type manual
- [ ] SSL verificación desactivada (si es desarrollo local)
- [ ] Servidor corriendo en el puerto correcto

## 🎯 Ejemplo Rápido - Copy/Paste

**JSON para el campo 'user':**
```json
{
  "email": "nuevo.usuario@test.com",
  "password": "Password123!",
  "driverLicense": "DL987654321",
  "billingAddress": {
    "street": "456 Oak Avenue",
    "city": "Los Angeles",
    "state": "CA",
    "zipCode": "90001",
    "country": "USA"
  }
}
```

¡Listo! Con esta guía deberías poder probar el endpoint sin problemas. 🚀

