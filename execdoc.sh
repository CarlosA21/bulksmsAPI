#!/bin/bash

# Iniciar aplicación
echo "8. Iniciando aplicación..."
sudo docker run -d \
  --name bulksms-api \
  --network bulksms-network \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://bulksms-mysql:3306/bulksmsdb \
  -e SPRING_DATASOURCE_USERNAME=bulksmsuser \
  -e SPRING_DATASOURCE_PASSWORD=bulksmspass \
  -e paypal.mode=sandbox \
  -e paypal.client.id=AXmZKG8wynj4Y0Icu_f68I1YHzT6olqUiF_NXQL9z2YhApGGWLuZI6KhFdXgZnsa1Jsfy1vU-cOzliSi \
  -e paypal.client.secret=AXmZKG8wynj4Y0Icu_f68I1YHzT6olqUiF_NXQL9z2YhApGGWLuZI6KhFdXgZnsa1Jsfy1vU-cOzliSi \
  --restart unless-stopped \
  carlosa21/bulksms-api:latest


  
# Verificar
echo "9. Verificando..."
sleep 20
sudo docker ps
sudo docker logs -f bulksms-api
