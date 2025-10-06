-- ==============================================
-- SCRIPT PARA LIMPIAR COLUMNAS DUPLICADAS
-- Ejecuta en phpMyAdmin paso a paso
-- ==============================================

-- PASO 1: Verificar columnas actuales
USE bulksms;
DESCRIBE messages;

-- PASO 2: Ver qué columnas de teléfono existen
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'bulksms'
  AND TABLE_NAME = 'messages'
  AND (COLUMN_NAME = 'phone_number' OR COLUMN_NAME = 'phoneNumber');

-- PASO 3: Si tienes datos en phone_number, copiarlos a phoneNumber
UPDATE messages
SET phoneNumber = phone_number
WHERE phone_number IS NOT NULL
  AND (phoneNumber IS NULL OR phoneNumber = '');

-- PASO 4: Eliminar la columna phone_number duplicada
ALTER TABLE messages DROP COLUMN phone_number;

-- PASO 5: Verificar que solo queda phoneNumber
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'bulksms'
  AND TABLE_NAME = 'messages'
  AND (COLUMN_NAME = 'phone_number' OR COLUMN_NAME = 'phoneNumber');

-- PASO 6: Verificar datos
SELECT messageId, message, phoneNumber, status
FROM messages
ORDER BY messageId DESC
LIMIT 5;
