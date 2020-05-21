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
     */
    String getNameCh();

    /**
     * english name
     * this alert plugin name eg: email , message ...
     * name can often be displayed on the page ui eg : email , message , MR , spark , hive ...
     * @return
     */
    String getNameEn();

    /**
     * id needs to be filled in the configuration file and is used by pluginload to determine whether to enable the plugin
     * @return
     */
    String getId();


    /**
     * Returns the configurable parameters that this plugin needs to display on the web ui
     * @return
     */
    String getParams();

    AlertChannel create(Map<String, String> config);
}
