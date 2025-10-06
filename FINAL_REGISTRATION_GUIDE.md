# 🚀 Guía Completa: Registro con Imagen (2 Pasos)

## ✅ Solución Final Implementada

Se ha implementado un enfoque de **2 pasos** para mantener la consistencia con el frontend existente que usa JSON:

1. **Paso 1:** Registro de usuario con JSON (sin imagen)
2. **Paso 2:** Subida de imagen de validación (opcional, con form-data)

---

## 📋 PASO 1: Registro de Usuario (JSON)

### Endpoint
```
POST /api/auth/register
```

### Headers
```
Content-Type: application/json
```

### Body (JSON)
```json
{
  "email": "newuser@example.com",
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

### Respuesta Exitosa (200 OK)
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "newuser@example.com",
  "userID": "1",
  "role": "USER",
  "secretKey": null
}
```

### En Postman (Paso 1)
1. **Método:** `POST`
2. **URL:** `https://localhost:8443/api/auth/register`
3. **Headers:** 
   - `Content-Type: application/json`
4. **Body:** Selecciona `raw` → `JSON`
5. Pega el JSON de arriba
6. Click en **Send**
7. **Guarda el `userID` y `token` de la respuesta** para el siguiente paso

---

## 📸 PASO 2: Subir Imagen de Validación (Form-Data)

### Endpoint
```
POST /api/auth/upload-validation-image/{userId}
```

### Headers
```
Authorization: Bearer {token_del_paso_1}
```

### Body (form-data)
```
KEY: file
TYPE: File
VALUE: [Select your image file]
```

### Respuesta Exitosa (200 OK)
```json
{
  "message": "Validation image uploaded successfully",
  "imageName": "user_1_validation.jpg",
  "accountValidated": false
}
```

### En Postman (Paso 2)
1. **Método:** `POST`
2. **URL:** `https://localhost:8443/api/auth/upload-validation-image/1`
   - Reemplaza `1` con el `userID` que recibiste en el Paso 1
3. **Headers:**
   - `Authorization: Bearer eyJhbGci...` (token del Paso 1)
4. **Body:** Selecciona `form-data`
5. Agrega:
   - **KEY:** `file`
   - **TYPE:** Cambia a `File`
   - **VALUE:** Click en "Select Files" y escoge una imagen
6. Click en **Send**

---

## 💻 Ejemplo Frontend Completo

### JavaScript Vanilla

```javascript
async function registerUserWithImage(userData, imageFile) {
  try {
    // PASO 1: Registrar usuario
    const registerResponse = await fetch('/api/auth/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(userData)
    });

    if (!registerResponse.ok) {
      const error = await registerResponse.json();
      throw new Error(error.error);
    }

    const { token, userID } = await registerResponse.json();
    
    // Guardar token
    localStorage.setItem('authToken', token);
    localStorage.setItem('userId', userID);

    // PASO 2: Subir imagen si existe
    if (imageFile) {
      const formData = new FormData();
      formData.append('file', imageFile);

      const uploadResponse = await fetch(`/api/auth/upload-validation-image/${userID}`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        body: formData
      });

      if (!uploadResponse.ok) {
        console.error('Failed to upload image, but registration succeeded');
      } else {
        console.log('Image uploaded successfully');
      }
    }

    return { success: true, token, userID };

  } catch (error) {
    console.error('Registration failed:', error);
    return { success: false, error: error.message };
  }
}

// USO
const userData = {
  email: "user@example.com",
  password: "Password123",
  driverLicense: "DL123456",
  billingAddress: {
    street: "123 Main St",
    city: "New York",
    state: "NY",
    "zipCode": "10001",
    country: "USA"
  }
};

const imageInput = document.getElementById('validationImage');
const imageFile = imageInput.files[0];

registerUserWithImage(userData, imageFile);
```

### React Example

