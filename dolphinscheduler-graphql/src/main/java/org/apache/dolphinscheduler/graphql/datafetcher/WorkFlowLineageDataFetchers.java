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

            int projectId = environment.getArgument("projectId");
            String searchVal = environment.getArgument("searchVal");

            try {
                searchVal = ParameterUtils.handleEscapes(searchVal);
                Map<String, Object> result = workFlowLineageService.queryWorkFlowLineageByName(searchVal,projectId);
                return returnDataList(result);
            } catch (Exception e) {
                logger.error(QUERY_WORKFLOW_LINEAGE_ERROR.getMsg(),e);
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

            int projectId = environment.getArgument("projectId");
            String ids = environment.getArgument("ids");

            try {
                ids = ParameterUtils.handleEscapes(ids);
                Set<Integer> idsSet = new HashSet<>();
                if (ids != null) {
                    String[] idsStr = ids.split(",");
                    for (String id : idsStr) {
                        idsSet.add(Integer.parseInt(id));
                    }
                }

                Map<String, Object> result = workFlowLineageService.queryWorkFlowLineageByIds(idsSet, projectId);
                return returnDataList(result);
            } catch (Exception e) {
                logger.error(QUERY_WORKFLOW_LINEAGE_ERROR.getMsg(),e);
                return error(QUERY_WORKFLOW_LINEAGE_ERROR.getCode(), QUERY_WORKFLOW_LINEAGE_ERROR.getMsg());
            }
        };
    }

}
