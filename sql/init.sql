-- 临时关闭严格模式，解决MySQL 8.4+数据过长报错问题
SET sql_mode = '';

-- 创建数据库
CREATE DATABASE IF NOT EXISTS competition_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE competition_db;

-- 1. 用户表
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(100) NOT NULL COMMENT '用户名/学号/工号',
  `password` varchar(200) NOT NULL COMMENT '密码（加密存储）',
  `name` varchar(50) NOT NULL COMMENT '真实姓名',
  `role` varchar(20) NOT NULL DEFAULT 'student' COMMENT '角色：student/teacher/admin',
  `major` varchar(100) DEFAULT NULL COMMENT '专业',
  `class` varchar(100) DEFAULT NULL COMMENT '班级',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0-否，1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 竞赛表
CREATE TABLE `competition` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '竞赛ID',
  `name` varchar(200) NOT NULL COMMENT '竞赛名称',
  `description` text COMMENT '竞赛描述',
  `organizer` varchar(200) DEFAULT NULL COMMENT '主办方',
  `level` varchar(20) DEFAULT NULL COMMENT '竞赛级别：校级/省级/国家级',
  `registration_start_time` datetime NOT NULL COMMENT '报名开始时间',
  `registration_end_time` datetime NOT NULL COMMENT '报名结束时间',
  `submission_end_time` datetime NOT NULL COMMENT '作品提交截止时间',
  `status` varchar(20) DEFAULT 'upcoming' COMMENT '状态：upcoming/ongoing/ended',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0-否，1-是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛表';

-- 3. 报名表
CREATE TABLE `registration` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '报名ID',
  `user_id` bigint NOT NULL COMMENT '学生ID',
  `competition_id` bigint NOT NULL COMMENT '竞赛ID',
  `teacher_id` bigint DEFAULT NULL COMMENT '指导教师ID',
  `team_name` varchar(100) DEFAULT NULL COMMENT '队伍名称',
  `team_members` text COMMENT '团队成员信息',
  `application_form` varchar(500) DEFAULT NULL COMMENT '报名表文件路径',
  `status` varchar(20) DEFAULT 'pending' COMMENT '状态：pending/approved/rejected',
  `comment` varchar(500) DEFAULT NULL COMMENT '审核意见',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0-否，1-是',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_competition_id` (`competition_id`),
  KEY `idx_teacher_id` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报名表';

-- 4. 作品表
CREATE TABLE `work` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '作品ID',
  `registration_id` bigint NOT NULL COMMENT '报名ID',
  `title` varchar(200) NOT NULL COMMENT '作品标题',
  `description` text COMMENT '作品描述',
  `file_path` varchar(500) NOT NULL COMMENT '作品文件路径',
  `submit_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0-否，1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_registration_id` (`registration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作品表';

-- 5. 成绩表
CREATE TABLE `score` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '成绩ID',
  `work_id` bigint NOT NULL COMMENT '作品ID',
  `teacher_id` bigint NOT NULL COMMENT '评审教师ID',
  `score` int NOT NULL COMMENT '评分（0-100）',
  `comment` text COMMENT '评语',
  `ai_score` int DEFAULT NULL COMMENT 'AI初评分',
  `ai_comment` text COMMENT 'AI初评评语',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0-否，1-是',
  PRIMARY KEY (`id`),
  KEY `idx_work_id` (`work_id`),
  KEY `idx_teacher_id` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩表';

-- 6. 通知表
CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `title` varchar(200) NOT NULL COMMENT '通知标题',
  `content` text NOT NULL COMMENT '通知内容',
  `user_id` bigint DEFAULT NULL COMMENT '接收用户ID（NULL表示全体用户）',
  `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读：0-否，1-是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除：0-否，1-是',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 插入默认管理员账号（密码：admin123）
INSERT INTO `user` (`username`, `password`, `name`, `role`) 
VALUES ('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', 'admin');