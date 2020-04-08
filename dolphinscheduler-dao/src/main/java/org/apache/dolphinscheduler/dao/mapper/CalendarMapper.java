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

import java.util.List;

import org.apache.dolphinscheduler.dao.entity.Calendar;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * Calendar mapper interface
 */
public interface CalendarMapper extends BaseMapper<Calendar> {

    /**
     * tenant page
     * @param page page
     * @param searchVal searchVal
     * @return tenant IPage
     */
    IPage<Calendar> queryCalendarPaging(IPage<Calendar> page,
            @Param("searchVal") String searchVal);
    /**
     * query Calendar by name
     * @param  name
     * @return Calendar list
     */
    List<Calendar> queryByCalendarName(@Param("name") String name);



}
