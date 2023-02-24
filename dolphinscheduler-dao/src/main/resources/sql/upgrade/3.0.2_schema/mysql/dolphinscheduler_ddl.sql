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


-- add unique key to t_ds_process_definition_log
drop PROCEDURE if EXISTS add_t_ds_process_definition_log_uk_uniq_idx_code_version;
delimiter d//
CREATE PROCEDURE add_t_ds_process_definition_log_uk_uniq_idx_code_version()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_NAME='t_ds_process_definition_log'
        AND TABLE_SCHEMA=(SELECT DATABASE())
        AND INDEX_NAME='uniq_idx_code_version')
    THEN
ALTER TABLE t_ds_process_definition_log ADD UNIQUE KEY uniq_idx_code_version(`code`,`version`);
END IF;
END;

d//

delimiter ;
CALL add_t_ds_process_definition_log_uk_uniq_idx_code_version;
DROP PROCEDURE add_t_ds_process_definition_log_uk_uniq_idx_code_version;
