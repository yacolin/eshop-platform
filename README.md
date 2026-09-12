# eshop-platform 电商小程序平台

基于 **Spring Boot 4.1.1 + Java 21** 的电商小程序后端。当前为**脚手架基线**（无业务代码），
tag：`scaffold/v0.1.0`。

## 技术栈

| 组件 | 版本/说明 |
|---|---|
| Spring Boot | 4.1.1（Java 21） |
| MyBatis-Plus | 3.5.17（`mybatis-plus-spring-boot4-starter`） |
| 数据库 | MySQL（本机 `eshop_db` 已建好；无 Flyway、无建库脚本） |
| 缓存 | Redis（Lettuce） |
| 认证 | jjwt 0.13（JWT access/refresh） |
| 接口文档 | springdoc-openapi 3.1（Swagger UI） |
| 其他 | Security / Validation / Lombok |

## 从模板创建新项目

本仓库定位是可复用的**脚手架模板**：日常更常见的用法不是直接开发它，而是用它
命令行生成一个全新命名的项目，省去"复制后手动改名"（旧项目名散落在 pom、Java
包/类名、yml、Makefile、代码生成器、docker-compose、README 等位置，手动易漏）。

```bash
# 在任意目录执行；脚本所在目录即模板源
<模板仓库路径>/new-project.sh <项目名> [选项]

<模板仓库路径>/new-project.sh mall                  # 生成 ./mall（默认库名 mall_db）
<模板仓库路径>/new-project.sh my-shop -o ../apps/my-shop
<模板仓库路径>/new-project.sh mall -p com.acme.mall  # 自定义 Java 根包
```

项目名用 **kebab-case**（小写字母/数字 + 连字符，如 `mall`、`my-shop`），脚本自动
推导三种形态并全量替换：

| 形态 | 示例 | 作用 |
|---|---|---|
| kebab | `my-shop` | Maven artifact / 输出目录 / jar 名 / `spring.application.name` |
| flat | `myshop` | Java 包末段 / 配置前缀 / docker 容器名前缀 |
| Pascal | `MyShop` | 启动类 `MyShopApplication`、测试类等 |

| 选项 | 说明 |
|---|---|
| `-o, --out <dir>` | 输出目录（默认 当前目录/<项目名>） |
| `-p, --package <pkg>` | Java 根包（默认 `com.example.<flat>`）；groupId 默认取其去掉最后一段 |
| `-g, --group <gid>` | Maven groupId |
| `-d, --db <dbname>` | 数据库名（默认 `<flat>_db`） |
| `-P, --prefix <pfx>` | 配置前缀（yml 根 key + `@ConfigurationProperties`）与 docker 容器名前缀 |
| `-f, --force` | 目标目录已存在且非空时，删除重建 |

替换范围覆盖：`pom.xml`、Java 包目录与启动类/测试类文件、`application.yml`、
`Makefile`、代码生成器与 `*.ftl` 模板、`docker-compose.yml`/`Dockerfile`、README 等；
复制时自动排除 `.git/.idea/target/.m2home/*.iml/.env/HELP.md` 及脚本自身。
替换按**最长 token 优先**，不会误伤库名、配置前缀、容器名等包含旧名前缀的词。

生成后还需人工处理：

1. `cd <新项目目录> && git init`；首次 `make test` 会自动选择 Maven 仓库位置（见「快速开始」）
2. 创建脚本提示的新库，并导入**模板库的表结构**（模板不含建库/建表脚本）
3. 微调 pom 描述、README 标题等业务措辞，yml 中的密钥按环境覆盖

> 注意：`new-project.sh` 只保留在模板仓库，不会复制进新项目；若模板后续新增含旧名
> 的字符串（如新业务模块名），需同步补充脚本内的替换 token 清单，否则会漏换。

## 快速开始

```bash
make run          # 启动（8080，dev 默认配置：eshop_db + 本机 root/123456 + 本地 Redis）
make test         # 跑测试
make build        # 打包 jar
make help         # 全部命令
```

- 健康检查：`GET /api/v1/health`（Security 白名单放行）
- Swagger UI：`http://localhost:8080/swagger-ui.html`
- **Maven 仓库自动选择**：默认用用户级 `~/.m2`（与其他项目/IDE 共用依赖，不重复下载）；
  `~/.m2` 不可写（沙箱等受限环境）时自动回落项目内 `.m2home`（已 gitignore），
  也可显式指定：`make run M2_HOME=/path/to/maven-user-home`

## 目录结构

