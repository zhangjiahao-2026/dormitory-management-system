#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
AI_SERVICE_DIR="$PROJECT_ROOT/ai-service"

cd "$AI_SERVICE_DIR"

if [[ ! -x ".venv/bin/uvicorn" ]]; then
  python3 -m venv .venv
  .venv/bin/pip install -r requirements.txt
fi

if [[ -f "$PROJECT_ROOT/.env.local" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "$PROJECT_ROOT/.env.local"
  set +a
fi

exec .venv/bin/uvicorn app.main:app --host 127.0.0.1 --port 8000
