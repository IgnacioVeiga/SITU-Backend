$EnvironmentName = if ($args.Count -gt 0) { $args[0] } else { "dev" }
$Mode = if ($args.Count -gt 1) { $args[1] } else { "local" }
$EnvFile = ".env.$EnvironmentName"
$DbContainerName = "situ_postgres"

function Ensure-JavaHome {
    $javaInHome = $false
    if ($env:JAVA_HOME) {
        $javaInHome = (Test-Path (Join-Path $env:JAVA_HOME "bin/java")) -or (Test-Path (Join-Path $env:JAVA_HOME "bin/java.exe"))
    }
    if ($javaInHome) {
        return
    }

    if ($env:JAVA_HOME) {
        Write-Host "Warning: JAVA_HOME is invalid ('$($env:JAVA_HOME)'). Trying auto-detection..."
    }

    $candidates = @(
        "/usr/lib/jvm/java-21-openjdk",
        "/usr/lib/jvm/jdk-21",
        "/usr/lib/jvm/temurin-21-jdk"
    )

    foreach ($candidate in $candidates) {
        if (Test-Path (Join-Path $candidate "bin/java")) {
            $env:JAVA_HOME = $candidate
            return
        }
    }

    $javaCommand = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCommand) {
        $javaPath = $javaCommand.Source
        $detectedHome = Split-Path -Parent (Split-Path -Parent $javaPath)
        if ($detectedHome -and ((Test-Path (Join-Path $detectedHome "bin/java")) -or (Test-Path (Join-Path $detectedHome "bin/java.exe")))) {
            $env:JAVA_HOME = $detectedHome
            return
        }
    }

    Write-Host "Could not find a valid JDK installation. Install Java 21 and retry."
    exit 1
}

function Wait-ForDatabase {
    $MaxAttempts = 30
    $DelaySeconds = 2

    for ($Attempt = 1; $Attempt -le $MaxAttempts; $Attempt++) {
        $Health = docker inspect --format "{{.State.Health.Status}}" $DbContainerName 2>$null
        if ($Health -eq "healthy") {
            Write-Host "Database container '$DbContainerName' is healthy."
            return $true
        }

        Write-Host "Waiting for database health ($Attempt/$MaxAttempts)..."
        Start-Sleep -Seconds $DelaySeconds
    }

    Write-Host "Database container '$DbContainerName' did not become healthy in time."
    return $false
}

if ($Mode -eq "docker" -or $Mode -eq "auto") {
    if (-not (Test-Path $EnvFile)) {
        Write-Host "Environment file not found: $EnvFile"
        Write-Host "Copy .env.example to $EnvFile and complete required values."
        exit 1
    }

    Write-Host "Starting PostgreSQL container for backend using '$EnvFile'..."
    docker compose --env-file $EnvFile up -d postgres
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }

    if ($Mode -eq "docker") {
        exit 0
    }

    if (-not (Wait-ForDatabase)) {
        exit 1
    }
}

if (-not (Test-Path $EnvFile)) {
    Write-Host "Environment file not found: $EnvFile"
    Write-Host "Copy .env.example to $EnvFile and complete required values."
    exit 1
}

Get-Content $EnvFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -and -not $line.StartsWith("#")) {
        $parts = $line.Split("=", 2)
        if ($parts.Count -eq 2) {
            [System.Environment]::SetEnvironmentVariable($parts[0], $parts[1])
        }
    }
}

if (-not $env:SPRING_PROFILES_ACTIVE) {
    $env:SPRING_PROFILES_ACTIVE = $EnvironmentName
}

Ensure-JavaHome

Write-Host "Starting backend locally with profile '$($env:SPRING_PROFILES_ACTIVE)' using '$EnvFile'..."
.\mvnw.cmd clean spring-boot:run
