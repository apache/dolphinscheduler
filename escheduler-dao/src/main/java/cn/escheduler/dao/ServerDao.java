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
package cn.escheduler.dao;

import cn.escheduler.common.model.MasterServer;
import cn.escheduler.dao.mapper.MasterServerMapper;
import cn.escheduler.dao.mapper.WorkerServerMapper;
import cn.escheduler.dao.model.WorkerServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

import static cn.escheduler.dao.datasource.ConnectionFactory.getMapper;

/**
 * server dao
 */
@Component
public class ServerDao extends AbstractBaseDao {

    @Autowired
    MasterServerMapper masterServerMapper;


    @Autowired
    WorkerServerMapper workerServerMapper;

    @Override
    protected void init() {
        masterServerMapper = getMapper(MasterServerMapper.class);
        workerServerMapper = getMapper(WorkerServerMapper.class);
    }

    /**
     * register master
     *
     * @param host
     * @param port
     * @param zkDirectory
     * @param resInfo
     * @param createTime
     * @param lastHeartbeatTime
     * @return
     */
    public int registerMaster(String host, int port , String  zkDirectory , String resInfo ,
                              Date createTime , Date lastHeartbeatTime) {

        MasterServer masterServer = new MasterServer();

        masterServer.setHost(host);
        masterServer.setPort(port);
        masterServer.setZkDirectory(zkDirectory);
        masterServer.setResInfo(resInfo);
        masterServer.setCreateTime(createTime);
        masterServer.setLastHeartbeatTime(lastHeartbeatTime);

        return masterServerMapper.insert(masterServer);
    }

    /**
     * update master
     *
     * @param host
     * @param port
     * @param resInfo
     * @param lastHeartbeatTime
     * @return
     */
    public int updateMaster(String host, int port , String resInfo , Date lastHeartbeatTime) {

        MasterServer masterServer = new MasterServer();

        masterServer.setHost(host);
        masterServer.setPort(port);
        masterServer.setResInfo(resInfo);
        masterServer.setLastHeartbeatTime(lastHeartbeatTime);

        return masterServerMapper.update(masterServer);
    }

    /**
     * delete master
     *
     * @param host
     * @return
     */
    public int deleteMaster(String host) {
        return masterServerMapper.deleteWorkerByHost(host);
    }

    /**
     * register master
     * @param host
     * @param port
     * @param zkDirectory
     * @param resInfo
     * @param createTime
     * @param lastHeartbeatTime
     * @return
     */
    public int registerWorker(String host, int port , String  zkDirectory , String resInfo ,
                              Date createTime , Date lastHeartbeatTime) {

        WorkerServer workerServer = new WorkerServer();

        workerServer.setHost(host);
        workerServer.setPort(port);
        workerServer.setZkDirectory(zkDirectory);
        workerServer.setResInfo(resInfo);
        workerServer.setCreateTime(createTime);
        workerServer.setLastHeartbeatTime(lastHeartbeatTime);

        return workerServerMapper.insert(workerServer);
    }

    /**
     *
     * update worker
     * @param host
     * @param port
     * @param resInfo
     * @param lastHeartbeatTime
     * @return
     */
    public int updateWorker(String host, int port , String resInfo , Date lastHeartbeatTime) {

        WorkerServer workerServer = new WorkerServer();

        workerServer.setHost(host);
        workerServer.setPort(port);
        workerServer.setResInfo(resInfo);
        workerServer.setLastHeartbeatTime(lastHeartbeatTime);

        return workerServerMapper.update(workerServer);
    }



    /**
     * delete worker by host
     *
     * @param host
     * @return
     */
    public int deleteWorker(String host) {
        return workerServerMapper.deleteWorkerByHost(host);
    }

}
