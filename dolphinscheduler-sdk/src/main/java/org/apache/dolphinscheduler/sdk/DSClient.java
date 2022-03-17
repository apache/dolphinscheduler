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

package org.apache.dolphinscheduler.sdk;

import org.apache.dolphinscheduler.sdk.model.analysis.QueryProcessInstanceStatesRequest;
import org.apache.dolphinscheduler.sdk.model.analysis.QueryProcessInstanceStatesResponse;
import org.apache.dolphinscheduler.sdk.model.analysis.QueryTaskStatesRequest;
import org.apache.dolphinscheduler.sdk.model.analysis.QueryTaskStatesResponse;
import org.apache.dolphinscheduler.sdk.model.executors.ExecuteProcessInstanceRequest;
import org.apache.dolphinscheduler.sdk.model.executors.ExecuteProcessInstanceResponse;
import org.apache.dolphinscheduler.sdk.model.executors.StartCheckProcessDefinitionRequest;
import org.apache.dolphinscheduler.sdk.model.executors.StartCheckProcessDefinitionResponse;
import org.apache.dolphinscheduler.sdk.model.executors.StartProcessInstanceRequest;
import org.apache.dolphinscheduler.sdk.model.executors.StartProcessInstanceResponse;
import org.apache.dolphinscheduler.sdk.model.logger.QueryLogRequest;
import org.apache.dolphinscheduler.sdk.model.logger.QueryLogResponse;
import org.apache.dolphinscheduler.sdk.model.process.definition.BatchDeleteProcessDefinitionByCodesRequest;
import org.apache.dolphinscheduler.sdk.model.process.definition.BatchDeleteProcessDefinitionByCodesResponse;
import org.apache.dolphinscheduler.sdk.model.process.definition.BatchExportProcessDefinitionByCodesRequest;
import org.apache.dolphinscheduler.sdk.model.process.definition.BatchExportProcessDefinitionByCodesResponse;
import org.apache.dolphinscheduler.sdk.model.process.definition.CreateProcessDefinitionRequest;
import org.apache.dolphinscheduler.sdk.model.process.definition.CreateProcessDefinitionResponse;
import org.apache.dolphinscheduler.sdk.model.process.definition.DeleteProcessDefinitionByCodeRequest;
import org.apache.dolphinscheduler.sdk.model.process.definition.DeleteProcessDefinitionByCodeResponse;
import org.apache.dolphinscheduler.sdk.model.process.definition.ImportProcessDefinitionRequest;
import org.apache.dolphinscheduler.sdk.model.process.definition.ImportProcessDefinitionResponse;
import org.apache.dolphinscheduler.sdk.model.process.definition.QueryProcessDefinitionByCodeRequest;
import org.apache.dolphinscheduler.sdk.model.process.definition.QueryProcessDefinitionByCodeResponse;
import org.apache.dolphinscheduler.sdk.model.process.definition.QueryProcessDefinitionListPagingRequest;
import org.apache.dolphinscheduler.sdk.model.process.definition.QueryProcessDefinitionListPagingResponse;
import org.apache.dolphinscheduler.sdk.model.process.definition.ReleaseProcessDefinitionRequest;
import org.apache.dolphinscheduler.sdk.model.process.definition.ReleaseProcessDefinitionResponse;
import org.apache.dolphinscheduler.sdk.model.process.definition.SwitchProcessDefinitionVersionRequest;
import org.apache.dolphinscheduler.sdk.model.process.definition.SwitchProcessDefinitionVersionResponse;
import org.apache.dolphinscheduler.sdk.model.process.definition.UpdateProcessDefinitionRequest;
import org.apache.dolphinscheduler.sdk.model.process.definition.UpdateProcessDefinitionResponse;
import org.apache.dolphinscheduler.sdk.model.process.definition.VerifyProcessDefinitionNameRequest;
import org.apache.dolphinscheduler.sdk.model.process.definition.VerifyProcessDefinitionNameResponse;
import org.apache.dolphinscheduler.sdk.model.process.instance.BatchDeleteProcessInstanceByIdsRequest;
import org.apache.dolphinscheduler.sdk.model.process.instance.BatchDeleteProcessInstanceByIdsResponse;
import org.apache.dolphinscheduler.sdk.model.process.instance.DeleteProcessInstanceByIdRequest;
import org.apache.dolphinscheduler.sdk.model.process.instance.DeleteProcessInstanceByIdResponse;
import org.apache.dolphinscheduler.sdk.model.process.instance.QueryParentInstanceBySubIdRequest;
import org.apache.dolphinscheduler.sdk.model.process.instance.QueryParentInstanceBySubIdResponse;
import org.apache.dolphinscheduler.sdk.model.process.instance.QueryProcessInstanceListRequest;
import org.apache.dolphinscheduler.sdk.model.process.instance.QueryProcessInstanceListResponse;
import org.apache.dolphinscheduler.sdk.model.process.instance.QueryProcessInstanceRequest;
import org.apache.dolphinscheduler.sdk.model.process.instance.QueryProcessInstanceResponse;
import org.apache.dolphinscheduler.sdk.model.process.instance.QuerySubProcessInstanceByTaskIdRequest;
import org.apache.dolphinscheduler.sdk.model.process.instance.QuerySubProcessInstanceByTaskIdResponse;
import org.apache.dolphinscheduler.sdk.model.process.instance.QueryTaskListByProcessIdRequest;
import org.apache.dolphinscheduler.sdk.model.process.instance.QueryTaskListByProcessIdResponse;
import org.apache.dolphinscheduler.sdk.model.process.instance.UpdateProcessInstanceRequest;
import org.apache.dolphinscheduler.sdk.model.process.instance.UpdateProcessInstanceResponse;
import org.apache.dolphinscheduler.sdk.model.project.CreateProjectRequest;
import org.apache.dolphinscheduler.sdk.model.project.CreateProjectResponse;
import org.apache.dolphinscheduler.sdk.model.project.DeleteProjectRequest;
import org.apache.dolphinscheduler.sdk.model.project.DeleteProjectResponse;
import org.apache.dolphinscheduler.sdk.model.project.QueryAllProjectListRequest;
import org.apache.dolphinscheduler.sdk.model.project.QueryAllProjectListResponse;
import org.apache.dolphinscheduler.sdk.model.project.QueryProjectByCodeRequest;
import org.apache.dolphinscheduler.sdk.model.project.QueryProjectByCodeResponse;
import org.apache.dolphinscheduler.sdk.model.project.QueryProjectListPagingRequest;
import org.apache.dolphinscheduler.sdk.model.project.QueryProjectListPagingResponse;
import org.apache.dolphinscheduler.sdk.model.project.UpdateProjectRequest;
import org.apache.dolphinscheduler.sdk.model.project.UpdateProjectResponse;
import org.apache.dolphinscheduler.sdk.model.queue.QueryQueueListPagingRequest;
import org.apache.dolphinscheduler.sdk.model.queue.QueryQueueListPagingResponse;
import org.apache.dolphinscheduler.sdk.model.schedule.CreateScheduleRequest;
import org.apache.dolphinscheduler.sdk.model.schedule.CreateScheduleResponse;
import org.apache.dolphinscheduler.sdk.model.schedule.DeleteScheduleByIdRequest;
import org.apache.dolphinscheduler.sdk.model.schedule.DeleteScheduleByIdResponse;
import org.apache.dolphinscheduler.sdk.model.schedule.QueryScheduleRequest;
import org.apache.dolphinscheduler.sdk.model.schedule.QueryScheduleResponse;
import org.apache.dolphinscheduler.sdk.model.schedule.ReleaseScheduleOfflineRequest;
import org.apache.dolphinscheduler.sdk.model.schedule.ReleaseScheduleOfflineResponse;
import org.apache.dolphinscheduler.sdk.model.schedule.ReleaseScheduleOnlineRequest;
import org.apache.dolphinscheduler.sdk.model.schedule.ReleaseScheduleOnlineResponse;
import org.apache.dolphinscheduler.sdk.model.schedule.UpdateScheduleRequest;
import org.apache.dolphinscheduler.sdk.model.schedule.UpdateScheduleResponse;
import org.apache.dolphinscheduler.sdk.model.task.definition.CreateTaskDefinitionCodeRequest;
import org.apache.dolphinscheduler.sdk.model.task.definition.CreateTaskDefinitionCodeResponse;
import org.apache.dolphinscheduler.sdk.model.task.definition.CreateTaskDefinitionRequest;
import org.apache.dolphinscheduler.sdk.model.task.definition.CreateTaskDefinitionResponse;
import org.apache.dolphinscheduler.sdk.model.task.definition.DeleteTaskDefinitionByCodeRequest;
import org.apache.dolphinscheduler.sdk.model.task.definition.DeleteTaskDefinitionByCodeResponse;
import org.apache.dolphinscheduler.sdk.model.task.definition.DeleteTaskDefinitionVersionRequest;
import org.apache.dolphinscheduler.sdk.model.task.definition.DeleteTaskDefinitionVersionResponse;
import org.apache.dolphinscheduler.sdk.model.task.definition.GenTaskCodeListRequest;
import org.apache.dolphinscheduler.sdk.model.task.definition.GenTaskCodeListResponse;
import org.apache.dolphinscheduler.sdk.model.task.definition.QueryTaskDefinitionDetailRequest;
import org.apache.dolphinscheduler.sdk.model.task.definition.QueryTaskDefinitionDetailResponse;
import org.apache.dolphinscheduler.sdk.model.task.definition.QueryTaskDefinitionListPagingRequest;
import org.apache.dolphinscheduler.sdk.model.task.definition.QueryTaskDefinitionListPagingResponse;
import org.apache.dolphinscheduler.sdk.model.task.definition.QueryTaskDefinitionVersionsRequest;
import org.apache.dolphinscheduler.sdk.model.task.definition.QueryTaskDefinitionVersionsResponse;
import org.apache.dolphinscheduler.sdk.model.task.definition.SwitchTaskDefinitionVersionRequest;
import org.apache.dolphinscheduler.sdk.model.task.definition.SwitchTaskDefinitionVersionResponse;
import org.apache.dolphinscheduler.sdk.model.task.definition.UpdateTaskDefinitionRequest;
import org.apache.dolphinscheduler.sdk.model.task.definition.UpdateTaskDefinitionResponse;
import org.apache.dolphinscheduler.sdk.model.task.instance.ForceTaskSuccessRequest;
import org.apache.dolphinscheduler.sdk.model.task.instance.ForceTaskSuccessResponse;
import org.apache.dolphinscheduler.sdk.model.task.instance.QueryTaskListPagingRequest;
import org.apache.dolphinscheduler.sdk.model.task.instance.QueryTaskListPagingResponse;
import org.apache.dolphinscheduler.sdk.model.tenant.CreateTenantRequest;
import org.apache.dolphinscheduler.sdk.model.tenant.CreateTenantResponse;
import org.apache.dolphinscheduler.sdk.model.tenant.QueryTenantListPagingRequest;
import org.apache.dolphinscheduler.sdk.model.tenant.QueryTenantListPagingResponse;
import org.apache.dolphinscheduler.sdk.model.tenant.VerifyTenantCodeRequest;
import org.apache.dolphinscheduler.sdk.model.tenant.VerifyTenantCodeResponse;
import org.apache.dolphinscheduler.sdk.model.token.CreateAccessTokenRequest;
import org.apache.dolphinscheduler.sdk.model.token.CreateAccessTokenResponse;
import org.apache.dolphinscheduler.sdk.model.token.QueryAccessTokenByUserRequest;
import org.apache.dolphinscheduler.sdk.model.token.QueryAccessTokenByUserResponse;
import org.apache.dolphinscheduler.sdk.model.token.UpdateAccessTokenRequest;
import org.apache.dolphinscheduler.sdk.model.token.UpdateAccessTokenResponse;
import org.apache.dolphinscheduler.sdk.model.user.CreateUserRequest;
import org.apache.dolphinscheduler.sdk.model.user.CreateUserResponse;
import org.apache.dolphinscheduler.sdk.model.user.DelUserByIdRequest;
import org.apache.dolphinscheduler.sdk.model.user.DelUserByIdResponse;
import org.apache.dolphinscheduler.sdk.model.user.GetUserInfoRequest;
import org.apache.dolphinscheduler.sdk.model.user.GetUserInfoResponse;
import org.apache.dolphinscheduler.sdk.model.user.GrantProjectByCodeRequest;
import org.apache.dolphinscheduler.sdk.model.user.GrantProjectByCodeResponse;
import org.apache.dolphinscheduler.sdk.model.user.QueryAuthorizedUserRequest;
import org.apache.dolphinscheduler.sdk.model.user.QueryAuthorizedUserResponse;
import org.apache.dolphinscheduler.sdk.model.user.QueryUserListRequest;
import org.apache.dolphinscheduler.sdk.model.user.QueryUserListResponse;
import org.apache.dolphinscheduler.sdk.model.user.RevokeProjectRequest;
import org.apache.dolphinscheduler.sdk.model.user.RevokeProjectResponse;
import org.apache.dolphinscheduler.sdk.tea.TeaModel;

