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
package org.apache.dolphinscheduler.dao.entity;

import java.util.Date;
import java.util.Objects;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("t_ds_queue")
public class Queue {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * queue name
     */
    private String queueName;
    /**
     * yarn queue name
     */
    private String queue;

    /**
     * create time
     */
    private Date createTime;
    /**
     * update time
     */
    private Date updateTime;

    public Queue() {
    }

    public Queue(String queueName, String queue) {
        Date now = new Date();
        this.queueName = queueName;
        this.queue = queue;
        this.createTime = now;
        this.updateTime = now;
    }

    public Queue(int id, String queueName, String queue) {
        Date now = new Date();
        this.id = id;
        this.queueName = queueName;
        this.queue = queue;
        this.createTime = now;
        this.updateTime = now;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Queue queue1 = (Queue) o;

        if (!Objects.equals(id, queue1.id)) {
            return false;
        }
        if (!queueName.equals(queue1.queueName)) {
            return false;
        }
        return queue.equals(queue1.queue);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + queueName.hashCode();
        result = 31 * result + queue.hashCode();
        return result;
    }
}
