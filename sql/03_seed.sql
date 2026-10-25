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

-- ----------------------------
-- 作物（与前端病害库演示对齐）
-- ----------------------------
INSERT INTO `crop` (`name`, `description`)
SELECT '水稻', '常见粮食作物'
WHERE NOT EXISTS (SELECT 1 FROM `crop` WHERE `name` = '水稻');

INSERT INTO `crop` (`name`, `description`)
SELECT '小麦', '常见粮食作物'
WHERE NOT EXISTS (SELECT 1 FROM `crop` WHERE `name` = '小麦');

INSERT INTO `crop` (`name`, `description`)
SELECT '玉米', '常见粮食作物'
WHERE NOT EXISTS (SELECT 1 FROM `crop` WHERE `name` = '玉米');

INSERT INTO `crop` (`name`, `description`)
SELECT '番茄', '常见蔬菜作物'
WHERE NOT EXISTS (SELECT 1 FROM `crop` WHERE `name` = '番茄');

INSERT INTO `crop` (`name`, `description`)
SELECT '苹果', '常见果树'
WHERE NOT EXISTS (SELECT 1 FROM `crop` WHERE `name` = '苹果');

-- ----------------------------
-- 病害库（6 条，与 diseaseCatalog.MOCK_DISEASES 一致）
-- ----------------------------
INSERT INTO `disease` (`crop_id`, `name`, `description`, `prevention_advice`)
SELECT c.id, '稻瘟病',
       '【发病原因】由稻瘟病菌（Magnaporthe oryzae）侵染引起。病谷、病稻草是重要初侵染源，分生孢子随气流传播，在叶片表面有水膜时侵入。\n【发病条件】适温（24～28℃）配合高湿、连阴雨、露水重的天气最易流行；偏施氮肥、密植郁闭、感病品种会显著加重发生。\n【描述】稻瘟病是水稻上危害最重的真菌性病害之一，可发生在叶片、节、穗颈等部位，流行年份可造成大幅度减产。',
       '选用抗病品种；浅水勤灌，避免长期深水与偏氮；破口抽穗期用三环唑、稻瘟灵等预防，发病后改用春雷霉素等治疗剂，注意轮换用药。'
FROM `crop` c WHERE c.name = '水稻'
  AND NOT EXISTS (SELECT 1 FROM `disease` d WHERE d.name = '稻瘟病' AND d.crop_id = c.id);

INSERT INTO `disease` (`crop_id`, `name`, `description`, `prevention_advice`)
SELECT c.id, '水稻纹枯病',
       '【发病原因】由立枯丝核菌（Rhizoctonia solani）侵染引起。菌核在土壤与病残体上越冬，随水流与农事操作扩散。\n【发病条件】高肥密植、长期深水、高温高湿天气利于扩展；氮肥偏多、田间郁闭会显著加重。\n【描述】水稻纹枯病主要危害叶鞘与茎秆，近水面处出现灰绿色水渍状病斑，后扩大为云纹状，严重时引起倒伏与枯孕穗。',
       '合理密植与浅水勤灌；控制氮肥；打捞菌核；孕穗前后用井岗霉素、噻呋酰胺等药剂保护，注意轮换用药。'
FROM `crop` c WHERE c.name = '水稻'
  AND NOT EXISTS (SELECT 1 FROM `disease` d WHERE d.name IN ('水稻纹枯病', '纹枯病专业防治') AND d.crop_id = c.id);

INSERT INTO `disease` (`crop_id`, `name`, `description`, `prevention_advice`)
SELECT c.id, '玉米普通锈病',
       '【发病原因】由玉米柄锈菌侵染引起。夏孢子随气流远距离传播，田间病残体与自生苗可提供菌源。\n【发病条件】凉爽高湿、昼夜温差大、叶面结露时间长时扩展快；感病品种、过密种植、偏施氮肥易加重。\n【描述】玉米普通锈病以叶部橙黄色夏孢子堆为典型症状，严重时叶片早枯，影响灌浆与产量。',
       '清除病残体，选用抗病品种；发病初期喷施三唑类或代森锰锌类药剂，7～10 天一次，连喷 2～3 次；保持通风，控制氮肥。'
FROM `crop` c WHERE c.name = '玉米'
  AND NOT EXISTS (SELECT 1 FROM `disease` d WHERE d.name = '玉米普通锈病' AND d.crop_id = c.id);

