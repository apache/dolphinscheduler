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


import org.apache.dolphinscheduler.dao.entity.Session;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class SessionMapperTest {

    @Autowired
    SessionMapper sessionMapper;

    /**
     * insert
     * @return Session
     */
    private Session insertOne(){
        //insertOne
        Session session = new Session();
        session.setId(UUID.randomUUID().toString());
        session.setLastLoginTime(new Date());
        session.setUserId(11111);
        sessionMapper.insert(session);
        return session;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate(){
        //insertOne
        Session session = insertOne();
        session.setLastLoginTime(new Date());
        //update
        int update = sessionMapper.updateById(session);
        Assert.assertEquals(update, 1);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){
        Session session = insertOne();
        int delete = sessionMapper.deleteById(session.getId());
        Assert.assertEquals(delete, 1);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        Session session = insertOne();
        //query
        List<Session> sessions = sessionMapper.selectList(null);
        Assert.assertNotEquals(sessions.size(), 0);
    }

    /**
     * test query by userId
     */
    @Test
    public void testQueryByUserId() {
        Session session = insertOne();
        List<Session> sessions = sessionMapper.queryByUserId(session.getUserId());
        Assert.assertNotEquals(sessions.size(), 0);

    }
}