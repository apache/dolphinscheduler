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

SET FOREIGN_KEY_CHECKS=0;
SET REFERENTIAL_INTEGRITY FALSE;

-- ----------------------------
-- Table structure for QRTZ_JOB_DETAILS
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_JOB_DETAILS CASCADE;
CREATE TABLE QRTZ_JOB_DETAILS
(
    SCHED_NAME        varchar(120) NOT NULL,
    JOB_NAME          varchar(200) NOT NULL,
    JOB_GROUP         varchar(200) NOT NULL,
    DESCRIPTION       varchar(250) DEFAULT NULL,
    JOB_CLASS_NAME    varchar(250) NOT NULL,
    IS_DURABLE        boolean      NOT NULL,
    IS_NONCONCURRENT  boolean      NOT NULL,
    IS_UPDATE_DATA    boolean      NOT NULL,
    REQUESTS_RECOVERY boolean      NOT NULL,
    JOB_DATA          blob,
    PRIMARY KEY (SCHED_NAME, JOB_NAME, JOB_GROUP)
);

-- ----------------------------
-- Table structure for QRTZ_TRIGGERS
-- ----------------------------
DROP TABLE IF EXISTS QRTZ_TRIGGERS CASCADE;
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
DROP TABLE IF EXISTS QRTZ_BLOB_TRIGGERS CASCADE;
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
DROP TABLE IF EXISTS QRTZ_CALENDARS CASCADE;
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
DROP TABLE IF EXISTS QRTZ_CRON_TRIGGERS CASCADE;
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
DROP TABLE IF EXISTS QRTZ_FIRED_TRIGGERS CASCADE;
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
    IS_NONCONCURRENT  boolean      DEFAULT NULL,
    REQUESTS_RECOVERY boolean      DEFAULT NULL,
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
DROP TABLE IF EXISTS QRTZ_LOCKS CASCADE;
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
DROP TABLE IF EXISTS QRTZ_PAUSED_TRIGGER_GRPS CASCADE;
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
DROP TABLE IF EXISTS QRTZ_SCHEDULER_STATE CASCADE;
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
DROP TABLE IF EXISTS QRTZ_SIMPLE_TRIGGERS CASCADE;
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
DROP TABLE IF EXISTS QRTZ_SIMPROP_TRIGGERS CASCADE;
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
    BOOL_PROP_1   boolean        DEFAULT NULL,
    BOOL_PROP_2   boolean        DEFAULT NULL,
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
DROP TABLE IF EXISTS t_ds_access_token CASCADE;
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
DROP TABLE IF EXISTS t_ds_alert CASCADE;
CREATE TABLE t_ds_alert
(
    id            int(11) NOT NULL AUTO_INCREMENT,
    title         varchar(64) DEFAULT NULL,
    sign           char(40) NOT NULL DEFAULT '',
    content       text,
    alert_status  tinyint(4) DEFAULT '0',
    warning_type  tinyint(4) DEFAULT '2',
    log           text,
    alertgroup_id int(11) DEFAULT NULL,
    create_time   datetime    DEFAULT NULL,
    update_time   datetime    DEFAULT NULL,
    project_code        bigint(20) DEFAULT NULL,
    process_definition_code        bigint(20) DEFAULT NULL,
    process_instance_id     int(11) DEFAULT NULL,
    alert_type     int(11) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY            idx_sign (sign)
);

-- ----------------------------
-- Records of t_ds_alert
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_alertgroup
-- ----------------------------
DROP TABLE IF EXISTS t_ds_alertgroup CASCADE;
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
DROP TABLE IF EXISTS t_ds_command CASCADE;
CREATE TABLE t_ds_command
(
    id                         int(11) NOT NULL AUTO_INCREMENT,
    command_type               tinyint(4) DEFAULT NULL,
    process_definition_code    bigint(20) DEFAULT NULL,
    command_param              text,
    task_depend_type           tinyint(4) DEFAULT NULL,
    failure_strategy           tinyint(4) DEFAULT '0',
    warning_type               tinyint(4) DEFAULT '0',
    warning_group_id           int(11) DEFAULT NULL,
    schedule_time              datetime DEFAULT NULL,
    start_time                 datetime DEFAULT NULL,
    executor_id                int(11) DEFAULT NULL,
    update_time                datetime DEFAULT NULL,
    process_instance_priority  int(11) DEFAULT '2',
    worker_group               varchar(64),
    environment_code           bigint(20) DEFAULT '-1',
    dry_run                    int NULL DEFAULT 0,
    process_instance_id        int(11) DEFAULT 0,
    process_definition_version int(11) DEFAULT 0,
    PRIMARY KEY (id),
    KEY                        priority_id_index (process_instance_priority, id)
);

-- ----------------------------
-- Records of t_ds_command
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_datasource
-- ----------------------------
DROP TABLE IF EXISTS t_ds_datasource CASCADE;
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
DROP TABLE IF EXISTS t_ds_error_command CASCADE;
CREATE TABLE t_ds_error_command
(
    id                         int(11) NOT NULL,
    command_type               tinyint(4) DEFAULT NULL,
    executor_id                int(11) DEFAULT NULL,
    process_definition_code    bigint(20) DEFAULT NULL,
    command_param              text,
    task_depend_type           tinyint(4) DEFAULT NULL,
    failure_strategy           tinyint(4) DEFAULT '0',
    warning_type               tinyint(4) DEFAULT '0',
    warning_group_id           int(11) DEFAULT NULL,
    schedule_time              datetime DEFAULT NULL,
    start_time                 datetime DEFAULT NULL,
    update_time                datetime DEFAULT NULL,
    process_instance_priority  int(11) DEFAULT '2',
    worker_group               varchar(64),
    environment_code           bigint(20) DEFAULT '-1',
    message                    text,
    dry_run                    int NULL DEFAULT 0,
    process_instance_id        int(11) DEFAULT 0,
    process_definition_version int(11) DEFAULT 0,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Records of t_ds_error_command
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_process_definition
-- ----------------------------
DROP TABLE IF EXISTS t_ds_process_definition CASCADE;
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
    execution_type   tinyint(4) DEFAULT '0',
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
DROP TABLE IF EXISTS t_ds_process_definition_log CASCADE;
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
    execution_type   tinyint(4) DEFAULT '0',
    operator         int(11) DEFAULT NULL,
    operate_time     datetime     DEFAULT NULL,
    create_time      datetime NOT NULL,
    update_time      datetime     DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uniq_idx_code_version (code, version) USING BTREE
);

