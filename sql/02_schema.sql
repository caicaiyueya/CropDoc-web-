-- 云农智诊：业务表结构（阶段2）
-- 依赖：先执行 01_create_database.sql
USE yunnongzhizhen;

SET NAMES utf8mb4;

-- ----------------------------
-- 用户
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`      VARCHAR(64)  NOT NULL COMMENT '账号（用户名/手机号/邮箱登录标识）',
    `password_hash` VARCHAR(100) NOT NULL COMMENT '密码BCrypt哈希',
    `nickname`      VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
    `avatar`        VARCHAR(255) DEFAULT NULL COMMENT '头像路径',
    `region`        VARCHAR(128) DEFAULT NULL COMMENT '地区',
    `contact`       VARCHAR(64)  DEFAULT NULL COMMENT '联系方式',
    `role`          VARCHAR(16)  NOT NULL DEFAULT 'FARMER' COMMENT '角色枚举：FARMER/EXPERT/ADMIN',
    `status`        VARCHAR(16)  NOT NULL DEFAULT 'NORMAL' COMMENT '状态枚举：NORMAL/DISABLED',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`),
    KEY `idx_user_role` (`role`),
    KEY `idx_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户';

-- ----------------------------
-- 农技员认证申请
-- ----------------------------
DROP TABLE IF EXISTS `certification_application`;
CREATE TABLE `certification_application` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`             BIGINT       NOT NULL COMMENT '申请人用户ID',
    `qualification_desc`  VARCHAR(1000) NOT NULL COMMENT '资质说明',
    `material_path`       VARCHAR(512) DEFAULT NULL COMMENT '材料文件路径',
    `status`              VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/APPROVED/REJECTED',
    `reviewer_id`         BIGINT       DEFAULT NULL COMMENT '审核人ID',
    `review_comment`      VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    `review_time`         DATETIME     DEFAULT NULL COMMENT '审核时间',
    `create_time`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_cert_user` (`user_id`),
    KEY `idx_cert_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='农技员认证申请';

-- ----------------------------
-- 作物
-- ----------------------------
DROP TABLE IF EXISTS `crop`;
CREATE TABLE `crop` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(64)  NOT NULL COMMENT '作物名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_crop_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作物';

-- ----------------------------
-- 病害资料
-- ----------------------------
DROP TABLE IF EXISTS `disease`;
CREATE TABLE `disease` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `crop_id`           BIGINT       NOT NULL COMMENT '作物ID',
    `name`              VARCHAR(128) NOT NULL COMMENT '病害名称',
    `description`       TEXT         COMMENT '描述',
    `prevention_advice` TEXT         COMMENT '防治建议',
    `level`             VARCHAR(16)  NOT NULL DEFAULT 'BASIC' COMMENT '资料层级：BASIC/PRO',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_disease_crop` (`crop_id`),
    KEY `idx_disease_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='病害资料';

-- ----------------------------
-- 识别记录
-- ----------------------------
DROP TABLE IF EXISTS `detection_record`;
CREATE TABLE `detection_record` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT         NOT NULL COMMENT '用户ID',
    `image_path`  VARCHAR(512)   NOT NULL COMMENT '图片相对路径',
    `crop_id`     BIGINT         DEFAULT NULL COMMENT '作物ID',
    `result`      VARCHAR(256)   DEFAULT NULL COMMENT '识别结果（病害名等）',
    `confidence`  DECIMAL(6, 4)  DEFAULT NULL COMMENT '置信度0-1',
    `description` TEXT           COMMENT '识别描述',
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_detect_user` (`user_id`),
    KEY `idx_detect_crop` (`crop_id`),
    KEY `idx_detect_ctime` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='病虫害识别记录';

-- ----------------------------
-- 智能诊断会话
-- ----------------------------
DROP TABLE IF EXISTS `diagnosis_session`;
CREATE TABLE `diagnosis_session` (
    `id`                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `detection_record_id`  BIGINT       NOT NULL COMMENT '关联检测记录ID',
    `user_id`              BIGINT       NOT NULL COMMENT '用户ID',
    `session_id`           VARCHAR(64)  NOT NULL COMMENT '会话ID（多轮同一会话）',
    `session_content`      MEDIUMTEXT   COMMENT '会话内容JSON',
    `create_time`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_diag_user` (`user_id`),
    KEY `idx_diag_detect` (`detection_record_id`),
    KEY `idx_diag_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能诊断会话';

-- ----------------------------
-- 社区帖子
-- ----------------------------
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `author_id`   BIGINT       NOT NULL COMMENT '作者ID',
    `title`       VARCHAR(200) NOT NULL COMMENT '标题',
    `content`     TEXT         NOT NULL COMMENT '内容',
    `images`      VARCHAR(2000) DEFAULT NULL COMMENT '图片路径，逗号分隔',
    `tag`         VARCHAR(16)  NOT NULL DEFAULT 'NORMAL' COMMENT '标签：NORMAL/GUIDE',
    `status`      VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/APPROVED/REJECTED/OFFLINE',
    `pinned`      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否置顶',
    `view_count`  INT          NOT NULL DEFAULT 0 COMMENT '浏览量（热度）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_post_author` (`author_id`),
    KEY `idx_post_status` (`status`),
    KEY `idx_post_tag` (`tag`),
    KEY `idx_post_ctime` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区帖子';

