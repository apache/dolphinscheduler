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

package org.apache.dolphinscheduler.spi.alert;

import org.apache.dolphinscheduler.spi.params.AbsPluginParams;

import java.util.List;
import java.util.Map;

/**
 * Each AlertPlugin need implement this interface
 * @author gaojun
 */
public interface AlertChannelFactory {

    /**
     * chinese name
     * this alert plugin name eg: email , message ...
     * name can often be displayed on the page ui eg : email , message , MR , spark , hive ...
     * @return
     *  this alert plugin chinese name
     */
    String getNameCh();

    /**
     * english name
     * this alert plugin name eg: email , message ...
     * name can often be displayed on the page ui eg : email , message , MR , spark , hive ...
     * @return
     *  this alert plugin english name
     */
    String getNameEn();

    /**
     * id should be used as the configuration file name of the plugin.
     * eg: If you develop and use an alert plugin named email_alert, you should add a configuration file in the conf/alert directory as email_alert.properties
     * and the name of the file should be equal to the return value of the 'getId()' method , like 'email_alert'
     * @return
     *  this alert plugin id
     */
    String getId();


    /**
     * Returns the configurable parameters that this plugin needs to display on the web ui
     * @return
     *  this alert plugin params
     */
    List<AbsPluginParams> getParams();

    /**
     * The parameters configured in the alert / xxx.properties file will be in the config map
     * @param config configured in the alert / xxx.properties file will be in the config map
     * @return
     *  AlertChannel
     */
    AlertChannel create(Map<String, String> config);
}
