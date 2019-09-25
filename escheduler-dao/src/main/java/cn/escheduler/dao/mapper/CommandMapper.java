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
package cn.escheduler.dao.mapper;

import cn.escheduler.dao.entity.Command;
import cn.escheduler.dao.entity.CommandCount;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

public interface CommandMapper extends BaseMapper<Command> {



    @Select("select * from t_escheduler_command ${ew.customSqlSegment}")
    List<Command> getAll(@Param(Constants.WRAPPER) Wrapper wrapper);

    Command getOneToRun();

    List<CommandCount> countCommandState(
            @Param("userId") int userId,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("projectIdString") String projectIdString);



}
