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