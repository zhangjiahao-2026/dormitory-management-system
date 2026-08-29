import httpx

from app.schemas.ticket import RepairCategory, Urgency
from app.services.classifier import LocalRuleClassifier
from app.services.llm_service import OpenAiCompatibleLlmClient
from app.services.risk_engine import RiskEngine, RiskResult


class TimeoutClient:
    def __init__(self):
        self.calls = 0

    def post(self, *args, **kwargs):
        self.calls += 1
        raise httpx.ReadTimeout("timeout")


class InvalidThenValidClient:
    def __init__(self):
        self.calls = 0

    def post(self, *args, **kwargs):
        self.calls += 1
        request = httpx.Request("POST", "https://example.com/v1/chat/completions")
        if self.calls == 1:
            return httpx.Response(200, request=request, json={"choices": [{"message": {"content": "not json"}}]})
        content = (
            '{"category":"NETWORK","urgency":"NORMAL","confidence":0.9,'
            '"need_more_information":false,"missing_information":[],'
            '"reason":"网络异常","department":"NETWORK_OPERATIONS"}'
        )
        return httpx.Response(200, request=request, json={"choices": [{"message": {"content": content}}]})


def test_unclassified_gas_risk_is_still_emergency():
    result = LocalRuleClassifier().classify("房间里闻到很重的燃气味")
    assert result.category == RepairCategory.OTHER
    assert result.urgency == Urgency.EMERGENCY
    assert result.confidence >= 0.75


def test_dangerous_generated_action_forces_review():
    classification = LocalRuleClassifier().classify("宿舍插座没有电")
    result = RiskEngine().post_check(
        classification=classification,
        actions=["学生可以自行拆卸插座并恢复供电"],
        has_evidence=True,
        retrieval_score=0.9,
        confidence_threshold=0.75,
        retrieval_threshold=0.18,
        pre_risk=RiskResult(),
    )
    assert result.requires_review is True
    assert "处理建议包含危险操作" in result.reasons


def test_prohibition_of_dangerous_action_is_not_a_dangerous_suggestion():
    classification = LocalRuleClassifier().classify("宿舍插座没有电")
    result = RiskEngine().post_check(
        classification=classification,
        actions=["禁止恢复供电或自行拆卸插座"],
        has_evidence=True,
        retrieval_score=0.9,
        confidence_threshold=0.75,
        retrieval_threshold=0.18,
        pre_risk=RiskResult(),
    )
    assert "处理建议包含危险操作" not in result.reasons


def test_llm_timeout_retries_then_returns_none():
    client = TimeoutClient()
    llm = OpenAiCompatibleLlmClient("key", "https://example.com", "model", client=client)
    assert llm.classify("网络断了") is None
    assert client.calls == 3


def test_invalid_json_is_repaired_once():
    client = InvalidThenValidClient()
    llm = OpenAiCompatibleLlmClient("key", "https://example.com", "model", client=client)
    result = llm.classify("网络断了")
    assert result is not None
    assert result.category == RepairCategory.NETWORK
    assert client.calls == 2


def test_unknown_department_is_rejected_by_schema():
    content = (
        '{"category":"NETWORK","urgency":"NORMAL","confidence":0.9,'
        '"need_more_information":false,"missing_information":[],'
        '"reason":"网络异常","department":"UNKNOWN_TEAM"}'
    )
    assert OpenAiCompatibleLlmClient._parse(content) is None
