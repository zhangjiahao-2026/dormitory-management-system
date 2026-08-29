from __future__ import annotations

import json
from pathlib import Path
from typing import Optional

import httpx
from pydantic import ValidationError

from app.schemas.ticket import ClassificationResult


class OpenAiCompatibleLlmClient:
    def __init__(
        self,
        api_key: str,
        base_url: str,
        model: str,
        timeout_seconds: float = 8.0,
        client: Optional[httpx.Client] = None,
    ):
        self.api_key = api_key.strip()
        self.base_url = base_url.rstrip("/")
        self.model = model
        self.timeout_seconds = timeout_seconds
        self.client = client or httpx.Client(timeout=timeout_seconds, trust_env=False)
        prompt_path = Path(__file__).parents[1] / "prompts" / "ticket_classification_v1.md"
        self.prompt = prompt_path.read_text(encoding="utf-8")

    @property
    def enabled(self) -> bool:
        return bool(self.api_key)

    def classify(self, description: str) -> Optional[ClassificationResult]:
        if not self.enabled:
            return None
        content = self._request(self.prompt, description, attempts=2)
        parsed = self._parse(content)
        if parsed is not None:
            return parsed
        repair_instruction = "修复下面内容，只输出符合原 JSON Schema 的 JSON：\n" + (content or "")
        return self._parse(self._request(self.prompt, repair_instruction, attempts=1))

    def _request(self, system_prompt: str, user_content: str, attempts: int) -> Optional[str]:
        endpoint = self.base_url + ("/chat/completions" if self.base_url.endswith("/v1") else "/v1/chat/completions")
        body = {
            "model": self.model,
            "temperature": 0.1,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_content},
            ],
        }
        for _ in range(attempts):
            try:
                response = self.client.post(
                    endpoint,
                    headers={"Authorization": f"Bearer {self.api_key}"},
                    json=body,
                    timeout=self.timeout_seconds,
                )
                response.raise_for_status()
                return response.json()["choices"][0]["message"]["content"]
            except (httpx.TimeoutException, httpx.HTTPError, KeyError, IndexError, ValueError):
                continue
        return None

    @staticmethod
    def _parse(content: Optional[str]) -> Optional[ClassificationResult]:
        if not content:
            return None
        text = content.strip()
        if text.startswith("```"):
            first_break = text.find("\n")
            last_fence = text.rfind("```")
            if first_break >= 0 and last_fence > first_break:
                text = text[first_break + 1 : last_fence].strip()
        try:
            return ClassificationResult.parse_obj(json.loads(text))
        except (json.JSONDecodeError, ValidationError, TypeError):
            return None
