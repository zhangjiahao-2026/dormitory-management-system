ALTER TABLE repair ADD COLUMN ai_request_id VARCHAR(64) NULL;
ALTER TABLE repair ADD COLUMN ai_assisted TINYINT NOT NULL DEFAULT 0;
ALTER TABLE repair ADD COLUMN ai_category VARCHAR(32) NULL;
ALTER TABLE repair ADD COLUMN ai_urgency VARCHAR(32) NULL;
ALTER TABLE repair ADD COLUMN ai_department VARCHAR(32) NULL;
ALTER TABLE repair ADD COLUMN human_confirmed_by VARCHAR(50) NULL;
ALTER TABLE repair ADD COLUMN human_confirmed_at VARCHAR(50) NULL;
CREATE UNIQUE INDEX uk_repair_ai_request_id ON repair (ai_request_id);

CREATE TABLE ai_repair_request (
    request_id          VARCHAR(64)  NOT NULL PRIMARY KEY,
    applicant_username  VARCHAR(50)  NOT NULL,
    applicant_name      VARCHAR(50)  NOT NULL,
    dormbuild_id        INT          NOT NULL,
    dormroom_id         INT          NOT NULL,
    title               VARCHAR(200) NOT NULL,
    content             TEXT         NOT NULL,
    category            VARCHAR(32),
    urgency             VARCHAR(32),
    confidence          DOUBLE,
    department          VARCHAR(32),
    department_name     VARCHAR(50),
    recommended_actions TEXT,
    review_reasons      TEXT,
    sources             TEXT,
    ai_status           VARCHAR(32),
    status              VARCHAR(32)  NOT NULL,
    created_at          VARCHAR(50)  NOT NULL,
    reviewed_by         VARCHAR(50),
    reviewed_at         VARCHAR(50),
    operator_comment    VARCHAR(500),
    repair_id           INT,
    INDEX idx_ai_repair_pending (status, dormbuild_id, created_at),
    INDEX idx_ai_repair_applicant (applicant_username, created_at)
);
