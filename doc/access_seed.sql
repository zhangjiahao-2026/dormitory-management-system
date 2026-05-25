-- 出入管理样例数据
-- 可重复执行：按业务字段判断是否已存在，避免重复插入。

SET NAMES utf8mb4;

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
