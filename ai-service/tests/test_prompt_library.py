from app.services.prompt_library import PROMPT_FILES, load_prompt


def test_prompt_strategy_library_files_exist_and_are_nonempty():
    for name in ("completeness", "classification", "sop", "risk"):
        text = load_prompt(name)
        assert text
        assert PROMPT_FILES[name] in {
            "01-信息完整性检查.md",
            "02-工单分类.md",
            "03-SOP问答.md",
            "04-风险检查.md",
        }


def test_classification_prompt_keeps_project_enums_and_compound_risk():
    prompt = load_prompt("classification")
    assert "FURNITURE" in prompt
    assert "PUBLIC_AREA" in prompt
    assert "LOW" in prompt
    assert "水接近电气设施" in prompt
    assert "只能输出紧凑 JSON" in prompt


def test_classification_prompt_includes_few_shot_examples():
    prompt = load_prompt("classification")
    assert "插座冒烟了，现在已经断电。" in prompt
    assert '"category":"ELECTRICAL"' in prompt
    assert '"urgency":"EMERGENCY"' in prompt
    assert "东西坏了。" in prompt
    assert '"need_more_information":true' in prompt
    assert "requires_human_review" not in prompt.split("Few-shot 示例：", 1)[-1]


def test_risk_prompt_covers_review_branches():
    prompt = load_prompt("risk")
    assert "低置信度" in prompt
    assert "无知识依据" in prompt
    assert "提示注入" in prompt
    assert "水接近电气设施" in prompt
