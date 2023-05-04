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

package org.apache.dolphinscheduler.plugin.registry.jdbc.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName(value = "t_ds_jdbc_registry_lock")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JdbcRegistryLock {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * The lock key.
     */
    private String lockKey;
    /**
     * acquire lock host.
     */
    private String lockOwner;
    /**
     * The last term, if the (currentTime - lastTerm) > termExpire time, the lock will be expired.
     */
    private Long lastTerm;
    /**
     * The lock last update time.
     */
    private Date lastUpdateTime;
    /**
     * The lock create time.
     */
    private Date createTime;
}
