#!/usr/bin/env bash
# ============================================================================
# 初始化数据库结构：检查并创建数据库（不存在时）→ 清空全部表 → 按依赖顺序重建
# 表结构与建表顺序取自 gf-eshop 同源的电商业务库 schema（MySQL 8，73 张表）。
#
# 用法：./db/reset_db.sh                       （默认库 eshop_db，本机 3306）
#       DB_NAME=xxx ./db/reset_db.sh           （指定其他库）
#       MYSQL_PASSWORD=xxx ./db/reset_db.sh    （连接参数：HOST/PORT/USER/PASSWORD）
#
# 注意：会清空并重建目标库内全部 73 张表（DROP 后再建），有数据时谨慎执行；
# 纯结构初始化（不灌种子），种子见 make db-seed / ./db/seed.sh。
# ============================================================================
set -euo pipefail

DB_NAME="${DB_NAME:-eshop_db}"
MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-123456}" # 默认本机 Homebrew MySQL root 密码，可用环境变量覆盖

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

MYSQL=(mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER")
[ -n "$MYSQL_PASSWORD" ] && MYSQL+=(-p"$MYSQL_PASSWORD")

echo "==> 检查数据库 $DB_NAME 是否存在..."
"${MYSQL[@]}" -e "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 各 SQL 文件自带 `USE eshop_db;`，这里剥离后由 -D "$DB_NAME" 显式选库，支持 DB_NAME 覆盖
strip_use() { sed '/^USE eshop_db;$/d' "$1"; }

echo "==> 清空并重建 ${DB_NAME}（DROP 全部旧表 -> 按依赖顺序建表）..."
{
  strip_use "$SCRIPT_DIR/00_drop_tables.sql"
  # 依赖顺序按分层批次：P0 → P1 → P1.5 → P2 → P3（仓库先于库存）→ P4 → P5
  for f in base_p0 mch_p0 usr_p0 sp_p0 sys_p0 \
           mch_p1 sp_p1 tx_p0 mkt_p0 rev_p0 \
           usr_p1 \
           sp_p2 tx_p1 tx_p2 mkt_p1 rev_p1 \
           sp_p4 sp_p3 \
           mch_p2 tx_p3 tx_p4 \
           sp_p5; do
    strip_use "$SCRIPT_DIR/$f.sql"
  done
} | "${MYSQL[@]}" "$DB_NAME"

TOTAL=$(grep -rh "^CREATE TABLE" "$SCRIPT_DIR"/*.sql | wc -l | tr -d ' ')
echo "==> 完成：$DB_NAME 共创建 ${TOTAL} 张表（不含种子，种子执行 ./db/seed.sh）"
