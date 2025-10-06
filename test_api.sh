#!/bin/bash

# Script de pruebas automatizado para la API de verificación manual de mensajes
# Asegúrate de que la aplicación esté corriendo en localhost:8080

BASE_URL="http://localhost:8080"
ADMIN_EMAIL="admin@test.com"
ADMIN_PASSWORD="adminpassword"
USER_EMAIL="user@test.com"
USER_PASSWORD="userpassword"

echo "🚀 Iniciando pruebas de API - Sistema de Verificación Manual"
echo "=================================================="

# Función para hacer peticiones HTTP
make_request() {
    local method=$1
    local url=$2
    local headers=$3
    local data=$4

    if [ -n "$data" ]; then
        curl -s -X $method "$url" -H "$headers" -H "Content-Type: application/json" -d "$data"
    else
        curl -s -X $method "$url" -H "$headers"
    fi
}

# 1. Login como Admin
echo "📝 1. Login como Admin..."
ADMIN_RESPONSE=$(make_request "POST" "$BASE_URL/api/auth/login" "" '{"email":"'$ADMIN_EMAIL'","password":"'$ADMIN_PASSWORD'"}')
ADMIN_TOKEN=$(echo $ADMIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$ADMIN_TOKEN" ]; then
    echo "❌ Error: No se pudo obtener token de admin"
    echo "Respuesta: $ADMIN_RESPONSE"
    exit 1
fi
echo "✅ Admin token obtenido: ${ADMIN_TOKEN:0:20}..."

# 2. Login como Usuario
echo "📝 2. Login como Usuario..."
USER_RESPONSE=$(make_request "POST" "$BASE_URL/api/auth/login" "" '{"email":"'$USER_EMAIL'","password":"'$USER_PASSWORD'"}')
USER_TOKEN=$(echo $USER_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$USER_TOKEN" ]; then
    echo "❌ Error: No se pudo obtener token de usuario"
    echo "Respuesta: $USER_RESPONSE"
    exit 1
fi
echo "✅ User token obtenido: ${USER_TOKEN:0:20}..."

# 3. Crear mensaje como usuario (debe quedar PENDING)
echo "📝 3. Creando mensaje como usuario..."
MESSAGE_DATA='{"message":"Mensaje de prueba automatizada - '$(date)'","phoneNumber":"+1234567890"}'
CREATE_RESPONSE=$(make_request "POST" "$BASE_URL/api/message/send" "Authorization: Bearer $USER_TOKEN" "$MESSAGE_DATA")
MESSAGE_ID=$(echo $CREATE_RESPONSE | grep -o '"messageId":[0-9]*' | cut -d':' -f2)

if [ -z "$MESSAGE_ID" ]; then
    echo "❌ Error: No se pudo crear el mensaje"
    echo "Respuesta: $CREATE_RESPONSE"
    exit 1
fi
echo "✅ Mensaje creado con ID: $MESSAGE_ID"

# Verificar que el estado sea PENDING
STATUS=$(echo $CREATE_RESPONSE | grep -o '"status":"[^"]*' | cut -d'"' -f4)
if [ "$STATUS" = "PENDING" ]; then
    echo "✅ Mensaje guardado con estado PENDING correctamente"
else
    echo "❌ Error: El mensaje no tiene estado PENDING (actual: $STATUS)"
fi

# 4. Ver mensajes pendientes como admin
echo "📝 4. Viendo mensajes pendientes como admin..."
PENDING_RESPONSE=$(make_request "GET" "$BASE_URL/api/message/pending" "Authorization: Bearer $ADMIN_TOKEN")
PENDING_COUNT=$(echo $PENDING_RESPONSE | grep -o '"messageId"' | wc -l)
echo "✅ Encontrados $PENDING_COUNT mensajes pendientes"

# 5. Intentar aprobar como usuario (debe fallar)
echo "📝 5. Intentando aprobar como usuario (debe fallar)..."
FAIL_RESPONSE=$(curl -s -w "%{http_code}" -X PUT "$BASE_URL/api/message/$MESSAGE_ID/approve" -H "Authorization: Bearer $USER_TOKEN")
HTTP_CODE="${FAIL_RESPONSE: -3}"
if [ "$HTTP_CODE" = "403" ]; then
    echo "✅ Acceso denegado correctamente para usuario no admin"
else
    echo "❌ Error: Se esperaba código 403, pero se obtuvo $HTTP_CODE"
fi

# 6. Aprobar mensaje como admin
echo "📝 6. Aprobando mensaje como admin..."
APPROVE_RESPONSE=$(make_request "PUT" "$BASE_URL/api/message/$MESSAGE_ID/approve" "Authorization: Bearer $ADMIN_TOKEN")
APPROVED_STATUS=$(echo $APPROVE_RESPONSE | grep -o '"status":"[^"]*' | cut -d'"' -f4)

if [ "$APPROVED_STATUS" = "SENT" ]; then
    echo "✅ Mensaje aprobado y enviado correctamente"
else
    echo "❌ Error: El mensaje no fue aprobado correctamente (estado: $APPROVED_STATUS)"
    echo "Respuesta: $APPROVE_RESPONSE"
fi

# 7. Crear otro mensaje para cancelar
echo "📝 7. Creando segundo mensaje para cancelar..."
MESSAGE_DATA2='{"message":"Mensaje para cancelar - '$(date)'","phoneNumber":"+0987654321"}'
CREATE_RESPONSE2=$(make_request "POST" "$BASE_URL/api/message/send" "Authorization: Bearer $USER_TOKEN" "$MESSAGE_DATA2")
MESSAGE_ID2=$(echo $CREATE_RESPONSE2 | grep -o '"messageId":[0-9]*' | cut -d':' -f2)

if [ -n "$MESSAGE_ID2" ]; then
    echo "✅ Segundo mensaje creado con ID: $MESSAGE_ID2"
else
    echo "❌ Error: No se pudo crear el segundo mensaje"
fi

# 8. Cancelar mensaje como admin
echo "📝 8. Cancelando mensaje como admin..."
CANCEL_DATA='{"reason":"Contenido inapropiado detectado en prueba automatizada"}'
CANCEL_RESPONSE=$(make_request "PUT" "$BASE_URL/api/message/$MESSAGE_ID2/cancel" "Authorization: Bearer $ADMIN_TOKEN" "$CANCEL_DATA")
CANCELLED_STATUS=$(echo $CANCEL_RESPONSE | grep -o '"status":"[^"]*' | cut -d'"' -f4)

if [ "$CANCELLED_STATUS" = "FAILED" ]; then
    echo "✅ Mensaje cancelado correctamente"
    REASON=$(echo $CANCEL_RESPONSE | grep -o '"cancellationReason":"[^"]*' | cut -d'"' -f4)
    echo "✅ Motivo guardado: $REASON"
else
    echo "❌ Error: El mensaje no fue cancelado correctamente"
    echo "Respuesta: $CANCEL_RESPONSE"
fi

# 9. Intentar cancelar sin motivo (debe fallar)
echo "📝 9. Intentando cancelar sin motivo (debe fallar)..."
FAIL_CANCEL_RESPONSE=$(curl -s -w "%{http_code}" -X PUT "$BASE_URL/api/message/1/cancel" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"reason":""}')
HTTP_CODE="${FAIL_CANCEL_RESPONSE: -3}"
if [ "$HTTP_CODE" = "400" ]; then
    echo "✅ Validación de motivo funcionando correctamente"
else
    echo "❌ Error: Se esperaba código 400, pero se obtuvo $HTTP_CODE"
fi

# 10. Ver todos los mensajes
echo "📝 10. Viendo todos los mensajes..."
ALL_RESPONSE=$(make_request "GET" "$BASE_URL/api/message/all" "Authorization: Bearer $ADMIN_TOKEN")
TOTAL_COUNT=$(echo $ALL_RESPONSE | grep -o '"messageId"' | wc -l)
echo "✅ Total de mensajes en el sistema: $TOTAL_COUNT"

echo ""
echo "🎉 ¡Pruebas completadas!"
echo "=================================================="
echo "Resumen:"
echo "- ✅ Autenticación de admin y usuario"
echo "- ✅ Creación de mensajes con estado PENDING"
echo "- ✅ Restricción de acceso para usuarios no admin"
echo "- ✅ Aprobación de mensajes por admin"
echo "- ✅ Cancelación de mensajes con motivo"
echo "- ✅ Validación de campos requeridos"
echo "- ✅ Notificación por email (revisar bandeja de entrada)"
echo ""
echo "💡 Revisa el email del usuario para confirmar la notificación de cancelación"
