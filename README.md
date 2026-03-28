# 📱 BulkSMS API

A production-ready **REST API for bulk SMS sending**, built with **Java + Spring Boot**. It includes JWT authentication, role-based access control (ADMIN/USER), a credit system, scheduled messaging, payment integrations (PayPal & Stripe), 2FA with Google Authenticator, and full deployment support on **AWS EC2** with **Docker** and **Nginx**.


> 🌐 Live at: [theglobalmessaging.com](https://theglobalmessaging.com)

---

## 🚀 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3 |
| Security | Spring Security + JWT + 2FA (TOTP) |
| Database | MySQL 8 |
| ORM | Spring Data JPA / Hibernate |
| Payments | PayPal SDK + Stripe API |
| Auth | JWT, Google OAuth2, BCrypt |
| Containerization | Docker + Docker Compose |
| Reverse Proxy | Nginx (HTTPS/SSL) |
| Cloud | AWS EC2 |
| Build Tool | Maven |

---

## 🏗️ Architecture

```
src/main/java/com/example/bulksmsAPI/
├── Controller/       # REST endpoints (AuthController, MessageController, etc.)
├── Services/         # Business logic layer
├── Repositories/     # Spring Data JPA interfaces
├── Models/           # JPA entities
│   └── DTO/          # Data Transfer Objects
├── Security/         # JWT filter, JwtUtil, EncryptionUtil
└── Config/           # SecurityConfig, HttpsConfig, CorsConfig
```

---

## ✨ Features

- 🔐 **JWT Authentication** — stateless, token-based auth with 24h expiry
- 👥 **Role-based Access Control** — `ROLE_USER` and `ROLE_ADMIN` with `@PreAuthorize`
- 🔑 **Two-Factor Authentication (2FA)** — Google Authenticator TOTP + QR code generation
- 🌐 **Google OAuth2 Login** — social login with token exchange
- 📧 **Password Reset via Email** — token-based reset flow with expiry
- 📨 **Bulk SMS Sending** — send to multiple phone numbers via Horisen API
- 🕐 **Scheduled Messages** — `@Scheduled` job runs every minute to dispatch pending messages
- 💳 **Payment Integration** — buy credits via PayPal or Stripe
- 💰 **Credit System** — users spend credits per SMS sent; admin approves messages
- 📋 **Contact Management** — add/edit/delete contacts with group support
- 🧾 **Billing Address** — CRUD for user billing info
- ✅ **Account Validation** — admin validates user identity via uploaded document image
- 📁 **File Upload** — legal ID images stored server-side for validation
- 🔒 **HTTPS** — SSL/TLS enforced in production profile

---

## 🔌 API Endpoints Overview

### Auth — `/api/auth`
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/register` | Public | Register a new user |
| POST | `/login` | Public | Login and receive JWT |
| POST | `/google` | Public | Google OAuth2 login |
| POST | `/enable-2fa` | Public | Generate 2FA secret + QR |
| POST | `/verify-2fa` | Public | Verify TOTP code |
| POST | `/request-password-reset/{email}` | Public | Send password reset email |
| POST | `/reset-password` | Public | Reset password with token |
| GET | `/list` | ADMIN | List all users |
| POST | `/createadmin` | ADMIN | Create admin user |
| PUT | `/edit/{id}` | ADMIN | Edit user |
| DELETE | `/delete/{id}` | ADMIN | Delete user |
| GET | `/pending-validations` | ADMIN | Get users pending validation |
| PUT | `/validate-account/{id}` | ADMIN | Approve user account |

### Messages — `/api/message`
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/send` | USER | Submit bulk SMS (status: PENDING) |
| PUT | `/{id}/approve` | ADMIN | Approve and dispatch SMS |
| PUT | `/{id}/cancel` | ADMIN | Cancel SMS with reason |
| GET | `/pending` | ADMIN | List pending messages |

### Scheduled Messages — `/api/scheduledmessages`
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/create` | USER | Schedule a message |
| GET | `/user/{userId}` | USER | Get user's scheduled messages |
| PUT | `/update/{id}` | USER | Update scheduled message |
| DELETE | `/delete/{id}` | USER | Delete scheduled message |

### Contacts — `/api/contacts`
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/add` | USER | Add a contact |
| POST | `/add-batch` | USER | Bulk import contacts |
| POST | `/createGroup` | USER | Create contact group |
| GET | `/user/{userId}` | USER | List user contacts |
| PUT | `/edit/{id}` | USER | Update contact |
| DELETE | `/{id}` | USER | Delete contact |

### Payments — `/api/payment`
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/pay` | USER | Create PayPal payment |
| GET | `/success` | Public | PayPal payment callback |
| POST | `/stripe/checkout` | USER | Create Stripe checkout session |
| GET | `/transactions/all` | ADMIN | List all transactions |

### Plans — `/api/plans`
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/` | Public | List available plans |
| POST | `/create` | ADMIN | Create plan |
| PUT | `/update/{id}` | ADMIN | Update plan |
| DELETE | `/{id}` | ADMIN | Delete plan |

### Billing — `/api/billing`
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/` | USER | Save billing address |
| PUT | `/update` | USER | Update billing address |
| GET | `/find-billing-by-user` | USER | Get billing address by user |

---

## ⚙️ Local Setup

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8
- Docker & Docker Compose (optional)

### 1. Clone the repository
```bash
git clone https://github.com/CarlosA21/bulksmsAPI.git
cd bulksmsAPI
```

### 2. Configure environment variables

Copy the example env file and fill in your values:
```bash
cp .env.example .env
```

Required variables:
```env
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/bulksms
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=yourpassword
JWT_SECRET=your_jwt_secret_here
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
PAYPAL_CLIENT_ID=your_paypal_client_id
PAYPAL_CLIENT_SECRET=your_paypal_client_secret
STRIPE_API_KEY=your_stripe_key
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```

### 3. Run with Maven
```bash
./mvnw spring-boot:run
```

The API will start on `http://localhost:8443`

---

## 🐳 Run with Docker

### Quick Start (recommended)
```bash
docker-compose up --build
```

### Production (with Nginx + HTTPS)
```bash
docker-compose -f docker-compose.prod.yml up -d
```

### DockerHub image
```bash
docker-compose -f docker-compose.dockerhub.yml up -d
```

See [README-Docker.md](README-Docker.md) for full details.

---

## ☁️ Deploy to AWS EC2

A full step-by-step deployment is documented in [README-EC2-Deployment.md](README-EC2-Deployment.md).

Quick automated deploy:
```bash
# Step 1 — Set up EC2 instance
bash paso-1-setup-ec2.sh

# Step 2 — Upload files to EC2
bash paso-2-subir-archivos.sh




# Step 3 — Launch application
bash paso-3-deployment.sh
```



---




## 🧪 Testing the API

A Postman collection is included:

```
postman_collection.json
```

Import it in Postman and set the `base_url` variable to your server address.


See [API_TESTING_GUIDE.md](API_TESTING_GUIDE.md) for detailed request examples

---

## 🧠 Testing & Validation Approach

This project was not only developed with functionality in mind, but also with a strong focus on **quality assurance, system validation, and reliability under different conditions**.

### Functional Testing
- Verified SMS sending workflows for single and multiple recipients  
- Validated authentication flows (JWT, OAuth2, 2FA)  
- Ensured correct behavior of role-based access control (USER / ADMIN)  
- Confirmed correct handling of scheduled messages and background jobs  

### Input Validation
- Tested invalid phone numbers and malformed requests  
- Verified handling of empty message bodies and missing required fields  
- Checked behavior with invalid or expired authentication tokens  

### Edge Case Testing
- Extremely large recipient lists (bulk messaging scenarios)  
- Concurrent API requests from multiple users  
- Repeated or duplicate message submissions  
- Edge scenarios in scheduled messaging timing  

### Error Handling
- Verified consistent API error responses and status codes  
- Ensured graceful handling of failed external API calls (Horisen, PayPal, Stripe)  
- Tested failure scenarios in authentication and payment flows  

### Performance Evaluation
- Observed response time under normal and high-load conditions  
- Identified potential bottlenecks in message processing and scheduling  
- Evaluated system behavior under concurrent usage  

### Consistency & Reliability
- Ensured consistent API responses across repeated requests  
- Validated system stability under unpredictable user inputs  
- Verified that the system behaves correctly even in partial failure scenarios  

---

## 🧪 QA Mindset & Learnings

During this project, I focused not only on building features, but also on understanding how the system behaves under real-world conditions.

Key takeaways:
- Systems must be validated under both expected and unexpected inputs  
- Performance and reliability are as important as functionality  
- Testing edge cases helps uncover hidden issues in production systems  
- Backend systems must handle unpredictable user behavior gracefully  

---

## 🔐 Security Notes

- All passwords are hashed with **BCrypt**
- JWT tokens expire after **24 hours**
- CSRF is disabled (stateless API)
- CORS is configured and can be adjusted in `SecurityConfig.java`
- Sensitive configuration is loaded from **environment variables** — never hardcoded

---



## 📂 Additional Documentation

| File | Description |
|------|-------------|
| [README-Docker.md](README-Docker.md) | Docker setup guide |
| [README-Docker-QuickStart.md](README-Docker-QuickStart.md) | Docker quick start |
| [README-EC2-Deployment.md](README-EC2-Deployment.md) | AWS EC2 deployment guide |
| [API_TESTING_GUIDE.md](API_TESTING_GUIDE.md) | Postman testing guide |
| [ADMIN_VALIDATION_GUIDE.md](ADMIN_VALIDATION_GUIDE.md) | Admin account validation guide |
| [LEGAL_ID_GUIDE.md](LEGAL_ID_GUIDE.md) | Legal ID verification guide |

---

## 👨‍💻 Author

**Carlos A.** — [GitHub @CarlosA21](https://github.com/CarlosA21)




---

## 📄 License

This project is for portfolio and demonstration purposes.
