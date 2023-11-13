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
-- Modify "t_ds_alert_plugin_instance" table
ALTER TABLE "t_ds_alert_plugin_instance" ADD COLUMN "instance_type" integer NOT NULL DEFAULT 0, ADD COLUMN "warning_type" integer NOT NULL DEFAULT 3;
-- Create "t_ds_listener_event" table
CREATE TABLE "t_ds_listener_event" ("id" integer NOT NULL, "content" text NULL, "sign" character varying(64) NOT NULL DEFAULT '', "post_status" integer NOT NULL DEFAULT 0, "event_type" integer NOT NULL, "log" text NULL, "create_time" timestamp NULL, "update_time" timestamp NULL, PRIMARY KEY ("id"));
-- Create index "idx_listener_event_post_status" to table: "t_ds_listener_event"
CREATE INDEX "idx_listener_event_post_status" ON "t_ds_listener_event" ("post_status");
-- Create index "idx_listener_event_sign" to table: "t_ds_listener_event"
CREATE INDEX "idx_listener_event_sign" ON "t_ds_listener_event" ("sign");
-- Set comment to column: "sign" on table: "t_ds_listener_event"
COMMENT ON COLUMN "t_ds_listener_event" ."sign" IS 'sign=sha1(content)';
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