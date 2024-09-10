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

DROP TABLE IF EXISTS QRTZ_FIRED_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_PAUSED_TRIGGER_GRPS;
DROP TABLE IF EXISTS QRTZ_SCHEDULER_STATE;
DROP TABLE IF EXISTS QRTZ_LOCKS;
DROP TABLE IF EXISTS QRTZ_SIMPLE_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_SIMPROP_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_CRON_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_BLOB_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_JOB_DETAILS;
DROP TABLE IF EXISTS QRTZ_CALENDARS;

CREATE TABLE QRTZ_JOB_DETAILS (
                                  SCHED_NAME character varying(120) NOT NULL,
                                  JOB_NAME character varying(200) NOT NULL,
                                  JOB_GROUP character varying(200) NOT NULL,
                                  DESCRIPTION character varying(250) NULL,
                                  JOB_CLASS_NAME character varying(250) NOT NULL,
                                  IS_DURABLE boolean NOT NULL,
                                  IS_NONCONCURRENT boolean NOT NULL,
                                  IS_UPDATE_DATA boolean NOT NULL,
                                  REQUESTS_RECOVERY boolean NOT NULL,
                                  JOB_DATA bytea NULL
);

alter table QRTZ_JOB_DETAILS add primary key(SCHED_NAME,JOB_NAME,JOB_GROUP);

CREATE TABLE QRTZ_TRIGGERS (
                               SCHED_NAME character varying(120) NOT NULL,
                               TRIGGER_NAME character varying(200) NOT NULL,
                               TRIGGER_GROUP character varying(200) NOT NULL,
                               JOB_NAME character varying(200) NOT NULL,
                               JOB_GROUP character varying(200) NOT NULL,
                               DESCRIPTION character varying(250) NULL,
                               NEXT_FIRE_TIME BIGINT NULL,
                               PREV_FIRE_TIME BIGINT NULL,
                               PRIORITY INTEGER NULL,
                               TRIGGER_STATE character varying(16) NOT NULL,
                               TRIGGER_TYPE character varying(8) NOT NULL,
                               START_TIME BIGINT NOT NULL,
                               END_TIME BIGINT NULL,
                               CALENDAR_NAME character varying(200) NULL,
                               MISFIRE_INSTR SMALLINT NULL,
                               JOB_DATA bytea NULL
) ;

alter table QRTZ_TRIGGERS add primary key(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);

CREATE TABLE QRTZ_SIMPLE_TRIGGERS (
                                      SCHED_NAME character varying(120) NOT NULL,
                                      TRIGGER_NAME character varying(200) NOT NULL,
                                      TRIGGER_GROUP character varying(200) NOT NULL,
                                      REPEAT_COUNT BIGINT NOT NULL,
                                      REPEAT_INTERVAL BIGINT NOT NULL,
                                      TIMES_TRIGGERED BIGINT NOT NULL
) ;

alter table QRTZ_SIMPLE_TRIGGERS add primary key(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);

CREATE TABLE QRTZ_CRON_TRIGGERS (
                                    SCHED_NAME character varying(120) NOT NULL,
                                    TRIGGER_NAME character varying(200) NOT NULL,
                                    TRIGGER_GROUP character varying(200) NOT NULL,
                                    CRON_EXPRESSION character varying(120) NOT NULL,
                                    TIME_ZONE_ID character varying(80)
) ;

alter table QRTZ_CRON_TRIGGERS add primary key(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);

CREATE TABLE QRTZ_SIMPROP_TRIGGERS (
                                       SCHED_NAME character varying(120) NOT NULL,
                                       TRIGGER_NAME character varying(200) NOT NULL,
                                       TRIGGER_GROUP character varying(200) NOT NULL,
                                       STR_PROP_1 character varying(512) NULL,
                                       STR_PROP_2 character varying(512) NULL,
                                       STR_PROP_3 character varying(512) NULL,
                                       INT_PROP_1 INT NULL,
                                       INT_PROP_2 INT NULL,
                                       LONG_PROP_1 BIGINT NULL,
                                       LONG_PROP_2 BIGINT NULL,
                                       DEC_PROP_1 NUMERIC(13,4) NULL,
                                       DEC_PROP_2 NUMERIC(13,4) NULL,
                                       BOOL_PROP_1 boolean NULL,
                                       BOOL_PROP_2 boolean NULL
) ;

alter table QRTZ_SIMPROP_TRIGGERS add primary key(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);

CREATE TABLE QRTZ_BLOB_TRIGGERS (
                                    SCHED_NAME character varying(120) NOT NULL,
                                    TRIGGER_NAME character varying(200) NOT NULL,
                                    TRIGGER_GROUP character varying(200) NOT NULL,
                                    BLOB_DATA bytea NULL
) ;

alter table QRTZ_BLOB_TRIGGERS add primary key(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);

CREATE TABLE QRTZ_CALENDARS (
                                SCHED_NAME character varying(120) NOT NULL,
                                CALENDAR_NAME character varying(200) NOT NULL,
                                CALENDAR bytea NOT NULL
) ;

alter table QRTZ_CALENDARS add primary key(SCHED_NAME,CALENDAR_NAME);

CREATE TABLE QRTZ_PAUSED_TRIGGER_GRPS (
                                          SCHED_NAME character varying(120) NOT NULL,
                                          TRIGGER_GROUP character varying(200) NOT NULL
) ;

alter table QRTZ_PAUSED_TRIGGER_GRPS add primary key(SCHED_NAME,TRIGGER_GROUP);

CREATE TABLE QRTZ_FIRED_TRIGGERS (
                                     SCHED_NAME character varying(120) NOT NULL,
                                     ENTRY_ID character varying(200) NOT NULL,
                                     TRIGGER_NAME character varying(200) NOT NULL,
                                     TRIGGER_GROUP character varying(200) NOT NULL,
                                     INSTANCE_NAME character varying(200) NOT NULL,
                                     FIRED_TIME BIGINT NOT NULL,
                                     SCHED_TIME BIGINT NOT NULL,
                                     PRIORITY INTEGER NOT NULL,
                                     STATE character varying(16) NOT NULL,
                                     JOB_NAME character varying(200) NULL,
                                     JOB_GROUP character varying(200) NULL,
                                     IS_NONCONCURRENT boolean NULL,
                                     REQUESTS_RECOVERY boolean NULL
) ;

alter table QRTZ_FIRED_TRIGGERS add primary key(SCHED_NAME,ENTRY_ID);

CREATE TABLE QRTZ_SCHEDULER_STATE (
                                      SCHED_NAME character varying(120) NOT NULL,
                                      INSTANCE_NAME character varying(200) NOT NULL,
                                      LAST_CHECKIN_TIME BIGINT NOT NULL,
                                      CHECKIN_INTERVAL BIGINT NOT NULL
) ;

alter table QRTZ_SCHEDULER_STATE add primary key(SCHED_NAME,INSTANCE_NAME);

CREATE TABLE QRTZ_LOCKS (
                            SCHED_NAME character varying(120) NOT NULL,
                            LOCK_NAME character varying(40) NOT NULL
) ;

alter table QRTZ_LOCKS add primary key(SCHED_NAME,LOCK_NAME);

CREATE INDEX IDX_QRTZ_J_REQ_RECOVERY ON QRTZ_JOB_DETAILS(SCHED_NAME,REQUESTS_RECOVERY);
CREATE INDEX IDX_QRTZ_J_GRP ON QRTZ_JOB_DETAILS(SCHED_NAME,JOB_GROUP);

CREATE INDEX IDX_QRTZ_T_J ON QRTZ_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_T_JG ON QRTZ_TRIGGERS(SCHED_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_T_C ON QRTZ_TRIGGERS(SCHED_NAME,CALENDAR_NAME);
CREATE INDEX IDX_QRTZ_T_G ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ_T_STATE ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_N_STATE ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_N_G_STATE ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_NEXT_FIRE_TIME ON QRTZ_TRIGGERS(SCHED_NAME,NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_ST ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_MISFIRE ON QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_ST_MISFIRE ON QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_NFT_ST_MISFIRE_GRP ON QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE);

CREATE INDEX IDX_QRTZ_FT_TRIG_INST_NAME ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME);
CREATE INDEX IDX_QRTZ_FT_INST_JOB_REQ_RCVRY ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY);
CREATE INDEX IDX_QRTZ_FT_J_G ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_FT_JG ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_FT_T_G ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ_FT_TG ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);


--
-- Table structure for table t_ds_access_token
--

DROP TABLE IF EXISTS t_ds_access_token;
CREATE TABLE t_ds_access_token (
                                   id int NOT NULL  ,
                                   user_id int DEFAULT NULL ,
                                   token varchar(64) DEFAULT NULL ,
                                   expire_time timestamp DEFAULT NULL ,
                                   create_time timestamp DEFAULT NULL ,
                                   update_time timestamp DEFAULT NULL ,
                                   PRIMARY KEY (id)
) ;

--
-- Table structure for table t_ds_alert
--