-- ----------------------------
-- Table structure for t_ds_task_definition
-- ----------------------------
DROP TABLE IF EXISTS t_ds_task_definition CASCADE;
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
    task_execute_type       int(11) DEFAULT '0',
    task_params             longtext,
    flag                    tinyint(2) DEFAULT NULL,
    task_priority           tinyint(4) DEFAULT '2',
    worker_group            varchar(200) DEFAULT NULL,
    environment_code        bigint(20) DEFAULT '-1',
    fail_retry_times        int(11) DEFAULT NULL,
    fail_retry_interval     int(11) DEFAULT NULL,
    timeout_flag            tinyint(2) DEFAULT '0',
    timeout_notify_strategy tinyint(4) DEFAULT NULL,
    timeout                 int(11) DEFAULT '0',
    delay_time              int(11) DEFAULT '0',
    task_group_id           int(11) DEFAULT NULL,
    task_group_priority     tinyint(4) DEFAULT '0',
    cpu_quota               int(11) DEFAULT '-1' NOT NULL,
    memory_max              int(11) DEFAULT '-1' NOT NULL,
    resource_ids            text,
    create_time             datetime    NOT NULL,
    update_time             datetime     DEFAULT NULL,
    PRIMARY KEY (id, code)
);

-- ----------------------------
-- Table structure for t_ds_task_definition_log
-- ----------------------------
DROP TABLE IF EXISTS t_ds_task_definition_log CASCADE;
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
    task_execute_type       int(11) DEFAULT '0',
    task_params             text,
    flag                    tinyint(2) DEFAULT NULL,
    task_priority           tinyint(4) DEFAULT '2',
    worker_group            varchar(200) DEFAULT NULL,
    environment_code        bigint(20) DEFAULT '-1',
    fail_retry_times        int(11) DEFAULT NULL,
    fail_retry_interval     int(11) DEFAULT NULL,
    timeout_flag            tinyint(2) DEFAULT '0',
    timeout_notify_strategy tinyint(4) DEFAULT NULL,
    timeout                 int(11) DEFAULT '0',
    delay_time              int(11) DEFAULT '0',
    resource_ids            text,
    operator                int(11) DEFAULT NULL,
    task_group_id           int(11) DEFAULT NULL,
    task_group_priority     tinyint(4) DEFAULT '0',
    cpu_quota               int(11) DEFAULT '-1' NOT NULL,
    memory_max              int(11) DEFAULT '-1' NOT NULL,
    operate_time            datetime     DEFAULT NULL,
    create_time             datetime    NOT NULL,
    update_time             datetime     DEFAULT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for t_ds_process_task_relation