import org.springframework.http.HttpMethod;

import com.google.common.reflect.TypeToken;

public class DSClient extends Client {

    public DSClient(final Config config) {
        super(config);
    }

    public CreateAccessTokenResponse createAccessToken(CreateAccessTokenRequest request) throws Exception {
        Params params = new Params().setAction("createAccessToken").setPathname("/dolphinscheduler/access-tokens").setMethod(HttpMethod.POST.name());
        return TeaModel.toModel(this.doRequest(request, params), new TypeToken<CreateAccessTokenResponse>() {
        }.getType());
    }

    public UpdateAccessTokenResponse updateAccessToken(UpdateAccessTokenRequest request) throws Exception {
        Params params = new Params().setAction("updateAccessToken").setPathname("/dolphinscheduler/access-tokens/${id}").setMethod(HttpMethod.PUT.name());
        return TeaModel.toModel(this.doRequest(request, params), new TypeToken<UpdateAccessTokenResponse>() {
        }.getType());
    }

    public QueryAccessTokenByUserResponse queryAccessTokenByUser(QueryAccessTokenByUserRequest request) throws Exception {
        Params params = new Params().setAction("queryAccessTokenByUser").setPathname("/dolphinscheduler/access-tokens/user/${userId}");
        return TeaModel.toModel(this.doRequest(request, params), new TypeToken<QueryAccessTokenByUserResponse>() {
        }.getType());
    }

