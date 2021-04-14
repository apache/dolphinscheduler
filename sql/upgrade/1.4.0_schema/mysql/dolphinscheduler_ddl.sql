/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

SET sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''));

-- ----------------------------
-- Table structure for t_ds_plugin_define
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_plugin_define`;
CREATE TABLE `t_ds_plugin_define` (
  `id` int NOT NULL AUTO_INCREMENT,
  `plugin_name` varchar(100) NOT NULL COMMENT 'the name of plugin eg: email',
  `plugin_type` varchar(100) NOT NULL COMMENT 'plugin type . alert=alert plugin, job=job plugin',
  `plugin_params` text COMMENT 'plugin params',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `t_ds_plugin_define_UN` (`plugin_name`,`plugin_type`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_plugin_define
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_alert_plugin_instance
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_alert_plugin_instance`;
CREATE TABLE `t_ds_alert_plugin_instance` (
                                              `id`                     int NOT NULL AUTO_INCREMENT,
                                              `plugin_define_id`       int NOT NULL,
                                              `plugin_instance_params` text COMMENT 'plugin instance params. Also contain the params value which user input in web ui.',
                                              `create_time`            timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                              `update_time`            timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                              `instance_name`          varchar(200) DEFAULT NULL COMMENT 'alert instance name',
                                              PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_ds_alert_plugin_instance
-- ----------------------------

-- uc_dolphin_T_t_ds_process_definition_A_warning_group_id
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_process_definition_A_warning_group_id;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_process_definition_A_warning_group_id()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_process_definition'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='warning_group_id')
   THEN
         ALTER TABLE t_ds_process_definition ADD COLUMN `warning_group_id` int(11) DEFAULT NULL COMMENT 'alert group id' AFTER `connects`;
       END IF;
 END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_process_definition_A_warning_group_id();
DROP PROCEDURE uc_dolphin_T_t_ds_process_definition_A_warning_group_id;

-- uc_dolphin_T_t_ds_process_definition_version_A_warning_group_id
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_process_definition_version_A_warning_group_id;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_process_definition_version_A_warning_group_id()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_process_definition_version'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='warning_group_id')
   THEN
         ALTER TABLE t_ds_process_definition_version ADD COLUMN `warning_group_id` int(11) DEFAULT NULL COMMENT 'alert group id' AFTER `connects`;
       END IF;
 END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_process_definition_version_A_warning_group_id();
DROP PROCEDURE uc_dolphin_T_t_ds_process_definition_version_A_warning_group_id;

-- uc_dolphin_T_t_ds_alertgroup_A_alert_instance_ids
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_alertgroup_A_alert_instance_ids;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_alertgroup_A_alert_instance_ids()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_alertgroup'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='alert_instance_ids')
   THEN
         ALTER TABLE t_ds_alertgroup ADD COLUMN `alert_instance_ids` varchar (255) DEFAULT NULL COMMENT 'alert instance ids' AFTER `id`;
       END IF;
 END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_alertgroup_A_alert_instance_ids();
DROP PROCEDURE uc_dolphin_T_t_ds_alertgroup_A_alert_instance_ids;

-- uc_dolphin_T_t_ds_alertgroup_A_create_user_id
drop PROCEDURE if EXISTS uc_dolphin_T_t_ds_alertgroup_A_create_user_id;
delimiter d//
CREATE PROCEDURE uc_dolphin_T_t_ds_alertgroup_A_create_user_id()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_ds_alertgroup'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME ='create_user_id')
   THEN
         ALTER TABLE t_ds_alertgroup ADD COLUMN `create_user_id` int(11) DEFAULT NULL COMMENT 'create user id' AFTER `alert_instance_ids`;
       END IF;
 END;

d//

delimiter ;
CALL uc_dolphin_T_t_ds_alertgroup_A_create_user_id();
DROP PROCEDURE uc_dolphin_T_t_ds_alertgroup_A_create_user_id;

-- ----------------------------
-- These columns will not be used in the new version,if you determine that the historical data is useless, you can delete it using the sql below
-- ----------------------------

-- ALTER TABLE t_ds_process_definition DROP receivers, DROP receivers_cc;

-- ALTER TABLE t_ds_process_definition_version DROP receivers, DROP receivers_cc;

-- ALTER TABLE  t_ds_alert DROP show_type,DROP alert_type,DROP receivers,DROP receivers_cc;

-- ALTER TABLE  t_ds_alertgroup DROP group_type;

-- DROP TABLE IF EXISTS t_ds_relation_user_alertgroup;

-- ----------------------------
-- Table structure for t_ds_dq_execute_result
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_dq_execute_result`;
CREATE TABLE `t_ds_dq_execute_result` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `process_definition_id` int(11) DEFAULT NULL COMMENT 'task id',
    `process_instance_id` int(11) DEFAULT NULL COMMENT 'process instance id',
    `task_instance_id` int(11) DEFAULT NULL COMMENT 'task instance id',
    `rule_type` int(11) DEFAULT NULL COMMENT 'rule type',
    `rule_name` varchar(255) DEFAULT NULL COMMENT 'rule name',
    `statistics_value` double DEFAULT NULL COMMENT 'statistics value',
    `comparison_value` double DEFAULT NULL COMMENT 'comparison value',
    `check_type` int(11) DEFAULT NULL COMMENT 'check type',
    `threshold` double DEFAULT NULL COMMENT 'threshold',
    `operator` int(11) DEFAULT NULL COMMENT 'operator',
    `failure_strategy` int(11) DEFAULT NULL COMMENT 'dqs job failure strategy',
    `state` int(11) DEFAULT NULL COMMENT 'task state',
    `user_id` int(11) DEFAULT NULL COMMENT 'create user id',
    `create_time` datetime DEFAULT NULL COMMENT 'create time',
    `update_time` datetime DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='rule calculate result';

-- ----------------------------
-- Table structure for t_ds_dq_rule
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_dq_rule`;
CREATE TABLE `t_ds_dq_rule` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `name` varchar(100) DEFAULT NULL COMMENT 'rule name',
    `type` int(11) DEFAULT NULL COMMENT 'rule type',
    `user_id` int(11) DEFAULT NULL COMMENT 'create user id',
    `create_time` datetime DEFAULT NULL COMMENT 'create time',
    `update_time` datetime DEFAULT NULL COMMENT 'update_time',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='rule info';

INSERT INTO `t_ds_dq_rule`
(id, name, `type`, user_id, create_time, update_time)
VALUES(1, '空值检测', 0, 1, '2020-01-12 00:00:00.0', '2020-01-12 00:00:00.0');
INSERT INTO `t_ds_dq_rule`
(id, name, `type`, user_id, create_time, update_time)
VALUES(2, '自定义SQL', 1, 1, '2020-01-12 00:00:00.0', '2020-01-12 00:00:00.0');
INSERT INTO `t_ds_dq_rule`
(id, name, `type`, user_id, create_time, update_time)
VALUES(3, '跨表准确性', 2, 1, '2020-01-12 00:00:00.0', '2020-01-12 00:00:00.0');
INSERT INTO `t_ds_dq_rule`
(id, name, `type`, user_id, create_time, update_time)
VALUES(4, '跨表值比对', 3, 1, '2020-01-12 00:00:00.0', '2020-01-12 00:00:00.0');

-- ----------------------------
-- Table structure for t_ds_dq_rule_execute_sql
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_dq_rule_execute_sql`;
    CREATE TABLE `t_ds_dq_rule_execute_sql` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `index` int(11) DEFAULT NULL COMMENT 'sql execute index',
    `sql` text COMMENT 'sql',
    `table_alias` varchar(255) DEFAULT NULL COMMENT 'table alias',
    `type` int(11) DEFAULT NULL COMMENT 'execute sql type : middle,statistics,comparison',
    `create_time` datetime DEFAULT NULL COMMENT 'create time',
    `update_time` datetime DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='data quality execute sql definition in rule';

INSERT INTO `t_ds_dq_rule_execute_sql`
(id, `index`, `sql`, table_alias, `type`, create_time, update_time)
VALUES(1, 1, 'SELECT COUNT(*) AS miss FROM ${src_table} WHERE (${src_field} is null or ${src_field} = '''') AND (${src_filter})', 'miss_count', 1, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');
INSERT INTO `t_ds_dq_rule_execute_sql`
(id, `index`, `sql`, table_alias, `type`, create_time, update_time)
VALUES(2, 1, 'SELECT COUNT(*) AS total FROM ${src_table} WHERE (${src_filter})', 'total_count', 2, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');
INSERT INTO `t_ds_dq_rule_execute_sql`
(id, `index`, `sql`, table_alias, `type`, create_time, update_time)
VALUES(3, 1, 'SELECT * FROM (SELECT * FROM ${src_table} WHERE (${src_filter})) ${src_table} LEFT JOIN (SELECT * FROM ${target_table} WHERE (${target_filter})) ${target_table} ON ${on_clause} WHERE ${where_clause}', 'miss_items', 0, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');
INSERT INTO `t_ds_dq_rule_execute_sql`
(id, `index`, `sql`, table_alias, `type`, create_time, update_time)
VALUES(4, 1, 'SELECT COUNT(*) AS miss FROM miss_items', 'miss_count', 1, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');
INSERT INTO `t_ds_dq_rule_execute_sql`
(id, `index`, `sql`, table_alias, `type`, create_time, update_time)
VALUES(5, 1, 'SELECT COUNT(*) AS total FROM ${target_table} WHERE (${target_filter})', 'total_count', 2, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');

-- ----------------------------
-- Table structure for t_ds_dq_rule_input_entry
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_dq_rule_input_entry`;
CREATE TABLE `t_ds_dq_rule_input_entry` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `field` varchar(255) DEFAULT NULL COMMENT 'form filed name',
    `type` int(11) DEFAULT NULL COMMENT 'form type',
    `title` varchar(255) DEFAULT NULL COMMENT 'form title',
    `value` varchar(255) DEFAULT NULL COMMENT 'default value, can be null',
    `options` text DEFAULT NULL COMMENT 'default options, can be null, like [{label:"",value:""}]',
    `placeholder` varchar(255) DEFAULT NULL COMMENT 'placeholder',
    `option_source_type` int(11) DEFAULT NULL COMMENT 'he source type of options, use default options or other',
    `value_type` int(11) DEFAULT NULL COMMENT 'input entry value type: string,array,number .etc',
    `input_type` int(11) DEFAULT NULL COMMENT 'input entry type: default,statistics,comparison,check',
    `is_show` tinyint(1) DEFAULT '1' COMMENT 'whether to display on the front end',
    `can_edit` tinyint(1) DEFAULT '1' COMMENT 'whether to edit on the front end',
    `is_emit` tinyint(1) DEFAULT '0' COMMENT 'is emit event',
    `is_validate` tinyint(1) DEFAULT '0' COMMENT 'is validate',
    `create_time` datetime DEFAULT NULL COMMENT 'create time',
    `update_time` datetime DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8 COMMENT='data quality rule input entry';

INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(1, 'src_connector_type', 2, '源数据类型', '', '[{"label":"HIVE","value":"HIVE"},{"label":"JDBC","value":"JDBC"}]', 'please select source connector type', 2, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(2, 'src_datasource_id', 2, '源数据源', '', NULL, 'please select source datasource id', 1, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(3, 'src_table', 2, '源数据表', NULL, NULL, 'Please enter source table name', 0, 0, 0, 1, 1, 1, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(4, 'src_filter', 0, '源表过滤条件', NULL, NULL, 'Please enter filter expression', 0, 3, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(5, 'src_field', 2, '源表检测列', NULL, NULL, 'Please enter column, only single column is supported', 0, 0, 0, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(6, 'statistics_name', 0, '统计值名', NULL, NULL, 'Please enter statistics name, the alias in statistics execute sql', 0, 0, 1, 0, 0, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(7, 'check_type', 2, '检测方式', '0', '[{"label":"统计值与固定值比较","value":"0"},{"label":"统计值与比对值比较","value":"1"},{"label":"统计值占比对值百分比","value":"2"}]', 'please select check type', 0, 0, 3, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(8, 'operator', 2, '操作符', '0', '[{"label":"=","value":"0"},{"label":"<","value":"1"},{"label":"<=","value":"2"},{"label":">","value":"3"},{"label":">=","value":"4"},{"label":"!=","value":"5"}]', 'please select operator', 0, 0, 3, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(9, 'threshold', 0, '阈值', NULL, NULL, 'Please enter threshold, number is needed', 0, 2, 3, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(10, 'failure_strategy', 2, '失败策略', '0', '[{"label":"结束","value":"0"},{"label":"继续","value":"1"},{"label":"结束并告警","value":"2"},{"label":"继续并告警","value":"3"}]', 'please select failure strategy', 0, 0, 3, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(11, 'target_connector_type', 2, '目标数据类型', '', '[{"label":"HIVE","value":"HIVE"},{"label":"JDBC","value":"JDBC"}]', 'Please select target connector type', 2, 0, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(12, 'target_datasource_id', 2, '目标数据源', '', NULL, 'Please select target datasource', 1, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(13, 'target_table', 2, '目标数据表', NULL, NULL, 'Please enter target table', 0, 0, 0, 1, 1, 1, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(14, 'target_filter', 0, '目标表过滤条件', NULL, NULL, 'Please enter target filter expression', 0, 3, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(15, 'mapping_columns', 6, 'ON语句', NULL, '[{"field":"src_field","props":{"placeholder":"Please input src field","rows":0,"disabled":false,"size":"small"},"type":"input","title":"源数据列"},{"field":"operator","props":{"placeholder":"Please input operator","rows":0,"disabled":false,"size":"small"},"type":"input","title":"操作符"},{"field":"target_field","props":{"placeholder":"Please input target field","rows":0,"disabled":false,"size":"small"},"type":"input","title":"目标数据列"}]', 'please enter mapping columns', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(16, 'statistics_execute_sql', 5, '统计值计算SQL', NULL, NULL, 'Please enter statistics execute sql', 0, 3, 0, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(17, 'comparison_name', 0, '比对值名', NULL, NULL, 'Please enter comparison name, the alias in comparison execute sql', 0, 0, 0, 0, 0, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(18, 'comparison_execute_sql', 5, '比对值计算SQL', NULL, NULL, 'Please enter comparison execute sql', 0, 3, 0, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(19, 'comparison_title', 0, '比对值', '表总行数', NULL, 'Please enter comparison title', 0, 0, 2, 1, 0, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(20, 'writer_connector_type', 2, '输出数据类型', '', '[{"label":"MYSQL","value":"0"},{"label":"POSTGRESQL","value":"1"}]', 'please select writer connector type', 0, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(21, 'writer_datasource_id', 2, '输出数据源', '', NULL, 'please select writer datasource id', 1, 2, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, type, title, value, options, placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(22, 'target_field', 2, '目标表检测列', NULL, NULL, 'Please enter column, only single column is supported', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');

-- ----------------------------
-- Table structure for t_ds_relation_rule_execute_sql
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_relation_rule_execute_sql`;
CREATE TABLE `t_ds_relation_rule_execute_sql` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
    `rule_id` int(11) DEFAULT NULL COMMENT 'rule id',
    `execute_sql_id` int(11) DEFAULT NULL COMMENT 'execute sql id',
    `create_time` datetime DEFAULT NULL COMMENT 'create time',
    `update_time` datetime DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `t_ds_relation_rule_execute_sql`
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(1, 1, 1, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');
INSERT INTO `t_ds_relation_rule_execute_sql`
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(2, 1, 2, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');
INSERT INTO `t_ds_relation_rule_execute_sql`
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(3, 2, 2, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');
INSERT INTO `t_ds_relation_rule_execute_sql`
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(4, 3, 3, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');
INSERT INTO `t_ds_relation_rule_execute_sql`
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(5, 3, 4, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');
INSERT INTO `t_ds_relation_rule_execute_sql`
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(6, 3, 5, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');

-- ----------------------------
-- Table structure for t_ds_relation_rule_input_entry
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_relation_rule_input_entry`;
CREATE TABLE `t_ds_relation_rule_input_entry` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
    `rule_id` int(11) DEFAULT NULL COMMENT 'rule id',
    `rule_input_entry_id` int(11) DEFAULT NULL COMMENT 'rule input entry id',
    `values_map` text COMMENT 'input entry value map',
    `index` int(11) DEFAULT NULL COMMENT 'index',
    `create_time` datetime DEFAULT NULL COMMENT 'create time',
    `update_time` datetime DEFAULT NULL COMMENT 'update time',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8;
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(1, 1, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(2, 1, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(3, 1, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(4, 1, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(5, 1, 5, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(6, 1, 6, '{"statistics_name":"miss_count.miss"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(7, 1, 7, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(8, 1, 8, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(9, 1, 9, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(10, 1, 10, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(11, 1, 17, '{"comparison_name":"total_count.total"}', 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(12, 1, 19, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(13, 2, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(14, 2, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(15, 2, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(16, 2, 6, '{"is_show":"true","can_edit":"true"}', 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(17, 2, 16, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(18, 2, 4, NULL, 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(19, 2, 7, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(20, 2, 8, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(21, 2, 9, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(22, 2, 10, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(23, 2, 17, '{"comparison_name":"total_count.total"}', 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(24, 2, 19, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(25, 3, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(26, 3, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(27, 3, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(28, 3, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(29, 3, 11, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(30, 3, 12, NULL, 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(31, 3, 13, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(32, 3, 14, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(33, 3, 15, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(34, 3, 7, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(35, 3, 8, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(36, 3, 9, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(37, 3, 10, NULL, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(38, 3, 17, '{"comparison_name":"total_count.total"}', 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(39, 3, 19, NULL, 15, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(40, 4, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(41, 4, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(42, 4, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(43, 4, 6, '{"is_show":"true","can_edit":"true"}', 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(44, 4, 16, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(45, 4, 11, NULL, 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(46, 4, 12, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(47, 4, 13, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(48, 4, 17, '{"is_show":"true","can_edit":"true"}', 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(49, 4, 18, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(50, 4, 7, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(51, 4, 8, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(52, 4, 9, NULL, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(53, 4, 10, NULL, 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(54, 1, 20, NULL, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(55, 1, 21, NULL, 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(56, 2, 20, NULL, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(57, 2, 21, NULL, 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(58, 3, 20, NULL, 16, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(59, 3, 21, NULL, 17, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(60, 4, 20, NULL, 15, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(61, 4, 21, NULL, 16, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(id, rule_id, rule_input_entry_id, values_map, `index`, create_time, update_time)
VALUES(62, 3, 6, '{"statistics_name":"miss_count.miss"}', 18, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
