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
-- modify_data_t_ds_dq_rule_input_entry
delimiter d//
CREATE OR REPLACE FUNCTION modify_data_t_ds_dq_rule_input_entry() RETURNS void AS $$
BEGIN
       IF EXISTS (SELECT 1
                  FROM information_schema.columns
                  WHERE table_name = 't_ds_dq_rule_input_entry'
                  AND column_name = 'value')
      THEN
         ALTER TABLE t_ds_dq_rule_input_entry
                 RENAME COLUMN "value" TO "data";
       END IF;
END;
$$ LANGUAGE plpgsql;
d//

select modify_data_t_ds_dq_rule_input_entry();
DROP FUNCTION IF EXISTS modify_data_t_ds_dq_rule_input_entry();

-- modify_data_type_t_ds_dq_rule_input_entry
delimiter d//
CREATE OR REPLACE FUNCTION modify_data_type_t_ds_dq_rule_input_entry() RETURNS void AS $$
BEGIN
       IF EXISTS (SELECT 1
                  FROM information_schema.columns
                  WHERE table_name = 't_ds_dq_rule_input_entry'
                  AND column_name = 'value_type')
      THEN
         ALTER TABLE t_ds_dq_rule_input_entry
                 RENAME COLUMN "value_type" TO "data_type";
       END IF;
END;
$$ LANGUAGE plpgsql;
d//

select modify_data_type_t_ds_dq_rule_input_entry();
DROP FUNCTION IF EXISTS modify_data_type_t_ds_dq_rule_input_entry();