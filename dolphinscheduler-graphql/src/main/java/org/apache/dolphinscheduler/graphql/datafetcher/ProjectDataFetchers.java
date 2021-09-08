package org.apache.dolphinscheduler.graphql.datafetcher;

import graphql.schema.DataFetcher;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ProcessDefinitionService;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.graphql.datafetcher.service.UserArgumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ProjectDataFetchers extends BaseDataFetchers {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserArgumentService userArgumentService;

    public DataFetcher<Result> mutationTypeCreateProject() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String projectName = environment.getArgument("projectName");
            String description = environment.getArgument("description");

            Map<String, Object> result = projectService.createProject(loginUser, projectName, description);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> mutationTypeUpdateProject() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int projectId = environment.getArgument("projectId");
            String projectName = environment.getArgument("projectName");
            String description = environment.getArgument("description");
            String userName = environment.getArgument("userName");

            Map<String, Object> result = projectService.update(loginUser, projectId, projectName, description, userName);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryProjectById() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int projectId = environment.getArgument("projectId");

            Map<String, Object> result = projectService.queryById(projectId);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryProjectListPaging() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String searchVal = environment.getArgument("searchVal");
            int pageNo = environment.getArgument("pageNo");
            int pageSize = environment.getArgument("pageSize");

            Result result = checkPageParams(pageNo, pageSize);
            if (!result.checkResult()) {
                return result;
            }
            searchVal = ParameterUtils.handleEscapes(searchVal);
            result = projectService.queryProjectListPaging(loginUser, pageSize, pageNo, searchVal);
            return result;
        };
    }

    public DataFetcher<Result>  mutationTypeDeleteProject() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int projectId = environment.getArgument("projectId");

            Map<String, Object> result = projectService.deleteProject(loginUser, projectId);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result>  queryTypeQueryUnauthorizedProject() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int userId = environment.getArgument("userId");

            Map<String, Object> result = projectService.queryUnauthorizedProject(loginUser, userId);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result>  queryTypeQueryAuthorizedProject() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int userId = environment.getArgument("userId");

            Map<String, Object> result = projectService.queryAuthorizedProject(loginUser, userId);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result>  queryTypeQueryProjectCreatedAndAuthorizedByUser() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            Map<String, Object> result = projectService.queryProjectCreatedAndAuthorizedByUser(loginUser);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result>  queryTypeQueryQueryAllProjectList() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            Map<String, Object> result = projectService.queryAllProjectList();
            return returnDataList(result);
        };
    }

}
