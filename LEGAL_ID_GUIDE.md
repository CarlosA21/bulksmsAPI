.# 📋 Guía: Sistema Mejorado de Identificación Legal

## 🎯 ¿Qué se ha implementado?

Se ha mejorado el sistema de identificación de usuarios para soportar múltiples tipos de documentos de identidad, reemplazando el campo único `driverLicense` por un sistema más robusto y flexible.

---

## 🆕 Nuevos Campos en la Entidad User

### 1. **legalIdType** (Enum)
Tipo de documento de identidad. Valores posibles:
- `DRIVER_LICENSE` - Licencia de conducir
- `PASSPORT` - Pasaporte
- `NATIONAL_ID` - Cédula/DNI
- `SSN` - Social Security Number
- `TAX_ID` - Número de identificación fiscal
- `VOTER_ID` - Credencial de elector
- `OTHER` - Otro tipo de documento

### 2. **legalIdNumber** (String)
Número del documento de identidad.

---

## 📊 Estructura de la Base de Datos

```sql
ALTER TABLE User 
ADD COLUMN legal_id_type VARCHAR(50);

ALTER TABLE User 
ADD COLUMN legal_id_number VARCHAR(100);
```

### Migración de Datos Existentes
Si tienes datos existentes con `driverLicense`, ejecuta:

```sql
UPDATE User 
SET legal_id_type = 'DRIVER_LICENSE',
    legal_id_number = driver_license
WHERE driver_license IS NOT NULL;
```

---

## 🔧 Uso en el Registro

### JSON para Registro (Nuevo formato)

```json
{
  "email": "user@example.com",
  "password": "SecurePassword123",
  "legalIdType": "PASSPORT",
  "legalIdNumber": "AB1234567",
  "billingAddress": {
    "addressLine1": "123 Main St",
    "addressLine2": "Apt 4B",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  }
}
```

### Compatibilidad con formato antiguo

El sistema mantiene **compatibilidad hacia atrás**. Si envías `driverLicense`, se convertirá automáticamente:

```json
{
  "email": "user@example.com",
  "password": "SecurePassword123",
  "driverLicense": "DL123456",
  "billingAddress": { ... }
}
```

Se convierte internamente a:
```
legalIdType = "DRIVER_LICENSE"
legalIdNumber = "DL123456"
```

---

## 💻 Ejemplos de Uso

### JavaScript - Registro con Pasaporte

```javascript
const registerUser = async (userData) => {
  const response = await fetch('/api/auth/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      email: userData.email,
      password: userData.password,
      legalIdType: 'PASSPORT',
      legalIdNumber: userData.passportNumber,
      billingAddress: userData.billingAddress
    })
  });
  
  return await response.json();
};
```

### React Component - Selector de Tipo de ID

```jsx
import React, { useState } from 'react';

const RegistrationForm = () => {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    legalIdType: 'DRIVER_LICENSE',
    legalIdNumber: '',
    billingAddress: {
      addressLine1: '',
      addressLine2: '',
      city: '',
      state: '',
      zipCode: '',
      country: ''
    }
  });

  const legalIdTypes = [
    { value: 'DRIVER_LICENSE', label: "Driver's License" },
    { value: 'PASSPORT', label: 'Passport' },
    { value: 'NATIONAL_ID', label: 'National ID' },
    { value: 'SSN', label: 'Social Security Number' },
    { value: 'TAX_ID', label: 'Tax ID' },
    { value: 'VOTER_ID', label: 'Voter ID' },
    { value: 'OTHER', label: 'Other' }
  ];

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    const response = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(formData)
    });
    
    const data = await response.json();
    // Handle response
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
      
      <div>
        <label>ID Type:</label>
        <select
          value={formData.legalIdType}
          onChange={(e) => setFormData({...formData, legalIdType: e.target.value})}
          required
        >
          {legalIdTypes.map(type => (
            <option key={type.value} value={type.value}>
              {type.label}
            </option>
          ))}
        </select>
      </div>
      
      <input
        type="text"
        value={formData.legalIdNumber}
        onChange={(e) => setFormData({...formData, legalIdNumber: e.target.value})}
        placeholder="ID Number"
        required
      />
      
      {/* Billing Address fields */}
      <input
        type="text"
        value={formData.billingAddress.addressLine1}
        onChange={(e) => setFormData({
          ...formData, 
          billingAddress: {...formData.billingAddress, addressLine1: e.target.value}
        })}
        placeholder="Address Line 1"
        required
      />
      
      {/* ... más campos ... */}
      
      <button type="submit">Register</button>
    </form>
  );
};

export default RegistrationForm;
```

### Angular Component

```typescript
// legal-id.enum.ts
export enum LegalIdType {
  DRIVER_LICENSE = 'DRIVER_LICENSE',
  PASSPORT = 'PASSPORT',
  NATIONAL_ID = 'NATIONAL_ID',
  SSN = 'SSN',
  TAX_ID = 'TAX_ID',
  VOTER_ID = 'VOTER_ID',
  OTHER = 'OTHER'
}

// registration.component.ts
import { Component } from '@angular/core';
import { LegalIdType } from './legal-id.enum';

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html'
})
export class RegistrationComponent {
  legalIdTypes = [
    { value: LegalIdType.DRIVER_LICENSE, label: "Driver's License" },
    { value: LegalIdType.PASSPORT, label: 'Passport' },
    { value: LegalIdType.NATIONAL_ID, label: 'National ID' },
    { value: LegalIdType.SSN, label: 'Social Security Number' },
    { value: LegalIdType.TAX_ID, label: 'Tax ID' },
    { value: LegalIdType.VOTER_ID, label: 'Voter ID' },
    { value: LegalIdType.OTHER, label: 'Other' }
  ];

  formData = {
    email: '',
    password: '',
    legalIdType: LegalIdType.DRIVER_LICENSE,
    legalIdNumber: '',
    billingAddress: {
      addressLine1: '',
      addressLine2: '',
      city: '',
      state: '',
      zipCode: '',
      country: ''
    }
  };

  async onSubmit() {
    const response = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(this.formData)
    });
    
    const data = await response.json();
    // Handle response
  }
}
```

