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

SET
FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for QRTZ_JOB_DETAILS
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_JOB_DETAILS;
CREATE TABLE QRTZ_JOB_DETAILS
(
    SCHED_NAME        varchar(120) NOT NULL,
    JOB_NAME          varchar(200) NOT NULL,
    JOB_GROUP         varchar(200) NOT NULL,
    DESCRIPTION       varchar(250) DEFAULT NULL,
    JOB_CLASS_NAME    varchar(250) NOT NULL,
    IS_DURABLE        varchar(1)   NOT NULL,
    IS_NONCONCURRENT  varchar(1)   NOT NULL,
    IS_UPDATE_DATA    varchar(1)   NOT NULL,
    REQUESTS_RECOVERY varchar(1)   NOT NULL,
    JOB_DATA          blob,
    PRIMARY KEY (SCHED_NAME, JOB_NAME, JOB_GROUP)
);

-- ----------------------------
-- Table structure for QRTZ_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_TRIGGERS;
CREATE TABLE QRTZ_TRIGGERS
(
    SCHED_NAME     varchar(120) NOT NULL,
    TRIGGER_NAME   varchar(200) NOT NULL,
    TRIGGER_GROUP  varchar(200) NOT NULL,
    JOB_NAME       varchar(200) NOT NULL,
    JOB_GROUP      varchar(200) NOT NULL,
    DESCRIPTION    varchar(250) DEFAULT NULL,
    NEXT_FIRE_TIME bigint(13) DEFAULT NULL,
    PREV_FIRE_TIME bigint(13) DEFAULT NULL,
    PRIORITY       int(11) DEFAULT NULL,
    TRIGGER_STATE  varchar(16)  NOT NULL,
    TRIGGER_TYPE   varchar(8)   NOT NULL,
    START_TIME     bigint(13) NOT NULL,
    END_TIME       bigint(13) DEFAULT NULL,
    CALENDAR_NAME  varchar(200) DEFAULT NULL,
    MISFIRE_INSTR  smallint(2) DEFAULT NULL,
    JOB_DATA       blob,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    CONSTRAINT QRTZ_TRIGGERS_ibfk_1 FOREIGN KEY (SCHED_NAME, JOB_NAME, JOB_GROUP) REFERENCES QRTZ_JOB_DETAILS (SCHED_NAME, JOB_NAME, JOB_GROUP)
);

-- ----------------------------
-- Table structure for QRTZ_BLOB_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_BLOB_TRIGGERS;
CREATE TABLE QRTZ_BLOB_TRIGGERS
(
    SCHED_NAME    varchar(120) NOT NULL,
    TRIGGER_NAME  varchar(200) NOT NULL,
    TRIGGER_GROUP varchar(200) NOT NULL,
    BLOB_DATA     blob,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) REFERENCES QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

-- ----------------------------
-- Records of QRTZ_BLOB_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_CALENDARS
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_CALENDARS;
CREATE TABLE QRTZ_CALENDARS
(
    SCHED_NAME    varchar(120) NOT NULL,
    CALENDAR_NAME varchar(200) NOT NULL,
    CALENDAR      blob         NOT NULL,
    PRIMARY KEY (SCHED_NAME, CALENDAR_NAME)
);

-- ----------------------------
-- Records of QRTZ_CALENDARS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_CRON_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_CRON_TRIGGERS;
CREATE TABLE QRTZ_CRON_TRIGGERS
(
    SCHED_NAME      varchar(120) NOT NULL,
    TRIGGER_NAME    varchar(200) NOT NULL,
    TRIGGER_GROUP   varchar(200) NOT NULL,
    CRON_EXPRESSION varchar(120) NOT NULL,
    TIME_ZONE_ID    varchar(80) DEFAULT NULL,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    CONSTRAINT QRTZ_CRON_TRIGGERS_ibfk_1 FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) REFERENCES QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

