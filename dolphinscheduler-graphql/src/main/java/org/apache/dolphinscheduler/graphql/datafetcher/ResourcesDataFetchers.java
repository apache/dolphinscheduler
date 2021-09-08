package org.apache.dolphinscheduler.graphql.datafetcher;

import graphql.schema.DataFetcher;
import org.apache.dolphinscheduler.api.controller.ResourcesController;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ResourcesService;
import org.apache.dolphinscheduler.api.service.UdfFuncService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.common.enums.ResourceType;
import org.apache.dolphinscheduler.common.enums.UdfType;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.graphql.datafetcher.service.UserArgumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.apache.dolphinscheduler.api.enums.Status.RESOURCE_FILE_IS_EMPTY;

@Component
public class ResourcesDataFetchers extends BaseDataFetchers {

    private static final Logger logger = LoggerFactory.getLogger(ResourcesController.class);

    @Autowired
    private ResourcesService resourceService;

    @Autowired
    private UdfFuncService udfFuncService;

    @Autowired
    private UserArgumentService userArgumentService;

    public DataFetcher<Result> mutationTypeCreateDirectory() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            ResourceType type = ResourceType.valueOf(environment.getArgument("resourceType"));
            String alias = environment.getArgument("alias");
            String description = environment.getArgument("description");
            int pid = environment.getArgument("pid");
            String currentDir = environment.getArgument("currentDir");

