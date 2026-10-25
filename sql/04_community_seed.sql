-- 云农智诊：社区演示种子（用户 / 已审核帖 / 评论）
-- 依赖：02_schema.sql；密码统一 Demo@123（也可由 DataInitializer 在启动时纠正）
-- 可重复执行：用户用 WHERE NOT EXISTS；帖子仅在尚无 APPROVED 帖时写入
USE yunnongzhizhen;

SET NAMES utf8mb4;

-- Demo@123 的 BCrypt（与 DataInitializer 演示账号一致；启动时会按明文重新校验/重置）
SET @demo_hash := '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsF.yH1qJqJqJqJqJq';

-- 若库中已有正确哈希可忽略；优先依赖后端 DataInitializer 写入可靠哈希
-- 这里用与 03_seed 同风格的占位：实际以应用启动初始化为准
SET @demo_hash := '$2a$10$YylIFkDK9NDBY8EtbA90zOCOM1i8lBrrq2msyfWp65wrKEgIDaHeK';

INSERT INTO `user` (`username`, `password_hash`, `nickname`, `role`, `status`, `region`)
SELECT 'farmer_wang', @demo_hash, '种田老王', 'FARMER', 'NORMAL', '湖南长沙'
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'farmer_wang');

INSERT INTO `user` (`username`, `password_hash`, `nickname`, `role`, `status`, `region`)
SELECT 'farmer_zhang', @demo_hash, '小麦张', 'FARMER', 'NORMAL', '河南周口'
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'farmer_zhang');

INSERT INTO `user` (`username`, `password_hash`, `nickname`, `role`, `status`, `region`)
SELECT 'farmer_li', @demo_hash, '玉米李', 'FARMER', 'NORMAL', '吉林长春'
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'farmer_li');

INSERT INTO `user` (`username`, `password_hash`, `nickname`, `role`, `status`, `region`)
SELECT 'farmer_chen', @demo_hash, '翠花种植', 'FARMER', 'NORMAL', '四川眉山'
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'farmer_chen');

INSERT INTO `user` (`username`, `password_hash`, `nickname`, `role`, `status`, `region`)
SELECT 'farmer_liu', @demo_hash, '田间小陈', 'FARMER', 'NORMAL', '江苏盐城'
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'farmer_liu');

INSERT INTO `user` (`username`, `password_hash`, `nickname`, `role`, `status`, `region`)
SELECT 'expert_li', @demo_hash, '李农技', 'EXPERT', 'NORMAL', '湖南农技中心'
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'expert_li');

INSERT INTO `user` (`username`, `password_hash`, `nickname`, `role`, `status`, `region`)
SELECT 'expert_zhao', @demo_hash, '赵专家', 'EXPERT', 'NORMAL', '省植保站'
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'expert_zhao');

UPDATE `user` SET `nickname` = '田间小陈' WHERE `username` = 'farmer_liu' AND `nickname` <> '田间小陈';

-- 仅当尚无已审核帖时写入演示帖与评论
INSERT INTO `post` (`author_id`, `title`, `content`, `images`, `tag`, `status`, `pinned`, `view_count`, `create_time`, `update_time`)
SELECT u.id,
       '春季稻瘟病早期识别与防治要点（专家科普）',
       '稻瘟病是水稻上最重要的真菌病害之一。叶片出现梭形灰绿色病斑时要提高警惕，破口抽穗前后遇连阴雨更易流行。\n建议选用抗病品种、控制氮肥，破口前预防用药，药剂轮换。',
       'experience||/images/hero-farm.jpg',
       'GUIDE', 'APPROVED', 1, 328, '2026-03-18 09:20:00', '2026-03-18 09:20:00'
FROM `user` u
WHERE u.username = 'expert_li'
  AND NOT EXISTS (SELECT 1 FROM `post` WHERE `status` = 'APPROVED' LIMIT 1);

INSERT INTO `post` (`author_id`, `title`, `content`, `images`, `tag`, `status`, `pinned`, `view_count`, `create_time`, `update_time`)
SELECT u.id,
       '玉米叶子发黄长锈斑，求助各位农友！',
       '自家两亩玉米下部叶片出现橙黄色粉状物，露水重。请问是否普通锈病？求用药建议。',
       'help||/images/hero-farm.jpg',
       'NORMAL', 'APPROVED', 0, 156, '2026-03-20 14:05:00', '2026-03-20 14:05:00'
FROM `user` u
WHERE u.username = 'farmer_li'
  AND (SELECT COUNT(*) FROM `post` WHERE `status` = 'APPROVED') BETWEEN 1 AND 1;

INSERT INTO `post` (`author_id`, `title`, `content`, `images`, `tag`, `status`, `pinned`, `view_count`, `create_time`, `update_time`)
SELECT u.id,
       '小麦扬花期防赤霉病，我的三年实操经验',
       '扬花初期用药、雨前雨后盯紧、药剂轮换、控氮通风。欢迎补充各地差异做法。',
       'experience||/images/hero-farm.jpg',
       'NORMAL', 'APPROVED', 0, 210, '2026-03-22 08:40:00', '2026-03-22 08:40:00'
