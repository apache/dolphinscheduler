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


import java.io.Serializable;
import java.util.List;
import java.util.Map;


import org.apache.dolphinscheduler.dao.entity.GlobalVariable;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yss.henghe.platform.tools.constraint.SourceCodeConstraint;

/**
 * Variable mapper interface
 */
@SourceCodeConstraint.AddedBy(SourceCodeConstraint.Author.ZHANGLONG)
public interface GlobalVariableMapper extends BaseMapper<GlobalVariable> {

    /**
     * tenant page
     * @param page page
     * @param searchVal searchVal
     * @return tenant IPage
     */
    IPage<GlobalVariable> queryVariablePaging(IPage<GlobalVariable> page,
            @Param("projectId") int projectId, @Param("searchVal") String searchVal);
    /**
     * query Variable
     * @param  name
     * @return Variable list
     */
    List<GlobalVariable> queryList(@Param("projectId") int projectId, @Param("key") String key , @Param("name") String name );

    /**
     *
     * @param id
     * @param projectId
     * @return
     */
    GlobalVariable selectById(@Param("projectId") int projectId, @Param("id") int id );

    /**
     *
     * @param projectId
     * @return
     */
    Map<String,String> queryMap( @Param("projectId") int projectId );

    /**
     *
     * @param key
     * @param projectId
     * @return
     */
    List<GlobalVariable> queryByVariableKey( @Param("projectId") int projectId , @Param("key") String key );

}
