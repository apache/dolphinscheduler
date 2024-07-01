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

-- ############################# t_ds_tenant #############################
drop PROCEDURE if EXISTS dolphin_t_ds_tenant_insert_default;
delimiter d//
CREATE PROCEDURE dolphin_t_ds_tenant_insert_default()
BEGIN
    IF
NOT EXISTS(SELECT 1
                   FROM t_ds_tenant
                   WHERE id = -1)
    THEN
        INSERT INTO `t_ds_tenant` VALUES ('-1', 'default', 'default tenant', '1', current_timestamp, current_timestamp);
END IF;
END;
d//

delimiter ;
CALL dolphin_t_ds_tenant_insert_default();
DROP PROCEDURE dolphin_t_ds_tenant_insert_default;

-- tenant improvement
UPDATE t_ds_schedules t1 JOIN t_ds_process_definition t2 ON t1.process_definition_code = t2.code LEFT JOIN t_ds_tenant t3 ON t2.tenant_id = t3.id SET t1.tenant_code = COALESCE(t3.tenant_code, 'default');
UPDATE `t_ds_process_instance` SET `tenant_code` = 'default' WHERE `tenant_code` IS NULL;

-- data quality support choose database
INSERT IGNORE INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(30, 'src_database', 'select', '$t(src_database)', NULL, NULL, 'please select source database', 0, 0, 0, 1, 1, 1, 1, current_timestamp, current_timestamp);
INSERT IGNORE INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(31, 'target_database', 'select', '$t(target_database)', NULL, NULL, 'please select target database', 0, 0, 0, 1, 1, 1, 1, current_timestamp, current_timestamp);

INSERT IGNORE INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(151, 1, 30, NULL, 2, current_timestamp, current_timestamp);
INSERT IGNORE INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(152, 2, 30, NULL, 2, current_timestamp, current_timestamp);
INSERT IGNORE INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(153, 3, 30, NULL, 2, current_timestamp, current_timestamp);
INSERT IGNORE INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(154, 4, 30, NULL, 2, current_timestamp, current_timestamp);
INSERT IGNORE INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(155, 5, 30, NULL, 2, current_timestamp, current_timestamp);
INSERT IGNORE INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(156, 6, 30, NULL, 2, current_timestamp, current_timestamp);
INSERT IGNORE INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(157, 7, 30, NULL, 2, current_timestamp, current_timestamp);
INSERT IGNORE INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(158, 8, 30, NULL, 2, current_timestamp, current_timestamp);
INSERT IGNORE INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(159, 9, 30, NULL, 2, current_timestamp, current_timestamp);
INSERT IGNORE INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(160, 10, 30, NULL, 2, current_timestamp, current_timestamp);
INSERT IGNORE INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(161, 3, 31, NULL, 6, current_timestamp, current_timestamp);
INSERT IGNORE INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(162, 4, 31, NULL, 7, current_timestamp, current_timestamp);
