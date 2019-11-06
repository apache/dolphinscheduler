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

import cn.escheduler.dao.model.UserInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;
public interface UserInfoMapper {


    @InsertProvider(type = UserInfoMapperProvider.class, method = "insert")
    @Options(useGeneratedKeys = true,keyProperty = "user.id")
    @SelectKey(statement = "SELECT nextval('test.users_id_seq')", keyProperty = "user.id", before = false, resultType = int.class)
    int insert(@Param("user") UserInfo user);

}
