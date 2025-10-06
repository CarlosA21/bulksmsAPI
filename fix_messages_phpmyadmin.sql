-- ==============================================
-- SCRIPT SQL PARA PHPMYADMIN - CORREGIR TABLA MESSAGES
-- Ejecuta cada sección por separado en phpMyAdmin
-- ==============================================

-- PASO 1: Seleccionar la base de datos
USE bulksms;

-- PASO 2: Verificar estructura actual de la tabla
DESCRIBE messages;

-- PASO 3: Verificar si existe la columna phone_number (snake_case)
SELECT COUNT(*) as phone_number_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'bulksms'
  AND TABLE_NAME = 'messages'
  AND COLUMN_NAME = 'phone_number';

-- PASO 4: Verificar si existe la columna phoneNumber (camelCase)
SELECT COUNT(*) as phoneNumber_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'bulksms'
  AND TABLE_NAME = 'messages'
  AND COLUMN_NAME = 'phoneNumber';

-- PASO 5: Renombrar columna phone_number a phoneNumber (si existe)
-- EJECUTA SOLO SI EL PASO 3 DEVOLVIÓ 1 Y EL PASO 4 DEVOLVIÓ 0
ALTER TABLE messages
CHANGE COLUMN phone_number phoneNumber VARCHAR(20) NULL;

-- PASO 6: Crear columna phoneNumber si no existe ninguna
-- EJECUTA SOLO SI AMBOS PASOS 3 Y 4 DEVOLVIERON 0
ALTER TABLE messages
ADD COLUMN phoneNumber VARCHAR(20) NULL;

-- PASO 7: Corregir campo status (convertir de ENUM a VARCHAR)
ALTER TABLE messages
MODIFY COLUMN status VARCHAR(20) NULL;

-- PASO 8: Asegurar que message sea TEXT
ALTER TABLE messages
MODIFY COLUMN message TEXT;

-- PASO 9: Asegurar que cancellationReason sea TEXT
ALTER TABLE messages
MODIFY COLUMN cancellationReason TEXT NULL;

-- PASO 10: Corregir campo date
ALTER TABLE messages
MODIFY COLUMN date TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP;

-- PASO 11: Limpiar valores problemáticos en status
UPDATE messages SET status = 'PENDING' WHERE status = '0' OR status = '' OR status IS NULL;
UPDATE messages SET status = 'SENT' WHERE status = '1';
UPDATE messages SET status = 'FAILED' WHERE status = '2';

-- PASO 12: Crear índices para mejor rendimiento
CREATE INDEX idx_messages_user_id ON messages(user_id);
CREATE INDEX idx_messages_status ON messages(status);
CREATE INDEX idx_messages_date ON messages(date);

-- PASO 13: Verificar estructura final
DESCRIBE messages;

-- PASO 14: Verificar datos actuales
SELECT messageId, message, phoneNumber, status, date, user_id, creditvalue, cancellationReason
FROM messages
ORDER BY messageId DESC
LIMIT 5;

-- PASO 15: Resumen final de la tabla
SELECT
    COUNT(*) as total_messages,
    COUNT(CASE WHEN phoneNumber IS NOT NULL AND phoneNumber != '' THEN 1 END) as messages_with_phone,
    COUNT(CASE WHEN phoneNumber IS NULL OR phoneNumber = '' THEN 1 END) as messages_without_phone,
    COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_messages,
    COUNT(CASE WHEN status = 'SENT' THEN 1 END) as sent_messages,
    COUNT(CASE WHEN status = 'FAILED' THEN 1 END) as failed_messages
FROM messages;
