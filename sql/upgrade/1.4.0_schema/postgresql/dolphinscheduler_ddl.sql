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
DROP TABLE IF EXISTS t_ds_plugin_define;
CREATE TABLE t_ds_plugin_define (
	id serial NOT NULL,
	plugin_name varchar(100) NOT NULL,
	plugin_type varchar(100) NOT NULL,
	plugin_params text NULL,
	create_time timestamp NULL,
	update_time timestamp NULL,
	CONSTRAINT t_ds_plugin_define_pk PRIMARY KEY (id),
	CONSTRAINT t_ds_plugin_define_un UNIQUE (plugin_name, plugin_type)
);


DROP TABLE IF EXISTS t_ds_alert_plugin_instance;
CREATE TABLE t_ds_alert_plugin_instance (
	id serial NOT NULL,
	plugin_define_id int4 NOT NULL,
	plugin_instance_params text NULL,
	create_time timestamp NULL,
	update_time timestamp NULL,
	alert_group_id int4 NOT NULL,
	instance_name varchar(200) NULL,
	CONSTRAINT t_ds_alert_plugin_instance_pk PRIMARY KEY (id)
);
