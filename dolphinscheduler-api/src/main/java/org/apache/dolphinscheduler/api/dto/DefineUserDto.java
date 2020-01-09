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
package org.apache.dolphinscheduler.api.dto;

import org.apache.dolphinscheduler.dao.entity.DefinitionGroupByUser;

import java.util.List;

/**
 * user process define dto
 */
public class DefineUserDto {

    private int count;

    private List<DefinitionGroupByUser> userList;

    public DefineUserDto(List<DefinitionGroupByUser> defineGroupByUsers) {

        for(DefinitionGroupByUser define : defineGroupByUsers){
            count += define.getCount();
        }
        this.userList = defineGroupByUsers;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<DefinitionGroupByUser> getUserList() {
        return userList;
    }

    public void setUserList(List<DefinitionGroupByUser> userList) {
        this.userList = userList;
    }
}
