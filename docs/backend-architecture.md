# 后端说明文档

> 范围说明：本文档覆盖 AIWMS 仓储管理系统后端结构与功能，聚焦仓储核心业务，便于论文撰写与前后端联调。
>
> 版本说明：基于 jshERP 后端裁剪优化，移除了财务、零售、多租户等与仓储管理无直接关联的 ERP 扩展模块，
> 保留完整的采购入库、销售出库、库存管理、调拨、报表统计等 WMS 核心能力。

## 1. 技术栈与入口

- 框架：Spring Boot 2.0 + MyBatis-Plus 3.0，数据库为 MySQL 8。
- 缓存/会话管理：Redis，用于 Token 会话存储与多实例共享。
- API 文档：Swagger 2 + swagger-bootstrap-ui（增强 UI）。
- 插件化架构：springboot-plugin-framework（基于 pf4j），支持热插拔业务扩展。
- AI 能力：集成 DeepSeek API，提供库存智能预测。
- 多租户：MyBatis-Plus TenantSqlParser，行级租户隔离。
- 入口类：[src/main/java/com/jsh/erp/ErpApplication.java](../src/main/java/com/jsh/erp/ErpApplication.java)
- 配置文件：[src/main/resources/application.properties](../src/main/resources/application.properties)
- 日志配置：[src/main/resources/logback-spring.xml](../src/main/resources/logback-spring.xml)
- Maven 构建：[pom.xml](../pom.xml)

## 2. 本地运行与构建

```bash
# 编译打包（跳过测试）
mvn clean package -DskipTests

# 运行（默认端口 9999，上下文路径 /AIWMS-boot）
java -jar target/jshERP.jar

# 开发模式（IDE 中直接运行 ErpApplication.main()）
```

运行前置条件：
- MySQL 8 运行在 `127.0.0.1:3306`，数据库名 `jsh_erp`，初始脚本见 [docs/jsh_erp.sql](../docs/jsh_erp.sql)。
- Redis 运行在 `127.0.0.1:6379`（无密码）。
- 配置项可通过环境变量覆盖，详见 `application.properties`。

## 3. 目录结构总览

