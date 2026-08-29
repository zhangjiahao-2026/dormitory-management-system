import json

from app.schemas.ticket import ClassificationResult, RepairCategory, Urgency
from app.services.classifier import LocalRuleClassifier, TicketClassifier
from app.services.llm_service import OpenAiCompatibleLlmClient


def test_emergency_keyword_overrides_urgency():
    result = LocalRuleClassifier().classify("插座突然冒烟并有焦味")
    assert result.category == RepairCategory.ELECTRICAL
    assert result.urgency == Urgency.EMERGENCY
    assert result.confidence >= 0.75


def test_clear_plumbing_problem_is_classified_locally():
    result = TicketClassifier().classify("洗手池下面一直滴水，地面已经湿了")
    assert result.category == RepairCategory.PLUMBING
    assert result.urgency == Urgency.HIGH


def test_unknown_problem_requests_more_information():
    result = LocalRuleClassifier().classify("量子传送门坏了")
    assert result.category == RepairCategory.OTHER
    assert result.need_more_information is True
    assert result.confidence < 0.75


def test_invalid_model_enum_is_rejected():
    payload = {
        "category": "MAGIC",
        "urgency": "NORMAL",
        "confidence": 0.9,
        "need_more_information": False,
        "missing_information": [],
        "reason": "invalid category",
        "department": None,
    }
    assert OpenAiCompatibleLlmClient._parse(json.dumps(payload)) is None


def test_valid_fenced_model_json_is_parsed():
    payload = ClassificationResult(
        category="NETWORK",
        urgency="NORMAL",
        confidence=0.88,
        reason="网络连接异常",
        department="NETWORK_OPERATIONS",
    ).json()
    result = OpenAiCompatibleLlmClient._parse(f"```json\n{payload}\n```")
    assert result is not None
    assert result.category == RepairCategory.NETWORK
