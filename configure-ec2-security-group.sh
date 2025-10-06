#!/bin/bash

# Script para configurar Security Group de EC2 para BulkSMS API con HTTPS
# Requiere AWS CLI configurado

echo "=== Configurando Security Group para BulkSMS API con HTTPS ==="

# Obtener el ID de la instancia actual
INSTANCE_ID=$(curl -s http://169.254.169.254/latest/meta-data/instance-id)
echo "ID de instancia: $INSTANCE_ID"

# Obtener el Security Group asociado
SECURITY_GROUP_ID=$(aws ec2 describe-instances --instance-ids $INSTANCE_ID --query 'Reservations[0].Instances[0].SecurityGroups[0].GroupId' --output text)
echo "Security Group ID: $SECURITY_GROUP_ID"

# Función para agregar regla si no existe
add_rule_if_not_exists() {
    local port=$1
    local protocol=$2
    local description=$3

    # Verificar si la regla ya existe
    existing_rule=$(aws ec2 describe-security-groups --group-ids $SECURITY_GROUP_ID --query "SecurityGroups[0].IpPermissions[?FromPort==\`$port\` && ToPort==\`$port\` && IpProtocol==\`$protocol\`]" --output text)

    if [ -z "$existing_rule" ]; then
        echo "Agregando regla para puerto $port ($description)..."
        aws ec2 authorize-security-group-ingress \
            --group-id $SECURITY_GROUP_ID \
            --protocol $protocol \
            --port $port \
            --cidr 0.0.0.0/0
        echo "✅ Regla agregada para puerto $port"
    else
        echo "✅ Regla para puerto $port ya existe"
    fi
}

# Agregar reglas necesarias
echo ""
echo "Configurando reglas de firewall..."

add_rule_if_not_exists 22 tcp "SSH"
add_rule_if_not_exists 8080 tcp "HTTP - BulkSMS API"
add_rule_if_not_exists 8443 tcp "HTTPS - BulkSMS API"

# Preguntar si quiere acceso externo a MySQL
echo ""
read -p "¿Deseas permitir acceso externo a MySQL (puerto 3306)? [y/N]: " allow_mysql
if [[ $allow_mysql =~ ^[Yy]$ ]]; then
    add_rule_if_not_exists 3306 tcp "MySQL"
    echo "⚠️  ADVERTENCIA: MySQL está accesible desde internet. Usa credenciales seguras."
fi

echo ""
echo "=== Configuración de Security Group completada ==="
echo ""
echo "✅ Puertos abiertos:"
echo "   - 22 (SSH)"
echo "   - 8080 (HTTP)"
echo "   - 8443 (HTTPS)"
if [[ $allow_mysql =~ ^[Yy]$ ]]; then
    echo "   - 3306 (MySQL)"
fi

echo ""
echo "🔍 Para verificar las reglas:"
echo "aws ec2 describe-security-groups --group-ids $SECURITY_GROUP_ID --query 'SecurityGroups[0].IpPermissions'"
