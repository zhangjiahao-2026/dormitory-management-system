from __future__ import annotations

from dataclasses import dataclass, field
import re
from typing import Iterable, List

from app.schemas.ticket import ClassificationResult
from app.services.classifier import matched_emergency_keywords


INJECTION_MARKERS = ("忽略之前", "忽略以上", "system prompt", "系统提示词", "开发者指令", "越权")
DANGEROUS_ACTIONS = ("自行拆卸", "拆开插座", "修改线路", "恢复供电", "带电操作", "绕过宿管")


@dataclass
class RiskResult:
    requires_review: bool = False
    reasons: List[str] = field(default_factory=list)

    def add(self, reason: str) -> None:
        if reason not in self.reasons:
            self.reasons.append(reason)
        self.requires_review = True


class RiskEngine:
    def pre_check(self, description: str) -> RiskResult:
        result = RiskResult()
        risky = matched_emergency_keywords(description)
        if risky:
            result.add("包含高风险信息：" + "、".join(risky))
        if any(marker in description.lower() for marker in INJECTION_MARKERS):
            result.add("输入疑似包含提示注入或越权指令")
        return result

    def post_check(
        self,
        classification: ClassificationResult,
        actions: Iterable[str],
        has_evidence: bool,
        retrieval_score: float,
        confidence_threshold: float,
        retrieval_threshold: float,
        pre_risk: RiskResult,
    ) -> RiskResult:
        result = RiskResult(pre_risk.requires_review, list(pre_risk.reasons))
        if classification.confidence < confidence_threshold:
            result.add("分类置信度不足")
        if classification.need_more_information:
            result.add("问题描述信息不足")
        if not has_evidence:
            result.add("未检索到可用维修 SOP")
        elif retrieval_score < retrieval_threshold:
            result.add("SOP 检索相关度不足")
        if classification.department is None:
            result.add("无法映射到有效处理部门")
        if any(self._contains_unsafe_instruction(action) for action in actions):
            result.add("处理建议包含危险操作")
        return result

    @staticmethod
    def _contains_unsafe_instruction(action: str) -> bool:
        for danger in DANGEROUS_ACTIONS:
            if danger not in action:
                continue
            prohibited = re.search(rf"(?:禁止|不得|严禁|不要)[^。；;]{{0,40}}{re.escape(danger)}", action)
            if prohibited is None:
                return True
        return False
