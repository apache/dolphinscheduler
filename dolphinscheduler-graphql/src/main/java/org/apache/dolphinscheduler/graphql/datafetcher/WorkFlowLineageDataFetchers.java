package org.apache.dolphinscheduler.graphql.datafetcher;

import graphql.schema.DataFetcher;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.WorkFlowLineageService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.graphql.datafetcher.service.UserArgumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.apache.dolphinscheduler.api.enums.Status.QUERY_WORKFLOW_LINEAGE_ERROR;

@Component
public class WorkFlowLineageDataFetchers extends BaseDataFetchers {

    private static final Logger logger = LoggerFactory.getLogger(WorkFlowLineageDataFetchers.class);

    @Autowired
    private WorkFlowLineageService workFlowLineageService;

    @Autowired
    private UserArgumentService userArgumentService;

    public DataFetcher<Result> queryTypeQueryWorkFlowLineageByName() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            long projectCode = Long.parseLong(environment.getArgument("projectId"));
            String workFlowName = environment.getArgument("workFlowName");

            try {
                workFlowName = ParameterUtils.handleEscapes(workFlowName);
                Map<String, Object> result = workFlowLineageService.queryWorkFlowLineageByName(projectCode, workFlowName);
                return returnDataList(result);
            } catch (Exception e) {
                logger.error(QUERY_WORKFLOW_LINEAGE_ERROR.getMsg(), e);
                return error(QUERY_WORKFLOW_LINEAGE_ERROR.getCode(), QUERY_WORKFLOW_LINEAGE_ERROR.getMsg());
            }
        };
    }



    public DataFetcher<Result> queryTypeQueryWorkFlowLineageByIds() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            long projectCode = Long.parseLong(environment.getArgument("projectId"));

            try {
                Map<String, Object> result = workFlowLineageService.queryWorkFlowLineage(projectCode);
                return returnDataList(result);
            } catch (Exception e) {
                logger.error(QUERY_WORKFLOW_LINEAGE_ERROR.getMsg(), e);
                return error(QUERY_WORKFLOW_LINEAGE_ERROR.getCode(), QUERY_WORKFLOW_LINEAGE_ERROR.getMsg());
            }
        };
    }

    public DataFetcher<Result> queryTypeQueryWorkFlowLineageByCode() {
        return environment -> {
            LinkedHashMap<String, String> loginUserMap = environment.getArgument("loginUser");
            Result selectUserResult = userArgumentService.getUserFromArgument(loginUserMap);
            if (selectUserResult.getCode() != Status.SUCCESS.getCode()) {
                return selectUserResult;
            }
            User loginUser = (User) selectUserResult.getData();

            long projectCode = Long.parseLong(environment.getArgument("projectId"));
            long workFlowCode = Long.parseLong(environment.getArgument("workFlowCode"));

            try {
                Map<String, Object> result = workFlowLineageService.queryWorkFlowLineageByCode(projectCode, workFlowCode);
                return returnDataList(result);
            } catch (Exception e) {
                logger.error(QUERY_WORKFLOW_LINEAGE_ERROR.getMsg(), e);
                return error(QUERY_WORKFLOW_LINEAGE_ERROR.getCode(), QUERY_WORKFLOW_LINEAGE_ERROR.getMsg());
            }
        };
    }

}
