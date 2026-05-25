-- 宿舍管理系统建库建表脚本
CREATE DATABASE IF NOT EXISTS dormitory DEFAULT CHARSET utf8mb4;
USE dormitory;

-- 1. 管理员表
CREATE TABLE admin (
    username    VARCHAR(50)  NOT NULL PRIMARY KEY,
    password    VARCHAR(255) NOT NULL,
    name        VARCHAR(50),
    gender      VARCHAR(10),
    age         INT          NOT NULL DEFAULT 0,
    phone_num   VARCHAR(20),
    email       VARCHAR(100),
    avatar      VARCHAR(255)
);

-- 2. 学生表
CREATE TABLE student (
    username    VARCHAR(50)  NOT NULL PRIMARY KEY,
    password    VARCHAR(255) NOT NULL,
    name        VARCHAR(50),
    age         INT          NOT NULL DEFAULT 0,
    gender      VARCHAR(10),
    phone_num   VARCHAR(20),
    email       VARCHAR(100),
    avatar      VARCHAR(255)
);

-- 3. 楼宇表
CREATE TABLE dorm_build (
    id               INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    dormbuild_id     INT          NOT NULL,
    dormbuild_name   VARCHAR(100),
    dormbuild_detail VARCHAR(500)
);

-- 4. 房间表
CREATE TABLE dorm_room (
    dormroom_id      INT          NOT NULL PRIMARY KEY,
    dormbuild_id     INT          NOT NULL,
    floor_num        INT          NOT NULL DEFAULT 0,
    max_capacity     INT          NOT NULL DEFAULT 4,
    current_capacity INT          NOT NULL DEFAULT 0,
    first_bed        VARCHAR(50)  DEFAULT NULL,
    second_bed       VARCHAR(50)  DEFAULT NULL,
    third_bed        VARCHAR(50)  DEFAULT NULL,
    fourth_bed       VARCHAR(50)  DEFAULT NULL
);

-- 5. 宿管表
CREATE TABLE dorm_manager (
    username     VARCHAR(50)  NOT NULL PRIMARY KEY,
    password     VARCHAR(255) NOT NULL,
    dormbuild_id INT          NOT NULL DEFAULT 0,
    name         VARCHAR(50),
    gender       VARCHAR(10),
    age          INT          NOT NULL DEFAULT 0,
    phone_num    VARCHAR(20),
    email        VARCHAR(100),
    avatar       VARCHAR(255)
);

-- 6. 调宿申请表
CREATE TABLE adjust_room (
    id             INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL,
    name           VARCHAR(50),
    currentroom_id INT          NOT NULL DEFAULT 0,
    currentbed_id  INT          NOT NULL DEFAULT 0,
    towardsroom_id INT          NOT NULL DEFAULT 0,
    towardsbed_id  INT          NOT NULL DEFAULT 0,
    state          VARCHAR(20),
    apply_time     VARCHAR(50),
    finish_time    VARCHAR(50)
);

-- 7. 公告表
CREATE TABLE notice (
    id           INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    content      TEXT,
    author       VARCHAR(50),
    release_time VARCHAR(50)
);

-- 8. 报修表
CREATE TABLE repair (
    id               INT          NOT NULL PRIMARY KEY,
    repairer         VARCHAR(50)  NOT NULL,
    dormbuild_id     INT          NOT NULL DEFAULT 0,
    dormroom_id      INT          NOT NULL DEFAULT 0,
    title            VARCHAR(200) NOT NULL,
    content          TEXT,
    state            VARCHAR(20),
    order_buildtime  VARCHAR(50),
    order_finishtime VARCHAR(50)
);

-- 9. 访客表
CREATE TABLE visitor (
    id          INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    gender      VARCHAR(10),
    phone_num   VARCHAR(20),
    origin_city VARCHAR(100),
    visit_time  VARCHAR(50),
    content     VARCHAR(500)
);

-- 10. 宿舍水电采集记录表
CREATE TABLE utility_usage (
    id             INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    dormroom_id    INT          NOT NULL,
    dormbuild_id   INT          NOT NULL,
    electric_usage DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    water_usage    DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    collect_time   VARCHAR(50),
    collect_source VARCHAR(20)
);

