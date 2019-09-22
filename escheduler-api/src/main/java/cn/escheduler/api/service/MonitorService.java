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
import cn.escheduler.api.utils.ZookeeperMonitor;
import cn.escheduler.common.enums.ZKNodeType;
import cn.escheduler.dao.MonitorDBDao;
import cn.escheduler.common.model.MasterServer;
import cn.escheduler.dao.model.MonitorRecord;
import cn.escheduler.dao.model.User;
import cn.escheduler.dao.model.ZookeeperRecord;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * monitor service
 */
@Service
public class MonitorService extends BaseService{

  /**
   * query database state
   *
   * @return
   */
  public Map<String,Object> queryDatabaseState(User loginUser) {
    Map<String, Object> result = new HashMap<>(5);

    List<MonitorRecord> monitorRecordList = MonitorDBDao.queryDatabaseState();

    result.put(Constants.DATA_LIST, monitorRecordList);
    putMsg(result, Status.SUCCESS);

    return result;

  }

  /**
   * query master list
   *
   * @param loginUser
   * @return
   */
  public Map<String,Object> queryMaster(User loginUser) {

    Map<String, Object> result = new HashMap<>(5);

    List<MasterServer> masterServers = getServerListFromZK(true);
    result.put(Constants.DATA_LIST, masterServers);
    putMsg(result,Status.SUCCESS);

    return result;
  }

  /**
   * query zookeeper state
   *
   * @return
   */
  public Map<String,Object> queryZookeeperState(User loginUser) {
    Map<String, Object> result = new HashMap<>(5);

    List<ZookeeperRecord> zookeeperRecordList = ZookeeperMonitor.zookeeperInfoList();

    result.put(Constants.DATA_LIST, zookeeperRecordList);
    putMsg(result, Status.SUCCESS);

    return result;

  }


  /**
   * query master list
   *
   * @param loginUser
   * @return
   */
  public Map<String,Object> queryWorker(User loginUser) {

    Map<String, Object> result = new HashMap<>(5);
    List<MasterServer> workerServers = getServerListFromZK(false);

    result.put(Constants.DATA_LIST, workerServers);
    putMsg(result,Status.SUCCESS);

    return result;
  }

  private List<MasterServer> getServerListFromZK(boolean isMaster){
    List<MasterServer> servers = new ArrayList<>();
    ZookeeperMonitor zookeeperMonitor = null;
    try{
      zookeeperMonitor = new ZookeeperMonitor();
      ZKNodeType zkNodeType = isMaster ? ZKNodeType.MASTER : ZKNodeType.WORKER;
      servers = zookeeperMonitor.getServersList(zkNodeType);
    }catch (Exception e){
      throw e;
    }finally {
      if(zookeeperMonitor != null){
        zookeeperMonitor.close();
      }
    }
    return servers;
  }

}
