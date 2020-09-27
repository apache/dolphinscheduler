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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * ProcessTag
 */
@TableName("t_ds_relation_process_tag")
public class ProcessTag {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private int id;

    @TableField("process_id")
    private int processID;

    @TableField("tag_id")
    private int tagID;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int gettagID() {
        return tagID;
    }

    public void settagID(int tagID) {
        this.tagID = tagID;
    }

    public int getProcessID() {
        return processID;
    }

    public void setProcessID(int processID) {
        this.processID = processID;
    }

    @Override
    public String toString() {

        return "ProcessTag{"
                + "id=" + id
                + ", processID=" + processID
                + ", tagID=" + tagID
                + '}';
    }
}
