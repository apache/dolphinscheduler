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

CREATE TABLE t_ds_project_user_permission (
    id INT AUTO_INCREMENT NOT NULL,
    project_code BIGINT NOT NULL,
    user_id INT NOT NULL,
    permission VARCHAR(32) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_project_user_permission UNIQUE (project_code, user_id, permission)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
CREATE INDEX idx_user_permission_project ON t_ds_project_user_permission (user_id, permission, project_code);

-- Presence of a row grants the project use of the resource.
CREATE TABLE t_ds_project_resource_authorization (
    id INT AUTO_INCREMENT NOT NULL,
    project_code BIGINT NOT NULL,
    resource_type VARCHAR(32) NOT NULL,
    -- Up to 255 UTF-8 characters, preserving case and trailing spaces.
    resource_key VARBINARY(1020) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_project_resource_authorization UNIQUE (project_code, resource_type, resource_key)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
CREATE INDEX idx_resource_authorization_project ON t_ds_project_resource_authorization (resource_type, resource_key, project_code);
