#!/usr/bin/env bash
# ============================================================================
# new-project.sh —— 从本模板（eshop-platform）命令行生成一个全新项目
#
# 用法（在任意目录执行，脚本所在目录即模板源）：
#   /path/to/eshop-platform/new-project.sh <项目名> [选项]
#
# 示例：
#   new-project.sh mall                          # 生成 ./mall
#   new-project.sh my-shop -o ../apps/my-shop    # 指定输出目录
#   new-project.sh mall -p com.acme.mall         # 自定义根包名
#   new-project.sh mall -d mall_db -P mall       # 自定义库名/配置前缀
#
# 项目名规范：小写字母/数字 + 连字符（kebab-case），如 mall / my-shop / demo-api。
# 自动推导三种形态：
#   kebab  my-shop       -> Maven artifact / 目录 / spring.application.name / jar 名
#   flat   myshop        -> Java 包末段（默认 com.example.myshop）
#   Pascal MyShop        -> 启动类名（MyShopApplication）等
# 配置前缀 / docker 容器名 / 库名默认也用 flat 形态，可用 -P / -d 覆盖。
#
# 选项：
#   -o, --out <dir>      输出目录（默认当前目录/<项目名>）
#   -p, --package <pkg>  新 Java 根包（默认 com.example.<flat>）
#   -g, --group <gid>    新 Maven groupId（默认取 --package 去掉最后一段）
#   -d, --db <dbname>    新数据库名（默认 <flat>_db）
#   -P, --prefix <pfx>   新配置前缀（eshop.security -> <pfx>.security）与容器名
#   -f, --force          目标目录已存在且非空时，删除重建
#   -h, --help           显示本帮助
#
# 说明：复制时排除 .git/.idea/target/.m2home/*.iml/.env/HELP.md 与本脚本，
# 生成后请自行 git init；数据库表结构请按新库名导入模板库的 schema。
# ============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

usage() {
    cat <<'EOF'
用法：new-project.sh <项目名> [选项]

项目名：kebab-case（小写字母/数字 + 连字符），如 mall、my-shop、demo-api。

选项：
  -o, --out <dir>      输出目录（默认当前目录/<项目名>）
  -p, --package <pkg>  新 Java 根包（默认 com.example.<flat>）
  -g, --group <gid>    新 Maven groupId（默认取 --package 去掉最后一段）
  -d, --db <dbname>    新数据库名（默认 <flat>_db）
  -P, --prefix <pfx>   新配置前缀（eshop.security -> <pfx>.security）与容器名
  -f, --force          目标目录已存在且非空时，删除重建
  -h, --help           显示本帮助

示例：
  new-project.sh mall
  new-project.sh my-shop -o ../apps/my-shop
  new-project.sh mall -p com.acme.mall -d mall_db
EOF
    exit 0
}

# ---------- 解析参数 ----------
NAME=""
OUT=""
PACKAGE=""
GROUP=""
DB=""
PREFIX=""
FORCE=0

while [[ $# -gt 0 ]]; do
    case "$1" in
        -o|--out)     OUT="$2"; shift 2 ;;
        -p|--package) PACKAGE="$2"; shift 2 ;;
        -g|--group)   GROUP="$2"; shift 2 ;;
        -d|--db)      DB="$2"; shift 2 ;;
        -P|--prefix)  PREFIX="$2"; shift 2 ;;
        -f|--force)   FORCE=1; shift ;;
        -h|--help)    usage ;;
        *)  if [[ -z "$NAME" ]]; then NAME="$1"; shift; else
                echo "错误：多余的参数 $1" >&2; usage; fi ;;
    esac
done

# ---------- 校验与推导 ----------
if [[ -z "$NAME" ]]; then
    echo "错误：缺少项目名" >&2
    usage
fi
if ! [[ "$NAME" =~ ^[a-z][a-z0-9-]*$ ]]; then
    echo "错误：项目名必须为 kebab-case（小写字母/数字 + 连字符），如 mall、my-shop" >&2
    exit 1
fi
if [[ "$NAME" == *eshop* ]]; then
    echo "错误：项目名不能包含 'eshop'" >&2
    exit 1
fi

FLAT=$(printf '%s' "$NAME" | tr -d '-')
PASCAL=$(printf '%s' "$NAME" | perl -pe 's/(^|-)([a-z])/\u$2/g')
DB="${DB:-${FLAT}_db}"
PREFIX="${PREFIX:-$FLAT}"
PACKAGE="${PACKAGE:-com.example.$FLAT}"
GROUP="${GROUP:-$(printf '%s' "$PACKAGE" | sed 's/\.[^.]*$//')}"

# 输出目录：相对路径基于当前工作目录解析为绝对路径
if [[ -z "$OUT" ]]; then
    OUT="$(pwd)/$NAME"
else
    mkdir -p "$OUT"
    OUT="$(cd "$OUT" && pwd)"
fi

echo "==================================================="
echo " 生成新项目：$NAME"
echo "  输出目录 : $OUT"
echo "  kebab    : $NAME        (artifact / app 名 / jar)"
echo "  flat     : $FLAT        (Java 包末段)"
echo "  Pascal   : $PASCAL      (启动类名)"
echo "  Java 包  : $PACKAGE     (groupId=$GROUP)"
echo "  数据库   : $DB"
echo "  前缀     : $PREFIX.security.*  容器: ${PREFIX}-mysql/redis/app"
echo "==================================================="

