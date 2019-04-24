-- 用户指定队列
alter table t_escheduler_user add queue varchar(64);

-- 访问token
CREATE TABLE `t_escheduler_access_token` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `token` varchar(64) DEFAULT NULL COMMENT 'token令牌',
  `expire_time` datetime DEFAULT NULL COMMENT 'token有效结束时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

CREATE TABLE `t_escheduler_error_command`  (
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
  `message` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '执行信息',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


CREATE TABLE `t_escheduler_worker_group`  (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(256) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL COMMENT '组名称',
  `ip_list` varchar(256) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL COMMENT 'worker地址列表',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

ALTER TABLE `t_escheduler_task_instance`
ADD COLUMN `worker_group_id` int(11) NULL DEFAULT -1 COMMENT '任务指定运行的worker分组' AFTER `task_instance_priority`;

ALTER TABLE `t_escheduler_command`
ADD COLUMN `worker_group_id` int(11) NULL DEFAULT -1 COMMENT '任务指定运行的worker分组'   NULL AFTER `process_instance_priority`;

ALTER TABLE `t_escheduler_error_command`
ADD COLUMN `worker_group_id` int(11) NULL DEFAULT -1 COMMENT '任务指定运行的worker分组'   NULL AFTER `process_instance_priority`;

ALTER TABLE `t_escheduler_schedules`
ADD COLUMN `worker_group_id` int(11) NULL DEFAULT -1 COMMENT '任务指定运行的worker分组'   NULL AFTER `process_instance_priority`;