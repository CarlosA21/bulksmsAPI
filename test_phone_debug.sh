#!/bin/bash

echo "=== TESTING MULTIPLE PHONE NUMBERS ==="
echo "Testing with the exact format your frontend is sending..."

# Test con el formato exacto que está enviando tu frontend
curl -X POST http://localhost:8080/api/message/test-dto \
  -H "Content-Type: application/json" \
  -d '{
    "message": "tstt",
    "phone_number": ["+18294584366", "+18099956009"],
    "date": "2025-09-20T23:48:47.361Z",
    "creditvalue": 2,
    "status": "pending"
  }'

echo -e "\n\n=== END TESTING ==="
