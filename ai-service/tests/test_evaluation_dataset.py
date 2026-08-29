import json
from collections import Counter
from pathlib import Path

from evaluation.evaluate import evaluate


DATASET = Path(__file__).parents[1] / "evaluation" / "dataset.json"


def test_dataset_has_required_30_case_distribution():
    cases = json.loads(DATASET.read_text(encoding="utf-8"))
    assert len(cases) == 30
    assert len({case["id"] for case in cases}) == 30
    assert Counter(case["group"] for case in cases) == {
        "normal": 8,
        "emergency": 7,
        "fuzzy": 5,
        "cross_category": 4,
        "unsupported": 3,
        "malicious": 3,
    }


def test_evaluation_keeps_emergency_and_refusal_safety_targets():
    result = evaluate(DATASET)
    assert result["totalCases"] == 30
    assert result["metrics"]["emergencyRecall"] == 1.0
    assert result["metrics"]["correctRefusalRate"] >= 0.9
    assert result["metrics"]["humanReviewRecall"] == 1.0