-- ----------------------------
-- Records of QRTZ_CRON_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_FIRED_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_FIRED_TRIGGERS;
CREATE TABLE QRTZ_FIRED_TRIGGERS
(
    SCHED_NAME        varchar(120) NOT NULL,
    ENTRY_ID          varchar(200) NOT NULL,
    TRIGGER_NAME      varchar(200) NOT NULL,
    TRIGGER_GROUP     varchar(200) NOT NULL,
    INSTANCE_NAME     varchar(200) NOT NULL,
    FIRED_TIME        bigint(13) NOT NULL,
    SCHED_TIME        bigint(13) NOT NULL,
    PRIORITY          int(11) NOT NULL,
    STATE             varchar(16)  NOT NULL,
    JOB_NAME          varchar(200) DEFAULT NULL,
    JOB_GROUP         varchar(200) DEFAULT NULL,
    IS_NONCONCURRENT  varchar(1)   DEFAULT NULL,
    REQUESTS_RECOVERY varchar(1)   DEFAULT NULL,
    PRIMARY KEY (SCHED_NAME, ENTRY_ID)
);

-- ----------------------------
-- Records of QRTZ_FIRED_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Records of QRTZ_JOB_DETAILS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_LOCKS
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_LOCKS;
CREATE TABLE QRTZ_LOCKS
(
    SCHED_NAME varchar(120) NOT NULL,
    LOCK_NAME  varchar(40)  NOT NULL,
    PRIMARY KEY (SCHED_NAME, LOCK_NAME)
);

-- ----------------------------
-- Records of QRTZ_LOCKS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_PAUSED_TRIGGER_GRPS
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_PAUSED_TRIGGER_GRPS;
CREATE TABLE QRTZ_PAUSED_TRIGGER_GRPS
(
    SCHED_NAME    varchar(120) NOT NULL,
    TRIGGER_GROUP varchar(200) NOT NULL,
    PRIMARY KEY (SCHED_NAME, TRIGGER_GROUP)
);

-- ----------------------------
-- Records of QRTZ_PAUSED_TRIGGER_GRPS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_SCHEDULER_STATE
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_SCHEDULER_STATE;
CREATE TABLE QRTZ_SCHEDULER_STATE
(
    SCHED_NAME        varchar(120) NOT NULL,
    INSTANCE_NAME     varchar(200) NOT NULL,
    LAST_CHECKIN_TIME bigint(13) NOT NULL,
    CHECKIN_INTERVAL  bigint(13) NOT NULL,
    PRIMARY KEY (SCHED_NAME, INSTANCE_NAME)
);

-- ----------------------------
-- Records of QRTZ_SCHEDULER_STATE
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_SIMPLE_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_SIMPLE_TRIGGERS;
CREATE TABLE QRTZ_SIMPLE_TRIGGERS
(
    SCHED_NAME      varchar(120) NOT NULL,
    TRIGGER_NAME    varchar(200) NOT NULL,
    TRIGGER_GROUP   varchar(200) NOT NULL,
    REPEAT_COUNT    bigint(7) NOT NULL,
    REPEAT_INTERVAL bigint(12) NOT NULL,
    TIMES_TRIGGERED bigint(10) NOT NULL,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    CONSTRAINT QRTZ_SIMPLE_TRIGGERS_ibfk_1 FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) REFERENCES QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

-- ----------------------------
-- Records of QRTZ_SIMPLE_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for QRTZ_SIMPROP_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_SIMPROP_TRIGGERS;
CREATE TABLE QRTZ_SIMPROP_TRIGGERS
(
    SCHED_NAME    varchar(120) NOT NULL,
    TRIGGER_NAME  varchar(200) NOT NULL,
    TRIGGER_GROUP varchar(200) NOT NULL,
    STR_PROP_1    varchar(512)   DEFAULT NULL,
    STR_PROP_2    varchar(512)   DEFAULT NULL,
    STR_PROP_3    varchar(512)   DEFAULT NULL,
    INT_PROP_1    int(11) DEFAULT NULL,
    INT_PROP_2    int(11) DEFAULT NULL,
    LONG_PROP_1   bigint(20) DEFAULT NULL,
    LONG_PROP_2   bigint(20) DEFAULT NULL,
    DEC_PROP_1    decimal(13, 4) DEFAULT NULL,
    DEC_PROP_2    decimal(13, 4) DEFAULT NULL,
    BOOL_PROP_1   varchar(1)     DEFAULT NULL,
    BOOL_PROP_2   varchar(1)     DEFAULT NULL,
    PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
    CONSTRAINT QRTZ_SIMPROP_TRIGGERS_ibfk_1 FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) REFERENCES QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
);

