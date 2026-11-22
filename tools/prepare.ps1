$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$resourceDir = Join-Path $projectRoot 'res'
New-Item -ItemType Directory -Path $resourceDir -Force | Out-Null

$model = Get-ChildItem -LiteralPath $projectRoot -Filter 'CS.m3g' -File -Recurse |
    Where-Object { $_.FullName -notlike "$resourceDir\*" } |
    Select-Object -First 1

if ($null -eq $model) {
    throw "No se encontró CS.m3g dentro de $projectRoot"
}

$destination = Join-Path $resourceDir 'CS.m3g'
Copy-Item -LiteralPath $model.FullName -Destination $destination -Force

# J2ME-Loader and several handset runtimes only accept M3G 1.0 headers.
# This asset uses only the 1.0 object types, but Blender stamped the file 1.1.
# Downgrade the staged copy's minor version and recompute section 0's Adler-32.
$bytes = [System.IO.File]::ReadAllBytes($destination)
$magic = [byte[]](0xAB, 0x4A, 0x53, 0x52, 0x31, 0x38, 0x34, 0xBB, 0x0D, 0x0A, 0x1A, 0x0A)
if ($bytes.Length -lt 60) {
    throw "CS.m3g es demasiado pequeño para ser un archivo M3G válido: $($bytes.Length) bytes"
}
for ($i = 0; $i -lt $magic.Length; $i++) {
    if ($bytes[$i] -ne $magic[$i]) {
        throw 'CS.m3g no tiene la firma binaria JSR-184 esperada'
    }
}

$headerDataOffset = 26
if ($bytes[$headerDataOffset] -ne 1 -or ($bytes[$headerDataOffset + 1] -ne 0 -and $bytes[$headerDataOffset + 1] -ne 1)) {
    throw "Versión M3G no compatible para el parche automático: $($bytes[$headerDataOffset]).$($bytes[$headerDataOffset + 1])"
}

$sectionStart = 12
$headerSectionLength = [BitConverter]::ToUInt32($bytes, $sectionStart + 1)
$checksumOffset = $sectionStart + [int]$headerSectionLength - 4
if ($headerSectionLength -lt 13 -or $checksumOffset -ge $bytes.Length) {
    throw 'La sección de encabezado de CS.m3g tiene una longitud inválida'
}

$bytes[$headerDataOffset + 1] = 0
$a = 1
$b = 0
for ($i = $sectionStart; $i -lt $checksumOffset; $i++) {
    $a = ($a + [int]$bytes[$i]) % 65521
    $b = ($b + $a) % 65521
}
$checksum = [uint32](($b -shl 16) -bor $a)
[BitConverter]::GetBytes($checksum).CopyTo($bytes, $checksumOffset)
[System.IO.File]::WriteAllBytes($destination, $bytes)

Write-Host "Modelo encontrado: $($model.FullName)"
Write-Host "Recurso listo:     $destination (M3G 1.0 compatible)"


