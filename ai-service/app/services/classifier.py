from __future__ import annotations

from dataclasses import dataclass
from typing import Dict, Iterable, Optional, Sequence

from app.schemas.ticket import ClassificationResult, Department, RepairCategory, Urgency


EMERGENCY_KEYWORDS = ("冒烟", "明火", "起火", "漏电", "电火花", "燃气", "受伤", "被困", "焦味")


@dataclass(frozen=True)
class CategoryRule:
    category: RepairCategory
    department: Department
    keywords: Sequence[str]


CATEGORY_RULES = (
    CategoryRule(RepairCategory.ELECTRICAL, Department.WATER_ELECTRIC, ("插座", "电线", "电路", "灯", "停电", "漏电", "电火花")),
    CategoryRule(RepairCategory.PLUMBING, Department.WATER_ELECTRIC, ("漏水", "滴水", "水管", "水龙头", "下水", "积水", "花洒")),
    CategoryRule(RepairCategory.NETWORK, Department.NETWORK_OPERATIONS, ("网络", "断网", "网线", "路由器", "wifi", "校园网")),
    CategoryRule(RepairCategory.DOOR_LOCK, Department.FACILITY_MAINTENANCE, ("门锁", "钥匙", "门把手", "反锁", "打不开")),
    CategoryRule(RepairCategory.AIR_CONDITIONER, Department.FACILITY_MAINTENANCE, ("空调", "制冷", "遥控器", "室内机")),
    CategoryRule(RepairCategory.FURNITURE, Department.FACILITY_MAINTENANCE, ("床板", "衣柜", "桌子", "椅子", "窗帘", "家具")),
    CategoryRule(RepairCategory.PUBLIC_AREA, Department.GENERAL_SERVICES, ("楼道", "走廊", "洗衣房", "公共区域", "楼梯")),
)


def matched_emergency_keywords(description: str) -> list[str]:
    return [keyword for keyword in EMERGENCY_KEYWORDS if keyword in description]


class LocalRuleClassifier:
    def classify(self, description: str) -> ClassificationResult:
        text = description.strip().lower()
        scores: Dict[CategoryRule, int] = {
            rule: sum(1 for keyword in rule.keywords if keyword.lower() in text)
            for rule in CATEGORY_RULES
        }
        best_rule = max(scores, key=scores.get) if scores else None
        best_score = scores.get(best_rule, 0) if best_rule else 0
        emergency = matched_emergency_keywords(text)

        if best_rule is None or best_score == 0:
            return ClassificationResult(
                category=RepairCategory.OTHER,
                urgency=Urgency.LOW,
                confidence=0.35,
                need_more_information=True,
                missing_information=["请补充故障设施和具体现象"],
                reason="描述中没有匹配到已支持的维修类别",
                department=None,
            )

        urgency = self._urgency(text, best_rule.category, bool(emergency))
        confidence = min(0.96, 0.7 + best_score * 0.1 + (0.08 if emergency else 0.0))
        missing = [] if len(text) >= 6 else ["请补充故障发生位置和现象"]
        return ClassificationResult(
            category=best_rule.category,
            urgency=urgency,
            confidence=round(confidence, 2),
            need_more_information=bool(missing),
            missing_information=missing,
            reason=self._reason(best_rule.category, emergency),
            department=best_rule.department,
        )

    @staticmethod
    def _urgency(text: str, category: RepairCategory, emergency: bool) -> Urgency:
        if emergency:
            return Urgency.EMERGENCY
        if any(keyword in text for keyword in ("一直", "大量", "完全", "无法使用", "积水")):
            return Urgency.HIGH
        if category in {RepairCategory.ELECTRICAL, RepairCategory.DOOR_LOCK}:
            return Urgency.HIGH
        return Urgency.NORMAL

    @staticmethod
    def _reason(category: RepairCategory, emergency: Iterable[str]) -> str:
        risky = list(emergency)
        if risky:
            return f"识别到{category.value}类故障及高风险信息：{'、'.join(risky)}"
        return f"描述中的设施和现象与{category.value}类维修规则匹配"


class TicketClassifier:
    def __init__(self, llm_client: Optional[object] = None):
        self.local = LocalRuleClassifier()
        self.llm_client = llm_client

    def classify(self, description: str) -> ClassificationResult:
        local_result = self.local.classify(description)
        if self.llm_client is None or not getattr(self.llm_client, "enabled", False):
            return local_result
        model_result = self.llm_client.classify(description)
        if model_result is None:
            return local_result
        if matched_emergency_keywords(description):
            model_result.urgency = Urgency.EMERGENCY
        return model_result
