package org.apache.dolphinscheduler.extract.master;

import org.apache.dolphinscheduler.extract.base.RpcMethod;
import org.apache.dolphinscheduler.extract.base.RpcService;
import org.apache.dolphinscheduler.extract.master.transportor.UpdateMasterHostResponse;

@RpcService
public interface IWorkerNodeService {

    @RpcMethod
    UpdateMasterHostResponse changeWorkflowInstanceHost(Integer processId, String newHost);

}
