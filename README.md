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

## 快速开始

```bash
make run          # 启动（8080，dev 默认配置：eshop_db + 本机 root/123456 + 本地 Redis）
make test         # 跑测试
make build        # 打包 jar
make help         # 全部命令
```

- 健康检查：`GET /api/v1/health`（Security 白名单放行）
- Swagger UI：`http://localhost:8080/swagger-ui.html`
- 本地 Maven 仓库重定向到 `.m2home`（已 gitignore），沙箱/受限环境通用

## 目录结构

```
src/main/java/com/example/eshopplatform/
├── EshopPlatformApplication.java   # 启动入口
├── common/                         # ApiResponse / ErrorCode / BizException / PageResult / 时间工具 / MP 配置
├── config/                         # Security(JWT 白名单+CORS) / JWT / 微信 / springdoc OpenAPI
├── security/                       # JWT 认证过滤器链
├── health/                         # /api/v1/health 健康检查
└── <域>/                           # 业务域（由 make gen 生成，见下）
    ├── entity/ mapper/ service/ controller/ dto/
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
```

**每表产出 6 个文件**（表前缀 → 业务域包，如 `sp_brands` → `com.example.eshopplatform.sp`）：

```
entity/<实体>.java           # @TableName + Lombok + @TableField
mapper/<实体>Mapper.java     # @Mapper（无 @MapperScan，靠注解注册）
service/<实体>Service.java   # 具体类（无接口、无 *ServiceImpl）
controller/<实体>Controller.java  # @RestController + 基础 CRUD 端点
dto/<实体>VO.java            # 响应对象（全字段 @Data + @Schema）
dto/<实体>Req.java           # create/update 入参（不含主键）
```

**工程约定**：

- Service 为**具体类**（`@Service` + `@RequiredArgsConstructor` + 注入 Mapper），不生成接口与 `*ServiceImpl`
- 类名**去掉域前缀**（域由包名表达）：`sp_brands` → 实体 `Brands`（`@TableName` 仍为 `sp_brands`），
  mapper/service/controller/dto 均无前缀（`BrandsMapper`/`BrandsService`/`BrandsController`/`BrandsVO`/`BrandsReq`）
- Controller 为 `@RestController`，路径规则 = **表名去域前缀 + 下划线转短横线（保留复数）**，
  如 `sp_brands` → `/api/v1/brands`、`sp_product_attributes` → `/api/v1/product-attributes`；
  端点 `GET/POST/PUT/DELETE` 统一返回 `ApiResponse` / `PageResult`，带 `@Operation`
- CRUD 出入参走 DTO（Req/VO），`toVO` / `apply` 字段映射由模板自动生成
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
2. Req/VO 按接口用例裁剪字段、补校验注解（`@NotNull`/`@NotBlank` 等）
3. Controller 路径默认已按规则生成（`/api/v1/brands` 等）；遇子资源/嵌套接口（如
   `/api/v1/brands/{id}/xxx`）或跨模块重名时，手动改成更精确的业务路径
4. 按接口端（公开/管理端）补 springdoc 分组 `@Tag`，并把真实路径补入 `application.yml` 的
   `eshop.security.whitelist` / `admin-paths`（否则 Security 默认拦截返回 403）
5. 时间字段在 VO 中转为 epoch 毫秒时间戳、核对逻辑删除字段等

**推荐编码流程（每个业务域都按这个顺序）**：

```
① 先生成样板 → make gen DOMAIN=<域>（或 GEN_TABLES="表1 表2"）
                产出可运行的骨架：entity / mapper / service / controller / dto(VO+Req)
② 再做开发   → 在上面的骨架上写业务：
                必做清单 1~5（改实体名/裁剪 DTO/补校验/调路径/注册白名单…）
③ 验证编译   → ./mvnw -Dmaven.repo.local=.m2home/repository test
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
| 代码生成 | `make gen`（FastAutoGenerator 3.5.17），每表 6 文件（entity/mapper/service/controller/dto×2） |
