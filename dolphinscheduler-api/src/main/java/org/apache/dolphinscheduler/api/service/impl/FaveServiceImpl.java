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

package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.configuration.TaskTypeConfiguration;
import org.apache.dolphinscheduler.api.dto.FavDto;
import org.apache.dolphinscheduler.api.service.FavService;
import org.apache.dolphinscheduler.dao.entity.Fav;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.FavMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class FaveServiceImpl extends BaseServiceImpl implements FavService {

    @Resource
    private TaskTypeConfiguration taskTypeConfiguration;
    @Resource
    private FavMapper favMapper;

    @Override
    public List<FavDto> getFavTaskList(User loginUser) {
        List<FavDto> result = new ArrayList<>();
        Set<String> userFavTaskTypes = favMapper.getUserFavTaskTypes(loginUser.getId());

        Set<FavDto> defaultTaskTypes = taskTypeConfiguration.getDefaultTaskTypes();
        defaultTaskTypes.forEach(e -> {
            if (userFavTaskTypes.contains(e.getTaskName())) {
                e.setCollection(true);
            }
            result.add(e);
        });
        return result;
    }

    @Override
    public boolean deleteFavTask(User loginUser, String taskName) {
        return favMapper.deleteUserFavTask(loginUser.getId(), taskName);
    }

    @Override
    public int addFavTask(User loginUser, String taskName) {
        favMapper.deleteUserFavTask(loginUser.getId(), taskName);
        return favMapper.insert(new Fav(null, taskName, loginUser.getId()));
    }
}
