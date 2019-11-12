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
package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * alertgroup mapper interface
 */
public interface AlertGroupMapper extends BaseMapper<AlertGroup> {


    /**
     * alertgroup page
     * @param page page
     * @param groupName groupName
     * @return alertgroup Ipage
     */
    IPage<AlertGroup> queryAlertGroupPage(Page page,
                                          @Param("groupName") String groupName);


    /**
     * query by group name
     * @param groupName groupName
     * @return alertgroup list
     */
    List<AlertGroup> queryByGroupName(@Param("groupName") String groupName);

    /**
     * query by userId
     * @param userId userId
     * @return alertgroup list
     */
    List<AlertGroup> queryByUserId(@Param("userId") int userId);


    /**
     * query by alert type
     * @param alertType alertType
     * @return alertgroup list
     */
    List<AlertGroup> queryByAlertType(@Param("alertType") AlertType alertType);

    /**
     * query all group list
     * @return alertgroup list
     */
    List<AlertGroup> queryAllGroupList();
}
