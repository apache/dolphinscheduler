alter table t_escheduler_user add queue varchar(64);

CREATE TABLE `escheduler`.`无标题`  (
  `id` int(11) NOT NULL COMMENT '主键',
  `command_type` tinyint(4) NULL DEFAULT NULL COMMENT '命令类型：0 启动工作流，1 从当前节点开始执行，2 恢复被容错的工作流，3 恢复暂停流程 4 从失败节点开始执行',
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
