SET sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''));
-- ac_escheduler_T_t_escheduler_queue_C_create_time
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_queue_C_create_time;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_queue_C_create_time()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_queue'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='create_time')
   THEN
         ALTER TABLE t_escheduler_queue ADD COLUMN create_time datetime DEFAULT NULL COMMENT '创建时间' AFTER queue;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_queue_C_create_time;
DROP PROCEDURE ac_escheduler_T_t_escheduler_queue_C_create_time;


-- ac_escheduler_T_t_escheduler_queue_C_update_time
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_queue_C_update_time;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_queue_C_update_time()
   BEGIN
       IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
           WHERE TABLE_NAME='t_escheduler_queue'
           AND TABLE_SCHEMA=(SELECT DATABASE())
           AND COLUMN_NAME='update_time')
   THEN
         ALTER TABLE t_escheduler_queue ADD COLUMN update_time datetime DEFAULT NULL COMMENT '更新时间' AFTER create_time;
       END IF;
 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_queue_C_update_time;
DROP PROCEDURE ac_escheduler_T_t_escheduler_queue_C_update_time;
