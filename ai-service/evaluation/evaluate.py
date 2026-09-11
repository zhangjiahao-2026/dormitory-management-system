"""离线评测脚本：用 dataset.json 跑完整报修分析流程并汇总指标。

不启动 Web 服务，也不调用大模型，走本地规则分类 + SOP 检索 + 风险检查。
运行：python evaluation/evaluate.py
"""
from __future__ import annotations

import json
import sys
from dataclasses import replace
from pathlib import Path
from typing import Dict, List

# evaluation/ 的上一级是 ai-service/，加进路径后才能 import app.*
ROOT = Path(__file__).parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from app.config import Settings
from app.schemas.ticket import TicketAnalyzeRequest
from app.schemas.ticket import TicketAnalyzeResponse
from app.services.classifier import TicketClassifier
from app.services.rag_service import RagService
from app.services.workflow import RepairWorkflow


def rate(numerator: int, denominator: int) -> float:
    """计算比例；分母为 0 时返回 0，避免除零。"""
    return round(numerator / denominator, 4) if denominator else 0.0


def evaluate(dataset_path: Path = ROOT / "evaluation" / "dataset.json") -> Dict[str, object]:
    """逐条跑评测集，返回总体指标和每条用例的实际输出。"""
    cases: List[dict] = json.loads(dataset_path.read_text(encoding="utf-8"))
    # 评测数据写到独立目录，避免污染正式运行时的 data/
    settings = replace(Settings(), knowledge_dir=ROOT / "knowledge", data_dir=ROOT / ".evaluation-data")
    # TicketClassifier 不传 LLM client，因此全程使用本地规则
    workflow = RepairWorkflow(settings, TicketClassifier(), RagService(settings.knowledge_dir))

    category_correct = 0  # 类别预测正确数
    emergencies = 0  # 预期为紧急的用例数
    emergency_recalled = 0  # 其中被正确标成 EMERGENCY 的数量
    retrieval_cases = 0  # 写了预期 SOP 的用例数
    retrieval_hits = 0  # Top-3 命中至少一个预期 SOP 的数量
    refusal_cases = 0  # 应当拒答（不给处理建议）的用例数
    refusal_correct = 0  # 实际没有给出建议的数量
    review_cases = 0  # 应当转人工的用例数
    review_correct = 0  # 实际标记了人工复核的数量
    valid_local_structures = 0  # 本地规则结果通过响应 Schema 校验的数量
    details = []

    for case in cases:
        # 楼栋/房间只是占位，分类依据是 description
        result = workflow.analyze(TicketAnalyzeRequest(
            building="测试楼",
            room="101",
            description=case["description"],
            user_id="evaluation",
        ))
        TicketAnalyzeResponse.parse_obj(result.dict())
        valid_local_structures += 1
        # 1) 分类准确率：全部用例都参与
        category_correct += result.category.value == case["expected_category"]
        # 2) 紧急召回：只统计预期为 EMERGENCY 的，漏标比多标更危险
        if case["expected_urgency"] == "EMERGENCY":
            emergencies += 1
            emergency_recalled += result.urgency.value == "EMERGENCY"
        # 3) SOP Top-3 命中：没有 expected_sop_ids 的模糊/恶意用例不参与
        if case["expected_sop_ids"]:
            retrieval_cases += 1
            returned_ids = {source.chunk_id for source in result.sources}
            retrieval_hits += bool(returned_ids.intersection(case["expected_sop_ids"]))
        # 4) 正确拒答：不该给建议时 recommended_actions 必须为空
        if case["should_refuse"]:
            refusal_cases += 1
            refusal_correct += not result.recommended_actions
        # 5) 人工复核召回：该转人工的必须标 requires_human_review
        if case["should_review"]:
            review_cases += 1
            review_correct += result.requires_human_review
        details.append({
            "id": case["id"],
            "category": result.category.value,
            "urgency": result.urgency.value,
            "review": result.requires_human_review,
            "sources": [source.chunk_id for source in result.sources],
        })

    return {
        "totalCases": len(cases),
        "metrics": {
            "classificationAccuracy": rate(category_correct, len(cases)),
            "emergencyRecall": rate(emergency_recalled, emergencies),
            "top3SopHitRate": rate(retrieval_hits, retrieval_cases),
            "localRuleSchemaValidityRate": rate(valid_local_structures, len(cases)),
            "correctRefusalRate": rate(refusal_correct, refusal_cases),
            "humanReviewRecall": rate(review_correct, review_cases),
        },
        "details": details,
    }


if __name__ == "__main__":
    print(json.dumps(evaluate(), ensure_ascii=False, indent=2))
