package com.example.eshopplatform.tool;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
 * 业务名默认由表名推导（见 {@link #resolveBusinessOf(String)}）：取域前缀后的首段词
 * 并做复数转单数，如 {@code sys_roles} → {@code com.example.eshopplatform.sys.role.entity.Roles}、
 * {@code sp_brands} → {@code ...sp.brand...}、{@code sp_products} → {@code ...sp.product...}。
 * 个别"归属/命名"需人工判断的表用 {@link #BUSINESS_EXCEPTIONS} 覆盖（如
 * {@code sys_role_permissions} → sys.permission、{@code sp_category_attributes} →
 * sp.categoryAttribute、{@code sp_category_brands} → sp.categoryBrand）。
 * 跨业务共用的守卫等放 {@code ...sys.common}。
 * 无域前缀或无法推导的表才按"域层平铺"生成。
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

        // 按生成单元（业务域模块，域内再按业务子模块分组）逐组执行生成：
        // 业务子模块默认由"域前缀后的首段词（复数转单数）"推导（sys_roles -> sys.role、
        // sp_products -> sp.product）；归属有歧义/需复合名的表用 BUSINESS_EXCEPTIONS 覆盖
        // （sys_role_permissions -> sys.permission、sp_category_attributes -> sp.categoryAttribute）。
        Map<String, List<String>> byModule = new LinkedHashMap<>();
        for (String table : tables) {
            String domain = moduleOf(table);
            String business = resolveBusinessOf(table);
            String group = business == null ? domain : domain + "." + business;
            byModule.computeIfAbsent(group, k -> new ArrayList<>()).add(table);
        }
        for (Map.Entry<String, List<String>> e : byModule.entrySet()) {
            generateModule(url, username, password, author, e.getKey(), e.getValue());
        }
        System.out.println("生成完成。产物目录：\n  " + outputDir()
                + "\n请 diff 检查后按业务域分批提交（勿把整个生成结果一次提交）。");
    }

    /**
     * 表名 -> 域内业务包名的"例外覆盖"：仅登记通用规则推导不出的归属判断——
     * <ul>
     *   <li>{@code sys_role_permissions}：按首段词应归 role，但其接口宿主是权限管理
     *       （/api/v1/permissions/roles/*），故归 permission；</li>
     *   <li>{@code sp_category_attributes}：需复合业务名 categoryAttribute，避免与未来
     *       通用属性域（sp_attributes）混淆；</li>
     *   <li>{@code sp_category_brands}：需复合业务名 categoryBrand（类目-品牌关联独立成业务，
     *       与 categoryAttribute 拆分口径一致）；</li>
     *   <li>{@code sp_attribute_values}：需复合业务名 attributeValue（属性取值表独立成业务，
     *       与主表 sp_attributes 拆开、与 categoryAttribute/categoryBrand 拆分口径一致）。</li>
     * </ul>
     * 其余表一律走通用规则 {@link #resolveBusinessOf(String)}，无需登记。
     */
    private static final Map<String, String> BUSINESS_EXCEPTIONS = Map.of(
            "sys_role_permissions", "permission",
            "sp_category_attributes", "categoryAttribute",
            "sp_category_brands", "categoryBrand",
            "sp_attribute_values", "attributeValue");

    /**
     * 推导表所属的域内业务名：
     * 1) 命中 {@link #BUSINESS_EXCEPTIONS} 直接返回；
     * 2) 否则取"域前缀后首段词"并做复数转单数（roles -> role、categories -> category）；
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
        int next = rest.indexOf('_');
        String head = next > 0 ? rest.substring(0, next) : rest;
        String singular = singularize(head);
        return singular.isEmpty() ? null : singular;
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
     * 对一个生成单元执行 FastAutoGenerator。
     *
     * @param module 包名段：业务域，或"域.业务子模块"（如 sys.role / sp.brand /
     *               sp.categoryAttribute，见 resolveBusinessOf）。
     *               产物包为 com.example.eshopplatform.<module>.<layer>，
     *               类名去前缀取的是包名首段（sys.role -> sys_；sp.brand -> sp_）。
     */
    private static void generateModule(String url, String user, String pass, String author,
                                       String module, List<String> tables) {
        System.out.println(">> 生成模块 '" + module + "'，表: " + tables);
        // 表前缀 = 模块包名首段 + 下划线（sys.role -> sys_）；用于类名去前缀
        int dot = module.indexOf('.');
        String prefix = (module.isEmpty() ? "" : module.substring(0, dot < 0 ? module.length() : dot) + "_");

        FastAutoGenerator.create(url, user, pass)
                // 全局：作者 / 输出目录（直接落 src/main）
                .globalConfig(builder -> builder
                        .author(author)
                        .outputDir(outputDir())
                        .disableOpenDir())
                // 包结构：com.example.eshopplatform.<module>.{entity,mapper,service,controller}
                //（module 可为"域.业务"，如 sys.role -> com.example.eshopplatform.sys.role.entity）
                .packageConfig(builder -> {
                    builder.parent(PARENT_PACKAGE);
                    if (!module.isEmpty()) {
                        builder.moduleName(module);
                    }
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
                            .formatFileName("%sController");
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
                    // 供模板拼包名用：com.example.eshopplatform[.<模块>].dto
                    String dtoPkg = PARENT_PACKAGE + (module.isEmpty() ? "" : "." + module) + ".dto";
                    builder.customMap(java.util.Map.of("dtoPkg", dtoPkg));
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
