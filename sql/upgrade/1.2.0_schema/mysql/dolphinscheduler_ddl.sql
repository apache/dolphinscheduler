SET sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''));
-- ut_dolphin_T_t_dolphinscheduler_access_token
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_access_token;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_access_token()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_access_token'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_access_token RENAME t_dolphinscheduler_access_token;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_access_token;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_access_token;

-- ut_dolphin_T_t_dolphinscheduler_alert
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_alert;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_alert()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_alert'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_alert RENAME t_dolphinscheduler_alert;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_alert;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_alert;

-- ut_dolphin_T_t_dolphinscheduler_alertgroup
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_alertgroup;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_alertgroup()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_alertgroup'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_alertgroup RENAME t_dolphinscheduler_alertgroup;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_alertgroup;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_alertgroup;

-- ut_dolphin_T_t_dolphinscheduler_command
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_command;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_command()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_command'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_command RENAME t_dolphinscheduler_command;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_command;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_command;

-- ut_dolphin_T_t_dolphinscheduler_datasource
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_datasource;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_datasource()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_datasource'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_datasource RENAME t_dolphinscheduler_datasource;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_datasource;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_datasource;

-- ut_dolphin_T_t_dolphinscheduler_error_command
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_error_command;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_error_command()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_error_command'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_error_command RENAME t_dolphinscheduler_error_command;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_error_command;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_error_command;

-- ut_dolphin_T_t_dolphinscheduler_master_server
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_master_server;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_master_server()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_master_server'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_master_server RENAME t_dolphinscheduler_master_server;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_master_server;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_master_server;

-- ut_dolphin_T_t_dolphinscheduler_process_definition
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_process_definition;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_process_definition()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_process_definition'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_process_definition RENAME t_dolphinscheduler_process_definition;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_process_definition;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_process_definition;

-- ut_dolphin_T_t_dolphinscheduler_process_instance
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_process_instance;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_process_instance()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_process_instance'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_process_instance RENAME t_dolphinscheduler_process_instance;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_process_instance;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_process_instance;

-- ut_dolphin_T_t_dolphinscheduler_project
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_project;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_project()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_project'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_project RENAME t_dolphinscheduler_project;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_project;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_project;

-- ut_dolphin_T_t_dolphinscheduler_queue
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_queue;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_queue()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_queue'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_queue RENAME t_dolphinscheduler_queue;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_queue;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_queue;

-- ut_dolphin_T_t_dolphinscheduler_relation_datasource_user
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_relation_datasource_user;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_relation_datasource_user()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_relation_datasource_user'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_relation_datasource_user RENAME t_dolphinscheduler_relation_datasource_user;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_relation_datasource_user;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_relation_datasource_user;

-- ut_dolphin_T_t_dolphinscheduler_relation_process_instance
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_relation_process_instance;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_relation_process_instance()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_relation_process_instance'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_relation_process_instance RENAME t_dolphinscheduler_relation_process_instance;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_relation_process_instance;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_relation_process_instance;

-- ut_dolphin_T_t_dolphinscheduler_relation_project_user
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_relation_project_user;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_relation_project_user()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_relation_project_user'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_relation_project_user RENAME t_dolphinscheduler_relation_project_user;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_relation_project_user;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_relation_project_user;

-- ut_dolphin_T_t_dolphinscheduler_relation_resources_user
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_relation_resources_user;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_relation_resources_user()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_relation_resources_user'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_relation_resources_user RENAME t_dolphinscheduler_relation_resources_user;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_relation_resources_user;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_relation_resources_user;

-- ut_dolphin_T_t_dolphinscheduler_relation_udfs_user
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_relation_udfs_user;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_relation_udfs_user()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_relation_udfs_user'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_relation_udfs_user RENAME t_dolphinscheduler_relation_udfs_user;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_relation_udfs_user;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_relation_udfs_user;

-- ut_dolphin_T_t_dolphinscheduler_relation_user_alertgroup
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_relation_user_alertgroup;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_relation_user_alertgroup()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_relation_user_alertgroup'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_relation_user_alertgroup RENAME t_dolphinscheduler_relation_user_alertgroup;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_relation_user_alertgroup;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_relation_user_alertgroup;

-- ut_dolphin_T_t_dolphinscheduler_resources
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_resources;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_resources()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_resources'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_resources RENAME t_dolphinscheduler_resources;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_resources;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_resources;

-- ut_dolphin_T_t_dolphinscheduler_schedules
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_schedules;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_schedules()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_schedules'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_schedules RENAME t_dolphinscheduler_schedules;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_schedules;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_schedules;

-- ut_dolphin_T_t_dolphinscheduler_session
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_session;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_session()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_session'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_session RENAME t_dolphinscheduler_session;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_session;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_session;

-- ut_dolphin_T_t_dolphinscheduler_task_instance
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_task_instance;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_task_instance()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_task_instance'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_task_instance RENAME t_dolphinscheduler_task_instance;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_task_instance;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_task_instance;

-- ut_dolphin_T_t_dolphinscheduler_tenant
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_tenant;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_tenant()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_tenant'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_tenant RENAME t_dolphinscheduler_tenant;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_tenant;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_tenant;

-- ut_dolphin_T_t_dolphinscheduler_udfs
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_udfs;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_udfs()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_udfs'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_udfs RENAME t_dolphinscheduler_udfs;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_udfs;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_udfs;

-- ut_dolphin_T_t_dolphinscheduler_user
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_user;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_user()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_user'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_user RENAME t_dolphinscheduler_user;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_user;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_user;

-- ut_dolphin_T_t_dolphinscheduler_version
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_version;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_version()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_version'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_version RENAME t_dolphinscheduler_version;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_version;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_version;

-- ut_dolphin_T_t_dolphinscheduler_worker_group
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_worker_group;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_worker_group()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_worker_group'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_worker_group RENAME t_dolphinscheduler_worker_group;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_worker_group;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_worker_group;

-- ut_dolphin_T_t_dolphinscheduler_worker_server
drop PROCEDURE if EXISTS ut_dolphin_T_t_dolphinscheduler_worker_server;
delimiter d//
CREATE PROCEDURE ut_dolphin_T_t_dolphinscheduler_worker_server()
	BEGIN
		IF EXISTS (SELECT 1 FROM information_schema.TABLES
			WHERE TABLE_NAME='t_escheduler_worker_server'
			AND TABLE_SCHEMA=(SELECT DATABASE()))
		THEN
			ALTER TABLE t_escheduler_worker_server RENAME t_dolphinscheduler_worker_server;
		END IF;
	END;
d//

delimiter ;
CALL ut_dolphin_T_t_dolphinscheduler_worker_server;
DROP PROCEDURE ut_dolphin_T_t_dolphinscheduler_worker_server;