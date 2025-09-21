#!/bin/bash

echo "Generating SSL certificate for BulkSMS API..."

# Create keystore directory if it doesn't exist
mkdir -p src/main/resources

# Generate self-signed certificate
keytool -genkeypair -alias bulksmsapi -keyalg RSA -keysize 2048 -storetype PKCS12 \
    -keystore src/main/resources/keystore.p12 -validity 365 \
    -dname "CN=localhost, OU=BulkSMS, O=BulkSMS API, L=City, S=State, C=US" \
    -storepass changeit

echo ""
echo "SSL Certificate generated successfully!"
echo "File: src/main/resources/keystore.p12"
echo "Alias: bulksmsapi"
echo "Password: changeit"
echo ""
echo "To enable HTTPS, set SSL_ENABLED=true in your environment variables"
echo "For production, replace this self-signed certificate with a valid one from a CA"