-- 11. 宿舍用电告警表
CREATE TABLE utility_alert (
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

-- 12. 水电全局配置表
CREATE TABLE utility_config (
    id             INT          NOT NULL PRIMARY KEY,
    electric_limit DECIMAL(10,2) NOT NULL DEFAULT 80.00,
    water_limit    DECIMAL(10,2) NOT NULL DEFAULT 20.00,
    update_time    VARCHAR(50)
);

-- 13. 学生返寝登记表
CREATE TABLE student_return_record (
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

-- 14. 学生晚归告警表
CREATE TABLE late_return_alert (
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

-- 15. 大件物品出入登记表
CREATE TABLE large_item_record (
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

-- ========== 初始数据 ==========

-- 管理员账号（密码: 123456，MD5+盐值加密）
INSERT INTO admin (username, password, name, gender, age, phone_num, email)
VALUES ('admin', 'e8c7659e7d15fa797bebf6e5ec9af446', '系统管理员', '男', 30, '13800000000', 'admin@dorm.com');

-- 楼宇
INSERT INTO dorm_build (dormbuild_id, dormbuild_name, dormbuild_detail) VALUES
(1, '1号楼', '男生宿舍楼，靠近东门和食堂'),
(2, '2号楼', '女生宿舍楼，靠近图书馆和运动场');

-- 宿管账号（密码: 123456，MD5+盐值加密）
INSERT INTO dorm_manager (username, password, dormbuild_id, name, gender, age, phone_num, email) VALUES
('manager1', 'e8c7659e7d15fa797bebf6e5ec9af446', 1, '张宿管', '男', 45, '13900000000', 'mgr1@dorm.com'),
('manager2', 'e8c7659e7d15fa797bebf6e5ec9af446', 2, '刘宿管', '女', 42, '13900000001', 'mgr2@dorm.com');

-- 学生账号（密码: 123456，MD5+盐值加密）
INSERT INTO student (username, password, name, age, gender, phone_num, email) VALUES
('stu001', 'e8c7659e7d15fa797bebf6e5ec9af446', '李同学', 20, '男', '13700000000', 'stu001@dorm.com'),
('stu002', 'e8c7659e7d15fa797bebf6e5ec9af446', '王同学', 19, '女', '13700000001', 'stu002@dorm.com'),
('stu003', 'e8c7659e7d15fa797bebf6e5ec9af446', '赵同学', 21, '男', '13700000002', 'stu003@dorm.com'),
('stu004', 'e8c7659e7d15fa797bebf6e5ec9af446', '陈同学', 20, '男', '13700000003', 'stu004@dorm.com'),
('stu005', 'e8c7659e7d15fa797bebf6e5ec9af446', '周同学', 19, '男', '13700000004', 'stu005@dorm.com'),
('stu006', 'e8c7659e7d15fa797bebf6e5ec9af446', '吴同学', 20, '男', '13700000005', 'stu006@dorm.com'),
('stu007', 'e8c7659e7d15fa797bebf6e5ec9af446', '孙同学', 22, '男', '13700000006', 'stu007@dorm.com'),
('stu008', 'e8c7659e7d15fa797bebf6e5ec9af446', '徐同学', 21, '男', '13700000007', 'stu008@dorm.com'),
('stu009', 'e8c7659e7d15fa797bebf6e5ec9af446', '胡同学', 20, '男', '13700000008', 'stu009@dorm.com'),
('stu010', 'e8c7659e7d15fa797bebf6e5ec9af446', '林同学', 19, '男', '13700000009', 'stu010@dorm.com'),
('stu011', 'e8c7659e7d15fa797bebf6e5ec9af446', '郭同学', 20, '男', '13700000010', 'stu011@dorm.com'),
('stu012', 'e8c7659e7d15fa797bebf6e5ec9af446', '何同学', 21, '男', '13700000011', 'stu012@dorm.com'),
('stu013', 'e8c7659e7d15fa797bebf6e5ec9af446', '高同学', 20, '男', '13700000012', 'stu013@dorm.com'),
('stu014', 'e8c7659e7d15fa797bebf6e5ec9af446', '罗同学', 22, '男', '13700000013', 'stu014@dorm.com'),
('stu015', 'e8c7659e7d15fa797bebf6e5ec9af446', '梁同学', 19, '男', '13700000014', 'stu015@dorm.com'),
('stu016', 'e8c7659e7d15fa797bebf6e5ec9af446', '郑同学', 20, '女', '13700000015', 'stu016@dorm.com'),
('stu017', 'e8c7659e7d15fa797bebf6e5ec9af446', '冯同学', 21, '女', '13700000016', 'stu017@dorm.com'),
('stu018', 'e8c7659e7d15fa797bebf6e5ec9af446', '许同学', 20, '女', '13700000017', 'stu018@dorm.com'),
('stu019', 'e8c7659e7d15fa797bebf6e5ec9af446', '蒋同学', 19, '女', '13700000018', 'stu019@dorm.com'),
('stu020', 'e8c7659e7d15fa797bebf6e5ec9af446', '沈同学', 21, '女', '13700000019', 'stu020@dorm.com');

-- 房间（床位占用与 current_capacity 保持一致）
INSERT INTO dorm_room (dormroom_id, dormbuild_id, floor_num, max_capacity, current_capacity, first_bed, second_bed, third_bed, fourth_bed) VALUES
(101, 1, 1, 4, 4, 'stu001', 'stu003', 'stu004', 'stu005'),
(102, 1, 1, 4, 4, 'stu006', 'stu007', 'stu008', 'stu009'),
(103, 1, 1, 4, 2, 'stu010', 'stu011', NULL, NULL),
(104, 1, 1, 4, 0, NULL, NULL, NULL, NULL),
(105, 1, 1, 4, 0, NULL, NULL, NULL, NULL),
(201, 1, 2, 4, 3, 'stu012', 'stu013', 'stu014', NULL),
(202, 1, 2, 4, 1, 'stu015', NULL, NULL, NULL),
(203, 1, 2, 4, 0, NULL, NULL, NULL, NULL),
(204, 1, 2, 4, 0, NULL, NULL, NULL, NULL),
(205, 1, 2, 4, 0, NULL, NULL, NULL, NULL),
(301, 1, 3, 4, 0, NULL, NULL, NULL, NULL),
(302, 1, 3, 4, 0, NULL, NULL, NULL, NULL),
(303, 1, 3, 4, 0, NULL, NULL, NULL, NULL),
(304, 1, 3, 4, 0, NULL, NULL, NULL, NULL),
(305, 1, 3, 4, 0, NULL, NULL, NULL, NULL),
(10101, 2, 1, 4, 3, 'stu002', 'stu016', 'stu017', NULL),
(10102, 2, 1, 4, 3, 'stu018', 'stu019', 'stu020', NULL),
(10103, 2, 1, 4, 0, NULL, NULL, NULL, NULL),
(10104, 2, 1, 4, 0, NULL, NULL, NULL, NULL),
(10105, 2, 1, 4, 0, NULL, NULL, NULL, NULL);

-- 公告
INSERT INTO notice (title, content, author, release_time) VALUES
('五一假期宿舍安全提醒', '离开宿舍前请关闭门窗、电源和水龙头，贵重物品请妥善保管。', '系统管理员', '2026-05-01 09:00:00'),
('本周公共区域卫生检查安排', '宿管老师将于周三下午检查楼道、洗衣房和公共活动室卫生。', '张宿管', '2026-05-12 10:30:00'),
('夏季用电安全提示', '请勿在宿舍内使用大功率违规电器，发现线路异常请及时报修。', '系统管理员', '2026-05-18 14:20:00'),
('毕业季访客登记提醒', '毕业季访客较多，请同学们提前登记来访信息并配合门岗核验。', '刘宿管', '2026-05-22 08:45:00');

-- 调宿申请
INSERT INTO adjust_room (username, name, currentroom_id, currentbed_id, towardsroom_id, towardsbed_id, state, apply_time, finish_time) VALUES
('stu010', '林同学', 103, 1, 104, 1, '未处理', '2026-05-20 09:12:00', NULL),
('stu011', '郭同学', 103, 2, 201, 4, '通过', '2026-05-08 15:30:00', '2026-05-09 10:00:00'),
('stu015', '梁同学', 202, 1, 203, 1, '驳回', '2026-05-10 11:20:00', '2026-05-11 16:40:00'),
('stu018', '许同学', 10102, 1, 10103, 1, '未处理', '2026-05-21 18:05:00', NULL),
('stu006', '吴同学', 102, 1, 105, 1, '通过', '2026-04-28 08:50:00', '2026-04-29 09:15:00'),
('stu013', '高同学', 201, 2, 204, 1, '驳回', '2026-05-03 13:25:00', '2026-05-04 10:20:00'),
('stu002', '王同学', 10101, 1, 10104, 1, '未处理', '2026-05-22 20:10:00', NULL),
('stu004', '陈同学', 101, 3, 103, 3, '通过', '2026-05-15 16:35:00', '2026-05-16 09:05:00');

-- 报修记录
INSERT INTO repair (id, repairer, dormbuild_id, dormroom_id, title, content, state, order_buildtime, order_finishtime) VALUES
(1, '李同学', 1, 101, '阳台灯不亮', '阳台顶灯打开后闪烁，疑似灯管老化。', '未完成', '2026-05-24 09:10:00', NULL),
(2, '赵同学', 1, 101, '洗手池下水慢', '洗手池排水速度很慢，可能需要疏通。', '完成', '2026-05-14 18:20:00', '2026-05-15 11:30:00'),
(3, '吴同学', 1, 102, '门锁松动', '宿舍门锁把手松动，关门时有异响。', '未完成', '2026-05-23 21:05:00', NULL),
(4, '孙同学', 1, 102, '空调遥控器失灵', '空调遥控器按键无反应，更换电池后仍不可用。', '完成', '2026-05-09 12:00:00', '2026-05-10 15:20:00'),
(5, '林同学', 1, 103, '窗帘滑轨脱落', '靠门一侧窗帘滑轨脱落，影响使用。', '未完成', '2026-05-22 08:35:00', NULL),
(6, '何同学', 1, 201, '卫生间水龙头漏水', '水龙头关闭后仍持续滴水。', '完成', '2026-05-06 07:50:00', '2026-05-06 17:40:00'),
(7, '高同学', 1, 201, '床板异响', '三号床床板翻身时异响明显。', '未完成', '2026-05-19 22:15:00', NULL),
(8, '梁同学', 1, 202, '插座无电', '书桌旁插座无法供电，请检查线路。', '完成', '2026-05-04 10:25:00', '2026-05-05 09:30:00'),
(9, '王同学', 2, 10101, '浴室花洒堵塞', '花洒出水不均匀，疑似堵塞。', '未完成', '2026-05-24 13:45:00', NULL),
(10, '郑同学', 2, 10101, '衣柜门合页损坏', '衣柜左侧门无法正常关闭。', '完成', '2026-05-11 16:10:00', '2026-05-12 14:00:00'),
(11, '许同学', 2, 10102, '宿舍灯管闪烁', '主灯打开后持续闪烁，影响晚间学习。', '未完成', '2026-05-20 19:30:00', NULL),
(12, '蒋同学', 2, 10102, '空调排水异常', '空调运行时室内机附近有滴水。', '完成', '2026-05-02 20:40:00', '2026-05-03 10:10:00');

-- 访客记录
INSERT INTO visitor (name, gender, phone_num, origin_city, visit_time, content) VALUES
('李女士', '女', '13600000001', '北京', '2026-05-24 10:00:00', '探望李同学并送生活用品'),
('赵先生', '男', '13600000002', '天津', '2026-05-23 15:30:00', '探望赵同学'),
('王女士', '女', '13600000003', '上海', '2026-05-22 09:20:00', '探望王同学'),
('陈先生', '男', '13600000004', '南京', '2026-05-21 18:10:00', '送换季衣物'),
('周女士', '女', '13600000005', '杭州', '2026-05-20 11:45:00', '办理住宿材料确认'),
('吴先生', '男', '13600000006', '苏州', '2026-05-18 14:00:00', '探望吴同学'),
('孙女士', '女', '13600000007', '广州', '2026-05-17 16:25:00', '取回学生证件'),
('徐先生', '男', '13600000008', '深圳', '2026-05-15 13:35:00', '送学习资料'),
('胡女士', '女', '13600000009', '成都', '2026-05-12 10:50:00', '探望胡同学'),
('林先生', '男', '13600000010', '武汉', '2026-05-08 17:15:00', '毕业材料交接');

-- 水电默认配置和示例数据
INSERT INTO utility_config (id, electric_limit, water_limit, update_time)
VALUES (1, 80.00, 20.00, '2026-05-24 00:00:00');

INSERT INTO utility_usage (dormroom_id, dormbuild_id, electric_usage, water_usage, collect_time, collect_source) VALUES
(101, 1, 52.40, 12.30, '2026-05-01 08:00:00', 'SCHEDULED'),
(101, 1, 65.74, 12.41, '2026-05-15 08:00:00', 'SCHEDULED'),
(101, 1, 58.90, 11.80, '2026-05-24 08:00:00', 'SCHEDULED'),
(102, 1, 91.80, 13.70, '2026-05-01 08:00:00', 'SCHEDULED'),
(102, 1, 78.20, 14.10, '2026-05-15 08:00:00', 'SCHEDULED'),
(102, 1, 96.40, 15.30, '2026-05-24 08:00:00', 'SCHEDULED'),
(103, 1, 41.60, 8.90, '2026-05-05 08:00:00', 'SCHEDULED'),
(103, 1, 44.30, 9.20, '2026-05-18 08:00:00', 'SCHEDULED'),
(201, 1, 69.10, 16.40, '2026-05-03 08:00:00', 'SCHEDULED'),
(201, 1, 72.50, 17.20, '2026-05-17 08:00:00', 'SCHEDULED'),
(202, 1, 28.60, 5.70, '2026-05-09 08:00:00', 'SCHEDULED'),
(202, 1, 31.20, 6.10, '2026-05-23 08:00:00', 'SCHEDULED'),
(10101, 2, 46.20, 22.10, '2026-05-01 08:00:00', 'SCHEDULED'),
(10101, 2, 50.80, 19.60, '2026-05-14 08:00:00', 'SCHEDULED'),
(10101, 2, 55.10, 21.30, '2026-05-24 08:00:00', 'SCHEDULED'),
(10102, 2, 49.70, 12.50, '2026-05-06 08:00:00', 'SCHEDULED'),
(10102, 2, 83.60, 13.40, '2026-05-20 08:00:00', 'SCHEDULED'),
(104, 1, 12.30, 2.10, '2026-05-24 08:00:00', 'MANUAL'),
(10103, 2, 10.80, 1.90, '2026-05-24 08:00:00', 'MANUAL');

INSERT INTO utility_alert (dormroom_id, dormbuild_id, alert_type, trigger_value, limit_value, status, create_time, handle_time) VALUES
(102, 1, 'ELECTRIC', 91.80, 80.00, 'UNHANDLED', '2026-05-01 08:00:00', NULL),
(102, 1, 'ELECTRIC', 96.40, 80.00, 'UNHANDLED', '2026-05-24 08:00:00', NULL),
(10101, 2, 'WATER', 22.10, 20.00, 'HANDLED', '2026-05-01 08:00:00', '2026-05-02 09:30:00'),
(10102, 2, 'ELECTRIC', 83.60, 80.00, 'UNHANDLED', '2026-05-20 08:00:00', NULL);

-- 出入管理样例数据
INSERT INTO student_return_record
    (student_username, student_name, dormbuild_id, dormroom_id, return_time, is_late, registrar, remark)
SELECT seed.student_username, seed.student_name, seed.dormbuild_id, seed.dormroom_id,
       seed.return_time, seed.is_late, seed.registrar, seed.remark
FROM (
    SELECT 'stu001' student_username, '李同学' student_name, 1 dormbuild_id, 101 dormroom_id, '2026-05-24 22:15:00' return_time, 0 is_late, 'manager1' registrar, '晚自习后正常返寝' remark
    UNION ALL SELECT 'stu003', '赵同学', 1, 101, '2026-05-24 22:48:00', 0, 'manager1', '门岗登记'
    UNION ALL SELECT 'stu006', '吴同学', 1, 102, '2026-05-24 23:36:00', 1, 'manager1', '晚归，已提醒说明原因'
    UNION ALL SELECT 'stu007', '孙同学', 1, 102, '2026-05-23 23:18:00', 1, 'manager1', '社团活动结束较晚'
    UNION ALL SELECT 'stu010', '林同学', 1, 103, '2026-05-22 21:55:00', 0, 'manager1', '正常返寝'
    UNION ALL SELECT 'stu012', '何同学', 1, 201, '2026-05-21 23:42:00', 1, 'manager1', '晚归待核实'
    UNION ALL SELECT 'stu015', '梁同学', 1, 202, '2026-05-20 22:30:00', 0, 'manager1', '正常返寝'
    UNION ALL SELECT 'stu002', '王同学', 2, 10101, '2026-05-24 22:05:00', 0, 'manager2', '正常返寝'
    UNION ALL SELECT 'stu016', '郑同学', 2, 10101, '2026-05-23 23:27:00', 1, 'manager2', '晚归，辅导员已知悉'
    UNION ALL SELECT 'stu018', '许同学', 2, 10102, '2026-05-22 22:40:00', 0, 'manager2', '正常返寝'
) seed
WHERE NOT EXISTS (
    SELECT 1
    FROM student_return_record existing
    WHERE existing.student_username = seed.student_username
      AND existing.return_time = seed.return_time
);

INSERT INTO late_return_alert
    (return_record_id, student_username, student_name, dormbuild_id, dormroom_id, return_time, status, create_time, handle_time)
SELECT r.id, r.student_username, r.student_name, r.dormbuild_id, r.dormroom_id,
       r.return_time, seed.status, seed.create_time, seed.handle_time
FROM student_return_record r
JOIN (
    SELECT 'stu006' student_username, '2026-05-24 23:36:00' return_time, 'UNHANDLED' status, '2026-05-24 23:36:00' create_time, NULL handle_time
    UNION ALL SELECT 'stu007', '2026-05-23 23:18:00', 'HANDLED', '2026-05-23 23:18:00', '2026-05-24 08:20:00'
    UNION ALL SELECT 'stu012', '2026-05-21 23:42:00', 'UNHANDLED', '2026-05-21 23:42:00', NULL
    UNION ALL SELECT 'stu016', '2026-05-23 23:27:00', 'UNHANDLED', '2026-05-23 23:27:00', NULL
) seed ON r.student_username = seed.student_username AND r.return_time = seed.return_time
WHERE NOT EXISTS (
    SELECT 1
    FROM late_return_alert existing
    WHERE existing.return_record_id = r.id
);

INSERT INTO large_item_record
    (student_username, student_name, dormbuild_id, dormroom_id, item_name, direction, register_time, registrar, remark)
SELECT seed.student_username, seed.student_name, seed.dormbuild_id, seed.dormroom_id,
       seed.item_name, seed.direction, seed.register_time, seed.registrar, seed.remark
FROM (
    SELECT 'stu001' student_username, '李同学' student_name, 1 dormbuild_id, 101 dormroom_id, '行李箱' item_name, 'IN' direction, '2026-05-24 18:20:00' register_time, 'manager1' registrar, '周末返校携带' remark
    UNION ALL SELECT 'stu004', '陈同学', 1, 101, '电脑主机', 'IN', '2026-05-24 19:05:00', 'manager1', '学习设备登记'
    UNION ALL SELECT 'stu006', '吴同学', 1, 102, '自行车', 'OUT', '2026-05-23 16:30:00', 'manager1', '带出维修'
    UNION ALL SELECT 'stu009', '胡同学', 1, 102, '被褥', 'OUT', '2026-05-22 10:15:00', 'manager1', '换季带回家'
    UNION ALL SELECT 'stu012', '何同学', 1, 201, '折叠桌', 'IN', '2026-05-21 14:40:00', 'manager1', '宿舍收纳用品'
    UNION ALL SELECT 'stu002', '王同学', 2, 10101, '收纳箱', 'IN', '2026-05-24 17:35:00', 'manager2', '生活用品'
    UNION ALL SELECT 'stu016', '郑同学', 2, 10101, '书架', 'OUT', '2026-05-23 11:25:00', 'manager2', '带出调整宿舍布局'
    UNION ALL SELECT 'stu018', '许同学', 2, 10102, '显示器', 'OUT', '2026-05-22 15:10:00', 'manager2', '带出维修'
) seed
WHERE NOT EXISTS (
    SELECT 1
    FROM large_item_record existing
    WHERE existing.student_username = seed.student_username
      AND existing.item_name = seed.item_name
      AND existing.register_time = seed.register_time
);

-- 校园基础数据扩容：3-5号楼、宿管、学生和房间
INSERT INTO dorm_build (dormbuild_id, dormbuild_name, dormbuild_detail)
SELECT seed.dormbuild_id, seed.dormbuild_name, seed.dormbuild_detail
FROM (
    SELECT 3 dormbuild_id, '3号楼' dormbuild_name, '综合宿舍楼，靠近教学区和医务室' dormbuild_detail
    UNION ALL SELECT 4, '4号楼', '研究生宿舍楼，靠近实验楼和图书馆'
    UNION ALL SELECT 5, '5号楼', '新生宿舍楼，靠近体育馆和生活服务中心'
) seed
WHERE NOT EXISTS (
    SELECT 1 FROM dorm_build existing WHERE existing.dormbuild_id = seed.dormbuild_id
);

INSERT INTO dorm_manager (username, password, dormbuild_id, name, gender, age, phone_num, email)
SELECT seed.username, seed.password, seed.dormbuild_id, seed.name, seed.gender, seed.age, seed.phone_num, seed.email
FROM (
    SELECT 'manager3' username, 'e8c7659e7d15fa797bebf6e5ec9af446' password, 3 dormbuild_id, '陈宿管' name, '男' gender, 43 age, '13900000002' phone_num, 'mgr3@dorm.com' email
    UNION ALL SELECT 'manager4', 'e8c7659e7d15fa797bebf6e5ec9af446', 4, '黄宿管', '女', 41, '13900000003', 'mgr4@dorm.com'
    UNION ALL SELECT 'manager5', 'e8c7659e7d15fa797bebf6e5ec9af446', 5, '赵宿管', '男', 46, '13900000004', 'mgr5@dorm.com'
) seed
WHERE NOT EXISTS (
    SELECT 1 FROM dorm_manager existing WHERE existing.username = seed.username
);

INSERT INTO student (username, password, name, age, gender, phone_num, email)
SELECT
    CONCAT('stu', LPAD(n, 3, '0')) username,
    'e8c7659e7d15fa797bebf6e5ec9af446' password,
    CONCAT('学生', LPAD(n, 3, '0')) name,
    18 + (n % 5) age,
    CASE WHEN n BETWEEN 41 AND 60 THEN '女' WHEN n % 2 = 0 THEN '女' ELSE '男' END gender,
    CONCAT('137', LPAD(n - 1, 8, '0')) phone_num,
    CONCAT('stu', LPAD(n, 3, '0'), '@dorm.com') email
FROM (
    SELECT t.tens * 10 + o.ones n
    FROM (
        SELECT 2 tens UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8
    ) t
    CROSS JOIN (
        SELECT 0 ones UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) o
) seed
WHERE n BETWEEN 21 AND 80
  AND NOT EXISTS (
      SELECT 1 FROM student existing WHERE existing.username = CONCAT('stu', LPAD(seed.n, 3, '0'))
  );

INSERT INTO dorm_room
    (dormroom_id, dormbuild_id, floor_num, max_capacity, current_capacity, first_bed, second_bed, third_bed, fourth_bed)
SELECT
    seed.dormroom_id,
    seed.dormbuild_id,
    seed.floor_num,
    4 max_capacity,
    seed.current_capacity,
    seed.first_bed,
    seed.second_bed,
    seed.third_bed,
    seed.fourth_bed
FROM (
    SELECT
        b.dormbuild_id * 10000 + 100 + r.room_index dormroom_id,
        b.dormbuild_id,
        CASE WHEN r.room_index <= 5 THEN 1 ELSE 2 END floor_num,
        CASE WHEN r.room_index <= 5 THEN 4 ELSE 0 END current_capacity,
        CASE WHEN r.room_index <= 5 THEN CONCAT('stu', LPAD(b.start_student + (r.room_index - 1) * 4, 3, '0')) ELSE NULL END first_bed,
        CASE WHEN r.room_index <= 5 THEN CONCAT('stu', LPAD(b.start_student + (r.room_index - 1) * 4 + 1, 3, '0')) ELSE NULL END second_bed,
        CASE WHEN r.room_index <= 5 THEN CONCAT('stu', LPAD(b.start_student + (r.room_index - 1) * 4 + 2, 3, '0')) ELSE NULL END third_bed,
        CASE WHEN r.room_index <= 5 THEN CONCAT('stu', LPAD(b.start_student + (r.room_index - 1) * 4 + 3, 3, '0')) ELSE NULL END fourth_bed
    FROM (
        SELECT 3 dormbuild_id, 21 start_student
        UNION ALL SELECT 4, 41
        UNION ALL SELECT 5, 61
    ) b
    CROSS JOIN (
        SELECT 1 room_index UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
        UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
    ) r
) seed
WHERE NOT EXISTS (
    SELECT 1 FROM dorm_room existing WHERE existing.dormroom_id = seed.dormroom_id
);
