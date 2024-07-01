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

import org.apache.dolphinscheduler.dao.DaoConfiguration;
import org.apache.dolphinscheduler.dao.plugin.api.dialect.DatabaseDialect;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.annotation.DbType;

// todo: use spring-shell to manage the command
@SpringBootApplication
@ImportAutoConfiguration(DaoConfiguration.class)
public class CommandApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommandApplication.class, args);
    }

    @Component
    @Slf4j
    static class JdbcRegistrySchemaInitializeCommand implements CommandLineRunner {

        @Autowired
        private DatabaseDialect databaseDialect;

        @Autowired
        private DbType dbType;

        @Autowired
        private DataSource dataSource;

        JdbcRegistrySchemaInitializeCommand() {
        }

        @Override
        public void run(String... args) throws Exception {
            if (databaseDialect.tableExists("t_ds_jdbc_registry_data")
                    || databaseDialect.tableExists("t_ds_jdbc_registry_lock")) {
                log.warn("t_ds_jdbc_registry_data/t_ds_jdbc_registry_lock already exists");
                return;
            }
            if (dbType == DbType.MYSQL) {
                jdbcRegistrySchemaInitializeInMysql();
            } else if (dbType == DbType.POSTGRE_SQL) {
                jdbcRegistrySchemaInitializeInPG();
            } else {
                log.error("Unsupported database type: {}", dbType);
            }
        }

        private void jdbcRegistrySchemaInitializeInMysql() throws SQLException {
            try (
                    Connection connection = dataSource.getConnection();
                    Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE `t_ds_jdbc_registry_data`\n" +
                        "(\n" +
                        "    `id`               bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',\n" +
                        "    `data_key`         varchar(256) NOT NULL COMMENT 'key, like zookeeper node path',\n" +
                        "    `data_value`       text         NOT NULL COMMENT 'data, like zookeeper node value',\n" +
                        "    `data_type`        tinyint(4) NOT NULL COMMENT '1: ephemeral node, 2: persistent node',\n"
                        +
                        "    `last_term`        bigint       NOT NULL COMMENT 'last term time',\n" +
                        "    `last_update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last update time',\n"
                        +
                        "    `create_time`      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',\n"
                        +
                        "    PRIMARY KEY (`id`),\n" +
                        "    unique (`data_key`)\n" +
                        ") ENGINE = InnoDB\n" +
                        "  DEFAULT CHARSET = utf8;");

                statement.execute("CREATE TABLE `t_ds_jdbc_registry_lock`\n" +
                        "(\n" +
                        "    `id`               bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',\n" +
                        "    `lock_key`         varchar(256) NOT NULL COMMENT 'lock path',\n" +
                        "    `lock_owner`       varchar(256) NOT NULL COMMENT 'the lock owner, ip_processId',\n" +
                        "    `last_term`        bigint       NOT NULL COMMENT 'last term time',\n" +
                        "    `last_update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'last update time',\n"
                        +
                        "    `create_time`      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',\n"
                        +
                        "    PRIMARY KEY (`id`),\n" +
                        "    unique (`lock_key`)\n" +
                        ") ENGINE = InnoDB\n" +
                        "  DEFAULT CHARSET = utf8;");
            }
        }

        private void jdbcRegistrySchemaInitializeInPG() throws SQLException {
            try (
                    Connection connection = dataSource.getConnection();
                    Statement statement = connection.createStatement()) {
                statement.execute("create table t_ds_jdbc_registry_data\n" +
                        "(\n" +
                        "    id               serial\n" +
                        "        constraint t_ds_jdbc_registry_data_pk primary key,\n" +
                        "    data_key         varchar                             not null,\n" +
                        "    data_value       text                                not null,\n" +
                        "    data_type        int4                                not null,\n" +
                        "    last_term        bigint                              not null,\n" +
                        "    last_update_time timestamp default current_timestamp not null,\n" +
                        "    create_time      timestamp default current_timestamp not null\n" +
                        ");");
                statement.execute(
                        "create unique index t_ds_jdbc_registry_data_key_uindex on t_ds_jdbc_registry_data (data_key);");
                statement.execute("create table t_ds_jdbc_registry_lock\n" +
                        "(\n" +
                        "    id               serial\n" +
                        "        constraint t_ds_jdbc_registry_lock_pk primary key,\n" +
                        "    lock_key         varchar                             not null,\n" +
                        "    lock_owner       varchar                             not null,\n" +
                        "    last_term        bigint                              not null,\n" +
                        "    last_update_time timestamp default current_timestamp not null,\n" +
                        "    create_time      timestamp default current_timestamp not null\n" +
                        ");");
                statement.execute(
                        "create unique index t_ds_jdbc_registry_lock_key_uindex on t_ds_jdbc_registry_lock (lock_key);");
            }
        }

    }
}
