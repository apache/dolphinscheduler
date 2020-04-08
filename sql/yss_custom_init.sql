


---------------------------  Custom SQL start  :  create by ZhangLong  --------------------------

DROP TABLE IF EXISTS `t_yss_calendar`;
CREATE TABLE `t_yss_calendar` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `name` varchar(100) NOT NULL COMMENT 'calendar name',
  `start_time` datetime NOT NULL COMMENT 'start time',
  `end_time` datetime  NOT NULL COMMENT 'end time',
  `flag` tinyint(4) DEFAULT NULL COMMENT '0 not available, 1 available',
  `desc` varchar(200) DEFAULT NULL COMMENT 'calendar description',
  `user_id` int(11) DEFAULT NULL COMMENT 'creator id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `t_yss_calendar_details`;
CREATE TABLE `t_yss_calendar_details` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
  `calendar_id` int(11) NOT NULL COMMENT 'calendar id',
  `stamp` date NOT NULL COMMENT 'calendar date stamp ',
  `flag` tinyint(4) DEFAULT NULL COMMENT '0 not available, 1 available',
  `user_id` int(11) DEFAULT NULL COMMENT 'creator id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'update time',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



UPDATE `t_ds_user` SET `user_password` = '21232f297a57a5a743894a0e4a801fc3' WHERE (`id` = '1');




---------------------------   Custom SQL end  :  create by ZhangLong     --------------------------

