DROP TABLE IF EXISTS QRTZ_BLOB_TRIGGERS;
CREATE TABLE QRTZ_BLOB_TRIGGERS (
  SCHED_NAME varchar(120) NOT NULL,
  TRIGGER_NAME varchar(200) NOT NULL,
  TRIGGER_GROUP varchar(200) NOT NULL,
  BLOB_DATA bytea NULL,
  PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

--
-- Table structure for table QRTZ_CALENDARS
--

DROP TABLE IF EXISTS QRTZ_CALENDARS;
CREATE TABLE QRTZ_CALENDARS (
  SCHED_NAME varchar(120) NOT NULL,
  CALENDAR_NAME varchar(200) NOT NULL,
  CALENDAR bytea NOT NULL,
  PRIMARY KEY (SCHED_NAME,CALENDAR_NAME)
);
--
-- Table structure for table QRTZ_CRON_TRIGGERS
--

DROP TABLE IF EXISTS QRTZ_CRON_TRIGGERS;
CREATE TABLE QRTZ_CRON_TRIGGERS (
  SCHED_NAME varchar(120) NOT NULL,
  TRIGGER_NAME varchar(200) NOT NULL,
  TRIGGER_GROUP varchar(200) NOT NULL,
  CRON_EXPRESSION varchar(120) NOT NULL,
  TIME_ZONE_ID varchar(80) DEFAULT NULL,
  PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

--
-- Table structure for table QRTZ_FIRED_TRIGGERS
--

DROP TABLE IF EXISTS QRTZ_FIRED_TRIGGERS;
CREATE TABLE QRTZ_FIRED_TRIGGERS (
  SCHED_NAME varchar(120) NOT NULL,
  ENTRY_ID varchar(95) NOT NULL,
  TRIGGER_NAME varchar(200) NOT NULL,
  TRIGGER_GROUP varchar(200) NOT NULL,
  INSTANCE_NAME varchar(200) NOT NULL,
  FIRED_TIME bigint NOT NULL,
  SCHED_TIME bigint NOT NULL,
  PRIORITY int NOT NULL,
  STATE varchar(16) NOT NULL,
  JOB_NAME varchar(200) DEFAULT NULL,
  JOB_GROUP varchar(200) DEFAULT NULL,
  IS_NONCONCURRENT varchar(1) DEFAULT NULL,
  REQUESTS_RECOVERY varchar(1) DEFAULT NULL,
  PRIMARY KEY (SCHED_NAME,ENTRY_ID)
) ;
  create index IDX_QRTZ_FT_TRIG_INST_NAME on QRTZ_FIRED_TRIGGERS (SCHED_NAME,INSTANCE_NAME);
  create index IDX_QRTZ_FT_INST_JOB_REQ_RCVRY on QRTZ_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY);
  create index IDX_QRTZ_FT_J_G on QRTZ_FIRED_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);
  create index IDX_QRTZ_FT_JG on QRTZ_FIRED_TRIGGERS (SCHED_NAME,JOB_GROUP);
  create index IDX_QRTZ_FT_T_G on QRTZ_FIRED_TRIGGERS (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
  create index IDX_QRTZ_FT_TG on QRTZ_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);

--
-- Table structure for table QRTZ_LOCKS
--

DROP TABLE IF EXISTS QRTZ_LOCKS;
CREATE TABLE QRTZ_LOCKS (
  SCHED_NAME varchar(120) NOT NULL,
  LOCK_NAME varchar(40) NOT NULL,
  PRIMARY KEY (SCHED_NAME,LOCK_NAME)
) ;

--
-- Table structure for table QRTZ_PAUSED_TRIGGER_GRPS
--

DROP TABLE IF EXISTS QRTZ_PAUSED_TRIGGER_GRPS;
CREATE TABLE QRTZ_PAUSED_TRIGGER_GRPS (
  SCHED_NAME varchar(120) NOT NULL,
  TRIGGER_GROUP varchar(200) NOT NULL,
  PRIMARY KEY (SCHED_NAME,TRIGGER_GROUP)
) ;

--
-- Table structure for table QRTZ_SCHEDULER_STATE
--

