-- 校园基础数据扩容脚本
-- 可重复执行：按楼宇编号、用户名、房间号判断是否已存在。

SET NAMES utf8mb4;

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
