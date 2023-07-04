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

INSERT INTO t_ds_tenant(id, tenant_code, description, queue_id, create_time, update_time) VALUES (-1, 'default', 'default tenant', '0', '2018-03-27 15:48:50', '2018-10-24 17:40:22');

-- tenant improvement
UPDATE t_ds_schedules t1 SET t1.tenant_code = COALESCE(t3.tenant_code, 'default') FROM t_ds_process_definition t2 LEFT JOIN t_ds_tenant t3 ON t2.tenant_id = t3.id WHERE t1.process_definition_code = t2.code;
UPDATE t_ds_process_instance SET tenant_code = 'default' WHERE tenant_code IS NULL;

-- udpate t_ds_dq_rule_input_entry.options value to add `|Actual - Expected|`
update t_ds_dq_rule_input_entry
set options = '[{"label":"Expected - Actual","value":"0"},{"label":"Actual - Expected","value":"1"},{"label":"Actual / Expected","value":"2"},{"label":"(Expected - Actual) / Expected","value":"3"},{"label":"|Actual - Expected|","value":"4"}]'
where id = 7;

-- update t_ds_relation_rule_input_entry remove src_filter in custom sql
delete from t_ds_relation_rule_input_entry
where rule_id = 2 and rule_input_entry_id = 4;

-- Ask require for some of select input
update t_ds_dq_rule_input_entry set is_validate = 1 where id in (1, 2, 11, 12, 25, 28);