# 📋 Guía: Endpoint de Validación de Usuarios Pendientes

## 🎯 Endpoint Creado

### GET `/api/auth/pending-validations`

Endpoint exclusivo para **administradores** que devuelve todos los usuarios con `accountValidated = false` junto con sus imágenes de validación.

---

## 📡 Uso del Endpoint

### Request

```
GET https://localhost:8443/api/auth/pending-validations

Headers:
  Authorization: Bearer {admin_token}
```

### Respuesta Exitosa (200 OK)

```json
[
  {
    "userId": 1,
    "email": "user1@example.com",
    "driverLicense": "DL123456",
    "validationImageName": "user_1_validation.jpg",
    "validationImageBase64": "/9j/4AAQSkZJRgABAQAAAQABAAD/2wBD...", 
    "billingAddress": {
      "street": "123 Main St",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001",
      "country": "USA"
    },
    "registrationDate": null
  },
  {
    "userId": 2,
    "email": "user2@example.com",
    "driverLicense": "DL789012",
    "validationImageName": "user_2_validation.png",
    "validationImageBase64": "iVBORw0KGgoAAAANSUhEUgAA...",
    "billingAddress": {
      "street": "456 Oak Ave",
      "city": "Los Angeles",
      "state": "CA",
      "zipCode": "90001",
      "country": "USA"
    },
    "registrationDate": null
  }
]
```

### Respuesta de Error (403 Forbidden)

```json
"Access Denied: Admin role required."
```

---

## 🧪 Prueba en Postman

1. **Método:** `GET`
2. **URL:** `https://localhost:8443/api/auth/pending-validations`
3. **Headers:**
   - `Authorization: Bearer {tu_token_de_admin}`
4. **Send**

**Nota:** Asegúrate de usar un token de un usuario con rol `ADMIN`.

---

## 🅰️ Componente Angular para Aprobación de Usuarios

### 1. Crear el Servicio

```typescript
// user-validation.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PendingUserValidation {
  userId: number;
  email: string;
  driverLicense: string;
  validationImageName: string;
  validationImageBase64: string;
  billingAddress: {
    street: string;
    city: string;
    state: string;
    zipCode: string;
    country: string;
  };
  registrationDate: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserValidationService {
  private apiUrl = 'https://localhost:8443/api/auth';

  constructor(private http: HttpClient) { }

  getPendingValidations(): Observable<PendingUserValidation[]> {
    const token = localStorage.getItem('authToken');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.get<PendingUserValidation[]>(
      `${this.apiUrl}/pending-validations`,
      { headers }
    );
  }

  validateUserAccount(userId: number, validated: boolean): Observable<any> {
    const token = localStorage.getItem('authToken');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.post(
      `${this.apiUrl}/validate-account/${userId}?validated=${validated}`,
      {},
      { headers }
    );
  }
}
```

### 2. Crear el Componente

```typescript
// pending-validations.component.ts
import { Component, OnInit } from '@angular/core';
import { UserValidationService, PendingUserValidation } from './user-validation.service';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-pending-validations',
  templateUrl: './pending-validations.component.html',
  styleUrls: ['./pending-validations.component.css']
})
export class PendingValidationsComponent implements OnInit {
  pendingUsers: PendingUserValidation[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private validationService: UserValidationService,
    private sanitizer: DomSanitizer
  ) { }

  ngOnInit(): void {
    this.loadPendingUsers();
  }

  loadPendingUsers(): void {
    this.loading = true;
    this.error = null;

    this.validationService.getPendingValidations().subscribe({
      next: (users) => {
        this.pendingUsers = users;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar usuarios pendientes';
        this.loading = false;
        console.error(err);
      }
    });
  }

  getImageUrl(base64: string): SafeUrl {
    if (!base64) return '';
    return this.sanitizer.bypassSecurityTrustUrl(`data:image/jpeg;base64,${base64}`);
  }

  approveUser(userId: number): void {
    if (confirm('¿Estás seguro de aprobar este usuario?')) {
      this.validationService.validateUserAccount(userId, true).subscribe({
        next: (response) => {
          alert('Usuario aprobado exitosamente');
          this.loadPendingUsers(); // Recargar lista
        },
        error: (err) => {
          alert('Error al aprobar usuario');
          console.error(err);
        }
      });
    }
  }

  rejectUser(userId: number): void {
    if (confirm('¿Estás seguro de rechazar este usuario?')) {
      this.validationService.validateUserAccount(userId, false).subscribe({
        next: (response) => {
          alert('Usuario rechazado');
          this.loadPendingUsers();
        },
        error: (err) => {
          alert('Error al rechazar usuario');
          console.error(err);
        }
      });
    }
  }
}
```