-- ----------------------------
DROP TABLE IF EXISTS t_ds_process_task_relation CASCADE;
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
DROP TABLE IF EXISTS t_ds_process_task_relation_log CASCADE;
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
DROP TABLE IF EXISTS t_ds_process_instance CASCADE;
CREATE TABLE t_ds_process_instance
(
    id                         int(11) NOT NULL AUTO_INCREMENT,
    name                       varchar(255) DEFAULT NULL,
    process_definition_version int(11) DEFAULT NULL,
    process_definition_code    bigint(20) not NULL,
    state                      tinyint(4) DEFAULT NULL,
    state_history              text,
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
    process_instance_priority  int(11) DEFAULT '2',
    worker_group               varchar(64)  DEFAULT NULL,
    environment_code           bigint(20) DEFAULT '-1',
    timeout                    int(11) DEFAULT '0',
    next_process_instance_id   int(11) DEFAULT '0',
    tenant_id                  int(11) NOT NULL DEFAULT '-1',
    var_pool                   longtext,
    dry_run                    int NULL DEFAULT 0,
    restart_time               datetime     DEFAULT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Records of t_ds_process_instance
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_project
-- ----------------------------
DROP TABLE IF EXISTS t_ds_project CASCADE;
CREATE TABLE t_ds_project
(
    id          int(11) NOT NULL AUTO_INCREMENT,
    name        varchar(100) DEFAULT NULL,
    code        bigint(20) NOT NULL,
    description varchar(255) DEFAULT NULL,
    user_id     int(11) DEFAULT NULL,
    flag        tinyint(4) DEFAULT '1',
    create_time datetime NOT NULL,
    update_time datetime     DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY unique_name (name),
    UNIQUE KEY unique_code (code)
);

-- ----------------------------
-- Records of t_ds_project
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_queue
-- ----------------------------
DROP TABLE IF EXISTS t_ds_queue CASCADE;
CREATE TABLE t_ds_queue
(
    id          int(11) NOT NULL AUTO_INCREMENT,
    queue_name  varchar(64) DEFAULT NULL,
    queue       varchar(64) DEFAULT NULL,
    create_time datetime    DEFAULT NULL,
    update_time datetime    DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY unique_queue_name (queue_name)
);

-- ----------------------------
-- Records of t_ds_queue
-- ----------------------------
INSERT INTO t_ds_queue
VALUES ('1', 'default', 'default', null, null);

-- ----------------------------
-- Table structure for t_ds_relation_datasource_user
-- ----------------------------
DROP TABLE IF EXISTS t_ds_relation_datasource_user CASCADE;
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
DROP TABLE IF EXISTS t_ds_relation_process_instance CASCADE;
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
DROP TABLE IF EXISTS t_ds_relation_project_user CASCADE;
CREATE TABLE t_ds_relation_project_user
(
    id          int(11) NOT NULL AUTO_INCREMENT,
    user_id     int(11) NOT NULL,
    project_id  int(11) DEFAULT NULL,
    perm        int(11) DEFAULT '1',
    create_time datetime DEFAULT NULL,
    update_time datetime DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uniq_uid_pid(user_id,project_id)
);

-- ----------------------------
-- Records of t_ds_relation_project_user
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_relation_resources_user
-- ----------------------------
DROP TABLE IF EXISTS t_ds_relation_resources_user CASCADE;
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
DROP TABLE IF EXISTS t_ds_relation_udfs_user CASCADE;
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
DROP TABLE IF EXISTS t_ds_resources CASCADE;
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
    full_name    varchar(128)  DEFAULT NULL,
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
DROP TABLE IF EXISTS t_ds_schedules CASCADE;
CREATE TABLE t_ds_schedules
(
    id                        int(11) NOT NULL AUTO_INCREMENT,
    process_definition_code   bigint(20) NOT NULL,
    start_time                datetime     NOT NULL,
    end_time                  datetime     NOT NULL,
    timezone_id               varchar(40) DEFAULT NULL,
    crontab                   varchar(255) NOT NULL,
    failure_strategy          tinyint(4) NOT NULL,
    user_id                   int(11) NOT NULL,
    release_state             tinyint(4) NOT NULL,
    warning_type              tinyint(4) NOT NULL,
    warning_group_id          int(11) DEFAULT NULL,
    process_instance_priority int(11) DEFAULT '2',
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
DROP TABLE IF EXISTS t_ds_session CASCADE;
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
DROP TABLE IF EXISTS t_ds_task_instance CASCADE;
CREATE TABLE t_ds_task_instance
(
    id                      int(11) NOT NULL AUTO_INCREMENT,
    name                    varchar(255) DEFAULT NULL,
    task_type               varchar(50) NOT NULL,
    task_execute_type       int(11) DEFAULT '0',
    task_code               bigint(20) NOT NULL,
    task_definition_version int(11) DEFAULT NULL,
    process_instance_id     int(11) DEFAULT NULL,
    state                   tinyint(4) DEFAULT NULL,
    submit_time             datetime     DEFAULT NULL,
    start_time              datetime     DEFAULT NULL,
    end_time                datetime     DEFAULT NULL,
    host                    varchar(135) DEFAULT NULL,
    execute_path            varchar(200) DEFAULT NULL,
    log_path                longtext DEFAULT NULL,
    alert_flag              tinyint(4) DEFAULT NULL,
    retry_times             int(4) DEFAULT '0',
    pid                     int(4) DEFAULT NULL,
    app_link                text,
    task_params             longtext,
    flag                    tinyint(4) DEFAULT '1',
    retry_interval          int(4) DEFAULT NULL,
    max_retry_times         int(2) DEFAULT NULL,
    task_instance_priority  int(11) DEFAULT NULL,
    worker_group            varchar(64)  DEFAULT NULL,
    environment_code        bigint(20) DEFAULT '-1',
    environment_config      text         DEFAULT '',
    executor_id             int(11) DEFAULT NULL,
    first_submit_time       datetime     DEFAULT NULL,
    delay_time              int(4) DEFAULT '0',
    task_group_id           int(11) DEFAULT NULL,
    var_pool                longtext,
    dry_run                 int NULL DEFAULT 0,
    cpu_quota               int(11) DEFAULT '-1' NOT NULL,
    memory_max              int(11) DEFAULT '-1' NOT NULL,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Records of t_ds_task_instance
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_tenant
-- ----------------------------
DROP TABLE IF EXISTS t_ds_tenant CASCADE;
CREATE TABLE t_ds_tenant
(
    id          int(11) NOT NULL AUTO_INCREMENT,
    tenant_code varchar(64)  DEFAULT NULL,
    description varchar(255) DEFAULT NULL,
    queue_id    int(11)      DEFAULT NULL,
    create_time datetime     DEFAULT NULL,
    update_time datetime     DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY unique_tenant_code (tenant_code)
);

-- ----------------------------
-- Records of t_ds_tenant
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_udfs
-- ----------------------------
DROP TABLE IF EXISTS t_ds_udfs CASCADE;
CREATE TABLE t_ds_udfs
(
    id            int(11)      NOT NULL AUTO_INCREMENT,
    user_id       int(11)      NOT NULL,
    func_name     varchar(100) NOT NULL,
    class_name    varchar(255) NOT NULL,
    type          tinyint(4)   NOT NULL,
    arg_types     varchar(255) DEFAULT NULL,
    database      varchar(255) DEFAULT NULL,
    description   varchar(255) DEFAULT NULL,
    resource_id   int(11)      NOT NULL,
    resource_name varchar(255) NOT NULL,
    create_time   datetime     NOT NULL,
    update_time   datetime     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY unique_func_name (func_name)
);

-- ----------------------------
-- Records of t_ds_udfs
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_user
-- ----------------------------
DROP TABLE IF EXISTS t_ds_user CASCADE;
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
    time_zone     varchar(32) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY user_name_unique (user_name)
);

-- ----------------------------
-- Records of t_ds_user
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_worker_group
-- ----------------------------
DROP TABLE IF EXISTS t_ds_worker_group CASCADE;
CREATE TABLE t_ds_worker_group
(
    id          bigint(11) NOT NULL AUTO_INCREMENT,
    name        varchar(255) NOT NULL,
    addr_list   text NULL DEFAULT NULL,
    create_time datetime NULL DEFAULT NULL,
    update_time datetime NULL DEFAULT NULL,
    description text NULL DEFAULT NULL,
    other_params_json text NULL DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY name_unique (name)
);

-- ----------------------------
-- Records of t_ds_worker_group
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_version
-- ----------------------------
DROP TABLE IF EXISTS t_ds_version CASCADE;
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
VALUES ('1', '3.1.5');


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
        '2018-10-24 17:40:22', null, 1, null);

-- ----------------------------
-- Table structure for t_ds_plugin_define
-- ----------------------------
DROP TABLE IF EXISTS t_ds_plugin_define CASCADE;
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
DROP TABLE IF EXISTS t_ds_alert_plugin_instance CASCADE;
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
-- Table structure for table `t_ds_dq_comparison_type`
--
DROP TABLE IF EXISTS `t_ds_dq_comparison_type`;
CREATE TABLE `t_ds_dq_comparison_type` (
                                           `id` int(11) NOT NULL AUTO_INCREMENT,
                                           `type` varchar(100) NOT NULL,
                                           `execute_sql` text DEFAULT NULL,
                                           `output_table` varchar(100) DEFAULT NULL,
                                           `name` varchar(100) DEFAULT NULL,
                                           `create_time` datetime DEFAULT NULL,
                                           `update_time` datetime DEFAULT NULL,
                                           `is_inner_source` tinyint(1) DEFAULT '0',
                                           PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(1, 'FixValue', NULL, NULL, NULL, '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', false);
INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(2, 'DailyAvg', 'select round(avg(statistics_value),2) as day_avg from t_ds_dq_task_statistics_value where data_time >=date_trunc(''DAY'', ${data_time}) and data_time < date_add(date_trunc(''day'', ${data_time}),1) and unique_code = ${unique_code} and statistics_name = ''${statistics_name}''', 'day_range', 'day_range.day_avg', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', true);
INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(3, 'WeeklyAvg', 'select round(avg(statistics_value),2) as week_avg from t_ds_dq_task_statistics_value where  data_time >= date_trunc(''WEEK'', ${data_time}) and data_time <date_trunc(''day'', ${data_time}) and unique_code = ${unique_code} and statistics_name = ''${statistics_name}''', 'week_range', 'week_range.week_avg', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', true);
INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(4, 'MonthlyAvg', 'select round(avg(statistics_value),2) as month_avg from t_ds_dq_task_statistics_value where  data_time >= date_trunc(''MONTH'', ${data_time}) and data_time <date_trunc(''day'', ${data_time}) and unique_code = ${unique_code} and statistics_name = ''${statistics_name}''', 'month_range', 'month_range.month_avg', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', true);
INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(5, 'Last7DayAvg', 'select round(avg(statistics_value),2) as last_7_avg from t_ds_dq_task_statistics_value where  data_time >= date_add(date_trunc(''day'', ${data_time}),-7) and  data_time <date_trunc(''day'', ${data_time}) and unique_code = ${unique_code} and statistics_name = ''${statistics_name}''', 'last_seven_days', 'last_seven_days.last_7_avg', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', true);
INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(6, 'Last30DayAvg', 'select round(avg(statistics_value),2) as last_30_avg from t_ds_dq_task_statistics_value where  data_time >= date_add(date_trunc(''day'', ${data_time}),-30) and  data_time < date_trunc(''day'', ${data_time}) and unique_code = ${unique_code} and statistics_name = ''${statistics_name}''', 'last_thirty_days', 'last_thirty_days.last_30_avg', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', true);
INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(7, 'SrcTableTotalRows', 'SELECT COUNT(*) AS total FROM ${src_table} WHERE (${src_filter})', 'total_count', 'total_count.total', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', false);
INSERT INTO `t_ds_dq_comparison_type`
(`id`, `type`, `execute_sql`, `output_table`, `name`, `create_time`, `update_time`, `is_inner_source`)
VALUES(8, 'TargetTableTotalRows', 'SELECT COUNT(*) AS total FROM ${target_table} WHERE (${target_filter})', 'total_count', 'total_count.total', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', false);

--
-- Table structure for table `t_ds_dq_execute_result`
--
DROP TABLE IF EXISTS `t_ds_dq_execute_result`;
CREATE TABLE `t_ds_dq_execute_result` (
                                          `id` int(11) NOT NULL AUTO_INCREMENT,
                                          `process_definition_id` int(11) DEFAULT NULL,
                                          `process_instance_id` int(11) DEFAULT NULL,
                                          `task_instance_id` int(11) DEFAULT NULL,
                                          `rule_type` int(11) DEFAULT NULL,
                                          `rule_name` varchar(255) DEFAULT NULL,
                                          `statistics_value` double DEFAULT NULL,
                                          `comparison_value` double DEFAULT NULL,
                                          `check_type` int(11) DEFAULT NULL,
                                          `threshold` double DEFAULT NULL,
                                          `operator` int(11) DEFAULT NULL,
                                          `failure_strategy` int(11) DEFAULT NULL,
                                          `state` int(11) DEFAULT NULL,
                                          `user_id` int(11) DEFAULT NULL,
                                          `comparison_type` int(11) DEFAULT NULL,
                                          `error_output_path` text DEFAULT NULL,
                                          `create_time` datetime DEFAULT NULL,
                                          `update_time` datetime DEFAULT NULL,
                                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table t_ds_dq_rule
--
DROP TABLE IF EXISTS `t_ds_dq_rule`;
CREATE TABLE `t_ds_dq_rule` (
                                `id` int(11) NOT NULL AUTO_INCREMENT,
                                `name` varchar(100) DEFAULT NULL,
                                `type` int(11) DEFAULT NULL,
                                `user_id` int(11) DEFAULT NULL,
                                `create_time` datetime DEFAULT NULL,
                                `update_time` datetime DEFAULT NULL,
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(1, '$t(null_check)', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(2, '$t(custom_sql)', 1, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(3, '$t(multi_table_accuracy)', 2, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(4, '$t(multi_table_value_comparison)', 3, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(5, '$t(field_length_check)', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(6, '$t(uniqueness_check)', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(7, '$t(regexp_check)', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(8, '$t(timeliness_check)', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(9, '$t(enumeration_check)', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO `t_ds_dq_rule`
(`id`, `name`, `type`, `user_id`, `create_time`, `update_time`)
VALUES(10, '$t(table_count_check)', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');

--
-- Table structure for table `t_ds_dq_rule_execute_sql`
--
DROP TABLE IF EXISTS `t_ds_dq_rule_execute_sql`;
CREATE TABLE `t_ds_dq_rule_execute_sql` (
                                            `id` int(11) NOT NULL AUTO_INCREMENT,
                                            `index` int(11) DEFAULT NULL,
                                            `sql` text DEFAULT NULL,
                                            `table_alias` varchar(255) DEFAULT NULL,
                                            `type` int(11) DEFAULT NULL,
                                            `is_error_output_sql` tinyint(1) DEFAULT '0',
                                            `create_time` datetime DEFAULT NULL,
                                            `update_time` datetime DEFAULT NULL,
                                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(1, 1, 'SELECT COUNT(*) AS nulls FROM null_items', 'null_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(2, 1, 'SELECT COUNT(*) AS total FROM ${src_table} WHERE (${src_filter})', 'total_count', 2, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(3, 1, 'SELECT COUNT(*) AS miss from miss_items', 'miss_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(4, 1, 'SELECT COUNT(*) AS valids FROM invalid_length_items', 'invalid_length_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(5, 1, 'SELECT COUNT(*) AS total FROM ${target_table} WHERE (${target_filter})', 'total_count', 2, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(6, 1, 'SELECT ${src_field} FROM ${src_table} group by ${src_field} having count(*) > 1', 'duplicate_items', 0, true, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(7, 1, 'SELECT COUNT(*) AS duplicates FROM duplicate_items', 'duplicate_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(8, 1, 'SELECT ${src_table}.* FROM (SELECT * FROM ${src_table} WHERE (${src_filter})) ${src_table} LEFT JOIN (SELECT * FROM ${target_table} WHERE (${target_filter})) ${target_table} ON ${on_clause} WHERE ${where_clause}', 'miss_items', 0, true, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(9, 1, 'SELECT * FROM ${src_table} WHERE (${src_field} not regexp ''${regexp_pattern}'') AND (${src_filter}) ', 'regexp_items', 0, true, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(10, 1, 'SELECT COUNT(*) AS regexps FROM regexp_items', 'regexp_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(11, 1, 'SELECT * FROM ${src_table} WHERE (to_unix_timestamp(${src_field}, ''${datetime_format}'')-to_unix_timestamp(''${deadline}'', ''${datetime_format}'') <= 0) AND (to_unix_timestamp(${src_field}, ''${datetime_format}'')-to_unix_timestamp(''${begin_time}'', ''${datetime_format}'') >= 0) AND (${src_filter}) ', 'timeliness_items', 0, 1, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(12, 1, 'SELECT COUNT(*) AS timeliness FROM timeliness_items', 'timeliness_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(13, 1, 'SELECT * FROM ${src_table} where (${src_field} not in ( ${enum_list} ) or ${src_field} is null) AND (${src_filter}) ', 'enum_items', 0, true, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(14, 1, 'SELECT COUNT(*) AS enums FROM enum_items', 'enum_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(15, 1, 'SELECT COUNT(*) AS total FROM ${src_table} WHERE (${src_filter})', 'table_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(16, 1, 'SELECT * FROM ${src_table} WHERE (${src_field} is null or ${src_field} = '''') AND (${src_filter})', 'null_items', 0, true, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_execute_sql`
(`id`, `index`, `sql`, `table_alias`, `type`, `is_error_output_sql`, `create_time`, `update_time`)
VALUES(17, 1, 'SELECT * FROM ${src_table} WHERE (length(${src_field}) ${logic_operator} ${field_length}) AND (${src_filter})', 'invalid_length_items', 0, true, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');

--
-- Table structure for table `t_ds_dq_rule_input_entry`
--
DROP TABLE IF EXISTS `t_ds_dq_rule_input_entry`;
CREATE TABLE `t_ds_dq_rule_input_entry` (
                                            `id` int(11) NOT NULL AUTO_INCREMENT,
                                            `field` varchar(255) DEFAULT NULL,
                                            `type` varchar(255) DEFAULT NULL,
                                            `title` varchar(255) DEFAULT NULL,
                                            `value` varchar(255)  DEFAULT NULL,
                                            `options` text DEFAULT NULL,
                                            `placeholder` varchar(255) DEFAULT NULL,
                                            `option_source_type` int(11) DEFAULT NULL,
                                            `value_type` int(11) DEFAULT NULL,
                                            `input_type` int(11) DEFAULT NULL,
                                            `is_show` tinyint(1) DEFAULT '1',
                                            `can_edit` tinyint(1) DEFAULT '1',
                                            `is_emit` tinyint(1) DEFAULT '0',
                                            `is_validate` tinyint(1) DEFAULT '1',
                                            `create_time` datetime DEFAULT NULL,
                                            `update_time` datetime DEFAULT NULL,
                                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(1, 'src_connector_type', 'select', '$t(src_connector_type)', '', '[{"label":"HIVE","value":"HIVE"},{"label":"JDBC","value":"JDBC"}]', 'please select source connector type', 2, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(2, 'src_datasource_id', 'select', '$t(src_datasource_id)', '', NULL, 'please select source datasource id', 1, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(3, 'src_table', 'select', '$t(src_table)', NULL, NULL, 'Please enter source table name', 0, 0, 0, 1, 1, 1, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(4, 'src_filter', 'input', '$t(src_filter)', NULL, NULL, 'Please enter filter expression', 0, 3, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(5, 'src_field', 'select', '$t(src_field)', NULL, NULL, 'Please enter column, only single column is supported', 0, 0, 0, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(6, 'statistics_name', 'input', '$t(statistics_name)', NULL, NULL, 'Please enter statistics name, the alias in statistics execute sql', 0, 0, 1, 0, 0, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(7, 'check_type', 'select', '$t(check_type)', '0', '[{"label":"Expected - Actual","value":"0"},{"label":"Actual - Expected","value":"1"},{"label":"Actual / Expected","value":"2"},{"label":"(Expected - Actual) / Expected","value":"3"}]', 'please select check type', 0, 0, 3, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(8, 'operator', 'select', '$t(operator)', '0', '[{"label":"=","value":"0"},{"label":"<","value":"1"},{"label":"<=","value":"2"},{"label":">","value":"3"},{"label":">=","value":"4"},{"label":"!=","value":"5"}]', 'please select operator', 0, 0, 3, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(9, 'threshold', 'input', '$t(threshold)', NULL, NULL, 'Please enter threshold, number is needed', 0, 2, 3, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(10, 'failure_strategy', 'select', '$t(failure_strategy)', '0', '[{"label":"Alert","value":"0"},{"label":"Block","value":"1"}]', 'please select failure strategy', 0, 0, 3, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(11, 'target_connector_type', 'select', '$t(target_connector_type)', '', '[{"label":"HIVE","value":"HIVE"},{"label":"JDBC","value":"JDBC"}]', 'Please select target connector type', 2, 0, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(12, 'target_datasource_id', 'select', '$t(target_datasource_id)', '', NULL, 'Please select target datasource', 1, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(13, 'target_table', 'select', '$t(target_table)', NULL, NULL, 'Please enter target table', 0, 0, 0, 1, 1, 1, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(14, 'target_filter', 'input', '$t(target_filter)', NULL, NULL, 'Please enter target filter expression', 0, 3, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(15, 'mapping_columns', 'group', '$t(mapping_columns)', NULL, '[{"field":"src_field","props":{"placeholder":"Please input src field","rows":0,"disabled":false,"size":"small"},"type":"input","title":"src_field"},{"field":"operator","props":{"placeholder":"Please input operator","rows":0,"disabled":false,"size":"small"},"type":"input","title":"operator"},{"field":"target_field","props":{"placeholder":"Please input target field","rows":0,"disabled":false,"size":"small"},"type":"input","title":"target_field"}]', 'please enter mapping columns', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(16, 'statistics_execute_sql', 'textarea', '$t(statistics_execute_sql)', NULL, NULL, 'Please enter statistics execute sql', 0, 3, 0, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(17, 'comparison_name', 'input', '$t(comparison_name)', NULL, NULL, 'Please enter comparison name, the alias in comparison execute sql', 0, 0, 0, 0, 0, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(18, 'comparison_execute_sql', 'textarea', '$t(comparison_execute_sql)', NULL, NULL, 'Please enter comparison execute sql', 0, 3, 0, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(19, 'comparison_type', 'select', '$t(comparison_type)', '', NULL, 'Please enter comparison title', 3, 0, 2, 1, 0, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(20, 'writer_connector_type', 'select', '$t(writer_connector_type)', '', '[{"label":"MYSQL","value":"0"},{"label":"POSTGRESQL","value":"1"}]', 'please select writer connector type', 0, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(21, 'writer_datasource_id', 'select', '$t(writer_datasource_id)', '', NULL, 'please select writer datasource id', 1, 2, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(22, 'target_field', 'select', '$t(target_field)', NULL, NULL, 'Please enter column, only single column is supported', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(23, 'field_length', 'input', '$t(field_length)', NULL, NULL, 'Please enter length limit', 0, 3, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(24, 'logic_operator', 'select', '$t(logic_operator)', '=', '[{"label":"=","value":"="},{"label":"<","value":"<"},{"label":"<=","value":"<="},{"label":">","value":">"},{"label":">=","value":">="},{"label":"<>","value":"<>"}]', 'please select logic operator', 0, 0, 3, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(25, 'regexp_pattern', 'input', '$t(regexp_pattern)', NULL, NULL, 'Please enter regexp pattern', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(26, 'deadline', 'input', '$t(deadline)', NULL, NULL, 'Please enter deadline', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(27, 'datetime_format', 'input', '$t(datetime_format)', NULL, NULL, 'Please enter datetime format', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(28, 'enum_list', 'input', '$t(enum_list)', NULL, NULL, 'Please enter enumeration', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_dq_rule_input_entry`
(`id`, `field`, `type`, `title`, `value`, `options`, `placeholder`, `option_source_type`, `value_type`, `input_type`, `is_show`, `can_edit`, `is_emit`, `is_validate`, `create_time`, `update_time`)
VALUES(29, 'begin_time', 'input', '$t(begin_time)', NULL, NULL, 'Please enter begin time', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');

--
-- Table structure for table `t_ds_dq_task_statistics_value`
--
DROP TABLE IF EXISTS `t_ds_dq_task_statistics_value`;
CREATE TABLE `t_ds_dq_task_statistics_value` (
                                                 `id` int(11) NOT NULL AUTO_INCREMENT,
                                                 `process_definition_id` int(11) DEFAULT NULL,
                                                 `task_instance_id` int(11) DEFAULT NULL,
                                                 `rule_id` int(11) NOT NULL,
                                                 `unique_code` varchar(255) NULL,
                                                 `statistics_name` varchar(255) NULL,
                                                 `statistics_value` double NULL,
                                                 `data_time` datetime DEFAULT NULL,
                                                 `create_time` datetime DEFAULT NULL,
                                                 `update_time` datetime DEFAULT NULL,
                                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `t_ds_relation_rule_execute_sql`
--
DROP TABLE IF EXISTS `t_ds_relation_rule_execute_sql`;
CREATE TABLE `t_ds_relation_rule_execute_sql` (
                                                  `id` int(11) NOT NULL AUTO_INCREMENT,
                                                  `rule_id` int(11) DEFAULT NULL,
                                                  `execute_sql_id` int(11) DEFAULT NULL,
                                                  `create_time` datetime NULL,
                                                  `update_time` datetime NULL,
                                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(1, 1, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(3, 5, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(2, 3, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(4, 3, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(5, 6, 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(6, 6, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(7, 7, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(8, 7, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(9, 8, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(10, 8, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(11, 9, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(12, 9, 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(13, 10, 15, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(14, 1, 16, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_execute_sql`
(`id`, `rule_id`, `execute_sql_id`, `create_time`, `update_time`)
VALUES(15, 5, 17, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');

--
-- Table structure for table `t_ds_relation_rule_input_entry`
--
DROP TABLE IF EXISTS `t_ds_relation_rule_input_entry`;
CREATE TABLE `t_ds_relation_rule_input_entry` (
                                                  `id` int(11) NOT NULL AUTO_INCREMENT,
                                                  `rule_id` int(11) DEFAULT NULL,
                                                  `rule_input_entry_id` int(11) DEFAULT NULL,
                                                  `values_map` text DEFAULT NULL,
                                                  `index` int(11) DEFAULT NULL,
                                                  `create_time` datetime DEFAULT NULL,
                                                  `update_time` datetime DEFAULT NULL,
                                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(1, 1, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(2, 1, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(3, 1, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(4, 1, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(5, 1, 5, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(6, 1, 6, '{"statistics_name":"null_count.nulls"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(7, 1, 7, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(8, 1, 8, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(9, 1, 9, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(10, 1, 10, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(11, 1, 17, '', 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(12, 1, 19, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(13, 2, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(14, 2, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(15, 2, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(16, 2, 6, '{"is_show":"true","can_edit":"true"}', 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(17, 2, 16, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(18, 2, 4, NULL, 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(19, 2, 7, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(20, 2, 8, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(21, 2, 9, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(22, 2, 10, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(24, 2, 19, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(25, 3, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(26, 3, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(27, 3, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(28, 3, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(29, 3, 11, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(30, 3, 12, NULL, 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(31, 3, 13, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(32, 3, 14, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(33, 3, 15, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(34, 3, 7, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(35, 3, 8, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(36, 3, 9, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(37, 3, 10, NULL, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(38, 3, 17, '{"comparison_name":"total_count.total"}', 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(39, 3, 19, NULL, 15, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(40, 4, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(41, 4, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(42, 4, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(43, 4, 6, '{"is_show":"true","can_edit":"true"}', 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(44, 4, 16, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(45, 4, 11, NULL, 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(46, 4, 12, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(47, 4, 13, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(48, 4, 17, '{"is_show":"true","can_edit":"true"}', 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(49, 4, 18, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(50, 4, 7, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(51, 4, 8, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(52, 4, 9, NULL, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(53, 4, 10, NULL, 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(62, 3, 6, '{"statistics_name":"miss_count.miss"}', 18, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(63, 5, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(64, 5, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(65, 5, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(66, 5, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(67, 5, 5, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(68, 5, 6, '{"statistics_name":"invalid_length_count.valids"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(69, 5, 24, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(70, 5, 23, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(71, 5, 7, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(72, 5, 8, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(73, 5, 9, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(74, 5, 10, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(75, 5, 17, '', 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(76, 5, 19, NULL, 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(79, 6, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(80, 6, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(81, 6, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(82, 6, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(83, 6, 5, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(84, 6, 6, '{"statistics_name":"duplicate_count.duplicates"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(85, 6, 7, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(86, 6, 8, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(87, 6, 9, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(88, 6, 10, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(89, 6, 17, '', 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(90, 6, 19, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(93, 7, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(94, 7, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(95, 7, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(96, 7, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(97, 7, 5, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(98, 7, 6, '{"statistics_name":"regexp_count.regexps"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(99, 7, 25, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(100, 7, 7, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(101, 7, 8, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(102, 7, 9, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(103, 7, 10, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(104, 7, 17, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(105, 7, 19, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(108, 8, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(109, 8, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(110, 8, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(111, 8, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(112, 8, 5, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(113, 8, 6, '{"statistics_name":"timeliness_count.timeliness"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(114, 8, 26, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(115, 8, 27, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(116, 8, 7, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(117, 8, 8, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(118, 8, 9, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(119, 8, 10, NULL, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(120, 8, 17, NULL, 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(121, 8, 19, NULL, 15, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(124, 9, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(125, 9, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(126, 9, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(127, 9, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(128, 9, 5, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(129, 9, 6, '{"statistics_name":"enum_count.enums"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(130, 9, 28, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(131, 9, 7, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(132, 9, 8, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(133, 9, 9, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(134, 9, 10, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(135, 9, 17, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(136, 9, 19, NULL, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(139, 10, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(140, 10, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(141, 10, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(142, 10, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(143, 10, 6, '{"statistics_name":"table_count.total"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(144, 10, 7, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(145, 10, 8, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(146, 10, 9, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(147, 10, 10, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(148, 10, 17, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO `t_ds_relation_rule_input_entry`
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(149, 10, 19, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(`id`, `rule_id`, `rule_input_entry_id`, `values_map`, `index`, `create_time`, `update_time`)
VALUES(150, 8, 29, NULL, 7, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');

--
-- Table structure for table t_ds_environment
--
DROP TABLE IF EXISTS t_ds_environment CASCADE;
CREATE TABLE t_ds_environment
(
    id          int       NOT NULL AUTO_INCREMENT,
    code        bigint(20) NOT NULL,
    name        varchar(100)       DEFAULT NULL,
    config      text               DEFAULT NULL,
    description text,
    operator    int                DEFAULT NULL,
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY environment_name_unique (name),
    UNIQUE KEY environment_code_unique (code)
);

--
-- Table structure for table t_ds_environment_worker_group_relation
--
DROP TABLE IF EXISTS t_ds_environment_worker_group_relation CASCADE;
CREATE TABLE t_ds_environment_worker_group_relation
(
    id               int          NOT NULL AUTO_INCREMENT,
    environment_code bigint(20) NOT NULL,
    worker_group     varchar(255) NOT NULL,
    operator         int                   DEFAULT NULL,
    create_time      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY environment_worker_group_unique (environment_code,worker_group)
);
DROP TABLE IF EXISTS t_ds_task_group_queue;
CREATE TABLE t_ds_task_group_queue
(
   id           int(11) NOT NULL AUTO_INCREMENT ,
   task_id      int(11) DEFAULT NULL ,
   task_name    VARCHAR(100) DEFAULT NULL ,
   group_id     int(11) DEFAULT NULL ,
   process_id   int(11) DEFAULT NULL ,
   priority     int(8) DEFAULT '0' ,
   status       int(4) DEFAULT '-1' ,
   force_start  int(4) DEFAULT '0' ,
   in_queue     int(4) DEFAULT '0' ,
   create_time  datetime DEFAULT NULL ,
   update_time  datetime DEFAULT NULL ,
   PRIMARY KEY (id)
);

DROP TABLE IF EXISTS t_ds_task_group;
CREATE TABLE t_ds_task_group
(
   id          int(11)  NOT NULL AUTO_INCREMENT ,
   name        varchar(100) DEFAULT NULL ,
   description varchar(255) DEFAULT NULL ,
   group_size  int(11) NOT NULL ,
   project_code  bigint(20) DEFAULT '0',
   use_size    int(11) DEFAULT '0' ,
   user_id     int(11) DEFAULT NULL ,
   status      int(4) DEFAULT '1'  ,
   create_time datetime DEFAULT NULL ,
   update_time datetime DEFAULT NULL ,
   PRIMARY KEY(id)
);

-- ----------------------------
-- Table structure for t_ds_alert_plugin_instance
-- ----------------------------
DROP TABLE IF EXISTS t_ds_audit_log;
CREATE TABLE t_ds_audit_log
(
    id                  int(11) NOT NULL AUTO_INCREMENT,
    user_id             int(11) NOT NULL,
    resource_type       int(11) NOT NULL,
    operation           int(11) NOT NULL,
    time                timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    resource_id         int(11) NOT NULL,
    PRIMARY KEY (id)
);


DROP TABLE IF EXISTS t_ds_k8s;
CREATE TABLE t_ds_k8s
(
    id           int(11) NOT NULL AUTO_INCREMENT ,
    k8s_name     varchar(100) DEFAULT NULL ,
    k8s_config   text DEFAULT NULL,
    create_time  datetime DEFAULT NULL ,
    update_time  datetime DEFAULT NULL ,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS t_ds_k8s_namespace;
CREATE TABLE t_ds_k8s_namespace (
    id                 int(11) NOT NULL AUTO_INCREMENT ,
    code               bigint(20) NOT NULL,
    limits_memory      int(11) DEFAULT NULL,
    namespace          varchar(100) DEFAULT NULL,
    user_id            int(11) DEFAULT NULL,
    pod_replicas       int(11) DEFAULT NULL,
    pod_request_cpu    decimal(14,3) DEFAULT NULL,
    pod_request_memory int(11) DEFAULT NULL,
    limits_cpu         decimal(14,3) DEFAULT NULL,
    cluster_code       bigint(20) NOT NULL,
    create_time        datetime DEFAULT NULL ,
    update_time        datetime DEFAULT NULL ,
    PRIMARY KEY (id) ,
    UNIQUE KEY k8s_namespace_unique (namespace,cluster_code)
);
-- ----------------------------
-- Records of t_ds_k8s_namespace
-- ----------------------------
INSERT INTO `t_ds_k8s_namespace`
(`id`,`code`,`limits_memory`,`namespace`,`user_id`,`pod_replicas`,`pod_request_cpu`,`pod_request_memory`,`limits_cpu`,`cluster_code`,`create_time`,`update_time`)
VALUES (1, 990001, 1000, 'flink_test', 1, 1, 0.1, 1, 100, 0, '2022-03-03 11:31:24.0', '2022-03-03 11:31:24.0');

INSERT INTO `t_ds_k8s_namespace`
(`id`,`code`,`limits_memory`,`namespace`,`user_id`,`pod_replicas`,`pod_request_cpu`,`pod_request_memory`,`limits_cpu`,`cluster_code`,`create_time`,`update_time`)
VALUES (2, 990002, 500, 'spark_test', 2, 1, 10000, 1, 100, 0, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');

INSERT INTO `t_ds_k8s_namespace`
(`id`,`code`,`limits_memory`,`namespace`,`user_id`,`pod_replicas`,`pod_request_cpu`,`pod_request_memory`,`limits_cpu`,`cluster_code`,`create_time`,`update_time`)
VALUES (3, 990003, 200, 'auth_test', 3, 1, 100, 1, 10000, 0, '2020-03-03 11:31:24.0', '2020-03-03 11:31:24.0');

-- ----------------------------
-- Table structure for t_ds_relation_namespace_user
-- ----------------------------
DROP TABLE IF EXISTS t_ds_relation_namespace_user;
CREATE TABLE t_ds_relation_namespace_user (
    id                int(11) NOT NULL AUTO_INCREMENT ,
    user_id           int(11) NOT NULL ,
    namespace_id      int(11) NOT NULL ,
    perm              int(11) DEFAULT '1' ,
    create_time       datetime DEFAULT NULL ,
    update_time       datetime DEFAULT NULL ,
    PRIMARY KEY (id) ,
    UNIQUE KEY namespace_user_unique (user_id,namespace_id)
);

-- ----------------------------
-- Table structure for t_ds_alert_send_status
-- ----------------------------
DROP TABLE IF EXISTS t_ds_alert_send_status CASCADE;
CREATE TABLE t_ds_alert_send_status
(
    id                            int NOT NULL AUTO_INCREMENT,
    alert_id                      int NOT NULL,
    alert_plugin_instance_id      int NOT NULL,
    send_status                   tinyint(4) DEFAULT '0',
    log                           text,
    create_time                   timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY alert_send_status_unique (alert_id,alert_plugin_instance_id)
);


--
-- Table structure for table t_ds_cluster
--
DROP TABLE IF EXISTS t_ds_cluster CASCADE;
CREATE TABLE t_ds_cluster
(
    id          int       NOT NULL AUTO_INCREMENT,
    code        bigint(20) NOT NULL,
    name        varchar(100)       DEFAULT NULL,
    config      text               DEFAULT NULL,
    description text,
    operator    int                DEFAULT NULL,
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY cluster_name_unique (name),
    UNIQUE KEY cluster_code_unique (code)
);

INSERT INTO `t_ds_cluster`
(`id`, `code`, `name`, `config`, `description`, `operator`, `create_time`, `update_time`)
VALUES (100, 0, 'ds_null_k8s', '{"k8s":"ds_null_k8s"}', 'test', 1, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');

--
-- Table structure for t_ds_fav_task
--
DROP TABLE IF EXISTS t_ds_fav_task CASCADE;
CREATE TABLE t_ds_fav_task
(
    id        bigint(20) NOT NULL AUTO_INCREMENT,
    task_name varchar(64) NOT NULL,
    user_id   int         NOT NULL,
    PRIMARY KEY (id)
);