DROP TABLE IF EXISTS t_ds_alert;
CREATE TABLE t_ds_alert (
                            id int NOT NULL  ,
                            title varchar(64) DEFAULT NULL ,
                            sign varchar(40) NOT NULL DEFAULT '',
                            content text ,
                            alert_status int DEFAULT '0' ,
                            warning_type int DEFAULT '2' ,
                            log text ,
                            alertgroup_id int DEFAULT NULL ,
                            create_time timestamp DEFAULT NULL ,
                            update_time timestamp DEFAULT NULL ,
                            project_code bigint DEFAULT NULL,
                            process_definition_code bigint DEFAULT NULL,
                            process_instance_id int DEFAULT NULL ,
                            alert_type int DEFAULT NULL ,
                            PRIMARY KEY (id)
);
comment on column t_ds_alert.sign is 'sign=sha1(content)';

create index idx_status on t_ds_alert (alert_status);
create index idx_sign on t_ds_alert (sign);

--
-- Table structure for table t_ds_alertgroup
--

DROP TABLE IF EXISTS t_ds_alertgroup;
CREATE TABLE t_ds_alertgroup(
                                id             int NOT NULL,
                                alert_instance_ids varchar (255) DEFAULT NULL,
                                create_user_id int4         DEFAULT NULL,
                                group_name     varchar(255) DEFAULT NULL,
                                description    varchar(255) DEFAULT NULL,
                                create_time    timestamp    DEFAULT NULL,
                                update_time    timestamp    DEFAULT NULL,
                                PRIMARY KEY (id),
                                CONSTRAINT t_ds_alertgroup_name_un UNIQUE (group_name)
) ;

--
-- Table structure for table t_ds_command
--

DROP TABLE IF EXISTS t_ds_command;
CREATE TABLE t_ds_command (
                              id int NOT NULL  ,
                              command_type              int DEFAULT NULL ,
                              process_definition_code   bigint NOT NULL ,
                              command_param             text ,
                              task_depend_type          int DEFAULT NULL ,
                              failure_strategy          int DEFAULT '0' ,
                              warning_type              int DEFAULT '0' ,
                              warning_group_id          int DEFAULT NULL ,
                              schedule_time             timestamp DEFAULT NULL ,
                              start_time                timestamp DEFAULT NULL ,
                              executor_id               int DEFAULT NULL ,
                              update_time               timestamp DEFAULT NULL ,
                              process_instance_priority int DEFAULT NULL ,
                              worker_group              varchar(64),
                              environment_code          bigint DEFAULT '-1',
                              dry_run                   int DEFAULT '0' ,
                              process_instance_id       int DEFAULT 0,
                              process_definition_version int DEFAULT 0,
                              PRIMARY KEY (id)
) ;

create index priority_id_index on t_ds_command (process_instance_priority,id);

--
-- Table structure for table t_ds_datasource
--

DROP TABLE IF EXISTS t_ds_datasource;
CREATE TABLE t_ds_datasource (
                                 id int NOT NULL  ,
                                 name varchar(64) NOT NULL ,
                                 note varchar(255) DEFAULT NULL ,
                                 type int NOT NULL ,
                                 user_id int NOT NULL ,
                                 connection_params text NOT NULL ,
                                 create_time timestamp NOT NULL ,
                                 update_time timestamp DEFAULT NULL ,
                                 PRIMARY KEY (id),
                                 CONSTRAINT t_ds_datasource_name_un UNIQUE (name, type)
) ;

--
-- Table structure for table t_ds_error_command
--

DROP TABLE IF EXISTS t_ds_error_command;
CREATE TABLE t_ds_error_command (
                                    id int NOT NULL  ,
                                    command_type              int DEFAULT NULL ,
                                    process_definition_code   bigint NOT NULL ,
                                    command_param             text ,
                                    task_depend_type          int DEFAULT NULL ,
                                    failure_strategy          int DEFAULT '0' ,
                                    warning_type              int DEFAULT '0' ,
                                    warning_group_id          int DEFAULT NULL ,
                                    schedule_time             timestamp DEFAULT NULL ,
                                    start_time                timestamp DEFAULT NULL ,
                                    executor_id               int DEFAULT NULL ,
                                    update_time               timestamp DEFAULT NULL ,
                                    process_instance_priority int DEFAULT NULL ,
                                    worker_group              varchar(64),
                                    environment_code          bigint DEFAULT '-1',
                                    dry_run                   int DEFAULT '0' ,
                                    message                   text ,
                                    process_instance_id       int DEFAULT 0,
                                    process_definition_version int DEFAULT 0,
                                    PRIMARY KEY (id)
);

--
-- Table structure for table t_ds_process_definition
--

DROP TABLE IF EXISTS t_ds_process_definition;
CREATE TABLE t_ds_process_definition (
                                         id int NOT NULL  ,
                                         code bigint NOT NULL,
                                         name varchar(255) DEFAULT NULL ,
                                         version int NOT NULL ,
                                         description text ,
                                         project_code bigint DEFAULT NULL ,
                                         release_state int DEFAULT NULL ,
                                         user_id int DEFAULT NULL ,
                                         global_params text ,
                                         locations text ,
                                         warning_group_id int DEFAULT NULL ,
                                         flag int DEFAULT NULL ,
                                         timeout int DEFAULT '0' ,
                                         tenant_id int DEFAULT '-1' ,
                                         execution_type int DEFAULT '0',
                                         create_time timestamp DEFAULT NULL ,
                                         update_time timestamp DEFAULT NULL ,
                                         PRIMARY KEY (id) ,
                                         CONSTRAINT process_definition_unique UNIQUE (name, project_code)
) ;

create index process_definition_index on t_ds_process_definition (code,id);

--
-- Table structure for table t_ds_process_definition_log
--

DROP TABLE IF EXISTS t_ds_process_definition_log;
CREATE TABLE t_ds_process_definition_log (
                                             id int NOT NULL  ,
                                             code bigint NOT NULL,
                                             name varchar(255) DEFAULT NULL ,
                                             version int NOT NULL ,
                                             description text ,
                                             project_code bigint DEFAULT NULL ,
                                             release_state int DEFAULT NULL ,
                                             user_id int DEFAULT NULL ,
                                             global_params text ,
                                             locations text ,
                                             warning_group_id int DEFAULT NULL ,
                                             flag int DEFAULT NULL ,
                                             timeout int DEFAULT '0' ,
                                             tenant_id int DEFAULT '-1' ,
                                             execution_type int DEFAULT '0',
                                             operator int DEFAULT NULL ,
                                             operate_time timestamp DEFAULT NULL ,
                                             create_time timestamp DEFAULT NULL ,
                                             update_time timestamp DEFAULT NULL ,
                                             PRIMARY KEY (id)
) ;

--
-- Table structure for table t_ds_task_definition
--

DROP TABLE IF EXISTS t_ds_task_definition;
CREATE TABLE t_ds_task_definition (
                                      id int NOT NULL  ,
                                      code bigint NOT NULL,
                                      name varchar(255) DEFAULT NULL ,
                                      version int NOT NULL ,
                                      description text ,
                                      project_code bigint DEFAULT NULL ,
                                      user_id int DEFAULT NULL ,
                                      task_type varchar(50) DEFAULT NULL ,
                                      task_params text ,
                                      flag int DEFAULT NULL ,
                                      task_priority int DEFAULT NULL ,
                                      worker_group varchar(255) DEFAULT NULL ,
                                      environment_code bigint DEFAULT '-1',
                                      fail_retry_times int DEFAULT NULL ,
                                      fail_retry_interval int DEFAULT NULL ,
                                      timeout_flag int DEFAULT NULL ,
                                      timeout_notify_strategy int DEFAULT NULL ,
                                      timeout int DEFAULT '0' ,
                                      delay_time int DEFAULT '0' ,
                                      task_group_id int DEFAULT NULL,
                                      task_group_priority int DEFAULT '0',
                                      resource_ids text ,
                                      create_time timestamp DEFAULT NULL ,
                                      update_time timestamp DEFAULT NULL ,
                                      PRIMARY KEY (id)
) ;

create index task_definition_index on t_ds_task_definition (project_code,id);

--
-- Table structure for table t_ds_task_definition_log
--

DROP TABLE IF EXISTS t_ds_task_definition_log;
CREATE TABLE t_ds_task_definition_log (
                                          id int NOT NULL  ,
                                          code bigint NOT NULL,
                                          name varchar(255) DEFAULT NULL ,
                                          version int NOT NULL ,
                                          description text ,
                                          project_code bigint DEFAULT NULL ,
                                          user_id int DEFAULT NULL ,
                                          task_type varchar(50) DEFAULT NULL ,
                                          task_params text ,
                                          flag int DEFAULT NULL ,
                                          task_priority int DEFAULT NULL ,
                                          worker_group varchar(255) DEFAULT NULL ,
                                          environment_code bigint DEFAULT '-1',
                                          fail_retry_times int DEFAULT NULL ,
                                          fail_retry_interval int DEFAULT NULL ,
                                          timeout_flag int DEFAULT NULL ,
                                          timeout_notify_strategy int DEFAULT NULL ,
                                          timeout int DEFAULT '0' ,
                                          delay_time int DEFAULT '0' ,
                                          resource_ids text ,
                                          operator int DEFAULT NULL ,
                                          task_group_id int DEFAULT NULL,
                                          task_group_priority int DEFAULT '0',
                                          operate_time timestamp DEFAULT NULL ,
                                          create_time timestamp DEFAULT NULL ,
                                          update_time timestamp DEFAULT NULL ,
                                          PRIMARY KEY (id)
) ;