```jsx
import React, { useState } from 'react';

function RegistrationForm() {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    driverLicense: '',
    billingAddress: {
      street: '',
      city: '',
      state: '',
      zipCode: '',
      country: ''
    }
  });
  const [imageFile, setImageFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      // PASO 1: Registrar usuario
      const registerRes = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });

      if (!registerRes.ok) {
        const errorData = await registerRes.json();
        throw new Error(errorData.error);
      }

      const { token, userID } = await registerRes.json();
      
      // Guardar token
      localStorage.setItem('authToken', token);
      localStorage.setItem('userId', userID);

      // PASO 2: Subir imagen si existe
      if (imageFile) {
        const formData = new FormData();
        formData.append('file', imageFile);

        await fetch(`/api/auth/upload-validation-image/${userID}`, {
          method: 'POST',
          headers: { 'Authorization': `Bearer ${token}` },
          body: formData
        });
      }

      // Redirigir al dashboard
      window.location.href = '/dashboard';

    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="email"
        value={formData.email}
        onChange={(e) => setFormData({...formData, email: e.target.value})}
        placeholder="Email"
        required
      />
      
      <input
        type="password"
        value={formData.password}
        onChange={(e) => setFormData({...formData, password: e.target.value})}
        placeholder="Password"
        required
      />
      
      <input
        type="text"
        value={formData.driverLicense}
        onChange={(e) => setFormData({...formData, driverLicense: e.target.value})}
        placeholder="Driver License"
        required
      />

      {/* Billing Address fields */}
      <input
        type="text"
        value={formData.billingAddress.street}
        onChange={(e) => setFormData({
          ...formData, 
          billingAddress: {...formData.billingAddress, street: e.target.value}
        })}
        placeholder="Street"
        required
      />

      {/* ... otros campos de billing address ... */}

      <input
        type="file"
        accept="image/*"
        onChange={(e) => setImageFile(e.target.files[0])}
      />

      {error && <div className="error">{error}</div>}
      
      <button type="submit" disabled={loading}>
        {loading ? 'Registering...' : 'Register'}
      </button>
    </form>
  );
}

export default RegistrationForm;
```

### Vue.js Example

