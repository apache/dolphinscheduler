-- MySQL dump 10.13  Distrib 8.0.19, for macos10.15 (x86_64)
--
-- Host: localhost    Database: dolphinscheduler
-- ------------------------------------------------------
-- Server version	5.7.28


DROP TABLE IF EXISTS `t_yss_calendar`;


CREATE TABLE `t_yss_calendar` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `name` varchar(100) NOT NULL COMMENT 'calendar name',
  `start_time` datetime NOT NULL COMMENT 'start time',
  `end_time` datetime NOT NULL COMMENT 'end time',
  `release_state` tinyint(4) DEFAULT NULL COMMENT '0 offline, 1 online',
  `description` varchar(200) DEFAULT NULL COMMENT 'calendar description',
  `user_id` int(11) DEFAULT NULL COMMENT 'creator id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  UNIQUE KEY `calendar_name_unique` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `t_yss_calendar_details`;

CREATE TABLE `t_yss_calendar_details` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `calendar_id` int(11) NOT NULL COMMENT 'calendar id',
  `stamp` DATE NOT NULL COMMENT 'calendar date stamp ',
  `flag` tinyint(4) DEFAULT NULL COMMENT '0 not available, 1 available',
  `user_id` int(11) DEFAULT NULL COMMENT 'creator id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`),
  KEY `calendar_details_index` (`calendar_id`,`stamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO `t_escheduler_user` VALUES ('1', 'sysadmin', '48a365b4ce1e322a55ae9017f3daf0c0', '1', 'xxx@qq.com', 'xx', '1',
 '2018-03-27 15:48:50', '2018-10-24 17:40:22');

INSERT INTO `t_ds_tenant` VALUES (1,'sysadmin','sysadmin','',1,'2020-04-13 09:06:41','2020-04-13 09:06:41');

ALTER TABLE `dolphinscheduler`.`t_ds_schedules`
ADD COLUMN `scheduler_calendar` VARCHAR(255) NULL AFTER `crontab`;