create index idx_task_definition_log_code_version on t_ds_task_definition_log (code,version);
create index idx_task_definition_log_project_code on t_ds_task_definition_log (project_code);

--
-- Table structure for table t_ds_process_task_relation
--

DROP TABLE IF EXISTS t_ds_process_task_relation;
CREATE TABLE t_ds_process_task_relation (
                                            id int NOT NULL  ,
                                            name varchar(255) DEFAULT NULL ,
                                            project_code bigint DEFAULT NULL ,
                                            process_definition_code bigint DEFAULT NULL ,
                                            process_definition_version int DEFAULT NULL ,
                                            pre_task_code bigint DEFAULT NULL ,
                                            pre_task_version int DEFAULT '0' ,
                                            post_task_code bigint DEFAULT NULL ,
                                            post_task_version int DEFAULT '0' ,
                                            condition_type int DEFAULT NULL ,
                                            condition_params text ,
                                            create_time timestamp DEFAULT NULL ,
                                            update_time timestamp DEFAULT NULL ,
                                            PRIMARY KEY (id)
) ;

create index process_task_relation_idx_project_code_process_definition_code on t_ds_process_task_relation (project_code,process_definition_code);
create index process_task_relation_idx_pre_task_code_version on t_ds_process_task_relation (pre_task_code, pre_task_version);
create index process_task_relation_idx_post_task_code_version on t_ds_process_task_relation (post_task_code, post_task_version);

--
-- Table structure for table t_ds_process_task_relation_log
--

DROP TABLE IF EXISTS t_ds_process_task_relation_log;
CREATE TABLE t_ds_process_task_relation_log (
                                                id int NOT NULL  ,
                                                name varchar(255) DEFAULT NULL ,
                                                project_code bigint DEFAULT NULL ,
                                                process_definition_code bigint DEFAULT NULL ,
                                                process_definition_version int DEFAULT NULL ,
                                                pre_task_code bigint DEFAULT NULL ,
                                                pre_task_version int DEFAULT '0' ,
                                                post_task_code bigint DEFAULT NULL ,
                                                post_task_version int DEFAULT '0' ,
                                                condition_type int DEFAULT NULL ,
                                                condition_params text ,
                                                operator int DEFAULT NULL ,
                                                operate_time timestamp DEFAULT NULL ,
                                                create_time timestamp DEFAULT NULL ,
                                                update_time timestamp DEFAULT NULL ,
                                                PRIMARY KEY (id)
) ;

create index process_task_relation_log_idx_project_code_process_definition_code on t_ds_process_task_relation_log (project_code,process_definition_code);

--
-- Table structure for table t_ds_process_instance
--

DROP TABLE IF EXISTS t_ds_process_instance;
CREATE TABLE t_ds_process_instance (
                                       id int NOT NULL  ,
                                       name varchar(255) DEFAULT NULL ,
                                       process_definition_code bigint DEFAULT NULL ,
                                       process_definition_version int DEFAULT NULL ,
                                       state int DEFAULT NULL ,
                                       recovery int DEFAULT NULL ,
                                       start_time timestamp DEFAULT NULL ,
                                       end_time timestamp DEFAULT NULL ,
                                       run_times int DEFAULT NULL ,
                                       host varchar(135) DEFAULT NULL ,
                                       command_type int DEFAULT NULL ,
                                       command_param text ,
                                       task_depend_type int DEFAULT NULL ,
                                       max_try_times int DEFAULT '0' ,
                                       failure_strategy int DEFAULT '0' ,
                                       warning_type int DEFAULT '0' ,
                                       warning_group_id int DEFAULT NULL ,
                                       schedule_time timestamp DEFAULT NULL ,
                                       command_start_time timestamp DEFAULT NULL ,
                                       global_params text ,
                                       process_instance_json text ,
                                       flag int DEFAULT '1' ,
                                       update_time timestamp NULL ,
                                       is_sub_process int DEFAULT '0' ,
                                       executor_id int NOT NULL ,
                                       history_cmd text ,
                                       dependence_schedule_times text ,
                                       process_instance_priority int DEFAULT NULL ,
                                       worker_group varchar(64) ,
                                       environment_code bigint DEFAULT '-1',
                                       timeout int DEFAULT '0' ,
                                       tenant_id int NOT NULL DEFAULT '-1' ,
                                       var_pool text ,
                                       dry_run int DEFAULT '0' ,
                                       next_process_instance_id int DEFAULT '0',
                                       restart_time timestamp DEFAULT NULL ,
                                       PRIMARY KEY (id)
) ;

create index process_instance_index on t_ds_process_instance (process_definition_code,id);
create index start_time_index on t_ds_process_instance (start_time,end_time);

--
-- Table structure for table t_ds_project
--

DROP TABLE IF EXISTS t_ds_project;
CREATE TABLE t_ds_project (
                              id int NOT NULL  ,
                              name varchar(100) DEFAULT NULL ,
                              code bigint NOT NULL,
                              description varchar(200) DEFAULT NULL ,
                              user_id int DEFAULT NULL ,
                              flag int DEFAULT '1' ,
                              create_time timestamp DEFAULT CURRENT_TIMESTAMP ,
                              update_time timestamp DEFAULT CURRENT_TIMESTAMP ,
                              PRIMARY KEY (id)
) ;

create index user_id_index on t_ds_project (user_id);
CREATE UNIQUE INDEX unique_name on t_ds_project (name);
CREATE UNIQUE INDEX unique_code on t_ds_project (code);

--
-- Table structure for table t_ds_queue
--

DROP TABLE IF EXISTS t_ds_queue;
CREATE TABLE t_ds_queue (
                            id int NOT NULL  ,
                            queue_name varchar(64) DEFAULT NULL ,
                            queue varchar(64) DEFAULT NULL ,
                            create_time timestamp DEFAULT NULL ,
                            update_time timestamp DEFAULT NULL ,
                            PRIMARY KEY (id)
);
-- add unique key to t_ds_queue
CREATE UNIQUE INDEX unique_queue_name on t_ds_queue (queue_name);

--
-- Table structure for table t_ds_relation_datasource_user
--

DROP TABLE IF EXISTS t_ds_relation_datasource_user;
CREATE TABLE t_ds_relation_datasource_user (
                                               id int NOT NULL  ,
                                               user_id int NOT NULL ,
                                               datasource_id int DEFAULT NULL ,
                                               perm int DEFAULT '1' ,
                                               create_time timestamp DEFAULT NULL ,
                                               update_time timestamp DEFAULT NULL ,
                                               PRIMARY KEY (id)
) ;

--
-- Table structure for table t_ds_relation_process_instance
--

DROP TABLE IF EXISTS t_ds_relation_process_instance;
CREATE TABLE t_ds_relation_process_instance (
                                                id int NOT NULL  ,
                                                parent_process_instance_id int DEFAULT NULL ,
                                                parent_task_instance_id int DEFAULT NULL ,
                                                process_instance_id int DEFAULT NULL ,
                                                PRIMARY KEY (id)
) ;


--
-- Table structure for table t_ds_relation_project_user
--

DROP TABLE IF EXISTS t_ds_relation_project_user;
CREATE TABLE t_ds_relation_project_user (
                                            id int NOT NULL  ,
                                            user_id int NOT NULL ,
                                            project_id int DEFAULT NULL ,
                                            perm int DEFAULT '1' ,
                                            create_time timestamp DEFAULT NULL ,
                                            update_time timestamp DEFAULT NULL ,
                                            PRIMARY KEY (id),
                                            CONSTRAINT t_ds_relation_project_user_un UNIQUE (user_id, project_id)
) ;

--
-- Table structure for table t_ds_relation_resources_user
--

DROP TABLE IF EXISTS t_ds_relation_resources_user;
CREATE TABLE t_ds_relation_resources_user (
                                              id int NOT NULL ,
                                              user_id int NOT NULL ,
                                              resources_id int DEFAULT NULL ,
                                              perm int DEFAULT '1' ,
                                              create_time timestamp DEFAULT NULL ,
                                              update_time timestamp DEFAULT NULL ,
                                              PRIMARY KEY (id)
) ;

--
-- Table structure for table t_ds_relation_udfs_user
--

DROP TABLE IF EXISTS t_ds_relation_udfs_user;
CREATE TABLE t_ds_relation_udfs_user (
                                         id int NOT NULL  ,
                                         user_id int NOT NULL ,
                                         udf_id int DEFAULT NULL ,
                                         perm int DEFAULT '1' ,
                                         create_time timestamp DEFAULT NULL ,
                                         update_time timestamp DEFAULT NULL ,
                                         PRIMARY KEY (id)
) ;
;

--
-- Table structure for table t_ds_resources
--

DROP TABLE IF EXISTS t_ds_resources;
CREATE TABLE t_ds_resources (
                                id int NOT NULL  ,
                                alias varchar(64) DEFAULT NULL ,
                                file_name varchar(64) DEFAULT NULL ,
                                description varchar(255) DEFAULT NULL ,
                                user_id int DEFAULT NULL ,
                                type int DEFAULT NULL ,
                                size bigint DEFAULT NULL ,
                                create_time timestamp DEFAULT NULL ,
                                update_time timestamp DEFAULT NULL ,
                                pid int,
                                full_name varchar(128),
                                is_directory boolean DEFAULT FALSE,
                                PRIMARY KEY (id),
                                CONSTRAINT t_ds_resources_un UNIQUE (full_name, type)
) ;


