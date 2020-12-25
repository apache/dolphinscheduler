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

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.CommandCount;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * command mapper interface
 */
public interface CommandMapper extends BaseMapper<Command> {


    /**
     * get one command
     * @return command
     */
    Command getOneToRun();

    /**
     * count command state
     * @param userId userId
     * @param startTime startTime
     * @param endTime endTime
     * @param projectIdArray projectIdArray
     * @return CommandCount list
     */
    List<CommandCount> countCommandState(
            @Param("userId") int userId,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("projectIdArray") Integer[] projectIdArray);



}
