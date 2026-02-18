@echo off
setlocal EnableDelayedExpansion

set ENVIRONMENT=%1
if "%ENVIRONMENT%"=="" set ENVIRONMENT=dev
set MODE=%2
if "%MODE%"=="" set MODE=local
set ENV_FILE=.env.%ENVIRONMENT%

if /I "%MODE%"=="docker" (
    if not exist "%ENV_FILE%" (
        echo Environment file not found: %ENV_FILE%
        echo Copy .env.example to %ENV_FILE% and complete required values.
        exit /b 1
    )

    echo Starting PostgreSQL container for backend using '%ENV_FILE%'...
    docker compose --env-file "%ENV_FILE%" up -d postgres
    exit /b %ERRORLEVEL%
)

if not exist "%ENV_FILE%" (
    echo Environment file not found: %ENV_FILE%
    echo Copy .env.example to %ENV_FILE% and complete required values.
    exit /b 1
)

for /f "usebackq tokens=1* delims==" %%A in ("%ENV_FILE%") do (
    set KEY=%%A
    set VALUE=%%B
    if not "!KEY!"=="" (
        if not "!KEY:~0,1!"=="#" (
            set "%%A=%%B"
        )
    )
)

if "%SPRING_PROFILES_ACTIVE%"=="" set SPRING_PROFILES_ACTIVE=%ENVIRONMENT%

echo Starting backend locally with profile '%SPRING_PROFILES_ACTIVE%' using '%ENV_FILE%'...
call mvnw.cmd spring-boot:run