DROP TABLE IF EXISTS QRTZ_SCHEDULER_STATE;
CREATE TABLE QRTZ_SCHEDULER_STATE (
  SCHED_NAME varchar(120) NOT NULL,
  INSTANCE_NAME varchar(200) NOT NULL,
  LAST_CHECKIN_TIME bigint NOT NULL,
  CHECKIN_INTERVAL bigint NOT NULL,
  PRIMARY KEY (SCHED_NAME,INSTANCE_NAME)
) ;

--
-- Table structure for table QRTZ_SIMPLE_TRIGGERS
--

DROP TABLE IF EXISTS QRTZ_SIMPLE_TRIGGERS;
CREATE TABLE QRTZ_SIMPLE_TRIGGERS (
  SCHED_NAME varchar(120) NOT NULL,
  TRIGGER_NAME varchar(200) NOT NULL,
  TRIGGER_GROUP varchar(200) NOT NULL,
  REPEAT_COUNT bigint NOT NULL,
  REPEAT_INTERVAL bigint NOT NULL,
  TIMES_TRIGGERED bigint NOT NULL,
  PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)

) ;

--
-- Table structure for table QRTZ_SIMPROP_TRIGGERS
--

DROP TABLE IF EXISTS QRTZ_SIMPROP_TRIGGERS;
CREATE TABLE QRTZ_SIMPROP_TRIGGERS (
  SCHED_NAME varchar(120) NOT NULL,
  TRIGGER_NAME varchar(200) NOT NULL,
  TRIGGER_GROUP varchar(200) NOT NULL,
  STR_PROP_1 varchar(512) DEFAULT NULL,
  STR_PROP_2 varchar(512) DEFAULT NULL,
  STR_PROP_3 varchar(512) DEFAULT NULL,
  INT_PROP_1 int DEFAULT NULL,
  INT_PROP_2 int DEFAULT NULL,
  LONG_PROP_1 bigint DEFAULT NULL,
  LONG_PROP_2 bigint DEFAULT NULL,
  DEC_PROP_1 decimal(13,4) DEFAULT NULL,
  DEC_PROP_2 decimal(13,4) DEFAULT NULL,
  BOOL_PROP_1 varchar(1) DEFAULT NULL,
  BOOL_PROP_2 varchar(1) DEFAULT NULL,
  PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
) ;

--
-- Table structure for table QRTZ_TRIGGERS
--

DROP TABLE IF EXISTS QRTZ_TRIGGERS;
CREATE TABLE QRTZ_TRIGGERS (
  SCHED_NAME varchar(120) NOT NULL,
  TRIGGER_NAME varchar(200) NOT NULL,
  TRIGGER_GROUP varchar(200) NOT NULL,
  JOB_NAME varchar(200) NOT NULL,
  JOB_GROUP varchar(200) NOT NULL,
  DESCRIPTION varchar(250) DEFAULT NULL,
  NEXT_FIRE_TIME bigint DEFAULT NULL,
  PREV_FIRE_TIME bigint DEFAULT NULL,
  PRIORITY int DEFAULT NULL,
  TRIGGER_STATE varchar(16) NOT NULL,
  TRIGGER_TYPE varchar(8) NOT NULL,
  START_TIME bigint NOT NULL,
  END_TIME bigint DEFAULT NULL,
  CALENDAR_NAME varchar(200) DEFAULT NULL,
  MISFIRE_INSTR smallint DEFAULT NULL,
  JOB_DATA bytea,
  PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
) ;

  create index IDX_QRTZ_T_J on QRTZ_TRIGGERS (SCHED_NAME,JOB_NAME,JOB_GROUP);
  create index IDX_QRTZ_T_JG  on QRTZ_TRIGGERS (SCHED_NAME,JOB_GROUP);
  create index IDX_QRTZ_T_C  on QRTZ_TRIGGERS (SCHED_NAME,CALENDAR_NAME);
  create index IDX_QRTZ_T_G on QRTZ_TRIGGERS  (SCHED_NAME,TRIGGER_GROUP);
  create index IDX_QRTZ_T_STATE on QRTZ_TRIGGERS  (SCHED_NAME,TRIGGER_STATE);
  create index IDX_QRTZ_T_N_STATE on QRTZ_TRIGGERS  (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE);
  create index IDX_QRTZ_T_N_G_STATE on QRTZ_TRIGGERS  (SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE);
  create index IDX_QRTZ_T_NEXT_FIRE_TIME on QRTZ_TRIGGERS  (SCHED_NAME,NEXT_FIRE_TIME);
  create index IDX_QRTZ_T_NFT_ST on QRTZ_TRIGGERS  (SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME);
  create index IDX_QRTZ_T_NFT_MISFIRE on QRTZ_TRIGGERS  (SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME);
  create index IDX_QRTZ_T_NFT_ST_MISFIRE on QRTZ_TRIGGERS  (SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE);
  create index IDX_QRTZ_T_NFT_ST_MISFIRE_GRP on QRTZ_TRIGGERS  (SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE);


