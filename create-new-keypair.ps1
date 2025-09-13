# Script para crear un nuevo Key Pair en AWS
# Ejecuta este script en PowerShell con AWS CLI configurado

$keyName = "bulksms-api-new-key"
$region = "us-east-1"  # Cambia por tu región

Write-Host "Creando nuevo Key Pair: $keyName" -ForegroundColor Green

# Crear el Key Pair y guardar la clave privada
aws ec2 create-key-pair --key-name $keyName --region $region --query 'KeyMaterial' --output text > "$env:USERPROFILE\Desktop\$keyName.pem"

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Key Pair creado exitosamente: $keyName.pem" -ForegroundColor Green
    Write-Host "📁 Archivo guardado en: $env:USERPROFILE\Desktop\$keyName.pem" -ForegroundColor Yellow

    # Establecer permisos correctos
    icacls "$env:USERPROFILE\Desktop\$keyName.pem" /inheritance:r /grant:r "$env:USERNAME`:R"

    Write-Host "🔒 Permisos establecidos correctamente" -ForegroundColor Green
    Write-Host ""
    Write-Host "⚠️  IMPORTANTE: Debes asociar esta nueva clave a tu instancia EC2" -ForegroundColor Red
    Write-Host "Puedes hacerlo desde AWS Console o detener/iniciar la instancia con la nueva clave" -ForegroundColor Yellow
} else {
    Write-Host "❌ Error al crear el Key Pair" -ForegroundColor Red
    Write-Host "Asegúrate de tener AWS CLI configurado correctamente" -ForegroundColor Yellow
}
