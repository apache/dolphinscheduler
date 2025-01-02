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
    title         varchar(512) DEFAULT NULL,
    sign           char(40) NOT NULL DEFAULT '',
    content       text,
    alert_status  tinyint(4) DEFAULT '0',
    warning_type  tinyint(4) DEFAULT '2',
    log           text,
    alertgroup_id int(11) DEFAULT NULL,
    create_time   datetime    DEFAULT NULL,
    update_time   datetime    DEFAULT NULL,
    project_code        bigint(20) DEFAULT NULL,
    workflow_definition_code        bigint(20) DEFAULT NULL,
    workflow_instance_id     int(11) DEFAULT NULL,
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
-- Table structure for t_ds_command
-- ----------------------------
DROP TABLE IF EXISTS t_ds_command CASCADE;
CREATE TABLE t_ds_command
(
    id                         int(11) NOT NULL AUTO_INCREMENT,
    command_type               tinyint(4) DEFAULT NULL,
    workflow_definition_code    bigint(20) DEFAULT NULL,
    command_param              text,
    task_depend_type           tinyint(4) DEFAULT NULL,
    failure_strategy           tinyint(4) DEFAULT '0',
    warning_type               tinyint(4) DEFAULT '0',
    warning_group_id           int(11) DEFAULT NULL,
    schedule_time              datetime DEFAULT NULL,
    start_time                 datetime DEFAULT NULL,
    executor_id                int(11) DEFAULT NULL,
    update_time                datetime DEFAULT NULL,
    workflow_instance_priority  int(11) DEFAULT '2',
    worker_group               varchar(255),
    tenant_code                varchar(64) DEFAULT 'default',
    environment_code           bigint(20) DEFAULT '-1',
    dry_run                    int NULL DEFAULT 0,
    workflow_instance_id        int(11) DEFAULT 0,
    workflow_definition_version int(11) DEFAULT 0,
    test_flag                  int NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY                        priority_id_index (workflow_instance_priority, id)
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
    workflow_definition_code    bigint(20) DEFAULT NULL,
    command_param              text,
    task_depend_type           tinyint(4) DEFAULT NULL,
    failure_strategy           tinyint(4) DEFAULT '0',
    warning_type               tinyint(4) DEFAULT '0',
    warning_group_id           int(11) DEFAULT NULL,
    schedule_time              datetime DEFAULT NULL,
    start_time                 datetime DEFAULT NULL,
    update_time                datetime DEFAULT NULL,
    workflow_instance_priority  int(11) DEFAULT '2',
    worker_group               varchar(255),
    tenant_code                varchar(64) DEFAULT 'default',
    environment_code           bigint(20) DEFAULT '-1',
    message                    text,
    dry_run                    int NULL DEFAULT 0,
    workflow_instance_id        int(11) DEFAULT 0,
    workflow_definition_version int(11) DEFAULT 0,
    test_flag                  int NULL DEFAULT 0,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Records of t_ds_error_command
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_workflow_definition
-- ----------------------------
DROP TABLE IF EXISTS t_ds_workflow_definition CASCADE;
CREATE TABLE t_ds_workflow_definition
(
    id               int(11) NOT NULL AUTO_INCREMENT,
    code             bigint(20) NOT NULL,
    name             varchar(255) DEFAULT NULL,
    version          int(11) NOT NULL DEFAULT 1,
    description      text,
    project_code     bigint(20) NOT NULL,
    release_state    tinyint(4) DEFAULT NULL,
    user_id          int(11) DEFAULT NULL,
    global_params    text,
    flag             tinyint(4) DEFAULT NULL,
    locations        text,
    warning_group_id int(11) DEFAULT NULL,
    timeout          int(11) DEFAULT '0',
    execution_type   tinyint(4) DEFAULT '0',
    create_time      datetime NOT NULL,
    update_time      datetime     DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY workflow_unique (name,project_code) USING BTREE,
    UNIQUE KEY code_unique (code)
);

-- ----------------------------
-- Table structure for t_ds_workflow_definition_log
-- ----------------------------
DROP TABLE IF EXISTS t_ds_workflow_definition_log CASCADE;
CREATE TABLE t_ds_workflow_definition_log
(
    id               int(11) NOT NULL AUTO_INCREMENT,
    code             bigint(20) NOT NULL,
    name             varchar(255) DEFAULT NULL,
    version          int(11) NOT NULL DEFAULT '1',
    description      text,
    project_code     bigint(20) NOT NULL,
    release_state    tinyint(4) DEFAULT NULL,
    user_id          int(11) DEFAULT NULL,
    global_params    text,
    flag             tinyint(4) DEFAULT NULL,
    locations        text,
    warning_group_id int(11) DEFAULT NULL,
    timeout          int(11) DEFAULT '0',
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
    name                    varchar(255) DEFAULT NULL,
    version                 int(11) NOT NULL DEFAULT '1',
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
    name                    varchar(255) DEFAULT NULL,
    version                 int(11) NOT NULL DEFAULT '1',
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
-- Table structure for t_ds_workflow_task_relation
-- ----------------------------
DROP TABLE IF EXISTS t_ds_workflow_task_relation CASCADE;
CREATE TABLE t_ds_workflow_task_relation
(
    id                         int(11) NOT NULL AUTO_INCREMENT,
    name                       varchar(255) DEFAULT NULL,
    workflow_definition_version int(11) DEFAULT NULL,
    project_code               bigint(20) NOT NULL,
    workflow_definition_code    bigint(20) NOT NULL,
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
-- Table structure for t_ds_workflow_task_relation_log
-- ----------------------------
DROP TABLE IF EXISTS t_ds_workflow_task_relation_log CASCADE;
CREATE TABLE t_ds_workflow_task_relation_log
(
    id                         int(11) NOT NULL AUTO_INCREMENT,
    name                       varchar(255) DEFAULT NULL,
    workflow_definition_version int(11) DEFAULT NULL,
    project_code               bigint(20) NOT NULL,
    workflow_definition_code    bigint(20) NOT NULL,
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
-- Table structure for t_ds_workflow_instance
-- ----------------------------
DROP TABLE IF EXISTS t_ds_workflow_instance CASCADE;
CREATE TABLE t_ds_workflow_instance
(
    id                         int(11) NOT NULL AUTO_INCREMENT,
    name                       varchar(255) DEFAULT NULL,
    workflow_definition_version int(11) NOT NULL DEFAULT '1',
    workflow_definition_code    bigint(20) not NULL,
    project_code               bigint(20) DEFAULT NULL,
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
    is_sub_workflow             int(11) DEFAULT '0',
    executor_id                int(11) NOT NULL,
    executor_name              varchar(64) DEFAULT NULL,
    history_cmd                text,
    workflow_instance_priority  int(11) DEFAULT '2',
    worker_group               varchar(64)  DEFAULT NULL,
    environment_code           bigint(20) DEFAULT '-1',
    timeout                    int(11) DEFAULT '0',
    next_workflow_instance_id   int(11) DEFAULT '0',
    tenant_code                varchar(64) DEFAULT 'default',
    var_pool                   longtext,
    dry_run                    int NULL DEFAULT 0,
    restart_time               datetime     DEFAULT NULL,
    test_flag                  int NULL DEFAULT 0,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for t_ds_project
-- ----------------------------
DROP TABLE IF EXISTS t_ds_project CASCADE;
CREATE TABLE t_ds_project
(
    id          int(11) NOT NULL AUTO_INCREMENT,
    name        varchar(255) DEFAULT NULL,
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
-- Table structure for t_ds_project_parameter
-- ----------------------------
DROP TABLE IF EXISTS t_ds_project_parameter CASCADE;
CREATE TABLE t_ds_project_parameter
(
    id              int(11) NOT NULL AUTO_INCREMENT,
    param_name      varchar(255) NOT NULL,
    param_value     text NOT NULL,
    param_data_type varchar(50) DEFAULT 'VARCHAR',
    code            bigint(20) NOT NULL,
    project_code    bigint(20) NOT NULL,
    user_id         int(11) DEFAULT NULL,
    operator        int(11) DEFAULT NULL,
    create_time     datetime NOT NULL,
    update_time     datetime     DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY unique_project_parameter_name (project_code, param_name),
    UNIQUE KEY unique_project_parameter_code (code)
);

-- ----------------------------
-- Records of t_ds_project_parameter
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_project_preference
-- ----------------------------
DROP TABLE IF EXISTS t_ds_project_preference CASCADE;
CREATE TABLE t_ds_project_preference
(
    id              int(11) NOT NULL AUTO_INCREMENT,
    code            bigint(20) NOT NULL,
    project_code    bigint(20) NOT NULL,
    preferences     varchar(512) NOT NULL,
    user_id         int(11) DEFAULT NULL,
    state           int(11) DEFAULT '1',
    create_time     datetime NOT NULL,
    update_time     datetime     DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY unique_project_preference_project_code (project_code),
    UNIQUE KEY unique_project_preference_code (code)
);

-- ----------------------------
-- Records of t_ds_project_preference
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
-- Table structure for t_ds_relation_workflow_instance
-- ----------------------------
DROP TABLE IF EXISTS t_ds_relation_workflow_instance CASCADE;
CREATE TABLE t_ds_relation_workflow_instance
(
    id                         int(11) NOT NULL AUTO_INCREMENT,
    parent_workflow_instance_id int(11) DEFAULT NULL,
    parent_task_instance_id    int(11) DEFAULT NULL,
    workflow_instance_id        int(11) DEFAULT NULL,
    PRIMARY KEY (id)
);

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
-- Deprecated
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
-- Deprecated
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
    workflow_definition_code   bigint(20) NOT NULL,
    start_time                datetime     NOT NULL,
    end_time                  datetime     NOT NULL,
    timezone_id               varchar(40) DEFAULT NULL,
    crontab                   varchar(255) NOT NULL,
    failure_strategy          tinyint(4) NOT NULL,
    user_id                   int(11) NOT NULL,
    release_state             tinyint(4) NOT NULL,
    warning_type              tinyint(4) NOT NULL,
    warning_group_id          int(11) DEFAULT NULL,
    workflow_instance_priority int(11) DEFAULT '2',
    worker_group              varchar(255) DEFAULT '',
    tenant_code                varchar(64) DEFAULT 'default',
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
    task_definition_version int(11) NOT NULL DEFAULT '1',
    workflow_instance_id     int(11) DEFAULT NULL,
    workflow_instance_name   varchar(255) DEFAULT NULL,
    project_code            bigint(20) DEFAULT NULL,
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
    worker_group            varchar(255)  DEFAULT NULL,
    environment_code        bigint(20) DEFAULT '-1',
    environment_config      text         DEFAULT '',
    executor_id             int(11) DEFAULT NULL,
    executor_name           varchar(64) DEFAULT NULL,
    first_submit_time       datetime     DEFAULT NULL,
    delay_time              int(4) DEFAULT '0',
    task_group_id           int(11) DEFAULT NULL,
    var_pool                longtext,
    dry_run                 int NULL DEFAULT 0,
    cpu_quota               int(11) DEFAULT '-1' NOT NULL,
    memory_max              int(11) DEFAULT '-1' NOT NULL,
    test_flag               int NULL DEFAULT 0,
    PRIMARY KEY (id)
);

-- ----------------------------
-- Records of t_ds_task_instance
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_task_instance_context
-- ----------------------------
DROP TABLE IF EXISTS `t_ds_task_instance_context`;
CREATE TABLE `t_ds_task_instance_context` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `task_instance_id` int(11) NOT NULL,
    `context` text NOT NULL,
    `context_type` varchar(200) NOT NULL,
    `create_time` datetime NOT NULL,
    `update_time` datetime NOT NULL,
    PRIMARY KEY (`id`)
);

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
INSERT IGNORE INTO `t_ds_tenant`
VALUES ('-1', 'default', 'default tenant', '1', current_timestamp, current_timestamp);

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
    tenant_id     int(11) DEFAULT -1,
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
    PRIMARY KEY (id),
    UNIQUE KEY name_unique (name)
);

-- ----------------------------
-- Records of t_ds_worker_group
-- ----------------------------

-- ----------------------------
-- Table structure for t_ds_relation_project_worker_group
-- ----------------------------
DROP TABLE IF EXISTS t_ds_relation_project_worker_group CASCADE;
CREATE TABLE t_ds_relation_project_worker_group
(
    id            int(11) NOT NULL AUTO_INCREMENT,
    project_code  bigint(20) NOT NULL,
    worker_group  varchar(255) DEFAULT NULL,
    create_time   datetime DEFAULT NULL,
    update_time   datetime DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY unique_project_worker_group(project_code,worker_group)
);

-- ----------------------------
-- Table structure for t_ds_version
-- ----------------------------
DROP TABLE IF EXISTS t_ds_version CASCADE;
CREATE TABLE t_ds_version
(
    id      int(11) NOT NULL AUTO_INCREMENT,
    version varchar(63) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY version_UNIQUE (version)
);

-- ----------------------------
-- Records of t_ds_version
-- ----------------------------
INSERT INTO t_ds_version
VALUES ('1', '3.3.0');


-- ----------------------------
-- Records of t_ds_alertgroup
-- ----------------------------
INSERT INTO t_ds_alertgroup(alert_instance_ids, create_user_id, group_name, description, create_time, update_time)
VALUES (NULL, 1, 'default admin warning group', 'default admin warning group', '2018-11-29 10:20:39',
        '2018-11-29 10:20:39');

-- ----------------------------
-- Records of t_ds_user
-- ----------------------------
INSERT INTO t_ds_user
VALUES ('1', 'admin', '7ad2410b2f4c074479a8937a28a22b8f', '0', 'xxx@qq.com', '', '-1', '2018-03-27 15:48:50',
        '2018-10-24 17:40:22', null, 1, null);

-- ----------------------------
-- Table structure for t_ds_plugin_define
-- ----------------------------
DROP TABLE IF EXISTS t_ds_plugin_define CASCADE;
CREATE TABLE t_ds_plugin_define
(
    id            int          NOT NULL AUTO_INCREMENT,
    plugin_name   varchar(255) NOT NULL,
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
-- Table structure for table t_ds_environment
--
DROP TABLE IF EXISTS t_ds_environment CASCADE;
CREATE TABLE t_ds_environment
(
    id          int       NOT NULL AUTO_INCREMENT,
    code        bigint(20) NOT NULL,
    name        varchar(255)       DEFAULT NULL,
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
   task_name    VARCHAR(255) DEFAULT NULL ,
   group_id     int(11) DEFAULT NULL ,
   workflow_instance_id   int(11) DEFAULT NULL ,
   priority     int(8) DEFAULT '0' ,
   status       int(4) DEFAULT '-1' ,
   force_start  int(4) DEFAULT '0' ,
   in_queue     int(4) DEFAULT '0' ,
   create_time  datetime DEFAULT NULL ,
   update_time  datetime DEFAULT NULL ,
   KEY idx_t_ds_task_group_queue_in_queue (in_queue) ,
   PRIMARY KEY (id)
);

DROP TABLE IF EXISTS t_ds_task_group;
CREATE TABLE t_ds_task_group
(
   id          int(11)  NOT NULL AUTO_INCREMENT ,
   name        varchar(255) DEFAULT NULL ,
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
-- Table structure for t_ds_audit_log
-- ----------------------------
DROP TABLE IF EXISTS t_ds_audit_log;
CREATE TABLE t_ds_audit_log
(
    id                  int(11) NOT NULL AUTO_INCREMENT,
    user_id             int(11) NOT NULL,
    model_id            bigint(20) NOT NULL,
    model_name          varchar(255) NOT NULL,
    model_type          varchar(255) NOT NULL,
    operation_type      varchar(255) NOT NULL,
    description         varchar(255) NOT NULL,
    latency             int(11) NOT NULL,
    detail              varchar(255) DEFAULT NULL,
    create_time         timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);


DROP TABLE IF EXISTS t_ds_k8s;
CREATE TABLE t_ds_k8s
(
    id           int(11) NOT NULL AUTO_INCREMENT ,
    k8s_name     varchar(255) DEFAULT NULL ,
    k8s_config   text DEFAULT NULL,
    create_time  datetime DEFAULT NULL ,
    update_time  datetime DEFAULT NULL ,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS t_ds_k8s_namespace;
CREATE TABLE t_ds_k8s_namespace (
    id                 int(11) NOT NULL AUTO_INCREMENT ,
    code               bigint(20) NOT NULL,
    namespace          varchar(255) DEFAULT NULL,
    user_id            int(11) DEFAULT NULL,
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
(`id`,`code`,`namespace`,`user_id`,`cluster_code`,`create_time`,`update_time`)
VALUES (1, 990001, 'flink_test', 1, 0, '2022-03-03 11:31:24.0', '2022-03-03 11:31:24.0');

INSERT INTO `t_ds_k8s_namespace`
(`id`,`code`,`namespace`,`user_id`,`cluster_code`,`create_time`,`update_time`)
VALUES (2, 990002, 'spark_test', 2, 0, '2021-03-03 11:31:24.0', '2021-03-03 11:31:24.0');

INSERT INTO `t_ds_k8s_namespace`
(`id`,`code`,`namespace`,`user_id`,`cluster_code`,`create_time`,`update_time`)
VALUES (3, 990003, 'auth_test', 3, 0, '2020-03-03 11:31:24.0', '2020-03-03 11:31:24.0');

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
    name        varchar(255)       DEFAULT NULL,
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
    task_type varchar(64) NOT NULL,
    user_id   int         NOT NULL,
    PRIMARY KEY (id)
);


DROP TABLE IF EXISTS t_ds_relation_sub_workflow;
CREATE TABLE t_ds_relation_sub_workflow (
    id BIGINT AUTO_INCREMENT NOT NULL,
    parent_workflow_instance_id BIGINT NOT NULL,
    parent_task_code BIGINT NOT NULL,
    sub_workflow_instance_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_parent_workflow_instance_id (parent_workflow_instance_id),
    INDEX idx_parent_task_code (parent_task_code),
    INDEX idx_sub_workflow_instance_id (sub_workflow_instance_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_ds_workflow_task_lineage
-- ----------------------------
DROP TABLE IF EXISTS t_ds_workflow_task_lineage;
CREATE TABLE t_ds_workflow_task_lineage
(
    `id`                           int      NOT NULL AUTO_INCREMENT,
    `workflow_definition_code`      bigint(20)   NOT NULL DEFAULT 0,
    `workflow_definition_version`   int      NOT NULL DEFAULT 0,
    `task_definition_code`         bigint(20)   NOT NULL DEFAULT 0,
    `task_definition_version`      int      NOT NULL DEFAULT 0,
    `dept_project_code`            bigint(20)   NOT NULL DEFAULT 0,
    `dept_workflow_definition_code` bigint(20)   NOT NULL DEFAULT 0,
    `dept_task_definition_code`    bigint(20)   NOT NULL DEFAULT 0,
    `create_time`                  datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`                  datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY                            `idx_workflow_code_version` (`workflow_definition_code`,`workflow_definition_version`),
    KEY                            `idx_task_code_version` (`task_definition_code`,`task_definition_version`),
    KEY                            `idx_dept_code` (`dept_project_code`,`dept_workflow_definition_code`,`dept_task_definition_code`)
);


-- ----------------------------
-- Table structure for jdbc registry
-- ----------------------------

DROP TABLE IF EXISTS `t_ds_jdbc_registry_data`;
CREATE TABLE `t_ds_jdbc_registry_data`
(
    `id`               bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `data_key`         varchar(256) NOT NULL COMMENT 'key, like zookeeper node path',
    `data_value`       text         NOT NULL COMMENT 'data, like zookeeper node value',
    `data_type`        varchar(64)  NOT NULL COMMENT 'EPHEMERAL, PERSISTENT',
    `client_id`        bigint       NOT NULL COMMENT 'client id',
    `create_time`      timestamp    NOT NULL default current_timestamp COMMENT 'create time',
    `last_update_time` timestamp    NOT NULL default current_timestamp COMMENT 'last update time',
    PRIMARY KEY (`id`),
    unique KEY `uk_t_ds_jdbc_registry_dataKey`(`data_key`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


DROP TABLE IF EXISTS `t_ds_jdbc_registry_lock`;
CREATE TABLE `t_ds_jdbc_registry_lock`
(
    `id`          bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `lock_key`    varchar(256) NOT NULL COMMENT 'lock path',
    `lock_owner`  varchar(256) NOT NULL COMMENT 'the lock owner, ip_processId',
    `client_id`   bigint       NOT NULL COMMENT 'client id',
    `create_time` timestamp    NOT NULL default current_timestamp COMMENT 'create time',
    PRIMARY KEY (`id`),
    unique KEY `uk_t_ds_jdbc_registry_lockKey`(`lock_key`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `t_ds_jdbc_registry_client_heartbeat`;
CREATE TABLE `t_ds_jdbc_registry_client_heartbeat`
(
    `id`                  bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `client_name`         varchar(256) NOT NULL COMMENT 'client name, ip_processId',
    `last_heartbeat_time` bigint       NOT NULL COMMENT 'last heartbeat timestamp',
    `connection_config`   text         NOT NULL COMMENT 'connection config',
    `create_time`         timestamp    NOT NULL default current_timestamp COMMENT 'create time',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `t_ds_jdbc_registry_data_change_event`;
CREATE TABLE `t_ds_jdbc_registry_data_change_event`
(
    `id`                 bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'primary key',
    `event_type`         varchar(64) NOT NULL COMMENT 'ADD, UPDATE, DELETE',
    `jdbc_registry_data` text        NOT NULL COMMENT 'jdbc registry data',
    `create_time`        timestamp   NOT NULL default current_timestamp COMMENT 'create time',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

-- ----------------------------
-- Table structure for jdbc registry
-- ----------------------------