```
AIWMS-boot/
├── pom.xml                              # Maven 构建配置与依赖声明
├── README.md                            # 项目说明
├── docs/                                # 项目文档
│   ├── CHANGELOG_REFACTOR.md            # 重构变更日志
│   ├── CODE_STYLE_GUIDE.md              # 代码风格指南
│   ├── FRONTEND_API.md                  # 前端接口文档
│   ├── STUDENT_EDITION_GUIDE.md         # 学生版使用指南
│   ├── jsh_erp.sql                      # 数据库初始化脚本
│   └── sql_material_ai_prediction.sql   # AI 预测功能增量 SQL
│
├── dist/                                # 打包输出目录
├── logs.home_IS_UNDEFINED/              # 日志输出目录（默认配置）
│
└── src/
    ├── main/
    │   ├── assembly/                    # Maven Assembly 打包描述
    │   │   └── assembly.xml
    │   ├── bin/                         # 启动/停止脚本
    │   │   ├── restart.sh
    │   │   ├── start.sh
    │   │   └── stop.sh
    │   ├── resources/
    │   │   ├── application.properties   # ★ 应用配置（端口/数据库/Redis/AI）
    │   │   ├── logback-spring.xml       # 日志格式与级别配置
    │   │   └── mapper_xml/             # ★ MyBatis Mapper XML（60 个）
    │   │       ├── AccountHeadMapper.xml / AccountHeadMapperEx.xml
    │   │       ├── AccountItemMapper.xml / AccountItemMapperEx.xml
    │   │       ├── AccountMapper.xml / AccountMapperEx.xml
    │   │       ├── DepotHeadMapper.xml / DepotHeadMapperEx.xml
    │   │       ├── DepotItemMapper.xml / DepotItemMapperEx.xml
    │   │       ├── DepotMapper.xml / DepotMapperEx.xml
    │   │       ├── FunctionMapper.xml / FunctionMapperEx.xml
    │   │       ├── MaterialMapper.xml / MaterialMapperEx.xml
    │   │       ├── MaterialCategoryMapper.xml / MaterialCategoryMapperEx.xml
    │   │       ├── MaterialExtendMapper.xml / MaterialExtendMapperEx.xml
    │   │       ├── MaterialAttributeMapper.xml / MaterialAttributeMapperEx.xml
    │   │       ├── MaterialPropertyMapper.xml / MaterialPropertyMapperEx.xml
    │   │       ├── MaterialCurrentStockMapper.xml / MaterialCurrentStockMapperEx.xml
    │   │       ├── MaterialInitialStockMapper.xml / MaterialInitialStockMapperEx.xml
    │   │       ├── SupplierMapper.xml / SupplierMapperEx.xml
    │   │       ├── UserMapper.xml / UserMapperEx.xml
    │   │       ├── RoleMapper.xml / RoleMapperEx.xml
    │   │       ├── TenantMapper.xml / TenantMapperEx.xml
    │   │       ├── PersonMapper.xml / PersonMapperEx.xml
    │   │       ├── OrganizationMapper.xml / OrganizationMapperEx.xml
    │   │       ├── MsgMapper.xml / MsgMapperEx.xml
    │   │       ├── LogMapper.xml / LogMapperEx.xml
    │   │       ├── SerialNumberMapper.xml / SerialNumberMapperEx.xml
    │   │       ├── SystemConfigMapper.xml / SystemConfigMapperEx.xml
    │   │       ├── PlatformConfigMapper.xml / PlatformConfigMapperEx.xml
    │   │       ├── UnitMapper.xml / UnitMapperEx.xml
    │   │       ├── UserBusinessMapper.xml / UserBusinessMapperEx.xml
    │   │       ├── InOutItemMapper.xml / InOutItemMapperEx.xml
    │   │       ├── OrgaUserRelMapper.xml / OrgaUserRelMapperEx.xml
    │   │       └── SequenceMapperEx.xml
    │   │
    │   └── java/com/jsh/erp/           # ★ Java 源代码主目录
    │       ├── ErpApplication.java      # 启动类（@SpringBootApplication）
    │       │
    │       ├── base/                    # ★ 基础类（5 个）
    │       │   ├── AjaxResult.java      #   统一响应体
    │       │   ├── BaseController.java  #   Controller 基类
    │       │   ├── PageDomain.java      #   分页请求参数封装
    │       │   ├── TableDataInfo.java   #   分页响应体（rows + total）
    │       │   └── TableSupport.java    #   分页参数工具
    │       │
    │       ├── config/                  # ★ 配置类（4 个）
    │       │   ├── Swagger2Config.java  #   Swagger API 文档配置
    │       │   ├── TenantConfig.java    #   多租户 SQL 拦截器配置
    │       │   ├── PluginBeanConfig.java    #   插件框架 Bean 配置
    │       │   └── PluginConfiguration.java #   插件运行模式配置
    │       │
    │       ├── constants/               # ★ 常量定义（2 个）
    │       │   ├── BusinessConstants.java   #   业务常量（会话时长、默认角色等）
    │       │   └── ExceptionConstants.java  #   异常状态码常量
    │       │
    │       ├── controller/              # ★ REST 控制器层（24 个）
    │       │   ├── UserController.java          #   用户管理与认证
    │       │   ├── RoleController.java          #   角色管理
    │       │   ├── FunctionController.java      #   功能/菜单管理
    │       │   ├── UserBusinessController.java  #   用户-角色-功能关系
    │       │   ├── TenantController.java        #   租户管理
    │       │   ├── OrganizationController.java  #   机构管理
    │       │   ├── PersonController.java        #   经手人管理
    │       │   ├── DepotController.java         #   仓库管理
    │       │   ├── MaterialController.java      #   商品管理
    │       │   ├── MaterialCategoryController.java  # 商品分类
    │       │   ├── MaterialExtendController.java    # 商品扩展信息
    │       │   ├── MaterialAttributeController.java # 商品多属性
    │       │   ├── MaterialPropertyController.java  # 商品属性值
    │       │   ├── SupplierController.java      #   供应商/客户管理
    │       │   ├── DepotHeadController.java     #   ★ 单据头管理（进销存核心）
    │       │   ├── DepotItemController.java     #   ★ 单据明细与报表
    │       │   ├── AccountController.java       #   账户管理
    │       │   ├── AccountHeadController.java   #   财务单据头
    │       │   ├── AccountItemController.java   #   财务单据明细
    │       │   ├── SystemConfigController.java  #   系统配置
    │       │   ├── PlatformConfigController.java #  平台配置
    │       │   ├── SerialNumberController.java  #   序列号管理
    │       │   ├── SequenceController.java      #   单据编号生成
    │       │   ├── UnitController.java          #   计量单位管理
    │       │   ├── LogController.java           #   操作日志
    │       │   ├── MsgController.java           #   站内消息
    │       │   ├── InOutItemController.java     #   收支项目管理
    │       │   └── PluginController.java        #   插件管理
    │       │
    │       ├── datasource/              # ★ 数据源层
    │       │   ├── entities/            #   实体/VO/Example（78 个）
    │       │   │   ├── 核心实体 ──────────────────────────────────
    │       │   │   ├── User.java / UserEx.java / UserExample.java
    │       │   │   ├── Role.java / RoleEx.java / RoleExample.java
    │       │   │   ├── Function.java / FunctionEx.java / FunctionExample.java
    │       │   │   ├── UserBusiness.java / UserBusinessExample.java
    │       │   │   ├── Tenant.java / TenantEx.java / TenantExample.java
    │       │   │   ├── Organization.java / OrganizationExample.java
    │       │   │   ├── OrgaUserRel.java / OrgaUserRelEx.java / OrgaUserRelExample.java
    │       │   │   ├── Person.java / PersonExample.java
    │       │   │   ├── 仓库与库存 ────────────────────────────────
    │       │   │   ├── Depot.java / DepotEx.java / DepotExample.java
    │       │   │   ├── DepotHead.java / DepotHeadExample.java
    │       │   │   ├── DepotHeadVo4Body.java
    │       │   │   ├── DepotItem.java / DepotItemExample.java
    │       │   │   ├── DepotItemVo4WithInfoEx.java / DepotItemVo4Material.java
    │       │   │   ├── DepotItemVo4DetailByTypeAndMId.java
    │       │   │   ├── DepotItemVo4MaterialAndSum.java
    │       │   │   ├── MaterialCurrentStock.java / MaterialCurrentStockExample.java
    │       │   │   ├── MaterialInitialStock.java / MaterialInitialStockExample.java
    │       │   │   ├── MaterialInitialStockWithMaterial.java
    │       │   │   ├── 商品管理 ──────────────────────────────────
    │       │   │   ├── Material.java / MaterialExample.java / MaterialVo4Unit.java
    │       │   │   ├── MaterialWithInitStock.java
    │       │   │   ├── MaterialExtend.java / MaterialExtendExample.java
    │       │   │   ├── MaterialCategory.java / MaterialCategoryExample.java
    │       │   │   ├── MaterialAttribute.java / MaterialAttributeExample.java
    │       │   │   ├── MaterialProperty.java / MaterialPropertyExample.java
    │       │   │   ├── 供应商/客户 ───────────────────────────────
    │       │   │   ├── Supplier.java / SupplierExample.java
    │       │   │   ├── 财务相关 ──────────────────────────────────
    │       │   │   ├── Account.java / AccountExample.java / AccountVo4Sum.java
    │       │   │   ├── AccountHead.java / AccountHeadExample.java
    │       │   │   ├── AccountHeadVo4Body.java / AccountHeadVo4ListEx.java
    │       │   │   ├── AccountItem.java / AccountItemExample.java
    │       │   │   ├── 系统配置 ──────────────────────────────────
    │       │   │   ├── SystemConfig.java / SystemConfigExample.java
    │       │   │   ├── PlatformConfig.java / PlatformConfigExample.java
    │       │   │   ├── Log.java / LogExample.java
    │       │   │   ├── Msg.java / MsgEx.java / MsgExample.java
    │       │   │   ├── SerialNumber.java / SerialNumberEx.java / SerialNumberExample.java
    │       │   │   ├── InOutItem.java / InOutItemExample.java
    │       │   │   ├── Unit.java / UnitExample.java
    │       │   │   └── SysLoginModel.java
    │       │   │
    │       │   ├── mappers/             #   Mapper 接口（59 个，双 Mapper 模式）
    │       │   │   ├── XxxMapper.java           # 标准 CRUD（MyBatis Generator 生成）
    │       │   │   └── XxxMapperEx.java         # 自定义复杂查询（配合自定义 XML）
    │       │   │
    │       │   └── vo/                  #   视图对象（25 个）
    │       │       ├── DepotHeadVo4List.java
    │       │       ├── DepotHeadVo4InDetail.java
    │       │       ├── DepotHeadVo4InOutMCount.java
    │       │       ├── DepotHeadVo4StatementAccount.java
    │       │       ├── DepotItemVo4Stock.java
    │       │       ├── DepotItemStockWarningCount.java
    │       │       ├── DepotItemVoBatchNumberList.java
    │       │       ├── MaterialDepotStock.java
    │       │       ├── MaterialExtendVo4List.java
    │       │       ├── MaterialsListVo.java
    │       │       ├── MaterialVoSearch.java
    │       │       ├── MaterialCountVo.java
    │       │       ├── InOutPriceVo.java
    │       │       ├── FinishDepositVo.java
    │       │       ├── AiPredictionGenerateItem.java
    │       │       ├── AiPredictionSaveItem.java
    │       │       ├── BillListCacheVo.java
    │       │       ├── AccountItemVo4List.java
    │       │       ├── AccountVo4InOutList.java
    │       │       ├── AccountVo4List.java
    │       │       ├── LogVo4List.java
    │       │       ├── SupplierSimple.java
    │       │       ├── TreeNode.java / TreeNodeEx.java
    │       │       └── NodeAttributes.java
    │       │
    │       ├── exception/               # ★ 异常处理（5 个）
    │       │   ├── GlobalExceptionHandler.java       # 全局异常拦截器
    │       │   ├── BusinessParamCheckingException.java # 业务参数校验异常
    │       │   ├── BusinessRunTimeException.java      # 业务运行时异常
    │       │   ├── JshException.java                  # 通用异常工具
    │       │   └── UtilException.java                 # 工具异常
    │       │
    │       ├── filter/                  # ★ 请求过滤器（1 个）
    │       │   └── LogCostFilter.java   #   登录校验 + 路径安全过滤
    │       │
    │       ├── service/                 # ★ 业务服务层（31 个）
    │       │   ├── 核心业务 ──────────────────────────────────────
    │       │   ├── UserService.java           # 用户注册/登录/Token 管理
    │       │   ├── RoleService.java           # 角色管理
    │       │   ├── FunctionService.java       # 功能/菜单/按钮权限
    │       │   ├── UserBusinessService.java   # 用户-角色-功能关联
    │       │   ├── TenantService.java         # 租户管理
    │       │   ├── OrganizationService.java   # 机构管理
    │       │   ├── OrgaUserRelService.java    # 机构-用户关系
    │       │   ├── PersonService.java         # 经手人管理
    │       │   ├── 仓库与单据 ────────────────────────────────────
    │       │   ├── DepotService.java          # 仓库管理
    │       │   ├── DepotHeadService.java      # ★ 单据头（进销存核心）
    │       │   ├── DepotItemService.java      # ★ 单据明细与库存变动
    │       │   ├── 商品管理 ──────────────────────────────────────
    │       │   ├── MaterialService.java       # 商品主数据管理
    │       │   ├── MaterialExtendService.java # 商品扩展信息（条码/价格）
    │       │   ├── MaterialCategoryService.java   # 商品分类（树形）
    │       │   ├── MaterialAttributeService.java  # 商品多属性
    │       │   ├── MaterialPropertyService.java   # 商品属性值
    │       │   ├── 供应商/客户 ───────────────────────────────────
    │       │   ├── SupplierService.java       # 供应商/客户管理
    │       │   ├── 财务相关 ──────────────────────────────────────
    │       │   ├── AccountService.java        # 账户管理
    │       │   ├── AccountHeadService.java    # 财务单据头
    │       │   ├── AccountItemService.java    # 财务单据明细
    │       │   ├── InOutItemService.java      # 收支项目
    │       │   ├── 辅助服务 ──────────────────────────────────────
    │       │   ├── SerialNumberService.java   # 序列号管理
    │       │   ├── SequenceService.java       # 单据编号序列生成
    │       │   ├── UnitService.java           # 计量单位
    │       │   ├── LogService.java            # 操作日志
    │       │   ├── MsgService.java            # 站内消息
    │       │   ├── SystemConfigService.java   # 系统配置
    │       │   ├── PlatformConfigService.java # 平台配置
    │       │   ├── RedisService.java          # ★ Redis 会话管理
    │       │   ├── AiPredictionService.java   # ★ AI 预测（DeepSeek API）
    │       │   └── FeatureSwitchService.java  # 功能开关（毕设/商业模式）
    │       │
    │       └── utils/                   # ★ 工具类（28 个）
    │           ├── AnnotationUtils.java       # 注解工具
    │           ├── BaseResponseInfo.java      # 统一响应体（code + data）
    │           ├── ColumnPropertyUtil.java     # 列属性工具
    │           ├── ComputerInfo.java          # 机器信息工具
    │           ├── Constants.java             # 通用常量
    │           ├── Convert.java               # 类型转换工具
    │           ├── ErpInfo.java               # ERP 信息常量
    │           ├── ExcelUtils.java            # ★ Excel 导入导出工具
    │           ├── ExceptionCodeConstants.java # 异常编码常量
    │           ├── ExtJsonUtils.java          # JSON 扩展工具
    │           ├── FileUtils.java             # 文件操作工具
    │           ├── HttpClient.java            # HTTP 客户端工具
    │           ├── JsonUtils.java             # JSON 序列化工具
    │           ├── OrderUtils.java            # 单据操作工具
    │           ├── PageQueryInfo.java         # 分页查询信息
    │           ├── PageUtils.java             # 分页工具
    │           ├── ParamUtils.java            # 参数处理工具
    │           ├── PermissionUtil.java        # ★ 权限判断工具
    │           ├── PinYinUtil.java            # 汉字转拼音
    │           ├── QueryUtils.java            # 查询条件构建工具
    │           ├── RandImageUtil.java         # 验证码图片生成
    │           ├── RegExpTools.java           # 正则表达式工具
    │           ├── ResponseCode.java          # 响应状态码枚举
    │           ├── ResponseJsonUtil.java       # 响应 JSON 工具
    │           ├── ServletUtils.java          # Servlet 工具
    │           ├── SqlUtil.java               # SQL 拼接工具
    │           ├── StringUtil.java            # ★ 字符串工具（判空/截取）
    │           └── Tools.java                 # ★ 通用工具（Token 解析/日期等）
    │
    └── test/                            # 测试代码
        ├── java/                        # 测试类
        └── resources/
            └── generatorConfig.xml      # MyBatis Generator 配置
```