    public QueryAllProjectListResponse queryAllProjectList(QueryAllProjectListRequest request) throws Exception {
        return this.queryAllProjectListWithOptions(request, new RuntimeOptions());
    }

    public QueryAllProjectListResponse queryAllProjectListWithOptions(QueryAllProjectListRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("queryAllProjectList")
                .setPathname("/dolphinscheduler/projects/list");
        return TeaModel.toModel(this.doRequest(request, params, runtime), new TypeToken<QueryAllProjectListResponse>() {
        }.getType());
    }

    public QueryProjectByCodeResponse queryProjectByCode(QueryProjectByCodeRequest request) throws Exception {
        return this.queryProjectByCodeWithOptions(request, new RuntimeOptions());
    }

    public QueryProjectByCodeResponse queryProjectByCodeWithOptions(QueryProjectByCodeRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("queryProjectByCode")
                .setPathname("/dolphinscheduler/projects/${projectCode}");
        return TeaModel.toModel(this.doRequest(request, params, runtime), new TypeToken<QueryProjectByCodeResponse>() {
        }.getType());
    }

    public QueryProjectListPagingResponse queryProjectListPaging(QueryProjectListPagingRequest request) throws Exception {
        return this.queryProjectListPagingWithOptions(request, new RuntimeOptions());
    }

    public QueryProjectListPagingResponse queryProjectListPagingWithOptions(QueryProjectListPagingRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("queryProjectListPaging")
                .setPathname("/dolphinscheduler/projects");
        return TeaModel.toModel(this.doRequest(request, params, runtime), new TypeToken<QueryProjectListPagingResponse>() {
        }.getType());
    }

    public QueryTaskDefinitionDetailResponse queryTaskDefinitionDetail(QueryTaskDefinitionDetailRequest request) throws Exception {
        return this.queryTaskDefinitionDetailWithOptions(request, new RuntimeOptions());
    }

    public QueryTaskDefinitionDetailResponse queryTaskDefinitionDetailWithOptions(QueryTaskDefinitionDetailRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("queryTaskDefinitionByCode")
                .setPathname("/dolphinscheduler/projects/${projectCode}/task-definition/${taskCode}");
        return TeaModel.toModel(this.doRequest(request, params, runtime), QueryTaskDefinitionDetailResponse.class);
    }

    public QueryTaskDefinitionListPagingResponse queryTaskDefinitionListPaging(QueryTaskDefinitionListPagingRequest request) throws Exception {
        return this.queryTaskDefinitionListPagingWithOptions(request, new RuntimeOptions());
    }

    public QueryTaskDefinitionListPagingResponse queryTaskDefinitionListPagingWithOptions(QueryTaskDefinitionListPagingRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("queryTaskDefinitionListPaging")
                .setPathname("/dolphinscheduler/projects/${projectCode}/task-definition");
        return TeaModel.toModel(this.doRequest(request, params, runtime), QueryTaskDefinitionListPagingResponse.class);
    }

    public CreateTaskDefinitionResponse createTaskDefinition(CreateTaskDefinitionRequest request) throws Exception {
        return this.createTaskDefinitionWithOptions(request, new RuntimeOptions());
    }

