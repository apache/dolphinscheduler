package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.WorkFlowLineage;
import org.apache.dolphinscheduler.dao.entity.WorkFlowRelation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class WorkFlowLineageServiceTest {

    @InjectMocks
    WorkFlowLineageService workFlowLineageService;

    @Test
    public void testQueryWorkFlowLineageByName() {
        String searchVal = "test";
        Map<String, Object> result = workFlowLineageService.queryWorkFlowLineageByName(searchVal,1);
        List<WorkFlowLineage> workFlowLineageList = (List<WorkFlowLineage>)result.get(Constants.DATA_LIST);
        Assert.assertTrue(workFlowLineageList.size()>0);
    }

    @Test
    public void testQueryWorkFlowLineageByIds() {

        Set<Integer> ids = new HashSet<>();
        ids.add(1);
        ids.add(2);
        Map<String, Object> result = workFlowLineageService.queryWorkFlowLineageByIds(ids,1);
        Map<String, Object> workFlowLists = (Map<String, Object>)result.get(Constants.DATA_LIST);
        List<WorkFlowLineage> workFlowLineages = (List<WorkFlowLineage>)workFlowLists.get("workFlowList");
        List<WorkFlowRelation> workFlowRelations = (List<WorkFlowRelation>)workFlowLists.get("workFlowRelationList");
        Assert.assertTrue(workFlowLineages.size()>0);
        Assert.assertTrue(workFlowRelations.size()>0);
    }

}