```
src/main/java/com/example/eshopplatform/
├── EshopPlatformApplication.java   # 启动入口
├── common/                         # ApiResponse / ErrorCode / BizException / PageResult / 时间工具 / MP 配置
├── config/                         # Security(JWT 白名单+CORS) / JWT / 微信 / springdoc OpenAPI
├── security/                       # JWT 认证过滤器链
├── health/                         # /api/v1/health 健康检查
└── <域>/                           # 业务域（由 make gen 生成，见下）
    └── <业务>/                     # 域内业务子包（如表 sp_brands → sp.brand）
        ├── entity/ mapper/ service/ dto/
        └── controller/             # 控制器平铺；端由类名后缀区分（XxxAdmin/XxxPublicController）
src/test/.../tool/CodeGenerator.java        # 代码生成器（test 作用域，不进运行包）
src/test/resources/templates/               # 自定义生成模板（service/controller/vo/req）
```

## 代码生成器（make gen）

MyBatis-Plus 官方 FastAutoGenerator 3.5.17，用于消灭"手写 entity/mapper/service/controller 骨架"的机械劳动。

```bash
make gen                                        # 生成全部业务表（按表前缀分域）
make gen DOMAIN=sp                              # 只生成 sp_ 域全部表（整域一把梭）
make gen GEN_TABLES="sp_brands usr_users"       # 只生成指定表（表少时用）
make gen GEN_OPTS="-Ddb.password=xxx -Dgen.author=me"   # 覆盖连接/作者
make gen GEN_OPTS="-Dgen.controllerLayout=admin" # 控制器按端拆分：both(缺省)/admin/public
```

**每表产出 8 个文件**（默认按端生成 Admin + Public 两套控制器，不需要的一侧手工删除即可；
表前缀 → 业务域包 → 业务子包，如 `sp_brands` → `com.example.eshopplatform.sp.brand`；
控制器平铺在 `controller` 内、以类名 `Admin`/`Public` 后缀区分端）：

```
<域>/<业务>/entity/<实体>.java            # @TableName + Lombok + @TableField
<域>/<业务>/mapper/<实体>Mapper.java      # @Mapper（无 @MapperScan，靠注解注册）
<域>/<业务>/service/<实体>Service.java    # 具体类（无接口、无 *ServiceImpl）
<域>/<业务>/controller/<实体>AdminController.java    # 管理端 CRUD（类上只写业务路径，运行时 /api/v1/admin/...）
<域>/<业务>/controller/<实体>PublicController.java   # 小程序端 CRUD（同理，运行时 /api/v1/public/...；不用可删）
<域>/<业务>/dto/<实体>VO.java             # 响应对象（deleted_at 不暴露；时间列 Long epoch 毫秒）
<域>/<业务>/dto/<实体>CreateReq.java      # 新增入参（不含主键与自动列；DB 必填列带空校验，String 带 DB 长度上限校验）
<域>/<业务>/dto/<实体>UpdateReq.java      # 更新入参（同 CreateReq 的自动列/校验规则）
```

**工程约定**：

- Service 为**具体类**（`@Service` + `@RequiredArgsConstructor` + 注入 Mapper），不生成接口与 `*ServiceImpl`
- 类名**去掉域前缀**（域由包名表达）：`sp_brands` → 实体 `Brands`（`@TableName` 仍为 `sp_brands`），
  mapper/service/controller/dto 均无前缀（`BrandsMapper`/`BrandsService`/`BrandsAdminController`/
  `BrandsVO`/`BrandsCreateReq`/`BrandsUpdateReq`）
- **控制器按端拆分**：**默认每表生成两套**（`both`）——
  管理端 `controller.XxxAdminController`（类上只写业务路径，运行时 `/api/v1/admin/<resource>`）与
  小程序端 `controller.XxxPublicController`（同理 `/api/v1/public/<resource>`）；
  多出来的一侧**直接删文件即可**（重复生成不会把它加回来，也不会覆盖已改文件）。
  想只生成一侧：`-Dgen.controllerLayout=admin|public`，或在
  `CodeGenerator.TABLE_LAYOUT` 登记该表（如内部表 `sys_operation_logs → admin`）。
  两个控制器**平铺在同一个 `controller` 包内**，端由类名后缀区分；
  `@Tag` 名与 `operationId` 同样带 `Admin`/`Public` 后缀，保证同表双端时全局唯一
