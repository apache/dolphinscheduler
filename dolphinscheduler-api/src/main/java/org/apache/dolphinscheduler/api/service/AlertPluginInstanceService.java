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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.vo.AlertPluginInstanceVO;
import org.apache.dolphinscheduler.common.enums.AlertPluginInstanceType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.List;

/**
 * alert plugin instance service
 */
public interface AlertPluginInstanceService {

    /**
     * creat alert plugin instance
     *
     * @param loginUser login user
     * @param pluginDefineId plugin define id
     * @param instanceName instance name
     * @param pluginInstanceParams plugin instance params
     * @return result
     */
    AlertPluginInstance create(User loginUser,
                               int pluginDefineId,
                               String instanceName,
                               AlertPluginInstanceType instanceType,
                               WarningType warningType,
                               String pluginInstanceParams);

    /**
     * update
     * @param loginUser login user
     * @param alertPluginInstanceId plugin instance id
     * @param instanceName instance name
     * @param pluginInstanceParams plugin instance params
     * @return result
     */
    AlertPluginInstance updateById(User loginUser,
                                   int alertPluginInstanceId,
                                   String instanceName,
                                   WarningType warningType,
                                   String pluginInstanceParams);

    /**
     * delete alert plugin instance
     *
     * @param loginUser login user
     * @param alertPluginInstanceId id
     * @return result
     */
    void deleteById(User loginUser, int alertPluginInstanceId);

    /**
     * get alert plugin instance
     *
     * @param loginUser login user
     * @param id get id
     * @return alert plugin
     */
    AlertPluginInstance getById(User loginUser, int id);

    /**
     * queryAll
     *
     * @return alert plugins
     */
    List<AlertPluginInstanceVO> queryAll();

    /**
     * checkExistPluginInstanceName
     * @param pluginName plugin name
     * @return isExist
     */
    boolean checkExistPluginInstanceName(String pluginName);

    /**
     * queryPluginPage
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo    page index
     * @param pageSize  page size
     * @return plugins
     */
    PageInfo<AlertPluginInstanceVO> listPaging(User loginUser, String searchVal, int pageNo, int pageSize);

    void testSend(int pluginDefineId, String pluginInstanceParams);
}
