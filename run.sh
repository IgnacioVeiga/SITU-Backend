#!/usr/bin/env bash
set -euo pipefail

ENVIRONMENT="${1:-dev}"
MODE="${2:-local}"
ENV_FILE=".env.${ENVIRONMENT}"
DB_CONTAINER_NAME="situ_postgres"

ensure_java_home() {
  if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/java" ]]; then
    return 0
  fi

  if [[ -n "${JAVA_HOME:-}" ]]; then
    echo "Warning: JAVA_HOME is invalid ('${JAVA_HOME}'). Trying auto-detection..."
  fi

  for candidate in \
    "/usr/lib/jvm/java-21-openjdk" \
    "/usr/lib/jvm/jdk-21" \
    "/usr/lib/jvm/temurin-21-jdk"; do
    if [[ -x "${candidate}/bin/java" ]]; then
      export JAVA_HOME="${candidate}"
      return 0
    fi
  done

  if command -v java >/dev/null 2>&1; then
    local java_path
    java_path="$(readlink -f "$(command -v java)" 2>/dev/null || command -v java)"
    local detected_home
    detected_home="$(dirname "$(dirname "${java_path}")")"
    if [[ -x "${detected_home}/bin/java" ]]; then
      export JAVA_HOME="${detected_home}"
      return 0
    fi
  fi

  echo "Could not find a valid JDK installation. Install Java 21 and retry."
  return 1
}

wait_for_db() {
  local attempts=30
  local sleep_seconds=2

  for ((i=1; i<=attempts; i++)); do
    local health
    health="$(docker inspect --format '{{.State.Health.Status}}' "${DB_CONTAINER_NAME}" 2>/dev/null || true)"
    if [[ "${health}" == "healthy" ]]; then
      echo "Database container '${DB_CONTAINER_NAME}' is healthy."
      return 0
    fi
    echo "Waiting for database health (${i}/${attempts})..."
    sleep "${sleep_seconds}"
  done

  echo "Database container '${DB_CONTAINER_NAME}' did not become healthy in time."
  return 1
}

if [[ "${MODE}" == "docker" || "${MODE}" == "auto" ]]; then
  if [[ ! -f "${ENV_FILE}" ]]; then
    echo "Environment file not found: ${ENV_FILE}"
    echo "Copy .env.example to ${ENV_FILE} and complete the required values."
    exit 1
  fi

  echo "Starting PostgreSQL container for backend using '${ENV_FILE}'..."
  docker compose --env-file "${ENV_FILE}" up -d postgres
  if [[ "${MODE}" == "docker" ]]; then
    exit $?
  fi

  wait_for_db
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

ensure_java_home

echo "Starting backend locally with profile '${SPRING_PROFILES_ACTIVE}' using '${ENV_FILE}'..."
bash ./mvnw clean spring-boot:run
