ALTER TABLE visitor ADD COLUMN dormbuild_id INT NULL;
ALTER TABLE visitor ADD COLUMN dormroom_id INT NULL;
ALTER TABLE visitor ADD COLUMN registrar VARCHAR(50) NULL;

CREATE INDEX idx_visitor_build_time ON visitor (dormbuild_id, visit_time);

-- Existing unscoped records remain visible to administrators only. Assign their
-- building and room explicitly before allowing dorm managers to process them.
