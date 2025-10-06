#!/bin/bash

# Script para probar el envío de mensajes y diagnosticar el problema del phoneNumber

echo "=== TESTING MESSAGE DTO ==="
echo "1. Testing DTO reception..."

# Test 1: Probar el endpoint de prueba
curl -X POST http://localhost:8080/api/message/test-dto \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Test message",
    "phoneNumber": "+1234567890"
  }'

echo -e "\n\n2. Testing actual send endpoint..."

# Test 2: Probar el endpoint real de envío (necesitarás autenticación)
# curl -X POST http://localhost:8080/api/message/send \
#   -H "Content-Type: application/json" \
#   -H "Authorization: Bearer YOUR_JWT_TOKEN" \
#   -d '{
#     "message": "Test message",
#     "phoneNumber": "+1234567890"
#   }'

echo -e "\n\n3. Getting all messages to check phoneNumber..."

# Test 3: Verificar mensajes existentes
curl -X GET http://localhost:8080/api/message/all

echo -e "\n\n=== END TESTING ==="