-- ----------------------------
-- Records of QRTZ_SIMPROP_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Records of QRTZ_TRIGGERS
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_access_token
-- ----------------------------
DROP TABLE IF EXISTS t_ds_access_token;
CREATE TABLE t_ds_access_token
(
    id          int(11) NOT NULL AUTO_INCREMENT,
    user_id     int(11) DEFAULT NULL,
    token       varchar(64) DEFAULT NULL,
    expire_time datetime    DEFAULT NULL,
    create_time datetime    DEFAULT NULL,
    update_time datetime    DEFAULT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Records of t_ds_access_token
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_alert
-- ----------------------------
DROP TABLE IF EXISTS t_ds_alert;
CREATE TABLE t_ds_alert
(
    id            int(11) NOT NULL AUTO_INCREMENT,
    title         varchar(64) DEFAULT NULL,
    content       text,
    alert_status  tinyint(4) DEFAULT '0',
    log           text,
    alertgroup_id int(11) DEFAULT NULL,
    create_time   datetime    DEFAULT NULL,
    update_time   datetime    DEFAULT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Records of t_ds_alert
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_alertgroup
-- ----------------------------
DROP TABLE IF EXISTS t_ds_alertgroup;
CREATE TABLE t_ds_alertgroup
(
    id                 int(11) NOT NULL AUTO_INCREMENT,
    alert_instance_ids varchar(255) DEFAULT NULL,
    create_user_id     int(11) DEFAULT NULL,
    group_name         varchar(255) DEFAULT NULL,
    description        varchar(255) DEFAULT NULL,
    create_time        datetime     DEFAULT NULL,
    update_time        datetime     DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY t_ds_alertgroup_name_un (group_name)
);

-- ----------------------------
-- Records of t_ds_alertgroup
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_command
-- ----------------------------
DROP TABLE IF EXISTS t_ds_command;
CREATE TABLE t_ds_command
(
    id                        int(11) NOT NULL AUTO_INCREMENT,
    command_type              tinyint(4) DEFAULT NULL,
    process_definition_id     int(11) DEFAULT NULL,
    command_param             text,
    task_depend_type          tinyint(4) DEFAULT NULL,
    failure_strategy          tinyint(4) DEFAULT '0',
    warning_type              tinyint(4) DEFAULT '0',
    warning_group_id          int(11) DEFAULT NULL,
    schedule_time             datetime DEFAULT NULL,
    start_time                datetime DEFAULT NULL,
    executor_id               int(11) DEFAULT NULL,
    update_time               datetime DEFAULT NULL,
    process_instance_priority int(11) DEFAULT NULL,
    worker_group              varchar(64),
    environment_code          bigint(20) DEFAULT '-1',
    PRIMARY KEY (id)
);

-- ----------------------------
-- Records of t_ds_command
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_datasource
-- ----------------------------
DROP TABLE IF EXISTS t_ds_datasource;
CREATE TABLE t_ds_datasource
(
    id                int(11) NOT NULL AUTO_INCREMENT,
    name              varchar(64) NOT NULL,
    note              varchar(255) DEFAULT NULL,
    type              tinyint(4) NOT NULL,
    user_id           int(11) NOT NULL,
    connection_params text        NOT NULL,
    create_time       datetime    NOT NULL,
    update_time       datetime     DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY t_ds_datasource_name_un (name, type)
);

-- ----------------------------
-- Records of t_ds_datasource
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_error_command
-- ----------------------------
DROP TABLE IF EXISTS t_ds_error_command;
CREATE TABLE t_ds_error_command
(
    id                        int(11) NOT NULL,
    command_type              tinyint(4) DEFAULT NULL,
    executor_id               int(11) DEFAULT NULL,
    process_definition_id     int(11) DEFAULT NULL,
    command_param             text,
    task_depend_type          tinyint(4) DEFAULT NULL,
    failure_strategy          tinyint(4) DEFAULT '0',
    warning_type              tinyint(4) DEFAULT '0',
    warning_group_id          int(11) DEFAULT NULL,
    schedule_time             datetime DEFAULT NULL,
    start_time                datetime DEFAULT NULL,
    update_time               datetime DEFAULT NULL,
    process_instance_priority int(11) DEFAULT NULL,
    worker_group              varchar(64),
    environment_code          bigint(20) DEFAULT '-1',
    message                   text,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Records of t_ds_error_command
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_process_definition
-- ----------------------------
DROP TABLE IF EXISTS t_ds_process_definition;
CREATE TABLE t_ds_process_definition
(
    id               int(11) NOT NULL AUTO_INCREMENT,
    code             bigint(20) NOT NULL,
    name             varchar(255) DEFAULT NULL,
    version          int(11) DEFAULT NULL,
    description      text,
    project_code     bigint(20) NOT NULL,
    release_state    tinyint(4) DEFAULT NULL,
    user_id          int(11) DEFAULT NULL,
    global_params    text,
    flag             tinyint(4) DEFAULT NULL,
    locations        text,
    warning_group_id int(11) DEFAULT NULL,
    timeout          int(11) DEFAULT '0',
    tenant_id        int(11) NOT NULL DEFAULT '-1',
    create_time      datetime NOT NULL,
    update_time      datetime     DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY process_unique (name,project_code) USING BTREE,
    UNIQUE KEY code_unique (code)
);

-- ----------------------------
-- Records of t_ds_process_definition
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_process_definition_log
-- ----------------------------
DROP TABLE IF EXISTS t_ds_process_definition_log;
CREATE TABLE t_ds_process_definition_log
(
    id               int(11) NOT NULL AUTO_INCREMENT,
    code             bigint(20) NOT NULL,
    name             varchar(200) DEFAULT NULL,
    version          int(11) DEFAULT NULL,
    description      text,
    project_code     bigint(20) NOT NULL,
    release_state    tinyint(4) DEFAULT NULL,
    user_id          int(11) DEFAULT NULL,
    global_params    text,
    flag             tinyint(4) DEFAULT NULL,
    locations        text,
    warning_group_id int(11) DEFAULT NULL,
    timeout          int(11) DEFAULT '0',
    tenant_id        int(11) NOT NULL DEFAULT '-1',
    operator         int(11) DEFAULT NULL,
    operate_time     datetime     DEFAULT NULL,
    create_time      datetime NOT NULL,
    update_time      datetime     DEFAULT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for t_ds_task_definition
-- ----------------------------
DROP TABLE IF EXISTS t_ds_task_definition;
CREATE TABLE t_ds_task_definition
(
    id                      int(11) NOT NULL AUTO_INCREMENT,
    code                    bigint(20) NOT NULL,
    name                    varchar(200) DEFAULT NULL,
    version                 int(11) DEFAULT NULL,
    description             text,
    project_code            bigint(20) NOT NULL,
    user_id                 int(11) DEFAULT NULL,
    task_type               varchar(50) NOT NULL,
    task_params             longtext,
    flag                    tinyint(2) DEFAULT NULL,
    task_priority           tinyint(4) DEFAULT NULL,
    worker_group            varchar(200) DEFAULT NULL,
    environment_code        bigint(20) DEFAULT '-1',
    fail_retry_times        int(11) DEFAULT NULL,
    fail_retry_interval     int(11) DEFAULT NULL,
    timeout_flag            tinyint(2) DEFAULT '0',
    timeout_notify_strategy tinyint(4) DEFAULT NULL,
    timeout                 int(11) DEFAULT '0',
    delay_time              int(11) DEFAULT '0',
    resource_ids            varchar(255) DEFAULT NULL,
    create_time             datetime    NOT NULL,
    update_time             datetime     DEFAULT NULL,
    PRIMARY KEY (id, code),
    UNIQUE KEY task_unique (name,project_code) USING BTREE
);

-- ----------------------------
-- Table structure for t_ds_task_definition_log
-- ----------------------------
DROP TABLE IF EXISTS t_ds_task_definition_log;
CREATE TABLE t_ds_task_definition_log
(
    id                      int(11) NOT NULL AUTO_INCREMENT,
    code                    bigint(20) NOT NULL,
    name                    varchar(200) DEFAULT NULL,
    version                 int(11) DEFAULT NULL,
    description             text,
    project_code            bigint(20) NOT NULL,
    user_id                 int(11) DEFAULT NULL,
    task_type               varchar(50) NOT NULL,
    task_params             text,
    flag                    tinyint(2) DEFAULT NULL,
    task_priority           tinyint(4) DEFAULT NULL,
    worker_group            varchar(200) DEFAULT NULL,
    environment_code        bigint(20) DEFAULT '-1',
    fail_retry_times        int(11) DEFAULT NULL,
    fail_retry_interval     int(11) DEFAULT NULL,
    timeout_flag            tinyint(2) DEFAULT '0',
    timeout_notify_strategy tinyint(4) DEFAULT NULL,
    timeout                 int(11) DEFAULT '0',
    delay_time              int(11) DEFAULT '0',
    resource_ids            varchar(255) DEFAULT NULL,
    operator                int(11) DEFAULT NULL,
    operate_time            datetime     DEFAULT NULL,
    create_time             datetime    NOT NULL,
    update_time             datetime     DEFAULT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for t_ds_process_task_relation
-- ----------------------------
DROP TABLE IF EXISTS t_ds_process_task_relation;
CREATE TABLE t_ds_process_task_relation
(
    id                         int(11) NOT NULL AUTO_INCREMENT,
    name                       varchar(200) DEFAULT NULL,
    process_definition_version int(11) DEFAULT NULL,
    project_code               bigint(20) NOT NULL,
    process_definition_code    bigint(20) NOT NULL,
    pre_task_code              bigint(20) NOT NULL,
    pre_task_version           int(11) NOT NULL,
    post_task_code             bigint(20) NOT NULL,
    post_task_version          int(11) NOT NULL,
    condition_type             tinyint(2) DEFAULT NULL,
    condition_params           text,
    create_time                datetime NOT NULL,
    update_time                datetime     DEFAULT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for t_ds_process_task_relation_log
-- ----------------------------
DROP TABLE IF EXISTS t_ds_process_task_relation_log;
CREATE TABLE t_ds_process_task_relation_log
(
    id                         int(11) NOT NULL AUTO_INCREMENT,
    name                       varchar(200) DEFAULT NULL,
    process_definition_version int(11) DEFAULT NULL,
    project_code               bigint(20) NOT NULL,
    process_definition_code    bigint(20) NOT NULL,
    pre_task_code              bigint(20) NOT NULL,
    pre_task_version           int(11) NOT NULL,
    post_task_code             bigint(20) NOT NULL,
    post_task_version          int(11) NOT NULL,
    condition_type             tinyint(2) DEFAULT NULL,
    condition_params           text,
    operator                   int(11) DEFAULT NULL,
    operate_time               datetime     DEFAULT NULL,
    create_time                datetime NOT NULL,
    update_time                datetime     DEFAULT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for t_ds_process_instance
-- ----------------------------
DROP TABLE IF EXISTS t_ds_process_instance;
CREATE TABLE t_ds_process_instance
(
    id                         int(11) NOT NULL AUTO_INCREMENT,
    name                       varchar(255) DEFAULT NULL,
    process_definition_version int(11) DEFAULT NULL,
    process_definition_code    bigint(20) not NULL,
    state                      tinyint(4) DEFAULT NULL,
    recovery                   tinyint(4) DEFAULT NULL,
    start_time                 datetime     DEFAULT NULL,
    end_time                   datetime     DEFAULT NULL,
    run_times                  int(11) DEFAULT NULL,
    host                       varchar(135) DEFAULT NULL,
    command_type               tinyint(4) DEFAULT NULL,
    command_param              text,
    task_depend_type           tinyint(4) DEFAULT NULL,
    max_try_times              tinyint(4) DEFAULT '0',
    failure_strategy           tinyint(4) DEFAULT '0',
    warning_type               tinyint(4) DEFAULT '0',
    warning_group_id           int(11) DEFAULT NULL,
    schedule_time              datetime     DEFAULT NULL,
    command_start_time         datetime     DEFAULT NULL,
    global_params              text,
    flag                       tinyint(4) DEFAULT '1',
    update_time                timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_sub_process             int(11) DEFAULT '0',
    executor_id                int(11) NOT NULL,
    history_cmd                text,
    process_instance_priority  int(11) DEFAULT NULL,
    worker_group               varchar(64)  DEFAULT NULL,
    environment_code           bigint(20) DEFAULT '-1',
    timeout                    int(11) DEFAULT '0',
    tenant_id                  int(11) NOT NULL DEFAULT '-1',
    var_pool                   longtext,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Records of t_ds_process_instance
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_project
-- ----------------------------
DROP TABLE IF EXISTS t_ds_project;
CREATE TABLE t_ds_project
(
    id          int(11) NOT NULL AUTO_INCREMENT,
    name        varchar(100) DEFAULT NULL,
    code        bigint(20) NOT NULL,
    description varchar(200) DEFAULT NULL,
    user_id     int(11) DEFAULT NULL,
    flag        tinyint(4) DEFAULT '1',
    create_time datetime NOT NULL,
    update_time datetime     DEFAULT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Records of t_ds_project
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_queue
-- ----------------------------
DROP TABLE IF EXISTS t_ds_queue;
CREATE TABLE t_ds_queue
(
    id          int(11) NOT NULL AUTO_INCREMENT,
    queue_name  varchar(64) DEFAULT NULL,
    queue       varchar(64) DEFAULT NULL,
    create_time datetime    DEFAULT NULL,
    update_time datetime    DEFAULT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Records of t_ds_queue
-- ----------------------------
INSERT INTO t_ds_queue
VALUES ('1', 'default', 'default', null, null);

-- ----------------------------
-- Table structure for t_ds_relation_datasource_user
-- ----------------------------
DROP TABLE IF EXISTS t_ds_relation_datasource_user;
CREATE TABLE t_ds_relation_datasource_user
(
    id            int(11) NOT NULL AUTO_INCREMENT,
    user_id       int(11) NOT NULL,
    datasource_id int(11) DEFAULT NULL,
    perm          int(11) DEFAULT '1',
    create_time   datetime DEFAULT NULL,
    update_time   datetime DEFAULT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Records of t_ds_relation_datasource_user
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_relation_process_instance
-- ----------------------------
DROP TABLE IF EXISTS t_ds_relation_process_instance;
CREATE TABLE t_ds_relation_process_instance
(
    id                         int(11) NOT NULL AUTO_INCREMENT,
    parent_process_instance_id int(11) DEFAULT NULL,
    parent_task_instance_id    int(11) DEFAULT NULL,
    process_instance_id        int(11) DEFAULT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Records of t_ds_relation_process_instance
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_relation_project_user
-- ----------------------------
DROP TABLE IF EXISTS t_ds_relation_project_user;
CREATE TABLE t_ds_relation_project_user
(
    id          int(11) NOT NULL AUTO_INCREMENT,
    user_id     int(11) NOT NULL,
    project_id  int(11) DEFAULT NULL,
    perm        int(11) DEFAULT '1',
    create_time datetime DEFAULT NULL,
    update_time datetime DEFAULT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Records of t_ds_relation_project_user
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_relation_resources_user
-- ----------------------------
DROP TABLE IF EXISTS t_ds_relation_resources_user;
CREATE TABLE t_ds_relation_resources_user
(
    id           int(11) NOT NULL AUTO_INCREMENT,
    user_id      int(11) NOT NULL,
    resources_id int(11) DEFAULT NULL,
    perm         int(11) DEFAULT '1',
    create_time  datetime DEFAULT NULL,
    update_time  datetime DEFAULT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Records of t_ds_relation_resources_user
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_relation_udfs_user
-- ----------------------------
DROP TABLE IF EXISTS t_ds_relation_udfs_user;
CREATE TABLE t_ds_relation_udfs_user
(
    id          int(11) NOT NULL AUTO_INCREMENT,
    user_id     int(11) NOT NULL,
    udf_id      int(11) DEFAULT NULL,
    perm        int(11) DEFAULT '1',
    create_time datetime DEFAULT NULL,
    update_time datetime DEFAULT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for t_ds_resources
-- ----------------------------
DROP TABLE IF EXISTS t_ds_resources;
CREATE TABLE t_ds_resources
(
    id           int(11) NOT NULL AUTO_INCREMENT,
    alias        varchar(64)  DEFAULT NULL,
    file_name    varchar(64)  DEFAULT NULL,
    description  varchar(255) DEFAULT NULL,
    user_id      int(11) DEFAULT NULL,
    type         tinyint(4) DEFAULT NULL,
    size         bigint(20) DEFAULT NULL,
    create_time  datetime     DEFAULT NULL,
    update_time  datetime     DEFAULT NULL,
    pid          int(11) DEFAULT NULL,
    full_name    varchar(64)  DEFAULT NULL,
    is_directory tinyint(4) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY t_ds_resources_un (full_name, type)
);

-- ----------------------------
-- Records of t_ds_resources
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_schedules
-- ----------------------------
DROP TABLE IF EXISTS t_ds_schedules;
CREATE TABLE t_ds_schedules
(
    id                        int(11) NOT NULL AUTO_INCREMENT,
    process_definition_id     int(11) NOT NULL,
    start_time                datetime     NOT NULL,
    end_time                  datetime     NOT NULL,
    timezone_id               varchar(40) DEFAULT NULL,
    crontab                   varchar(255) NOT NULL,
    failure_strategy          tinyint(4) NOT NULL,
    user_id                   int(11) NOT NULL,
    release_state             tinyint(4) NOT NULL,
    warning_type              tinyint(4) NOT NULL,
    warning_group_id          int(11) DEFAULT NULL,
    process_instance_priority int(11) DEFAULT NULL,
    worker_group              varchar(64) DEFAULT '',
    environment_code          bigint(20) DEFAULT '-1',
    create_time               datetime     NOT NULL,
    update_time               datetime     NOT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Records of t_ds_schedules
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_session
-- ----------------------------
DROP TABLE IF EXISTS t_ds_session;
CREATE TABLE t_ds_session
(
    id              varchar(64) NOT NULL,
    user_id         int(11) DEFAULT NULL,
    ip              varchar(45) DEFAULT NULL,
    last_login_time datetime    DEFAULT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Records of t_ds_session
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_task_instance
-- ----------------------------
DROP TABLE IF EXISTS t_ds_task_instance;
CREATE TABLE t_ds_task_instance
(
    id                      int(11) NOT NULL AUTO_INCREMENT,
    name                    varchar(255) DEFAULT NULL,
    task_type               varchar(50) NOT NULL,
    task_code               bigint(20) NOT NULL,
    task_definition_version int(11) DEFAULT NULL,
    process_instance_id     int(11) DEFAULT NULL,
    state                   tinyint(4) DEFAULT NULL,
    submit_time             datetime     DEFAULT NULL,
    start_time              datetime     DEFAULT NULL,
    end_time                datetime     DEFAULT NULL,
    host                    varchar(135) DEFAULT NULL,
    execute_path            varchar(200) DEFAULT NULL,
    log_path                varchar(200) DEFAULT NULL,
    alert_flag              tinyint(4) DEFAULT NULL,
    retry_times             int(4) DEFAULT '0',
    pid                     int(4) DEFAULT NULL,
    app_link                text,
    task_params             text,
    flag                    tinyint(4) DEFAULT '1',
    retry_interval          int(4) DEFAULT NULL,
    max_retry_times         int(2) DEFAULT NULL,
    task_instance_priority  int(11) DEFAULT NULL,
    worker_group            varchar(64)  DEFAULT NULL,
    environment_code        bigint(20) DEFAULT '-1',
    environment_config      text DEFAULT '',
    executor_id             int(11) DEFAULT NULL,
    first_submit_time       datetime     DEFAULT NULL,
    delay_time              int(4) DEFAULT '0',
    var_pool                longtext,
    PRIMARY KEY (id),
    FOREIGN KEY (process_instance_id) REFERENCES t_ds_process_instance (id) ON DELETE CASCADE
);

-- ----------------------------
-- Records of t_ds_task_instance
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_tenant
-- ----------------------------
DROP TABLE IF EXISTS t_ds_tenant;
CREATE TABLE t_ds_tenant
(
    id          int(11) NOT NULL AUTO_INCREMENT,
    tenant_code varchar(64)  DEFAULT NULL,
    description varchar(255) DEFAULT NULL,
    queue_id    int(11) DEFAULT NULL,
    create_time datetime     DEFAULT NULL,
    update_time datetime     DEFAULT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Records of t_ds_tenant
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_udfs
-- ----------------------------
DROP TABLE IF EXISTS t_ds_udfs;
CREATE TABLE t_ds_udfs
(
    id            int(11) NOT NULL AUTO_INCREMENT,
    user_id       int(11) NOT NULL,
    func_name     varchar(100) NOT NULL,
    class_name    varchar(255) NOT NULL,
    type          tinyint(4) NOT NULL,
    arg_types     varchar(255) DEFAULT NULL,
    database      varchar(255) DEFAULT NULL,
    description   varchar(255) DEFAULT NULL,
    resource_id   int(11) NOT NULL,
    resource_name varchar(255) NOT NULL,
    create_time   datetime     NOT NULL,
    update_time   datetime     NOT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Records of t_ds_udfs
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_user
-- ----------------------------
DROP TABLE IF EXISTS t_ds_user;
CREATE TABLE t_ds_user
(
    id            int(11) NOT NULL AUTO_INCREMENT,
    user_name     varchar(64) DEFAULT NULL,
    user_password varchar(64) DEFAULT NULL,
    user_type     tinyint(4) DEFAULT NULL,
    email         varchar(64) DEFAULT NULL,
    phone         varchar(11) DEFAULT NULL,
    tenant_id     int(11) DEFAULT NULL,
    create_time   datetime    DEFAULT NULL,
    update_time   datetime    DEFAULT NULL,
    queue         varchar(64) DEFAULT NULL,
    state         int(1) DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY user_name_unique (user_name)
);

-- ----------------------------
-- Records of t_ds_user
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_worker_group
-- ----------------------------
DROP TABLE IF EXISTS t_ds_worker_group;
CREATE TABLE t_ds_worker_group
(
    id          bigint(11) NOT NULL AUTO_INCREMENT,
    name        varchar(255) NOT NULL,
    addr_list   text NULL DEFAULT NULL,
    create_time datetime NULL DEFAULT NULL,
    update_time datetime NULL DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY name_unique (name)
);

-- ----------------------------
-- Records of t_ds_worker_group
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_version
-- ----------------------------
DROP TABLE IF EXISTS t_ds_version;
CREATE TABLE t_ds_version
(
    id      int(11) NOT NULL AUTO_INCREMENT,
    version varchar(200) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY version_UNIQUE (version)
);

-- ----------------------------
-- Records of t_ds_version
-- ----------------------------
INSERT INTO t_ds_version
VALUES ('1', '1.4.0');


-- ----------------------------
-- Records of t_ds_alertgroup
-- ----------------------------
INSERT INTO t_ds_alertgroup(alert_instance_ids, create_user_id, group_name, description, create_time, update_time)
VALUES ('1,2', 1, 'default admin warning group', 'default admin warning group', '2018-11-29 10:20:39',
        '2018-11-29 10:20:39');

-- ----------------------------
-- Records of t_ds_user
-- ----------------------------
INSERT INTO t_ds_user
VALUES ('1', 'admin', '7ad2410b2f4c074479a8937a28a22b8f', '0', 'xxx@qq.com', '', '0', '2018-03-27 15:48:50',
        '2018-10-24 17:40:22', null, 1);

-- ----------------------------
-- Table structure for t_ds_plugin_define
-- ----------------------------
DROP TABLE IF EXISTS t_ds_plugin_define;
CREATE TABLE t_ds_plugin_define
(
    id            int          NOT NULL AUTO_INCREMENT,
    plugin_name   varchar(100) NOT NULL,
    plugin_type   varchar(100) NOT NULL,
    plugin_params text,
    create_time   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY t_ds_plugin_define_UN (plugin_name,plugin_type)
);

-- ----------------------------
-- Table structure for t_ds_alert_plugin_instance
-- ----------------------------
DROP TABLE IF EXISTS t_ds_alert_plugin_instance;
CREATE TABLE t_ds_alert_plugin_instance
(
    id                     int NOT NULL AUTO_INCREMENT,
    plugin_define_id       int NOT NULL,
    plugin_instance_params text,
    create_time            timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    update_time            timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    instance_name          varchar(200) DEFAULT NULL,
    PRIMARY KEY (id)
);

--
-- Table structure for table t_ds_environment
--
DROP TABLE IF EXISTS t_ds_environment;
CREATE TABLE t_ds_environment
(
  id            int NOT NULL AUTO_INCREMENT,
  code          bigint(20) NOT NULL,
  name          varchar(100) DEFAULT NULL,
  config        text DEFAULT NULL,
  description   text,
  operator      int DEFAULT NULL,
  create_time   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY environment_name_unique (name),
  UNIQUE KEY environment_code_unique (code)
);

--
-- Table structure for table t_ds_environment_worker_group_relation
--
DROP TABLE IF EXISTS t_ds_environment_worker_group_relation;
CREATE TABLE t_ds_environment_worker_group_relation
(
    id                  int NOT NULL AUTO_INCREMENT,
    environment_code    bigint(20) NOT NULL,
    worker_group        varchar(255) NOT NULL,
    operator            int DEFAULT NULL,
    create_time         timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id) ,
    UNIQUE KEY environment_worker_group_unique (environment_code,worker_group)
);
