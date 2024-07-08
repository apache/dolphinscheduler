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

package org.apache.dolphinscheduler.tools.command;

import org.apache.dolphinscheduler.dao.plugin.api.dialect.DatabaseDialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.annotation.DbType;

@Component
@Slf4j
public class JdbcRegistrySchemaInitializeCommand implements ICommand {

    @Autowired
    private DatabaseDialect databaseDialect;

    @Autowired
    private DbType dbType;

    @Autowired
    private DataSource dataSource;

    @SneakyThrows
    @Override
    public void run(String... args) {
        switch (dbType) {
            case MYSQL:
                jdbcRegistrySchemaInitializeInMysql();
                break;
            case POSTGRE_SQL:
                jdbcRegistrySchemaInitializeInPG();
                break;
            default:
                log.error("Unsupported database type: {}", dbType);
        }
    }

    private void jdbcRegistrySchemaInitializeInMysql() throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            // todo: directly load jdbc sql file
            statement.execute("DROP TABLE IF EXISTS `t_ds_jdbc_registry_data`;");
            statement.execute(
                    "CREATE TABLE `t_ds_jdbc_registry_data`\n" +
                            "(\n" +
                            "    `id`               bigint(11)   NOT NULL AUTO_INCREMENT COMMENT 'primary key',\n" +
                            "    `data_key`         varchar(256) NOT NULL COMMENT 'key, like zookeeper node path',\n"
                            +
                            "    `data_value`       text         NOT NULL COMMENT 'data, like zookeeper node value',\n"
                            +
                            "    `data_type`        varchar(64)  NOT NULL COMMENT 'EPHEMERAL, PERSISTENT',\n" +
                            "    `client_id`        bigint(11)   NOT NULL COMMENT 'client id',\n" +
                            "    `create_time`      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',\n"
                            +
                            "    `last_update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'last update time',\n"
                            +
                            "    PRIMARY KEY (`id`),\n" +
                            "    unique Key `uk_t_ds_jdbc_registry_dataKey` (`data_key`)\n" +
                            ") ENGINE = InnoDB\n" +
                            "  DEFAULT CHARSET = utf8;");

            statement.execute("DROP TABLE IF EXISTS `t_ds_jdbc_registry_lock`;");
            statement.execute(
                    "CREATE TABLE `t_ds_jdbc_registry_lock`\n" +
                            "(\n" +
                            "    `id`          bigint(11)   NOT NULL AUTO_INCREMENT COMMENT 'primary key',\n" +
                            "    `lock_key`    varchar(256) NOT NULL COMMENT 'lock path',\n" +
                            "    `lock_owner`  varchar(256) NOT NULL COMMENT 'the lock owner, ip_processId',\n" +
                            "    `client_id`   bigint(11)   NOT NULL COMMENT 'client id',\n" +
                            "    `create_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',\n"
                            +
                            "    PRIMARY KEY (`id`),\n" +
                            "    unique Key `uk_t_ds_jdbc_registry_lockKey` (`lock_key`)\n" +
                            ") ENGINE = InnoDB\n" +
                            "  DEFAULT CHARSET = utf8;");

            statement.execute("DROP TABLE IF EXISTS `t_ds_jdbc_registry_client_heartbeat`;");
            statement.execute("CREATE TABLE `t_ds_jdbc_registry_client_heartbeat`\n" +
                    "(\n" +
                    "    `id`                  bigint(11)   NOT NULL COMMENT 'primary key',\n" +
                    "    `client_name`         varchar(256) NOT NULL COMMENT 'client name, ip_processId',\n" +
                    "    `last_heartbeat_time` bigint(11)   NOT NULL COMMENT 'last heartbeat timestamp',\n" +
                    "    `connection_config`   text         NOT NULL COMMENT 'connection config',\n" +
                    "    `create_time`         timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',\n"
                    +
                    "    PRIMARY KEY (`id`)\n" +
                    ") ENGINE = InnoDB\n" +
                    "  DEFAULT CHARSET = utf8;");

            statement.execute("DROP TABLE IF EXISTS `t_ds_jdbc_registry_data_change_event`;");
            statement.execute("CREATE TABLE `t_ds_jdbc_registry_data_change_event`\n" +
                    "(\n" +
                    "    `id`                 bigint(11)  NOT NULL AUTO_INCREMENT COMMENT 'primary key',\n" +
                    "    `event_type`         varchar(64) NOT NULL COMMENT 'ADD, UPDATE, DELETE',\n" +
                    "    `jdbc_registry_data` text        NOT NULL COMMENT 'jdbc registry data',\n" +
                    "    `create_time`        timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',\n"
                    +
                    "    PRIMARY KEY (`id`)\n" +
                    ") ENGINE = InnoDB\n" +
                    "  DEFAULT CHARSET = utf8;");
        }
    }

    private void jdbcRegistrySchemaInitializeInPG() throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {

            statement.execute("DROP TABLE IF EXISTS t_ds_jdbc_registry_data;");
            statement.execute(
                    "create table t_ds_jdbc_registry_data\n" +
                            "(\n" +
                            "    id               bigserial NOT NULL,\n" +
                            "    data_key         varchar   not null,\n" +
                            "    data_value       text      not null,\n" +
                            "    data_type        varchar   not null,\n" +
                            "    client_id        bigint    not null,\n" +
                            "    create_time      timestamp not null default current_timestamp,\n" +
                            "    last_update_time timestamp not null default current_timestamp,\n" +
                            "PRIMARY KEY (id)\n" +
                            ");");
            statement.execute(
                    "create unique index uk_t_ds_jdbc_registry_dataKey on t_ds_jdbc_registry_data (data_key);");

            statement.execute("DROP TABLE IF EXISTS t_ds_jdbc_registry_lock;");
            statement.execute(
                    "create table t_ds_jdbc_registry_lock\n" +
                            "(\n" +
                            "    id          bigserial NOT NULL,\n" +
                            "    lock_key    varchar   not null,\n" +
                            "    lock_owner  varchar   not null,\n" +
                            "    client_id   bigint    not null,\n" +
                            "    create_time timestamp not null default current_timestamp,\n" +
                            "PRIMARY KEY (id)\n" +
                            ");");
            statement.execute(
                    "create unique index uk_t_ds_jdbc_registry_lockKey on t_ds_jdbc_registry_lock (lock_key);");

            statement.execute("DROP TABLE IF EXISTS t_ds_jdbc_registry_client_heartbeat;");
            statement.execute("create table t_ds_jdbc_registry_client_heartbeat\n" +
                    "(\n" +
                    "    id                  bigint    NOT NULL,\n" +
                    "    client_name         varchar   not null,\n" +
                    "    last_heartbeat_time bigint    not null,\n" +
                    "    connection_config   text      not null,\n" +
                    "    create_time         timestamp not null default current_timestamp,\n" +
                    "PRIMARY KEY (id)\n" +
                    ");");

            statement.execute("DROP TABLE IF EXISTS t_ds_jdbc_registry_data_change_event;");
            statement.execute("create table t_ds_jdbc_registry_data_change_event\n" +
                    "(\n" +
                    "    id                 bigserial NOT NULL,\n" +
                    "    event_type         varchar   not null,\n" +
                    "    jdbc_registry_data text      not null,\n" +
                    "    create_time        timestamp not null default current_timestamp,\n" +
                    "PRIMARY KEY (id)\n" +
                    ");");
        }
    }

}