--
-- Table structure for table QRTZ_JOB_DETAILS
--

DROP TABLE IF EXISTS QRTZ_JOB_DETAILS;
CREATE TABLE QRTZ_JOB_DETAILS (
  SCHED_NAME varchar(120) NOT NULL,
  JOB_NAME varchar(200) NOT NULL,
  JOB_GROUP varchar(200) NOT NULL,
  DESCRIPTION varchar(250) DEFAULT NULL,
  JOB_CLASS_NAME varchar(250) NOT NULL,
  IS_DURABLE varchar(1) NOT NULL,
  IS_NONCONCURRENT varchar(1) NOT NULL,
  IS_UPDATE_DATA varchar(1) NOT NULL,
  REQUESTS_RECOVERY varchar(1) NOT NULL,
  JOB_DATA bytea,
  PRIMARY KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)
) ;
  create index  IDX_QRTZ_J_REQ_RECOVERY on QRTZ_JOB_DETAILS (SCHED_NAME,REQUESTS_RECOVERY);
  create index  IDX_QRTZ_J_GRP on QRTZ_JOB_DETAILS (SCHED_NAME,JOB_GROUP);

alter table QRTZ_BLOB_TRIGGERS drop CONSTRAINT if EXISTS QRTZ_BLOB_TRIGGERS_ibfk_1;
alter table QRTZ_BLOB_TRIGGERS add CONSTRAINT QRTZ_BLOB_TRIGGERS_ibfk_1 FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) REFERENCES QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);

alter table QRTZ_CRON_TRIGGERS drop CONSTRAINT if EXISTS QRTZ_CRON_TRIGGERS_ibfk_1;
alter table QRTZ_CRON_TRIGGERS add CONSTRAINT QRTZ_CRON_TRIGGERS_ibfk_1 FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) REFERENCES QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);

alter table QRTZ_SIMPLE_TRIGGERS drop CONSTRAINT if EXISTS QRTZ_SIMPLE_TRIGGERS_ibfk_1;
alter table QRTZ_SIMPLE_TRIGGERS add CONSTRAINT QRTZ_SIMPLE_TRIGGERS_ibfk_1 FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) REFERENCES QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);

alter table QRTZ_SIMPROP_TRIGGERS drop CONSTRAINT if EXISTS QRTZ_SIMPROP_TRIGGERS_ibfk_1;
alter table QRTZ_SIMPROP_TRIGGERS add CONSTRAINT QRTZ_SIMPROP_TRIGGERS_ibfk_1 FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) REFERENCES QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);

alter table QRTZ_TRIGGERS drop CONSTRAINT if EXISTS QRTZ_TRIGGERS_ibfk_1;
alter table QRTZ_TRIGGERS add CONSTRAINT QRTZ_TRIGGERS_ibfk_1 FOREIGN KEY (SCHED_NAME, JOB_NAME, JOB_GROUP) REFERENCES QRTZ_JOB_DETAILS (SCHED_NAME, JOB_NAME, JOB_GROUP);



--
-- Table structure for table t_escheduler_access_token
--