-- ----------------------------
-- 评论
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `post_id`         BIGINT       NOT NULL COMMENT '帖子ID',
    `author_id`       BIGINT       NOT NULL COMMENT '作者ID',
    `content`         VARCHAR(1000) NOT NULL COMMENT '内容',
    `is_professional` TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否专业回答',
    `is_quality`      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否优质',
    `parent_id`       BIGINT       DEFAULT NULL COMMENT '父评论ID',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_comment_post` (`post_id`),
    KEY `idx_comment_author` (`author_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子评论';

-- ----------------------------
-- 收藏
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
    `post_id`     BIGINT   NOT NULL COMMENT '帖子ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_fav_user_post` (`user_id`, `post_id`),
    KEY `idx_fav_post` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏';

-- ----------------------------
-- 举报
-- ----------------------------
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `reporter_id`   BIGINT       NOT NULL COMMENT '举报人ID',
    `target_type`   VARCHAR(32)  NOT NULL COMMENT '目标类型：POST/COMMENT/USER',
    `target_id`     BIGINT       NOT NULL COMMENT '目标ID',
    `reason`        VARCHAR(500) NOT NULL COMMENT '举报原因',
    `status`        VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/HANDLED',
    `handler_id`    BIGINT       DEFAULT NULL COMMENT '处理人ID',
    `handle_result` VARCHAR(500) DEFAULT NULL COMMENT '处理结果',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_report_status` (`status`),
    KEY `idx_report_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报';

-- ----------------------------
-- 反馈
-- ----------------------------
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
    `content`     VARCHAR(1000) NOT NULL COMMENT '反馈内容',
    `reply`       VARCHAR(1000) DEFAULT NULL COMMENT '管理员回复',
    `status`      VARCHAR(16)  NOT NULL DEFAULT 'OPEN' COMMENT '状态：OPEN/REPLIED/CLOSED',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_feedback_user` (`user_id`),
    KEY `idx_feedback_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈';

-- ----------------------------
-- 资讯
-- ----------------------------
DROP TABLE IF EXISTS `news`;
CREATE TABLE `news` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`       VARCHAR(200) NOT NULL COMMENT '标题',
    `content`     MEDIUMTEXT   NOT NULL COMMENT '内容',
    `cover`       VARCHAR(512) DEFAULT NULL COMMENT '封面图',
    `status`      VARCHAR(16)  NOT NULL DEFAULT 'ONLINE' COMMENT '状态：ONLINE/OFFLINE',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_news_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资讯';

-- ----------------------------
-- Banner
-- ----------------------------
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`       VARCHAR(128) NOT NULL COMMENT '标题',
    `image_path`  VARCHAR(512) NOT NULL COMMENT '图片路径',
    `link_url`    VARCHAR(512) DEFAULT NULL COMMENT '跳转链接',
    `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '排序，越大越靠前',
    `status`      VARCHAR(16)  NOT NULL DEFAULT 'ONLINE' COMMENT '状态：ONLINE/OFFLINE',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_banner_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Banner';

-- ----------------------------
-- 公告
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`       VARCHAR(200) NOT NULL COMMENT '标题',
    `content`     TEXT         NOT NULL COMMENT '内容',
    `status`      VARCHAR(16)  NOT NULL DEFAULT 'ONLINE' COMMENT '状态：ONLINE/OFFLINE',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_notice_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告';

-- ----------------------------
-- 管理员操作日志（角色调整、禁用等）
-- ----------------------------
DROP TABLE IF EXISTS `admin_operation_log`;
CREATE TABLE `admin_operation_log` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `operator_id`   BIGINT       NOT NULL COMMENT '操作人ID',
    `target_user_id` BIGINT      DEFAULT NULL COMMENT '目标用户ID',
    `action`        VARCHAR(64)  NOT NULL COMMENT '操作类型',
    `detail`        VARCHAR(1000) DEFAULT NULL COMMENT '详情',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_oplog_operator` (`operator_id`),
    KEY `idx_oplog_action` (`action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作日志';
