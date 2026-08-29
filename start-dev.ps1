$ErrorActionPreference = 'Stop'

Set-Location $PSScriptRoot

if ($PSVersionTable.PSVersion.Major -ge 5 -and $env:JAVA_HOME -like '*jdk-25*') {
    $env:MAVEN_OPTS = "$env:MAVEN_OPTS --add-opens=java.base/java.lang=ALL-UNNAMED"
}

function Find-Openssl {
    $cmd = Get-Command openssl -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    $candidatos = @(
        "$env:ProgramFiles\Git\usr\bin\openssl.exe",
        "${env:ProgramFiles(x86)}\Git\usr\bin\openssl.exe"
    ) | Where-Object { $_ -and (Test-Path $_) }
    return $candidatos | Select-Object -First 1
}

function Ensure-DockerSecrets {
    $secretsDir = Join-Path $PSScriptRoot 'secrets'
    New-Item -ItemType Directory -Force -Path $secretsDir | Out-Null

    $pgPasswordFile = Join-Path $secretsDir 'postgres_password.txt'
    $jwtPrivateFile = Join-Path $secretsDir 'jwt-private-key.pem'
    $jwtPublicFile = Join-Path $secretsDir 'jwt-public-key.pem'

    if ((Test-Path $pgPasswordFile) -and (Test-Path $jwtPrivateFile) -and (Test-Path $jwtPublicFile)) {
        return
    }

    Write-Host 'Primeira execução com Docker: gerando segredos locais (nunca versionados)...'

    if (-not (Test-Path $pgPasswordFile)) {
        $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
        $bytes = New-Object byte[] 24
        $rng.GetBytes($bytes)
        $senha = [Convert]::ToBase64String($bytes) -replace '[=+/]', ''
        [System.IO.File]::WriteAllText($pgPasswordFile, $senha, [System.Text.Encoding]::ASCII)
        Write-Host '  - secrets/postgres_password.txt criado.'
    }

    if (-not (Test-Path $jwtPrivateFile) -or -not (Test-Path $jwtPublicFile)) {
        $openssl = Find-Openssl
        if (-not $openssl) {
            throw ('OpenSSL não encontrado (normalmente instalado junto com o Git for Windows). ' +
                'Instale o Git for Windows ou gere as chaves manualmente conforme o README (secao "Com Docker") antes de continuar.')
        }
        & $openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out $jwtPrivateFile 2>$null
        & $openssl rsa -pubout -in $jwtPrivateFile -out $jwtPublicFile 2>$null
        Write-Host '  - secrets/jwt-private-key.pem e jwt-public-key.pem criados.'
    }
}

$dockerAvailable = $false
try {
    docker info --format '{{.ServerVersion}}' *> $null
    $dockerAvailable = $LASTEXITCODE -eq 0
}
catch {
    $dockerAvailable = $false
}

if ($dockerAvailable) {
    Write-Host 'Docker disponível: preparando segredos e iniciando PostgreSQL + Quarkus via Compose.'
    Ensure-DockerSecrets
    docker compose up --build
    exit $LASTEXITCODE
}

Write-Host 'Docker indisponível: iniciando Quarkus com banco compatível em memória (perfil dev).'
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
    & $maven.Source quarkus:dev '-Dquarkus.profile=dev' '-Dquarkus.analytics.disabled=true'
    exit $LASTEXITCODE
}
finally {
    Pop-Location
}
