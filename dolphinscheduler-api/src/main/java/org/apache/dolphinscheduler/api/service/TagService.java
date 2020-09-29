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
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Tag;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.TagMapper;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 *
 * tag service
 */
@Service
public class TagService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(TagService.class);

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectService projectService;

    /**
     * create tag
     *
     * @param loginUser login user
     * @param projectName project name
     * @param tagname tag name
     * @return create result code
     */
    public Map<String, Object> createTag(User loginUser, String projectName, String tagname) {

        Map<String, Object> result = new HashMap<>(5);

        //check project auth
        Project project = projectMapper.queryByName(projectName);
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }

        Tag tag = new Tag();

        tag.setName(tagname);
        tag.setProjectId(project.getId());
        tag.setUserId(loginUser.getId());
        tagMapper.insert(tag);

        result.put(Constants.DATA_LIST, tagMapper.selectById(tag.getId()));
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * delete tag by id
     * @param tagId tag id
     * @param loginUser   login user
     * @param projectName project name
     * @return delete result code
     */
    public Map<String,Object> deleteTag(User loginUser, String projectName, Integer tagId) {

        Map<String, Object> result = new HashMap<>();

        //check project auth
        Project project = projectMapper.queryByName(projectName);
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }

        Tag tag = tagMapper.selectById(tagId);

        if (tag == null) {
            putMsg(result, Status.TAG_NOT_EXIST, tag);
            return result;
        }
        // Determine if the login user is the owner of the tag
        if (loginUser.getId() != tag.getUserId() && loginUser.getUserType() != UserType.ADMIN_USER) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        int delete = tagMapper.deleteById(tagId);
        if (delete > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.DELETE_TAG_ERROR);
        }
        return result;
    }
    /**
     * update Tag
     *
     * @param loginUser   login user
     * @param projectName project name
     * @param tagId tag id
     * @param tagName tag name
     * @return update result code
     */

    public Map<String, Object> updateTag(User loginUser, String projectName, Integer tagId, String tagName) {

        Map<String, Object> result = new HashMap<>(5);

        Project project = projectMapper.queryByName(projectName);
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }

        Tag tag = tagMapper.selectById(tagId);

        if (tag == null) {
            putMsg(result, Status.TAG_NOT_EXIST, tagName);
            return result;
        } else {
            putMsg(result, Status.SUCCESS);
        }

        tag.setName(tagName);
        tag.setProjectId(project.getId());

        int update = tagMapper.updateById(tag);
        if (update > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.UPDATE_TAG_ERROR);
        }
        return result;
    }

    /**
     * admin can view all tags
     *
     * @param loginUser login user
     * @param projectName project name
     * @param searchVal search value
     * @param pageSize page size
     * @param pageNo page number
     * @return tag list which the login user have permission to see
     */
    public Map<String, Object> queryTagListPaging(User loginUser, String projectName, Integer pageSize, Integer pageNo, String searchVal) {

        Map<String, Object> result = new HashMap<>();
        PageInfo pageInfo = new PageInfo<Tag>(pageNo, pageSize);

        Page<Tag> page = new Page(pageNo, pageSize);

        Project project = projectMapper.queryByName(projectName);

        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }

        int userId = loginUser.getUserType() == UserType.ADMIN_USER ? 0 : loginUser.getId();
        IPage<Tag> tagIPage = tagMapper.queryTagListPaging(page, userId, project.getId(), searchVal);

        pageInfo.setTotalCount((int) tagIPage.getTotal());
        pageInfo.setLists(tagIPage.getRecords());
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }
    
    /**
     * query tag  by id
     *
     * @param tagId tag id
     * @return tag detail information
     */

    public Map<String, Object> queryById(Integer tagId) {

        Map<String, Object> result = new HashMap<>(5);
        Tag tag = tagMapper.selectById(tagId);

        if (tag != null) {
            result.put(Constants.DATA_LIST, tag);
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.TAG_NOT_FOUND, tagId);
        }
        return result;
    }
}