> **统计**：约 280+ Java 文件，覆盖 base(5) · config(4) · constants(2) · controller(24) · datasource/entities(78) · datasource/mappers(59) · datasource/vo(25) · exception(5) · filter(1) · service(31) · utils(28) 十一个顶级模块。Mapper XML 60 个，SQL 初始化脚本 2 个。

## 4. 应用启动与运行流程

### 4.1 启动流程

1. [ErpApplication.java](../src/main/java/com/jsh/erp/ErpApplication.java) 通过 `@SpringBootApplication` 启动 Spring 容器。
2. `@MapperScan("com.jsh.erp.datasource.mappers")` 扫描并注册所有 MyBatis Mapper。
3. `@ServletComponentScan` 注册 `LogCostFilter`（`@WebFilter`）。
4. `@EnableScheduling` / `@EnableAsync` 启用定时任务与异步能力。
5. `TenantConfig` 中的 `PaginationInterceptor` 注册多租户 SQL 解析器与分页拦截器。
6. `PluginConfiguration` + `PluginBeanConfig` 初始化插件框架，扫描 `plugins/` 目录加载插件。
7. 服务启动完成，监听端口 `9999`，上下文路径 `/AIWMS-boot`。

### 4.2 请求处理流程

```
客户端请求
  │
  ▼
LogCostFilter（拦截所有 /* 请求）
  ├─ 路径遍历攻击检测 → 拒绝
  ├─ Redis 中存在 userId → 放行
  ├─ 白名单 URL → 放行（/user/login, /user/register, /doc.html 等）
  └─ 未登录 → 返回 500 + "loginOut"
  │
  ▼
TenantConfig.PaginationInterceptor
  ├─ 从 X-Access-Token 解析 tenantId
  ├─ TenantSqlParser 自动注入 WHERE tenant_id = ?
  └─ 系统表（jsh_sequence, jsh_function 等）跳过租户过滤
  │
  ▼
Controller → Service → Mapper → MySQL
  │
  ▼
GlobalExceptionHandler（统一异常处理）
  ├─ BusinessParamCheckingException → 返回业务错误码
  ├─ BusinessRunTimeException → 返回业务错误码
  └─ 其他 Exception → 返回 500 系统错误
  │
  ▼
BaseResponseInfo { code, data } → 客户端
```

