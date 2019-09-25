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

import cn.escheduler.common.enums.UserType;
import cn.escheduler.dao.datasource.ConnectionFactory;
import cn.escheduler.dao.entity.User;
import com.baomidou.mybatisplus.annotation.TableField;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserMapperTest {


    @Autowired
    private UserMapper userMapper;

    @Test
    public void testInsert(){
        User user = new User();
        user.setUserName("user1");
        user.setUserPassword("1");
        user.setEmail("xx@123.com");
        user.setUserType(UserType.GENERAL_USER);
        user.setCreateTime(new Date());
        user.setTenantId(1);
        user.setUpdateTime(new Date());
        int res = userMapper.insert(user);
        Assert.assertEquals(res,1);
    }


    @Test
    public void testQueryAllGeneralUser() {
        List<User> users = userMapper.queryAllGeneralUser();
        System.out.println(users.size());
        List<User> user1 = userMapper.selectList(null);
        System.out.println(user1.size());
    }

    @Test
    public void testQueryByUserNameAccurately() {
    }

    @Test
    public void testQueryUserByNamePassword() {
    }

    @Test
    public void testQueryUserPaging() {
    }

    @Test
    public void testGetDetailsById() {
    }

    @Test
    public void testQueryUserListByAlertGroupId() {
    }

    @Test
    public void testQueryTenantCodeByUserId() {
    }

    @Test
    public void testQueryUserByToken() {
    }

    @Test
    public void testQueryAllGeneralUser1() {
    }

    @Test
    public void testQueryByUserNameAccurately1() {
    }

    @Test
    public void testQueryUserByNamePassword1() {
    }

    @Test
    public void testQueryUserPaging1() {
    }

    @Test
    public void testGetDetailsById1() {
    }

    @Test
    public void testQueryUserListByAlertGroupId1() {
    }

    @Test
    public void testQueryTenantCodeByUserId1() {
    }

    @Test
    public void testQueryUserByToken1() {
    }
}