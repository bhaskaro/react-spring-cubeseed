#!/usr/bin/env bash
# deploy.sh — rebuild and start db/api/web with health checks
set -euo pipefail

# --------------------------- config / helpers -------------------------------
COMPOSE_BIN=""
if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
  COMPOSE_BIN="docker compose"
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE_BIN="docker-compose"
else
  echo "ERROR: Docker Compose is required (either 'docker compose' or 'docker-compose')." >&2
  exit 1
fi

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

PRUNE=0
if [[ "${1:-}" == "--prune" ]]; then PRUNE=1; fi

echo "==> Project: $ROOT"

# Resolve service names from compose file (db, api, web)
SERVICES="$($COMPOSE_BIN config --services)"
has_svc() { printf '%s\n' "$SERVICES" | grep -qx "$1"; }
DB_SVC="db";  has_svc "$DB_SVC"  || { echo "ERROR: No 'db' service in compose.";  exit 1; }
API_SVC="api"; has_svc "$API_SVC" || { echo "ERROR: No 'api' service in compose."; exit 1; }
WEB_SVC="web"; has_svc "$WEB_SVC" || { echo "ERROR: No 'web' service in compose."; exit 1; }

# --------------------------- .env validation --------------------------------
if [[ ! -f .env ]]; then
  echo "ERROR: Missing .env (must contain JWT_SECRET=<base64>)." >&2
  echo "Generate one:  echo \"JWT_SECRET=\$(openssl rand -base64 64 | tr -d '\\r\\n')\" > .env" >&2
  exit 1
fi

ENV_LINE="$(grep -m1 '^JWT_SECRET=' .env | tr -d '\r')"

# Read the file, strip CRs (Windows), keep content in memory
if [ -z "$ENV_LINE" ]; then
  echo "ERROR: .env must contain a line starting with JWT_SECRET=" >&2
  exit 1
fi

case "$ENV_LINE" in
  JWT_SECRET=*) ;;
  *)
    echo "ERROR: .env must start with 'JWT_SECRET='." >&2
    exit 1
    ;;
esac

JWT_SECRET_VALUE="${ENV_LINE#JWT_SECRET=}"
if [[ -z "$JWT_SECRET_VALUE" ]]; then
  echo "ERROR: JWT_SECRET is empty." >&2
  exit 1
fi

# Must be valid Base64 and >= 32 bytes when decoded
if ! decoded_len="$(printf '%s' "$JWT_SECRET_VALUE" | base64 -d 2>/dev/null | wc -c)"; then
  echo "ERROR: JWT_SECRET is not valid Base64." >&2
  exit 1
fi
if [[ "$decoded_len" -lt 32 ]]; then
  echo "ERROR: JWT_SECRET too short (<32 bytes). Use: openssl rand -base64 64" >&2
  exit 1
fi

# --------------------------- cleanup / build --------------------------------
echo "==> Stopping existing containers (keeping DB volume)..."
$COMPOSE_BIN down --remove-orphans || true

if [[ $PRUNE -eq 1 ]]; then
  echo "==> Pruning dangling images & builder cache..."
  docker image prune -f || true
  docker builder prune -f || true
fi

echo "==> Building images (no cache): $API_SVC, $WEB_SVC"
$COMPOSE_BIN build --no-cache "$API_SVC" "$WEB_SVC"

# --------------------------- start & wait -----------------------------------
echo "==> Starting $DB_SVC..."
$COMPOSE_BIN up -d "$DB_SVC"

echo "==> Waiting for $DB_SVC to be healthy..."
# Wait up to 60s for "(healthy)"
for i in $(seq 1 60); do
  if $COMPOSE_BIN ps | grep -E "${DB_SVC}.*\(healthy\)" >/dev/null 2>&1; then
    echo "   $DB_SVC is healthy."
    break
  fi
  sleep 1
  if [[ $i -eq 60 ]]; then
    echo "ERROR: $DB_SVC not healthy in time." >&2
    $COMPOSE_BIN logs --tail=120 "$DB_SVC" || true
    exit 1
  fi
done

echo "==> Starting $API_SVC and $WEB_SVC..."
$COMPOSE_BIN up -d "$API_SVC" "$WEB_SVC"

# Determine API host:port from compose (defaults if not mapped)
API_PORT="8080"
# Try to detect published port
if $COMPOSE_BIN ps | awk -v svc="$API_SVC" '$0 ~ svc {print $0}' | grep -Eo '0\.0\.0\.0:[0-9]+->8080/tcp' >/dev/null 2>&1; then
  API_PORT="$($COMPOSE_BIN ps | awk -v svc="$API_SVC" '$0 ~ svc {print $0}' | grep -Eo '0\.0\.0\.0:[0-9]+->8080/tcp' | head -n1 | sed 's/.*:\([0-9]\+\).*/\1/')"
fi

echo "==> Waiting for API health on :$API_PORT ..."
# Try /actuator/health for up to 60s
for i in $(seq 1 60); do
  if curl -fsS "http://localhost:${API_PORT}/actuator/health" >/dev/null 2>&1; then
    echo "   API is up."
    break
  fi
  sleep 1
  if [[ $i -eq 60 ]]; then
    echo "ERROR: API failed to start. Recent logs:" >&2
    $COMPOSE_BIN logs --tail=200 "$API_SVC" >&2 || true
    exit 1
  fi
done

# --------------------------- quick checks -----------------------------------
echo "==> Quick checks"
echo "• Host → API /api/hello:"
if ! curl -fsS "http://localhost:${API_PORT}/api/hello" ; then
  echo "(non-fatal) /api/hello check failed — see API logs below."
  $COMPOSE_BIN logs --tail=120 "$API_SVC" || true
fi
echo

echo "• Web (nginx) serves bundle index.html:"
if ! curl -fsSI http://localhost/ | head -n1 ; then
  echo "(non-fatal) web root check failed — see WEB logs below."
  $COMPOSE_BIN logs --tail=120 "$WEB_SVC" || true
fi

echo "• Web → API (inside network):"
if ! $COMPOSE_BIN exec -T "$WEB_SVC" sh -lc 'wget -qO- http://'"$API_SVC"':8080/api/hello || echo FAIL' ; then
  echo "(non-fatal) web→api check failed — verify Nginx proxy_pass to http://api:8080"
fi
echo

echo "==> DONE."
echo "Open the app at:  http://<server-ip>/"
echo "API direct:       http://<server-ip>:${API_PORT}/api/hello"
echo
echo "Tips:"
echo "  ./deploy.sh           # rebuild api+web, restart stack"
echo "  ./deploy.sh --prune   # also prune dangling images/cache"