### 4.3 核心配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `server.port` | `9999` | 服务端口 |
| `server.servlet.context-path` | `/AIWMS-boot` | 上下文路径 |
| `spring.datasource.url` | `jdbc:mysql://127.0.0.1:3306/jsh_erp` | 数据库连接 |
| `spring.datasource.username` | `root` | 数据库用户 |
| `spring.datasource.password` | 环境变量 `MYSQL_PASSWORD` | 数据库密码 |
| `spring.redis.host` | `127.0.0.1` | Redis 地址 |
| `spring.redis.port` | `6379` | Redis 端口 |
| `manage.roleId` | `10` | 管理员角色 ID |
| `tenant.userNumLimit` | `1000000` | 租户用户数上限 |
| `file.uploadType` | `1`（本地） | 文件上传方式（1=本地，2=OSS） |
| `file.path` | `/opt/AIWMS/upload` | 本地上传路径 |
| `biz.graduationMode` | `false` | 毕设模式开关 |
| `ai.deepseek.api-url` | `https://api.deepseek.com/chat/completions` | AI 接口地址 |
| `ai.deepseek.api-key` | 环境变量 `DEEPSEEK_API_KEY` | AI 接口密钥 |
| `ai.deepseek.model` | `deepseek-chat` | AI 模型名称 |
| `ai.deepseek.timeout-millis` | `30000` | AI 请求超时（ms） |
| `plugin.runMode` | `prod` | 插件运行模式 |
| `plugin.pluginPath` | `plugins` | 插件目录 |

## 5. 数据库与 ORM 层

### 5.1 数据库设计

- 数据库：MySQL 8，库名 `jsh_erp`。
- 表前缀：`jsh_`，核心业务表约 30 张。
- 初始数据：[docs/jsh_erp.sql](../docs/jsh_erp.sql)
- AI 预测增量：[docs/sql_material_ai_prediction.sql](../docs/sql_material_ai_prediction.sql)

核心表结构：

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `jsh_user` | 用户表 | id, login_name, username, password, tenant_id |
| `jsh_role` | 角色表 | id, name, tenant_id |
| `jsh_function` | 功能/菜单表 | id, name, url, component, p_number, sort |
| `jsh_user_business` | 用户-角色-功能关联 | id, type, key_id, value, btn_str |
| `jsh_tenant` | 租户表 | id, name, status |
| `jsh_organization` | 机构表 | id, name, p_id, type |
| `jsh_orga_user_rel` | 机构-用户关系 | id, orga_id, user_id |
| `jsh_depot` | 仓库表 | id, name, is_default |
| `jsh_depot_head` | 单据头表 | id, number, type, sub_type, status, tenant_id |
| `jsh_depot_item` | 单据明细表 | id, depot_head_id, material_id, depot_id, oper_number |
| `jsh_material` | 商品主表 | id, name, model, standard, bar_code |
| `jsh_material_extend` | 商品扩展信息 | id, material_id, depot_id, current_stock, current_price |
| `jsh_material_category` | 商品分类 | id, name, p_id, sort |
| `jsh_material_attribute` | 商品多属性 | id, name |
| `jsh_material_property` | 商品属性值 | id, attribute_id, value |
| `jsh_material_current_stock` | 当前库存 | id, material_id, depot_id, current_number |
| `jsh_material_initial_stock` | 期初库存 | id, material_id, depot_id, initial_number |
| `jsh_supplier` | 供应商/客户 | id, name, type, contacts |
| `jsh_account` | 账户表 | id, name, is_default |
| `jsh_account_head` | 财务单据头 | id, number, type, status |
| `jsh_account_item` | 财务单据明细 | id, account_head_id, depot_head_id |
| `jsh_in_out_item` | 收支项目 | id, name, type |
| `jsh_serial_number` | 序列号 | id, material_id, serial_number |
| `jsh_unit` | 计量单位 | id, name |
| `jsh_log` | 操作日志 | id, user_id, content, ip |
| `jsh_msg` | 站内消息 | id, user_id, content, status |
| `jsh_system_config` | 系统配置 | id, key, value |
| `jsh_platform_config` | 平台配置 | id, key, value |
| `jsh_person` | 经手人 | id, name, type |
| `jsh_sequence` | 序列号生成 | id, value |

### 5.2 ORM 框架

采用 **MyBatis-Plus 3.0.7.1**（非 JPA），配合 **MyBatis Generator** 生成基础代码。

**双 Mapper 模式**：

| 类型 | 说明 | 示例 |
|------|------|------|
| `XxxMapper.java` | 标准 CRUD（Generator 生成），配合 `XxxExample` | `MaterialMapper.selectByExample()` |
| `XxxMapperEx.java` | 自定义复杂查询（手写），配合自定义 XML | `MaterialMapperEx.getMaterialByBarCode()` |

- Mapper XML 位置：`src/main/resources/mapper_xml/*.xml`（共 60 个）
- 代码生成配置：`src/test/resources/generatorConfig.xml`
- 多租户 SQL 拦截：`TenantConfig` 中 `TenantSqlParser` 自动注入 `WHERE tenant_id = ?`
- 分页：`PaginationInterceptor`（MyBatis-Plus 内置）+ `PageHelper`

### 5.3 多租户架构

`TenantConfig` 通过 MyBatis-Plus 的 `TenantSqlParser` 实现行级租户隔离：

