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

import static org.apache.dolphinscheduler.api.enums.Status.AUTHORIZED_DATA_SOURCE;
import static org.apache.dolphinscheduler.api.enums.Status.CONNECTION_TEST_FAILURE;
import static org.apache.dolphinscheduler.api.enums.Status.CONNECT_DATASOURCE_FAILURE;
import static org.apache.dolphinscheduler.api.enums.Status.CREATE_DATASOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_DATA_SOURCE_FAILURE;
import static org.apache.dolphinscheduler.api.enums.Status.GET_DATASOURCE_DATABASES_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GET_DATASOURCE_TABLES_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.GET_DATASOURCE_TABLE_COLUMNS_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.KERBEROS_STARTUP_STATE;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_DATASOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UNAUTHORIZED_DATASOURCE;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_DATASOURCE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.VERIFY_DATASOURCE_NAME_FAILURE;

import org.apache.dolphinscheduler.api.aspect.AccessLogAnnotation;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.DataSourceService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.datasource.api.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.CommonUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.spi.datasource.ConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * data source controller
 */
@Tag(name = "DATA_SOURCE_TAG")
@RestController
@RequestMapping("datasources")
public class DataSourceController extends BaseController {

    @Autowired
    private DataSourceService dataSourceService;

    /**
     * create data source
     *
     * @param loginUser login user
     * @param jsonStr   datasource param
     *                  example: {"type":"MYSQL","name":"txx","note":"","host":"localhost","port":3306,"principal":"","javaSecurityKrb5Conf":"","loginUserKeytabUsername":"","loginUserKeytabPath":"","userName":"root","password":"xxx","database":"ds","connectType":"","other":{"serverTimezone":"GMT-8"},"id":2}
     * @return create result code
     */
    @Operation(summary = "createDataSource", description = "CREATE_DATA_SOURCE_NOTES")
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_DATASOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> createDataSource(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @Parameter(name = "dataSourceParam", description = "DATA_SOURCE_PARAM", required = true) @RequestBody String jsonStr) {
        BaseDataSourceParamDTO dataSourceParam = DataSourceUtils.buildDatasourceParam(jsonStr);
        return dataSourceService.createDataSource(loginUser, dataSourceParam);
    }

