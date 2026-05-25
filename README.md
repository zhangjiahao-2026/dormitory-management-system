# 高校宿舍管理系统

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.6.3-green.svg)
![Vue](https://img.shields.io/badge/Vue-3-blue.svg)
![MySQL](https://img.shields.io/badge/MySQL-5.7+-lightgrey.svg)
![Java](https://img.shields.io/badge/Java-11-orange.svg)

基于 Spring Boot 2.6.3、Vue 3、Element Plus、MyBatis-Plus 和 MySQL 的高校宿舍管理系统。系统面向管理员、宿管和学生三类角色，覆盖宿舍基础信息、报修调宿、访客登记、水电管理、出入管理和首页告警看板等常用宿舍业务。

## 功能概览

### 管理员

- 首页数据看板：学生、入住、报修、空宿舍、晚归告警、水电告警等指标。
- 用户与宿舍管理：学生管理、宿管管理、楼宇管理、房间管理。
- 日常业务管理：公告管理、报修管理、调宿管理、访客管理。
- 水电管理：查看采集记录、手动模拟采集、处理电量/水量告警、配置全局水电阈值。
- 出入管理：返寝登记、大件物品进出登记、晚归告警处理。

### 宿管

- 查看首页看板和本楼栋相关数据。
- 管理本楼栋学生、楼宇、房间、报修、调宿和访客记录。
- 进行水电采集模拟，查看和处理本楼栋水电告警。
- 登记学生返寝和大件物品进出，处理本楼栋晚归告警。

### 学生

- 查看“我的宿舍”，包含楼栋、房间、床位、室友和本月水电用量。
- 提交报修申请和调宿申请。
- 查看和维护个人信息。

## 近期新增模块

### 水电管理

- 记录宿舍电量和水量采集数据。
- 支持手动模拟采集和每日定时采集。
- 支持电量、水量超限告警。
- 首页展示未处理水电告警并提供快捷入口。
- 全局水电阈值仅管理员可修改，宿管和学生不能修改配置。

### 出入管理

- 支持学生返寝登记。
- 返寝时间超过 23:00 自动标记晚归并生成晚归告警。
- 支持晚归告警处理。
- 支持大件物品带入、带出登记。

### 学生端宿舍页

- 学生登录后默认进入“我的宿舍”。
- 展示当前宿舍、床位、室友和入住人数。
- 展示本月电量、水量使用汇总。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 后端 | Spring Boot 2.6.3、MyBatis-Plus 3.5.1、Maven |
| 前端 | Vue 3、Vue Router、Vuex、Element Plus、Axios、ECharts |
| 数据库 | MySQL 5.7+ |
| 运行环境 | JDK 11、Node.js 14+ |

## 项目结构

```text
DormitoryManagementSystem/
├── Dormitory_business/                 # Spring Boot 后端
│   ├── src/main/java/com/example/springboot/
│   │   ├── common/                     # 配置、工具类、拦截器、定时任务
│   │   ├── controller/                 # 控制器
│   │   ├── entity/                     # 实体类
│   │   ├── mapper/                     # MyBatis-Plus Mapper
│   │   └── service/                    # 业务逻辑
│   └── src/main/resources/
│       └── application.properties      # 后端端口和数据库配置
├── vue/                                # Vue 3 前端
│   ├── src/assets/                     # CSS、页面脚本、图片资源
│   ├── src/components/                 # 公共组件
│   ├── src/layout/                     # 布局组件
│   ├── src/router/                     # 路由配置
│   ├── src/utils/                      # Axios 请求封装
│   └── src/views/                      # 页面视图
├── doc/                                # 数据库脚本
│   ├── dormitory.sql                   # 全量初始化脚本
│   ├── utility_migration.sql           # 水电管理迁移脚本
│   ├── access_migration.sql            # 出入管理迁移脚本
│   ├── access_seed.sql                 # 出入管理样例数据
│   └── campus_seed.sql                 # 校园基础扩展示例数据
└── README.md
```

## 快速启动

### 环境要求

- JDK 11
- Maven 3.6+
- Node.js 14+
- MySQL 5.7+

### 1. 初始化数据库

全量初始化推荐直接执行：

```bash
mysql -u root -p < doc/dormitory.sql
```

如果已有旧数据库，可按模块增量执行：

```bash
mysql -u root -p dormitory < doc/utility_migration.sql
mysql -u root -p dormitory < doc/access_migration.sql
mysql -u root -p dormitory < doc/access_seed.sql
mysql -u root -p dormitory < doc/campus_seed.sql
```

### 2. 启动后端

```bash
cd Dormitory_business
mvn spring-boot:run
```

后端默认运行在：

```text
http://localhost:9091
```

数据库连接配置位于：

```text
Dormitory_business/src/main/resources/application.properties
```

### 3. 启动前端

```bash
cd vue
npm install
npm run serve
```

前端端口以 Vue CLI 输出为准，常见地址为：

```text
http://localhost:8080
http://localhost:8081
```

## 默认账号

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | admin | 123456 |
| 宿管 | manager1 | 123456 |
| 学生 | stu001 | 123456 |

## 验证命令

后端测试：

```bash
cd Dormitory_business
mvn test
```

前端生产构建：

```bash
cd vue
npm run build
```

## 权限说明

- 未登录用户访问业务接口会被拦截。
- 学生登录后默认进入“我的宿舍”，不能访问管理端首页和后台管理页面。
- 宿管只能查看和处理自己负责楼栋范围内的数据。
- 水电阈值属于全局系统配置，仅管理员可以修改。