1. 从请求头 `X-Access-Token` 解析 `tenantId`。
2. 对所有非系统表自动注入 `WHERE tenant_id = ?`。
3. 系统表（`jsh_sequence`、`jsh_function`、`jsh_platform_config`、`jsh_tenant`）跳过租户过滤。
4. 超管（`tenantId=0`）不做租户约束。

## 6. 控制器层（Controller）

### 6.1 统一设计模式

所有 Controller 均遵循标准 CRUD 模式：

| HTTP 方法 | 路径模式 | 说明 |
|-----------|----------|------|
| `GET` | `/{entity}/info?id=` | 查询单条详情 |
| `GET` | `/{entity}/list?currentPage=&pageSize=&search=` | 分页列表查询 |
| `POST` | `/{entity}/add` | 新增 |
| `PUT` | `/{entity}/update` | 编辑 |
| `DELETE` | `/{entity}/delete?id=` | 删除单条 |
| `DELETE` | `/{entity}/deleteBatch` | 批量删除 |
| `GET` | `/{entity}/checkIsNameExist?name=` | 名称唯一性校验 |
| `POST` | `/{entity}/batchSetStatus` | 批量启停用 |

响应格式统一为 `BaseResponseInfo`：

```json
{
  "code": 200,
  "data": {
    "rows": [],
    "total": 100
  }
}
```

### 6.2 控制器清单

#### 用户与权限模块

| Controller | 路径前缀 | 核心接口 |
|------------|----------|----------|
| `UserController` | `/user` | `login` · `logout` · `registerUser` · `randomImage` · `getUserSession` · `addUser` · `updateUser` · `getUserList` · `getUserBtnByCurrentUser` · `resetPwd` · `batchSetStatus` · `getUserWithChecked` |
| `RoleController` | `/role` | `add` · `update` · `allList` · `checkIsNameExist` · `list` · `delete` · `deleteBatch` · `batchSetStatus` |
| `FunctionController` | `/function` | `add` · `update` · `checkIsNameExist` · `checkIsNumberExist` · `findMenuByPNumber` · `findRoleFunction` · `findRoleFunctionsById` · `list` · `delete` · `deleteBatch` |
| `UserBusinessController` | `/userBusiness` | `add` · `update` · `checkIsValueExist` · `updateBtnStr` · `updateOneValueByKeyIdAndType` |
| `TenantController` | `/tenant` | `add` · `update` · `checkIsNameExist` · `list` · `delete` · `deleteBatch` · `batchSetStatus` |
| `OrganizationController` | `/organization` | `getOrganizationTree` · `getAllOrganizationTreeByUser` |

#### 基础资料模块

| Controller | 路径前缀 | 核心接口 |
|------------|----------|----------|
| `DepotController` | `/depot` | `add` · `update` · `checkIsNameExist` · `list` · `delete` · `deleteBatch` · `updateIsDefault` · `batchSetStatus` · `findDepotByCurrentUser` · `findUserDepot` · `getAllListWithStock` |
| `PersonController` | `/person` | `add` · `update` · `checkIsNameExist` · `getPersonByNumType` · `list` · `delete` · `deleteBatch` · `batchSetStatus` |
| `AccountController` | `/account` | `add` · `update` · `checkIsNameExist` · `getAccount` · `list` · `delete` · `deleteBatch` · `updateIsDefault` · `batchSetStatus` · `findBySelect` · `findAccountInOutList` · `listWithBalance` · `getStatistics` |
| `SupplierController` | `/supplier` | `add` · `update` · `checkIsNameAndTypeExist` · `findBySelect_sup/cus/retail/organ` · `list` · `delete` · `deleteBatch` · `importVendor` · `importCustomer` · `exportExcel` · `batchSetStatus` · `getAllCustomer` · `getUserCustomerValue` |
| `UnitController` | `/unit` | `add` · `getAllList` · `list` · `delete` · `deleteBatch` |

#### 商品管理模块

| Controller | 路径前缀 | 核心接口 |
|------------|----------|----------|
| `MaterialController` | `/material` | `add` · `update` · `checkIsExist` · `findBySelect` · `getMaterialByParam` · `getMaterialByBarCode` · `getMaxBarCode` · `batchUpdate` · `changeNameToPinYin` · `list` · `delete` · `deleteBatch` · `importExcel` · `exportExcel` · `batchSetStatus` · `batchSetMaterialCurrentStock` · `batchSetMaterialCurrentUnitPrice` · `getListWithStock` · `getMaterialDepotStock` · `findById` · `findByIdWithBarCode` · `getMaterialByMeId` · `getMaterialEnableSerialNumberList` · `getMaterialNameList` |
| `MaterialCategoryController` | `/materialCategory` | `getMaterialCategoryTree` · `findById` · `checkIsNameExist` · `add` · `update` · `list` · `delete` · `deleteBatch` |
| `MaterialExtendController` | `/materialsExtend` | `checkIsBarCodeExist` · `getDetailList` · `getInfoByBarCode` |
| `MaterialAttributeController` | `/materialAttribute` | `add` · `update` · `checkIsNameExist` · `getNameList` · `getValueListById` · `list` · `delete` · `deleteBatch` |
| `MaterialPropertyController` | `/materialProperty` | `addOrUpdate` · `list` · `delete` · `deleteBatch` · `getAllList` |

#### ★ 单据管理模块（核心业务）

| Controller | 路径前缀 | 核心接口 |
|------------|----------|----------|
| `DepotHeadController` | `/depotHead` | `addDepotHeadAndDetail` · `updateDepotHeadAndDetail` · `getDetailByNumber` · `list` · `delete` · `deleteBatch` · `batchSetStatus`（审核/反审核） · `getBuyAndSaleStatistics` · `waitBillCount` · `waitBillList` · `getBillListByLinkNumber` · `batchAddDepotHeadAndDetail` · `forceCloseBatch` · `findInOutDetail` · `findInOutMaterialCount` · `findAllocationDetail` · `getStatementAccount` · `getNeedCount` · `getCreatorByCurrentUser` · `debtList` · `debtExport` · `forceClosePurchaseBatch` |
| `DepotItemController` | `/depotItem` | `buyOrSalePrice` · `findStockByDepotAndBarCode` · `getBatchNumberList` · `getDetailList` · `findDetailByDepotIdsAndMaterialId` · `getMaterialDepotStockByParam` · `findStockWarningCount` · `generateAiPrediction` · `saveAiPrediction` · `buyIn` · `saleOut` · `retailOut` · `getInOutStock` · `getInOutStockCountMoney` · `importItemExcel` |

单据类型说明（通过 `type` + `subType` 区分）：

