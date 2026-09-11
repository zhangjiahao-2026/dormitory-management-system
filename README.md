# 高校宿舍管理系统

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.6.3-green.svg)
![Vue](https://img.shields.io/badge/Vue-3-blue.svg)
![FastAPI](https://img.shields.io/badge/FastAPI-0.103-009688.svg)
![Chroma](https://img.shields.io/badge/RAG-Chroma-orange.svg)
![MySQL](https://img.shields.io/badge/MySQL-5.7+-lightgrey.svg)
![H2](https://img.shields.io/badge/H2-local%20demo-blue.svg)
![Java](https://img.shields.io/badge/Java-11-orange.svg)

基于 Spring Boot 2.6.3、Vue 3、Element Plus、MyBatis-Plus 和 MySQL 的高校宿舍管理系统。系统面向管理员、宿管和学生三类角色，覆盖宿舍基础信息、报修调宿、访客登记、水电管理、出入管理和首页告警看板等常用宿舍业务。

## AI 报修工单助手

### 项目解决的问题

传统宿舍报修依赖学生手动选择类别、描述故障，再由宿管人工判断紧急程度和处理部门，容易出现信息不完整、危险事件未及时升级、处理依据不可追溯等问题。本项目在保留原人工报修入口的基础上增加 AI 辅助链路：

- 分类阶段支持 OpenAI-compatible 模型 Prompt；完整性、安全检查和 RAG 依据校验使用确定性代码规则，不配置模型时仍可离线运行。
- 从 6 份宿舍维修 SOP 中检索 Top-3 依据，展示文档、章节和相关度；离线模式采用哈希向量、关键词命中和词元重叠融合排序，不宣称为神经语义 Embedding。
- 对漏电、明火、燃气、水接近电气设施和提示注入等风险进行前后置检查。
- AI 只生成待审核申请；宿管或管理员确认后才创建正式工单。
- 无有效 SOP、低置信度、模型超时或输出异常时安全降级并转人工。

### AI 工作流

```mermaid
flowchart LR
    A[学生填写报修描述] --> B[Session 与宿舍信息校验]
    B --> C[风险预检]
    C --> D[本地规则 / 大模型分类]
    D --> E[Chroma 检索 Top-3 SOP]
    E --> F[建议与来源安全检查]
    F --> G{结果可信且有依据?}
    G -- 否 --> H[标记人工复核]
    G -- 是 --> I[生成待审核申请]
    H --> I
    I --> J[宿管按楼栋审核]
    J -- 确认 --> K[事务内创建正式工单]
    J -- 拒绝 --> L[记录拒绝原因]
```

详细流程、角色边界和风险规则见 [AI 报修流程说明](doc/ai-repair-workflow.md)。

### 演示截图

![AI 报修待审核、运行指标与人工确认入口](doc/images/ai-repair-review.jpg)

上图为本地演示数据：AI 分析先进入待审核列表，宿管可查看分类、紧急程度、置信度和 SOP 来源，确认后才会生成正式报修工单。

### 核心指标

以下结果来自 `ai-service/evaluation/dataset.json` 的 30 条离线评测，运行本地规则、SOP 检索和风险检查，不调用外部大模型：

| 指标 | 结果 |
| --- | ---: |
| 分类准确率 | 90% |
| 紧急事件召回率 | 100% |
| SOP Top-3 命中率 | 86.36% |
| 本地规则 Schema 合法率 | 100% |
| 正确拒答率 | 100% |
| 人工复核召回率 | 100% |

### 风险规则迭代

离线评测暴露了“漏水已经流到插座附近”这类跨类别复合风险。项目在 Prompt 和本地分类器中同步加入“水 + 电气设施”规则，命中后强制升级为紧急事件并转人工复核。当前 30 条评测集上的紧急事件召回率为 100%，分类准确率为 90%，剩余误差和改进方向在报告中如实记录。

分类、SOP 建议和安全约束策略位于 [`ai-service/app/prompts/`](ai-service/app/prompts/)，可执行评测集位于 [`ai-service/evaluation/dataset.json`](ai-service/evaluation/dataset.json)，迭代复盘见 [`ai-service/evaluation/评估迭代报告.md`](ai-service/evaluation/评估迭代报告.md)。

### AI 服务配置

AI 服务读取 `AI_LLM_API_KEY`、`AI_LLM_BASE_URL` 和 `AI_LLM_MODEL`。未配置真实模型时自动使用本地规则和 RAG；真实密钥只应放在被 Git 忽略的 `.env.local`。启动后访问 `http://localhost:8080`，学生可提交 AI 报修申请，宿管或管理员可在“报修信息”中审核。完整的 MySQL 部署说明见下方“快速启动”。

生产部署时请设置 `AI_ENV=production` 和非空的 `AI_SERVICE_TOKEN`，并让 Spring Boot 使用相同令牌；AI 服务在生产模式缺少令牌时会拒绝启动。跨域来源通过 `CORS_ALLOWED_ORIGINS` 配置，开发环境默认允许 `http://localhost:8080` 和 `http://127.0.0.1:8080`，生产环境应改为实际前端域名。

### 安全边界

- 登录响应、用户列表和 Session 用户查询均不返回密码字段。
- 新密码使用 BCrypt；历史 MD5 记录在用户成功登录后自动升级，无需一次性重置账号。
- 管理员可执行全局管理；学生只能修改本人资料、查询本人报修；宿管的房间、调宿和报修操作按所属楼栋校验。
- 头像更新接口只接收用户名和头像文件名，不能通过完整实体覆盖他人资料。

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

## 校园社区模块

新增“校园社区 / 帖子管理”功能，提供轻量交流区，可用于失物寻物、招领信息、生活分享和求助互助。

### 功能说明

- 学生端新增“校园社区”页面，路由为 `/forumInfo`。
- 管理员和宿管端新增“帖子管理”页面，路由为 `/forumManageInfo`。
- 帖子分类固定为：`失物`、`招领`、`分享`、`求助`。
- 学生可以发布帖子、浏览帖子、按分类筛选、搜索标题或内容、查看详情、发表评论。
- 帖子支持上传一张图片，复用 `/files/upload` 上传接口；评论暂只支持文字。
- 学生可以编辑、删除自己发布的帖子和评论。
- 管理员和宿管可以查看全部帖子，并删除不合适的帖子或评论。
- 帖子和评论采用逻辑删除，不会从数据库物理移除。

## AI 小助手模块

系统新增全局右下角悬浮 AI 小助手，登录后在后台和学生端页面均可使用。小助手采用“本地意图识别 + 大模型兜底”的混合模式：

- 已知宿舍业务命令优先由后端本地规则解析，例如查询学生、查询空床位、查看晚归告警、跳转公告管理、生成报修或公告草稿。
- 本地无法明确识别的普通问题会调用 OpenAI-compatible 大模型接口，例如宿舍管理建议、公告文案润色、注意事项总结等。
- 大模型只允许返回文字回复和“建议动作”，后端会重新映射到本地能力注册表，并按当前登录角色二次校验。
- 所有新增、修改类操作只会打开现有页面并预填草稿，不会直接写入数据库，仍需用户在页面中手动确认保存。
- 删除类指令不会自动执行，小助手会提示用户进入对应页面手动处理。

### 支持示例

- `查张三`：查询当前角色有权限访问的学生、访客、报修、论坛等模块，并提供跳转或搜索按钮。
- `3号楼空床位`：查询楼宇、房间、床位相关信息。
- `最近晚归告警`：返回晚归告警摘要并可跳转出入管理页面。
- `新增公告：今晚停水`：跳转公告管理并打开新增公告草稿。
- `给101宿舍新增报修：灯坏了`：跳转报修页面并生成待确认草稿。

### 大模型环境变量

大模型调用默认通过环境变量配置，不需要把真实密钥提交到仓库：

| 环境变量 | 说明 | 默认值 |
| --- | --- | --- |
| `LLM_API_KEY` | OpenAI-compatible 服务的 API Key；缺少时自动降级为本地助手 | 空 |
| `LLM_BASE_URL` | 服务地址，例如 `https://api.deepseek.com` | `https://api.openai.com` |
| `LLM_MODEL` | 模型名称，例如 `deepseek-chat` 或服务商提供的模型名 | `gpt-4o-mini` |

Windows PowerShell 示例：

```powershell
setx LLM_API_KEY "your-api-key"
setx LLM_BASE_URL "https://api.deepseek.com"
setx LLM_MODEL "your-model-name"
```

`setx` 写入的是后续新终端的用户环境变量。配置后请重新打开终端，并重启 Spring Boot 后端；否则当前已运行的后端进程仍读取不到新值。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 后端 | Spring Boot 2.6.3、MyBatis-Plus 3.5.1、Maven |
| AI 服务 | FastAPI、Pydantic、Chroma、SQLite、OpenAI-compatible API |
| 前端 | Vue 3、Vue Router、Vuex、Element Plus、Axios、ECharts |
| 数据库 | MySQL 5.7+ |
| 运行环境 | JDK 11、Node.js 14+、Python 3.9+ |

## 项目结构

```text
DormitoryManagementSystem/
├── ai-service/                         # AI 报修分析服务
│   ├── app/prompts/                    # Prompt/规则策略文档（仅分类 Prompt 直接调用模型）
│   ├── app/services/                   # 分类、RAG、风险和工作流
│   ├── evaluation/                     # 30 条评测集和评估脚本
│   ├── knowledge/                      # 6 份宿舍维修 SOP
│   └── tests/                          # AI 单元与安全测试
├── Dormitory_business/                 # Spring Boot 后端
│   ├── src/main/java/com/example/springboot/
│   │   ├── common/                     # 配置、工具类、拦截器、定时任务
│   │   ├── controller/                 # 控制器
│   │   │   └── AssistantController.java # AI 小助手接口
│   │   ├── entity/                     # 实体类
│   │   ├── mapper/                     # MyBatis-Plus Mapper
│   │   └── service/                    # 业务逻辑
│   └── src/main/resources/
│       ├── application.properties      # MySQL 部署配置
│       ├── application-local.properties # H2 本地演示配置
│       └── schema-h2.sql               # H2 表结构与演示数据
├── vue/                                # Vue 3 前端
│   ├── src/assets/                     # CSS、页面脚本、图片资源
│   ├── src/components/                 # 公共组件
│   │   └── AssistantWidget.vue         # 全局 AI 小助手组件
│   ├── src/layout/                     # 布局组件
│   ├── src/router/                     # 路由配置
│   ├── src/utils/                      # Axios 请求封装、AI 小助手事件桥
│   └── src/views/                      # 页面视图
├── doc/                                # 数据库脚本
│   ├── ai-repair-workflow.md           # AI 流程、边界和接口约定
│   ├── images/                         # 仓库演示截图
│   ├── dormitory.sql                   # 全量初始化脚本
│   ├── utility_migration.sql           # 水电管理迁移脚本
│   ├── access_migration.sql            # 出入管理迁移脚本
│   ├── access_seed.sql                 # 出入管理样例数据
│   └── campus_seed.sql                 # 校园基础扩展示例数据
├── .github/workflows/ci.yml           # Python、Maven 与前端构建 CI
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
mysql -u root -p dormitory < doc/visitor_scope_migration.sql
```

### 2. 启动后端

如果需要启用 AI 小助手的大模型兜底能力，请先配置 `LLM_API_KEY`、`LLM_BASE_URL` 和 `LLM_MODEL` 环境变量。未配置 `LLM_API_KEY` 时，系统仍可正常启动，小助手会保留本地查询、跳转和草稿能力，并对未知问题返回降级提示。

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

以下账号和数据仅用于本地演示，不应用于生产环境。

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

AI 服务单元测试与离线评测：

```bash
cd ai-service
python -m pytest
python evaluation/evaluate.py
```

离线评测不调用外部大模型，可在本地稳定复现 README 中的指标。
