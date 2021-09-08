package org.apache.dolphinscheduler.graphql.datafetcher;

import graphql.schema.DataFetcher;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.DataSourceService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.datasource.BaseDataSourceParamDTO;
import org.apache.dolphinscheduler.common.datasource.ConnectionParam;
import org.apache.dolphinscheduler.common.datasource.DatasourceUtil;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.utils.CommonUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.graphql.datafetcher.service.UserArgumentService;
import org.apache.dolphinscheduler.graphql.utils.DataSourceParamDTOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DataSourceDataFetchers extends BaseDataFetchers {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceDataFetchers.class);

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private UserArgumentService userArgumentService;

    public DataFetcher<Result> mutationTypeCreateDataSource() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            LinkedHashMap<String, Object> dataSourceParamMap = dataFetchingEnvironment.getArgument("dataSourceParam");
            BaseDataSourceParamDTO dataSourceParam =
                    DataSourceParamDTOUtil.getDataSourceParamDTO(
                            DbType.valueOf((String) dataSourceParamMap.get("dbType"))
                    );

            dataSourceParam.setId((Integer) dataSourceParamMap.get("id"));
            dataSourceParam.setDatabase((String) dataSourceParamMap.get("database"));
            dataSourceParam.setHost((String) dataSourceParamMap.get("host"));
            dataSourceParam.setName((String) dataSourceParamMap.get("name"));
            dataSourceParam.setNote((String) dataSourceParamMap.get("note"));
            dataSourceParam.setOther(JSONUtils.toMap((String) dataSourceParamMap.get("other")));
            dataSourceParam.setPassword((String) dataSourceParamMap.get("password"));
            dataSourceParam.setPort((Integer) dataSourceParamMap.get("port"));
            dataSourceParam.setUserName((String) dataSourceParamMap.get("userName"));

            return dataSourceService.createDataSource(loginUser, dataSourceParam);
        };
    }

    public DataFetcher<Result> mutationTypeUpdateDataSource() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            Map<String, Object> dataSourceParamMap = dataFetchingEnvironment.getArgument("dataSourceParam");
            BaseDataSourceParamDTO dataSourceParam =
                    DataSourceParamDTOUtil.getDataSourceParamDTO(
                            DbType.valueOf((String) dataSourceParamMap.get("dbType"))
                    );

            dataSourceParam.setId((Integer) dataSourceParamMap.get("id"));
            dataSourceParam.setDatabase((String) dataSourceParamMap.get("database"));
            dataSourceParam.setHost((String) dataSourceParamMap.get("host"));
            dataSourceParam.setName((String) dataSourceParamMap.get("name"));
            dataSourceParam.setNote((String) dataSourceParamMap.get("note"));
            dataSourceParam.setOther(JSONUtils.toMap((String) dataSourceParamMap.get("other")));
            dataSourceParam.setPassword((String) dataSourceParamMap.get("password"));
            dataSourceParam.setPort((Integer) dataSourceParamMap.get("port"));
            dataSourceParam.setUserName((String) dataSourceParamMap.get("userName"));

            logger.info("login user {} updateProcessInstance datasource name: {}, note: {}, type: {}, other: {}",
                    loginUser.getUserName(), dataSourceParam.getName(), dataSourceParam.getNote(), dataSourceParam.getType(), dataSourceParam.getOther());

            return dataSourceService.updateDataSource(dataSourceParam.getId(), loginUser, dataSourceParam);
        };
    }

    public DataFetcher<Result> queryTypeQueryDataSource() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int id = dataFetchingEnvironment.getArgument("id");

            logger.info("login user {}, query datasource: {}",
                    loginUser.getUserName(), id);
            Map<String, Object> result = dataSourceService.queryDataSource(id);
            return returnDataList(result);
        };
    }

    public DataFetcher<DbType> dataSourceDbType() {
        return dataFetchingEnvironment -> {
            DataSource dataSource = dataFetchingEnvironment.getSource();

            return dataSource.getType();
        };
    }

    public DataFetcher<Result> queryTypeQueryDataSourceList() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            DbType type = DbType.valueOf(dataFetchingEnvironment.getArgument("dbType"));

            Map<String, Object> result = dataSourceService.queryDataSourceList(loginUser, type.ordinal());
            return returnDataList(result);
        };
    }

    public DataFetcher<DbType> BaseDataSourceParamDTOTypeDbType() {
        return dataFetchingEnvironment -> {
            BaseDataSourceParamDTO baseDataSourceParamDTO = dataFetchingEnvironment.getSource();

            return baseDataSourceParamDTO.getType();
        };
    }

    public DataFetcher<Result> queryTypeQueryDataSourceListPaging() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String searchVal = dataFetchingEnvironment.getArgument("searchVal");
            int pageNo = dataFetchingEnvironment.getArgument("pageNo");
            int pageSize = dataFetchingEnvironment.getArgument("pageSize");

            Result result = checkPageParams(pageNo, pageSize);
            if (!result.checkResult()) {
                return result;
            }
            searchVal = ParameterUtils.handleEscapes(searchVal);
            return dataSourceService.queryDataSourceListPaging(loginUser, searchVal, pageNo, pageSize);
        };
    }

    public DataFetcher<Result> queryTypeConnectDataSource() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            Map<String, Object> dataSourceParamMap = dataFetchingEnvironment.getArgument("dataSourceParam");
            BaseDataSourceParamDTO dataSourceParam =
                    DataSourceParamDTOUtil.getDataSourceParamDTO(
                            DbType.valueOf((String) dataSourceParamMap.get("dbType"))
                    );

            dataSourceParam.setId((Integer) dataSourceParamMap.get("id"));
            dataSourceParam.setDatabase((String) dataSourceParamMap.get("database"));
            dataSourceParam.setHost((String) dataSourceParamMap.get("host"));
            dataSourceParam.setName((String) dataSourceParamMap.get("name"));
            dataSourceParam.setNote((String) dataSourceParamMap.get("note"));
            dataSourceParam.setOther(JSONUtils.toMap((String) dataSourceParamMap.get("other")));
            dataSourceParam.setPassword((String) dataSourceParamMap.get("password"));
            dataSourceParam.setPort((Integer) dataSourceParamMap.get("port"));
            dataSourceParam.setUserName((String) dataSourceParamMap.get("userName"));

            logger.info("login user {}, connect datasource: {}, note: {}, type: {}, other: {}",
                    loginUser.getUserName(), dataSourceParam.getName(), dataSourceParam.getNote(), dataSourceParam.getType(), dataSourceParam.getOther());
            DatasourceUtil.checkDatasourceParam(dataSourceParam);
            ConnectionParam connectionParams = DatasourceUtil.buildConnectionParams(dataSourceParam);
            return dataSourceService.checkConnection(dataSourceParam.getType(), connectionParams);
        };
    }

    public DataFetcher<Result> queryTypeConnectionTest() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int id = dataFetchingEnvironment.getArgument("id");

            logger.info("connection test, login user:{}, id:{}", loginUser.getUserName(), id);

            return dataSourceService.connectionTest(id);
        };
    }

    public DataFetcher<Result> mutationTypeDeleteDataSource() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int id = dataFetchingEnvironment.getArgument("id");

            logger.info("delete datasource,login user:{}, id:{}", loginUser.getUserName(), id);
            return dataSourceService.delete(loginUser, id);
        };
    }

    public DataFetcher<Result> queryTypeVerifyDataSourceName() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            String name = dataFetchingEnvironment.getArgument("name");

            logger.info("login user {}, verfiy datasource name: {}",
                    loginUser.getUserName(), name);

            return dataSourceService.verifyDataSourceName(name);
        };
    }

    public DataFetcher<Result> queryTypeUnauthDatasource() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int userId = dataFetchingEnvironment.getArgument("userId");

            logger.info("unauthorized datasource, login user:{}, unauthorized userId:{}",
                    loginUser.getUserName(), userId);
            Map<String, Object> result = dataSourceService.unauthDatasource(loginUser, userId);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeAuthedDatasource() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            int userId = dataFetchingEnvironment.getArgument("userId");

            logger.info("authorized data source, login user:{}, authorized useId:{}",
                    loginUser.getUserName(), userId);
            Map<String, Object> result = dataSourceService.authedDatasource(loginUser, userId);
            return returnDataList(result);
        };
    }

    public DataFetcher<Result> queryTypeGetKerberosStartupState() {
        return dataFetchingEnvironment -> {
            LinkedHashMap<String, String> loginUserMap = dataFetchingEnvironment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                logger.error("user not exist,  user id {}", loginUserMap.get("id"));
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            logger.info("login user {}", loginUser.getUserName());
            // if upload resource is HDFS and kerberos startup is true , else false
            return success(Status.SUCCESS.getMsg(), CommonUtils.getKerberosStartupState());
        };
    }

}