| 类型 | 说明 | 前端对应 |
|------|------|----------|
| `入库` / `采购` | 采购入库 | `PurchaseInList.vue` |
| `入库` / `采购退货` | 采购退货 | `PurchaseBackList.vue` |
| `出库` / `销售` | 销售出库 | `SaleOutList.vue` |
| `出库` / `销售退货` | 销售退货 | `SaleBackList.vue` |
| `订单` / `采购` | 采购订单 | `PurchaseOrderList.vue` |
| `订单` / `销售` | 销售订单 | `SaleOrderList.vue` |
| `入库` / `其他` | 其他入库 | `OtherInList.vue` |
| `出库` / `其他` | 其他出库 | `OtherOutList.vue` |
| `出库` / `调拨` | 调拨出库 | `AllocationOutList.vue` |

#### 系统配置模块

| Controller | 路径前缀 | 核心接口 |
|------------|----------|----------|
| `SystemConfigController` | `/systemConfig` | `add` · `update` · `getCurrentInfo` · `fileSizeLimit` · `list` · `exportExcelByParam` · `upload` · `static/**` |
| `PlatformConfigController` | `/platformConfig` | `getInfoByKey` · `getPlatform/checkcodeFlag` · `getPlatform/name` · `getPlatform/url` · `getPlatform/registerFlag` · `updatePlatformConfigByKey` |
| `LogController` | `/log` | `list` |
| `SerialNumberController` | `/serialNumber` | `getEnableSerialNumberList` · `batAddSerialNumber` |
| `SequenceController` | `/sequence` | `buildNumber` |
| `PluginController` | `/plugin` | `list` · `files` · `stop/{id}` · `start/{id}` · `uninstall/{id}` · `installByPath` · `uploadInstallPluginJar` · `uploadPluginConfigFile` · `back/{pluginId}` · `getMacWithSecret` · `checkByPluginId` |

#### 财务模块（保留但前端裁剪）

| Controller | 路径前缀 | 核心接口 |
|------------|----------|----------|
| `AccountHeadController` | `/accountHead` | `addAccountHeadAndDetail` · `updateAccountHeadAndDetail` · `getDetailByNumber` · `getFinancialBillNoByBillId` |
| `AccountItemController` | `/accountItem` | `getDetailList` |
| `MsgController` | `/msg` | `getMsgByStatus` · `batchUpdateStatus` · `getMsgCountByStatus` · `getMsgCountByType` · `readAllMsg` |
| `InOutItemController` | `/inOutItem` | 完整 CRUD + `findBySelect` + `batchSetStatus` |

## 7. 服务层（Service）

### 7.1 核心业务服务

#### 用户认证与会话（UserService + RedisService）

- 登录流程：用户名/密码校验 → 生成 Token → 存储到 Redis（Hash 结构，key=token, field=userId）
- Token 机制：请求头 `X-Access-Token` → Redis 查询 `userId` → 过期自动续期
- 会话有效期：`BusinessConstants.MAX_SESSION_IN_SECONDS`（默认 7 天）
- 密码加密：前端 MD5 → 后端 MD5 校验

#### 单据核心逻辑（DepotHeadService + DepotItemService）

- 新增单据：事务内同时写入 `jsh_depot_head` 和 `jsh_depot_item`
- 审核/反审核：更新单据状态，同时更新 `jsh_material_current_stock`（当前库存）
- 库存变动：审核时按 `oper_number`（入库为正、出库为负）更新库存
- 关联单据：通过 `link_number` 关联采购订单与采购入库单等
- 批量操作：`batchAddDepotHeadAndDetail` 支持批量导入

#### AI 智能预测（AiPredictionService）

- 接口：`DeepSeek API`（`https://api.deepseek.com/chat/completions`）
- 模型：`deepseek-chat`，超时 30 秒
- 流程：构建物料/仓库上下文 → 调用 AI 生成预测 → 解析 JSON 结果 → 保存到数据库
- 兜底：AI 调用失败时使用规则计算建议数量
- 预测结果字段：`forecastQty`（预测数量）、`suggestQty`（建议数量）、`aiAnalysis`（AI 分析说明）

#### 功能开关（FeatureSwitchService）

- 配置项：`biz.graduationMode`（毕设模式开关）
- 毕设模式（`true`）：关闭商业化入口
- 商业模式（`false`）：完整功能

### 7.2 服务清单

| 服务类 | 职责 | 依赖 |
|--------|------|------|
| `UserService` | 用户注册/登录/角色分配，Token 会话管理 | `RedisService` |
| `RoleService` | 角色 CRUD 与状态管理 | — |
| `FunctionService` | 菜单/功能/按钮权限树构建 | `UserBusinessService` |
| `UserBusinessService` | 用户-角色-功能关联维护 | — |
| `TenantService` | 租户 CRUD 与状态管理 | — |
| `OrganizationService` | 部门/组织树管理 | `OrgaUserRelService` |
| `OrgaUserRelService` | 机构-用户关系维护 | — |
| `PersonService` | 经手人 CRUD 与状态管理 | — |
| `DepotService` | 仓库 CRUD 与用户仓库权限 | `UserBusinessService` |
| `DepotHeadService` | ★ 单据头 CRUD、审核/反审核、报表查询 | `DepotItemService`, `SequenceService` |
| `DepotItemService` | ★ 单据明细、库存变动、报表统计、AI 预测保存 | `MaterialExtendService`, `AiPredictionService` |
| `MaterialService` | 商品主数据 CRUD、导入导出、拼音转换 | `MaterialExtendService` |
| `MaterialExtendService` | 商品扩展信息（条码/价格/库存） | — |
| `MaterialCategoryService` | 商品分类树形结构管理 | — |
| `MaterialAttributeService` | 商品多属性管理 | `MaterialPropertyService` |
| `MaterialPropertyService` | 商品属性值管理 | — |
| `SupplierService` | 供应商/客户 CRUD、导入导出 | — |
| `AccountService` | 账户 CRUD 与默认账户管理 | — |
| `AccountHeadService` | 财务单据头 CRUD | `AccountItemService` |
| `AccountItemService` | 财务单据明细 CRUD | — |
| `InOutItemService` | 收支项目 CRUD | — |
| `SerialNumberService` | 序列号管理 | — |
| `SequenceService` | 单据编号序列生成（自增） | — |
| `UnitService` | 计量单位 CRUD | — |
| `LogService` | 操作日志记录与查询 | — |
| `MsgService` | 站内消息管理 | — |
| `SystemConfigService` | 系统配置 CRUD | — |
| `PlatformConfigService` | 平台配置 CRUD | — |
| `RedisService` | ★ Redis 会话管理（Token ↔ userId 映射） | — |
| `AiPredictionService` | ★ AI 预测（调用 DeepSeek API） | `DepotItemService` |
| `FeatureSwitchService` | 功能开关（毕设/商业模式） | — |

## 8. 权限与安全

