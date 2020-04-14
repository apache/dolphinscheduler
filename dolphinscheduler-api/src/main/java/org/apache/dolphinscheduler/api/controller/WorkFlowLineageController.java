package org.apache.dolphinscheduler.api.controller;

import org.apache.dolphinscheduler.api.service.WorkFlowLineageService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.apache.dolphinscheduler.api.enums.Status.QUERY_WORKFLOW_LINEAGE_ERROR;

@RestController
@RequestMapping("lineages/{projectId}")
public class WorkFlowLineageController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(WorkFlowLineageController.class);

    @Autowired
    private WorkFlowLineageService workFlowLineageService;

    @GetMapping(value="/list-name")
    @ResponseStatus(HttpStatus.OK)
    public Result queryWorkFlowLineageByName(@ApiIgnore @RequestParam(value = "searchVal", required = false) String searchVal,@ApiParam(name = "projectId", value = "PROJECT_ID", required = true) @PathVariable int projectId) {
        try {
            searchVal = ParameterUtils.handleEscapes(searchVal);
            Map<String, Object> result = workFlowLineageService.queryWorkFlowLineageByName(searchVal,projectId);
            return returnDataList(result);
        } catch (Exception e){
            logger.error(QUERY_WORKFLOW_LINEAGE_ERROR.getMsg(),e);
            return error(QUERY_WORKFLOW_LINEAGE_ERROR.getCode(), QUERY_WORKFLOW_LINEAGE_ERROR.getMsg());
        }
    }

    @GetMapping(value="/list-ids")
    @ResponseStatus(HttpStatus.OK)
    public Result queryWorkFlowLineageByIds(@ApiIgnore @RequestParam(value = "ids", required = false) String ids,@ApiParam(name = "projectId", value = "PROJECT_ID", required = true) @PathVariable int projectId) {

        try {
            ids = ParameterUtils.handleEscapes(ids);
            Set<Integer> idsSet = new HashSet<>();
            if(ids != null) {
                String[] idsStr = ids.split(",");
                for (String id : idsStr)
                {
                    idsSet.add(Integer.parseInt(id));
                }
            }

            Map<String, Object> result = workFlowLineageService.queryWorkFlowLineageByIds(idsSet, projectId);
            return returnDataList(result);
        } catch (Exception e){
            logger.error(QUERY_WORKFLOW_LINEAGE_ERROR.getMsg(),e);
            return error(QUERY_WORKFLOW_LINEAGE_ERROR.getCode(), QUERY_WORKFLOW_LINEAGE_ERROR.getMsg());
        }
    }
}
