#!/usr/bin/env bash
set -euo pipefail

ENVIRONMENT="${1:-dev}"
MODE="${2:-local}"
ENV_FILE=".env.${ENVIRONMENT}"

if [[ "${MODE}" == "docker" ]]; then
  if [[ ! -f "${ENV_FILE}" ]]; then
    echo "Environment file not found: ${ENV_FILE}"
    echo "Copy .env.example to ${ENV_FILE} and complete the required values."
    exit 1
  fi

  echo "Starting PostgreSQL container for backend using '${ENV_FILE}'..."
  docker compose --env-file "${ENV_FILE}" up -d postgres
  exit $?
fi

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "Environment file not found: ${ENV_FILE}"
  echo "Copy .env.example to ${ENV_FILE} and complete the required values."
  exit 1
fi

set -a
source "${ENV_FILE}"
set +a

export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-${ENVIRONMENT}}"

echo "Starting backend locally with profile '${SPRING_PROFILES_ACTIVE}' using '${ENV_FILE}'..."
./mvnw spring-boot:run
