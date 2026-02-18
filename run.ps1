$EnvironmentName = if ($args.Count -gt 0) { $args[0] } else { "dev" }
$Mode = if ($args.Count -gt 1) { $args[1] } else { "local" }
$EnvFile = ".env.$EnvironmentName"

if ($Mode -eq "docker") {
    if (-not (Test-Path $EnvFile)) {
        Write-Host "Environment file not found: $EnvFile"
        Write-Host "Copy .env.example to $EnvFile and complete required values."
        exit 1
    }

    Write-Host "Starting PostgreSQL container for backend using '$EnvFile'..."
    docker compose --env-file $EnvFile up -d postgres
    exit $LASTEXITCODE
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

Write-Host "Starting backend locally with profile '$($env:SPRING_PROFILES_ACTIVE)' using '$EnvFile'..."
.\mvnw.cmd spring-boot:run