- Controller 为 `@RestController`，**类上只写业务路径**（表名去域前缀 + 下划线转短横线，保留复数），
  如 `sp_brands` → `@RequestMapping("/brands")`；**端前缀 `/api/v1/admin`、`/api/v1/public` 由
  `WebConfig#configurePathMatch` 按标记注解统一添加**——管理端标 `@AdminController`、公开端标
  `@PublicController`（`common/web`），运行时最终路径仍是 `/api/v1/admin/brands`。Spring 的
  `addPathPrefix` 只取**第一个命中**的前缀（不叠加），故两端各用一个完整前缀；Security 的
  `admin-paths`/`whitelist` 与 springdoc 的 `paths-to-match` 仍按完整路径匹配，springdoc 3.1
  能正确反映该前缀（分组文档路径完整）；未标注的控制器（如健康检查 `/api/v1/health`）不受影响；
  端点 `GET/POST/PUT/DELETE` 统一返回 `ApiResponse` / `PageResult`，带 `@Operation`；
  写接口请求体带 `@Valid`（触发 DTO 校验注解）
- CRUD 出入参走 DTO（CreateReq/UpdateReq/VO），`toVO` / `apply` 字段映射由模板自动生成；
  `apply` 只落业务字段（created_at/updated_at/deleted_at 等自动列绝不赋值），
  VO 时间列由 `toVO` 用 `TimeUtil.toEpochMillis` 转 Long（epoch 毫秒）返回
- **Service 语义约定**（模板注释已写死，勿各模块写歪）：
  - `apply` 传**整个 Req DTO**（不学逐个字段传参的长参数风格）；要"null 兜默认值"时在
    apply 内对该字段显式兜底即可
  - `update` 为 **DTO 覆盖语义**：MP 默认 NOT_NULL 策略，req 中 null 字段不生成 SET、
    保留原值（非全量重置）；做"传 null=重置默认"语义需自行兜底
  - `delete` 默认**逻辑删除**：表含 `deleted_at`（datetime，NULL=未删除）时实体该字段自动标
    `@TableLogic(value="null", delval="now()")`，`deleteById` 转 `SET deleted_at=now()`、
    普通查询自动带 `deleted_at IS NULL`；只有无该列的表（流水/记录表）才是物理删除。
    有"被引用/子级拒删"约束的模块参照 delete 注释里的骨架先 count 后抛
    `BizException.conflict`，防误删
  - 分页统一用 `normalizePage/normalizeSize` 归一化；带筛选的分页按生成的注释样例用
    **condition 链式**（`eq(boolean, col, val)` 一行一个条件，判定写 condition 内），
    业务语义判定（parentId>0 之类）也写在 condition 里，过长再抽局部 boolean
- **不生成 mapper.xml**（见下节）

**重复生成会不会覆盖已写好的业务？** 不会。生成器**默认跳过已存在的文件**（未开启
fileOverride），只新建不存在的文件：

- 已生成并被手工改过的类 → 再次 `make gen` 保持不动
- 新增表 / 新增文件 → 正常补建
- 因此开发流程是：先写完/提交业务 → 需要补新表时再 `make gen GEN_TABLES="新表..."`，
  生成后用 `git status` / `git diff` 检查，新增内容按域提交
- 唯一的"覆盖"场景：**删除旧文件后重新生成**（模板或命名规则升级需要时才这么做）。
  所以升级模板/规则前务必先 commit，删除旧文件重生成后再逐文件 diff 核对

**生成后的必做清单**（机械骨架之外的人工部分）：

1. 实体类名已按规则**去掉域前缀**（`usr_users` → 类 `Users`，`@TableName` 仍为 `usr_users`）；
   如需单数类名（`Users` → `User`）直接改名并保留 `@TableName`
2. CreateReq/UpdateReq 已按 DB 约束带**基础校验**：NOT NULL 且无默认值的列自动加
   `@NotNull`/`@NotBlank`，String 列按 DB 长度上限自动加 `@Size(max=...)`；仍按接口用例
   裁剪字段、补充业务校验（如 `@Min`/`@Max`/枚举校验等模板无法从 DB 推导的部分）。
   update 为 DTO 覆盖语义（null 保留原值，非全量重置），做部分更新（PATCH）的模块自行放松
3. Controller 路径默认已按规则生成（`/api/v1/brands` 等）；遇子资源/嵌套接口（如
   `/api/v1/brands/{id}/xxx`）或跨模块重名时，手动改成更精确的业务路径
4. 按接口端（公开/管理端）补 springdoc 分组 `@Tag`，并把真实路径补入 `application.yml` 的
   `eshop.security.whitelist` / `admin-paths`（否则 Security 默认拦截返回 403）
