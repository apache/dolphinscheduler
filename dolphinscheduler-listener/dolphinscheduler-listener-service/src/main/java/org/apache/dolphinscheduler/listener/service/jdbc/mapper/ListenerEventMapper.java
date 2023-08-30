/*
 *
 *  * Licensed to Apache Software Foundation (ASF) under one or more contributor
 *  * license agreements. See the NOTICE file distributed with
 *  * this work for additional information regarding copyright
 *  * ownership. Apache Software Foundation (ASF) licenses this file to you under
 *  * the Apache License, Version 2.0 (the "License"); you may
 *  * not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package org.apache.dolphinscheduler.listener.service.jdbc.mapper;

import org.apache.dolphinscheduler.listener.service.jdbc.JdbcListenerEvent;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

public interface ListenerEventMapper extends BaseMapper<JdbcListenerEvent> {

    @Insert({"<script>",
            "        insert into t_ds_listener_event ( content, post_status, event_type, log, plugin_instance_id, create_time, update_time)",
            "        values",
            "        <foreach collection='jdbcListenerEvents' item='jdbcListenerEvent' separator=','>",
            "            (#{jdbcListenerEvent.content},#{jdbcListenerEvent.postStatus}," +
                    "            #{jdbcListenerEvent.eventType},#{jdbcListenerEvent.log},#{jdbcListenerEvent.pluginInstanceId}, #{jdbcListenerEvent.createTime}, #{jdbcListenerEvent.updateTime})"
                    +
                    "        </foreach>",
            "</script>"})
    int batchInsert(@Param("jdbcListenerEvents") List<JdbcListenerEvent> jdbcListenerEvents);
}
