from __future__ import annotations

from enum import Enum
from typing import List, Optional

from pydantic import BaseModel, Field, validator


class RepairCategory(str, Enum):
    ELECTRICAL = "ELECTRICAL"
    PLUMBING = "PLUMBING"
    NETWORK = "NETWORK"
    DOOR_LOCK = "DOOR_LOCK"
    AIR_CONDITIONER = "AIR_CONDITIONER"
    FURNITURE = "FURNITURE"
    PUBLIC_AREA = "PUBLIC_AREA"
    OTHER = "OTHER"


class Urgency(str, Enum):
    EMERGENCY = "EMERGENCY"
    HIGH = "HIGH"
    NORMAL = "NORMAL"
    LOW = "LOW"


class Department(str, Enum):
    WATER_ELECTRIC = "WATER_ELECTRIC"
    NETWORK_OPERATIONS = "NETWORK_OPERATIONS"
    FACILITY_MAINTENANCE = "FACILITY_MAINTENANCE"
    CAMPUS_EMERGENCY = "CAMPUS_EMERGENCY"
    GENERAL_SERVICES = "GENERAL_SERVICES"


DEPARTMENT_NAMES = {
    Department.WATER_ELECTRIC: "水电维修组",
    Department.NETWORK_OPERATIONS: "网络运维组",
    Department.FACILITY_MAINTENANCE: "设施维修组",
    Department.CAMPUS_EMERGENCY: "校园应急协调组",
    Department.GENERAL_SERVICES: "综合维修组",
}


CATEGORY_NAMES = {
    RepairCategory.ELECTRICAL: "电路故障",
    RepairCategory.PLUMBING: "给排水故障",
    RepairCategory.NETWORK: "网络故障",
    RepairCategory.DOOR_LOCK: "门锁故障",
    RepairCategory.AIR_CONDITIONER: "空调故障",
    RepairCategory.FURNITURE: "家具设施",
    RepairCategory.PUBLIC_AREA: "公共区域",
    RepairCategory.OTHER: "其他",
}


class ClassificationResult(BaseModel):
    category: RepairCategory
    urgency: Urgency
    confidence: float = Field(ge=0.0, le=1.0)
    need_more_information: bool = False
    missing_information: List[str] = Field(default_factory=list)
    reason: str = Field(min_length=1, max_length=300)
    department: Optional[Department] = None

    @validator("missing_information", each_item=True)
    def validate_missing_information(cls, value: str) -> str:
        value = value.strip()
        if not value:
            raise ValueError("missing information cannot be empty")
        return value[:100]


class AnalyzeStatus(str, Enum):
    NEED_REVIEW = "NEED_REVIEW"
    READY_FOR_CONFIRMATION = "READY_FOR_CONFIRMATION"


class TicketAnalyzeRequest(BaseModel):
    building: str = Field(min_length=1, max_length=30)
    room: str = Field(min_length=1, max_length=30)
    description: str = Field(min_length=4, max_length=2000)
    user_id: Optional[str] = Field(default=None, max_length=64)

    @validator("building", "room", "description")
    def strip_required_text(cls, value: str) -> str:
        value = value.strip()
        if not value:
            raise ValueError("value cannot be blank")
        return value


class SopSource(BaseModel):
    document: str
    section: str
    chunk_id: str
    score: float = Field(ge=0.0, le=1.0)


class TicketAnalyzeResponse(BaseModel):
    request_id: str
    category: RepairCategory
    category_name: str
    urgency: Urgency
    confidence: float = Field(ge=0.0, le=1.0)
    suggested_department: Optional[Department]
    suggested_department_name: Optional[str]
    recommended_actions: List[str]
    requires_human_review: bool
    review_reasons: List[str]
    sources: List[SopSource]
    status: AnalyzeStatus
    need_more_information: bool = False
    missing_information: List[str] = Field(default_factory=list)
    prompt_version: str = "ticket_classification_v1"
    model_name: str = "local-rules"
