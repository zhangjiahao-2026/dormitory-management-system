from __future__ import annotations

import json
import sqlite3
from pathlib import Path
from typing import Optional

from app.schemas.ticket import TicketAnalyzeResponse
from app.schemas.feedback import FeedbackRequest, MetricsResponse


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
            connection.execute(
                """
                CREATE TABLE IF NOT EXISTS ai_feedback (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    request_id TEXT NOT NULL UNIQUE,
                    rating TEXT NOT NULL,
                    reason TEXT,
                    expected_category TEXT,
                    comment TEXT,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (request_id) REFERENCES ai_analysis_log(request_id)
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

    def save_feedback(self, feedback: FeedbackRequest) -> None:
        with self._connect() as connection:
            exists = connection.execute(
                "SELECT 1 FROM ai_analysis_log WHERE request_id = ?", (feedback.request_id,)
            ).fetchone()
            if exists is None:
                raise ValueError("analysis request does not exist")
            connection.execute(
                """
                INSERT INTO ai_feedback (request_id, rating, reason, expected_category, comment)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(request_id) DO UPDATE SET
                    rating = excluded.rating,
                    reason = excluded.reason,
                    expected_category = excluded.expected_category,
                    comment = excluded.comment,
                    updated_at = CURRENT_TIMESTAMP
                """,
                (
                    feedback.request_id,
                    feedback.rating.value,
                    feedback.reason.value if feedback.reason else None,
                    feedback.expected_category.value if feedback.expected_category else None,
                    feedback.comment.strip() if feedback.comment else None,
                ),
            )

    def metrics(self) -> MetricsResponse:
        with self._connect() as connection:
            totals = connection.execute(
                """
                SELECT COUNT(*) total,
                       SUM(CASE WHEN error_type IS NULL THEN 1 ELSE 0 END) successful,
                       AVG(latency_ms) average_latency,
                       SUM(CASE WHEN status IS NOT NULL THEN 1 ELSE 0 END) structured,
                       SUM(requires_human_review) reviewed
                FROM ai_analysis_log
                """
            ).fetchone()
            feedback = connection.execute(
                "SELECT COUNT(*) total, SUM(CASE WHEN rating = 'UP' THEN 1 ELSE 0 END) positive FROM ai_feedback"
            ).fetchone()
            errors = connection.execute(
                """
                SELECT error_type, COUNT(*) count
                FROM ai_analysis_log
                WHERE error_type IS NOT NULL
                GROUP BY error_type
                ORDER BY count DESC, error_type
                LIMIT 5
                """
            ).fetchall()
        total = int(totals["total"] or 0)
        feedback_total = int(feedback["total"] or 0)
        return MetricsResponse(
            total_requests=total,
            success_rate=self._rate(totals["successful"], total),
            average_latency_ms=round(float(totals["average_latency"] or 0.0), 2),
            structured_output_rate=self._rate(totals["structured"], total),
            human_review_rate=self._rate(totals["reviewed"], total),
            positive_feedback_rate=(self._rate(feedback["positive"], feedback_total) if feedback_total else None),
            top_errors={row["error_type"]: int(row["count"]) for row in errors},
        )

    @staticmethod
    def _rate(numerator: Optional[int], denominator: int) -> float:
        return round(float(numerator or 0) / denominator, 4) if denominator else 0.0
