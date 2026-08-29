from __future__ import annotations

import json
import sqlite3
from pathlib import Path
from typing import Optional

from app.schemas.ticket import TicketAnalyzeResponse


class AnalysisLogRepository:
    def __init__(self, database_path: Path):
        database_path.parent.mkdir(parents=True, exist_ok=True)
        self.database_path = database_path
        self._initialize()

    def _connect(self) -> sqlite3.Connection:
        connection = sqlite3.connect(str(self.database_path))
        connection.row_factory = sqlite3.Row
        return connection

    def _initialize(self) -> None:
        with self._connect() as connection:
            connection.execute(
                """
                CREATE TABLE IF NOT EXISTS ai_analysis_log (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    request_id TEXT NOT NULL UNIQUE,
                    user_id TEXT,
                    description_hash TEXT NOT NULL,
                    category TEXT,
                    urgency TEXT,
                    confidence REAL,
                    suggested_department TEXT,
                    retrieved_chunks TEXT,
                    prompt_version TEXT,
                    model_name TEXT,
                    latency_ms INTEGER,
                    requires_human_review INTEGER NOT NULL DEFAULT 1,
                    status TEXT NOT NULL,
                    error_type TEXT,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """
            )

    def save_analysis(
        self,
        request_id: str,
        user_id: Optional[str],
        description_hash: str,
        response: TicketAnalyzeResponse,
        latency_ms: int,
        error_type: Optional[str],
    ) -> None:
        chunks = [source.chunk_id for source in response.sources]
        with self._connect() as connection:
            connection.execute(
                """
                INSERT INTO ai_analysis_log (
                    request_id, user_id, description_hash, category, urgency, confidence,
                    suggested_department, retrieved_chunks, prompt_version, model_name,
                    latency_ms, requires_human_review, status, error_type
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                (
                    request_id,
                    user_id,
                    description_hash,
                    response.category.value,
                    response.urgency.value,
                    response.confidence,
                    response.suggested_department.value if response.suggested_department else None,
                    json.dumps(chunks, ensure_ascii=False),
                    response.prompt_version,
                    response.model_name,
                    latency_ms,
                    int(response.requires_human_review),
                    response.status.value,
                    error_type,
                ),
            )
