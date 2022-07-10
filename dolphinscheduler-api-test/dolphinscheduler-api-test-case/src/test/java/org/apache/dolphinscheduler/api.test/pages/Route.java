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

package org.apache.dolphinscheduler.api.test.pages;

public class Route {
    private static final String SEPARATE = "/";

    /**
     * login page api
     */

    public static String login() {
        return "/login";
    }

    public static String loginOut() {
        return "/signOut";
    }

    /**
     * tenants
     */

    public static String tenants() {
        return "/tenants";
    }

    public static String tenants(int id) {
        return "/tenants/" + String.valueOf(id);
    }

    public static String tenantsList() {
        return "/tenants/list";
    }

    public static String tenantsVerifyCode() {
        return "/tenants/verify-code";
    }

    /**
     * token page api
     */

    public static String accessTokens() {
        return "/access-tokens";
    }

    public static String accessTokens(int id) {
        return "/access-tokens";
    }

    public static String accessTokenByUser(int userId) {
        return accessTokens() + "/user/" + String.valueOf(userId);
    }

    public static String generateAccessToken() {
        return accessTokens() + "/generate";
    }

    /**
     * project page api
     */

    public static String projects() {
        return "/projects";
    }

    public static String authProject(int userId) {
        return projects() + "/authed-project";
    }

    public static String authUserByProjectCode(int projectCode) {
        return projects() + "/authed-user";
    }

    public static String queryProjectCreatedAndAuthorizedByUser() {
        return projects() + "/created-and-authed";
    }

    public static String queryAllProjectList() {
        return projects() + "/list";
    }

    public static String queryUnauthorizedProject(int userId) {
        return projects() + "/unauth-project";
    }

    public static String queryProjectByCode(int code) {
        return projects() + "/code";
    }

    /**
     * workflow relation page api
     */

    public static String saveWorkFlowRelation(String projectCode) {
        return projects() + SEPARATE + projectCode + SEPARATE + "process-task-relation";
    }

    public static String deleteWorkFlowEdge(String projectCode, int processDefinitionCode, int preTaskCode, int postTaskCode) {
        return saveWorkFlowRelation(projectCode) + SEPARATE + processDefinitionCode + SEPARATE + preTaskCode + SEPARATE + postTaskCode;
    }

    public static String deleteWorkFlowRelation(String projectCode, int taskCode) {
        return saveWorkFlowRelation(projectCode) + SEPARATE + "process-task-relation" + SEPARATE + taskCode;
    }

    public static String queryWorkFlowDownstreamRelation(String projectCode, int taskCode) {
        return deleteWorkFlowRelation(projectCode, taskCode) + SEPARATE + "/downstream";
    }

    public static String queryWorkFlowUpstreamRelation(String projectCode, int taskCode) {
        return deleteWorkFlowRelation(projectCode, taskCode) + SEPARATE + "/upstream";
    }

    /*
     * workflow definition page api
     */

    public static String workFlowDefinition(String projectCode) {
        return projects() + SEPARATE + projectCode + SEPARATE + "process-definition";
    }

    public static String workFlowDefinition(String projectCode, String workFlowDefinitionCode) {
        return workFlowDefinition(projectCode) + SEPARATE + workFlowDefinitionCode + SEPARATE + "release";
    }

    public static String workFlowRun(String projectCode) {
        return projects() + SEPARATE + projectCode + SEPARATE + "/executors/start-process-instance";
    }


}