# ---------- 安全检查 ----------
if [[ "$OUT" == "/" || "$OUT" == "$SCRIPT_DIR" ]]; then
    echo "错误：非法输出目录 $OUT" >&2
    exit 1
fi
if [[ -d "$OUT" && -n "$(ls -A "$OUT" 2>/dev/null)" ]]; then
    if [[ $FORCE -eq 1 ]]; then
        rm -rf "$OUT"
    else
        echo "错误：目标目录 $OUT 已存在且非空（用 -f 删除重建）" >&2
        exit 1
    fi
fi
mkdir -p "$OUT"

# ---------- 复制模板（排除环境/构建/IDE 文件） ----------
# 若输出目录恰好位于模板目录内部，排除其第一级路径，避免递归复制
REL_OUT=""
case "$OUT" in
    "$SCRIPT_DIR"/*) REL_OUT="${OUT#"$SCRIPT_DIR"/}" ;;
esac

rsync_args=(-a --exclude='.git' --exclude='.idea' --exclude='target'
    --exclude='.m2home' --exclude='*.iml' --exclude='.env'
    --exclude='HELP.md' --exclude='todo.md' --exclude='roadmap.md'
    --exclude='new-project.sh' --exclude='.DS_Store')
if [[ -n "$REL_OUT" ]]; then
    rsync_args+=(--exclude="/${REL_OUT%%/*}")
fi
rsync "${rsync_args[@]}" "$SCRIPT_DIR/" "$OUT/"

# ---------- 内容替换（最长 token 优先，防止误伤 eshop_db / eshop.security） ----------
# bash3 无 mapfile，用 while read 收集文本文件列表
FILES=()
while IFS= read -r f; do
    FILES+=("$f")
done < <(find "$OUT" -type f \( \
    -name '*.java' -o -name '*.xml' -o -name '*.yml' -o -name '*.yaml' \
    -o -name '*.properties' -o -name '*.md' -o -name '*.ftl' \
    -o -name 'Makefile' -o -name 'Dockerfile' -o -name 'mvnw' \
    -o -name 'mvnw.cmd' -o -name '.gitignore' -o -name '.gitattributes' \
    -o -name '*.example' \) 2>/dev/null)

PKG_SLASH=$(printf '%s' "$PACKAGE" | tr '.' '/')
PKG="$PACKAGE" PKGS="$PKG_SLASH" K="$NAME" F="$FLAT" C="$PASCAL" \
D="$DB" PFX="$PREFIX" perl -pi -e '
    s{\Qcom.example.eshopplatform\E}{$ENV{PKG}}g;      # 点分全包名
    s{\Qcom/example/eshopplatform\E}{$ENV{PKGS}}g;     # README 中的斜杠路径
    s{\Qeshop-platform\E}{$ENV{K}}g;                   # kebab：artifact/jar/app 名
    s{\Qeshopplatform\E}{$ENV{F}}g;                    # 残留的连写名（包末段）
    s{\QEshopPlatform\E}{$ENV{C}}g;                    # Pascal：类名
    s{\Qeshop_db\E}{$ENV{D}}g;                         # 库名
    s{\Qeshop.security\E}{$ENV{PFX}.security}g;        # 配置前缀（先于兜底）
    s{\Qeshop-mysql\E}{$ENV{PFX}-mysql}g;              # docker 容器名
    s{\Qeshop-redis\E}{$ENV{PFX}-redis}g;
    s{\Qeshop-app\E}{$ENV{PFX}-app}g;
    s{\Qeshop\E}{$ENV{PFX}}g;                          # 兜底：yml 根 key/注释
' "${FILES[@]}"

# groupId（仅当与 com.example 不同才需替换 pom）
if [[ "$GROUP" != "com.example" ]]; then
    G="$GROUP" perl -pi -e 's{<groupId>com\.example</groupId>}{<groupId>$ENV{G}</groupId>}' \
        "$OUT/pom.xml"
fi

# ---------- 迁移 Java 包目录 ----------
OLD_PKG_DIR="com/example/eshopplatform"
NEW_PKG_DIR=$(printf '%s' "$PACKAGE" | tr '.' '/')
for base in src/main/java src/test/java; do
    old="$OUT/$base/$OLD_PKG_DIR"
    new="$OUT/$base/$NEW_PKG_DIR"
    if [[ -d "$old" ]]; then
        mkdir -p "$(dirname "$new")"
        mv "$old" "$new"
    fi
done
# 清理迁移后遗留的空父目录（如 com/example）
find "$OUT/src" -depth -type d -empty -delete 2>/dev/null || true

# ---------- 重命名启动类 / 测试类文件（类名=文件名） ----------
while IFS= read -r f; do
    dir=$(dirname "$f")
    base=$(basename "$f")
    mv "$f" "$dir/$(printf '%s' "$base" | sed "s/EshopPlatform/$PASCAL/")"
done < <(find "$OUT/src" -type f -name 'EshopPlatform*.java')

echo "✔ 生成完成：$OUT"
echo
echo "下一步："
echo "  cd $OUT && git init          # 初始化仓库"
echo "  make test                    # 编译并跑测试（首次会下载依赖）"
echo "  按需核对：pom.xml 描述 / README 标题 / application.yml 敏感项"
echo "  数据库：请创建 $DB 并导入模板库（eshop_db）的表结构"
