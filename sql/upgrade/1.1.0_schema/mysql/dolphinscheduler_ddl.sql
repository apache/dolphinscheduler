SET sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''));

-- ac_escheduler_T_t_escheduler_process_definition_C_tenant_id
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_process_definition_C_tenant_id;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_process_definition_C_tenant_id()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_process_definition'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='tenant_id')
   THEN
         ALTER TABLE `t_escheduler_process_definition` ADD COLUMN `tenant_id` int(11) NOT NULL DEFAULT -1 COMMENT '租户id' AFTER `timeout`;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_process_definition_C_tenant_id;
DROP PROCEDURE ac_escheduler_T_t_escheduler_process_definition_C_tenant_id;

-- ac_escheduler_T_t_escheduler_process_instance_C_tenant_id
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_process_instance_C_tenant_id;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_process_instance_C_tenant_id()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_process_instance'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='tenant_id')
   THEN
         ALTER TABLE `t_escheduler_process_instance` ADD COLUMN `tenant_id` int(11) NOT NULL DEFAULT -1 COMMENT '租户id' AFTER `timeout`;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_process_instance_C_tenant_id;
DROP PROCEDURE ac_escheduler_T_t_escheduler_process_instance_C_tenant_id;