### 3. Template HTML

```html
<!-- pending-validations.component.html -->
<div class="container">
  <h2>Usuarios Pendientes de Validación</h2>

  <div *ngIf="loading" class="loading">
    <p>Cargando usuarios...</p>
  </div>

  <div *ngIf="error" class="error-message">
    {{ error }}
  </div>

  <div *ngIf="!loading && pendingUsers.length === 0" class="no-users">
    <p>No hay usuarios pendientes de validación</p>
  </div>

  <div class="user-cards" *ngIf="!loading && pendingUsers.length > 0">
    <div class="user-card" *ngFor="let user of pendingUsers">
      <div class="user-info">
        <h3>{{ user.email }}</h3>
        <p><strong>ID:</strong> {{ user.userId }}</p>
        <p><strong>Licencia:</strong> {{ user.driverLicense }}</p>
        
        <div class="billing-info">
          <h4>Dirección de Facturación</h4>
          <p>{{ user.billingAddress.street }}</p>
          <p>{{ user.billingAddress.city }}, {{ user.billingAddress.state }} {{ user.billingAddress.zipCode }}</p>
          <p>{{ user.billingAddress.country }}</p>
        </div>
      </div>

      <div class="validation-image">
        <h4>Imagen de Validación</h4>
        <img 
          *ngIf="user.validationImageBase64" 
          [src]="getImageUrl(user.validationImageBase64)" 
          alt="Validation Image"
          class="image-preview"
        />
        <p *ngIf="!user.validationImageBase64" class="no-image">
          No hay imagen de validación
        </p>
      </div>

      <div class="actions">
        <button 
          class="btn-approve" 
          (click)="approveUser(user.userId)"
          [disabled]="!user.validationImageBase64"
        >
          ✓ Aprobar
        </button>
        <button 
          class="btn-reject" 
          (click)="rejectUser(user.userId)"
        >
          ✗ Rechazar
        </button>
      </div>
    </div>
  </div>
</div>
```

### 4. Estilos CSS

```css
/* pending-validations.component.css */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

h2 {
  color: #333;
  margin-bottom: 20px;
}

.loading {
  text-align: center;
  padding: 40px;
  font-size: 18px;
  color: #666;
}

.error-message {
  background-color: #fee;
  color: #c00;
  padding: 15px;
  border-radius: 5px;
  margin-bottom: 20px;
}

.no-users {
  text-align: center;
  padding: 40px;
  color: #666;
}

.user-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.user-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.user-info h3 {
  color: #2c3e50;
  margin-top: 0;
  margin-bottom: 15px;
}

.user-info p {
  margin: 8px 0;
  color: #555;
}

.billing-info {
  margin-top: 15px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 5px;
}

.billing-info h4 {
  margin-top: 0;
  color: #495057;
  font-size: 14px;
}

.validation-image {
  margin: 20px 0;
}

.validation-image h4 {
  margin-bottom: 10px;
  color: #495057;
}

.image-preview {
  width: 100%;
  max-height: 300px;
  object-fit: contain;
  border: 1px solid #dee2e6;
  border-radius: 5px;
  background: #f8f9fa;
}

.no-image {
  text-align: center;
  padding: 40px;
  color: #999;
  background: #f8f9fa;
  border-radius: 5px;
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.btn-approve,
.btn-reject {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-approve {
  background-color: #28a745;
  color: white;
}

.btn-approve:hover:not(:disabled) {
  background-color: #218838;
}

.btn-approve:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
  opacity: 0.5;
}

.btn-reject {
  background-color: #dc3545;
  color: white;
}

.btn-reject:hover {
  background-color: #c82333;
}
```

