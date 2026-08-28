$ErrorActionPreference = 'Stop'

Set-Location $PSScriptRoot

$dockerAvailable = $false
try {
    docker info --format '{{.ServerVersion}}' *> $null
    $dockerAvailable = $LASTEXITCODE -eq 0
}
catch {
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
    $wrapper = Join-Path $PSScriptRoot 'mvnw.cmd'
    if (Test-Path $wrapper) {
        $maven = Get-Command $wrapper -ErrorAction SilentlyContinue
    }
}
if ($null -eq $maven -and $env:MAVEN_HOME) {
    $mavenHomeCommand = Join-Path $env:MAVEN_HOME 'bin\mvn.cmd'
    if (Test-Path $mavenHomeCommand) {
        $maven = Get-Command $mavenHomeCommand -ErrorAction SilentlyContinue
    }
}
if ($null -eq $maven) {
    $knownMaven = @(
        'C:\workspace\maven\maven-3.9.16\bin\mvn.cmd',
        'C:\Program Files\Apache\maven\bin\mvn.cmd'
    ) | Where-Object { Test-Path $_ } | Select-Object -First 1
    if ($knownMaven) {
        $maven = Get-Command $knownMaven -ErrorAction SilentlyContinue
    }
}
if ($null -eq $maven) {
    throw 'Maven ou Maven Wrapper não encontrado. Instale Maven ou adicione mvnw.cmd ao projeto.'
}

Push-Location backend
try {
    & $maven.Source quarkus:dev '-Dquarkus.profile=dev'
    exit $LASTEXITCODE
}
finally {
    Pop-Location
}