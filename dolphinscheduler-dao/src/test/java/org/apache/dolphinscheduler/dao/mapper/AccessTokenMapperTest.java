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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.AccessToken;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * AccessToken mapper test
 */
public class AccessTokenMapperTest extends BaseDaoTest {

    @Autowired
    private AccessTokenMapper accessTokenMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * test insert
     */
    @Test
    public void testInsert() throws Exception {
        Integer userId = 1;

        AccessToken accessToken = createAccessToken(userId);
        assertThat(accessToken.getId(), greaterThan(0));
    }

    /**
     * test delete AccessToken By UserId
     */
    @Test
    public void testDeleteAccessTokenByUserId() {
        Integer userId = 1;
        int insertCount = 0;

        for (int i = 0; i < 10; i++) {
            try {
                createAccessToken(userId);
                insertCount++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int deleteCount = accessTokenMapper.deleteAccessTokenByUserId(userId);
        Assert.assertEquals(insertCount, deleteCount);
    }


    /**
     * test select by id
     *
     * @throws Exception
     */
    @Test
    public void testSelectById() throws Exception {
        Integer userId = 1;
        AccessToken accessToken = createAccessToken(userId);
        AccessToken resultAccessToken = accessTokenMapper.selectById(accessToken.getId());
        assertEquals(accessToken, resultAccessToken);
    }

    /**
     * test hashCode method
     *
     * @throws Exception
     */
    @Test
    public void testHashCodeMethod() throws Exception {
        Integer userId = 1;
        AccessToken accessToken = createAccessToken(userId);
        AccessToken resultAccessToken = accessTokenMapper.selectById(accessToken.getId());
        boolean flag = accessToken.equals(resultAccessToken);
        assertTrue(flag);
    }

    /**
     * test equals method
     *
     * @throws Exception
     */
    @Test
    public void testEqualsMethod() throws Exception {
        Integer userId = 1;
        AccessToken accessToken = createAccessToken(userId);
        int result = accessToken.hashCode();
        assertNotNull(result);
    }

    /**
     * test page
     */
    @Test
    public void testSelectAccessTokenPage() throws Exception {
        Integer count = 4;
        String userName = "zhangsan";

        Integer offset = 2;
        Integer size = 2;

        Map<Integer, AccessToken> accessTokenMap = createAccessTokens(count, userName);

        Page page = new Page(offset, size);
        IPage<AccessToken> accessTokenPage = accessTokenMapper.selectAccessTokenPage(page, userName, 0);

        assertEquals(Integer.valueOf(accessTokenPage.getRecords().size()), size);

        for (AccessToken accessToken : accessTokenPage.getRecords()) {
            AccessToken resultAccessToken = accessTokenMap.get(accessToken.getId());
            assertEquals(accessToken, resultAccessToken);
        }
    }


    /**
     * test update
     */
    @Test
    public void testUpdate() throws Exception {
        Integer userId = 1;
        AccessToken accessToken = createAccessToken(userId);
        //update
        accessToken.setToken("56789");
        accessToken.setExpireTime(DateUtils.getCurrentDate());
        accessToken.setUpdateTime(DateUtils.getCurrentDate());
        int status = accessTokenMapper.updateById(accessToken);
        if (status != 1) {
            Assert.fail("update access token fail");
        }
        AccessToken resultAccessToken = accessTokenMapper.selectById(accessToken.getId());
        assertEquals(accessToken, resultAccessToken);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete() throws Exception {
        Integer userId = 1;

        AccessToken accessToken = createAccessToken(userId);
        int status = accessTokenMapper.deleteById(accessToken.getId());
        if (status != 1) {
            Assert.fail("delete access token data fail");
        }

        AccessToken resultAccessToken =
                accessTokenMapper.selectById(accessToken.getId());
        assertNull(resultAccessToken);
    }

    /**
     * create accessTokens
     *
     * @param count    create accessToken count
     * @param userName username
     * @return accessToken map
     * @throws Exception
     */
    private Map<Integer, AccessToken> createAccessTokens(
            Integer count, String userName) throws Exception {

        User user = createUser(userName);

        Map<Integer, AccessToken> accessTokenMap = new HashMap<>();
        for (int i = 1; i <= count; i++) {
            AccessToken accessToken = createAccessToken(user.getId(), userName);

            accessTokenMap.put(accessToken.getId(), accessToken);
        }

        return accessTokenMap;
    }

    /**
     * create user
     *
     * @param userName userName
     * @return user
     */
    private User createUser(String userName) {
        User user = new User();
        user.setUserName(userName);
        user.setUserPassword("123");
        user.setUserType(UserType.GENERAL_USER);
        user.setEmail("test@qq.com");
        user.setPhone("13102557272");
        user.setTenantId(1);
        user.setCreateTime(DateUtils.getCurrentDate());
        user.setUpdateTime(DateUtils.getCurrentDate());
        user.setQueue("default");

        int status = userMapper.insert(user);

        if (status != 1) {
            Assert.fail("insert user data error");
        }

        return user;
    }

    /**
     * create access token
     *
     * @param userId   userId
     * @param userName userName
     * @return accessToken
     */
    private AccessToken createAccessToken(Integer userId, String userName) {
        //insertOne
        AccessToken accessToken = new AccessToken();
        accessToken.setUserName(userName);
        accessToken.setUserId(userId);
        accessToken.setToken(String.valueOf(ThreadLocalRandom.current().nextLong()));
        accessToken.setCreateTime(DateUtils.getCurrentDate());
        accessToken.setUpdateTime(DateUtils.getCurrentDate());
        accessToken.setExpireTime(DateUtils.getCurrentDate());

        int status = accessTokenMapper.insert(accessToken);

        if (status != 1) {
            Assert.fail("insert data error");
        }
        return accessToken;
    }

    /**
     * create access token
     *
     * @param userId userId
     * @return accessToken
     * @throws Exception
     */
    private AccessToken createAccessToken(Integer userId) throws Exception {
        return createAccessToken(userId, null);
    }

}