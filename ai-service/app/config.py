from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path


BASE_DIR = Path(__file__).resolve().parents[1]


@dataclass(frozen=True)
class Settings:
    knowledge_dir: Path = Path(os.getenv("AI_KNOWLEDGE_DIR", BASE_DIR / "knowledge"))
    data_dir: Path = Path(os.getenv("AI_DATA_DIR", BASE_DIR / "data"))
    llm_api_key: str = os.getenv("AI_LLM_API_KEY", "").strip()
    llm_base_url: str = os.getenv("AI_LLM_BASE_URL", "https://api.openai.com").rstrip("/")
    llm_model: str = os.getenv("AI_LLM_MODEL", "gpt-4o-mini")
    internal_token: str = os.getenv("AI_SERVICE_TOKEN", "").strip()
    request_timeout_seconds: float = float(os.getenv("AI_LLM_TIMEOUT_SECONDS", "8"))
    confidence_threshold: float = float(os.getenv("AI_CONFIDENCE_THRESHOLD", "0.75"))
    retrieval_threshold: float = float(os.getenv("AI_RETRIEVAL_THRESHOLD", "0.18"))


settings = Settings()