### 5. Módulo (app.module.ts)

```typescript
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';

import { AppComponent } from './app.component';
import { PendingValidationsComponent } from './pending-validations/pending-validations.component';
import { UserValidationService } from './user-validation.service';

@NgModule({
  declarations: [
    AppComponent,
    PendingValidationsComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule
  ],
  providers: [UserValidationService],
  bootstrap: [AppComponent]
})
export class AppModule { }
```

### 6. Routing (app-routing.module.ts)

```typescript
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PendingValidationsComponent } from './pending-validations/pending-validations.component';

const routes: Routes = [
  { 
    path: 'admin/pending-validations', 
    component: PendingValidationsComponent 
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
```

---

## 🔐 Guard de Autenticación (Opcional)

```typescript
// admin.guard.ts
import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {
  constructor(private router: Router) {}

  canActivate(): boolean {
    const token = localStorage.getItem('authToken');
    const role = localStorage.getItem('role');

    if (token && role === 'ADMIN') {
      return true;
    }

    this.router.navigate(['/login']);
    return false;
  }
}

// Usar en routing:
// { path: 'admin/pending-validations', component: PendingValidationsComponent, canActivate: [AdminGuard] }
```

---

## 🔄 Flujo Completo

1. **Usuario se registra** → `POST /api/auth/register`
2. **Usuario sube imagen** → `POST /api/auth/upload-validation-image/{userId}`
3. **Admin accede al panel** → `GET /api/auth/pending-validations`
4. **Admin revisa la imagen y datos**
5. **Admin aprueba/rechaza** → `POST /api/auth/validate-account/{userId}?validated=true/false`
6. **Usuario es notificado** (opcional: implementar notificación por email)

---

## 📊 Respuesta del Endpoint de Validación

```json
{
  "message": "User account validation status updated",
  "userId": 1,
  "accountValidated": true
}
```

---

## 🎨 Ejemplo de Vista Final

```
┌─────────────────────────────────────────────────────┐
│  Usuarios Pendientes de Validación                 │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌─────────────────┐  ┌─────────────────┐         │
│  │ user@email.com  │  │ user2@email.com │         │
│  │ ID: 1           │  │ ID: 2           │         │
│  │ DL: ABC123      │  │ DL: XYZ789      │         │
│  │                 │  │                 │         │
│  │ [Imagen]        │  │ [Imagen]        │         │
│  │                 │  │                 │         │
│  │ [✓ Aprobar]     │  │ [✓ Aprobar]     │         │
│  │ [✗ Rechazar]    │  │ [✗ Rechazar]    │         │
│  └─────────────────┘  └─────────────────┘         │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

## ✅ Features Implementadas

- ✅ Endpoint que devuelve usuarios no validados
- ✅ Imágenes convertidas a Base64 para fácil uso en Angular
- ✅ Información completa del usuario (email, licencia, dirección)
- ✅ Solo accesible para administradores
- ✅ Endpoint para aprobar/rechazar usuarios
- ✅ Componente Angular completo y funcional

---

## 🚀 Para Producción

**Mejoras sugeridas:**

1. **Paginación:** Si hay muchos usuarios, agregar paginación
2. **Filtros:** Filtrar por fecha, email, etc.
3. **Búsqueda:** Barra de búsqueda para encontrar usuarios
4. **Notificaciones:** Email al usuario cuando sea aprobado/rechazado
5. **Historial:** Log de quién aprobó/rechazó y cuándo
6. **Zoom de imagen:** Modal para ver imagen en tamaño completo
7. **Comentarios:** Permitir al admin dejar notas sobre la validación

---

¡El sistema está listo para usar! 🎉

