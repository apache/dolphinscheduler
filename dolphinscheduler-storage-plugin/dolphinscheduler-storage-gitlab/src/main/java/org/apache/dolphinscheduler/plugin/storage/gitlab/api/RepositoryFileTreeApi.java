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

package org.apache.dolphinscheduler.plugin.storage.gitlab.api;

import org.apache.dolphinscheduler.plugin.storage.gitlab.dto.GitlabFileNode;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.RepositoryFileApi;

@Slf4j
public class RepositoryFileTreeApi extends RepositoryFileApi {

    public RepositoryFileTreeApi(GitLabApi gitLabApi) {
        super(gitLabApi);
    }

    /**
     * 提取gitlab项目指定文件夹下的目录树
     */
    public List<GitlabFileNode> getFileTree(Object projectIdOrPath, String filePath,
                                            String ref) throws GitLabApiException {

        Form form = new Form();
        addFormParam(form, "path", filePath);
        addFormParam(form, "ref", urlEncode(ref));

        return getGitlabTreeNodes(projectIdOrPath, form);
    }

    /**
     * 提取gitlab项目下的目录树（全量）
     */
    public List<GitlabFileNode> getFileTreeRecursive(Object projectIdOrPath, String ref) throws GitLabApiException {

        Form form = new Form();
        addFormParam(form, "recursive", true);
        addFormParam(form, "ref", urlEncode(ref));

        return getGitlabTreeNodes(projectIdOrPath, form);
    }

    private List<GitlabFileNode> getGitlabTreeNodes(Object projectIdOrPath, Form form) {
        try (
                Response response = get(Response.Status.OK, form.asMap(), "projects",
                        getProjectIdOrPath(projectIdOrPath), "repository", "tree")) {
            return response.readEntity(new GenericType<List<GitlabFileNode>>() {
            });
        } catch (Exception e) {
            log.error("get gitlab repository file tree error", e);
            return Collections.emptyList();
        }
    }

}
