-- 迁移：删除病害资料层级字段（已有库执行一次即可）
USE yunnongzhizhen;

SET NAMES utf8mb4;

-- MySQL 8+ 可直接 DROP INDEX；若索引名不同可忽略报错后继续
ALTER TABLE `disease` DROP INDEX `idx_disease_level`;
ALTER TABLE `disease` DROP COLUMN `level`;