--
-- Table structure for table t_ds_schedules
--

DROP TABLE IF EXISTS t_ds_schedules;
CREATE TABLE t_ds_schedules (
                                id int NOT NULL ,
                                process_definition_code bigint NOT NULL ,
                                start_time timestamp NOT NULL ,
                                end_time timestamp NOT NULL ,
                                timezone_id varchar(40) default NULL ,
                                crontab varchar(255) NOT NULL ,
                                failure_strategy int NOT NULL ,
                                user_id int NOT NULL ,
                                release_state int NOT NULL ,
                                warning_type int NOT NULL ,
                                warning_group_id int DEFAULT NULL ,
                                process_instance_priority int DEFAULT NULL ,
                                worker_group varchar(64),
                                environment_code bigint DEFAULT '-1',
                                create_time timestamp NOT NULL ,
                                update_time timestamp NOT NULL ,
                                PRIMARY KEY (id)
);

--
-- Table structure for table t_ds_session
--

DROP TABLE IF EXISTS t_ds_session;
CREATE TABLE t_ds_session (
                              id varchar(64) NOT NULL ,
                              user_id int DEFAULT NULL ,
                              ip varchar(45) DEFAULT NULL ,
                              last_login_time timestamp DEFAULT NULL ,
                              PRIMARY KEY (id)
);

--
-- Table structure for table t_ds_task_instance
--

DROP TABLE IF EXISTS t_ds_task_instance;
CREATE TABLE t_ds_task_instance (
                                    id int NOT NULL  ,
                                    name varchar(255) DEFAULT NULL ,
                                    task_type varchar(50) DEFAULT NULL ,
                                    task_code bigint NOT NULL,
                                    task_definition_version int DEFAULT NULL ,
                                    process_instance_id int DEFAULT NULL ,
                                    state int DEFAULT NULL ,
                                    submit_time timestamp DEFAULT NULL ,
                                    start_time timestamp DEFAULT NULL ,
                                    end_time timestamp DEFAULT NULL ,
                                    host varchar(135) DEFAULT NULL ,
                                    execute_path varchar(200) DEFAULT NULL ,
                                    log_path text DEFAULT NULL ,
                                    alert_flag int DEFAULT NULL ,
                                    retry_times int DEFAULT '0' ,
                                    pid int DEFAULT NULL ,
                                    app_link text ,
                                    task_params text ,
                                    flag int DEFAULT '1' ,
                                    retry_interval int DEFAULT NULL ,
                                    max_retry_times int DEFAULT NULL ,
                                    task_instance_priority int DEFAULT NULL ,
                                    worker_group varchar(64),
                                    environment_code bigint DEFAULT '-1',
                                    environment_config text,
                                    executor_id int DEFAULT NULL ,
                                    first_submit_time timestamp DEFAULT NULL ,
                                    delay_time int DEFAULT '0' ,
                                    task_group_id int DEFAULT NULL,
                                    var_pool text ,
                                    dry_run int DEFAULT '0' ,
                                    PRIMARY KEY (id),
                                    CONSTRAINT foreign_key_instance_id FOREIGN KEY(process_instance_id) REFERENCES t_ds_process_instance(id) ON DELETE CASCADE
) ;

create index idx_task_instance_code_version on t_ds_task_instance (task_code, task_definition_version);

--
-- Table structure for table t_ds_tenant
--

DROP TABLE IF EXISTS t_ds_tenant;
CREATE TABLE t_ds_tenant (
                             id int NOT NULL  ,
                             tenant_code varchar(64) DEFAULT NULL ,
                             description varchar(255) DEFAULT NULL ,
                             queue_id int DEFAULT NULL ,
                             create_time timestamp DEFAULT NULL ,
                             update_time timestamp DEFAULT NULL ,
                             PRIMARY KEY (id)
) ;
-- add unique key to t_ds_tenant
CREATE UNIQUE INDEX unique_tenant_code on t_ds_tenant (tenant_code);

--
-- Table structure for table t_ds_udfs
--

DROP TABLE IF EXISTS t_ds_udfs;
CREATE TABLE t_ds_udfs (
                           id int NOT NULL  ,
                           user_id int NOT NULL ,
                           func_name varchar(100) NOT NULL ,
                           class_name varchar(255) NOT NULL ,
                           type int NOT NULL ,
                           arg_types varchar(255) DEFAULT NULL ,
                           database varchar(255) DEFAULT NULL ,
                           description varchar(255) DEFAULT NULL ,
                           resource_id int NOT NULL ,
                           resource_name varchar(255) NOT NULL ,
                           create_time timestamp NOT NULL ,
                           update_time timestamp NOT NULL ,
                           PRIMARY KEY (id)
) ;
-- add unique key to t_ds_udfs
CREATE UNIQUE INDEX unique_func_name on t_ds_udfs (func_name);

--
-- Table structure for table t_ds_user
--

DROP TABLE IF EXISTS t_ds_user;
CREATE TABLE t_ds_user (
                           id int NOT NULL  ,
                           user_name varchar(64) DEFAULT NULL ,
                           user_password varchar(64) DEFAULT NULL ,
                           user_type int DEFAULT NULL ,
                           email varchar(64) DEFAULT NULL ,
                           phone varchar(11) DEFAULT NULL ,
                           tenant_id int DEFAULT NULL ,
                           create_time timestamp DEFAULT NULL ,
                           update_time timestamp DEFAULT NULL ,
                           queue varchar(64) DEFAULT NULL ,
                           state int DEFAULT 1 ,
                           time_zone varchar(32) DEFAULT NULL,
                           PRIMARY KEY (id)
);
comment on column t_ds_user.state is 'state 0:disable 1:enable';

--
-- Table structure for table t_ds_version
--

DROP TABLE IF EXISTS t_ds_version;
CREATE TABLE t_ds_version (
                              id int NOT NULL ,
                              version varchar(200) NOT NULL,
                              PRIMARY KEY (id)
) ;
create index version_index on t_ds_version(version);

--
-- Table structure for table t_ds_worker_group
--

DROP TABLE IF EXISTS t_ds_worker_group;
CREATE TABLE t_ds_worker_group (
                                   id bigint NOT NULL  ,
                                   name varchar(255) NOT NULL ,
                                   addr_list text DEFAULT NULL ,
                                   create_time timestamp DEFAULT NULL ,
                                   update_time timestamp DEFAULT NULL ,
                                   PRIMARY KEY (id) ,
                                   CONSTRAINT name_unique UNIQUE (name)
) ;

DROP SEQUENCE IF EXISTS t_ds_access_token_id_sequence;
CREATE SEQUENCE  t_ds_access_token_id_sequence;
ALTER TABLE t_ds_access_token ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_access_token_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_alert_id_sequence;
CREATE SEQUENCE  t_ds_alert_id_sequence;
ALTER TABLE t_ds_alert ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_alert_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_alertgroup_id_sequence;
CREATE SEQUENCE  t_ds_alertgroup_id_sequence;
ALTER TABLE t_ds_alertgroup ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_alertgroup_id_sequence');

DROP SEQUENCE IF EXISTS t_ds_command_id_sequence;
CREATE SEQUENCE  t_ds_command_id_sequence;
ALTER TABLE t_ds_command ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_command_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_datasource_id_sequence;
CREATE SEQUENCE  t_ds_datasource_id_sequence;
ALTER TABLE t_ds_datasource ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_datasource_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_process_definition_id_sequence;
CREATE SEQUENCE  t_ds_process_definition_id_sequence;
ALTER TABLE t_ds_process_definition ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_process_definition_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_process_definition_log_id_sequence;
CREATE SEQUENCE  t_ds_process_definition_log_id_sequence;
ALTER TABLE t_ds_process_definition_log ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_process_definition_log_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_task_definition_id_sequence;
CREATE SEQUENCE  t_ds_task_definition_id_sequence;
ALTER TABLE t_ds_task_definition ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_task_definition_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_task_definition_log_id_sequence;
CREATE SEQUENCE  t_ds_task_definition_log_id_sequence;
ALTER TABLE t_ds_task_definition_log ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_task_definition_log_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_process_task_relation_id_sequence;
CREATE SEQUENCE  t_ds_process_task_relation_id_sequence;
ALTER TABLE t_ds_process_task_relation ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_process_task_relation_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_process_task_relation_log_id_sequence;
CREATE SEQUENCE  t_ds_process_task_relation_log_id_sequence;
ALTER TABLE t_ds_process_task_relation_log ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_process_task_relation_log_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_process_instance_id_sequence;
CREATE SEQUENCE  t_ds_process_instance_id_sequence;
ALTER TABLE t_ds_process_instance ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_process_instance_id_sequence');

DROP SEQUENCE IF EXISTS t_ds_project_id_sequence;
CREATE SEQUENCE  t_ds_project_id_sequence;
ALTER TABLE t_ds_project ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_project_id_sequence');

DROP SEQUENCE IF EXISTS t_ds_queue_id_sequence;
CREATE SEQUENCE  t_ds_queue_id_sequence;
ALTER TABLE t_ds_queue ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_queue_id_sequence');

