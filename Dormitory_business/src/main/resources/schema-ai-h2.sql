-- The local base schema predates database-generated repair IDs. MyBatis omits
-- AUTO ids on insert, so align H2 with the production repair table behavior.
ALTER TABLE repair ALTER COLUMN id INT AUTO_INCREMENT;

ALTER TABLE repair ADD COLUMN IF NOT EXISTS ai_request_id VARCHAR(64);
ALTER TABLE repair ADD COLUMN IF NOT EXISTS ai_assisted INT NOT NULL DEFAULT 0;
ALTER TABLE repair ADD COLUMN IF NOT EXISTS ai_category VARCHAR(32);
ALTER TABLE repair ADD COLUMN IF NOT EXISTS ai_urgency VARCHAR(32);
ALTER TABLE repair ADD COLUMN IF NOT EXISTS ai_department VARCHAR(32);
ALTER TABLE repair ADD COLUMN IF NOT EXISTS human_confirmed_by VARCHAR(50);
ALTER TABLE repair ADD COLUMN IF NOT EXISTS human_confirmed_at VARCHAR(50);
CREATE UNIQUE INDEX IF NOT EXISTS uk_repair_ai_request_id ON repair (ai_request_id);

CREATE TABLE IF NOT EXISTS ai_repair_request (
    request_id          VARCHAR(64)  NOT NULL PRIMARY KEY,
    applicant_username  VARCHAR(50)  NOT NULL,
    applicant_name      VARCHAR(50)  NOT NULL,
    dormbuild_id        INT          NOT NULL,
    dormroom_id         INT          NOT NULL,
    title               VARCHAR(200) NOT NULL,
    content             CLOB         NOT NULL,
    category            VARCHAR(32),
    urgency             VARCHAR(32),
    confidence          DOUBLE,
    department          VARCHAR(32),
    department_name     VARCHAR(50),
    recommended_actions CLOB,
    review_reasons      CLOB,
    sources             CLOB,
    ai_status           VARCHAR(32),
    status              VARCHAR(32)  NOT NULL,
    created_at          VARCHAR(50)  NOT NULL,
    reviewed_by         VARCHAR(50),
    reviewed_at         VARCHAR(50),
    operator_comment    VARCHAR(500),
    repair_id           INT
);
CREATE INDEX IF NOT EXISTS idx_ai_repair_pending ON ai_repair_request (status, dormbuild_id, created_at);
CREATE INDEX IF NOT EXISTS idx_ai_repair_applicant ON ai_repair_request (applicant_username, created_at);
