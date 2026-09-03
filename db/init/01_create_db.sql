-- ============================================================================
-- 建库（MySQL 首次启动挂载 /docker-entrypoint-initdb.d 自动执行；本地已存在则跳过）
-- 表结构与种子由 db/ 下的脚本完成（make db-init / make db-seed），本文件只建库。
-- ============================================================================
CREATE DATABASE IF NOT EXISTS `eshop_db`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