```vue
<template>
  <form @submit.prevent="handleRegister">
    <input v-model="formData.email" type="email" placeholder="Email" required />
    <input v-model="formData.password" type="password" placeholder="Password" required />
    <input v-model="formData.driverLicense" type="text" placeholder="Driver License" required />
    
    <!-- Billing Address -->
    <input v-model="formData.billingAddress.street" placeholder="Street" required />
    <input v-model="formData.billingAddress.city" placeholder="City" required />
    <input v-model="formData.billingAddress.state" placeholder="State" required />
    <input v-model="formData.billingAddress.zipCode" placeholder="Zip Code" required />
    <input v-model="formData.billingAddress.country" placeholder="Country" required />
    
    <!-- Validation Image -->
    <input type="file" @change="handleFileChange" accept="image/*" />
    
    <button type="submit" :disabled="loading">
      {{ loading ? 'Registering...' : 'Register' }}
    </button>
    
    <div v-if="error" class="error">{{ error }}</div>
  </form>
</template>

<script>
export default {
  data() {
    return {
      formData: {
        email: '',
        password: '',
        driverLicense: '',
        billingAddress: {
          street: '',
          city: '',
          state: '',
          zipCode: '',
          country: ''
        }
      },
      imageFile: null,
      loading: false,
      error: null
    }
  },
  methods: {
    handleFileChange(e) {
      this.imageFile = e.target.files[0];
    },
    async handleRegister() {
      this.loading = true;
      this.error = null;

      try {
        // PASO 1: Registrar
        const registerRes = await fetch('/api/auth/register', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(this.formData)
        });

        if (!registerRes.ok) {
          const errorData = await registerRes.json();
          throw new Error(errorData.error);
        }

        const { token, userID } = await registerRes.json();
        localStorage.setItem('authToken', token);
        localStorage.setItem('userId', userID);

        // PASO 2: Subir imagen
        if (this.imageFile) {
          const formData = new FormData();
          formData.append('file', this.imageFile);

          await fetch(`/api/auth/upload-validation-image/${userID}`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` },
            body: formData
          });
        }

        this.$router.push('/dashboard');

      } catch (err) {
        this.error = err.message;
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>
```

---

## 🔍 Endpoints Adicionales

### Obtener Imagen de Validación
```
GET /api/auth/validation-image/{userId}
Headers: Authorization: Bearer {token}
```

**Respuesta:** Imagen en formato binario (JPEG/PNG)

### Verificar si la Cuenta está Validada
```
GET /api/auth/is-validated/{userId}
Headers: Authorization: Bearer {token}
```

**Respuesta:**
```json
{
  "userId": 1,
  "accountValidated": false
}
```

### Validar Cuenta de Usuario (Solo Admin)
```
POST /api/auth/validate-account/{userId}?validated=true
Headers: Authorization: Bearer {admin_token}
```

**Respuesta:**
```json
{
  "message": "User account validation status updated",
  "userId": 1,
  "accountValidated": true
}
```

---

## 🎯 Flujo Completo de Validación de Usuario

```
1. Usuario se registra → POST /api/auth/register (JSON)
   ↓
2. Recibe token y userID
   ↓
3. Usuario sube imagen → POST /api/auth/upload-validation-image/{userId}
   ↓
4. Admin revisa imagen → GET /api/auth/validation-image/{userId}
   ↓
5. Admin valida cuenta → POST /api/auth/validate-account/{userId}?validated=true
   ↓
6. Usuario ahora tiene accountValidated = true
```

---

## ⚙️ Configuración del Servidor

### application.properties
```properties
# Multipart File Upload
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# File Storage
file.upload-dir=uploads/validation-images
```

### Estructura de Archivos
```
uploads/
  └── validation-images/
      ├── user_1_validation.jpg
      ├── user_2_validation.png
      └── user_3_validation.jpg
```

---

## 🐛 Troubleshooting

### Error: "Email already in use"
**Causa:** Ya existe un usuario con ese email  
**Solución:** Usar un email diferente o eliminar el usuario existente

### Error: "Content-Type 'application/octet-stream' is not supported"
**Causa:** Enviando form-data al endpoint de registro  
**Solución:** El registro usa JSON (`/register`), solo la imagen usa form-data (`/upload-validation-image`)

### Error: "Maximum upload size exceeded"
**Causa:** La imagen es mayor a 10MB  
**Solución:** Redimensionar la imagen antes de subirla

### Error: "User not found with ID"
**Causa:** userId incorrecto  
**Solución:** Verificar que estás usando el userID correcto del Paso 1

### Error: 401 Unauthorized al subir imagen
**Causa:** Token inválido o no enviado  
**Solución:** Agregar header `Authorization: Bearer {token}`

---

## ✅ Checklist de Prueba

### Paso 1 - Registro (Postman)
- [ ] Método: POST
- [ ] URL: `/api/auth/register`
- [ ] Body: `raw` → `JSON`
- [ ] JSON válido y completo
- [ ] Headers: `Content-Type: application/json`
- [ ] Respuesta 200 con token y userID

### Paso 2 - Subir Imagen (Postman)
- [ ] Método: POST
- [ ] URL: `/api/auth/upload-validation-image/{userID}`
- [ ] Body: `form-data`
- [ ] KEY: `file`, TYPE: `File`
- [ ] Headers: `Authorization: Bearer {token}`
- [ ] Imagen seleccionada (< 10MB)
- [ ] Respuesta 200 con mensaje de éxito

---

## 📊 Ventajas de Este Enfoque

✅ **Consistencia:** Mismo formato JSON que usas actualmente  
✅ **Separación:** Registro y subida de imagen son independientes  
✅ **Flexibilidad:** Usuario puede subir imagen después  
✅ **Mejor UX:** Puedes mostrar progreso separado para cada paso  
✅ **Fácil de probar:** JSON en Postman es más simple  
✅ **Manejo de errores:** Puedes manejar errores de cada paso por separado  

---

## 🚀 Resumen Rápido

**¿Cómo registrar un usuario con imagen?**

1. **Registro (JSON):** 
   ```javascript
   POST /api/auth/register
   Body: { email, password, driverLicense, billingAddress }
   ```

2. **Subir Imagen (Form-Data):**
   ```javascript
   POST /api/auth/upload-validation-image/{userId}
   Body: FormData con 'file'
   Headers: Authorization: Bearer {token}
   ```

**¡Listo!** 🎉

