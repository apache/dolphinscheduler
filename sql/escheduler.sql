/*
Navicat MySQL Data Transfer

Source Server         : xx.xx
Source Server Version : 50725
Source Host           : 192.168.xx.xx:3306
Source Database       : escheduler

Target Server Type    : MYSQL
Target Server Version : 50725
File Encoding         : 65001

Date: 2019-03-23 11:47:30
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_escheduler_alert
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_alert`;
CREATE TABLE `t_escheduler_alert` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(64) DEFAULT NULL COMMENT '消息标题',
  `show_type` tinyint(4) DEFAULT NULL COMMENT '发送格式，0是TABLE,1是TEXT',
  `content` text COMMENT '消息内容（可以是邮件，可以是短信。邮件是JSON Map存放，短信是字符串）',
  `alert_type` tinyint(4) DEFAULT NULL COMMENT '0是邮件，1是短信',
  `alert_status` tinyint(4) DEFAULT '0' COMMENT '0是待执行，1是执行成功，2执行失败',
  `log` text COMMENT '执行日志',
  `alertgroup_id` int(11) DEFAULT NULL COMMENT '发送组',
  `receivers` text COMMENT '收件人',
  `receivers_cc` text COMMENT '抄送人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_escheduler_alertgroup
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_alertgroup`;
CREATE TABLE `t_escheduler_alertgroup` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `group_name` varchar(255) DEFAULT NULL COMMENT '组名称',
  `group_type` tinyint(4) DEFAULT NULL COMMENT '组类型(邮件0，短信1...)',
  `desc` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_escheduler_command
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_command`;
CREATE TABLE `t_escheduler_command` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `command_type` tinyint(4) DEFAULT NULL COMMENT '命令类型：0 启动工作流,1 从当前节点开始执行,2 恢复被容错的工作流,3 恢复暂停流程,4 从失败节点开始执行,5 补数,6 调度,7 重跑,8 暂停,9 停止,10 恢复等待线程',
  `process_definition_id` int(11) DEFAULT NULL COMMENT '流程定义id',
  `command_param` text COMMENT '命令的参数（json格式）',
  `task_depend_type` tinyint(4) DEFAULT NULL COMMENT '节点依赖类型：0 当前节点,1 向前执行,2 向后执行',
  `failure_strategy` tinyint(4) DEFAULT '0' COMMENT '失败策略：0结束，1继续',
  `warning_type` tinyint(4) DEFAULT '0' COMMENT '告警类型：0 不发,1 流程成功发,2 流程失败发,3 成功失败都发',
  `warning_group_id` int(11) DEFAULT NULL COMMENT '告警组',
  `schedule_time` datetime DEFAULT NULL COMMENT '预期运行时间',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `executor_id` int(11) DEFAULT NULL COMMENT '执行用户id',
  `dependence` varchar(255) DEFAULT NULL COMMENT '依赖字段',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `process_instance_priority` int(11) DEFAULT NULL COMMENT '流程实例优先级：0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_escheduler_datasource
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_datasource`;
CREATE TABLE `t_escheduler_datasource` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) NOT NULL COMMENT '数据源名称',
  `note` varchar(256) DEFAULT NULL COMMENT '描述',
  `type` tinyint(4) NOT NULL COMMENT '数据源类型：0 mysql,1 postgresql,2 hive,3 spark',
  `user_id` int(11) NOT NULL COMMENT '创建用户id',
  `connection_params` text NOT NULL COMMENT '连接参数(json格式)',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_escheduler_master_server
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_master_server`;
CREATE TABLE `t_escheduler_master_server` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `host` varchar(45) DEFAULT NULL COMMENT 'ip',
  `port` int(11) DEFAULT NULL COMMENT '进程号',
  `zk_directory` varchar(64) DEFAULT NULL COMMENT 'zk注册目录',
  `res_info` varchar(256) DEFAULT NULL COMMENT '集群资源信息：json格式{"cpu":xxx,"memroy":xxx}',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `last_heartbeat_time` datetime DEFAULT NULL COMMENT '最后心跳时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_escheduler_process_definition
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_process_definition`;
CREATE TABLE `t_escheduler_process_definition` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) DEFAULT NULL COMMENT '流程定义名称',
  `version` int(11) DEFAULT NULL COMMENT '流程定义版本',
  `release_state` tinyint(4) DEFAULT NULL COMMENT '流程定义的发布状态：0 未上线  1已上线',
  `project_id` int(11) DEFAULT NULL COMMENT '项目id',
  `user_id` int(11) DEFAULT NULL COMMENT '流程定义所属用户id',
  `process_definition_json` longtext COMMENT '流程定义json串',
  `desc` text COMMENT '流程定义描述',
  `global_params` text COMMENT '全局参数',
  `flag` tinyint(4) DEFAULT NULL COMMENT '流程是否可用\r\n：0 不可用\r\n，1 可用',
  `locations` text COMMENT '节点坐标信息',
  `connects` text COMMENT '节点连线信息',
  `receivers` text COMMENT '收件人',
  `receivers_cc` text COMMENT '抄送人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `process_definition_index` (`project_id`,`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_escheduler_process_instance
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_process_instance`;
CREATE TABLE `t_escheduler_process_instance` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) DEFAULT NULL COMMENT '流程实例名称',
  `process_definition_id` int(11) DEFAULT NULL COMMENT '流程定义id',
  `state` tinyint(4) DEFAULT NULL COMMENT '流程实例状态：0 提交成功,1 正在运行,2 准备暂停,3 暂停,4 准备停止,5 停止,6 失败,7 成功,8 需要容错,9 kill,10 等待线程,11 等待依赖完成',
  `recovery` tinyint(4) DEFAULT NULL COMMENT '流程实例容错标识：0 正常,1 需要被容错重启',
  `start_time` datetime DEFAULT NULL COMMENT '流程实例开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '流程实例结束时间',
  `run_times` int(11) DEFAULT NULL COMMENT '流程实例运行次数',
  `host` varchar(45) DEFAULT NULL COMMENT '流程实例所在的机器',
  `command_type` tinyint(4) DEFAULT NULL COMMENT '命令类型：0 启动工作流,1 从当前节点开始执行,2 恢复被容错的工作流,3 恢复暂停流程,4 从失败节点开始执行,5 补数,6 调度,7 重跑,8 暂停,9 停止,10 恢复等待线程',
  `command_param` text COMMENT '命令的参数（json格式）',
  `task_depend_type` tinyint(4) DEFAULT NULL COMMENT '节点依赖类型：0 当前节点,1 向前执行,2 向后执行',
  `max_try_times` tinyint(4) DEFAULT '0' COMMENT '最大重试次数',
  `failure_strategy` tinyint(4) DEFAULT '0' COMMENT '失败策略 0 失败后结束，1 失败后继续',
  `warning_type` tinyint(4) DEFAULT '0' COMMENT '告警类型：0 不发,1 流程成功发,2 流程失败发,3 成功失败都发',
  `warning_group_id` int(11) DEFAULT NULL COMMENT '告警组id',
  `schedule_time` datetime DEFAULT NULL COMMENT '预期运行时间',
  `command_start_time` datetime DEFAULT NULL COMMENT '开始命令时间',
  `global_params` text COMMENT '全局参数（固化流程定义的参数）',
  `process_instance_json` longtext COMMENT '流程实例json(copy的流程定义的json)',
  `flag` tinyint(4) DEFAULT '1' COMMENT '是否可用，1 可用，0不可用',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_sub_process` int(11) DEFAULT '0' COMMENT '是否是子工作流 1 是，0 不是',
  `executor_id` int(11) NOT NULL COMMENT '命令执行者',
  `locations` text COMMENT '节点坐标信息',
  `connects` text COMMENT '节点连线信息',
  `history_cmd` text COMMENT '历史命令，记录所有对流程实例的操作',
  `dependence_schedule_times` text COMMENT '依赖节点的预估时间',
  `process_instance_priority` int(11) DEFAULT NULL COMMENT '流程实例优先级：0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  PRIMARY KEY (`id`),
  KEY `process_instance_index` (`process_definition_id`,`id`) USING BTREE,
  KEY `start_time_index` (`start_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_escheduler_project
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_project`;
CREATE TABLE `t_escheduler_project` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) DEFAULT NULL COMMENT '项目名称',
  `desc` varchar(200) DEFAULT NULL COMMENT '项目描述',
  `user_id` int(11) DEFAULT NULL COMMENT '所属用户',
  `flag` tinyint(4) DEFAULT '1' COMMENT '是否可用  1 可用,0 不可用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `user_id_index` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_escheduler_queue
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_queue`;
CREATE TABLE `t_escheduler_queue` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `queue_name` varchar(64) DEFAULT NULL COMMENT '队列名称',
  `queue` varchar(64) DEFAULT NULL COMMENT 'yarn队列名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_escheduler_relation_datasource_user
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_relation_datasource_user`;
CREATE TABLE `t_escheduler_relation_datasource_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int(11) NOT NULL COMMENT '用户id',
  `datasource_id` int(11) DEFAULT NULL COMMENT '数据源id',
  `perm` int(11) DEFAULT '1' COMMENT '权限',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_escheduler_relation_process_instance
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_relation_process_instance`;
CREATE TABLE `t_escheduler_relation_process_instance` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_process_instance_id` int(11) DEFAULT NULL COMMENT '父流程实例id',
  `parent_task_instance_id` int(11) DEFAULT NULL COMMENT '父任务实例id',
  `process_instance_id` int(11) DEFAULT NULL COMMENT '子流程实例id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_escheduler_relation_project_user
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_relation_project_user`;
CREATE TABLE `t_escheduler_relation_project_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int(11) NOT NULL COMMENT '用户id',
  `project_id` int(11) DEFAULT NULL COMMENT '项目id',
  `perm` int(11) DEFAULT '1' COMMENT '权限',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `user_id_index` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_escheduler_relation_resources_user
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_relation_resources_user`;
CREATE TABLE `t_escheduler_relation_resources_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT '用户id',
  `resources_id` int(11) DEFAULT NULL COMMENT '资源id',
  `perm` int(11) DEFAULT '1' COMMENT '权限',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_escheduler_relation_udfs_user
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_relation_udfs_user`;
CREATE TABLE `t_escheduler_relation_udfs_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int(11) NOT NULL COMMENT '用户id',
  `udf_id` int(11) DEFAULT NULL COMMENT 'udf id',
  `perm` int(11) DEFAULT '1' COMMENT '权限',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_escheduler_relation_user_alertgroup
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_relation_user_alertgroup`;
CREATE TABLE `t_escheduler_relation_user_alertgroup` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `alertgroup_id` int(11) DEFAULT NULL COMMENT '组消息id',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_escheduler_resources
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_resources`;
CREATE TABLE `t_escheduler_resources` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `alias` varchar(64) DEFAULT NULL COMMENT '别名',
  `file_name` varchar(64) DEFAULT NULL COMMENT '文件名',
  `desc` varchar(256) DEFAULT NULL COMMENT '描述',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `type` tinyint(4) DEFAULT NULL COMMENT '资源类型，0 FILE，1 UDF',
  `size` bigint(20) DEFAULT NULL COMMENT '资源大小',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_escheduler_schedules
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_schedules`;
CREATE TABLE `t_escheduler_schedules` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `process_definition_id` int(11) NOT NULL COMMENT '流程定义id',
  `start_time` datetime NOT NULL COMMENT '调度开始时间',
  `end_time` datetime NOT NULL COMMENT '调度结束时间',
  `crontab` varchar(256) NOT NULL COMMENT 'crontab 表达式',
  `failure_strategy` tinyint(4) NOT NULL COMMENT '失败策略： 0 结束，1 继续',
  `user_id` int(11) NOT NULL COMMENT '用户id',
  `release_state` tinyint(4) NOT NULL COMMENT '状态：0 未上线，1 上线',
  `warning_type` tinyint(4) NOT NULL COMMENT '告警类型：0 不发,1 流程成功发,2 流程失败发,3 成功失败都发',
  `warning_group_id` int(11) DEFAULT NULL COMMENT '告警组id',
  `process_instance_priority` int(11) DEFAULT NULL COMMENT '流程实例优先级：0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_escheduler_session
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_session`;
CREATE TABLE `t_escheduler_session` (
  `id` varchar(64) NOT NULL COMMENT '主键',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `ip` varchar(45) DEFAULT NULL COMMENT '登录ip',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_escheduler_task_instance
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_task_instance`;
CREATE TABLE `t_escheduler_task_instance` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) DEFAULT NULL COMMENT '任务名称',
  `task_type` varchar(64) DEFAULT NULL COMMENT '任务类型',
  `process_definition_id` int(11) DEFAULT NULL COMMENT '流程定义id',
  `process_instance_id` int(11) DEFAULT NULL COMMENT '流程实例id',
  `task_json` longtext COMMENT '任务节点json',
  `state` tinyint(4) DEFAULT NULL COMMENT '任务实例状态：0 提交成功,1 正在运行,2 准备暂停,3 暂停,4 准备停止,5 停止,6 失败,7 成功,8 需要容错,9 kill,10 等待线程,11 等待依赖完成',
  `submit_time` datetime DEFAULT NULL COMMENT '任务提交时间',
  `start_time` datetime DEFAULT NULL COMMENT '任务开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '任务结束时间',
  `host` varchar(45) DEFAULT NULL COMMENT '执行任务的机器',
  `execute_path` varchar(200) DEFAULT NULL COMMENT '任务执行路径',
  `log_path` varchar(200) DEFAULT NULL COMMENT '任务日志路径',
  `alert_flag` tinyint(4) DEFAULT NULL COMMENT '是否告警',
  `retry_times` int(4) DEFAULT '0' COMMENT '重试次数',
  `pid` int(4) DEFAULT NULL COMMENT '进程pid',
  `app_link` varchar(255) DEFAULT NULL COMMENT 'yarn app id',
  `flag` tinyint(4) DEFAULT '1' COMMENT '是否可用：0 不可用，1 可用',
  `retry_interval` int(4) DEFAULT NULL COMMENT '重试间隔',
  `max_retry_times` int(2) DEFAULT NULL COMMENT '最大重试次数',
  `task_instance_priority` int(11) DEFAULT NULL COMMENT '任务实例优先级：0 Highest,1 High,2 Medium,3 Low,4 Lowest',
  PRIMARY KEY (`id`),
  KEY `process_instance_id` (`process_instance_id`) USING BTREE,
  KEY `task_instance_index` (`process_definition_id`,`process_instance_id`) USING BTREE,
  CONSTRAINT `foreign_key_instance_id` FOREIGN KEY (`process_instance_id`) REFERENCES `t_escheduler_process_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_escheduler_tenant
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_tenant`;
CREATE TABLE `t_escheduler_tenant` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_code` varchar(64) DEFAULT NULL COMMENT '租户编码',
  `tenant_name` varchar(64) DEFAULT NULL COMMENT '租户名称',
  `desc` varchar(256) DEFAULT NULL COMMENT '描述',
  `queue_id` int(11) DEFAULT NULL COMMENT '队列id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_escheduler_udfs
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_udfs`;
CREATE TABLE `t_escheduler_udfs` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int(11) NOT NULL COMMENT '用户id',
  `func_name` varchar(100) NOT NULL COMMENT 'UDF函数名',
  `class_name` varchar(255) NOT NULL COMMENT '类名',
  `type` tinyint(4) NOT NULL COMMENT 'Udf函数类型',
  `arg_types` varchar(255) DEFAULT NULL COMMENT '参数',
  `database` varchar(255) DEFAULT NULL COMMENT '库名',
  `desc` varchar(255) DEFAULT NULL COMMENT '描述',
  `resource_id` int(11) NOT NULL COMMENT '资源id',
  `resource_name` varchar(255) NOT NULL COMMENT '资源名称',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_escheduler_user
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_user`;
CREATE TABLE `t_escheduler_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `user_name` varchar(64) DEFAULT NULL COMMENT '用户名',
  `user_password` varchar(64) DEFAULT NULL COMMENT '用户密码',
  `user_type` tinyint(4) DEFAULT NULL COMMENT '用户类型：0 管理员，1 普通用户',
  `email` varchar(64) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(11) DEFAULT NULL COMMENT '手机',
  `tenant_id` int(11) DEFAULT NULL COMMENT '管理员0,普通用户所属租户id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_name_unique` (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_escheduler_worker_server
-- ----------------------------
DROP TABLE IF EXISTS `t_escheduler_worker_server`;
CREATE TABLE `t_escheduler_worker_server` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `host` varchar(45) DEFAULT NULL COMMENT 'ip',
  `port` int(11) DEFAULT NULL COMMENT '进程号',
  `zk_directory` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT 'zk注册目录',
  `res_info` varchar(255) DEFAULT NULL COMMENT '集群资源信息：json格式{"cpu":xxx,"memroy":xxx}',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `last_heartbeat_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Records of t_escheduler_user,user : admin , password : escheduler123
INSERT INTO `t_escheduler_user` VALUES ('1', 'admin', '055a97b5fcd6d120372ad1976518f371', '0', 'xxx@qq.com', 'xxxx', '0', '2018-03-27 15:48:50', '2018-10-24 17:40:22');
INSERT INTO `t_escheduler_alertgroup` VALUES (1, 'escheduler管理员告警组', '0', 'escheduler管理员告警组','2018-11-29 10:20:39', '2018-11-29 10:20:39');
INSERT INTO `t_escheduler_relation_user_alertgroup` VALUES ('1', '1', '1', '2018-11-29 10:22:33', '2018-11-29 10:22:33');

-- Records of t_escheduler_queue,default queue name : default
INSERT INTO `t_escheduler_queue` VALUES ('1', 'default', 'default');


