# Guía de Registro con Imagen de Validación

## Resumen de Cambios

Se ha modificado el sistema de registro para permitir que los usuarios suban una imagen de validación directamente durante el proceso de registro. Esto optimiza el flujo y reduce la carga en la API.

## Endpoint Actualizado

### POST `/api/auth/register`

**Tipo de Contenido:** `multipart/form-data`

**Parámetros:**

1. **user** (JSON, requerido):
   ```json
   {
     "email": "user@example.com",
     "password": "securePassword123",
     "driverLicense": "ABC123456",
     "billingAddress": {
       "street": "123 Main St",
       "city": "New York",
       "state": "NY",
       "zipCode": "10001",
       "country": "USA"
     }
   }
   ```

2. **validationImage** (File, opcional):
   - Imagen de validación (JPG, PNG, etc.)
   - Tamaño máximo: 10MB

## Ejemplo de Uso

### JavaScript/Fetch

```javascript
const registerUser = async (userData, imageFile) => {
  const formData = new FormData();
  
  // Agregar datos del usuario como JSON
  const userBlob = new Blob([JSON.stringify(userData)], {
    type: 'application/json'
  });
  formData.append('user', userBlob);
  
  // Agregar imagen si existe
  if (imageFile) {
    formData.append('validationImage', imageFile);
  }
  
  try {
    const response = await fetch('https://your-api.com/api/auth/register', {
      method: 'POST',
      body: formData
    });
    
    const data = await response.json();
    
    if (response.ok) {
      console.log('Registro exitoso:', data);
      // Guardar token, redireccionar, etc.
      localStorage.setItem('token', data.token);
      localStorage.setItem('userId', data.userID);
    } else {
      console.error('Error en registro:', data.error);
    }
  } catch (error) {
    console.error('Error de red:', error);
  }
};

// Ejemplo de uso
const userData = {
  email: "newuser@example.com",
  password: "MySecurePass123",
  driverLicense: "DL123456",
  billingAddress: {
    street: "123 Main St",
    city: "New York",
    state: "NY",
    zipCode: "10001",
    country: "USA"
  }
};

const imageInput = document.getElementById('validationImage');
const imageFile = imageInput.files[0];

registerUser(userData, imageFile);
```

### React Example

```jsx
import React, { useState } from 'react';

const RegistrationForm = () => {
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
  const [validationImage, setValidationImage] = useState(null);

  const handleImageChange = (e) => {
    setValidationImage(e.target.files[0]);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    const formDataToSend = new FormData();
    
    const userBlob = new Blob([JSON.stringify(formData)], {
      type: 'application/json'
    });
    formDataToSend.append('user', userBlob);
    
    if (validationImage) {
      formDataToSend.append('validationImage', validationImage);
    }
    
    try {
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        body: formDataToSend
      });
      
      const data = await response.json();
      
      if (response.ok) {
        // Registro exitoso
        localStorage.setItem('token', data.token);
        // Redireccionar al dashboard
      } else {
        // Mostrar error
        alert(data.error);
      }
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* Campos del formulario */}
      <input
        type="file"
        accept="image/*"
        onChange={handleImageChange}
        name="validationImage"
      />
      <button type="submit">Registrarse</button>
    </form>
  );
};
```

### Axios Example

```javascript
import axios from 'axios';

const registerWithImage = async (userData, imageFile) => {
  const formData = new FormData();
  
  formData.append('user', new Blob([JSON.stringify(userData)], {
    type: 'application/json'
  }));
  
  if (imageFile) {
    formData.append('validationImage', imageFile);
  }
  
  try {
    const response = await axios.post('/api/auth/register', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    
    return response.data;
  } catch (error) {
    throw error.response.data;
  }
};
```

## Respuesta Exitosa

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "user@example.com",
  "userID": "123",
  "role": "USER",
  "secretKey": null
}
```

## Respuesta de Error

```json
{
  "error": "Email already in use"
}
```

## Validaciones

- La imagen es **opcional** durante el registro
- Tamaño máximo del archivo: **10MB**
- Formatos aceptados: JPG, PNG, GIF, etc.
- Si no se proporciona imagen, el usuario puede subirla después usando el endpoint `/api/auth/upload-validation-image/{userId}`

## Ventajas de este Enfoque

1. **Reducción de llamadas a la API**: Todo se hace en una sola petición
2. **Mejor experiencia de usuario**: Proceso más fluido
3. **Menos carga en el servidor**: Un solo round-trip HTTP
4. **Mantenimiento simplificado**: Un endpoint en lugar de dos

## Endpoints Relacionados

### Subir imagen después del registro (si no se subió durante el registro)
```
POST /api/auth/upload-validation-image/{userId}
```

### Obtener imagen de validación
```
GET /api/auth/validation-image/{userId}
```

### Validar cuenta de usuario (solo admin)
```
POST /api/auth/validate-account/{userId}?validated=true
```

### Verificar si la cuenta está validada
```
GET /api/auth/is-validated/{userId}
```

## Configuración del Servidor

El servidor está configurado para aceptar archivos de hasta 10MB. Esta configuración se encuentra en `application.properties`:

```properties
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

## Notas Importantes

- El campo `validationImage` es **opcional** (`required = false`)
- La imagen se almacena automáticamente con el nombre `user_{userId}_validation.{extension}`
- La ruta de la imagen se guarda en la base de datos en los campos `validationImageName` y `validationImagePath`
- La cuenta del usuario tendrá `accountValidated = false` hasta que un administrador la valide

