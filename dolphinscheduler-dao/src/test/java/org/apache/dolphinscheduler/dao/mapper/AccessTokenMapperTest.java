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

import org.apache.dolphinscheduler.dao.entity.AccessToken;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
public class AccessTokenMapperTest {


    @Resource
    AccessTokenMapper accessTokenMapper;


    /**
     * insert
     * @return AccessToken
     */
    private AccessToken insertOne(){
        //insertOne
        AccessToken accessToken = new AccessToken();
        accessToken.setUserId(4);
        accessToken.setToken("hello, access token");
        accessToken.setCreateTime(new Date());
        accessToken.setUpdateTime(new Date());
        accessToken.setExpireTime(new Date());
        accessTokenMapper.insert(accessToken);
        return accessToken;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate(){
        //insertOne
        AccessToken accessToken = insertOne();
        //update
        accessToken.setToken("hello, token");
        int update = accessTokenMapper.updateById(accessToken);
        accessTokenMapper.deleteById(accessToken.getId());
        Assert.assertEquals(update, 1);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){

        AccessToken accessToken = insertOne();
        int delete = accessTokenMapper.deleteById(accessToken.getId());
        Assert.assertEquals(delete, 1);
    }

    /**
     * test query
     */
    @Test
    public void testQuery(){

        AccessToken accessToken = insertOne();
        //query
        List<AccessToken> token = accessTokenMapper.selectList(null);
        Assert.assertNotEquals(token.size(), 0);
        accessTokenMapper.deleteById(accessToken.getId());
    }

    /**
     * test page
     */
    @Test
    public void testSelectAccessTokenPage() {
        AccessToken accessToken = insertOne();
        Page page = new Page(1, 3);
        String userName = "";
        IPage<AccessToken> accessTokenPage = accessTokenMapper.selectAccessTokenPage(page, userName, 4);
        Assert.assertNotEquals(accessTokenPage.getTotal(), 0);
        accessTokenMapper.deleteById(accessToken.getId());
    }


}