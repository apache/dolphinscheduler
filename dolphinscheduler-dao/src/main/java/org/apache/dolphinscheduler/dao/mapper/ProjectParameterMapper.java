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

import org.apache.dolphinscheduler.dao.entity.ProjectParameter;

import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface ProjectParameterMapper extends BaseMapper<ProjectParameter> {

    ProjectParameter queryByCode(@Param("code") long code);

    List<ProjectParameter> queryByCodes(@Param("codes") Collection<Long> codes);

    ProjectParameter queryByName(@Param("paramName") String paramName);

    IPage<ProjectParameter> queryProjectParameterListPaging(IPage<ProjectParameter> page,
                                                            @Param("projectCode") long projectCode,
                                                            @Param("projectParameterIds") List<Integer> projectParameterIds,
                                                            @Param("searchName") String searchName,
                                                            @Param("projectParameterDataType") String projectParameterDataType);

    List<ProjectParameter> queryByProjectCode(@Param("projectCode") long projectCode);
}
