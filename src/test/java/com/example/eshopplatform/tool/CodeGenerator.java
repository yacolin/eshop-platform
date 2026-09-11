package com.example.eshopplatform.tool;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MyBatis-Plus 代码生成器（官方 FastAutoGenerator，3.5.x）。
 *
 * <p><b>作用</b>：连上 eshop_db 按表生成 entity / mapper / service / controller / dto（不含
 * mapper.xml：SQL 走 MyBatis-Plus Wrapper/注解，不用 XML 文件），
 * 消灭手写 CRUD 骨架的机械劳动；Service 为具体类（不生成接口与 *ServiceImpl，
 * 入参出参走 dto：写请求拆分为 {@code XxxCreateReq}/{@code XxxUpdateReq}，出参为
 * {@code XxxVO}；时间列 VO 统一 Long（epoch 毫秒）），生成后仍需人工核对
 * （类名单复数、CreateReq/UpdateReq/VO 字段裁剪与补充校验、controller 路径/权限与
 * 统一返回 {@code ApiResponse} 等业务化改造）。
 *
 * <p><b>包结构约定</b>（工程约定）：表前缀 = 业务域模块；**类名去掉前缀**（域已由包表达），
 * 生成到 {@code com.example.eshopplatform.<前缀>} 下的 entity / mapper / service /
 * controller / dto。
 * 例如 {@code usr_users} → 前缀 {@code usr} → {@code com.example.eshopplatform.usr.entity.Users}
 * （类名已去 {@code usr_} 前缀，{@code @TableName} 仍为 {@code usr_users}），
 * 同域 dto 生成 {@code UsersVO / UsersCreateReq / UsersUpdateReq} 等。
 *
 * <p><b>域内业务子包</b>（工程约定）：域内业务增多时按业务拆子包（业务→层），
 * 业务名默认由表名推导（见 {@link #resolveBusinessOf(String)}）：取域前缀后的整段
 * 剩余名、去下划线驼峰拼接、末词复数转单数——单词表如 {@code sys_roles} →
 * {@code com.example.eshopplatform.sys.role.entity.Roles}、{@code sp_brands} →
 * {@code ...sp.brand...}；复合表如 {@code sp_category_attributes} →
 * {@code ...sp.categoryAttribute...}、{@code sp_attribute_values} →
 * {@code ...sp.attributeValue...}、{@code sys_role_permissions} →
 * {@code ...sys.rolePermission...}。
 * 跨业务共用的守卫等放 {@code ...sys.common}。
 * 无域前缀或无法推导的表才按"域层平铺"生成。
 *
 * <p><b>控制器按端拆分</b>（Admin / Public）：每张表<b>默认生成两套</b> Controller
 * （{@code both}）——管理端 {@code <实体>AdminController}
 * （{@code /api/v1/admin/<resource>}）与小程序端 {@code <实体>PublicController}
 * （{@code /api/v1/public/<resource>}），两者统一落 {@code <域>.<业务>.controller} 包
 * （不按端再分子包），{@code @Tag} 名与 {@code operationId} 均带 {@code Admin}/{@code Public}
 * 后缀（同表双端时 operationId 仍全局唯一）；不需要的一侧手工删除即可。
 * 全局默认可用 {@code -Dgen.controllerLayout=admin|public|both} 覆盖，逐表覆盖见
 * {@link #TABLE_LAYOUT}。
 *
 * <p><b>重复运行安全</b>：生成器默认不覆盖已存在文件（未开启 fileOverride），
 * 只新建缺失文件，已写好的业务改动不会被冲掉；模板/命名规则升级需先 git 提交、
 * 删除旧文件后再重新生成，并以 diff 核对。
 *
 * <p><b>运行方式</b>（生成器仅 test 作用域，勿放入主代码）：
 * <pre>
 *   # 1) 编译测试类（拉取 generator/freemarker 依赖）
 *   ./mvnw -Dmaven.repo.local=.m2home/repository test-compile
 *   # 2) IDE 中运行本类的 main（IntelliJ 直接 Run 'CodeGenerator.main()'）
 *   #    不带参数 = 生成全部表（自动按表前缀分域）；
 *   #    参数支持：域前缀（sp = 生成 sp_* 整域）/ 具体表名（usr_users usr_addresses），可混用
 *   #    数据库连接默认同 application.yml 开发配置，可用 -Ddb.url/-Ddb.username/-Ddb.password 覆盖
 *   #    按端布局：-Dgen.controllerLayout=admin|public|both（缺省 both；逐表覆盖见 TABLE_LAYOUT）
 * </pre>
 */
public class CodeGenerator {

    /** 生成代码的根包（与主代码一致，勿改） */
    private static final String PARENT_PACKAGE = "com.example.eshopplatform";

    /**
     * 产物根目录：默认 src/main/java（生成后 diff 检查再提交）；
     * 可用 {@code -Dgen.outputDir=<目录>} 覆盖（如试生成到 /tmp 验证包路径，勿提交产物）。
     */
    private static String outputDir() {
        return System.getProperty("gen.outputDir",
                new File("").getAbsolutePath() + "/src/main/java");
    }

    public static void main(String[] args) throws Exception {
        // 数据库连接：默认与 src/main/resources/application.yml 开发配置一致，可用系统属性覆盖
        String url = System.getProperty("db.url",
                "jdbc:mysql://127.0.0.1:3306/eshop_db?useUnicode=true&characterEncoding=utf8"
                        + "&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true");
        String username = System.getProperty("db.username", "root");
        String password = System.getProperty("db.password", "123456");
        // 类注释作者：默认留空（不输出 @author），需要时用 -Dgen.author=xxx 指定
        String author = System.getProperty("gen.author", "");

        // 待生成表：未传参 = 全部业务表；参数支持两种：
        //   1) 裸域前缀（无下划线，如 sp / usr）= 生成该域全部表（sp_*）
        //   2) 具体表名（含下划线，如 sp_brands）= 只生成指定表
        List<String> tables = args.length > 0
                ? expandTables(url, username, password, List.of(args))
                : listAllTables(url, username, password);
        System.out.println("待生成表(" + tables.size() + "): " + tables);

        // 按生成单元（业务域模块 × 按端布局）逐组执行生成：
        // 业务名统一由"域前缀后的整段复合名"推导（下划线去连、末词复数转单数）：
        // sys_roles -> sys.role、sys_role_permissions -> sys.rolePermission、
        // sp_category_attributes -> sp.categoryAttribute、sp_attribute_values -> sp.attributeValue；
        // 命名规则表达不了的场景才走 BUSINESS_EXCEPTIONS（当前无例外）。
        // 控制器按端拆分：both 表拆进 admin/public 两组各跑一遍，
        // 第二遍只补缺失的 Controller（已生成的 entity/mapper/service/dto 不会被覆盖）。
        Map<String, List<String>> byModuleLayout = new LinkedHashMap<>();
        for (String table : tables) {
            String domain = moduleOf(table);
            String business = resolveBusinessOf(table);
            String module = business == null ? domain : domain + "." + business;
            for (String layout : expandLayout(layoutOf(table))) {
                byModuleLayout.computeIfAbsent(module + "|" + layout, k -> new ArrayList<>()).add(table);
            }
        }
        for (Map.Entry<String, List<String>> e : byModuleLayout.entrySet()) {
            int sep = e.getKey().lastIndexOf('|');
            generateModule(url, username, password, author,
                    e.getKey().substring(0, sep), e.getKey().substring(sep + 1), e.getValue());
        }
        System.out.println("生成完成。产物目录：\n  " + outputDir()
                + "\n请 diff 检查后按业务域分批提交（勿把整个生成结果一次提交）。");
    }

    /**
     * 表名 -> 域内业务包名的"例外覆盖"：为命名规则（整段复合单数化）表达不了的场景预留。
     * 当前无例外——包括 {@code sys_role_permissions} 在内一律按规则推导
     * （sys_role_permissions -> sys.rolePermission）。
     * 未来若遇到非规则名词/特殊归属，再在此登记并说明理由。
     */
    private static final Map<String, String> BUSINESS_EXCEPTIONS = Map.of();

    /**
     * 控制器按端拆分（Admin / Public）：
     * <ul>
     *   <li><b>全局默认 {@code both}</b>：{@code -Dgen.controllerLayout=admin|public|both} 可改。
     *       默认给每张表生成管理端 + 小程序端两套 Controller——多出来的一侧按需手工删除，
     *       比事后补建省事（删除后重复生成也不会被恢复：生成器不覆盖已存在文件，但也不重建已删除文件）。</li>
     *   <li>生成的 Controller 统一落在 {@code <域>.<业务>.controller} 子包（不按端再分子包），
     *       类名 {@code <实体><Admin|Public>Controller} 区分端；路由前缀 {@code /api/v1/<端>}，
     *       {@code operationId} 带端后缀（admin/public 同表生成两套时仍全局唯一）；</li>
     *   <li>{@code both} 的实现：同一张表拆进 admin/public 两组各跑一遍，第二遍只补缺失文件，
     *       已存在的 entity/mapper/service/dto 不会被覆盖（日志里的 "already exists" WARN 属正常）；</li>
     *   <li><b>逐表覆盖</b>：在 {@link #TABLE_LAYOUT} 登记「表名 → 布局」，未登记的表用全局默认；
     *       内部表只想留管理端时登记 {@code admin} 即可（如 {@code sys_*}）。</li>
     * </ul>
     */
    private static final String DEFAULT_CONTROLLER_LAYOUT =
            System.getProperty("gen.controllerLayout", "both");

    /** 合法的按端布局取值 */
    private static final Set<String> VALID_LAYOUTS = Set.of("admin", "public", "both");

    /**
     * 表 → 按端布局覆盖（admin / public / both）：默认双端，需要"只生成一侧"的表在此登记，
     * 未登记的表用 {@link #DEFAULT_CONTROLLER_LAYOUT}（both）。
     * 例：{@code Map.of("sys_operation_logs", "admin", "sys_login_histories", "admin")}
     * 表示这两张内部表只生成管理端。
     */
    private static final Map<String, String> TABLE_LAYOUT = Map.of();

    /** 解析某张表的控制器布局：逐表覆盖优先，其次全局默认；非法值直接报错（不静默生成错包） */
    private static String layoutOf(String table) {
        String layout = TABLE_LAYOUT.getOrDefault(table, DEFAULT_CONTROLLER_LAYOUT);
        if (!VALID_LAYOUTS.contains(layout)) {
            throw new IllegalArgumentException("非法的控制器布局 '" + layout + "'（表 " + table
                    + "），可选值 " + VALID_LAYOUTS
                    + "；见 CodeGenerator.TABLE_LAYOUT 或 -Dgen.controllerLayout");
        }
        return layout;
    }

    /** 布局展开：both → [admin, public]；其余 → 单元素列表 */
    private static List<String> expandLayout(String layout) {
        return "both".equals(layout) ? List.of("admin", "public") : List.of(layout);
    }

    /**
     * 推导表所属的域内业务名：
     * 1) 命中 {@link #BUSINESS_EXCEPTIONS} 直接返回；
     * 2) 否则取"域前缀后整段剩余名"：按下划线分词、去连成驼峰、仅对末词做复数转单数
     *    （category_attributes -> categoryAttribute、attribute_values -> attributeValue、
     *    brands -> brand、categories -> category、staff 原样）；
     * 3) 无域前缀/无法推导返回 null（该表按域层平铺生成）。
     */
    private static String resolveBusinessOf(String table) {
        String override = BUSINESS_EXCEPTIONS.get(table);
        if (override != null) {
            return override;
        }
        int idx = table.indexOf('_');
        if (idx <= 0) {
            return null; // 无域前缀：归入根包
        }
        String rest = table.substring(idx + 1);
        if (rest.isEmpty()) {
            return null;
        }
        String[] tokens = rest.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            String word = tokens[i];
            if (word.isEmpty()) {
                continue;
            }
            if (i == tokens.length - 1) {
                word = singularize(word); // 仅末词是中心名词，做单数化
            }
            if (sb.length() == 0) {
                sb.append(word);
            } else {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    /**
     * 简单英文复数转单数（尽力而为，只覆盖规则化名词）：
     * -ies -> -y（categories -> category、inventories -> inventory）；
     * -sses -> 去 es（addresses -> address、businesses -> business）；
     * -s/-ss 以外去尾 s（roles -> role、brands -> brand、staff 原样）。
     * 非规则名词（如 series/status）若作为业务名出现，请走 {@link #BUSINESS_EXCEPTIONS} 覆盖。
     */
    private static String singularize(String word) {
        if (word.endsWith("ies")) {
            return word.substring(0, word.length() - 3) + "y";
        }
        if (word.endsWith("sses")) {
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("s") && !word.endsWith("ss")) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }

    /** 表前缀 = 业务域模块名（首个下划线前的小写词，如 sp_products -> sp）；无前缀表归入根包 */
    private static String moduleOf(String table) {
        int idx = table.indexOf('_');
        return idx > 0 ? table.substring(0, idx) : "";
    }

    /** 展开参数：裸域前缀（如 sp）展开为该前缀全部表；其余按具体表名保留 */
    private static List<String> expandTables(String url, String user, String pass,
                                             List<String> args) throws Exception {
        List<String> all = listAllTables(url, user, pass);
        List<String> result = new ArrayList<>();
        for (String arg : args) {
            if (arg.indexOf('_') < 0) { // 域前缀模式：sp -> sp_* 全部表
                List<String> matched = all.stream()
                        .filter(t -> t.startsWith(arg + "_"))
                        .toList();
                if (matched.isEmpty()) {
                    System.out.println("!! 未找到前缀为 '" + arg + "_' 的表，已跳过");
                }
                result.addAll(matched);
            } else {                    // 具体表名模式
                result.add(arg);
            }
        }
        return result.stream().distinct().toList();
    }

    /** 查库中全部表（排除视图） */
    private static List<String> listAllTables(String url, String user, String pass) throws Exception {
        String dbName = parseDbName(url);
        List<String> result = new ArrayList<>();
        String sql = "SELECT table_name FROM information_schema.tables "
                + "WHERE table_schema = ? AND table_type = 'BASE TABLE' ORDER BY table_name";
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dbName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString(1));
                }
            }
        }
        return result;
    }

    private static String parseDbName(String url) {
        String base = url.substring(url.indexOf("//") + 2);
        String path = base.substring(base.indexOf('/') + 1);
        int q = path.indexOf('?');
        return q > 0 ? path.substring(0, q) : path;
    }

    /**
     * 对一个生成单元（业务模块 × 按端布局）执行 FastAutoGenerator。
     *
     * @param module 包名段：业务域，或"域.业务子模块"（如 sys.role / sp.brand /
     *               sp.categoryAttribute，见 resolveBusinessOf）。
     *               产物包为 com.example.eshopplatform.<module>.<layer>，
     *               类名去前缀取的是包名首段（sys.role -> sys_；sp.brand -> sp_）。
     * @param layout 按端布局：{@code admin}（管理端）或 {@code public}（小程序端），
     *               只会传入 {@link #expandLayout} 展开后的单值；决定 Controller 子包
     *               （统一落 controller 子包，端由类名 Admin/Public 区分）、
     *               路由前缀（/api/v1/admin|public）、
     *               类名后缀（Admin/Public）与 operationId 后缀。
     */
    private static void generateModule(String url, String user, String pass, String author,
                                       String module, String layout, List<String> tables) {
        String layoutSuffix = "admin".equals(layout) ? "Admin" : "Public";
        String apiBase = "/api/v1/" + layout;
        String layoutLabel = "admin".equals(layout) ? "管理端" : "小程序端";
        System.out.println(">> 生成模块 '" + module + "'（" + layoutLabel + "），表: " + tables);
        // 表前缀 = 模块包名首段 + 下划线（sys.role -> sys_）；用于类名去前缀
        int dot = module.indexOf('.');
        String prefix = (module.isEmpty() ? "" : module.substring(0, dot < 0 ? module.length() : dot) + "_");

        FastAutoGenerator.create(url, user, pass)
                // 全局：作者 / 输出目录（直接落 src/main）
                .globalConfig(builder -> builder
                        .author(author)
                        .outputDir(outputDir())
                        .disableOpenDir())
                // 包结构：com.example.eshopplatform.<module>.{entity,mapper,service,dto,controller}
                // 控制器统一落 controller 子包（不按端再分子包），端由类名 Admin/Public 区分
                .packageConfig(builder -> {
                    builder.parent(PARENT_PACKAGE);
                    if (!module.isEmpty()) {
                        builder.moduleName(module);
                    }
                    builder.controller("controller");
                })
                // 命名：按表前缀去掉域前缀（类名不再带 sys_ 等，如 sys_roles -> Roles，
                // @TableName 仍保留原表名 sys_roles）
                .strategyConfig(builder -> {
                    builder.addInclude(tables.toArray(new String[0]));
                    if (!prefix.isEmpty()) {
                        builder.addTablePrefix(prefix);
                    }
                    builder.entityBuilder()
                            .enableLombok()                 // 实体用 Lombok（省 getter/setter）
                            .enableTableFieldAnnotation()   // 字段一律 @TableField，杜绝命名歧义
                            .javaTemplate("templates/entity.java"); // 接管默认模板（@author 留空）
                    builder.controllerBuilder()
                            .enableRestStyle()          // @RestController（RESTful）
                            .template("templates/controller.java")  // 自定义基础 CRUD 模板
                            // 类名带端后缀：<实体>AdminController / <实体>PublicController
                            .formatFileName("%s" + layoutSuffix + "Controller");
                    builder.serviceBuilder()
                            // 工程约定：Service 为具体类，不生成接口与 *ServiceImpl；
                            // 用自定义模板输出 @Service + 注入 Mapper（src/test/resources/templates/service.java.ftl）
                            .disableServiceImpl()
                            .serviceTemplate("templates/service.java")
                            .formatServiceFileName("%sService");
                    builder.mapperBuilder()
                            // 工程约定：不生成 mapper.xml（SQL 用 Wrapper/注解）
                            .disableMapperXml()
                            // 工程约定：Mapper 接口加 @Mapper 注解（无 @MapperScan）
                            .enableMapperAnnotation()
                            .mapperTemplate("templates/mapper.java")   // 接管默认模板（@author 留空）
                            .formatMapperFileName("%sMapper");
                })
                // DTO：每个业务域一个 dto 包，每表生成 <实体>VO / <实体>CreateReq / <实体>UpdateReq
                //（创建/更新入参拆分，模板自动按 DB 约束补空校验注解；时间列 VO 以 Long epoch 毫秒返回）
                .injectionConfig(builder -> {
                    String dtoPkg = PARENT_PACKAGE + (module.isEmpty() ? "" : "." + module) + ".dto";
                    Map<String, Object> custom = new HashMap<>();
                    custom.put("dtoPkg", dtoPkg);             // 供模板拼包名
                    custom.put("apiBase", apiBase);           // 路由前缀：/api/v1/admin 或 /api/v1/public
                    custom.put("layoutSuffix", layoutSuffix); // 类名/operationId 端后缀：Admin / Public
                    custom.put("layoutLabel", layoutLabel);   // 中文端名，用于 @Tag 描述
                    builder.customMap(custom);
                    builder.customFile(cf -> cf
                            .formatNameFunction(t -> t.getEntityName())
                            .fileName("VO.java")
                            .packageName("dto")
                            .templatePath("templates/vo.java.ftl"));
                    builder.customFile(cf -> cf
                            .formatNameFunction(t -> t.getEntityName())
                            .fileName("CreateReq.java")
                            .packageName("dto")
                            .templatePath("templates/req-create.java.ftl"));
                    builder.customFile(cf -> cf
                            .formatNameFunction(t -> t.getEntityName())
                            .fileName("UpdateReq.java")
                            .packageName("dto")
                            .templatePath("templates/req-update.java.ftl"));
                })
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}