INSERT INTO `disease` (`crop_id`, `name`, `description`, `prevention_advice`)
SELECT c.id, '小麦赤霉病',
       '【发病原因】主要由禾谷镰孢等镰刀菌侵染。病残体与土壤带菌，孢子借风雨飞溅到花器侵入。\n【发病条件】扬花期遇连阴雨、高温高湿最易大发生；感病品种、氮肥过量、田间郁闭会提高风险。\n【描述】小麦赤霉病主要危害穗部，形成粉红霉层与枯白穗，籽粒皱缩，还可能产生真菌毒素，影响食品安全。',
       '扬花初期及时预防用药（氰烯菌酯、戊唑醇等）；雨前雨后补喷；收获后清除病残，合理轮作与控氮。'
FROM `crop` c WHERE c.name = '小麦'
  AND NOT EXISTS (SELECT 1 FROM `disease` d WHERE d.name = '小麦赤霉病' AND d.crop_id = c.id);

INSERT INTO `disease` (`crop_id`, `name`, `description`, `prevention_advice`)
SELECT c.id, '番茄晚疫病',
       '【发病原因】由致病疫霉（Phytophthora infestans）侵染。病残体与种薯（相关寄主）可带菌，孢子囊随风雨扩散。\n【发病条件】冷凉高湿、叶面结露、通风不良时极易流行；连作田与密植棚室风险更高。\n【描述】番茄晚疫病可在短期内造成叶片、茎秆与果实大面积坏死，棚室与露地均可暴发。',
       '降温排湿，避免叶面长时间结露；发现中心病株立即清除；烯酰吗啉、霜脲氰等药剂保护与治疗交替使用。'
FROM `crop` c WHERE c.name = '番茄'
  AND NOT EXISTS (SELECT 1 FROM `disease` d WHERE d.name = '番茄晚疫病' AND d.crop_id = c.id);

INSERT INTO `disease` (`crop_id`, `name`, `description`, `prevention_advice`)
SELECT c.id, '苹果黑星病',
       '【发病原因】由苹果黑星病菌侵染。病叶病果越冬，子囊孢子与分生孢子借风雨侵染幼嫩组织。\n【发病条件】春季多雨、湿润凉爽时初侵染高峰明显；树冠郁闭、清园不彻底会加重全年发生。\n【描述】苹果黑星病危害叶片与果实，形成橄榄色至黑色病斑，导致早期落叶与果实商品性下降。',
       '秋冬彻底清园；萌芽前石硫合剂；生长季节保护性与治疗性杀菌剂交替，重点抓好幼果期防治。'
FROM `crop` c WHERE c.name = '苹果'
  AND NOT EXISTS (SELECT 1 FROM `disease` d WHERE d.name = '苹果黑星病' AND d.crop_id = c.id);

-- 旧名「纹枯病专业防治」：若已有「水稻纹枯病」则删除旧名，否则改名（避免重复）
DELETE d FROM `disease` d
INNER JOIN `crop` c ON d.crop_id = c.id AND c.name = '水稻'
WHERE d.name = '纹枯病专业防治'
  AND EXISTS (
    SELECT 1 FROM `disease` x
    WHERE x.crop_id = d.crop_id AND x.name = '水稻纹枯病' AND x.id <> d.id
  );

UPDATE `disease`
SET `name` = '水稻纹枯病',
    `description` = '【发病原因】由立枯丝核菌（Rhizoctonia solani）侵染引起。菌核在土壤与病残体上越冬，随水流与农事操作扩散。\n【发病条件】高肥密植、长期深水、高温高湿天气利于扩展；氮肥偏多、田间郁闭会显著加重。\n【描述】水稻纹枯病主要危害叶鞘与茎秆，近水面处出现灰绿色水渍状病斑，后扩大为云纹状，严重时引起倒伏与枯孕穗。',
    `prevention_advice` = '合理密植与浅水勤灌；控制氮肥；打捞菌核；孕穗前后用井岗霉素、噻呋酰胺等药剂保护，注意轮换用药。'
WHERE `name` = '纹枯病专业防治';

-- 同作物「水稻纹枯病」去重，保留最小 id
DELETE d1 FROM `disease` d1
INNER JOIN `disease` d2
  ON d1.crop_id = d2.crop_id
 AND d1.name = d2.name
 AND d1.name = '水稻纹枯病'
 AND d1.id > d2.id;