DROP SEQUENCE IF EXISTS t_ds_relation_datasource_user_id_sequence;
CREATE SEQUENCE  t_ds_relation_datasource_user_id_sequence;
ALTER TABLE t_ds_relation_datasource_user ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_relation_datasource_user_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_relation_process_instance_id_sequence;
CREATE SEQUENCE  t_ds_relation_process_instance_id_sequence;
ALTER TABLE t_ds_relation_process_instance ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_relation_process_instance_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_relation_project_user_id_sequence;
CREATE SEQUENCE  t_ds_relation_project_user_id_sequence;
ALTER TABLE t_ds_relation_project_user ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_relation_project_user_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_relation_resources_user_id_sequence;
CREATE SEQUENCE  t_ds_relation_resources_user_id_sequence;
ALTER TABLE t_ds_relation_resources_user ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_relation_resources_user_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_relation_udfs_user_id_sequence;
CREATE SEQUENCE  t_ds_relation_udfs_user_id_sequence;
ALTER TABLE t_ds_relation_udfs_user ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_relation_udfs_user_id_sequence');

DROP SEQUENCE IF EXISTS t_ds_resources_id_sequence;
CREATE SEQUENCE  t_ds_resources_id_sequence;
ALTER TABLE t_ds_resources ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_resources_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_schedules_id_sequence;
CREATE SEQUENCE  t_ds_schedules_id_sequence;
ALTER TABLE t_ds_schedules ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_schedules_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_task_instance_id_sequence;
CREATE SEQUENCE  t_ds_task_instance_id_sequence;
ALTER TABLE t_ds_task_instance ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_task_instance_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_tenant_id_sequence;
CREATE SEQUENCE  t_ds_tenant_id_sequence;
ALTER TABLE t_ds_tenant ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_tenant_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_udfs_id_sequence;
CREATE SEQUENCE  t_ds_udfs_id_sequence;
ALTER TABLE t_ds_udfs ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_udfs_id_sequence');
DROP SEQUENCE IF EXISTS t_ds_user_id_sequence;
CREATE SEQUENCE  t_ds_user_id_sequence;
ALTER TABLE t_ds_user ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_user_id_sequence');

DROP SEQUENCE IF EXISTS t_ds_version_id_sequence;
CREATE SEQUENCE  t_ds_version_id_sequence;
ALTER TABLE t_ds_version ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_version_id_sequence');

DROP SEQUENCE IF EXISTS t_ds_worker_group_id_sequence;
CREATE SEQUENCE  t_ds_worker_group_id_sequence;
ALTER TABLE t_ds_worker_group ALTER COLUMN id SET DEFAULT NEXTVAL('t_ds_worker_group_id_sequence');

-- Records of t_ds_user?user : admin , password : dolphinscheduler123
INSERT INTO t_ds_user(user_name, user_password, user_type, email, phone, tenant_id, state, create_time, update_time, time_zone)
VALUES ('admin', '7ad2410b2f4c074479a8937a28a22b8f', '0', 'xxx@qq.com', '', '0', 1, '2018-03-27 15:48:50', '2018-10-24 17:40:22', null);

-- Records of t_ds_alertgroup, default admin warning group
INSERT INTO t_ds_alertgroup(alert_instance_ids, create_user_id, group_name, description, create_time, update_time)
VALUES ('1,2', 1, 'default admin warning group', 'default admin warning group', '2018-11-29 10:20:39', '2018-11-29 10:20:39');

-- Records of t_ds_queue,default queue name : default
INSERT INTO t_ds_queue(queue_name, queue, create_time, update_time)
VALUES ('default', 'default', '2018-11-29 10:22:33', '2018-11-29 10:22:33');

-- Records of t_ds_queue,default queue name : default
INSERT INTO t_ds_version(version) VALUES ('3.0.0');

--
-- Table structure for table t_ds_plugin_define
--

DROP TABLE IF EXISTS t_ds_plugin_define;
CREATE TABLE t_ds_plugin_define (
                                    id serial NOT NULL,
                                    plugin_name varchar(100) NOT NULL,
                                    plugin_type varchar(100) NOT NULL,
                                    plugin_params text NULL,
                                    create_time timestamp NULL,
                                    update_time timestamp NULL,
                                    CONSTRAINT t_ds_plugin_define_pk PRIMARY KEY (id),
                                    CONSTRAINT t_ds_plugin_define_un UNIQUE (plugin_name, plugin_type)
);

--
-- Table structure for table t_ds_alert_plugin_instance
--

DROP TABLE IF EXISTS t_ds_alert_plugin_instance;
CREATE TABLE t_ds_alert_plugin_instance (
                                            id serial NOT NULL,
                                            plugin_define_id int4 NOT NULL,
                                            plugin_instance_params text NULL,
                                            create_time timestamp NULL,
                                            update_time timestamp NULL,
                                            instance_name varchar(200) NULL,
                                            CONSTRAINT t_ds_alert_plugin_instance_pk PRIMARY KEY (id)
);

