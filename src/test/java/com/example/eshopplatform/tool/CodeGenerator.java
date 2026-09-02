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
 * 入参出参走 dto 的 Req/VO），生成后仍需人工核对
 * （类名单复数、Req/VO 字段裁剪与校验、controller 路径/权限与统一返回
 * {@code ApiResponse} 等业务化改造）。
 *
 * <p><b>包结构约定</b>（工程约定）：表前缀 = 业务域模块；**类名去掉前缀**（域已由包表达），
 * 生成到 {@code com.example.eshopplatform.<前缀>} 下的 entity / mapper / service /
 * controller / dto。
 * 例如 {@code usr_users} → 前缀 {@code usr} → {@code com.example.eshopplatform.usr.entity.Users}
 * （类名已去 {@code usr_} 前缀，{@code @TableName} 仍为 {@code usr_users}），
 * 同域 dto 生成 {@code UsersVO / UsersReq} 等。
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
 *   #    不带参数 = 按前缀分组生成全表；带参数 = 只生成指定表，如：usr_users usr_addresses
 *   #    数据库连接默认同 application.yml 开发配置，可用 -Ddb.url/-Ddb.username/-Ddb.password 覆盖
 * </pre>
 */
public class CodeGenerator {

    /** 生成代码的根包（与主代码一致，勿改） */
    private static final String PARENT_PACKAGE = "com.example.eshopplatform";

    /** 主代码根目录：src/main/java（产物直接落盘，生成后 diff 检查再提交） */
    private static final String JAVA_DIR =
            new File("").getAbsolutePath() + "/src/main/java";

    public static void main(String[] args) throws Exception {
        // 数据库连接：默认与 src/main/resources/application.yml 开发配置一致，可用系统属性覆盖
        String url = System.getProperty("db.url",
                "jdbc:mysql://127.0.0.1:3306/eshop_db?useUnicode=true&characterEncoding=utf8"
                        + "&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true");
        String username = System.getProperty("db.username", "root");
        String password = System.getProperty("db.password", "123456");
        String author = System.getProperty("gen.author", "yacolin");

        // 待生成表：未传参则取库中全部业务表
        List<String> tables = args.length > 0
                ? List.of(args)
                : listAllTables(url, username, password);
        System.out.println("待生成表(" + tables.size() + "): " + tables);

        // 按表前缀分组 => 每个前缀是一个业务域模块，逐组执行生成
        Map<String, List<String>> byModule = new LinkedHashMap<>();
        for (String table : tables) {
            String module = moduleOf(table);
            byModule.computeIfAbsent(module, k -> new ArrayList<>()).add(table);
        }
        for (Map.Entry<String, List<String>> e : byModule.entrySet()) {
            generateModule(url, username, password, author, e.getKey(), e.getValue());
        }
        System.out.println("生成完成。产物目录：\n  " + JAVA_DIR
                + "\n请 diff 检查后按业务域分批提交（勿把整个生成结果一次提交）。");
    }

    /** 表前缀 = 业务域模块名（首个下划线前的小写词，如 sp_products -> sp）；无前缀表归入根包 */
    private static String moduleOf(String table) {
        int idx = table.indexOf('_');
        return idx > 0 ? table.substring(0, idx) : "";
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

    /** 对一个业务域模块执行 FastAutoGenerator */
    private static void generateModule(String url, String user, String pass, String author,
                                       String module, List<String> tables) {
        System.out.println(">> 生成模块 '" + module + "'，表: " + tables);

        FastAutoGenerator.create(url, user, pass)
                // 全局：作者 / 输出目录（直接落 src/main）
                .globalConfig(builder -> builder
                        .author(author)
                        .outputDir(JAVA_DIR)
                        .disableOpenDir())
                // 包结构：com.example.eshopplatform.<module>.{entity,mapper,service,controller}
                .packageConfig(builder -> {
                    builder.parent(PARENT_PACKAGE);
                    if (!module.isEmpty()) {
                        builder.moduleName(module);
                    }
                })
                // 命名：按模块去掉表前缀（类名不再带 sp/usr 等域前缀，如 sp_brands -> Brands，
                // @TableName 仍保留原表名 sp_brands）
                .strategyConfig(builder -> {
                    builder.addInclude(tables.toArray(new String[0]));
                    if (!module.isEmpty()) {
                        builder.addTablePrefix(module + "_");
                    }
                    builder.entityBuilder()
                            .enableLombok()                 // 实体用 Lombok（省 getter/setter）
                            .enableTableFieldAnnotation();   // 字段一律 @TableField，杜绝命名歧义
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
                            .formatMapperFileName("%sMapper");
                })
                // DTO：每个业务域一个 dto 包，每表生成 <实体>VO / <实体>Req
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
                            .fileName("Req.java")
                            .packageName("dto")
                            .templatePath("templates/req.java.ftl"));
                })
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}
