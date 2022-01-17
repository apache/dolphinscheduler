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

delimiter d//
CREATE OR REPLACE FUNCTION public.dolphin_update_metadata(
	)
    RETURNS character varying
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
DECLARE
v_schema varchar;
BEGIN
    ---get schema name
    v_schema =current_schema();

EXECUTE 'DROP INDEX IF EXISTS "process_task_relation_idx_project_code_process_definition_code"';
EXECUTE 'CREATE INDEX IF NOT EXISTS process_task_relation_idx_project_code_process_definition_code ON ' || quote_ident(v_schema) ||'.t_ds_process_task_relation USING Btree("project_code","process_definition_code")';

EXECUTE 'DROP INDEX IF EXISTS "process_task_relation_log_idx_project_code_process_definition_code"';
EXECUTE 'CREATE INDEX IF NOT EXISTS process_task_relation_log_idx_project_code_process_definition_code ON ' || quote_ident(v_schema) ||'.t_ds_process_task_relation_log USING Btree("project_code","process_definition_code")';

EXECUTE 'DROP INDEX IF EXISTS "idx_task_definition_log_code_version"';
EXECUTE 'CREATE INDEX IF NOT EXISTS idx_task_definition_log_code_version ON ' || quote_ident(v_schema) ||'.t_ds_task_definition_log USING Btree("code","version")';

return 'Success!';
exception when others then
		---Raise EXCEPTION '(%)',SQLERRM;
        return SQLERRM;
END;
$BODY$;

select dolphin_update_metadata();

d//