-- ----------------------------
-- 资讯（与 newsMock.MOCK_NEWS 对齐；空库时插入）
-- ----------------------------
INSERT INTO `news` (`title`, `content`, `cover`, `status`, `create_time`, `update_time`)
SELECT '春季田间管理要点：控水控肥防病害',
       '进入返青拔节期，需根据墒情合理灌溉，避免偏施氮肥，重点监测叶部病害早期症状。\n\n春季是作物生长与病虫害交错上升的关键阶段。建议浅水勤灌、增施磷钾肥，结合田间巡查及时发现病斑并取样识别。',
       '/images/news/spring-field.png', 'ONLINE', '2026-10-24 08:30:00', '2026-10-24 08:30:00'
WHERE NOT EXISTS (SELECT 1 FROM `news` WHERE `title` = '春季田间管理要点：控水控肥防病害');

INSERT INTO `news` (`title`, `content`, `cover`, `status`, `create_time`, `update_time`)
SELECT '南方稻区注意稻瘟病早期监测',
       '近期温湿度适宜稻瘟病菌萌发，破口抽穗前应做好预防用药与田间观察。\n\n稻瘟病流行与气候密切相关。农户可关注当地农技部门预警，选用抗病品种，并在关键生育期落实药剂保护。',
       '/images/news/rice-blast-monitor.png', 'ONLINE', '2026-10-24 07:50:00', '2026-10-24 07:50:00'
WHERE NOT EXISTS (SELECT 1 FROM `news` WHERE `title` = '南方稻区注意稻瘟病早期监测');

INSERT INTO `news` (`title`, `content`, `cover`, `status`, `create_time`, `update_time`)
SELECT '智能识别如何辅助基层农技服务',
       '拍照识病可缩短初诊时间，结合问诊建议为农户提供可操作的防治路径。\n\n基层农技员可将 AI 识别结果作为初筛参考，再结合田间环境与用药史给出最终方案，提升服务效率与可追溯性。',
       '/images/news/smart-diagnosis.png', 'ONLINE', '2026-10-23 18:20:00', '2026-10-23 18:20:00'
WHERE NOT EXISTS (SELECT 1 FROM `news` WHERE `title` = '智能识别如何辅助基层农技服务');

INSERT INTO `news` (`title`, `content`, `cover`, `status`, `create_time`, `update_time`)
SELECT '农技员认证通道已开放申请',
       '普通农户可提交资质说明与证明材料，审核通过后可发布农技指导内容。\n\n认证农技员将获得专业病害资料查看与农技指导发帖权限，欢迎有经验的种植户与技术员申请。',
       '/images/news/tech-cert.png', 'ONLINE', '2026-10-23 15:10:00', '2026-10-23 15:10:00'
WHERE NOT EXISTS (SELECT 1 FROM `news` WHERE `title` = '农技员认证通道已开放申请');

INSERT INTO `news` (`title`, `content`, `cover`, `status`, `create_time`, `update_time`)
SELECT '雨后田间病害应急处理指南',
       '强降雨后及时排水降湿，清除病叶病果，必要时补喷保护性杀菌剂。\n\n雨后叶面长时间结露利于病原侵入。建议优先改善通风排水，再根据作物与病害类型选择药剂，注意安全间隔期。',
       '/images/hero-farm.jpg', 'ONLINE', '2026-10-22 16:40:00', '2026-10-22 16:40:00'
WHERE NOT EXISTS (SELECT 1 FROM `news` WHERE `title` = '雨后田间病害应急处理指南');

-- ----------------------------
-- 农技员认证「已通过」（与 DataInitializer.ensureExpertCertifications 对齐）
-- ----------------------------
INSERT INTO `certification_application`
  (`user_id`, `qualification_desc`, `material_path`, `status`, `reviewer_id`, `review_comment`, `review_time`, `create_time`, `update_time`)
SELECT u.id,
       CONCAT(IFNULL(u.nickname, u.username), '：演示农技员账号，预置已通过认证'),
       NULL,
       'APPROVED',
       (SELECT id FROM `user` WHERE username = 'admin' LIMIT 1),
       '演示种子：预置农技员已通过',
       DATE_SUB(NOW(), INTERVAL 3 DAY),
       DATE_SUB(NOW(), INTERVAL 5 DAY),
       DATE_SUB(NOW(), INTERVAL 3 DAY)
FROM `user` u
WHERE u.role = 'EXPERT'
  AND NOT EXISTS (
    SELECT 1 FROM `certification_application` c
    WHERE c.user_id = u.id AND c.status = 'APPROVED'
  );