FROM `user` u
WHERE u.username = 'farmer_zhang'
  AND (SELECT COUNT(*) FROM `post` WHERE `status` = 'APPROVED') BETWEEN 2 AND 2;

INSERT INTO `post` (`author_id`, `title`, `content`, `images`, `tag`, `status`, `pinned`, `view_count`, `create_time`, `update_time`)
SELECT u.id,
       '棚室番茄晚疫病：降湿比打药更重要',
       '先通风排湿，再配合药剂保护；发现中心病株立即清除。',
       'experience||/images/hero-farm.jpg',
       'GUIDE', 'APPROVED', 0, 275, '2026-03-24 16:30:00', '2026-03-24 16:30:00'
FROM `user` u
WHERE u.username = 'expert_zhao'
  AND (SELECT COUNT(*) FROM `post` WHERE `status` = 'APPROVED') BETWEEN 3 AND 3;

-- 评论（按标题关联帖子，避免依赖自增 id）
INSERT INTO `comment` (`post_id`, `author_id`, `content`, `is_professional`, `is_quality`, `create_time`, `update_time`)
SELECT p.id, u.id, '写得很清楚，正好我们村最近准备统一预防用药，收藏了！', 0, 0, '2026-03-18 11:02:00', '2026-03-18 11:02:00'
FROM `post` p
JOIN `user` u ON u.username = 'farmer_wang'
WHERE p.title LIKE '春季稻瘟病早期识别%'
  AND NOT EXISTS (
    SELECT 1 FROM `comment` c WHERE c.post_id = p.id AND c.content LIKE '写得很清楚%'
  );

INSERT INTO `comment` (`post_id`, `author_id`, `content`, `is_professional`, `is_quality`, `create_time`, `update_time`)
SELECT p.id, u.id, '补充一点：感病品种田块破口抽穗期建议连续保护两次，间隔 5～7 天。', 1, 1, '2026-03-18 15:40:00', '2026-03-18 15:40:00'
FROM `post` p
JOIN `user` u ON u.username = 'expert_zhao'
WHERE p.title LIKE '春季稻瘟病早期识别%'
  AND NOT EXISTS (
    SELECT 1 FROM `comment` c WHERE c.post_id = p.id AND c.content LIKE '补充一点%'
  );

INSERT INTO `comment` (`post_id`, `author_id`, `content`, `is_professional`, `is_quality`, `create_time`, `update_time`)
SELECT p.id, u.id, '从描述看高度疑似普通锈病。初期可用三唑类药剂，7～10 天一次，连喷 2～3 次。', 1, 1, '2026-03-20 16:18:00', '2026-03-20 16:18:00'
FROM `post` p
JOIN `user` u ON u.username = 'expert_li'
WHERE p.title LIKE '玉米叶子发黄长锈斑%'
  AND NOT EXISTS (
    SELECT 1 FROM `comment` c WHERE c.post_id = p.id AND c.content LIKE '从描述看高度疑似%'
  );

INSERT INTO `comment` (`post_id`, `author_id`, `content`, `is_professional`, `is_quality`, `create_time`, `update_time`)
SELECT p.id, u.id, '我们这边也这样，打了两遍好多了，记得别偏施氮肥。', 0, 0, '2026-03-20 18:05:00', '2026-03-20 18:05:00'
FROM `post` p
JOIN `user` u ON u.username = 'farmer_chen'
WHERE p.title LIKE '玉米叶子发黄长锈斑%'
  AND NOT EXISTS (
    SELECT 1 FROM `comment` c WHERE c.post_id = p.id AND c.content LIKE '我们这边也这样%'
  );

INSERT INTO `comment` (`post_id`, `author_id`, `content`, `is_professional`, `is_quality`, `create_time`, `update_time`)
SELECT p.id, u.id, '学到了，往年扬花遇雨就慌，今年按这个节奏试试看。', 0, 0, '2026-03-22 10:12:00', '2026-03-22 10:12:00'
FROM `post` p
JOIN `user` u ON u.username = 'farmer_wang'
WHERE p.title LIKE '小麦扬花期防赤霉病%'
  AND NOT EXISTS (
    SELECT 1 FROM `comment` c WHERE c.post_id = p.id AND c.content LIKE '学到了%'
  );

INSERT INTO `comment` (`post_id`, `author_id`, `content`, `is_professional`, `is_quality`, `create_time`, `update_time`)
SELECT p.id, u.id, '降湿这点太关键了，去年就是闷棚打药效果差。', 0, 0, '2026-03-24 19:10:00', '2026-03-24 19:10:00'
FROM `post` p
JOIN `user` u ON u.username = 'farmer_chen'
WHERE p.title LIKE '棚室番茄晚疫病%'
  AND NOT EXISTS (
    SELECT 1 FROM `comment` c WHERE c.post_id = p.id AND c.content LIKE '降湿这点太关键%'
  );