```html
<!-- registration.component.html -->
<form (ngSubmit)="onSubmit()">
  <input 
    [(ngModel)]="formData.email" 
    type="email" 
    placeholder="Email"
    required
  />
  
  <input 
    [(ngModel)]="formData.password" 
    type="password" 
    placeholder="Password"
    required
  />
  
  <label>ID Type:</label>
  <select [(ngModel)]="formData.legalIdType" required>
    <option *ngFor="let type of legalIdTypes" [value]="type.value">
      {{ type.label }}
    </option>
  </select>
  
  <input 
    [(ngModel)]="formData.legalIdNumber" 
    type="text" 
    placeholder="ID Number"
    required
  />
  
  <!-- Billing Address fields -->
  
  <button type="submit">Register</button>
</form>
```

---

## 🔒 Consideraciones de Seguridad

### 1. **Cifrado de Datos Sensibles**
Los números de identificación legal son PII (Personally Identifiable Information). Considera cifrarlos:

```java
// Antes de guardar
user.setLegalIdNumber(EncryptionUtil.encrypt(legalIdNumber));

// Al recuperar
String decryptedId = EncryptionUtil.decrypt(user.getLegalIdNumber());
```

### 2. **Validación por Tipo**
Implementa validaciones específicas según el tipo de documento:

```java
public boolean validateLegalId(LegalIdType type, String number) {
    switch (type) {
        case SSN:
            // Validar formato SSN: XXX-XX-XXXX
            return number.matches("\\d{3}-\\d{2}-\\d{4}");
        case PASSPORT:
            // Validar formato de pasaporte
            return number.matches("[A-Z]{1,2}\\d{6,9}");
        case DRIVER_LICENSE:
            // Validar según estado/país
            return number.length() >= 5 && number.length() <= 20;
        default:
            return number != null && !number.isEmpty();
    }
}
```

### 3. **Enmascaramiento en Logs**
```java
public String maskLegalId(String legalId) {
    if (legalId == null || legalId.length() < 4) {
        return "****";
    }
    return "****" + legalId.substring(legalId.length() - 4);
}
```

---

## 📋 Actualización del DTO PendingUserValidationDTO

El DTO de usuarios pendientes también incluye estos campos:

```java
{
  "userId": 1,
  "email": "user@example.com",
  "legalIdType": "PASSPORT",
  "legalIdNumber": "AB1234567",
  "validationImageBase64": "...",
  "billingAddress": { ... }
}
```

---

## 🔄 Migración desde Sistema Antiguo

### Paso 1: Ejecutar Script SQL
```bash
mysql -u root -p bulksms < migration_legal_id.sql
```

### Paso 2: Actualizar Frontend
Gradualmente reemplaza `driverLicense` por `legalIdType` + `legalIdNumber`.

### Paso 3: Periodo de Transición
El sistema acepta ambos formatos durante la transición.

### Paso 4: Deprecar Campo Antiguo
Después de la migración completa:
```sql
ALTER TABLE User DROP COLUMN driver_license;
```

---

## ✅ Ventajas del Nuevo Sistema

1. **Flexibilidad**: Soporta cualquier tipo de documento de identidad
2. **Internacional**: Funciona para usuarios de cualquier país
3. **Escalabilidad**: Fácil agregar nuevos tipos de documentos
4. **Validación**: Permite validaciones específicas por tipo
5. **Compliance**: Mejor para cumplimiento normativo (GDPR, CCPA, etc.)
6. **Seguridad**: Facilita el cifrado y enmascaramiento de datos sensibles

---

## 🧪 Prueba en Postman

### Request de Registro con Pasaporte
```
POST http://localhost:8443/api/auth/register

Headers:
  Content-Type: application/json

Body:
{
  "email": "john.doe@example.com",
  "password": "SecurePass123!",
  "legalIdType": "PASSPORT",
  "legalIdNumber": "US12345678",
  "billingAddress": {
    "addressLine1": "123 Main Street",
    "addressLine2": "Suite 100",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  }
}
```

### Request con Social Security Number
```json
{
  "email": "jane.smith@example.com",
  "password": "SecurePass123!",
  "legalIdType": "SSN",
  "legalIdNumber": "123-45-6789",
  "billingAddress": { ... }
}
```

---

## 📊 Valores del Enum en la Base de Datos

Los valores se guardan como strings en la base de datos:

| Enum Value | Database Value | Display Name |
|------------|----------------|--------------|
| DRIVER_LICENSE | DRIVER_LICENSE | Driver's License |
| PASSPORT | PASSPORT | Passport |
| NATIONAL_ID | NATIONAL_ID | National ID |
| SSN | SSN | Social Security Number |
| TAX_ID | TAX_ID | Tax ID |
| VOTER_ID | VOTER_ID | Voter ID |
| OTHER | OTHER | Other |

---

## 🎯 Recomendaciones Finales

1. **Cifra los datos sensibles** antes de guardarlos en la base de datos
2. **Implementa validación** específica por tipo de documento
3. **Registra auditorías** de acceso a datos de identificación
4. **Enmascara los IDs** en logs y respuestas API
5. **Implementa 2FA** para operaciones que muestren datos completos
6. **Cumple con regulaciones** (GDPR, CCPA, etc.) para datos PII
7. **Documenta claramente** qué tipo de IDs aceptas por país/región

---

¡Sistema implementado y listo para usar! 🚀

