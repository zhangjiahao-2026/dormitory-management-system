from __future__ import annotations

from functools import lru_cache
from pathlib import Path


PROMPTS_DIR = Path(__file__).resolve().parents[1] / "prompts"

PROMPT_FILES = {
    "completeness": "01-信息完整性检查.md",
    "classification": "02-工单分类.md",
    "sop": "03-SOP问答.md",
    "risk": "04-风险检查.md",
}


@lru_cache
def load_prompt(name: str) -> str:
    filename = PROMPT_FILES[name]
    return (PROMPTS_DIR / filename).read_text(encoding="utf-8").strip()
