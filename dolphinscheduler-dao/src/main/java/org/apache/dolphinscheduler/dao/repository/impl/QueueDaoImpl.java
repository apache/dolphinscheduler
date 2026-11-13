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

package org.apache.dolphinscheduler.dao.repository.impl;

import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.mapper.QueueMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.QueueDao;

import java.util.List;

import lombok.NonNull;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.metadata.IPage;

@Repository
public class QueueDaoImpl extends BaseDao<Queue, QueueMapper> implements QueueDao {

    public QueueDaoImpl(@NonNull QueueMapper queueMapper) {
        super(queueMapper);
    }

    @Override
    public IPage<Queue> queryQueuePaging(IPage<Queue> page, List<Integer> ids, String searchVal) {
        return mybatisMapper.queryQueuePaging(page, ids, searchVal);
    }

    @Override
    public boolean existQueue(String queue, String queueName) {
        return Boolean.TRUE.equals(mybatisMapper.existQueue(queue, queueName));
    }

    @Override
    public Queue queryQueueName(String queue, String queueName) {
        return mybatisMapper.queryQueueName(queue, queueName);
    }
}
