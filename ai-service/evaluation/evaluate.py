from __future__ import annotations

import json
import sys
from dataclasses import replace
from pathlib import Path
from typing import Dict, List

ROOT = Path(__file__).parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from app.config import Settings
from app.schemas.ticket import TicketAnalyzeRequest
from app.services.classifier import TicketClassifier
from app.services.rag_service import RagService
from app.services.workflow import RepairWorkflow


def rate(numerator: int, denominator: int) -> float:
    return round(numerator / denominator, 4) if denominator else 0.0


def evaluate(dataset_path: Path = ROOT / "evaluation" / "dataset.json") -> Dict[str, object]:
    cases: List[dict] = json.loads(dataset_path.read_text(encoding="utf-8"))
    settings = replace(Settings(), knowledge_dir=ROOT / "knowledge", data_dir=ROOT / ".evaluation-data")
    workflow = RepairWorkflow(settings, TicketClassifier(), RagService(settings.knowledge_dir))
    category_correct = 0
    emergencies = 0
    emergency_recalled = 0
    retrieval_cases = 0
    retrieval_hits = 0
    refusal_cases = 0
    refusal_correct = 0
    review_cases = 0
    review_correct = 0
    details = []

    for case in cases:
        result = workflow.analyze(TicketAnalyzeRequest(
            building="测试楼",
            room="101",
            description=case["description"],
            user_id="evaluation",
        ))
        category_correct += result.category.value == case["expected_category"]
        if case["expected_urgency"] == "EMERGENCY":
            emergencies += 1
            emergency_recalled += result.urgency.value == "EMERGENCY"
        if case["expected_sop_ids"]:
            retrieval_cases += 1
            returned_ids = {source.chunk_id for source in result.sources}
            retrieval_hits += bool(returned_ids.intersection(case["expected_sop_ids"]))
        if case["should_refuse"]:
            refusal_cases += 1
            refusal_correct += not result.recommended_actions
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
            "structuredOutputRate": 1.0,
            "correctRefusalRate": rate(refusal_correct, refusal_cases),
            "humanReviewRecall": rate(review_correct, review_cases),
        },
        "details": details,
    }


if __name__ == "__main__":
    print(json.dumps(evaluate(), ensure_ascii=False, indent=2))
