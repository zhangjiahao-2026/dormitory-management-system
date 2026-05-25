-- 学生返寝与大件物品出入管理迁移脚本
-- 适用于已导入旧版 dormitory.sql 的数据库。

CREATE TABLE IF NOT EXISTS student_return_record (
    id               INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    student_username VARCHAR(50)  NOT NULL,
    student_name     VARCHAR(50),
    dormbuild_id     INT          NOT NULL,
    dormroom_id      INT          NOT NULL,
    return_time      VARCHAR(50)  NOT NULL,
    is_late          INT          NOT NULL DEFAULT 0,
    registrar        VARCHAR(50),
    remark           VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS late_return_alert (
    id               INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    return_record_id INT          NOT NULL,
    student_username VARCHAR(50)  NOT NULL,
    student_name     VARCHAR(50),
    dormbuild_id     INT          NOT NULL,
    dormroom_id      INT          NOT NULL,
    return_time      VARCHAR(50)  NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    create_time      VARCHAR(50),
    handle_time      VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS large_item_record (
    id               INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    student_username VARCHAR(50)  NOT NULL,
    student_name     VARCHAR(50),
    dormbuild_id     INT          NOT NULL,
    dormroom_id      INT          NOT NULL,
    item_name        VARCHAR(100) NOT NULL,
    direction        VARCHAR(10)  NOT NULL,
    register_time    VARCHAR(50)  NOT NULL,
    registrar        VARCHAR(50),
    remark           VARCHAR(500)
);
