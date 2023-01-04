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

DROP PROCEDURE IF EXISTS ut_dolphin_T_t_ds_fav;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_ds_fav()
BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_ds_fav'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
ALTER TABLE t_ds_fav RENAME t_ds_fav_task;
END IF;
END;
d//

delimiter ;
CALL ut_dolphin_T_t_ds_fav;
DROP PROCEDURE ut_dolphin_T_t_ds_fav;

CREATE TABLE IF NOT EXISTS t_ds_fav_task
(
    id        serial      NOT NULL,
    task_name varchar(64) NOT NULL,
    user_id   int         NOT NULL,
    PRIMARY KEY (id)
);
