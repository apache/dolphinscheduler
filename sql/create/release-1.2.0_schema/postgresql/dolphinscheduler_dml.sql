-- Records of t_escheduler_user,user : admin , password : escheduler123
INSERT INTO "t_escheduler_user" VALUES ('1', 'admin', '055a97b5fcd6d120372ad1976518f371', '0', 'xxx@qq.com', 'xx', '0', '2018-03-27 15:48:50', '2018-10-24 17:40:22');
INSERT INTO "t_escheduler_alertgroup" VALUES (1, 'escheduler管理员告警组', '0', 'escheduler管理员告警组','2018-11-29 10:20:39', '2018-11-29 10:20:39');
INSERT INTO "t_escheduler_relation_user_alertgroup" VALUES ('1', '1', '1', '2018-11-29 10:22:33', '2018-11-29 10:22:33');

-- Records of t_escheduler_queue,default queue name : default
INSERT INTO "t_escheduler_queue" VALUES ('1', 'default', 'default');
INSERT INTO "t_escheduler_version" VALUES ('1', '1.2.0');