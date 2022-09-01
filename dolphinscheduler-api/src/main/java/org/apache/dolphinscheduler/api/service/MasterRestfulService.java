package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.remote.dto.MasterTaskInstanceDispatchingDto;
import org.apache.dolphinscheduler.remote.dto.MasterWorkflowInstanceExecutingListingDto;

import java.util.List;

public interface MasterRestfulService {
    List<MasterWorkflowInstanceExecutingListingDto> listingExecutingWorkflowsByMasterAddress(String masterAddress);

    List<MasterTaskInstanceDispatchingDto> listingDispatchingTaskInstanceByMasterAddress(String masterAddress);
}
