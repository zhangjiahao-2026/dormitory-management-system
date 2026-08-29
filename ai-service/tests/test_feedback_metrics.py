from pathlib import Path

from app.repositories.analysis_log_repository import AnalysisLogRepository
from app.schemas.feedback import FeedbackRating, FeedbackReason, FeedbackRequest
from app.schemas.ticket import AnalyzeStatus, RepairCategory, TicketAnalyzeResponse, Urgency


def response(request_id: str) -> TicketAnalyzeResponse:
    return TicketAnalyzeResponse(
        request_id=request_id,
        category=RepairCategory.ELECTRICAL,
        category_name="电路故障",
        urgency=Urgency.EMERGENCY,
        confidence=0.96,
        suggested_department="WATER_ELECTRIC",
        suggested_department_name="水电维修组",
        recommended_actions=["保持断电"],
        requires_human_review=True,
        review_reasons=["涉及用电安全"],
        sources=[],
        status=AnalyzeStatus.NEED_REVIEW,
    )


def test_feedback_is_upserted_and_metrics_are_real(tmp_path: Path):
    repository = AnalysisLogRepository(tmp_path / "metrics.sqlite3")
    repository.save_analysis("req_feedback_001", "stu001", "hash", response("req_feedback_001"), 120, None)
    repository.save_feedback(FeedbackRequest(request_id="req_feedback_001", rating=FeedbackRating.UP))
    repository.save_feedback(FeedbackRequest(
        request_id="req_feedback_001",
        rating=FeedbackRating.DOWN,
        reason=FeedbackReason.CLASSIFICATION_ERROR,
        expected_category=RepairCategory.PLUMBING,
        comment="类别需要修正",
    ))

    metrics = repository.metrics()
    assert metrics.total_requests == 1
    assert metrics.success_rate == 1.0
    assert metrics.human_review_rate == 1.0
    assert metrics.positive_feedback_rate == 0.0


def test_feedback_rejects_unknown_request(tmp_path: Path):
    repository = AnalysisLogRepository(tmp_path / "metrics.sqlite3")
    try:
        repository.save_feedback(FeedbackRequest(request_id="req_missing_001", rating=FeedbackRating.UP))
        assert False, "expected unknown request to fail"
    except ValueError as error:
        assert "does not exist" in str(error)
