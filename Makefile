# 电商小程序平台（eshop-platform）—— Spring Boot 4 / Java 21 / Maven
# 常用：make run（启动项目） make build（打包） make test（跑测试）
#       make gen（MyBatis-Plus 代码生成） make help（全部命令）
# 全部命令：make help

.PHONY: help run stop compile test build run-jar gen

# Maven 调用：把 Maven 本地仓库重定向到工作区 .m2home（已 gitignore），
# 受限环境（沙箱等无法写 ~/.m2）与正常开发环境同样适用；
# 如需追加参数：make MVN_ARGS="-X" run
MVN := MAVEN_USER_HOME=$(CURDIR)/.m2home ./mvnw -Dmaven.repo.local=$(CURDIR)/.m2home/repository
JAR := target/eshop-platform-0.0.1-SNAPSHOT.jar

# ---- 代码生成（MyBatis-Plus FastAutoGenerator，见 src/test/.../tool/CodeGenerator.java）----
GEN_MAIN := com.example.eshopplatform.tool.CodeGenerator
# build-classpath 输出的精确 test classpath 文件（target 已被 gitignore）
GEN_CP := target/gen-classpath.txt
# 用法：
#   make gen                                    # 生成全部业务表（自动按表前缀分域）
#   make gen DOMAIN=sp                          # 只生成 sp_ 域全部表（表多时一把梭）
#   make gen GEN_TABLES="usr_users usr_infos"   # 只生成指定表（表少时用）
#   make gen GEN_OPTS="-Ddb.password=xxx -Dgen.author=me"  # 覆盖数据库连接/作者

help: ## 显示所有命令
	@grep -E '^[a-zA-Z_-]+:.*?## ' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  %-12s %s\n", $$1, $$2}'

run: ## 启动项目（开发模式，前台运行，端口 8080）
	$(MVN) spring-boot:run $(MVN_ARGS)

stop: ## 停止 :8080 端口上的服务
	-@lsof -ti :8080 | xargs kill -9 2>/dev/null

compile: ## 编译（跳过测试）
	$(MVN) -DskipTests compile $(MVN_ARGS)

test: ## 运行测试
	$(MVN) test $(MVN_ARGS)

build: ## 打包可执行 jar（跳过测试）
	$(MVN) -DskipTests package $(MVN_ARGS)

run-jar: ## 运行已打包的 jar（需先 make build）
	java -jar $(JAR)

gen: ## 运行 MyBatis-Plus 代码生成器（DOMAIN=域前缀 整域；GEN_TABLES=表名列表；缺省全表；GEN_OPTS=-Ddb.*/-Dgen.author）
	$(MVN) -q test-compile
	$(MVN) -q dependency:build-classpath -DincludeScope=test -Dmdep.outputFile=$(GEN_CP)
	java $(GEN_OPTS) -cp "target/classes:target/test-classes:$$(cat $(GEN_CP))" $(GEN_MAIN) $(if $(DOMAIN),$(DOMAIN),$(GEN_TABLES))
