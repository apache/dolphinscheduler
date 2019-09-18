SET sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''));
-- ac_escheduler_T_t_escheduler_version
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_version;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_version()
   BEGIN
       drop table if exists t_escheduler_version;
       CREATE TABLE  IF NOT EXISTS  `t_escheduler_version` (
         `id` int(11) NOT NULL AUTO_INCREMENT,
         `version` varchar(200) NOT NULL,
         PRIMARY KEY (`id`),
         UNIQUE KEY `version_UNIQUE` (`version`)
       ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='版本表';

 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_version;
DROP PROCEDURE ac_escheduler_T_t_escheduler_version;

-- ac_escheduler_T_t_escheduler_user_C_queue
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_user_C_queue;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_user_C_queue()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_user'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='queue')
   THEN
         ALTER TABLE t_escheduler_user ADD COLUMN queue varchar(64) COMMENT '队列' AFTER update_time;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_user_C_queue;
DROP PROCEDURE ac_escheduler_T_t_escheduler_user_C_queue;

-- ac_escheduler_T_t_escheduler_access_token
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_access_token;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_access_token()
   BEGIN
       drop table if exists t_escheduler_access_token;
       CREATE TABLE  IF NOT EXISTS  `t_escheduler_access_token` (
         `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
         `user_id` int(11) DEFAULT NULL COMMENT '用户id',
         `token` varchar(64) DEFAULT NULL COMMENT 'token令牌',
         `expire_time` datetime DEFAULT NULL COMMENT 'token有效结束时间',
         `create_time` datetime DEFAULT NULL COMMENT '创建时间',
         `update_time` datetime DEFAULT NULL COMMENT '更新时间',
         PRIMARY KEY (`id`)
       ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_access_token;
DROP PROCEDURE ac_escheduler_T_t_escheduler_access_token;

-- ac_escheduler_T_t_escheduler_error_command
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_error_command;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_error_command()
   BEGIN
       drop table if exists t_escheduler_error_command;
       CREATE TABLE  IF NOT EXISTS  `t_escheduler_error_command` (
           `id` int(11) NOT NULL COMMENT '主键',
           `command_type` tinyint(4) NULL DEFAULT NULL COMMENT '命令类型：0 启动工作流,1 从当前节点开始执行,2 恢复被容错的工作流,3 恢复暂停流程,4 从失败节点开始执行,5 补数,6 调度,7 重跑,8 暂停,9 停止,10 恢复等待线程',
           `executor_id` int(11) NULL DEFAULT NULL COMMENT '命令执行者',
           `process_definition_id` int(11) NULL DEFAULT NULL COMMENT '流程定义id',
           `command_param` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '命令的参数（json格式）',
           `task_depend_type` tinyint(4) NULL DEFAULT NULL COMMENT '节点依赖类型',
           `failure_strategy` tinyint(4) NULL DEFAULT 0 COMMENT '失败策略：0结束，1继续',
           `warning_type` tinyint(4) NULL DEFAULT 0 COMMENT '告警类型',
           `warning_group_id` int(11) NULL DEFAULT NULL COMMENT '告警组',
           `schedule_time` datetime(0) NULL DEFAULT NULL COMMENT '预期运行时间',
           `start_time` datetime(0) NULL DEFAULT NULL COMMENT '开始时间',
           `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
           `dependence` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '依赖字段',
           `process_instance_priority` int(11) NULL DEFAULT NULL COMMENT '流程实例优先级：0 Highest,1 High,2 Medium,3 Low,4 Lowest',
           `worker_group_id` int(11) NULL DEFAULT -1 COMMENT '任务指定运行的worker分组',
           `message` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '执行信息',
           PRIMARY KEY (`id`) USING BTREE
       ) ENGINE = InnoDB AUTO_INCREMENT=1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_error_command;
DROP PROCEDURE ac_escheduler_T_t_escheduler_error_command;

-- ac_escheduler_T_t_escheduler_worker_group
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_worker_group;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_worker_group()
   BEGIN
       drop table if exists t_escheduler_worker_group;
       CREATE TABLE  IF NOT EXISTS  `t_escheduler_worker_group` (
           `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
           `name` varchar(256)  NULL DEFAULT NULL COMMENT '组名称',
           `ip_list` varchar(256)  NULL DEFAULT NULL COMMENT 'worker地址列表',
           `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
           `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
           PRIMARY KEY (`id`) USING BTREE
       ) ENGINE = InnoDB AUTO_INCREMENT=1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_worker_group;
DROP PROCEDURE ac_escheduler_T_t_escheduler_worker_group;

-- ac_escheduler_T_t_escheduler_task_instance_C_worker_group_id
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_task_instance_C_worker_group_id;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_task_instance_C_worker_group_id()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_task_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='worker_group_id')
   THEN
         ALTER TABLE t_escheduler_task_instance ADD COLUMN `worker_group_id` int(11) NULL DEFAULT -1 COMMENT '任务指定运行的worker分组' AFTER `task_instance_priority`;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_task_instance_C_worker_group_id;
DROP PROCEDURE ac_escheduler_T_t_escheduler_task_instance_C_worker_group_id;


-- ac_escheduler_T_t_escheduler_command_C_worker_group_id
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_command_C_worker_group_id;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_command_C_worker_group_id()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_command'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='worker_group_id')
   THEN
         ALTER TABLE t_escheduler_command ADD COLUMN `worker_group_id` int(11) NULL DEFAULT -1 COMMENT '任务指定运行的worker分组' AFTER `process_instance_priority`;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_command_C_worker_group_id;
DROP PROCEDURE ac_escheduler_T_t_escheduler_command_C_worker_group_id;

-- ac_escheduler_T_t_escheduler_schedules_C_worker_group_id
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_schedules_C_worker_group_id;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_schedules_C_worker_group_id()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_schedules'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='worker_group_id')
   THEN
         ALTER TABLE t_escheduler_schedules ADD COLUMN `worker_group_id` int(11) NULL DEFAULT -1 COMMENT '任务指定运行的worker分组' AFTER `process_instance_priority`;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_schedules_C_worker_group_id;
DROP PROCEDURE ac_escheduler_T_t_escheduler_schedules_C_worker_group_id;

-- ac_escheduler_T_t_escheduler_process_instance_C_worker_group_id
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_process_instance_C_worker_group_id;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_process_instance_C_worker_group_id()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_process_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='worker_group_id')
   THEN
         ALTER TABLE t_escheduler_process_instance ADD COLUMN `worker_group_id` int(11) NULL DEFAULT -1 COMMENT '任务指定运行的worker分组' AFTER `process_instance_priority`;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_process_instance_C_worker_group_id;
DROP PROCEDURE ac_escheduler_T_t_escheduler_process_instance_C_worker_group_id;


-- ac_escheduler_T_t_escheduler_process_instance_C_timeout
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_process_instance_C_timeout;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_process_instance_C_timeout()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_process_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='timeout')
   THEN
         ALTER TABLE `t_escheduler_process_instance` ADD COLUMN `timeout` int(11) NULL DEFAULT 0  COMMENT '超时时间' AFTER `worker_group_id`;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_process_instance_C_timeout;
DROP PROCEDURE ac_escheduler_T_t_escheduler_process_instance_C_timeout;


-- ac_escheduler_T_t_escheduler_process_definition_C_timeout
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_process_definition_C_timeout;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_process_definition_C_timeout()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_process_definition'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='timeout')
   THEN
         ALTER TABLE `t_escheduler_process_definition` ADD COLUMN `timeout` int(11) NULL DEFAULT 0 COMMENT '超时时间' AFTER `create_time`;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_process_definition_C_timeout;
DROP PROCEDURE ac_escheduler_T_t_escheduler_process_definition_C_timeout;