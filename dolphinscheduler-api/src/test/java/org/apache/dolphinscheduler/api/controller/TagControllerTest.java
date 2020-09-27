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

package org.apache.dolphinscheduler.api.controller;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.TagService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.User;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TagControllerTest extends AbstractControllerTest {

    private static Logger logger = LoggerFactory.getLogger(TagControllerTest.class);

    @InjectMocks
    private TagController tagController;

    @Mock
    private TagService tagService;

    protected User user;

    @Before
    public void before() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("admin");

        user = loginUser;
    }

    @Test
    public void testCreateTag() throws Exception {

        String projectName = "project_test";
        String tagName = "tag_test";
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put("tagId", 1);

        Mockito.when(tagService.createTag(user,projectName,tagName)).thenReturn(result);

        Result response = tagController.createTag(user, projectName, tagName);
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testDeleteTag() throws Exception {
        String projectName = "project_test";
        int id = 1;

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);

        Mockito.when(tagService.deleteTag(user, projectName, id)).thenReturn(result);
        Result response = tagController.deleteTag(user, projectName, id);

        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testUpdateTag() throws Exception {
        String projectName = "project_test";
        String tagName = "tag_test";
        int tagId = 1;
        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put("tagId", 1);

        Mockito.when(tagService.updateTag(user, projectName, tagId, tagName)).thenReturn(result);

        Result response = tagController.updateTag(user, projectName, tagName, tagId);
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testTagListPaging() throws Exception {
        String projectName = "project_test";
        int pageNo = 1;
        int pageSize = 10;
        String searchVal = "";
        int userId = 1;

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, new PageInfo<Resource>(1, 10));

        Mockito.when(tagService.queryTagListPaging(user, projectName, pageSize, pageNo, searchVal)).thenReturn(result);
        Result response = tagController.queryTagListPaging(user, projectName, pageNo, searchVal, pageSize);

        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    private void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }
}