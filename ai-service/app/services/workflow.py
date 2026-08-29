from __future__ import annotations

import hashlib
import re
import time
import uuid
from datetime import datetime
from typing import List, Optional

from app.config import Settings
from app.schemas.ticket import (
    AnalyzeStatus,
    CATEGORY_NAMES,
    DEPARTMENT_NAMES,
    SopSource,
    TicketAnalyzeRequest,
    TicketAnalyzeResponse,
)
from app.services.classifier import TicketClassifier
from app.services.rag_service import RagService, SearchResult
from app.services.risk_engine import RiskEngine


class RepairWorkflow:
    def __init__(
        self,
        settings: Settings,
        classifier: TicketClassifier,
        rag_service: RagService,
        repository: Optional[object] = None,
    ):
        self.settings = settings
        self.classifier = classifier
        self.rag_service = rag_service
        self.risk_engine = RiskEngine()
        self.repository = repository

    def analyze(self, request: TicketAnalyzeRequest) -> TicketAnalyzeResponse:
        started = time.perf_counter()
        request_id = self._request_id()
        description = self._sanitize(request.description)
        error_type: Optional[str] = None
        try:
            pre_risk = self.risk_engine.pre_check(description)
            classification = self.classifier.classify(description)
            results = self.rag_service.search(description, classification.category.value, top_k=3)
            actions = self._actions(results)
            top_score = results[0].score if results else 0.0
            risk = self.risk_engine.post_check(
                classification=classification,
                actions=actions,
                has_evidence=bool(results),
                retrieval_score=top_score,
                confidence_threshold=self.settings.confidence_threshold,
                retrieval_threshold=self.settings.retrieval_threshold,
                pre_risk=pre_risk,
            )
            if not results or top_score < self.settings.retrieval_threshold:
                actions = []
            response = TicketAnalyzeResponse(
                request_id=request_id,
                category=classification.category,
                category_name=CATEGORY_NAMES[classification.category],
                urgency=classification.urgency,
                confidence=classification.confidence,
                suggested_department=classification.department,
                suggested_department_name=(
                    DEPARTMENT_NAMES[classification.department] if classification.department else None
                ),
                recommended_actions=actions[:4],
                requires_human_review=risk.requires_review,
                review_reasons=risk.reasons,
                sources=[self._source(result) for result in results],
                status=(AnalyzeStatus.NEED_REVIEW if risk.requires_review else AnalyzeStatus.READY_FOR_CONFIRMATION),
                need_more_information=classification.need_more_information,
                missing_information=classification.missing_information,
                model_name=(
                    getattr(self.classifier.llm_client, "model", "local-rules")
                    if getattr(self.classifier.llm_client, "enabled", False)
                    else "local-rules"
                ),
            )
        except Exception:
            error_type = "WORKFLOW_ERROR"
            fallback = self.classifier.local.classify(description)
            response = TicketAnalyzeResponse(
                request_id=request_id,
                category=fallback.category,
                category_name=CATEGORY_NAMES[fallback.category],
                urgency=fallback.urgency,
                confidence=fallback.confidence,
                suggested_department=fallback.department,
                suggested_department_name=DEPARTMENT_NAMES.get(fallback.department),
                recommended_actions=[],
                requires_human_review=True,
                review_reasons=["AI 分析服务异常，请转人工处理"],
                sources=[],
                status=AnalyzeStatus.NEED_REVIEW,
                need_more_information=fallback.need_more_information,
                missing_information=fallback.missing_information,
                model_name="local-fallback",
            )
        self._save_log(request, description, response, started, error_type)
        return response

    @staticmethod
    def _sanitize(description: str) -> str:
        text = re.sub(r"[\x00-\x08\x0b\x0c\x0e-\x1f]", "", description)
        return re.sub(r"\s+", " ", text).strip()

    @staticmethod
    def _actions(results: List[SearchResult]) -> List[str]:
        actions: List[str] = []
        for result in results:
            for sentence in re.split(r"[。；;\n]", result.content):
                sentence = sentence.strip(" ，,。")
                if sentence and sentence not in actions:
                    actions.append(sentence)
        return actions

    @staticmethod
    def _source(result: SearchResult) -> SopSource:
        return SopSource(
            document=result.document,
            section=result.section,
            chunk_id=result.chunk_id,
            score=result.score,
        )

    @staticmethod
    def _request_id() -> str:
        return f"req_{datetime.now().strftime('%Y%m%d')}_{uuid.uuid4().hex[:12]}"

    def _save_log(
        self,
        request: TicketAnalyzeRequest,
        description: str,
        response: TicketAnalyzeResponse,
        started: float,
        error_type: Optional[str],
    ) -> None:
        if self.repository is None:
            return
        self.repository.save_analysis(
            request_id=response.request_id,
            user_id=request.user_id,
            description_hash=hashlib.sha256(description.encode("utf-8")).hexdigest(),
            response=response,
            latency_ms=int((time.perf_counter() - started) * 1000),
            error_type=error_type,
        )
