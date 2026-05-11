# 高校宿舍管理系统

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.6.3-green.svg) ![Vue](https://img.shields.io/badge/Vue-3-blue.svg) ![MySQL](https://img.shields.io/badge/MySQL-5.7+-lightgrey.svg) ![Java](https://img.shields.io/badge/Java-11-orange.svg)

基于 Spring Boot + Vue 3 的高校宿舍管理系统，支持三种角色：系统管理员、宿舍管理员、学生。

## 功能概览

**系统管理员**
- 首页数据看板（ECharts 可视化）
- 学生管理、宿管管理、楼宇管理、房间管理
- 公告管理、报修管理、调宿管理、访客管理

**宿舍管理员**
- 首页数据看板
- 学生查询、楼宇/房间管理、公告发布
- 报修处理、调宿审批、访客登记

**学生**
- 查看我的宿舍信息
- 在线报修、申请调宿
- 个人信息管理

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 2.6.3、MyBatis-Plus 3.5.1 |
| 前端 | Vue 3、Element Plus、Axios、ECharts |
| 数据库 | MySQL 5.7+ |
| 构建工具 | Maven、Vue CLI |

## 项目结构

```
DormitoryManagementSystem/
├── Dormitory_business/     # Spring Boot 后端
│   └── src/main/java/com/example/springboot/
│       ├── common/         # 配置类、工具类、拦截器
│       ├── controller/     # 控制器层
│       ├── entity/         # 实体类
│       ├── mapper/         # MyBatis-Plus Mapper
│       └── service/        # 业务逻辑层
├── vue/                    # Vue 3 前端
│   └── src/
│       ├── assets/         # CSS、JS
│       ├── components/     # 公共组件
│       ├── layout/         # 布局组件
│       ├── router/         # 路由配置
│       ├── utils/          # Axios 封装
│       └── views/          # 页面视图
└── doc/                    # 文档与截图
    ├── dormitory.sql       # 建表脚本（含初始数据）
    └── img/                # 运行效果截图
```

## 快速启动

### 环境要求

- JDK 11
- Maven 3.6+
- Node.js 14+
- MySQL 5.7+

### 1. 初始化数据库

```bash
mysql -u root -p < doc/dormitory.sql
```

### 2. 启动后端

```bash
cd Dormitory_business
# 修改 src/main/resources/application.properties 中的数据库连接信息
mvn spring-boot:run
```

后端默认运行在 `http://localhost:9091`

### 3. 启动前端

```bash
cd vue
npm install
npm run serve
```

前端默认运行在 `http://localhost:8080`

### 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 宿管 | manager1 | 123456 |
| 学生 | stu001 | 123456 |

