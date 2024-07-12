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


DROP TABLE IF EXISTS `t_ds_jdbc_registry_data`;
CREATE TABLE `t_ds_jdbc_registry_data`
(
    `id`               bigint(11)   NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `data_key`         varchar(256) NOT NULL COMMENT 'key, like zookeeper node path',
    `data_value`       text         NOT NULL COMMENT 'data, like zookeeper node value',
    `data_type`        varchar(64)  NOT NULL COMMENT 'EPHEMERAL, PERSISTENT',
    `client_id`        bigint(11)   NOT NULL COMMENT 'client id',
    `create_time`      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `last_update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'last update time',
    PRIMARY KEY (`id`),
    unique Key `uk_t_ds_jdbc_registry_dataKey` (`data_key`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


DROP TABLE IF EXISTS `t_ds_jdbc_registry_lock`;
CREATE TABLE `t_ds_jdbc_registry_lock`
(
    `id`          bigint(11)   NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `lock_key`    varchar(256) NOT NULL COMMENT 'lock path',
    `lock_owner`  varchar(256) NOT NULL COMMENT 'the lock owner, ip_processId',
    `client_id`   bigint(11)   NOT NULL COMMENT 'client id',
    `create_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    PRIMARY KEY (`id`),
    unique Key `uk_t_ds_jdbc_registry_lockKey` (`lock_key`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `t_ds_jdbc_registry_client_heartbeat`;
CREATE TABLE `t_ds_jdbc_registry_client_heartbeat`
(
    `id`                  bigint(11)   NOT NULL COMMENT 'primary key',
    `client_name`         varchar(256) NOT NULL COMMENT 'client name, ip_processId',
    `last_heartbeat_time` bigint(11)   NOT NULL COMMENT 'last heartbeat timestamp',
    `connection_config`   text         NOT NULL COMMENT 'connection config',
    `create_time`         timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `t_ds_jdbc_registry_data_change_event`;
CREATE TABLE `t_ds_jdbc_registry_data_change_event`
(
    `id`                 bigint(11)  NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `event_type`         varchar(64) NOT NULL COMMENT 'ADD, UPDATE, DELETE',
    `jdbc_registry_data` text        NOT NULL COMMENT 'jdbc registry data',
    `create_time`        timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
