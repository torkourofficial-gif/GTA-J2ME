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
Write-Host "Modelo encontrado: $($model.FullName)"
Write-Host "Recurso listo:     $destination"

