#!/usr/bin/env bash
# ============================================================================
# 灌入开发种子数据（可重复执行）：清空业务数据 -> RBAC/基础种子 -> Python 批量测试数据
# 编排与数据源取自配套的电商业务库种子脚本（已直接搬入 db/seed/）。
# 说明：上游 rbac 种子与 Python 种子对 usr_levels 等固定键数据存在重复定义（漂移），
# 故第 3 步统一以 seed_test_data.py --clean 先自清业务表再生成，保证可重复执行。
#
# 用法：./db/seed.sh                        （默认库 eshop_db，本机 3306，root/123456）
#       DB_NAME=xxx ./db/seed.sh            （指定其他库；Python 端读取同名单环境变量）
#       MYSQL_PASSWORD=xxx ./db/seed.sh     （连接参数：HOST/PORT/USER/PASSWORD）
#
# 前置：表结构已存在（make db-init / ./db/reset_db.sh）；依赖 python3 + pymysql。
# 注意：会清空业务种子表后重建，生产环境切勿执行。
# ============================================================================
set -euo pipefail

DB_NAME="${DB_NAME:-eshop_db}"
MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-123456}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SEED_DIR="$SCRIPT_DIR/seed"

# Python 种子（seed_common.py）读取 DB_HOST/DB_PORT/DB_USER/DB_PASSWORD/DB_NAME，做一次映射导出
export DB_NAME DB_HOST="$MYSQL_HOST" DB_PORT="$MYSQL_PORT" DB_USER="$MYSQL_USER" DB_PASSWORD="$MYSQL_PASSWORD"

MYSQL=(mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER")
[ -n "$MYSQL_PASSWORD" ] && MYSQL+=(-p"$MYSQL_PASSWORD")

strip_use() { sed '/^USE eshop_db;$/d' "$1"; }

echo "==> 第 1 步：清空旧种子数据（seed_clean.sql）..."
echo "==> 第 2 步：RBAC / 基础种子（seed_rbac.sql）..."
{
  strip_use "$SEED_DIR/seed_clean.sql"
  strip_use "$SEED_DIR/seed_rbac.sql"
} | "${MYSQL[@]}" "$DB_NAME"

echo "==> 第 3 步：Python 批量测试数据（seed_test_data.py --clean，先自清业务表再生成）..."
cd "$SEED_DIR"
python3 seed_test_data.py --clean
echo "==> 完成：$DB_NAME 种子灌入完毕"
