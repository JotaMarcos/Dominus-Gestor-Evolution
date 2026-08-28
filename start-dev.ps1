$ErrorActionPreference = 'Stop'

Set-Location $PSScriptRoot

$dockerAvailable = $false
try {
    docker info --format '{{.ServerVersion}}' *> $null
    $dockerAvailable = $LASTEXITCODE -eq 0
} catch {
    $dockerAvailable = $false
}

if ($dockerAvailable) {
    Write-Host 'Docker disponível: iniciando PostgreSQL e Quarkus via Compose.'
    docker compose up --build
    exit $LASTEXITCODE
}

Write-Host 'Docker indisponível: iniciando Quarkus com PostgreSQL compatível em memória (perfil dev).'
$maven = Get-Command mvn -ErrorAction SilentlyContinue
if ($null -eq $maven) {
    $maven = Get-Command '.\mvnw.cmd' -ErrorAction SilentlyContinue
}
if ($null -eq $maven) {
    throw 'Maven ou Maven Wrapper não encontrado. Instale Maven ou adicione mvnw.cmd ao projeto.'
}

Push-Location backend
try {
    & $maven.Source quarkus:dev '-Dquarkus.profile=dev'
    exit $LASTEXITCODE
} finally {
    Pop-Location
}