    /**
     * updateProcessInstance data source
     *
     * @param loginUser login user
     * @param id        datasource id
     * @param jsonStr   datasource param
     *                  example: {"type":"MYSQL","name":"txx","note":"","host":"localhost","port":3306,"principal":"","javaSecurityKrb5Conf":"","loginUserKeytabUsername":"","loginUserKeytabPath":"","userName":"root","password":"xxx","database":"ds","connectType":"","other":{"serverTimezone":"GMT-8"},"id":2}
     * @return update result code
     */
    @Operation(summary = "updateDataSource", description = "UPDATE_DATA_SOURCE_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "DATA_SOURCE_ID", required = true, schema = @Schema(implementation = int.class)),
            @Parameter(name = "dataSourceParam", description = "DATA_SOURCE_PARAM", required = true, schema = @Schema(implementation = BaseDataSourceParamDTO.class))
    })
    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_DATASOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> updateDataSource(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @PathVariable(value = "id") Integer id,
                                           @RequestBody String jsonStr) {
        BaseDataSourceParamDTO dataSourceParam = DataSourceUtils.buildDatasourceParam(jsonStr);
        dataSourceParam.setId(id);
        return dataSourceService.updateDataSource(dataSourceParam.getId(), loginUser, dataSourceParam);
    }

    /**
     * query data source detail
     *
     * @param loginUser login user
     * @param id datasource id
     * @return data source detail
     */
    @Operation(summary = "queryDataSource", description = "QUERY_DATA_SOURCE_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "DATA_SOURCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))

    })
    @GetMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DATASOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> queryDataSource(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @PathVariable("id") int id) {
        BaseDataSourceParamDTO dataSource = dataSourceService.queryDataSource(id, loginUser);
        return Result.success(dataSource);
    }

    /**
     * query datasource by type
     *
     * @param loginUser login user
     * @param type data source type
     * @return data source list page
     */
    @Operation(summary = "queryDataSourceList", description = "QUERY_DATA_SOURCE_LIST_BY_TYPE_NOTES")
    @Parameters({
            @Parameter(name = "type", description = "DB_TYPE", required = true, schema = @Schema(implementation = DbType.class)),
    })
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DATASOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> queryDataSourceList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @RequestParam("type") DbType type) {
        List<DataSource> datasourceList = dataSourceService.queryDataSourceList(loginUser, type.ordinal());
        return Result.success(datasourceList);
    }

    /**
     * query datasource with paging
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo page number
     * @param pageSize page size
     * @return data source list page
     */
    @Operation(summary = "queryDataSourceListPaging", description = "QUERY_DATA_SOURCE_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "searchVal", description = "SEARCH_VAL", schema = @Schema(implementation = String.class)),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "20"))
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DATASOURCE_ERROR)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> queryDataSourceListPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                    @RequestParam(value = "searchVal", required = false) String searchVal,
                                                    @RequestParam("pageNo") Integer pageNo,
                                                    @RequestParam("pageSize") Integer pageSize) {
        Result<Object> result = checkPageParams(pageNo, pageSize);
        if (!result.checkResult()) {
            return result;
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        PageInfo<DataSource> pageInfo =
                dataSourceService.queryDataSourceListPaging(loginUser, searchVal, pageNo, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * connect datasource
     *
     * @param loginUser login user
     * @param jsonStr   datasource param
     *                  example: {"type":"MYSQL","name":"txx","note":"","host":"localhost","port":3306,"principal":"","javaSecurityKrb5Conf":"","loginUserKeytabUsername":"","loginUserKeytabPath":"","userName":"root","password":"xxx","database":"ds","connectType":"","other":{"serverTimezone":"GMT-8"},"id":2}
     * @return connect result code
     */
    @Operation(summary = "connectDataSource", description = "CONNECT_DATA_SOURCE_NOTES")
    @PostMapping(value = "/connect")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(CONNECT_DATASOURCE_FAILURE)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> connectDataSource(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "dataSourceParam") @RequestBody String jsonStr) {
        BaseDataSourceParamDTO dataSourceParam = DataSourceUtils.buildDatasourceParam(jsonStr);
        DataSourceUtils.checkDatasourceParam(dataSourceParam);
        ConnectionParam connectionParams = DataSourceUtils.buildConnectionParams(dataSourceParam);
        return dataSourceService.checkConnection(dataSourceParam.getType(), connectionParams);
    }

    /**
     * connection test
     *
     * @param loginUser login user
     * @param id data source id
     * @return connect result code
     */
    @Operation(summary = "connectionTest", description = "CONNECT_DATA_SOURCE_TEST_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "DATA_SOURCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/{id}/connect-test")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(CONNECTION_TEST_FAILURE)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> connectionTest(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @PathVariable("id") int id) {
        return dataSourceService.connectionTest(id);
    }

    /**
     * delete datasource by id
     *
     * @param loginUser login user
     * @param id datasource id
     * @return delete result
     */
    @Operation(summary = "deleteDataSource", description = "DELETE_DATA_SOURCE_NOTES")
    @Parameters({
            @Parameter(name = "id", description = "DATA_SOURCE_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_DATA_SOURCE_FAILURE)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> deleteDataSource(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @PathVariable("id") int id) {
        return dataSourceService.delete(loginUser, id);
    }

    /**
     * verify datasource name
     *
     * @param loginUser login user
     * @param name data source name
     * @return true if data source name not exists.otherwise return false
     */
    @Operation(summary = "verifyDataSourceName", description = "VERIFY_DATA_SOURCE_NOTES")
    @Parameters({
            @Parameter(name = "name", description = "DATA_SOURCE_NAME", required = true, schema = @Schema(implementation = String.class))
    })
    @GetMapping(value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_DATASOURCE_NAME_FAILURE)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> verifyDataSourceName(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                               @RequestParam(value = "name") String name) {
        return dataSourceService.verifyDataSourceName(name);
    }

    /**
     * unauthorized datasource
     *
     * @param loginUser login user
     * @param userId user id
     * @return unauthed data source result code
     */
    @Operation(summary = "unauthDatasource", description = "UNAUTHORIZED_DATA_SOURCE_NOTES")
    @Parameters({
            @Parameter(name = "userId", description = "USER_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/unauth-datasource")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UNAUTHORIZED_DATASOURCE)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> unAuthDatasource(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @RequestParam("userId") Integer userId) {

        List<DataSource> unAuthDatasourceList = dataSourceService.unAuthDatasource(loginUser, userId);
        return Result.success(unAuthDatasourceList);
    }

    /**
     * authorized datasource
     *
     * @param loginUser login user
     * @param userId user id
     * @return authorized result code
     */
    @Operation(summary = "authedDatasource", description = "AUTHORIZED_DATA_SOURCE_NOTES")
    @Parameters({
            @Parameter(name = "userId", description = "USER_ID", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/authed-datasource")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(AUTHORIZED_DATA_SOURCE)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> authedDatasource(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @RequestParam("userId") Integer userId) {
        List<DataSource> authedDatasourceList = dataSourceService.authedDatasource(loginUser, userId);
        return Result.success(authedDatasourceList);
    }

    /**
     * get user info
     *
     * @param loginUser login user
     * @return user info data
     */
    @Operation(summary = "getKerberosStartupState", description = "GET_USER_INFO_NOTES")
    @GetMapping(value = "/kerberos-startup-state")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(KERBEROS_STARTUP_STATE)
    @AccessLogAnnotation(ignoreRequestArgs = "loginUser")
    public Result<Object> getKerberosStartupState(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        // if upload resource is HDFS and kerberos startup is true , else false
        return success(Status.SUCCESS.getMsg(), CommonUtils.getKerberosStartupState());
    }

    @Operation(summary = "tables", description = "GET_DATASOURCE_TABLES_NOTES")
    @Parameters({
            @Parameter(name = "datasourceId", description = "DATA_SOURCE_ID", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "database", description = "DATABASE", required = true, schema = @Schema(implementation = String.class, example = "test"))
    })
    @GetMapping(value = "/tables")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_DATASOURCE_TABLES_ERROR)
    public Result<Object> getTables(@RequestParam("datasourceId") Integer datasourceId,
                                    @RequestParam(value = "database") String database) {
        List<ParamsOptions> options = dataSourceService.getTables(datasourceId, database);
        return Result.success(options);
    }

    @Operation(summary = "tableColumns", description = "GET_DATASOURCE_TABLE_COLUMNS_NOTES")
    @Parameters({
            @Parameter(name = "datasourceId", description = "DATA_SOURCE_ID", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "tableName", description = "TABLE_NAME", required = true, schema = @Schema(implementation = String.class, example = "test")),
            @Parameter(name = "database", description = "DATABASE", required = true, schema = @Schema(implementation = String.class, example = "test"))
    })
    @GetMapping(value = "/tableColumns")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_DATASOURCE_TABLE_COLUMNS_ERROR)
    public Result<Object> getTableColumns(@RequestParam("datasourceId") Integer datasourceId,
                                          @RequestParam("tableName") String tableName,
                                          @RequestParam(value = "database") String database) {
        List<ParamsOptions> options = dataSourceService.getTableColumns(datasourceId, database, tableName);
        return Result.success(options);
    }

    @Operation(summary = "databases", description = "GET_DATASOURCE_DATABASE_NOTES")
    @Parameters({
            @Parameter(name = "datasourceId", description = "DATA_SOURCE_ID", required = true, schema = @Schema(implementation = int.class, example = "1"))
    })
    @GetMapping(value = "/databases")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_DATASOURCE_DATABASES_ERROR)
    public Result<Object> getDatabases(@RequestParam("datasourceId") Integer datasourceId) {
        List<ParamsOptions> options = dataSourceService.getDatabases(datasourceId);
        return Result.success(options);
    }
}
