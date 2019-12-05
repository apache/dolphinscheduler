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

import cn.escheduler.common.utils.OSUtils;
import cn.escheduler.dao.datasource.ConnectionFactory;
import cn.escheduler.common.model.MasterServer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

@SpringBootTest
public class MasterServerMapperTest {

    @Autowired
    MasterServerMapper masterServerMapper;

    @Before
    public void before(){
        masterServerMapper = ConnectionFactory.getSqlSession().getMapper(MasterServerMapper.class);
    }

    @Test
    public void queryAllMaster() {

        MasterServer masterServer = new MasterServer();
        String host = OSUtils.getHost();
        masterServer.setHost(host);
        masterServer.setLastHeartbeatTime(new Date());
        masterServer.setPort(19282);
        masterServer.setCreateTime(new Date());
        masterServer.setZkDirectory("/escheduler/masters/" + host + "_0000000001");

        masterServerMapper.insert(masterServer);
        Assert.assertNotEquals(masterServer.getId(), 0);


        masterServer.setPort(12892);
        int update = masterServerMapper.update(masterServer);
        Assert.assertEquals(update, 1);


        List<MasterServer> masterServers = masterServerMapper.queryAllMaster();

        MasterServer findMaster = null;
        for(MasterServer master : masterServers){
            if(master.getId() == masterServer.getId()){
                findMaster = master;
            }
        }
        Assert.assertNotEquals(findMaster, null);

        int delete = masterServerMapper.deleteWorkerByHost(host);
        Assert.assertEquals(delete, 1);


    }

}