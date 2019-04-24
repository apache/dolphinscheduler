SET sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''));
-- ac_escheduler_T_t_escheduler_version
drop PROCEDURE if EXISTS ac_escheduler_T_t_escheduler_version;
delimiter d//
CREATE PROCEDURE ac_escheduler_T_t_escheduler_version()
   BEGIN
       drop table if exists t_escheduler_version;
       CREATE TABLE  IF NOT EXISTS  `t_escheduler_version` (
         `id` int(11) NOT NULL AUTO_INCREMENT,
         `version` varchar(200) NOT NULL,
         PRIMARY KEY (`id`),
         UNIQUE KEY `version_UNIQUE` (`version`)
       ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='版本表';

 END;

d//

delimiter ;
CALL ac_escheduler_T_t_escheduler_version;
DROP PROCEDURE ac_escheduler_T_t_escheduler_version;