-- ----------------------------
-- 演示用户病害识别记录（image_path = 识别路径）
-- ----------------------------
INSERT INTO `detection_record`
  (`user_id`, `image_path`, `crop_id`, `result`, `confidence`, `description`, `create_time`, `update_time`)
SELECT u.id, '/images/diseases/rice-blast.png', c.id, '稻瘟病', 0.9300,
       '叶片梭形灰绿色病斑，疑似稻瘟病。', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)
FROM `user` u CROSS JOIN `crop` c
WHERE u.username = 'farmer_wang' AND c.name = '水稻'
  AND NOT EXISTS (SELECT 1 FROM `detection_record` d WHERE d.user_id = u.id AND d.result = '稻瘟病');

INSERT INTO `detection_record`
  (`user_id`, `image_path`, `crop_id`, `result`, `confidence`, `description`, `create_time`, `update_time`)
SELECT u.id, '/images/diseases/wheat-fusarium.png', c.id, '小麦赤霉病', 0.8800,
       '扬花期穗部粉红霉层，疑似赤霉病。', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)
FROM `user` u CROSS JOIN `crop` c
WHERE u.username = 'farmer_zhang' AND c.name = '小麦'
  AND NOT EXISTS (SELECT 1 FROM `detection_record` d WHERE d.user_id = u.id AND d.result = '小麦赤霉病');

INSERT INTO `detection_record`
  (`user_id`, `image_path`, `crop_id`, `result`, `confidence`, `description`, `create_time`, `update_time`)
SELECT u.id, '/images/diseases/corn-common-rust.png', c.id, '玉米普通锈病', 0.9100,
       '下部叶片橙黄色粉状物，疑似普通锈病。', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)
FROM `user` u CROSS JOIN `crop` c
WHERE u.username = 'farmer_li' AND c.name = '玉米'
  AND NOT EXISTS (SELECT 1 FROM `detection_record` d WHERE d.user_id = u.id AND d.result = '玉米普通锈病');

INSERT INTO `detection_record`
  (`user_id`, `image_path`, `crop_id`, `result`, `confidence`, `description`, `create_time`, `update_time`)
SELECT u.id, '/images/diseases/tomato-late-blight.png', c.id, '番茄晚疫病', 0.8600,
       '棚室叶片水渍状病斑，疑似晚疫病。', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)
FROM `user` u CROSS JOIN `crop` c
WHERE u.username = 'farmer_chen' AND c.name = '番茄'
  AND NOT EXISTS (SELECT 1 FROM `detection_record` d WHERE d.user_id = u.id AND d.result = '番茄晚疫病');

INSERT INTO `detection_record`
  (`user_id`, `image_path`, `crop_id`, `result`, `confidence`, `description`, `create_time`, `update_time`)
SELECT u.id, '/images/diseases/rice-sheath-blight.png', c.id, '水稻纹枯病', 0.9000,
       '近水面叶鞘云纹状病斑，疑似纹枯病。', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)
FROM `user` u CROSS JOIN `crop` c
WHERE u.username = 'farmer_liu' AND c.name = '水稻'
  AND NOT EXISTS (SELECT 1 FROM `detection_record` d WHERE d.user_id = u.id AND d.result = '水稻纹枯病');

INSERT INTO `detection_record`
  (`user_id`, `image_path`, `crop_id`, `result`, `confidence`, `description`, `create_time`, `update_time`)
SELECT u.id, '/images/diseases/rice-blast.png', c.id, '稻瘟病', 0.9500,
       '田间巡查示范识别：稻瘟病。', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)
FROM `user` u CROSS JOIN `crop` c
WHERE u.username = 'expert_li' AND c.name = '水稻'
  AND NOT EXISTS (SELECT 1 FROM `detection_record` d WHERE d.user_id = u.id AND d.result = '稻瘟病');

INSERT INTO `detection_record`
  (`user_id`, `image_path`, `crop_id`, `result`, `confidence`, `description`, `create_time`, `update_time`)
SELECT u.id, '/images/diseases/tomato-late-blight.png', c.id, '番茄晚疫病', 0.9200,
       '棚室示范识别：番茄晚疫病。', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)
FROM `user` u CROSS JOIN `crop` c
WHERE u.username = 'expert_zhao' AND c.name = '番茄'
  AND NOT EXISTS (SELECT 1 FROM `detection_record` d WHERE d.user_id = u.id AND d.result = '番茄晚疫病');
