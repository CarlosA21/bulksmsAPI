#!/bin/bash

echo "=== CREANDO USUARIO ADMINISTRADOR EN BULKSMS API ==="

# Verificar que el contenedor MySQL esté corriendo
if ! sudo docker ps | grep -q "bulksms-mysql"; then
    echo "Error: El contenedor bulksms-mysql no está corriendo"
    echo "Ejecuta primero: ./deploy21.sh"
    exit 1
fi

# Esperar a que MySQL esté completamente listo
echo "Verificando que MySQL esté listo..."
for i in {1..30}; do
    if sudo docker exec bulksms-mysql mysqladmin ping -h localhost --silent 2>/dev/null; then
        echo "MySQL está listo!"
        break
    fi
    echo "Esperando MySQL... ($i/30 segundos)"
    sleep 1
done

# Crear el archivo SQL temporal
cat > /tmp/create_admin.sql << 'EOF'
USE bulksmsdb;

-- Insertar usuario administrador
INSERT INTO user (
    username,
    email,
    password,
    roles,
    driver_license,
    dob,

)
VALUES (
    'admin',
    'admin@bulksms.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'ROLE_ADMIN',
    'ENCRYPTED_DL_123456789',
    '1990-01-01',
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
SELECT 'Usuario creado:' as info;
SELECT user_id, username, email, roles, dob FROM User WHERE username = 'admin';

-- Verificar la cuenta de crédito
SELECT 'Cuenta de crédito:' as info;
SELECT ca.*, u.username
FROM credit_account ca
JOIN User u ON ca.user_id = u.user_id
WHERE u.username = 'admin';

-- Verificar la dirección de facturación
SELECT 'Dirección de facturación:' as info;
SELECT ba.*, u.username
FROM billing_address ba
JOIN User u ON ba.user_id = u.user_id
WHERE u.username = 'admin';
EOF

# Ejecutar el SQL en el contenedor
echo "Ejecutando script SQL..."
sudo docker exec -i bulksms-mysql mysql -u bulksmsuser -pbulksmspass < /tmp/create_admin.sql

# Verificar si hubo errores
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ USUARIO ADMINISTRADOR CREADO EXITOSAMENTE"
    echo ""
    echo "=== CREDENCIALES DEL ADMIN ==="
    echo "Username: admin"
    echo "Email: admin@bulksms.com"
    echo "Password: password"
    echo "Role: ROLE_ADMIN"
    echo "Balance inicial: $1000 USD"
    echo ""
    echo "Ya puedes hacer login desde el frontend con estas credenciales."
else
    echo ""
    echo "❌ ERROR AL CREAR EL USUARIO ADMINISTRADOR"
    echo "Verifica que el contenedor MySQL esté corriendo y que la base de datos esté inicializada."
fi

# Limpiar archivo temporal
rm -f /tmp/create_admin.sql

echo ""
echo "=== PROCESO COMPLETADO ==="
