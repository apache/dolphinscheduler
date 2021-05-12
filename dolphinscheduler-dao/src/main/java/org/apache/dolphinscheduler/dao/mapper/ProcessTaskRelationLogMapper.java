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

import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * process task relation log mapper interface
 */
public interface ProcessTaskRelationLogMapper extends BaseMapper<ProcessTaskRelationLog> {

    /**
     * query process task relation log
     *
     * @param processCode process definition code
     * @param processVersion process version
     * @return process task relation log
     */
    List<ProcessTaskRelationLog> queryByProcessCodeAndVersion(@Param("processCode") long processCode,
                                                              @Param("processVersion") int processVersion);

    List<ProcessTaskRelationLog> queryByTaskRelationList(@Param("processCode") long processCode,
                                                         @Param("processVersion") int processVersion,
                                                         @Param("taskCode") long taskCode,
                                                         @Param("taskVersion") long taskVersion);
}
