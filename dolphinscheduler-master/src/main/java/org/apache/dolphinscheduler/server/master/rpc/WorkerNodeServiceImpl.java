package org.apache.dolphinscheduler.server.master.rpc;

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.extract.master.IWorkerNodeService;
import org.apache.dolphinscheduler.extract.master.transportor.UpdateMasterHostResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkerNodeServiceImpl implements IWorkerNodeService {

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    @Override
    public UpdateMasterHostResponse changeWorkflowInstanceHost(Integer processId, String newHost) {

        int x = 0;
        try {
            ProcessInstance processInstance = processInstanceMapper.queryDetailById(processId);
            x = processInstanceMapper.updateProcessInstanceByHost(processId, newHost);
            // taskInstanceMapper.updateTaskInstanceByHost(id, newHost);
            log.info("Host is change and id is {}.", processInstance.getId());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        if (x > 0) {
            return UpdateMasterHostResponse.success();
        }
        return UpdateMasterHostResponse.failed("Not change master host");

    }

}