### 8.1 认证机制

未使用 Spring Security，采用**自定义 Filter + Redis Token** 方案：

- [LogCostFilter.java](../src/main/java/com/jsh/erp/filter/LogCostFilter.java)：`@WebFilter(urlPatterns = "/*")` 拦截所有请求。
- 白名单 URL：`/user/login`、`/user/register`、`/user/registerUser`、`/user/randomImage`、`/doc.html` 等。
- Token 校验：从请求头读取 `X-Access-Token` → Redis 查询 `userId` → 存在则放行。
- 路径安全：检测 `..`、`%2e`、`%2E` 等路径遍历攻击。
- 未认证请求返回 `500` + body `"loginOut"`。

### 8.2 权限模型

基于 `jsh_user_business` 表的三级权限体系：

| 层级 | type 值 | 说明 |
|------|---------|------|
| 用户-角色 | `UserRole` | 用户与角色的关联 |
| 角色-功能 | `RoleFunctions` | 角色与菜单/功能的关联 |
| 用户-仓库 | `UserDepot` | 用户可访问的仓库权限 |
| 用户-客户 | `UserCustomer` | 用户可访问的客户权限 |

权限判断逻辑（`PermissionUtil`）：
- 超管（`roleId=10`）拥有所有权限。
- 普通用户通过 `UserBusiness` 表查询可见菜单和按钮。
- 按钮级权限：`jsh_user_business.btn_str` 存储 JSON 数组，格式为 `[{funId: 1, btnStr: "add,edit,delete"}]`。

### 8.3 前后端鉴权对齐

| 项 | 前端 | 后端 |
|----|------|------|
| Token 存储 | localStorage `pro__Access-Token` | Redis Hash（key=token） |
| 请求头 | `X-Access-Token` | `LogCostFilter` 读取 `X-Access-Token` |
| 登录状态判断 | `Vue.ls.get('Login_userId')` | `RedisService.getObjectFromSessionByKey("userId")` |
| 会话过期 | 7 天 | `MAX_SESSION_IN_SECONDS` |

## 9. 异常处理

### 9.1 全局异常拦截

[GlobalExceptionHandler.java](../src/main/java/com/jsh/erp/exception/GlobalExceptionHandler.java) 通过 `@RestControllerAdvice` 统一处理异常：

| 异常类型 | 返回码 | 说明 |
|----------|--------|------|
| `BusinessParamCheckingException` | 自定义业务码 | 参数校验失败 |
| `BusinessRunTimeException` | 自定义业务码 | 业务逻辑异常 |
| `Exception`（兜底） | `500` | 系统内部错误 |

### 9.2 异常码常量

[ExceptionConstants.java](../src/main/java/com/jsh/erp/constants/ExceptionConstants.java) 定义了统一的异常状态码：

- `GLOBAL_RETURNS_CODE`：响应中的状态码字段名
- `GLOBAL_RETURNS_DATA`：响应中的数据字段名
- `SERVICE_SYSTEM_ERROR_CODE`：系统错误码
- `SERVICE_SYSTEM_ERROR_MSG`：系统错误提示

## 10. 插件化架构

### 10.1 插件框架

基于 `springboot-plugin-framework`（pf4j 封装）：

- [PluginConfiguration.java](../src/main/java/com/jsh/erp/config/PluginConfiguration.java)：配置插件路径与运行模式。
- [PluginBeanConfig.java](../src/main/java/com/jsh/erp/config/PluginBeanConfig.java)：注册 `AutoPluginApplication` + MyBatis 扩展。
- 插件 REST 前缀：`/api/plugin/{pluginId}/`
- 支持插件级 MyBatis Mapper 扩展。

### 10.2 插件管理接口

| 接口 | 说明 |
|------|------|
| `GET /plugin/list` | 获取插件信息列表 |
| `GET /plugin/files` | 获取插件 jar 文件名 |
| `POST /plugin/start/{id}` | 启动插件 |
| `POST /plugin/stop/{id}` | 停止插件 |
| `POST /plugin/uninstall/{id}` | 卸载插件 |
| `POST /plugin/installByPath` | 按路径安装插件 |
| `POST /plugin/uploadInstallPluginJar` | 上传安装插件 jar |
| `POST /plugin/uploadPluginConfigFile` | 上传插件配置文件 |
| `POST /plugin/back/{pluginId}` | 备份插件 |
| `GET /plugin/getMacWithSecret` | 获取 MAC 和密钥 |
| `GET /plugin/checkByPluginId` | 校验插件是否已启用 |

## 11. 工具类说明

| 工具类 | 职责 | 使用场景 |
|--------|------|----------|
| `Tools` | ★ Token 解析、日期格式化、租户 ID 获取 | 全局通用 |
| `StringUtil` | 字符串判空、截取、拼接 | 全局通用 |
| `PermissionUtil` | 权限判断（超管校验、功能 ID 映射） | Service 层 |
| `ExcelUtils` | Excel 导入导出（基于 JExcelAPI） | 商品/供应商导入导出 |
| `RandImageUtil` | 验证码图片生成 | 登录验证码 |
| `HttpClient` | HTTP 客户端封装 | AI API 调用 |
| `PinYinUtil` | 汉字转拼音（基于 pinyin4j） | 商品名称拼音码 |
| `OrderUtils` | 单据操作工具 | 单据审核/反审核 |
| `SqlUtil` | SQL 条件拼接 | 查询条件构建 |
| `QueryUtils` | 查询参数解析 | 列表查询 |
| `PageUtils` | 分页参数处理 | 分页查询 |
| `JsonUtils` | JSON 序列化/反序列化 | 全局通用 |
| `ExtJsonUtils` | JSON 扩展操作 | 配置解析 |
| `FileUtils` | 文件操作（上传/下载/删除） | 文件管理 |
| `ResponseJsonUtil` | 响应 JSON 构建 | Controller 层 |
| `BaseResponseInfo` | 统一响应体（code + data） | Controller 层 |
| `ServletUtils` | Servlet 请求工具 | 参数获取 |
| `ParamUtils` | 参数类型转换 | Controller 层 |
| `Convert` | 基础类型转换 | 全局通用 |
| `RegExpTools` | 正则表达式校验 | 参数校验 |
| `AnnotationUtils` | 注解反射工具 | 框架内部 |
| `ColumnPropertyUtil` | 列属性映射 | Excel 导出 |
| `ComputerInfo` | 机器信息获取 | 注册激活 |
| `Constants` | 通用常量 | 全局通用 |
| `ErpInfo` | ERP 信息常量 | 全局通用 |
| `ExceptionCodeConstants` | 异常编码常量 | 异常处理 |
| `ResponseCode` | 响应状态码枚举 | Controller 层 |
| `PageQueryInfo` | 分页查询信息封装 | 分页查询 |