    public CreateTaskDefinitionResponse createTaskDefinitionWithOptions(CreateTaskDefinitionRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("save")
                .setPathname("/dolphinscheduler/projects/${projectCode}/task-definition")
                .setMethod(HttpMethod.POST.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), CreateTaskDefinitionResponse.class);
    }

    public UpdateTaskDefinitionResponse updateTaskDefinition(UpdateTaskDefinitionRequest request) throws Exception {
        return this.updateTaskDefinitionWithOptions(request, new RuntimeOptions());
    }

    public UpdateTaskDefinitionResponse updateTaskDefinitionWithOptions(UpdateTaskDefinitionRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("save")
                .setPathname("/dolphinscheduler/projects/${projectCode}/task-definition/${code}")
                .setMethod(HttpMethod.PUT.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), UpdateTaskDefinitionResponse.class);
    }

    public DeleteTaskDefinitionByCodeResponse deleteTaskDefinitionByCode(DeleteTaskDefinitionByCodeRequest request) throws Exception {
        return this.deleteTaskDefinitionByCodeWithOptions(request, new RuntimeOptions());
    }

    public DeleteTaskDefinitionByCodeResponse deleteTaskDefinitionByCodeWithOptions(DeleteTaskDefinitionByCodeRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("save")
                .setPathname("/dolphinscheduler/projects/${projectCode}/task-definition/${code}")
                .setMethod(HttpMethod.DELETE.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), DeleteTaskDefinitionByCodeResponse.class);
    }

    public DeleteTaskDefinitionVersionResponse deleteTaskDefinitionVersion(DeleteTaskDefinitionVersionRequest request) throws Exception {
        return this.deleteTaskDefinitionVersionWithOptions(request, new RuntimeOptions());
    }

    public DeleteTaskDefinitionVersionResponse deleteTaskDefinitionVersionWithOptions(DeleteTaskDefinitionVersionRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("deleteVersion")
                .setPathname("/dolphinscheduler/projects/${projectCode}/task-definition/${code}/versions/${version}")
                .setMethod(HttpMethod.DELETE.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), DeleteTaskDefinitionVersionResponse.class);
    }

    public QueryTaskListPagingResponse queryTaskListPaging(QueryTaskListPagingRequest request) throws Exception {
        RuntimeOptions runtimeOptions = new RuntimeOptions();
        runtimeOptions.connectTimeout = 60 * 1000;
        runtimeOptions.readTimeout = 60 * 1000;
        return this.queryTaskListPagingWithOptions(request, runtimeOptions);
    }

    public QueryTaskListPagingResponse queryTaskListPagingWithOptions(QueryTaskListPagingRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("queryTaskListPaging")
                .setPathname("/dolphinscheduler/projects/${projectCode}/task-instances");
        return TeaModel.toModel(this.doRequest(request, params, runtime), new TypeToken<QueryTaskListPagingResponse>() {
        }.getType());
    }

    public ForceTaskSuccessResponse forceTaskSuccess(ForceTaskSuccessRequest request) throws Exception {
        return this.forceTaskSuccessWithOptions(request, new RuntimeOptions());
    }

    public ForceTaskSuccessResponse forceTaskSuccessWithOptions(ForceTaskSuccessRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("force-success")
                .setPathname("/dolphinscheduler/projects/${projectCode}/task-instances/${id}/force-success")
                .setMethod(HttpMethod.POST.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), ForceTaskSuccessResponse.class);
    }

    public GenTaskCodeListResponse genTaskCodeList(GenTaskCodeListRequest request) throws Exception {
        return this.genTaskCodeListWithOptions(request, new RuntimeOptions());
    }

    public GenTaskCodeListResponse genTaskCodeListWithOptions(GenTaskCodeListRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("genTaskCodeList")
                .setPathname("/dolphinscheduler/projects/${projectCode}/task-definition/gen-task-codes");
        return TeaModel.toModel(this.doRequest(request, params, runtime), GenTaskCodeListResponse.class);
    }

    public QueryTaskDefinitionVersionsResponse queryTaskDefinitionVersions(QueryTaskDefinitionVersionsRequest request) throws Exception {
        return this.queryTaskDefinitionVersionsWithOptions(request, new RuntimeOptions());
    }

    public QueryTaskDefinitionVersionsResponse queryTaskDefinitionVersionsWithOptions(QueryTaskDefinitionVersionsRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("queryVersions")
                .setPathname("/dolphinscheduler/projects/${projectCode}/task-definition/${code}/versions");
        return TeaModel.toModel(this.doRequest(request, params, runtime), QueryTaskDefinitionVersionsResponse.class);
    }

    public SwitchTaskDefinitionVersionResponse switchTaskDefinitionVersion(SwitchTaskDefinitionVersionRequest request) throws Exception {
        return this.switchTaskDefinitionVersionWithOptions(request, new RuntimeOptions());
    }

    public SwitchTaskDefinitionVersionResponse switchTaskDefinitionVersionWithOptions(SwitchTaskDefinitionVersionRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("switchVersion")
                .setPathname("/dolphinscheduler/projects/${projectCode}/task-definition/${code}/versions/${version}");
        return TeaModel.toModel(this.doRequest(request, params, runtime), SwitchTaskDefinitionVersionResponse.class);

    }

    public QueryProcessDefinitionByCodeResponse queryProcessDefinitionByCode(QueryProcessDefinitionByCodeRequest request) throws Exception {
        return this.queryProcessDefinitionByCodeWithOptions(request, new RuntimeOptions());
    }

    public QueryProcessDefinitionByCodeResponse queryProcessDefinitionByCodeWithOptions(QueryProcessDefinitionByCodeRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("queryProcessDefinitionByCode")
                .setPathname("/dolphinscheduler/projects/${projectCode}/process-definition/${code}");
        return TeaModel.toModel(this.doRequest(request, params, runtime), QueryProcessDefinitionByCodeResponse.class);
    }

    public CreateProjectResponse createProject(CreateProjectRequest request) throws Exception {
        return this.createProjectWithOptions(request, new RuntimeOptions());
    }

    public CreateProjectResponse createProjectWithOptions(CreateProjectRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("create")
                .setPathname("/dolphinscheduler/projects")
                .setMethod(HttpMethod.POST.name())
                .setReqBodyType("form");
        return TeaModel.toModel(this.doRequest(request, params, runtime), CreateProjectResponse.class);
    }

    public UpdateProjectResponse updateProject(UpdateProjectRequest request) throws Exception {
        return this.updateProjectWithOptions(request, new RuntimeOptions());
    }

    public UpdateProjectResponse updateProjectWithOptions(UpdateProjectRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("update")
                .setPathname("/dolphinscheduler/projects/${code}")
                .setMethod(HttpMethod.PUT.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), UpdateProjectResponse.class);
    }

    public DeleteProjectResponse deleteProject(DeleteProjectRequest request) throws Exception {
        return this.deleteProjectWithOptions(request, new RuntimeOptions());
    }

    public DeleteProjectResponse deleteProjectWithOptions(DeleteProjectRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("delete")
                .setPathname("/dolphinscheduler/projects/${code}")
                .setMethod(HttpMethod.DELETE.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), DeleteProjectResponse.class);
    }

    public QueryUserListResponse queryUserList(QueryUserListRequest request) throws Exception {
        Params params = new Params().setAction("queryUserListPaging").setPathname("/dolphinscheduler/users/list-paging");
        return TeaModel.toModel(this.doRequest(request, params, new RuntimeOptions()), new TypeToken<QueryUserListResponse>() {
        }.getType());
    }

    public CreateUserResponse createUser(CreateUserRequest request) throws Exception {
        return this.createUserWithOptions(request, new RuntimeOptions());
    }

    public CreateUserResponse createUserWithOptions(CreateUserRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("createUser")
                .setPathname("/dolphinscheduler/users/create")
                .setMethod(HttpMethod.POST.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), CreateUserResponse.class);
    }

    public GetUserInfoResponse getUserInfo(GetUserInfoRequest request) throws Exception {
        return this.getUserInfoWithOptions(request, new RuntimeOptions());
    }

    public GetUserInfoResponse getUserInfoWithOptions(GetUserInfoRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("getUserInfo")
                .setPathname("/dolphinscheduler/users/get-user-info");
        return TeaModel.toModel(this.doRequest(request, params, runtime), GetUserInfoResponse.class);
    }

    public GrantProjectByCodeResponse grantProjectByCode(GrantProjectByCodeRequest request) throws Exception {
        Params params = new Params().setAction("grantProjectByCode")
                .setPathname("/dolphinscheduler/users/grant-project-by-code")
                .setMethod(HttpMethod.POST.name());
        return TeaModel.toModel(this.doRequest(request, params, new RuntimeOptions()), GrantProjectByCodeResponse.class);
    }

    public RevokeProjectResponse revokeProject(RevokeProjectRequest request) throws Exception {
        Params params = new Params().setAction("revokeProject")
                .setPathname("/dolphinscheduler/users/revoke-project")
                .setMethod(HttpMethod.POST.name());
        return TeaModel.toModel(this.doRequest(request, params, new RuntimeOptions()), RevokeProjectResponse.class);
    }

    public QueryAuthorizedUserResponse queryAuthorizedUser(QueryAuthorizedUserRequest request) throws Exception {
        Params params = new Params().setAction("queryAuthorizedUser")
                .setPathname("/dolphinscheduler/projects/authed-user")
                .setMethod(HttpMethod.GET.name());
        return TeaModel.toModel(this.doRequest(request, params, new RuntimeOptions()), QueryAuthorizedUserResponse.class);
    }

    public DelUserByIdResponse delUserById(DelUserByIdRequest request) throws Exception {
        return this.delUserByIdWithOptions(request, new RuntimeOptions());
    }

    public DelUserByIdResponse delUserByIdWithOptions(DelUserByIdRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("delUserById")
                .setPathname("/dolphinscheduler/users/delete")
                .setMethod(HttpMethod.POST.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), DelUserByIdResponse.class);
    }

    public QueryQueueListPagingResponse queryQueueListPaging(QueryQueueListPagingRequest request) throws Exception {
        return this.queryQueueListPagingWithOptions(request, new RuntimeOptions());
    }

    public QueryQueueListPagingResponse queryQueueListPagingWithOptions(QueryQueueListPagingRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("queryQueueListPaging")
                .setPathname("/dolphinscheduler/queues");
        return TeaModel.toModel(this.doRequest(request, params, runtime), new TypeToken<QueryQueueListPagingResponse>() {
        }.getType());
    }

    public CreateTenantResponse createTenant(CreateTenantRequest request) throws Exception {
        return this.createTenantWithOptions(request, new RuntimeOptions());
    }

    public CreateTenantResponse createTenantWithOptions(CreateTenantRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("createTenant")
                .setPathname("/dolphinscheduler/tenants")
                .setMethod(HttpMethod.POST.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), CreateTenantResponse.class);
    }

    public QueryTenantListPagingResponse queryTenantListPaging(QueryTenantListPagingRequest request) throws Exception {
        return this.queryTenantListPagingWithOptions(request, new RuntimeOptions());
    }

    public QueryTenantListPagingResponse queryTenantListPagingWithOptions(QueryTenantListPagingRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("queryTenantlistPaging")
                .setPathname("/dolphinscheduler/tenants");
        return TeaModel.toModel(this.doRequest(request, params, runtime), new TypeToken<QueryTenantListPagingResponse>() {
        }.getType());
    }

    public VerifyTenantCodeResponse verifyTenantCode(VerifyTenantCodeRequest request) throws Exception {
        return this.verifyTenantCodeWithOptions(request, new RuntimeOptions());
    }

    public VerifyTenantCodeResponse verifyTenantCodeWithOptions(VerifyTenantCodeRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("verifyTenantCode")
                .setPathname("/dolphinscheduler/tenants/verify-code");
        return TeaModel.toModel(this.doRequest(request, params, runtime), VerifyTenantCodeResponse.class);
    }

    public QueryLogResponse queryLog(QueryLogRequest request) throws Exception {
        return this.queryLogWithOptions(request, new RuntimeOptions());
    }

    public QueryLogResponse queryLogWithOptions(QueryLogRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("queryLog")
                .setPathname("/dolphinscheduler/log/detail");
        return TeaModel.toModel(this.doRequest(request, params, runtime), QueryLogResponse.class);
    }

    public QueryProcessDefinitionListPagingResponse queryProcessDefinitionListPaging(QueryProcessDefinitionListPagingRequest request) throws Exception {
        return this.queryProcessDefinitionListPagingWithOptions(request, new RuntimeOptions());
    }

    public QueryProcessDefinitionListPagingResponse queryProcessDefinitionListPagingWithOptions(QueryProcessDefinitionListPagingRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("queryListPaging")
                .setPathname("/dolphinscheduler/projects/${projectCode}/process-definition");
        return TeaModel.toModel(this.doRequest(request, params, runtime), new TypeToken<QueryProcessDefinitionListPagingResponse>() {
        }.getType());
    }

    public CreateProcessDefinitionResponse createProcessDefinition(CreateProcessDefinitionRequest request) throws Exception {
        return this.createProcessDefinitionWithOptions(request, new RuntimeOptions());
    }

    public CreateProcessDefinitionResponse createProcessDefinitionWithOptions(CreateProcessDefinitionRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("createProcessDefinition")
                .setPathname("/dolphinscheduler/projects/${projectCode}/process-definition")
                .setMethod(HttpMethod.POST.name())
                .setReqBodyType("form");
        return TeaModel.toModel(this.doRequest(request, params, runtime), CreateProcessDefinitionResponse.class);
    }

    public DeleteProcessDefinitionByCodeResponse deleteProcessDefinitionByCode(DeleteProcessDefinitionByCodeRequest request) throws Exception {
        return this.deleteProcessDefinitionByCodeWithOptions(request, new RuntimeOptions());
    }

    public DeleteProcessDefinitionByCodeResponse deleteProcessDefinitionByCodeWithOptions(DeleteProcessDefinitionByCodeRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("deleteByCode")
                .setPathname("/dolphinscheduler/projects/${projectCode}/process-definition/${code}")
                .setMethod(HttpMethod.DELETE.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), DeleteProcessDefinitionByCodeResponse.class);
    }

    public UpdateProcessDefinitionResponse updateProcessDefinition(UpdateProcessDefinitionRequest request) throws Exception {
        return this.updateProcessDefinitionWithOptions(request, new RuntimeOptions());
    }

    public UpdateProcessDefinitionResponse updateProcessDefinitionWithOptions(UpdateProcessDefinitionRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("update")
                .setPathname("/dolphinscheduler/projects/${projectCode}/process-definition/${code}")
                .setMethod(HttpMethod.PUT.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), UpdateProcessDefinitionResponse.class);
    }

    public ReleaseProcessDefinitionResponse releaseProcessDefinition(ReleaseProcessDefinitionRequest request) throws Exception {
        return this.releaseProcessDefinitionWithOptions(request, new RuntimeOptions());
    }

    public ReleaseProcessDefinitionResponse releaseProcessDefinitionWithOptions(ReleaseProcessDefinitionRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("release")
                .setPathname("/dolphinscheduler/projects/${projectCode}/process-definition/${code}/release")
                .setMethod(HttpMethod.POST.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), ReleaseProcessDefinitionResponse.class);
    }

    public SwitchProcessDefinitionVersionResponse switchProcessDefinitionVersion(SwitchProcessDefinitionVersionRequest request) throws Exception {
        return this.switchProcessDefinitionVersionWithOptions(request, new RuntimeOptions());
    }

    public SwitchProcessDefinitionVersionResponse switchProcessDefinitionVersionWithOptions(SwitchProcessDefinitionVersionRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("release")
                .setPathname("/dolphinscheduler/projects/${projectCode}/process-definition/${code}/versions/${version}");
        return TeaModel.toModel(this.doRequest(request, params, runtime), SwitchProcessDefinitionVersionResponse.class);
    }

    public VerifyProcessDefinitionNameResponse verifyProcessDefinitionName(VerifyProcessDefinitionNameRequest request) throws Exception {
        return this.verifyProcessDefinitionNameWithOptions(request, new RuntimeOptions());
    }

    public VerifyProcessDefinitionNameResponse verifyProcessDefinitionNameWithOptions(VerifyProcessDefinitionNameRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("verify-name")
                .setPathname("/dolphinscheduler/projects/${projectCode}/process-definition/verify-name");
        return TeaModel.toModel(this.doRequest(request, params, runtime), VerifyProcessDefinitionNameResponse.class);
    }

    public BatchDeleteProcessDefinitionByCodesResponse batchDeleteProcessDefinitionByCodes(BatchDeleteProcessDefinitionByCodesRequest request) throws Exception {
        return this.batchDeleteProcessDefinitionByCodesWithOptions(request, new RuntimeOptions());
    }

    public BatchDeleteProcessDefinitionByCodesResponse batchDeleteProcessDefinitionByCodesWithOptions(BatchDeleteProcessDefinitionByCodesRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("batchDeleteByCodes")
                .setPathname("/dolphinscheduler/projects/${projectCode}/process-definition/batch-delete")
                .setMethod(HttpMethod.POST.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), BatchDeleteProcessDefinitionByCodesResponse.class);
    }

    public BatchDeleteProcessInstanceByIdsResponse batchDeleteProcessInstanceByIds(BatchDeleteProcessInstanceByIdsRequest request) throws Exception {
        return this.batchDeleteProcessInstanceByIdsWithOptions(request, new RuntimeOptions());
    }

    public BatchDeleteProcessInstanceByIdsResponse batchDeleteProcessInstanceByIdsWithOptions(BatchDeleteProcessInstanceByIdsRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("batchDeleteProcessInstanceByIds")
                .setPathname("/dolphinscheduler/projects/${projectCode}/process-instances/batch-delete")
                .setMethod(HttpMethod.POST.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), BatchDeleteProcessInstanceByIdsResponse.class);
    }

    public QueryParentInstanceBySubIdResponse queryParentInstanceBySubId(QueryParentInstanceBySubIdRequest request) throws Exception {
        return this.queryParentInstanceBySubIdWithOptions(request, new RuntimeOptions());
    }

    public QueryParentInstanceBySubIdResponse queryParentInstanceBySubIdWithOptions(QueryParentInstanceBySubIdRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("batchDeleteProcessInstanceByIds")
                .setPathname("/dolphinscheduler/projects/${projectCode}/process-instances/query-parent-by-sub");
        return TeaModel.toModel(this.doRequest(request, params, runtime), QueryParentInstanceBySubIdResponse.class);
    }

    public QuerySubProcessInstanceByTaskIdResponse querySubProcessInstanceByTaskId(QuerySubProcessInstanceByTaskIdRequest request) throws Exception {
        return this.querySubProcessInstanceByTaskIdWithOptions(request, new RuntimeOptions());
    }

    public QuerySubProcessInstanceByTaskIdResponse querySubProcessInstanceByTaskIdWithOptions(QuerySubProcessInstanceByTaskIdRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("batchDeleteProcessInstanceByIds")
                .setPathname("/dolphinscheduler/projects/${projectCode}/process-instances/query-sub-by-parent");
        return TeaModel.toModel(this.doRequest(request, params, runtime), QuerySubProcessInstanceByTaskIdResponse.class);
    }

    public BatchExportProcessDefinitionByCodesResponse batchExportProcessDefinitionByCodes(BatchExportProcessDefinitionByCodesRequest request) throws Exception {
        return this.batchExportProcessDefinitionByCodesWithOptions(request, new RuntimeOptions());
    }

    public BatchExportProcessDefinitionByCodesResponse batchExportProcessDefinitionByCodesWithOptions(BatchExportProcessDefinitionByCodesRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("batchExportByCodes")
                .setPathname("/dolphinscheduler/projects/${projectCode}/process-definition/batch-export")
                .setMethod(HttpMethod.POST.name()).setBodyType("byte");
        return TeaModel.toModel(this.doRequest(request, params, runtime), BatchExportProcessDefinitionByCodesResponse.class);
    }


    public QueryProcessInstanceListResponse queryProcessInstanceList(QueryProcessInstanceListRequest request) throws Exception {
        return this.queryProcessInstanceListWithOptions(request, new RuntimeOptions());
    }

    public QueryProcessInstanceListResponse queryProcessInstanceListWithOptions(QueryProcessInstanceListRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("queryProcessInstanceListPaging")
                .setPathname("/dolphinscheduler/projects/${projectCode}/process-instances");
        return TeaModel.toModel(this.doRequest(request, params, runtime), new TypeToken<QueryProcessInstanceListResponse>() {
        }.getType());
    }


    public QueryProcessInstanceResponse queryProcessInstanceById(QueryProcessInstanceRequest request) throws Exception {
        return this.queryProcessInstanceByIdWithOptions(request, new RuntimeOptions());
    }

    public QueryProcessInstanceResponse queryProcessInstanceByIdWithOptions(QueryProcessInstanceRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("queryProcessInstanceById")
                .setPathname("/dolphinscheduler/projects/${projectCode}/process-instances/${id}");
        return TeaModel.toModel(this.doRequest(request, params, runtime), QueryProcessInstanceResponse.class);
    }

    public QueryTaskListByProcessIdResponse queryTaskListByProcessId(QueryTaskListByProcessIdRequest request) throws Exception {
        return this.queryTaskListByProcessIdWithOptions(request, new RuntimeOptions());
    }

    public QueryTaskListByProcessIdResponse queryTaskListByProcessIdWithOptions(QueryTaskListByProcessIdRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("queryTaskListByProcessId")
                .setPathname("/dolphinscheduler/projects/${projectCode}/process-instances/${id}/tasks");
        return TeaModel.toModel(this.doRequest(request, params, runtime), QueryTaskListByProcessIdResponse.class);
    }

    public ImportProcessDefinitionResponse importProcessDefinition(ImportProcessDefinitionRequest request) throws Exception {
        return this.importProcessDefinitionWithOptions(request, new RuntimeOptions());
    }

    public ImportProcessDefinitionResponse importProcessDefinitionWithOptions(ImportProcessDefinitionRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("importProcessDefinition")
                .setPathname("/dolphinscheduler/projects/${projectCode}/process-definition/import")
                .setMethod(HttpMethod.POST.name()).setReqBodyType("multiPartFormData");
        return TeaModel.toModel(this.doRequest(request, request.stream, params, runtime), ImportProcessDefinitionResponse.class);
    }

    public UpdateProcessInstanceResponse updateProcessInstance(UpdateProcessInstanceRequest request) throws Exception {
        return this.updateProcessInstanceWithOptions(request, new RuntimeOptions());
    }

    public UpdateProcessInstanceResponse updateProcessInstanceWithOptions(UpdateProcessInstanceRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("updateProcessInstance")
                .setPathname("/dolphinscheduler/projects/${projectCode}/process-instances/${id}")
                .setMethod(HttpMethod.PUT.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), UpdateProcessInstanceResponse.class);
    }

    public DeleteProcessInstanceByIdResponse deleteProcessInstanceById(DeleteProcessInstanceByIdRequest request) throws Exception {
        return this.deleteProcessInstanceByIdWithOptions(request, new RuntimeOptions());
    }

    public DeleteProcessInstanceByIdResponse deleteProcessInstanceByIdWithOptions(DeleteProcessInstanceByIdRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("updateProcessInstance")
                .setPathname("/dolphinscheduler/projects/${projectCode}/process-instances/${id}")
                .setMethod(HttpMethod.DELETE.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), DeleteProcessInstanceByIdResponse.class);
    }

    public StartProcessInstanceResponse startProcessInstance(StartProcessInstanceRequest request) throws Exception {
        return this.startProcessInstanceWithOptions(request, new RuntimeOptions());
    }

    public StartProcessInstanceResponse startProcessInstanceWithOptions(StartProcessInstanceRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("startProcessInstance")
                .setPathname("/dolphinscheduler/projects/${projectCode}/executors/start-process-instance")
                .setMethod(HttpMethod.POST.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), StartProcessInstanceResponse.class);
    }

    public ExecuteProcessInstanceResponse executeProcessInstance(ExecuteProcessInstanceRequest request) throws Exception {
        return this.executeProcessInstanceWithOptions(request, new RuntimeOptions());
    }

    public ExecuteProcessInstanceResponse executeProcessInstanceWithOptions(ExecuteProcessInstanceRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("execute")
                .setPathname("/dolphinscheduler/projects/${projectCode}/executors/execute")
                .setMethod(HttpMethod.POST.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), ExecuteProcessInstanceResponse.class);
    }

    public StartCheckProcessDefinitionResponse startCheckProcessDefinition(StartCheckProcessDefinitionRequest request) throws Exception {
        return this.startCheckProcessDefinitionWithOptions(request, new RuntimeOptions());
    }

    public StartCheckProcessDefinitionResponse startCheckProcessDefinitionWithOptions(StartCheckProcessDefinitionRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("startCheckProcessDefinition")
                .setPathname("/dolphinscheduler/projects/${projectCode}/executors/start-check")
                .setMethod(HttpMethod.POST.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), StartCheckProcessDefinitionResponse.class);
    }

    public QueryScheduleResponse querySchedule(QueryScheduleRequest request) throws Exception {
        return this.queryScheduleWithOptions(request, new RuntimeOptions());
    }

    public QueryScheduleResponse queryScheduleWithOptions(QueryScheduleRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("queryScheduleList")
                .setPathname("/dolphinscheduler/projects/${projectCode}/schedules");
        return TeaModel.toModel(this.doRequest(request, params, runtime), new TypeToken<QueryScheduleResponse>() {
        }.getType());
    }

    public CreateScheduleResponse createSchedule(CreateScheduleRequest request) throws Exception {
        return this.createScheduleWithOptions(request, new RuntimeOptions());
    }

    public CreateScheduleResponse createScheduleWithOptions(CreateScheduleRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("createSchedule")
                .setPathname("/dolphinscheduler/projects/${projectCode}/schedules")
                .setMethod(HttpMethod.POST.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), CreateScheduleResponse.class);
    }

    public UpdateScheduleResponse updateSchedule(UpdateScheduleRequest request) throws Exception {
        return this.updateScheduleWithOptions(request, new RuntimeOptions());
    }

    public UpdateScheduleResponse updateScheduleWithOptions(UpdateScheduleRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("updateSchedule")
                .setPathname("/dolphinscheduler/projects/${projectCode}/schedules/${id}")
                .setMethod(HttpMethod.PUT.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), UpdateScheduleResponse.class);
    }

    public DeleteScheduleByIdResponse deleteSchedule(DeleteScheduleByIdRequest request) throws Exception {
        return this.deleteScheduleWithOptions(request, new RuntimeOptions());
    }

    public DeleteScheduleByIdResponse deleteScheduleWithOptions(DeleteScheduleByIdRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("deleteScheduleById")
                .setPathname("/dolphinscheduler/projects/${projectCode}/schedules/${id}")
                .setMethod(HttpMethod.DELETE.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), DeleteScheduleByIdResponse.class);
    }

    public ReleaseScheduleOnlineResponse releaseScheduleOnline(ReleaseScheduleOnlineRequest request) throws Exception {
        return this.releaseScheduleOnlineWithOptions(request, new RuntimeOptions());
    }

    public ReleaseScheduleOnlineResponse releaseScheduleOnlineWithOptions(ReleaseScheduleOnlineRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("online")
                .setPathname("/dolphinscheduler/projects/${projectCode}/schedules/${id}/online")
                .setMethod(HttpMethod.POST.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), ReleaseScheduleOnlineResponse.class);
    }

    public ReleaseScheduleOfflineResponse releaseScheduleOffline(ReleaseScheduleOfflineRequest request) throws Exception {
        return this.releaseScheduleOfflineWithOptions(request, new RuntimeOptions());
    }

    public ReleaseScheduleOfflineResponse releaseScheduleOfflineWithOptions(ReleaseScheduleOfflineRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("deleteScheduleById")
                .setPathname("/dolphinscheduler/projects/${projectCode}/schedules/${id}/offline")
                .setMethod(HttpMethod.POST.name());
        return TeaModel.toModel(this.doRequest(request, params, runtime), ReleaseScheduleOfflineResponse.class);
    }

    public QueryProcessInstanceStatesResponse queryProcessInstanceStates(QueryProcessInstanceStatesRequest request) throws Exception {
        return this.queryProcessInstanceStatesWithOptions(request, new RuntimeOptions());
    }

    public QueryProcessInstanceStatesResponse queryProcessInstanceStatesWithOptions(QueryProcessInstanceStatesRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("countProcessInstanceState")
                .setPathname("/dolphinscheduler/projects/analysis/process-state-count");
        return TeaModel.toModel(this.doRequest(request, params, runtime), new TypeToken<QueryProcessInstanceStatesResponse>() {
        }.getType());
    }

    public QueryTaskStatesResponse queryTaskStates(QueryTaskStatesRequest request) throws Exception {
        return this.queryTaskStatesWithOptions(request, new RuntimeOptions());
    }

    public QueryTaskStatesResponse queryTaskStatesWithOptions(QueryTaskStatesRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("countTaskState")
                .setPathname("/dolphinscheduler/projects/analysis/task-state-count");
        return TeaModel.toModel(this.doRequest(request, params, runtime), new TypeToken<QueryTaskStatesResponse>() {
        }.getType());
    }

    public CreateTaskDefinitionCodeResponse createTaskDefinitionCode(CreateTaskDefinitionCodeRequest request) throws Exception {
        return this.createTaskDefinitionCodeWithOptions(request, new RuntimeOptions());
    }

    public CreateTaskDefinitionCodeResponse createTaskDefinitionCodeWithOptions(CreateTaskDefinitionCodeRequest request, RuntimeOptions runtime) throws Exception {
        Params params = new Params().setAction("genTaskCodeList")
                .setPathname("/dolphinscheduler/projects/${projectCode}/task-definition/gen-task-codes");
        return TeaModel.toModel(this.doRequest(request, params, runtime), CreateTaskDefinitionCodeResponse.class);
    }
}