DROP TABLE IF EXISTS t_escheduler_access_token;
CREATE TABLE t_escheduler_access_token (
  id int NOT NULL  ,
  user_id int DEFAULT NULL ,
  token varchar(64) DEFAULT NULL ,
  expire_time timestamp DEFAULT NULL ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;

--
-- Table structure for table t_escheduler_alert
--

DROP TABLE IF EXISTS t_escheduler_alert;
CREATE TABLE t_escheduler_alert (
  id int NOT NULL  ,
  title varchar(64) DEFAULT NULL ,
  show_type int DEFAULT NULL ,
  content text ,
  alert_type int DEFAULT NULL ,
  alert_status int DEFAULT '0' ,
  log text ,
  alertgroup_id int DEFAULT NULL ,
  receivers text ,
  receivers_cc text ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;
--
-- Table structure for table t_escheduler_alertgroup
--

DROP TABLE IF EXISTS t_escheduler_alertgroup;
CREATE TABLE t_escheduler_alertgroup (
  id int NOT NULL  ,
  group_name varchar(255) DEFAULT NULL ,
  group_type int DEFAULT NULL ,
  "desc" varchar(255) DEFAULT NULL ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;

--
-- Table structure for table t_escheduler_command
--

DROP TABLE IF EXISTS t_escheduler_command;
CREATE TABLE t_escheduler_command (
  id int NOT NULL  ,
  command_type int DEFAULT NULL ,
  process_definition_id int DEFAULT NULL ,
  command_param text ,
  task_depend_type int DEFAULT NULL ,
  failure_strategy int DEFAULT '0' ,
  warning_type int DEFAULT '0' ,
  warning_group_id int DEFAULT NULL ,
  schedule_time timestamp DEFAULT NULL ,
  start_time timestamp DEFAULT NULL ,
  executor_id int DEFAULT NULL ,
  dependence varchar(255) DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  process_instance_priority int DEFAULT NULL ,
  worker_group_id int DEFAULT '-1' ,
  PRIMARY KEY (id)
) ;

--
-- Table structure for table t_escheduler_datasource
--

DROP TABLE IF EXISTS t_escheduler_datasource;
CREATE TABLE t_escheduler_datasource (
  id int NOT NULL  ,
  name varchar(64) NOT NULL ,
  note varchar(256) DEFAULT NULL ,
  type int NOT NULL ,
  user_id int NOT NULL ,
  connection_params text NOT NULL ,
  create_time timestamp NOT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;

--
-- Table structure for table t_escheduler_error_command
--

DROP TABLE IF EXISTS t_escheduler_error_command;
CREATE TABLE t_escheduler_error_command (
  id int NOT NULL ,
  command_type int DEFAULT NULL ,
  executor_id int DEFAULT NULL ,
  process_definition_id int DEFAULT NULL ,
  command_param text ,
  task_depend_type int DEFAULT NULL ,
  failure_strategy int DEFAULT '0' ,
  warning_type int DEFAULT '0' ,
  warning_group_id int DEFAULT NULL ,
  schedule_time timestamp DEFAULT NULL ,
  start_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  dependence text ,
  process_instance_priority int DEFAULT NULL ,
  worker_group_id int DEFAULT '-1' ,
  message text ,
  PRIMARY KEY (id)
);
--
-- Table structure for table t_escheduler_master_server
--

DROP TABLE IF EXISTS t_escheduler_master_server;
CREATE TABLE t_escheduler_master_server (
  id int NOT NULL  ,
  host varchar(45) DEFAULT NULL ,
  port int DEFAULT NULL ,
  zk_directory varchar(64) DEFAULT NULL ,
  res_info varchar(256) DEFAULT NULL ,
  create_time timestamp DEFAULT NULL ,
  last_heartbeat_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;

--
-- Table structure for table t_escheduler_process_definition
--

DROP TABLE IF EXISTS t_escheduler_process_definition;
CREATE TABLE t_escheduler_process_definition (
  id int NOT NULL  ,
  name varchar(255) DEFAULT NULL ,
  version int DEFAULT NULL ,
  release_state int DEFAULT NULL ,
  project_id int DEFAULT NULL ,
  user_id int DEFAULT NULL ,
  process_definition_json text ,
  "desc" text ,
  global_params text ,
  flag int DEFAULT NULL ,
  locations text ,
  connects text ,
  receivers text ,
  receivers_cc text ,
  create_time timestamp DEFAULT NULL ,
  timeout int DEFAULT '0' ,
  tenant_id int NOT NULL DEFAULT '-1' ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;

create index process_definition_index on t_escheduler_process_definition (project_id,id);

--
-- Table structure for table t_escheduler_process_instance
--

DROP TABLE IF EXISTS t_escheduler_process_instance;
CREATE TABLE t_escheduler_process_instance (
  id int NOT NULL  ,
  name varchar(255) DEFAULT NULL ,
  process_definition_id int DEFAULT NULL ,
  state int DEFAULT NULL ,
  recovery int DEFAULT NULL ,
  start_time timestamp DEFAULT NULL ,
  end_time timestamp DEFAULT NULL ,
  run_times int DEFAULT NULL ,
  host varchar(45) DEFAULT NULL ,
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
  locations text ,
  connects text ,
  history_cmd text ,
  dependence_schedule_times text ,
  process_instance_priority int DEFAULT NULL ,
  worker_group_id int DEFAULT '-1' ,
  timeout int DEFAULT '0' ,
  tenant_id int NOT NULL DEFAULT '-1' ,
  PRIMARY KEY (id)
) ;
  create index process_instance_index on t_escheduler_process_instance (process_definition_id,id);
  create index start_time_index on t_escheduler_process_instance (start_time);

--
-- Table structure for table t_escheduler_project
--

DROP TABLE IF EXISTS t_escheduler_project;
CREATE TABLE t_escheduler_project (
  id int NOT NULL  ,
  name varchar(100) DEFAULT NULL ,
  ”desc“ varchar(200) DEFAULT NULL ,
  user_id int DEFAULT NULL ,
  flag int DEFAULT '1' ,
  create_time timestamp DEFAULT CURRENT_TIMESTAMP ,
  update_time timestamp DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (id)
) ;
  create index user_id_index on t_escheduler_project (user_id);

--
-- Table structure for table t_escheduler_queue
--

DROP TABLE IF EXISTS t_escheduler_queue;
CREATE TABLE t_escheduler_queue (
  id int NOT NULL  ,
  queue_name varchar(64) DEFAULT NULL ,
  queue varchar(64) DEFAULT NULL ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
);


--
-- Table structure for table t_escheduler_relation_datasource_user
--

DROP TABLE IF EXISTS t_escheduler_relation_datasource_user;
CREATE TABLE t_escheduler_relation_datasource_user (
  id int NOT NULL  ,
  user_id int NOT NULL ,
  datasource_id int DEFAULT NULL ,
  perm int DEFAULT '1' ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;
;

--
-- Table structure for table t_escheduler_relation_process_instance
--

DROP TABLE IF EXISTS t_escheduler_relation_process_instance;
CREATE TABLE t_escheduler_relation_process_instance (
  id int NOT NULL  ,
  parent_process_instance_id int DEFAULT NULL ,
  parent_task_instance_id int DEFAULT NULL ,
  process_instance_id int DEFAULT NULL ,
  PRIMARY KEY (id)
) ;


--
-- Table structure for table t_escheduler_relation_project_user
--

DROP TABLE IF EXISTS t_escheduler_relation_project_user;
CREATE TABLE t_escheduler_relation_project_user (
  id int NOT NULL  ,
  user_id int NOT NULL ,
  project_id int DEFAULT NULL ,
  perm int DEFAULT '1' ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;
create index relation_project_user_id_index on t_escheduler_relation_project_user (user_id);

--
-- Table structure for table t_escheduler_relation_resources_user
--

DROP TABLE IF EXISTS t_escheduler_relation_resources_user;
CREATE TABLE t_escheduler_relation_resources_user (
  id int NOT NULL ,
  user_id int NOT NULL ,
  resources_id int DEFAULT NULL ,
  perm int DEFAULT '1' ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;

--
-- Table structure for table t_escheduler_relation_udfs_user
--

DROP TABLE IF EXISTS t_escheduler_relation_udfs_user;
CREATE TABLE t_escheduler_relation_udfs_user (
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
-- Table structure for table t_escheduler_relation_user_alertgroup
--

DROP TABLE IF EXISTS t_escheduler_relation_user_alertgroup;
CREATE TABLE t_escheduler_relation_user_alertgroup (
  id int NOT NULL,
  alertgroup_id int DEFAULT NULL,
  user_id int DEFAULT NULL,
  create_time timestamp DEFAULT NULL,
  update_time timestamp DEFAULT NULL,
  PRIMARY KEY (id)
);

--
-- Table structure for table t_escheduler_resources
--

DROP TABLE IF EXISTS t_escheduler_resources;
CREATE TABLE t_escheduler_resources (
  id int NOT NULL  ,
  alias varchar(64) DEFAULT NULL ,
  file_name varchar(64) DEFAULT NULL ,
  "desc" varchar(256) DEFAULT NULL ,
  user_id int DEFAULT NULL ,
  type int DEFAULT NULL ,
  size bigint DEFAULT NULL ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;
;

--
-- Table structure for table t_escheduler_schedules
--

DROP TABLE IF EXISTS t_escheduler_schedules;
CREATE TABLE t_escheduler_schedules (
  id int NOT NULL  ,
  process_definition_id int NOT NULL ,
  start_time timestamp NOT NULL ,
  end_time timestamp NOT NULL ,
  crontab varchar(256) NOT NULL ,
  failure_strategy int NOT NULL ,
  user_id int NOT NULL ,
  release_state int NOT NULL ,
  warning_type int NOT NULL ,
  warning_group_id int DEFAULT NULL ,
  process_instance_priority int DEFAULT NULL ,
  worker_group_id int DEFAULT '-1' ,
  create_time timestamp NOT NULL ,
  update_time timestamp NOT NULL ,
  PRIMARY KEY (id)
);

--
-- Table structure for table t_escheduler_session
--

DROP TABLE IF EXISTS t_escheduler_session;
CREATE TABLE t_escheduler_session (
  id varchar(64) NOT NULL ,
  user_id int DEFAULT NULL ,
  ip varchar(45) DEFAULT NULL ,
  last_login_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
);

--
-- Table structure for table t_escheduler_task_instance
--

DROP TABLE IF EXISTS t_escheduler_task_instance;
CREATE TABLE t_escheduler_task_instance (
  id int NOT NULL  ,
  name varchar(255) DEFAULT NULL ,
  task_type varchar(64) DEFAULT NULL ,
  process_definition_id int DEFAULT NULL ,
  process_instance_id int DEFAULT NULL ,
  task_json text ,
  state int DEFAULT NULL ,
  submit_time timestamp DEFAULT NULL ,
  start_time timestamp DEFAULT NULL ,
  end_time timestamp DEFAULT NULL ,
  host varchar(45) DEFAULT NULL ,
  execute_path varchar(200) DEFAULT NULL ,
  log_path varchar(200) DEFAULT NULL ,
  alert_flag int DEFAULT NULL ,
  retry_times int DEFAULT '0' ,
  pid int DEFAULT NULL ,
  app_link varchar(255) DEFAULT NULL ,
  flag int DEFAULT '1' ,
  retry_interval int DEFAULT NULL ,
  max_retry_times int DEFAULT NULL ,
  task_instance_priority int DEFAULT NULL ,
  worker_group_id int DEFAULT '-1' ,
  PRIMARY KEY (id)
) ;

--
-- Table structure for table t_escheduler_tenant
--

DROP TABLE IF EXISTS t_escheduler_tenant;
CREATE TABLE t_escheduler_tenant (
  id int NOT NULL  ,
  tenant_code varchar(64) DEFAULT NULL ,
  tenant_name varchar(64) DEFAULT NULL ,
  "desc" varchar(256) DEFAULT NULL ,
  queue_id int DEFAULT NULL ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;

--
-- Table structure for table t_escheduler_udfs
--

DROP TABLE IF EXISTS t_escheduler_udfs;
CREATE TABLE t_escheduler_udfs (
  id int NOT NULL  ,
  user_id int NOT NULL ,
  func_name varchar(100) NOT NULL ,
  class_name varchar(255) NOT NULL ,
  type int NOT NULL ,
  arg_types varchar(255) DEFAULT NULL ,
  database varchar(255) DEFAULT NULL ,
  "desc" varchar(255) DEFAULT NULL ,
  resource_id int NOT NULL ,
  resource_name varchar(255) NOT NULL ,
  create_time timestamp NOT NULL ,
  update_time timestamp NOT NULL ,
  PRIMARY KEY (id)
) ;

--
-- Table structure for table t_escheduler_user
--

DROP TABLE IF EXISTS t_escheduler_user;
CREATE TABLE t_escheduler_user (
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
  PRIMARY KEY (id)
);

--
-- Table structure for table t_escheduler_version
--

DROP TABLE IF EXISTS t_escheduler_version;
CREATE TABLE t_escheduler_version (
  id int NOT NULL ,
  version varchar(200) NOT NULL,
  PRIMARY KEY (id)
) ;
create index version_index on t_escheduler_version(version);

--
-- Table structure for table t_escheduler_worker_group
--

DROP TABLE IF EXISTS t_escheduler_worker_group;
CREATE TABLE t_escheduler_worker_group (
  id bigint NOT NULL  ,
  name varchar(256) DEFAULT NULL ,
  ip_list varchar(256) DEFAULT NULL ,
  create_time timestamp DEFAULT NULL ,
  update_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;

--
-- Table structure for table t_escheduler_worker_server
--

DROP TABLE IF EXISTS t_escheduler_worker_server;
CREATE TABLE t_escheduler_worker_server (
  id int NOT NULL  ,
  host varchar(45) DEFAULT NULL ,
  port int DEFAULT NULL ,
  zk_directory varchar(64)   DEFAULT NULL ,
  res_info varchar(255) DEFAULT NULL ,
  create_time timestamp DEFAULT NULL ,
  last_heartbeat_time timestamp DEFAULT NULL ,
  PRIMARY KEY (id)
) ;


DROP SEQUENCE IF EXISTS t_escheduler_access_token_id_sequence;
CREATE SEQUENCE  t_escheduler_access_token_id_sequence;
ALTER TABLE t_escheduler_access_token ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_access_token_id_sequence');
DROP SEQUENCE IF EXISTS t_escheduler_alert_id_sequence;
CREATE SEQUENCE  t_escheduler_alert_id_sequence;
ALTER TABLE t_escheduler_alert ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_alert_id_sequence');
DROP SEQUENCE IF EXISTS t_escheduler_alertgroup_id_sequence;
CREATE SEQUENCE  t_escheduler_alertgroup_id_sequence;
ALTER TABLE t_escheduler_alertgroup ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_alertgroup_id_sequence');

DROP SEQUENCE IF EXISTS t_escheduler_command_id_sequence;
CREATE SEQUENCE  t_escheduler_command_id_sequence;
ALTER TABLE t_escheduler_command ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_command_id_sequence');
DROP SEQUENCE IF EXISTS t_escheduler_datasource_id_sequence;
CREATE SEQUENCE  t_escheduler_datasource_id_sequence;
ALTER TABLE t_escheduler_datasource ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_datasource_id_sequence');
DROP SEQUENCE IF EXISTS t_escheduler_master_server_id_sequence;
CREATE SEQUENCE  t_escheduler_master_server_id_sequence;
ALTER TABLE t_escheduler_master_server ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_master_server_id_sequence');
DROP SEQUENCE IF EXISTS t_escheduler_process_definition_id_sequence;
CREATE SEQUENCE  t_escheduler_process_definition_id_sequence;
ALTER TABLE t_escheduler_process_definition ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_process_definition_id_sequence');
DROP SEQUENCE IF EXISTS t_escheduler_process_instance_id_sequence;
CREATE SEQUENCE  t_escheduler_process_instance_id_sequence;
ALTER TABLE t_escheduler_process_instance ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_process_instance_id_sequence');
DROP SEQUENCE IF EXISTS t_escheduler_project_id_sequence;
CREATE SEQUENCE  t_escheduler_project_id_sequence;
ALTER TABLE t_escheduler_project ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_project_id_sequence');
DROP SEQUENCE IF EXISTS t_escheduler_queue_id_sequence;
CREATE SEQUENCE  t_escheduler_queue_id_sequence;
ALTER TABLE t_escheduler_queue ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_queue_id_sequence');

DROP SEQUENCE IF EXISTS t_escheduler_relation_datasource_user_id_sequence;
CREATE SEQUENCE  t_escheduler_relation_datasource_user_id_sequence;
ALTER TABLE t_escheduler_relation_datasource_user ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_relation_datasource_user_id_sequence');
DROP SEQUENCE IF EXISTS t_escheduler_relation_process_instance_id_sequence;
CREATE SEQUENCE  t_escheduler_relation_process_instance_id_sequence;
ALTER TABLE t_escheduler_relation_process_instance ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_relation_process_instance_id_sequence');
DROP SEQUENCE IF EXISTS t_escheduler_relation_project_user_id_sequence;
CREATE SEQUENCE  t_escheduler_relation_project_user_id_sequence;
ALTER TABLE t_escheduler_relation_project_user ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_relation_project_user_id_sequence');
DROP SEQUENCE IF EXISTS t_escheduler_relation_resources_user_id_sequence;
CREATE SEQUENCE  t_escheduler_relation_resources_user_id_sequence;
ALTER TABLE t_escheduler_relation_resources_user ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_relation_resources_user_id_sequence');
DROP SEQUENCE IF EXISTS t_escheduler_relation_udfs_user_id_sequence;
CREATE SEQUENCE  t_escheduler_relation_udfs_user_id_sequence;
ALTER TABLE t_escheduler_relation_udfs_user ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_relation_udfs_user_id_sequence');
DROP SEQUENCE IF EXISTS t_escheduler_relation_user_alertgroup_id_sequence;
CREATE SEQUENCE  t_escheduler_relation_user_alertgroup_id_sequence;
ALTER TABLE t_escheduler_relation_user_alertgroup ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_relation_user_alertgroup_id_sequence');

DROP SEQUENCE IF EXISTS t_escheduler_resources_id_sequence;
CREATE SEQUENCE  t_escheduler_resources_id_sequence;
ALTER TABLE t_escheduler_resources ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_resources_id_sequence');
DROP SEQUENCE IF EXISTS t_escheduler_schedules_id_sequence;
CREATE SEQUENCE  t_escheduler_schedules_id_sequence;
ALTER TABLE t_escheduler_schedules ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_schedules_id_sequence');
DROP SEQUENCE IF EXISTS t_escheduler_task_instance_id_sequence;
CREATE SEQUENCE  t_escheduler_task_instance_id_sequence;
ALTER TABLE t_escheduler_task_instance ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_task_instance_id_sequence');
DROP SEQUENCE IF EXISTS t_escheduler_tenant_id_sequence;
CREATE SEQUENCE  t_escheduler_tenant_id_sequence;
ALTER TABLE t_escheduler_tenant ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_tenant_id_sequence');
DROP SEQUENCE IF EXISTS t_escheduler_udfs_id_sequence;
CREATE SEQUENCE  t_escheduler_udfs_id_sequence;
ALTER TABLE t_escheduler_udfs ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_udfs_id_sequence');
DROP SEQUENCE IF EXISTS t_escheduler_user_id_sequence;
CREATE SEQUENCE  t_escheduler_user_id_sequence;
ALTER TABLE t_escheduler_user ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_user_id_sequence');

DROP SEQUENCE IF EXISTS t_escheduler_version_id_sequence;
CREATE SEQUENCE  t_escheduler_version_id_sequence;
ALTER TABLE t_escheduler_version ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_version_id_sequence');

DROP SEQUENCE IF EXISTS t_escheduler_worker_group_id_sequence;
CREATE SEQUENCE  t_escheduler_worker_group_id_sequence;
ALTER TABLE t_escheduler_worker_group ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_worker_group_id_sequence');
DROP SEQUENCE IF EXISTS t_escheduler_worker_server_id_sequence;
CREATE SEQUENCE  t_escheduler_worker_server_id_sequence;
ALTER TABLE t_escheduler_worker_server ALTER COLUMN id SET DEFAULT NEXTVAL('t_escheduler_worker_server_id_sequence');