## 12. 构建与部署

### 12.1 Maven 构建

```bash
# 编译打包（跳过测试）
mvn clean package -DskipTests

# 仅运行测试
mvn test

# 代码生成（MyBatis Generator）
mvn mybatis-generator:generate
```

构建输出：
- `target/jshERP.jar`：可执行 JAR（Spring Boot Fat JAR）
- `dist/`：Assembly 打包输出（含启动脚本）

### 12.2 部署方式

```bash
# 直接运行
java -jar jshERP.jar

# 指定配置（环境变量覆盖）
MYSQL_URL=jdbc:mysql://... MYSQL_PASSWORD=xxx REDIS_HOST=... java -jar jshERP.jar

# 使用启动脚本
chmod +x bin/start.sh
./bin/start.sh

# 停止服务
./bin/stop.sh

# 重启服务
./bin/restart.sh
```

### 12.3 环境变量

| 环境变量 | 说明 | 默认值 |
|----------|------|--------|
| `MYSQL_URL` | 数据库连接 URL | `jdbc:mysql://127.0.0.1:3306/jsh_erp?...` |
| `MYSQL_USERNAME` | 数据库用户名 | `root` |
| `MYSQL_PASSWORD` | 数据库密码 | （需配置） |
| `REDIS_HOST` | Redis 地址 | `127.0.0.1` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `REDIS_PASSWORD` | Redis 密码 | （空） |
| `FILE_UPLOAD_PATH` | 文件上传路径 | `/opt/AIWMS/upload` |
| `TOMCAT_BASEDIR` | Tomcat 临时目录 | `/opt/tmp/tomcat` |
| `DEEPSEEK_API_URL` | AI 接口地址 | `https://api.deepseek.com/chat/completions` |
| `DEEPSEEK_API_KEY` | AI 接口密钥 | （需配置） |
| `DEEPSEEK_MODEL` | AI 模型名称 | `deepseek-chat` |
| `DEEPSEEK_TIMEOUT_MILLIS` | AI 请求超时 | `30000` |

## 13. API 文档

### 13.1 Swagger 文档

启动服务后访问：
- Swagger UI：`http://localhost:9999/AIWMS-boot/swagger-ui.html`
- Bootstrap UI（增强）：`http://localhost:9999/AIWMS-boot/doc.html`

配置类：[Swagger2Config.java](../src/main/java/com/jsh/erp/config/Swagger2Config.java)

### 13.2 前后端接口对照

完整接口清单见前端文档 `frontend-architecture.md` 第 9.3 节，后端每个接口均可在对应的 Controller 中找到实现。

## 14. 前后端联调要点

### 14.1 必须对齐的配置

| 项 | 前端 | 后端 | 状态 |
|----|------|------|------|
| 上下文路径 | `/AIWMS-boot`（request.js） | `/AIWMS-boot`（application.properties） | ✅ 已对齐 |
| Token 头 | `X-Access-Token`（request.js） | `X-Access-Token`（LogCostFilter + RedisService） | ✅ 已对齐 |
| 分页响应格式 | `res.data.rows` / `res.data.total` | `TableDataInfo.rows` / `TableDataInfo.total` | ✅ 已对齐 |
| 错误码 | `200`=成功，`510`=警告，其他=错误 | `BaseResponseInfo.code` | ✅ 已对齐 |

### 14.2 开发代理配置

前端 `vue.config.js` 配置代理：

```javascript
proxy: {
  '/AIWMS-boot': {
    target: 'http://localhost:9999',
    changeOrigin: true
  }
}
```

### 14.3 响应格式约定

所有接口返回统一格式：

```json
{
  "code": 200,
  "data": {
    "rows": [],
    "total": 100
  }
}
```

错误响应：

```json
{
  "code": 500,
  "data": "错误信息"
}
```

业务警告：

```json
{
  "code": 510,
  "data": "警告信息"
}
```

## 15. 项目轻量化说明

本次迭代对后端项目进行了轻量化优化，聚焦仓储管理系统核心功能：

### 15.1 已优化项

| 类别 | 优化内容 | 说明 |
|------|----------|------|
| 代码冗余 | 移除 `FunctionController.getUserBtnByCurrentUser` | 与 `UserController` 中实现完全相同，前端仅调用 `/user/getUserBtnByCurrentUser` |
| 前端裁剪模块 | 消息管理、微信登录、收支项目等 | 后端接口保留，前端按需接入 |

### 15.2 后端保留但前端裁剪的模块

以下后端模块已实现完整功能，前端因聚焦仓储核心进行了裁剪，如需恢复可直接接入：

| 模块 | Controller | 说明 |
|------|-----------|------|
| 消息管理 | `MsgController` | 站内消息推送与已读管理 |
| 微信登录 | `UserController.weixinLogin` | 微信扫码登录与绑定 |
| 收支项目 | `InOutItemController` | 收支项目管理 |
| 财务单据 | `AccountHeadController` / `AccountItemController` | 收付款单据管理 |
| 插件管理（完整） | `PluginController` | 插件安装/卸载/启停 |
| 对账单 | `DepotHeadController.getStatementAccount` | 客户/供应商对账 |
| 欠款管理 | `DepotHeadController.debtList` | 欠款单据查询与导出 |

### 15.3 依赖清单

| 依赖 | 版本 | 用途 |
|------|------|------|
| `spring-boot-starter-web` | 2.0.0 | Web 服务 |
| `mybatis-plus-boot-starter` | 3.0.7.1 | ORM 框架 |
| `mysql-connector-java` | 8.0.33 | MySQL 驱动 |
| `spring-boot-starter-redis` | 1.4.1 | Redis 缓存/会话 |
| `springfox-swagger2` | 2.7.0 | API 文档 |
| `swagger-bootstrap-ui` | 1.6 | Swagger 增强 UI |
| `fastjson` | 1.2.83 | JSON 序列化 |
| `lombok` | 1.18.30 | 简化 Java Bean |
| `pagehelper-spring-boot-starter` | 1.2.13 | 分页插件 |
| `aliyun-sdk-oss` | 3.10.1 | 阿里云 OSS 文件上传 |
| `itextpdf` | 5.5.13.1 | PDF 生成 |
| `jxl` | 2.6.12 | Excel 读写（JExcelAPI） |
| `httpclient` | 4.5.2 | HTTP 客户端（调 AI API） |
| `pinyin4j` | 2.5.1 | 汉字转拼音 |
| `commons-io` | 1.3.2 | IO 工具 |
| `javax.mail` | 1.6.2 | 邮件发送 |
| `springboot-plugin-framework` | 2.2.1 | 插件化框架 |
| `springboot-plugin-framework-extension-mybatis` | 2.2.1 | 插件 MyBatis 扩展 |
