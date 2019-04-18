-- 用户指定队列
alter table t_escheduler_user add queue varchar(64);

-- 访问token
CREATE TABLE `t_escheduler_access_token` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int(11) DEFAULT NULL COMMENT '用户id',
  `token` varchar(64) DEFAULT NULL COMMENT 'token令牌',
  `expire_time` datetime DEFAULT NULL COMMENT 'token有效结束时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;