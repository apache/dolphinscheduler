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
package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.ZookeeperMonitor;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ZKNodeType;
import org.apache.dolphinscheduler.dao.MonitorDBDao;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.entity.MonitorRecord;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.ZookeeperRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.dolphinscheduler.common.utils.Preconditions.*;

/**
 * monitor service
 */
@Service
public class MonitorService extends BaseService{

  @Autowired
  private ZookeeperMonitor zookeeperMonitor;

  @Autowired
  private MonitorDBDao monitorDBDao;
  /**
   * query database state
   *
   * @param loginUser login user
   * @return data base state
   */
  public Map<String,Object> queryDatabaseState(User loginUser) {
    Map<String, Object> result = new HashMap<>(5);

    List<MonitorRecord> monitorRecordList = monitorDBDao.queryDatabaseState();

    result.put(Constants.DATA_LIST, monitorRecordList);
    putMsg(result, Status.SUCCESS);

    return result;

  }

  /**
   * query master list
   *
   * @param loginUser login user
   * @return master information list
   */
  public Map<String,Object> queryMaster(User loginUser) {

    Map<String, Object> result = new HashMap<>(5);

    List<Server> masterServers = getServerListFromZK(true);
    result.put(Constants.DATA_LIST, masterServers);
    putMsg(result,Status.SUCCESS);

    return result;
  }

  /**
   * query zookeeper state
   *
   * @param loginUser login user
   * @return zookeeper information list
   */
  public Map<String,Object> queryZookeeperState(User loginUser) {
    Map<String, Object> result = new HashMap<>(5);

    List<ZookeeperRecord> zookeeperRecordList = zookeeperMonitor.zookeeperInfoList();

    result.put(Constants.DATA_LIST, zookeeperRecordList);
    putMsg(result, Status.SUCCESS);

    return result;

  }


  /**
   * query worker list
   *
   * @param loginUser login user
   * @return worker information list
   */
  public Map<String,Object> queryWorker(User loginUser) {

    Map<String, Object> result = new HashMap<>(5);
    List<Server> masterServers = getServerListFromZK(false);

    result.put(Constants.DATA_LIST, masterServers);
    putMsg(result,Status.SUCCESS);

    return result;
  }

  public List<Server> getServerListFromZK(boolean isMaster){

    checkNotNull(zookeeperMonitor);
    ZKNodeType zkNodeType = isMaster ? ZKNodeType.MASTER : ZKNodeType.WORKER;
    return zookeeperMonitor.getServersList(zkNodeType);
  }

}
