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
import cn.escheduler.dao.mapper.QueueMapper;
import cn.escheduler.dao.model.Queue;
import cn.escheduler.dao.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * queue service
 */
@Service
public class QueueService extends BaseService{


  @Autowired
  private QueueMapper queueMapper;

  /**
   * query queue list
   *
   * @param loginUser
   * @return
   */
  public Map<String, Object> queryList(User loginUser) {
    Map<String, Object> result = new HashMap<>(5);
    if (checkAdmin(loginUser, result)) {
      return result;
    }

    List<Queue> queueList = queueMapper.queryAllQueue();
    result.put(Constants.DATA_LIST, queueList);
    putMsg(result,Status.SUCCESS);

    return result;
  }

}
