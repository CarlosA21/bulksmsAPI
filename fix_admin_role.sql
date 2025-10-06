-- Script para verificar y corregir el rol del usuario admin
USE bulksmsdb;

-- Verificar usuarios existentes y sus roles
SELECT user_id, username, email, roles FROM User WHERE username LIKE '%admin%' OR email LIKE '%admin%';

-- Si existe un usuario admin, actualizar su rol a 'ADMIN' (sin prefijo ROLE_)
UPDATE User SET roles = 'ADMIN' WHERE username = 'admin' OR email LIKE '%admin%';

-- Verificar que el cambio se aplicó correctamente
SELECT user_id, username, email, roles FROM User WHERE username = 'admin' OR email LIKE '%admin%';

-- Si no existe usuario admin, crear uno (opcional - descomenta las siguientes líneas si necesitas crear uno)
-- INSERT INTO User (username, email, password, roles, driver_license, dob)
-- VALUES ('admin', 'admin@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN', 'encrypted_license', '1990-01-01');
