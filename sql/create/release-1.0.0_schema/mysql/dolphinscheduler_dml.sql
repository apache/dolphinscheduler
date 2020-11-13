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

-- Records of t_escheduler_user,user : admin , password : dolphinscheduler123
INSERT INTO `qrtz_locks`
VALUES ('DolphinScheduler', 'TRIGGER_ACCESS');

INSERT INTO `qrtz_scheduler_state`
VALUES ('DolphinScheduler', 'node1171605170605170', '1605260913526', '5000');

-- ----------------------------
-- Records of t_ds_access_token
-- ----------------------------
INSERT INTO `t_ds_access_token`
VALUES ('2', '2', '3f4d80bc5f5da6da375716b4bd7125d2', '2100-04-30 11:00:00', '2020-11-13 03:47:37',
        '2020-11-13 03:47:37');

        -- ----------------------------
-- Records of t_ds_alertgroup
-- ----------------------------
INSERT INTO `t_ds_alertgroup`
VALUES ('1', 'default admin warning group', '0', 'default admin warning group', '2018-11-29 10:20:39',
        '2018-11-29 10:20:39');

        -- ----------------------------
-- Records of t_ds_project
-- ----------------------------
INSERT INTO `t_ds_project`
VALUES ('2', 'athena', '', '2', '1', '2020-11-13 03:48:03', '2020-11-13 03:48:03');

-- ----------------------------
-- Records of t_ds_queue
-- ----------------------------
INSERT INTO `t_ds_queue`
VALUES ('1', 'default', 'default', null, null);

-- ----------------------------
-- Records of t_ds_relation_user_alertgroup
-- ----------------------------
INSERT INTO `t_ds_relation_user_alertgroup`
VALUES ('1', '1', '1', '2018-11-29 10:22:33', '2018-11-29 10:22:33');


-- ----------------------------
-- Records of t_ds_tenant
-- ----------------------------
INSERT INTO `t_ds_tenant`
VALUES ('1', 'root', 'root', '', '1', '2020-11-13 03:45:47', '2020-11-13 03:45:47');


-- ----------------------------
-- Records of t_ds_user
-- ----------------------------
INSERT INTO `t_ds_user`
VALUES ('1', 'admin', '7ad2410b2f4c074479a8937a28a22b8f', '0', 'xxx@qq.com', '', '0', '2018-03-27 15:48:50',
        '2018-10-24 17:40:22', null);
INSERT INTO `t_ds_user`
VALUES ('2', 'athena', '0d251e25ebcc810ddcecf1f698ac51f8', '1', 'xxx@qq.com', '', '1', '2020-11-13 03:46:19',
        '2020-11-13 03:46:19', 'default');


-- ----------------------------
-- Records of t_ds_version
-- ----------------------------
INSERT INTO `t_ds_version`
VALUES ('1', '1.3.0');
