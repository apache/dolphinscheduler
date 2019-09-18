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
package cn.escheduler.api.service;

import cn.escheduler.api.enums.Status;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.dao.mapper.MasterServerMapper;
import cn.escheduler.dao.mapper.WorkerServerMapper;
import cn.escheduler.common.model.MasterServer;
import cn.escheduler.dao.model.User;
import cn.escheduler.dao.model.WorkerServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * server service
 */
@Service
public class ServerService extends BaseService{


    @Autowired
    MasterServerMapper masterServerMapper;

    @Autowired
    WorkerServerMapper workerServerMapper;

    /**
     * query master list
     *
     * @param loginUser
     * @return
     */
    public Map<String,Object> queryMaster(User loginUser) {

        Map<String, Object> result = new HashMap<>(5);

        List<MasterServer> masterList = masterServerMapper.queryAllMaster();
        result.put(Constants.DATA_LIST, masterList);
        putMsg(result,Status.SUCCESS);

        return result;
    }

    /**
     * query worker list
     *
     * @param loginUser
     * @return
     */
    public Map<String,Object> queryWorker(User loginUser) {
        Map<String, Object> result = new HashMap<>();

        List<WorkerServer> workerList = workerServerMapper.queryAllWorker();
        result.put(Constants.DATA_LIST, workerList);
        putMsg(result,Status.SUCCESS);
        return result;
    }
}
