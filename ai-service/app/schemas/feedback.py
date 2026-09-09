from enum import Enum
from typing import Dict, Optional

from pydantic import BaseModel, Field

from app.schemas.ticket import RepairCategory


class FeedbackRating(str, Enum):
    UP = "UP"
    DOWN = "DOWN"


class FeedbackReason(str, Enum):
    CLASSIFICATION_ERROR = "CLASSIFICATION_ERROR"
    URGENCY_ERROR = "URGENCY_ERROR"
    RETRIEVAL_ERROR = "RETRIEVAL_ERROR"
    UNSUPPORTED_ANSWER = "UNSUPPORTED_ANSWER"
    UNCLEAR_EXPLANATION = "UNCLEAR_EXPLANATION"
    OTHER = "OTHER"


class FeedbackRequest(BaseModel):
    request_id: str = Field(min_length=8, max_length=64)
    rating: FeedbackRating
    reason: Optional[FeedbackReason] = None
    expected_category: Optional[RepairCategory] = None
    comment: Optional[str] = Field(default=None, max_length=500)


class MetricsResponse(BaseModel):
    total_requests: int
    success_rate: float
    average_latency_ms: float
    structured_output_rate: float
    human_review_rate: float
    positive_feedback_rate: Optional[float]
    top_errors: Dict[str, int]
