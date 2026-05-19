# AIWMS-boot — 智能仓库管理系统（后端）

基于 Spring Boot 的智能仓库管理系统后端，支持进销存核心业务流程，并集成 DeepSeek AI 实现物料智能预测。

## 技术栈

| 组件 | 版本 |
|------|------|
| Spring Boot | 2.0.0.RELEASE |
| MyBatis-Plus | 3.0.7.1 |
| MySQL | 5.7+ / 8.x |
| Redis | — |
| Java | 1.8 |
| Swagger (Bootstrap UI) | 2.7.0 / 1.6 |
| Lombok | 1.18.30 |
| PageHelper | 1.2.13 |
| iTextPDF | 5.5.13.1 |
| Aliyun OSS SDK | 3.10.1 |

## 项目结构

```
src/main/java/com/jsh/erp/
├── ErpApplication.java      # 启动入口
├── base/                    # 通用基础模块
├── config/                  # 配置类
├── constants/               # 常量定义
├── controller/              # REST 接口层
├── datasource/              # 数据源 & MyBatis Mapper
├── exception/               # 异常处理
├── filter/                  # 请求过滤器（登录校验等）
├── service/                 # 业务逻辑层
└── utils/                   # 工具类
```

## 快速开始

### 1. 环境要求

- **JDK 8** 或以上
- **MySQL 5.7+** / 8.x
- **Redis**
- **Maven 3.x**

### 2. 初始化数据库

使用 `docs/jsh_erp.sql` 初始化数据库结构和基础数据：

```bash
mysql -u root -p < docs/jsh_erp.sql
```

如需 AI 物料预测表，额外执行：

```bash
mysql -u root -p jsh_erp < docs/sql_material_ai_prediction.sql
```

### 3. 配置

编辑 `src/main/resources/application.properties`，或通过环境变量覆盖默认值：

| 环境变量 | 说明 | 默认值 |
|---------|------|--------|
| `MYSQL_URL` | 数据库连接地址 | `jdbc:mysql://127.0.0.1:3306/jsh_erp` |
| `MYSQL_USERNAME` | 数据库用户名 | `root` |
| `MYSQL_PASSWORD` | 数据库密码 | *(空)* |
| `REDIS_HOST` | Redis 地址 | `127.0.0.1` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `REDIS_PASSWORD` | Redis 密码 | *(空)* |
| `FILE_UPLOAD_PATH` | 文件上传路径 | `/opt/AIWMS/upload` |
| `DEEPSEEK_API_KEY` | DeepSeek API 密钥 | *(空)* |

### 4. 构建与运行

```bash
mvn clean package -DskipTests
java -jar target/jshERP.jar
```

启动后访问：

- **服务地址**：`http://localhost:9999/AIWMS-boot`
- **Swagger 文档**：`http://localhost:9999/AIWMS-boot/doc.html`

### 5. 前端联调

如需配合前端开发，在前端工程目录执行：

```bash
yarn run serve   # 或 npm run serve
```

## 核心功能模块

- **进销存管理**：采购入库、销售出库、库存盘点
- **财务管理**：收付款、账户管理
- **基础数据**：商品管理、供应商/客户管理、仓库管理
- **报表分析**：经营报表、库存报表
- **AI 物料预测**：基于 DeepSeek 的物料需求智能分析

## 文档

| 文档 | 说明 |
|------|------|
| `docs/STUDENT_EDITION_GUIDE.md` | 项目概况与简化策略说明 |
| `docs/CODE_STYLE_GUIDE.md` | 后端编码规范 |
| `docs/CHANGELOG_REFACTOR.md` | 重构变更日志 |
| `docs/FRONTEND_API.md` | 前端接口文档 |

## 许可证

本项目基于 [jshERP](https://gitee.com/jishenghua/jshERP) 二次开发，仅供学习与毕业设计使用。
