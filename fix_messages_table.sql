-- Script completo para verificar y corregir la tabla messages
USE bulksms;

-- 1. Verificar la estructura actual de la tabla messages
SHOW CREATE TABLE messages;
DESCRIBE messages;

-- 2. Verificar si existe la columna phone_number (snake_case)
SELECT COUNT(*) as phone_number_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'messages' AND COLUMN_NAME = 'phone_number';

-- 3. Verificar si existe la columna phoneNumber (camelCase)
SELECT COUNT(*) as phoneNumber_exists
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'messages' AND COLUMN_NAME = 'phoneNumber';

-- 4. Si existe phone_number pero no phoneNumber, renombrar la columna
-- Esto mantendrá los datos existentes
ALTER TABLE messages
CHANGE COLUMN phone_number phoneNumber VARCHAR(20) NULL;

-- 5. Si no existe ninguna de las dos, crear la columna phoneNumber
ALTER TABLE messages
ADD COLUMN IF NOT EXISTS phoneNumber VARCHAR(20) NULL;

-- 6. Verificar si hay datos en la tabla y cuáles son los valores actuales
SELECT COUNT(*) as total_messages FROM messages;

-- 7. Mostrar algunos registros para verificar los datos actuales
SELECT messageId, message, phoneNumber, status, date, user_id, creditvalue, cancellationReason
FROM messages
ORDER BY messageId DESC
LIMIT 10;

-- 8. Verificar qué valores únicos hay en el campo status
SELECT DISTINCT status, COUNT(*) as count FROM messages GROUP BY status;

-- 9. Verificar registros con phoneNumber NULL
SELECT messageId, message, phoneNumber, status FROM messages WHERE phoneNumber IS NULL;

-- 10. Corregir la estructura de la tabla si es necesario

-- 10a. Corregir el campo status para que sea VARCHAR en lugar de ENUM/TINYINT
ALTER TABLE messages
MODIFY COLUMN status VARCHAR(20) NULL;

-- 10b. Asegurar que el campo message puede almacenar texto largo
ALTER TABLE messages
MODIFY COLUMN message TEXT;

-- 10c. Asegurar que cancellationReason puede almacenar texto largo
ALTER TABLE messages
MODIFY COLUMN cancellationReason TEXT NULL;

-- 10d. Asegurar que el campo date es del tipo correcto
ALTER TABLE messages
MODIFY COLUMN date TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP;

-- 11. Actualizar registros con status problemático (si los hay)
-- Convertir valores numéricos o problemáticos a texto válido
UPDATE messages SET status = 'PENDING' WHERE status = '0' OR status = '' OR status IS NULL;
UPDATE messages SET status = 'SENT' WHERE status = '1';
UPDATE messages SET status = 'FAILED' WHERE status = '2';

-- 12. Verificar la estructura final después de los cambios
DESCRIBE messages;

-- 13. Verificar algunos registros después de las correcciones
SELECT messageId, message, phoneNumber, status, date, user_id, creditvalue, cancellationReason
FROM messages
ORDER BY messageId DESC
LIMIT 5;

-- 14. Verificar que no hay más problemas con el status
SELECT DISTINCT status, COUNT(*) as count FROM messages GROUP BY status;

-- 15. Crear índices si no existen (para mejorar rendimiento)
CREATE INDEX IF NOT EXISTS idx_messages_user_id ON messages(user_id);
CREATE INDEX IF NOT EXISTS idx_messages_status ON messages(status);
CREATE INDEX IF NOT EXISTS idx_messages_date ON messages(date);

-- 16. Mostrar el resumen final
SELECT
    COUNT(*) as total_messages,
    COUNT(CASE WHEN phoneNumber IS NOT NULL AND phoneNumber != '' THEN 1 END) as messages_with_phone,
    COUNT(CASE WHEN phoneNumber IS NULL OR phoneNumber = '' THEN 1 END) as messages_without_phone,
    COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_messages,
    COUNT(CASE WHEN status = 'SENT' THEN 1 END) as sent_messages,
    COUNT(CASE WHEN status = 'FAILED' THEN 1 END) as failed_messages
FROM messages;
