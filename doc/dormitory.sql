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

-- ========== 初始数据 ==========

-- 管理员账号（密码: 123456，MD5+盐值加密）
INSERT INTO admin (username, password, name, gender, age, phone_num, email)
VALUES ('admin', 'e8c7659e7d15fa797bebf6e5ec9af446', '系统管理员', '男', 30, '13800000000', 'admin@dorm.com');

-- 楼宇
INSERT INTO dorm_build (dormbuild_id, dormbuild_name, dormbuild_detail) VALUES
(1, '1号楼', '男生宿舍楼'),
(2, '2号楼', '女生宿舍楼');

-- 宿管账号（密码: 123456，MD5+盐值加密）
INSERT INTO dorm_manager (username, password, dormbuild_id, name, gender, age, phone_num, email)
VALUES ('manager1', 'e8c7659e7d15fa797bebf6e5ec9af446', 1, '张宿管', '男', 45, '13900000000', 'mgr1@dorm.com');

-- 学生账号（密码: 123456，MD5+盐值加密）
INSERT INTO student (username, password, name, age, gender, phone_num, email) VALUES
('stu001', 'e8c7659e7d15fa797bebf6e5ec9af446', '李同学', 20, '男', '13700000000', 'stu001@dorm.com'),
('stu002', 'e8c7659e7d15fa797bebf6e5ec9af446', '王同学', 19, '女', '13700000001', 'stu002@dorm.com'),
('stu003', 'e8c7659e7d15fa797bebf6e5ec9af446', '赵同学', 21, '男', '13700000002', 'stu003@dorm.com');

-- 房间（1号楼每层5间房，共5层）
INSERT INTO dorm_room (dormroom_id, dormbuild_id, floor_num, max_capacity, current_capacity, first_bed, second_bed, third_bed, fourth_bed) VALUES
(101, 1, 1, 4, 2, 'stu001', 'stu003', NULL, NULL),
(102, 1, 1, 4, 0, NULL, NULL, NULL, NULL),
(103, 1, 1, 4, 0, NULL, NULL, NULL, NULL),
(104, 1, 1, 4, 0, NULL, NULL, NULL, NULL),
(105, 1, 1, 4, 0, NULL, NULL, NULL, NULL),
(201, 1, 2, 4, 0, NULL, NULL, NULL, NULL),
(202, 1, 2, 4, 0, NULL, NULL, NULL, NULL),
(203, 1, 2, 4, 0, NULL, NULL, NULL, NULL),
(204, 1, 2, 4, 0, NULL, NULL, NULL, NULL),
(205, 1, 2, 4, 0, NULL, NULL, NULL, NULL),
(301, 1, 3, 4, 0, NULL, NULL, NULL, NULL),
(302, 1, 3, 4, 0, NULL, NULL, NULL, NULL),
(303, 1, 3, 4, 0, NULL, NULL, NULL, NULL),
(304, 1, 3, 4, 0, NULL, NULL, NULL, NULL),
(305, 1, 3, 4, 0, NULL, NULL, NULL, NULL);

-- 2号楼房间
INSERT INTO dorm_room (dormroom_id, dormbuild_id, floor_num, max_capacity, current_capacity, first_bed, second_bed, third_bed, fourth_bed) VALUES
(10101, 2, 1, 4, 1, 'stu002', NULL, NULL, NULL),
(10102, 2, 1, 4, 0, NULL, NULL, NULL, NULL),
(10103, 2, 1, 4, 0, NULL, NULL, NULL, NULL),
(10104, 2, 1, 4, 0, NULL, NULL, NULL, NULL),
(10105, 2, 1, 4, 0, NULL, NULL, NULL, NULL);
