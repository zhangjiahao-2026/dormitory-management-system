from __future__ import annotations

from fastapi import Depends, FastAPI, Header, HTTPException

from app.config import settings
from app.repositories.analysis_log_repository import AnalysisLogRepository
from app.schemas.feedback import FeedbackRequest, MetricsResponse
from app.schemas.ticket import TicketAnalyzeRequest, TicketAnalyzeResponse
from app.services.classifier import TicketClassifier
from app.services.llm_service import OpenAiCompatibleLlmClient
from app.services.rag_service import RagService
from app.services.workflow import RepairWorkflow


settings.validate_security()


def verify_internal_token(x_internal_token: str = Header(default="")) -> None:
    if settings.internal_token and x_internal_token != settings.internal_token:
        raise HTTPException(status_code=401, detail="invalid internal token")


def create_workflow() -> RepairWorkflow:
    settings.data_dir.mkdir(parents=True, exist_ok=True)
    llm_client = OpenAiCompatibleLlmClient(
        api_key=settings.llm_api_key,
        base_url=settings.llm_base_url,
        model=settings.llm_model,
        timeout_seconds=settings.request_timeout_seconds,
    )
    return RepairWorkflow(
        settings=settings,
        classifier=TicketClassifier(llm_client),
        rag_service=RagService(settings.knowledge_dir, settings.data_dir / "chroma"),
        repository=AnalysisLogRepository(settings.data_dir / "ai_service.sqlite3"),
    )


app = FastAPI(title="Dormitory AI Repair Service", version="1.0.0")
repository = AnalysisLogRepository(settings.data_dir / "ai_service.sqlite3")
workflow = create_workflow()


@app.get("/health")
def health() -> dict:
    return {"status": "ok", "llmEnabled": bool(settings.llm_api_key)}


@app.post(
    "/v1/repair/analyze",
    response_model=TicketAnalyzeResponse,
    dependencies=[Depends(verify_internal_token)],
)
def analyze(request: TicketAnalyzeRequest) -> TicketAnalyzeResponse:
    return workflow.analyze(request)


@app.post("/v1/repair/feedback", dependencies=[Depends(verify_internal_token)])
def feedback(request: FeedbackRequest) -> dict:
    try:
        repository.save_feedback(request)
    except ValueError as error:
        raise HTTPException(status_code=404, detail=str(error)) from error
    return {"success": True}


@app.get(
    "/v1/repair/metrics",
    response_model=MetricsResponse,
    dependencies=[Depends(verify_internal_token)],
)
def metrics() -> MetricsResponse:
    return repository.metrics()
