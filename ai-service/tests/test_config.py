from dataclasses import replace

import pytest

from app.config import Settings


def test_production_requires_internal_token():
    settings = replace(Settings(), environment="production", internal_token="")
    with pytest.raises(RuntimeError, match="AI_SERVICE_TOKEN"):
        settings.validate_security()


def test_development_can_run_without_internal_token():
    settings = replace(Settings(), environment="development", internal_token="")
    settings.validate_security()
