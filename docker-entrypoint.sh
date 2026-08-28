#!/bin/sh
set -eu

if [ -z "${DB_PASS:-}" ] && [ -n "${DB_PASS_FILE:-}" ] && [ -f "${DB_PASS_FILE}" ]; then
  export DB_PASS="$(cat "${DB_PASS_FILE}")"
fi

exec java -jar /app/quarkus-app/quarkus-run.jar