--
-- Table structure for table t_ds_dq_comparison_type
--
DROP TABLE IF EXISTS t_ds_dq_comparison_type;
CREATE TABLE t_ds_dq_comparison_type (
                                         id serial NOT NULL,
                                         "type" varchar NOT NULL,
                                         execute_sql varchar NULL,
                                         output_table varchar NULL,
                                         "name" varchar NULL,
                                         create_time timestamp NULL,
                                         update_time timestamp NULL,
                                         is_inner_source bool NULL,
                                         CONSTRAINT t_ds_dq_comparison_type_pk PRIMARY KEY (id)
);
INSERT INTO t_ds_dq_comparison_type
(id, "type", execute_sql, output_table, "name", create_time, update_time, is_inner_source)
VALUES(1, 'FixValue', NULL, NULL, NULL, '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', false);
INSERT INTO t_ds_dq_comparison_type
(id, "type", execute_sql, output_table, "name", create_time, update_time, is_inner_source)
VALUES(2, 'DailyAvg', 'select round(avg(statistics_value),2) as day_avg from t_ds_dq_task_statistics_value where data_time >=date_trunc(''DAY'', ${data_time}) and data_time < date_add(date_trunc(''day'', ${data_time}),1) and unique_code = ${unique_code} and statistics_name = ''${statistics_name}''', 'day_range', 'day_range.day_avg', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', true);
INSERT INTO t_ds_dq_comparison_type
(id, "type", execute_sql, output_table, "name", create_time, update_time, is_inner_source)
VALUES(3, 'WeeklyAvg', 'select round(avg(statistics_value),2) as week_avg from t_ds_dq_task_statistics_value where  data_time >= date_trunc(''WEEK'', ${data_time}) and data_time <date_trunc(''day'', ${data_time}) and unique_code = ${unique_code} and statistics_name = ''${statistics_name}''', 'week_range', 'week_range.week_avg', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', true);
INSERT INTO t_ds_dq_comparison_type
(id, "type", execute_sql, output_table, "name", create_time, update_time, is_inner_source)
VALUES(4, 'MonthlyAvg', 'select round(avg(statistics_value),2) as month_avg from t_ds_dq_task_statistics_value where  data_time >= date_trunc(''MONTH'', ${data_time}) and data_time <date_trunc(''day'', ${data_time}) and unique_code = ${unique_code} and statistics_name = ''${statistics_name}''', 'month_range', 'month_range.month_avg', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', true);
INSERT INTO t_ds_dq_comparison_type
(id, "type", execute_sql, output_table, "name", create_time, update_time, is_inner_source)
VALUES(5, 'Last7DayAvg', 'select round(avg(statistics_value),2) as last_7_avg from t_ds_dq_task_statistics_value where  data_time >= date_add(date_trunc(''day'', ${data_time}),-7) and  data_time <date_trunc(''day'', ${data_time}) and unique_code = ${unique_code} and statistics_name = ''${statistics_name}''', 'last_seven_days', 'last_seven_days.last_7_avg', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', true);
INSERT INTO t_ds_dq_comparison_type
(id, "type", execute_sql, output_table, "name", create_time, update_time, is_inner_source)
VALUES(6, 'Last30DayAvg', 'select round(avg(statistics_value),2) as last_30_avg from t_ds_dq_task_statistics_value where  data_time >= date_add(date_trunc(''day'', ${data_time}),-30) and  data_time < date_trunc(''day'', ${data_time}) and unique_code = ${unique_code} and statistics_name = ''${statistics_name}''', 'last_thirty_days', 'last_thirty_days.last_30_avg', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', true);
INSERT INTO t_ds_dq_comparison_type
(id, "type", execute_sql, output_table, "name", create_time, update_time, is_inner_source)
VALUES(7, 'SrcTableTotalRows', 'SELECT COUNT(*) AS total FROM ${src_table} WHERE (${src_filter})', 'total_count', 'total_count.total', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', false);
INSERT INTO t_ds_dq_comparison_type
(id, "type", execute_sql, output_table, "name", create_time, update_time, is_inner_source)
VALUES(8, 'TargetTableTotalRows', 'SELECT COUNT(*) AS total FROM ${target_table} WHERE (${target_filter})', 'total_count', 'total_count.total', '2021-06-30 00:00:00.000', '2021-06-30 00:00:00.000', false);

--
-- Table structure for table t_ds_dq_execute_result
--
DROP TABLE IF EXISTS t_ds_dq_execute_result;
CREATE TABLE t_ds_dq_execute_result (
                                        id serial NOT NULL,
                                        process_definition_id int4 NULL,
                                        process_instance_id int4 NULL,
                                        task_instance_id int4 NULL,
                                        rule_type int4 NULL,
                                        rule_name varchar(255) DEFAULT NULL,
                                        statistics_value float8 NULL,
                                        comparison_value float8 NULL,
                                        check_type int4 NULL,
                                        threshold float8 NULL,
                                        "operator" int4 NULL,
                                        failure_strategy int4 NULL,
                                        state int4 NULL,
                                        user_id int4 NULL,
                                        create_time timestamp NULL,
                                        update_time timestamp NULL,
                                        comparison_type int4 NULL,
                                        error_output_path text NULL,
                                        CONSTRAINT t_ds_dq_execute_result_pk PRIMARY KEY (id)
);

--
-- Table structure for table t_ds_dq_rule
--
DROP TABLE IF EXISTS t_ds_dq_rule;
CREATE TABLE t_ds_dq_rule (
                              id serial NOT NULL,
                              "name" varchar(100) DEFAULT NULL,
                              "type" int4 NULL,
                              user_id int4 NULL,
                              create_time timestamp NULL,
                              update_time timestamp NULL,
                              CONSTRAINT t_ds_dq_rule_pk PRIMARY KEY (id)
);
INSERT INTO t_ds_dq_rule
(id, "name", "type", user_id, create_time, update_time)
VALUES(1, '$t(null_check)', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO t_ds_dq_rule
(id, "name", "type", user_id, create_time, update_time)
VALUES(2, '$t(custom_sql)', 1, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO t_ds_dq_rule
(id, "name", "type", user_id, create_time, update_time)
VALUES(3, '$t(multi_table_accuracy)', 2, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO t_ds_dq_rule
(id, "name", "type", user_id, create_time, update_time)
VALUES(4, '$t(multi_table_value_comparison)', 3, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO t_ds_dq_rule
(id, "name", "type", user_id, create_time, update_time)
VALUES(5, '$t(field_length_check)', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO t_ds_dq_rule
(id, "name", "type", user_id, create_time, update_time)
VALUES(6, '$t(uniqueness_check)', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO t_ds_dq_rule
(id, "name", "type", user_id, create_time, update_time)
VALUES(7, '$t(regexp_check)', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO t_ds_dq_rule
(id, "name", "type", user_id, create_time, update_time)
VALUES(8, '$t(timeliness_check)', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO t_ds_dq_rule
(id, "name", "type", user_id, create_time, update_time)
VALUES(9, '$t(enumeration_check)', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');
INSERT INTO t_ds_dq_rule
(id, "name", "type", user_id, create_time, update_time)
VALUES(10, '$t(table_count_check)', 0, 1, '2020-01-12 00:00:00.000', '2020-01-12 00:00:00.000');

--
-- Table structure for table t_ds_dq_rule_execute_sql
--
DROP TABLE IF EXISTS t_ds_dq_rule_execute_sql;
CREATE TABLE t_ds_dq_rule_execute_sql (
                                          id serial NOT NULL,
                                          "index" int4 NULL,
                                          "sql" text NULL,
                                          table_alias varchar(255) DEFAULT NULL,
                                          "type" int4 NULL,
                                          create_time timestamp NULL,
                                          update_time timestamp NULL,
                                          is_error_output_sql bool NULL,
                                          CONSTRAINT t_ds_dq_rule_execute_sql_pk PRIMARY KEY (id)
);
INSERT INTO t_ds_dq_rule_execute_sql
(id, "index", "sql", table_alias, "type", is_error_output_sql, create_time, update_time)
VALUES(1, 1, 'SELECT COUNT(*) AS nulls FROM null_items', 'null_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_execute_sql
(id, "index", "sql", table_alias, "type", is_error_output_sql, create_time, update_time)
VALUES(2, 1, 'SELECT COUNT(*) AS total FROM ${src_table} WHERE (${src_filter})', 'total_count', 2, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_execute_sql
(id, "index", "sql", table_alias, "type", is_error_output_sql, create_time, update_time)
VALUES(3, 1, 'SELECT COUNT(*) AS miss from miss_items', 'miss_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_execute_sql
(id, "index", "sql", table_alias, "type", is_error_output_sql, create_time, update_time)
VALUES(4, 1, 'SELECT COUNT(*) AS valids FROM invalid_length_items', 'invalid_length_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_execute_sql
(id, "index", "sql", table_alias, "type", is_error_output_sql, create_time, update_time)
VALUES(5, 1, 'SELECT COUNT(*) AS total FROM ${target_table} WHERE (${target_filter})', 'total_count', 2, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_execute_sql
(id, "index", "sql", table_alias, "type", is_error_output_sql, create_time, update_time)
VALUES(6, 1, 'SELECT ${src_field} FROM ${src_table} group by ${src_field} having count(*) > 1', 'duplicate_items', 0, true, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_execute_sql
(id, "index", "sql", table_alias, "type", is_error_output_sql, create_time, update_time)
VALUES(7, 1, 'SELECT COUNT(*) AS duplicates FROM duplicate_items', 'duplicate_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_execute_sql
(id, "index", "sql", table_alias, "type", is_error_output_sql, create_time, update_time)
VALUES(8, 1, 'SELECT ${src_table}.* FROM (SELECT * FROM ${src_table} WHERE (${src_filter})) ${src_table} LEFT JOIN (SELECT * FROM ${target_table} WHERE (${target_filter})) ${target_table} ON ${on_clause} WHERE ${where_clause}', 'miss_items', 0, true, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_execute_sql
(id, "index", "sql", table_alias, "type", is_error_output_sql, create_time, update_time)
VALUES(9, 1, 'SELECT * FROM ${src_table} WHERE (${src_field} not regexp ''${regexp_pattern}'') AND (${src_filter}) ', 'regexp_items', 0, true, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_execute_sql
(id, "index", "sql", table_alias, "type", is_error_output_sql, create_time, update_time)
VALUES(10, 1, 'SELECT COUNT(*) AS regexps FROM regexp_items', 'regexp_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_execute_sql
(id, "index", "sql", table_alias, "type", is_error_output_sql, create_time, update_time)
VALUES(11, 1, 'SELECT * FROM ${src_table} WHERE (to_unix_timestamp(${src_field}, ''${datetime_format}'')-to_unix_timestamp(''${deadline}'', ''${datetime_format}'') <= 0) AND (to_unix_timestamp(${src_field}, ''${datetime_format}'')-to_unix_timestamp(''${begin_time}'', ''${datetime_format}'') >= 0) AND (${src_filter}) ', 'timeliness_items', 0, true, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_execute_sql
(id, "index", "sql", table_alias, "type", is_error_output_sql, create_time, update_time)
VALUES(12, 1, 'SELECT COUNT(*) AS timeliness FROM timeliness_items', 'timeliness_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_execute_sql
(id, "index", "sql", table_alias, "type", is_error_output_sql, create_time, update_time)
VALUES(13, 1, 'SELECT * FROM ${src_table} where (${src_field} not in ( ${enum_list} ) or ${src_field} is null) AND (${src_filter}) ', 'enum_items', 0, true, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_execute_sql
(id, "index", "sql", table_alias, "type", is_error_output_sql, create_time, update_time)
VALUES(14, 1, 'SELECT COUNT(*) AS enums FROM enum_items', 'enum_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_execute_sql
(id, "index", "sql", table_alias, "type", is_error_output_sql, create_time, update_time)
VALUES(15, 1, 'SELECT COUNT(*) AS total FROM ${src_table} WHERE (${src_filter})', 'table_count', 1, false, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_execute_sql
(id, "index", "sql", table_alias, "type", is_error_output_sql, create_time, update_time)
VALUES(16, 1, 'SELECT * FROM ${src_table} WHERE (${src_field} is null or ${src_field} = '''') AND (${src_filter})', 'null_items', 0, true, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_execute_sql
(id, "index", "sql", table_alias, "type", is_error_output_sql, create_time, update_time)
VALUES(17, 1, 'SELECT * FROM ${src_table} WHERE (length(${src_field}) ${logic_operator} ${field_length}) AND (${src_filter})', 'invalid_length_items', 0, true, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');

--
-- Table structure for table t_ds_dq_rule_input_entry
--
DROP TABLE IF EXISTS t_ds_dq_rule_input_entry;
CREATE TABLE t_ds_dq_rule_input_entry (
                                          id serial NOT NULL,
                                          field varchar(255) DEFAULT NULL,
                                          "type" varchar(255) DEFAULT NULL,
                                          title varchar(255) DEFAULT NULL,
                                          value varchar(255)  DEFAULT NULL,
                                          "options" text DEFAULT NULL,
                                          placeholder varchar(255) DEFAULT NULL,
                                          option_source_type int4 NULL,
                                          value_type int4 NULL,
                                          input_type int4 NULL,
                                          is_show int2 NULL DEFAULT '1'::smallint,
                                          can_edit int2 NULL DEFAULT '1'::smallint,
                                          is_emit int2 NULL DEFAULT '0'::smallint,
                                          is_validate int2 NULL DEFAULT '0'::smallint,
                                          create_time timestamp NULL,
                                          update_time timestamp NULL,
                                          CONSTRAINT t_ds_dq_rule_input_entry_pk PRIMARY KEY (id)
);
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(1, 'src_connector_type', 'select', '$t(src_connector_type)', '', '[{"label":"HIVE","value":"HIVE"},{"label":"JDBC","value":"JDBC"}]', 'please select source connector type', 2, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(2, 'src_datasource_id', 'select', '$t(src_datasource_id)', '', NULL, 'please select source datasource id', 1, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(3, 'src_table', 'select', '$t(src_table)', NULL, NULL, 'Please enter source table name', 0, 0, 0, 1, 1, 1, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(4, 'src_filter', 'input', '$t(src_filter)', NULL, NULL, 'Please enter filter expression', 0, 3, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(5, 'src_field', 'select', '$t(src_field)', NULL, NULL, 'Please enter column, only single column is supported', 0, 0, 0, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(6, 'statistics_name', 'input', '$t(statistics_name)', NULL, NULL, 'Please enter statistics name, the alias in statistics execute sql', 0, 0, 1, 0, 0, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(7, 'check_type', 'select', '$t(check_type)', '0', '[{"label":"Expected - Actual","value":"0"},{"label":"Actual - Expected","value":"1"},{"label":"Actual / Expected","value":"2"},{"label":"(Expected - Actual) / Expected","value":"3"}]', 'please select check type', 0, 0, 3, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(8, 'operator', 'select', '$t(operator)', '0', '[{"label":"=","value":"0"},{"label":"<","value":"1"},{"label":"<=","value":"2"},{"label":">","value":"3"},{"label":">=","value":"4"},{"label":"!=","value":"5"}]', 'please select operator', 0, 0, 3, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(9, 'threshold', 'input', '$t(threshold)', NULL, NULL, 'Please enter threshold, number is needed', 0, 2, 3, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(10, 'failure_strategy', 'select', '$t(failure_strategy)', '0', '[{"label":"Alert","value":"0"},{"label":"Block","value":"1"}]', 'please select failure strategy', 0, 0, 3, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(11, 'target_connector_type', 'select', '$t(target_connector_type)', '', '[{"label":"HIVE","value":"HIVE"},{"label":"JDBC","value":"JDBC"}]', 'Please select target connector type', 2, 0, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(12, 'target_datasource_id', 'select', '$t(target_datasource_id)', '', NULL, 'Please select target datasource', 1, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(13, 'target_table', 'select', '$t(target_table)', NULL, NULL, 'Please enter target table', 0, 0, 0, 1, 1, 1, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(14, 'target_filter', 'input', '$t(target_filter)', NULL, NULL, 'Please enter target filter expression', 0, 3, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(15, 'mapping_columns', 'group', '$t(mapping_columns)', NULL, '[{"field":"src_field","props":{"placeholder":"Please input src field","rows":0,"disabled":false,"size":"small"},"type":"input","title":"src_field"},{"field":"operator","props":{"placeholder":"Please input operator","rows":0,"disabled":false,"size":"small"},"type":"input","title":"operator"},{"field":"target_field","props":{"placeholder":"Please input target field","rows":0,"disabled":false,"size":"small"},"type":"input","title":"target_field"}]', 'please enter mapping columns', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(16, 'statistics_execute_sql', 'textarea', '$t(statistics_execute_sql)', NULL, NULL, 'Please enter statistics execute sql', 0, 3, 0, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(17, 'comparison_name', 'input', '$t(comparison_name)', NULL, NULL, 'Please enter comparison name, the alias in comparison execute sql', 0, 0, 0, 0, 0, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(18, 'comparison_execute_sql', 'textarea', '$t(comparison_execute_sql)', NULL, NULL, 'Please enter comparison execute sql', 0, 3, 0, 1, 1, 0, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(19, 'comparison_type', 'select', '$t(comparison_type)', '', NULL, 'Please enter comparison title', 3, 0, 2, 1, 0, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(20, 'writer_connector_type', 'select', '$t(writer_connector_type)', '', '[{"label":"MYSQL","value":"0"},{"label":"POSTGRESQL","value":"1"}]', 'please select writer connector type', 0, 2, 0, 1, 1, 1, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(21, 'writer_datasource_id', 'select', '$t(writer_datasource_id)', '', NULL, 'please select writer datasource id', 1, 2, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(22, 'target_field', 'select', '$t(target_field)', NULL, NULL, 'Please enter column, only single column is supported', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(23, 'field_length', 'input', '$t(field_length)', NULL, NULL, 'Please enter length limit', 0, 3, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(24, 'logic_operator', 'select', '$t(logic_operator)', '=', '[{"label":"=","value":"="},{"label":"<","value":"<"},{"label":"<=","value":"<="},{"label":">","value":">"},{"label":">=","value":">="},{"label":"<>","value":"<>"}]', 'please select logic operator', 0, 0, 3, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(25, 'regexp_pattern', 'input', '$t(regexp_pattern)', NULL, NULL, 'Please enter regexp pattern', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(26, 'deadline', 'input', '$t(deadline)', NULL, NULL, 'Please enter deadline', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(27, 'datetime_format', 'input', '$t(datetime_format)', NULL, NULL, 'Please enter datetime format', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(28, 'enum_list', 'input', '$t(enum_list)', NULL, NULL, 'Please enter enumeration', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_dq_rule_input_entry
(id, field, "type", title, value, "options", placeholder, option_source_type, value_type, input_type, is_show, can_edit, is_emit, is_validate, create_time, update_time)
VALUES(29, 'begin_time', 'input', '$t(begin_time)', NULL, NULL, 'Please enter begin time', 0, 0, 0, 1, 1, 0, 0, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');

--
-- Table structure for table t_ds_dq_task_statistics_value
--
DROP TABLE IF EXISTS t_ds_dq_task_statistics_value;
CREATE TABLE t_ds_dq_task_statistics_value (
                                               id serial NOT NULL,
                                               process_definition_id int4 NOT NULL,
                                               task_instance_id int4 NULL,
                                               rule_id int4 NOT NULL,
                                               unique_code varchar NOT NULL,
                                               statistics_name varchar NULL,
                                               statistics_value float8 NULL,
                                               data_time timestamp(0) NULL,
                                               create_time timestamp(0) NULL,
                                               update_time timestamp(0) NULL,
                                               CONSTRAINT t_ds_dq_task_statistics_value_pk PRIMARY KEY (id)
);

--
-- Table structure for table t_ds_relation_rule_execute_sql
--
DROP TABLE IF EXISTS t_ds_relation_rule_execute_sql;
CREATE TABLE t_ds_relation_rule_execute_sql (
                                                id serial NOT NULL,
                                                rule_id int4 NULL,
                                                execute_sql_id int4 NULL,
                                                create_time timestamp NULL,
                                                update_time timestamp NULL,
                                                CONSTRAINT t_ds_relation_rule_execute_sql_pk PRIMARY KEY (id)
);
INSERT INTO t_ds_relation_rule_execute_sql
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(1, 1, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_execute_sql
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(3, 5, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_execute_sql
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(2, 3, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_execute_sql
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(4, 3, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_execute_sql
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(5, 6, 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_execute_sql
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(6, 6, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_execute_sql
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(7, 7, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_execute_sql
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(8, 7, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_execute_sql
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(9, 8, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_execute_sql
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(10, 8, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_execute_sql
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(11, 9, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_execute_sql
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(12, 9, 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_execute_sql
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(13, 10, 15, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_execute_sql
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(14, 1, 16, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_execute_sql
(id, rule_id, execute_sql_id, create_time, update_time)
VALUES(15, 5, 17, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');

--
-- Table structure for table t_ds_relation_rule_input_entry
--
DROP TABLE IF EXISTS t_ds_relation_rule_input_entry;
CREATE TABLE t_ds_relation_rule_input_entry (
                                                id serial NOT NULL,
                                                rule_id int4 NULL,
                                                rule_input_entry_id int4 NULL,
                                                values_map text NULL,
                                                "index" int4 NULL,
                                                create_time timestamp NULL,
                                                update_time timestamp NULL,
                                                CONSTRAINT t_ds_relation_rule_input_entry_pk PRIMARY KEY (id)
);
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(1, 1, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(2, 1, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(3, 1, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(4, 1, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(5, 1, 5, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(6, 1, 6, '{"statistics_name":"null_count.nulls"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(7, 1, 7, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(8, 1, 8, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(9, 1, 9, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(10, 1, 10, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(11, 1, 17, '', 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(12, 1, 19, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(13, 2, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(14, 2, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(15, 2, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(16, 2, 6, '{"is_show":"true","can_edit":"true"}', 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(17, 2, 16, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(18, 2, 4, NULL, 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(19, 2, 7, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(20, 2, 8, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(21, 2, 9, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(22, 2, 10, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(24, 2, 19, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(25, 3, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(26, 3, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(27, 3, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(28, 3, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(29, 3, 11, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(30, 3, 12, NULL, 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(31, 3, 13, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(32, 3, 14, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(33, 3, 15, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(34, 3, 7, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(35, 3, 8, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(36, 3, 9, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(37, 3, 10, NULL, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(38, 3, 17, '{"comparison_name":"total_count.total"}', 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(39, 3, 19, NULL, 15, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(40, 4, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(41, 4, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(42, 4, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(43, 4, 6, '{"is_show":"true","can_edit":"true"}', 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(44, 4, 16, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(45, 4, 11, NULL, 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(46, 4, 12, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(47, 4, 13, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(48, 4, 17, '{"is_show":"true","can_edit":"true"}', 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(49, 4, 18, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(50, 4, 7, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(51, 4, 8, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(52, 4, 9, NULL, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(53, 4, 10, NULL, 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(62, 3, 6, '{"statistics_name":"miss_count.miss"}', 18, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(63, 5, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(64, 5, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(65, 5, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(66, 5, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(67, 5, 5, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(68, 5, 6, '{"statistics_name":"invalid_length_count.valids"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(69, 5, 24, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(70, 5, 23, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(71, 5, 7, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(72, 5, 8, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(73, 5, 9, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(74, 5, 10, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(75, 5, 17, '', 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(76, 5, 19, NULL, 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(79, 6, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(80, 6, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(81, 6, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(82, 6, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(83, 6, 5, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(84, 6, 6, '{"statistics_name":"duplicate_count.duplicates"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(85, 6, 7, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(86, 6, 8, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(87, 6, 9, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(88, 6, 10, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(89, 6, 17, '', 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(90, 6, 19, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(93, 7, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(94, 7, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(95, 7, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(96, 7, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(97, 7, 5, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(98, 7, 6, '{"statistics_name":"regexp_count.regexps"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(99, 7, 25, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(100, 7, 7, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(101, 7, 8, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(102, 7, 9, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(103, 7, 10, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(104, 7, 17, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(105, 7, 19, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(108, 8, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(109, 8, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(110, 8, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(111, 8, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(112, 8, 5, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(113, 8, 6, '{"statistics_name":"timeliness_count.timeliness"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(114, 8, 26, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(115, 8, 27, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(116, 8, 7, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(117, 8, 8, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(118, 8, 9, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(119, 8, 10, NULL, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(120, 8, 17, NULL, 14, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(121, 8, 19, NULL, 15, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(124, 9, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(125, 9, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(126, 9, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(127, 9, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(128, 9, 5, NULL, 5, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(129, 9, 6, '{"statistics_name":"enum_count.enums"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(130, 9, 28, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(131, 9, 7, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(132, 9, 8, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(133, 9, 9, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(134, 9, 10, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(135, 9, 17, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(136, 9, 19, NULL, 13, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(139, 10, 1, NULL, 1, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(140, 10, 2, NULL, 2, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(141, 10, 3, NULL, 3, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(142, 10, 4, NULL, 4, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(143, 10, 6, '{"statistics_name":"table_count.total"}', 6, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(144, 10, 7, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(145, 10, 8, NULL, 8, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(146, 10, 9, NULL, 9, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(147, 10, 10, NULL, 10, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(148, 10, 17, NULL, 11, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(149, 10, 19, NULL, 12, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');
INSERT INTO t_ds_relation_rule_input_entry
(id, rule_id, rule_input_entry_id, values_map, "index", create_time, update_time)
VALUES(150, 8, 29, NULL, 7, '2021-03-03 11:31:24.000', '2021-03-03 11:31:24.000');

--
-- Table structure for table t_ds_environment
--

DROP TABLE IF EXISTS t_ds_environment;
CREATE TABLE t_ds_environment (
                                  id serial NOT NULL,
                                  code bigint NOT NULL,
                                  name varchar(100) DEFAULT NULL,
                                  config text DEFAULT NULL,
                                  description text,
                                  operator int DEFAULT NULL,
                                  create_time timestamp DEFAULT NULL,
                                  update_time timestamp DEFAULT NULL,
                                  PRIMARY KEY (id),
                                  CONSTRAINT environment_name_unique UNIQUE (name),
                                  CONSTRAINT environment_code_unique UNIQUE (code)
);

--
-- Table structure for table t_ds_environment_worker_group_relation
--

DROP TABLE IF EXISTS t_ds_environment_worker_group_relation;
CREATE TABLE t_ds_environment_worker_group_relation (
                                                        id serial NOT NULL,
                                                        environment_code bigint NOT NULL,
                                                        worker_group varchar(255) NOT NULL,
                                                        operator int DEFAULT NULL,
                                                        create_time timestamp DEFAULT NULL,
                                                        update_time timestamp DEFAULT NULL,
                                                        PRIMARY KEY (id) ,
                                                        CONSTRAINT environment_worker_group_unique UNIQUE (environment_code,worker_group)
);

--
-- Table structure for table t_ds_task_group_queue
--

DROP TABLE IF EXISTS t_ds_task_group_queue;
CREATE TABLE t_ds_task_group_queue (
                                       id serial NOT NULL,
                                       task_id      int DEFAULT NULL ,
                                       task_name    VARCHAR(100) DEFAULT NULL ,
                                       group_id     int DEFAULT NULL ,
                                       process_id   int DEFAULT NULL ,
                                       priority     int DEFAULT '0' ,
                                       status       int DEFAULT '-1' ,
                                       force_start  int DEFAULT '0' ,
                                       in_queue     int DEFAULT '0' ,
                                       create_time  timestamp DEFAULT NULL ,
                                       update_time  timestamp DEFAULT NULL ,
                                       PRIMARY KEY (id)
);

--
-- Table structure for table t_ds_task_group
--

DROP TABLE IF EXISTS t_ds_task_group;
CREATE TABLE t_ds_task_group (
                                 id serial NOT NULL,
                                 name        varchar(100) DEFAULT NULL ,
                                 description varchar(200) DEFAULT NULL ,
                                 group_size  int NOT NULL ,
                                 project_code bigint DEFAULT '0' ,
                                 use_size    int DEFAULT '0' ,
                                 user_id     int DEFAULT NULL ,
                                 status      int DEFAULT '1'  ,
                                 create_time timestamp DEFAULT NULL ,
                                 update_time timestamp DEFAULT NULL ,
                                 PRIMARY KEY(id)
);

-- ----------------------------
-- Table structure for t_ds_audit_log
-- ----------------------------
DROP TABLE IF EXISTS t_ds_audit_log;
CREATE TABLE t_ds_audit_log (
                                id serial NOT NULL,
                                user_id int NOT NULL,
                                resource_type int NOT NULL,
                                operation int NOT NULL,
                                time timestamp DEFAULT NULL ,
                                resource_id int NOT NULL,
                                PRIMARY KEY (id)
);

--
-- Table structure for table t_ds_k8s
--

DROP TABLE IF EXISTS t_ds_k8s;
CREATE TABLE t_ds_k8s (
                          id serial NOT NULL,
                          k8s_name    VARCHAR(100) DEFAULT NULL ,
                          k8s_config  text ,
                          create_time timestamp DEFAULT NULL ,
                          update_time timestamp DEFAULT NULL ,
                          PRIMARY KEY (id)
);

--
-- Table structure for table t_ds_k8s_namespace
--

DROP TABLE IF EXISTS t_ds_k8s_namespace;
CREATE TABLE t_ds_k8s_namespace (
                                    id serial NOT NULL,
                                    limits_memory      int DEFAULT NULL ,
                                    namespace          varchar(100) DEFAULT NULL ,
                                    online_job_num     int DEFAULT '0' ,
                                    user_id            int DEFAULT NULL,
                                    pod_replicas       int DEFAULT NULL,
                                    pod_request_cpu    NUMERIC(13,4) NULL,
                                    pod_request_memory int DEFAULT NULL,
                                    limits_cpu         NUMERIC(13,4) NULL,
                                    k8s                varchar(100) DEFAULT NULL,
                                    create_time        timestamp DEFAULT NULL ,
                                    update_time        timestamp DEFAULT NULL ,
                                    PRIMARY KEY (id) ,
                                    CONSTRAINT k8s_namespace_unique UNIQUE (namespace,k8s)
);

--
-- Table structure for table t_ds_relation_namespace_user
--

DROP TABLE IF EXISTS t_ds_relation_namespace_user;
CREATE TABLE t_ds_relation_namespace_user (
                                              id serial NOT NULL,
                                              user_id           int DEFAULT NULL ,
                                              namespace_id      int DEFAULT NULL ,
                                              perm              int DEFAULT NULL ,
                                              create_time       timestamp DEFAULT NULL ,
                                              update_time       timestamp DEFAULT NULL ,
                                              PRIMARY KEY (id) ,
                                              CONSTRAINT namespace_user_unique UNIQUE (user_id,namespace_id)
);

-- ----------------------------
-- Table structure for t_ds_alert_send_status
-- ----------------------------
DROP TABLE IF EXISTS t_ds_alert_send_status;
CREATE TABLE t_ds_alert_send_status (
                                        id                           serial NOT NULL,
                                        alert_id                     int NOT NULL,
                                        alert_plugin_instance_id     int NOT NULL,
                                        send_status                  int DEFAULT '0',
                                        log                          text,
                                        create_time                  timestamp DEFAULT NULL,
                                        PRIMARY KEY (id),
                                        CONSTRAINT alert_send_status_unique UNIQUE (alert_id,alert_plugin_instance_id)
);