            return resourceService.createDirectory(loginUser, alias, description, type, pid, currentDir);
        };
    }

    public DataFetcher<Result> queryTypeQueryResourceList() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            ResourceType type = ResourceType.valueOf(environment.getArgument("resourceType"));

            Map<String, Object> result = resourceService.queryResourceList(loginUser, type);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryResourceListPaging() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            ResourceType type = ResourceType.valueOf(environment.getArgument("resourceType"));
            int id = environment.getArgument("id");
            int pageNo = environment.getArgument("pageNo");
            String searchVal = environment.getArgument("searchVal");
            int pageSize = environment.getArgument("pageSize");

            Result result = checkPageParams(pageNo, pageSize);
            if (!result.checkResult()) {
                return result;
            }

            searchVal = ParameterUtils.handleEscapes(searchVal);
            result = resourceService.queryResourceListPaging(loginUser, id, type, searchVal, pageNo, pageSize);
            return result;
        };
    }

    public DataFetcher<Result> mutationTypeDeleteResource() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int resourceId = environment.getArgument("resourceId");

            return resourceService.delete(loginUser, resourceId);
        };
    }

    public DataFetcher<Result> queryTypeVerifyResourceName() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String fullName = environment.getArgument("fullName");
            ResourceType type = ResourceType.valueOf(environment.getArgument("resourceType"));

            return resourceService.verifyResourceName(fullName, type, loginUser);
        };
    }

    public DataFetcher<Result> queryTypeQueryResourceJarList() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            ResourceType type = ResourceType.valueOf(environment.getArgument("resourceType"));
            ProgramType programType = environment.getArgument("programType") == null
                    ? null
                    : ProgramType.valueOf(environment.getArgument("programType"));

            Map<String, Object> result = resourceService.queryResourceByProgramType(loginUser, type, programType);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryResource() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String fullName = environment.getArgument("fullName");
            int id = environment.getArgument("id");
            ResourceType type = ResourceType.valueOf(environment.getArgument("resourceType"));

            return resourceService.queryResource(fullName, id, type);
        };
    }

    public DataFetcher<Result> queryTypeViewResource() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int resourceId = environment.getArgument("resourceId");
            int skipLineNum = environment.getArgument("skipLineNum");
            int limit = environment.getArgument("limit");

            return resourceService.readResource(resourceId, skipLineNum, limit);
        };
    }

    public DataFetcher<Result> mutationTypeOnlineCreateResource() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            ResourceType type = ResourceType.valueOf(environment.getArgument("resourceType"));
            String fileName = environment.getArgument("fileName");
            String fileSuffix = environment.getArgument("fileSuffix");
            String description = environment.getArgument("description");
            String content = environment.getArgument("content");
            int pid = environment.getArgument("pid");
            String currentDir = environment.getArgument("currentDir");

            if (StringUtils.isEmpty(content)) {
                logger.error("resource file contents are not allowed to be empty");
                return error(Status.RESOURCE_FILE_IS_EMPTY.getCode(), RESOURCE_FILE_IS_EMPTY.getMsg());
            }
            return resourceService.onlineCreateResource(loginUser, type, fileName, fileSuffix, description, content, pid, currentDir);
        };
    }

    public DataFetcher<Result> mutationTypeUpdateResourceContent() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int resourceId = environment.getArgument("resourceId");
            String content = environment.getArgument("content");

            if (StringUtils.isEmpty(content)) {
                logger.error("The resource file contents are not allowed to be empty");
                return error(Status.RESOURCE_FILE_IS_EMPTY.getCode(), RESOURCE_FILE_IS_EMPTY.getMsg());
            }
            return resourceService.updateResourceContent(resourceId, content);
        };
    }

    public DataFetcher<ResponseEntity> queryTypeDownloadResource() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return ResponseEntity.notFound().build();
            }
            User loginUser = (User) selectUserResult.getData();

            int resourceId = environment.getArgument("resourceId");

            Resource file = resourceService.downloadResource(resourceId);
            if (file == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Status.RESOURCE_NOT_EXIST.getMsg());
            }
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        };
    }

    public DataFetcher<Result> mutationTypeCreateUdfFunc() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            UdfType type = UdfType.valueOf(environment.getArgument("udfType"));
            String funcName = environment.getArgument("funcName");
            String className = environment.getArgument("className");
            String argTypes = environment.getArgument("argTypes");
            String database = environment.getArgument("database");
            String description = environment.getArgument("description");
            int resourceId = environment.getArgument("resourceId");

            return udfFuncService.createUdfFunction(loginUser, funcName, className, argTypes, database, description, type, resourceId);
        };
    }

    public DataFetcher<Result> queryTypeViewUIUdfFunction() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int id = environment.getArgument("id");

            Map<String, Object> map = udfFuncService.queryUdfFuncDetail(id);
            return returnDataList(map);
        };
    }

    public DataFetcher<Result> mutationTypeUpdateUdfFunc() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int udfFuncId = environment.getArgument("udfFuncId");
            UdfType type = UdfType.valueOf(environment.getArgument("udfType"));
            String funcName = environment.getArgument("funcNamec");
            String className = environment.getArgument("className");
            String argTypes = environment.getArgument("argTypes");
            String database = environment.getArgument("database");
            String description = environment.getArgument("description");
            int resourceId = environment.getArgument("resourceId");

            Map<String, Object> result = udfFuncService.updateUdfFunc(udfFuncId, funcName, className, argTypes, database, description, type, resourceId);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeQueryUdfFuncListPaging() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int pageNo = environment.getArgument("pageNo");
            String searchVal = environment.getArgument("searchVal");
            int pageSize = environment.getArgument("pageSize");

            Result result = checkPageParams(pageNo, pageSize);
            if (!result.checkResult()) {
                return result;

            }
            result = udfFuncService.queryUdfFuncListPaging(loginUser, searchVal, pageNo, pageSize);
            return result;
        };
    }

    public DataFetcher<Result> queryTypeQueryUdfFuncList() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            UdfType type = UdfType.valueOf(environment.getArgument("udfType"));

            Map<String, Object> result = udfFuncService.queryUdfFuncList(loginUser, type.ordinal());
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeVerifyUdfFuncName() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String name = environment.getArgument("name");

            return udfFuncService.verifyUdfFuncByName(name);
        };
    }

    public DataFetcher<Result> mutationTypeDeleteUdfFunc() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int udfFuncId = environment.getArgument("udfFuncId");

            return udfFuncService.delete(udfFuncId);
        };
    }

    public DataFetcher<Result> queryTypeAuthorizedFile() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int userId = environment.getArgument("userId");

            Map<String, Object> result = resourceService.authorizedFile(loginUser, userId);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeAuthorizeResourceTree() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int userId = environment.getArgument("userId");

            Map<String, Object> result = resourceService.authorizeResourceTree(loginUser, userId);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeUnauthUDFFunc() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int userId = environment.getArgument("userId");

            Map<String, Object> result = resourceService.unauthorizedUDFFunction(loginUser, userId);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeAuthorizedUDFFunction() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int userId = environment.getArgument("userId");

            Map<String, Object> result = resourceService.authorizedUDFFunction(loginUser, userId);
            return returnDataList(result);
        };
    }

}