5. 时间列模板已统一转 Long（epoch 毫秒）返回；表含 `deleted_at` 时模板自动标
   `@TableLogic`（逻辑删除，VO/Req/apply 均不含该列），无该列的表生成的是物理删除，
   需按业务确认

**推荐编码流程（每个业务域都按这个顺序）**：

```
① 先生成样板 → make gen DOMAIN=<域>（或 GEN_TABLES="表1 表2"）
                产出可运行的骨架：entity / mapper / service / controller / dto(VO+CreateReq+UpdateReq)
② 再做开发   → 在上面的骨架上写业务：
                必做清单 1~5（改实体名/裁剪 DTO/补校验/调路径/注册白名单…）
③ 验证编译   → make test
④ 按域提交   → git add src/main/java/com/example/eshopplatform/<域> &&
                git commit -m "feat(<域>): ..."
```

> 规则：**先 `make gen` 生成样板代码，再在此基础上开发**——不要在生成前手写
> entity/mapper/service 等机械代码；样板生成后已被手改的文件，重复生成也不会被覆盖。

## mapper.xml 使用说明

**现状**：项目刻意**不生成 mapper.xml**，SQL 一律走 MyBatis-Plus 的 Wrapper / Mapper 注解，
`src/main/resources` 下没有 `mapper/` 目录。

但 `application.yml` 的以下配置**已预留**，将来需要 XML 时无需改配置：

```yaml
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
```

**什么时候需要手写 SQL**：MP `BaseMapper`/Wrapper 覆盖不了的时候，典型如
多表 join 查询、复杂统计/报表、批量 update、`GROUP BY`/窗口函数等。

**两种写法任选**：

1. **注解式（推荐先用）**——直接在 Mapper 接口写：
   ```java
   @Mapper
   public interface BrandsMapper extends BaseMapper<Brands> {
       @Select("SELECT * FROM sp_brands WHERE name LIKE CONCAT('%', #{kw}, '%')")
       List<Brands> search(@Param("kw") String kw);
   }
   ```
2. **XML 式**——自建 `src/main/resources/mapper/<实体>Mapper.xml`，namespace 写 Mapper
   接口全限定名，接口上加对应方法声明：
   ```xml
   <mapper namespace="com.example.eshopplatform.sp.mapper.BrandsMapper">
       <select id="search" resultType="com.example.eshopplatform.sp.entity.Brands">
           SELECT * FROM sp_brands WHERE name LIKE CONCAT('%', #{kw}, '%')
       </select>
   </mapper>
   ```
   （`mapper-locations` 已就绪，建好目录与文件即可被扫描，无需改配置）

**将来若想让生成器重新输出 XML 骨架**（可选，建议按需再开）：改
`src/test/java/com/example/eshopplatform/tool/CodeGenerator.java`：
去掉 `mapperBuilder().disableMapperXml()`，并在 `packageConfig` 加
`pathInfo(OutputFile.xml → src/main/resources/mapper)`。需要恢复实现细节时，可用
`git log --oneline src/test/java/com/example/eshopplatform/tool/CodeGenerator.java`
查看该文件的历史变更作参考。

## 提交节奏

- 脚手架基线：tag `scaffold/v0.1.0`（无业务代码：基础设施 + 开发工具）
- 业务开发按**域**推进并分批提交：`make gen` 生成 → diff 核对 → 按域提交
  （如 `feat(usr): ...`、`feat(sp): ...`），每域提交时同步补 Security 路径 / springdoc 分组
- commit message 遵循 `<type>(<scope>): <中文描述>` 规范

## 工程现状备忘

| 项 | 现状 |
|---|---|
| 数据库 | `eshop_db` 已建好并直接连接；无 Flyway、无建库/种子脚本（73 张业务表，前缀分域 usr/sp/tx/mch/mkt/rev/sys/base） |
| mapper.xml | 不生成（走 Wrapper/注解）；`mapper-locations` 已预留，需要时自建 `resources/mapper` |
| springdoc | 暂无业务接口，仅基础配置；按端分组随业务接口补齐 |
| Security | whitelist / admin-paths 当前仅基础设施路径（`/error`、`/api/v1/health`、文档路径）；业务路径随各域提交补入 |
| 代码生成 | `make gen`（FastAutoGenerator 3.5.17），每表 8 文件（entity/mapper/service/dto×3 + Admin/Public 两个 controller，默认 `both`，不用的一侧可删）；控制器平铺于 `controller`、类名带 `Admin`/`Public` 后缀（`-Dgen.controllerLayout=admin\|public\|both`） |
