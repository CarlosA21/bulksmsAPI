-- Script SQL para crear un usuario administrador en bulksmsAPI
-- Ejecutar dentro del contenedor MySQL: sudo docker exec -it bulksms-mysql mysql -u bulksmsuser -p

USE bulksmsdb;

-- Insertar usuario administrador
INSERT INTO User (
    username,
    email,
    password,
    roles,
    driver_license,
    dob,
    secretKey
)
VALUES (
    'admin',
    'admin@bulksms.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- password "password" hasheado con BCrypt
    'ROLE_ADMIN',
    'ENCRYPTED_DL_123456789', -- Valor encriptado para driver license (requerido)
    '1990-01-01',
    'admin-secret-key-2024'
);

-- Crear cuenta de crédito para el usuario admin
INSERT INTO credit_account (user_id, balance, currency)
VALUES (
    (SELECT user_id FROM User WHERE username = 'admin'),
    1000.00,
    'USD'
);

-- Crear dirección de facturación para el usuario admin
INSERT INTO billing_address (
    user_id,
    street,
    city,
    state,
    zip_code,
    country
)
VALUES (
    (SELECT user_id FROM User WHERE username = 'admin'),
    'Admin Street 123',
    'Admin City',
    'Admin State',
    '12345',
    'USA'
);

-- Verificar que se creó el usuario correctamente
SELECT user_id, username, email, roles, dob FROM User WHERE username = 'admin';

-- Verificar la cuenta de crédito
SELECT ca.*, u.username
FROM credit_account ca
JOIN User u ON ca.user_id = u.user_id
WHERE u.username = 'admin';

-- Verificar la dirección de facturación
SELECT ba.*, u.username
FROM billing_address ba
JOIN User u ON ba.user_id = u.user_id
WHERE u.username = 'admin';

-- CREDENCIALES DEL USUARIO ADMIN:
-- Username: admin
-- Email: admin@bulksms.com
-- Password: password
-- Role: ROLE_ADMIN
-- Balance inicial: $1000 USD

-- INSTRUCCIONES DE USO:
-- 1. Conectarse al contenedor MySQL:
--    sudo docker exec -it bulksms-mysql mysql -u bulksmsuser -p
-- 2. Cuando pida la contraseña, usar: bulksmspass
-- 3. Copiar y pegar este script completo
-- 4. El usuario admin estará listo para usar en el frontend
