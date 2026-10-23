-- 云农智诊：种子数据
-- 依赖：已执行 02_schema.sql
-- ADMIN 密码为 Admin@123 的 BCrypt；也可由应用启动时 DataInitializer 自动创建
USE yunnongzhizhen;

SET NAMES utf8mb4;

-- 管理员建议由应用启动时 DataInitializer 创建/纠正（admin / Admin@123）
-- 以下哈希对应 Admin@123；若与运行中不一致，重启后端会自动重置
INSERT INTO `user` (`username`, `password_hash`, `nickname`, `role`, `status`)
SELECT 'admin',
       '$2a$10$YylIFkDK9NDBY8EtbA90zOCOM1i8lBrrq2msyfWp65wrKEgIDaHeK',
       '系统管理员',
       'ADMIN',
       'NORMAL'
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin');

-- 纠正错误种子哈希（可选，重复执行安全）
UPDATE `user`
SET `password_hash` = '$2a$10$YylIFkDK9NDBY8EtbA90zOCOM1i8lBrrq2msyfWp65wrKEgIDaHeK',
    `role` = 'ADMIN',
    `status` = 'NORMAL'
WHERE `username` = 'admin';

INSERT INTO `crop` (`name`, `description`)
SELECT '水稻', '常见粮食作物'
WHERE NOT EXISTS (SELECT 1 FROM `crop` WHERE `name` = '水稻');

INSERT INTO `crop` (`name`, `description`)
SELECT '小麦', '常见粮食作物'
WHERE NOT EXISTS (SELECT 1 FROM `crop` WHERE `name` = '小麦');

INSERT INTO `disease` (`crop_id`, `name`, `description`, `prevention_advice`, `level`)
SELECT c.id, '稻瘟病', '叶片出现梭形病斑', '选用抗病品种，合理密植', 'BASIC'
FROM `crop` c WHERE c.name = '水稻'
  AND NOT EXISTS (SELECT 1 FROM `disease` d WHERE d.name = '稻瘟病' AND d.crop_id = c.id);

INSERT INTO `disease` (`crop_id`, `name`, `description`, `prevention_advice`, `level`)
SELECT c.id, '纹枯病专业防治', '专业级纹枯病资料', '药剂轮换与田间管理综合方案', 'PRO'
FROM `crop` c WHERE c.name = '水稻'
  AND NOT EXISTS (SELECT 1 FROM `disease` d WHERE d.name = '纹枯病专业防治' AND d.crop_id = c.id);
