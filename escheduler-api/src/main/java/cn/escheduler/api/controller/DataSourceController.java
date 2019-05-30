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
package cn.escheduler.api.controller;

import cn.escheduler.api.enums.Status;
import cn.escheduler.api.service.DataSourceService;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.Result;
import cn.escheduler.common.enums.DbType;
import cn.escheduler.common.utils.ParameterUtils;
import cn.escheduler.dao.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static cn.escheduler.api.enums.Status.*;


/**
 * data source controller
 */
@RestController
@RequestMapping("datasources")
public class DataSourceController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceController.class);

    @Autowired
    private DataSourceService dataSourceService;

    /**
     * create data source
     * 创建数据源
     *
     * @param loginUser
     * @param name
     * @param note
     * @param type
     * @param other
     * @return
     */
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Result createDataSource(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam("name") String name,
                                   @RequestParam(value = "note", required = false) String note,
                                   @RequestParam(value = "type") DbType type,
                                   @RequestParam(value = "host") String host,
                                   @RequestParam(value = "port") String port,
                                   @RequestParam(value = "database") String database,
                                   @RequestParam(value = "userName") String userName,
                                   @RequestParam(value = "password") String password,
                                   @RequestParam(value = "other") String other) {
        logger.info("login user {} create datasource ame: {}, note: {}, type: {}, other: {}",
                loginUser.getUserName(), name, note, type, other);
        try {
            String parameter = dataSourceService.buildParameter(name, note, type, host, port, database, userName, password, other);
            Map<String, Object> result = dataSourceService.createDataSource(loginUser, name, note, type, parameter);
            return returnDataList(result);

        } catch (Exception e) {
            logger.error(CREATE_DATASOURCE_ERROR.getMsg(),e);
            return error(Status.CREATE_DATASOURCE_ERROR.getCode(), Status.CREATE_DATASOURCE_ERROR.getMsg());
        }
    }


    /**
     * updateProcessInstance data source
     *
     * @param loginUser
     * @param name
     * @param note
     * @param type
     * @param other
     * @return
     */
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    public Result updateDataSource(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam("id") int id,
                                   @RequestParam("name") String name,
                                   @RequestParam(value = "note", required = false) String note,
                                   @RequestParam(value = "type") DbType type,
                                   @RequestParam(value = "host") String host,
                                   @RequestParam(value = "port") String port,
                                   @RequestParam(value = "database") String database,
                                   @RequestParam(value = "userName") String userName,
                                   @RequestParam(value = "password") String password,
                                   @RequestParam(value = "other") String other) {
        logger.info("login user {} updateProcessInstance datasource name: {}, note: {}, type: {}, other: {}",
                loginUser.getUserName(), name, note, type, other);
        try {
            String parameter = dataSourceService.buildParameter(name, note, type, host, port, database, userName, password, other);
            Map<String, Object> dataSource = dataSourceService.updateDataSource(id, loginUser, name, note, type, parameter);
            return returnDataList(dataSource);
        } catch (Exception e) {
            logger.error(UPDATE_DATASOURCE_ERROR.getMsg(),e);
            return error(UPDATE_DATASOURCE_ERROR.getCode(), UPDATE_DATASOURCE_ERROR.getMsg());
        }


    }

    /**
     * query data source
     *
     * @param loginUser
     * @param id
     * @return
     */
    @PostMapping(value = "/update-ui")
    @ResponseStatus(HttpStatus.OK)
    public Result queryDataSource(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam("id") int id) {
        logger.info("login user {}, query datasource: {}",
                loginUser.getUserName(), id);
        try {
            Map<String, Object> result  = dataSourceService.queryDataSource(id);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(QUERY_DATASOURCE_ERROR.getMsg(),e);
            return error(Status.QUERY_DATASOURCE_ERROR.getCode(), Status.QUERY_DATASOURCE_ERROR.getMsg());
        }


    }

    /**
     * query datasouce by type
     *
     * @param loginUser
     * @return
     */
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    public Result queryDataSourceList(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                      @RequestParam("type") DbType type) {
        try {
            Map<String, Object> result = dataSourceService.queryDataSourceList(loginUser, type.ordinal());
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(QUERY_DATASOURCE_ERROR.getMsg(),e);
            return error(Status.QUERY_DATASOURCE_ERROR.getCode(), Status.QUERY_DATASOURCE_ERROR.getMsg());
        }
    }

    /**
     * query datasource with paging
     *
     * @param loginUser
     * @param searchVal
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    public Result queryDataSourceListPaging(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @RequestParam(value = "searchVal", required = false) String searchVal,
                                            @RequestParam("pageNo") Integer pageNo,
                                            @RequestParam("pageSize") Integer pageSize) {
        try {
            Map<String, Object> result = checkPageParams(pageNo, pageSize);
            if (result.get(Constants.STATUS) != Status.SUCCESS) {
                return returnDataListPaging(result);
            }
            searchVal = ParameterUtils.handleEscapes(searchVal);
            result = dataSourceService.queryDataSourceListPaging(loginUser, searchVal, pageNo, pageSize);
            return returnDataListPaging(result);
        } catch (Exception e) {
            logger.error(QUERY_DATASOURCE_ERROR.getMsg(),e);
            return error(QUERY_DATASOURCE_ERROR.getCode(), QUERY_DATASOURCE_ERROR.getMsg());
        }
    }

    /**
     * connec datasource
     *
     * @param loginUser
     * @param name
     * @param note
     * @param type
     * @param other
     * @return
     */
    @PostMapping(value = "/connect")
    @ResponseStatus(HttpStatus.OK)
    public Result connectDataSource(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                    @RequestParam("name") String name,
                                    @RequestParam(value = "note", required = false) String note,
                                    @RequestParam(value = "type") DbType type,
                                    @RequestParam(value = "host") String host,
                                    @RequestParam(value = "port") String port,
                                    @RequestParam(value = "database") String database,
                                    @RequestParam(value = "userName") String userName,
                                    @RequestParam(value = "password") String password,
                                    @RequestParam(value = "other") String other) {
        logger.info("login user {}, connect datasource: {} failure, note: {}, type: {}, other: {}",
                loginUser.getUserName(), name, note, type, other);
        try {
            String parameter = dataSourceService.buildParameter(name, note, type, host, port, database, userName, password, other);
            Boolean isConnection = dataSourceService.checkConnection(type, parameter);
            Result result = new Result();

            if (isConnection) {
                putMsg(result, SUCCESS);
            } else {
                putMsg(result, CONNECT_DATASOURCE_FAILURE);
            }
            return result;
        } catch (Exception e) {
            logger.error(CONNECT_DATASOURCE_FAILURE.getMsg(),e);
            return error(CONNECT_DATASOURCE_FAILURE.getCode(), CONNECT_DATASOURCE_FAILURE.getMsg());
        }
    }

    /**
     * connection test
     *
     * @param loginUser
     * @return
     */
    @GetMapping(value = "/connect-by-id")
    @ResponseStatus(HttpStatus.OK)
    public Result connectionTest(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                 @RequestParam("id") int id) {
        logger.info("connection test, login user:{}, id:{}", loginUser.getUserName(), id);

        try {
            Boolean isConnection = dataSourceService.connectionTest(loginUser, id);
            Result result = new Result();

            if (isConnection) {
                putMsg(result, SUCCESS);
            } else {
                putMsg(result, CONNECTION_TEST_FAILURE);
            }
            return result;
        } catch (Exception e) {
            logger.error(CONNECTION_TEST_FAILURE.getMsg(),e);
            return error(CONNECTION_TEST_FAILURE.getCode(), CONNECTION_TEST_FAILURE.getMsg());
        }

    }

    /**
     * delete datasource by id
     *
     * @param loginUser
     * @param id datasource id
     * @return
     */
    @GetMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    public Result delete(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                         @RequestParam("id") int id) {
        try {
            logger.info("delete datasource,login user:{}, id:{}", loginUser.getUserName(), id);
            return dataSourceService.delete(loginUser, id);
        } catch (Exception e) {
            logger.error(DELETE_DATA_SOURCE_FAILURE.getMsg(),e);
            return error(DELETE_DATA_SOURCE_FAILURE.getCode(), DELETE_DATA_SOURCE_FAILURE.getMsg());
        }
    }

    /**
     * verify datasource name
     *
     * @param loginUser
     * @param name
     * @return
     */
    @GetMapping(value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    public Result verifyDataSourceName(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @RequestParam(value = "name") String name
    ) {
        logger.info("login user {}, verfiy datasource name: {}",
                loginUser.getUserName(), name);

        try {
            return dataSourceService.verifyDataSourceName(loginUser, name);
        } catch (Exception e) {
            logger.error(VERFIY_DATASOURCE_NAME_FAILURE.getMsg(),e);
            return error(VERFIY_DATASOURCE_NAME_FAILURE.getCode(), VERFIY_DATASOURCE_NAME_FAILURE.getMsg());
        }
    }



    /**
     * unauthorized datasource
     *
     * @param loginUser
     * @param userId
     * @return
     */
    @GetMapping(value = "/unauth-datasource")
    @ResponseStatus(HttpStatus.OK)
    public Result unauthDatasource(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam("userId") Integer userId) {
        try {
            logger.info("unauthorized datasource, login user:{}, unauthorized userId:{}",
                    loginUser.getUserName(), userId);
            Map<String, Object> result = dataSourceService.unauthDatasource(loginUser, userId);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(UNAUTHORIZED_DATASOURCE.getMsg(),e);
            return error(UNAUTHORIZED_DATASOURCE.getCode(), UNAUTHORIZED_DATASOURCE.getMsg());
        }
    }


    /**
     * authorized datasource
     *
     * @param loginUser
     * @param userId
     * @return
     */
    @GetMapping(value = "/authed-datasource")
    @ResponseStatus(HttpStatus.OK)
    public Result authedDatasource(@RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam("userId") Integer userId) {
        try {
            logger.info("authorized data source, login user:{}, authorized useId:{}",
                    loginUser.getUserName(), userId);
            Map<String, Object> result = dataSourceService.authedDatasource(loginUser, userId);
            return returnDataList(result);
        } catch (Exception e) {
            logger.error(AUTHORIZED_DATA_SOURCE.getMsg(),e);
            return error(AUTHORIZED_DATA_SOURCE.getCode(), AUTHORIZED_DATA_SOURCE.getMsg());
        }
    }
}
