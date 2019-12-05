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

import cn.escheduler.dao.datasource.ConnectionFactory;
import cn.escheduler.dao.model.UserAlertGroup;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

public class UserAlertGroupMapperTest {


    @Autowired
    UserAlertGroupMapper userAlertGroupMapper;

    @Before
    public void before(){
        userAlertGroupMapper = ConnectionFactory.getSqlSession().getMapper(UserAlertGroupMapper.class);
    }

    @Test
    public void testInsert(){
        UserAlertGroup userAlertGroup = new UserAlertGroup();
        userAlertGroup.setUserId(2);
        userAlertGroup.setAlertgroupId(10021);
        userAlertGroup.setCreateTime(new Date());
        userAlertGroup.setUpdateTime(new Date());
        userAlertGroupMapper.insert(userAlertGroup);
        Assert.assertNotEquals(userAlertGroup.getId(), 0);


        int delete = userAlertGroupMapper.deleteByAlertgroupId(userAlertGroup.getAlertgroupId());
        Assert.assertEquals(delete, 1);
    }


}
