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

import org.apache.dolphinscheduler.common.enums.AlertStatus;
import org.apache.dolphinscheduler.dao.entity.ListenerEvent;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface ListenerEventMapper extends BaseMapper<ListenerEvent> {

    int batchInsert(@Param("events") List<ListenerEvent> events);

    void insertServerDownEvent(@Param("event") ListenerEvent event,
                               @Param("crashAlarmSuppressionStartTime") Date crashAlarmSuppressionStartTime);

    List<ListenerEvent> listingListenerEventByStatus(@Param("minId") int minId,
                                                     @Param("postStatus") int postStatus,
                                                     @Param("limit") int limit);

    void updateListenerEvent(@Param("eventId") int eventId, @Param("postStatus") AlertStatus postStatus,
                             @Param("log") String log, @Param("updateTime") Date updateTime);
}
