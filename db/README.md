# 数据库（建库 / 建表 / 种子）

建库、建表、种子脚本整体取自 gf-eshop 同源的已开发电商业务 schema（MySQL 8）：
73 张业务表按域前缀 usr/sp/tx/mch/mkt/rev/sys/base 拆分建表文件，无外键、关联由
业务层保证，配套种子脚本一并内附。本目录是这份业务库 SQL 在仓库内的拷贝，
脚本可直接对本机或新库执行。

## 目录结构

| 路径 | 说明 |
|---|---|
| `init/01_create_db.sql` | 建库（docker-compose 挂载 `/docker-entrypoint-initdb.d`，全新数据卷首次启动自动建 `eshop_db`） |
| `00_drop_tables.sql` | 清空全部表（73 张，逆依赖顺序 + `FOREIGN_KEY_CHECKS=0`） |
| `{base,mch,usr,sp,sys,tx,mkt,rev}_p*.sql` | 建表：按业务域拆分、域内 P0~P5 编号；**真实依赖顺序见 `reset_db.sh`**（沿用 std `run.sh` 的 cat 顺序） |
| `seed/` | 种子数据：`seed_clean.sql`（清空业务表 + 重置自增）、`seed_rbac.sql`（RBAC/角色/员工/消费者等基础数据）、`seed_*.py`（Python 批量测试数据，入口 `seed_test_data.py`） |
| `reset_db.sh` | 建库（不存在时）→ 清空 → 按依赖重建全部表（仅结构，无种子） |
| `seed.sh` | 清空业务数据 → RBAC/基础种子 → Python 批量测试数据（`--clean` 幂等可重复执行） |

## 用法

```bash
make db-init     # 仅初始化结构：建库 + 清空 + 重建 73 张表（有数据时谨慎执行）
make db-seed     # 仅灌开发种子（依赖 python3 + pymysql；生产切勿执行）
make db-reset    # 开发环境一步重置：结构 + 种子
```

也可直接执行脚本，连接参数与环境变量一致，可用 `DB_NAME` / `MYSQL_HOST` /
`MYSQL_PORT` / `MYSQL_USER` / `MYSQL_PASSWORD` 覆盖：

```bash
./db/reset_db.sh                 # 默认 eshop_db、本机 3306、root/123456
DB_NAME=shop_test ./db/reset_db.sh && DB_NAME=shop_test ./db/seed.sh
```

> 各 SQL 文件自带的 `USE eshop_db;` 由脚本执行时剥离、改以 `DB_NAME` 显式选库，
> 因此换库名执行**无需改任何文件内容**。

## 维护约定

- 上游为 gf-eshop 同源的业务库表结构：改表结构先改上游业务，再同步本目录拷贝
  与实体注解；不要在本目录手改出与上游不一致的 schema（除非业务有意分叉）；
- 已记录的修正（上游种子间漂移，内附副本已兼容，同步上游后可回退）：
  - `seed/seed_clean.sql`：`mch_roles` / `mch_role_permissions` 系上游沿用旧表名，
    已按现 schema 改为 `mch_merchant_roles` / `mch_merchant_role_permissions`；
  - `seed.sh` 第 3 步以 `seed_test_data.py --clean` 执行：上游 RBAC 种子与 Python
    种子对 `usr_levels` 等固定键数据重复定义，`--clean` 先自清业务表再生成；
- 实体 / 写接口语义与 schema 一致：时间列由数据库自动维护，主键策略在实体
  `@TableId` 显式声明并与 DDL 一致（如 sp_brands 自增 `IdType.AUTO`）；
- 无 Flyway：本机 `eshop_db` 已建好直接连接；新环境先 `make db-init` 建结构、
  `make db-seed` 灌开发数据（或 docker-compose 全新卷自动建库后再在容器内执行）。

## 模板语义

本仓库是脚手架模板（`new-project.sh` 整体复制，docker-compose 挂载 `./db/init`）。
生成新项目后按新库替换 `db/init/01_create_db.sql` 中的库名，并清理/替换本目录为
新项目自己的 schema（同 lease-platform 做法：每个项目内附各自数据库设计）。
