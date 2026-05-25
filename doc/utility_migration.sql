USE dormitory;

CREATE TABLE IF NOT EXISTS utility_usage (
    id             INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    dormroom_id    INT          NOT NULL,
    dormbuild_id   INT          NOT NULL,
    electric_usage DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    water_usage    DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    collect_time   VARCHAR(50),
    collect_source VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS utility_alert (
    id            INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    dormroom_id   INT          NOT NULL,
    dormbuild_id  INT          NOT NULL,
    alert_type    VARCHAR(20)  NOT NULL,
    trigger_value DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    limit_value   DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status        VARCHAR(20)  NOT NULL,
    create_time   VARCHAR(50),
    handle_time   VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS utility_config (
    id             INT          NOT NULL PRIMARY KEY,
    electric_limit DECIMAL(10,2) NOT NULL DEFAULT 80.00,
    water_limit    DECIMAL(10,2) NOT NULL DEFAULT 20.00,
    update_time    VARCHAR(50)
);

INSERT IGNORE INTO utility_config (id, electric_limit, water_limit, update_time)
VALUES (1, 80.00, 20.00, '2026-05-24 00:00:00');
