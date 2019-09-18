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
import cn.escheduler.dao.model.AccessToken;
import cn.escheduler.dao.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

/**
 * access token test
 */
public class AccessTokenMapperTest {


    AccessTokenMapper accessTokenMapper;

    @Before
    public void before(){
        accessTokenMapper = ConnectionFactory.getSqlSession().getMapper(AccessTokenMapper.class);
    }

    @Test
    public void testInsert(){
        AccessToken accessToken = new AccessToken();
        accessToken.setUserId(10);
        accessToken.setExpireTime(new Date());
        accessToken.setToken("ssssssssssssssssssssssssss");
        accessToken.setCreateTime(new Date());
        accessToken.setUpdateTime(new Date());
        accessTokenMapper.insert(accessToken);
    }

    @Test
    public void testListPaging(){
        Integer count = accessTokenMapper.countAccessTokenPaging(1,"");
        Assert.assertTrue( count >= 0);

        List<AccessToken> accessTokenList = accessTokenMapper.queryAccessTokenPaging(1,"", 0, 2);

        Assert.assertTrue( accessTokenList.size() >= 0);
    }




}
