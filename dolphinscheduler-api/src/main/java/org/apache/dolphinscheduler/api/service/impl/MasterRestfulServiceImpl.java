package org.apache.dolphinscheduler.api.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.api.configuration.RpcConfiguration;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.MasterRestfulService;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.OkHttpUtils;
import org.apache.dolphinscheduler.remote.dto.MasterTaskInstanceDispatchingDto;
import org.apache.dolphinscheduler.remote.dto.MasterWorkflowInstanceExecutingListingDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static org.apache.dolphinscheduler.api.enums.Status.LISTING_DISPATCHING_TASK_INSTANCES_BY_MASTER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.LISTING_EXECUTING_WORKFLOWS_BY_MASTER_ERROR;

@Slf4j
@Service
public class MasterRestfulServiceImpl implements MasterRestfulService {

    @Autowired
    private RpcConfiguration rpcConfiguration;

    private static final String LISTING_EXECUTING_WORKFLOWS_BY_MASTER_ADDRESS_URI =
            "/workflow/listingExecutingWorkflows";
    private static final String LISTING_DISPATCHING_TASKINSTANCES_BY_MASTER_ADDRESS_URI =
            "/task/listingDispatchingTaskInstances";

    @Override
    public List<MasterWorkflowInstanceExecutingListingDto> listingExecutingWorkflowsByMasterAddress(String masterAddress) {
        if (StringUtils.isBlank(masterAddress)) {
            return Collections.emptyList();
        }
        String url = getUrl(masterAddress, LISTING_EXECUTING_WORKFLOWS_BY_MASTER_ADDRESS_URI);
        try {
            // todo: add sdk
            String response = OkHttpUtils.get(url);
            if (StringUtils.isEmpty(response)) {
                log.error(
                        "Query executing workflows by master address error, the response is empty, url: {}, masterAddress: {}",
                        url, masterAddress);
                throw new ServiceException(LISTING_EXECUTING_WORKFLOWS_BY_MASTER_ERROR);
            }
            return JSONUtils.parseObject(response,
                    new TypeReference<List<MasterWorkflowInstanceExecutingListingDto>>() {
                    });
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Query executing workflows by master address error, url: {}, masterAddress: {}", url,
                    masterAddress, ex);
            throw new ServiceException(LISTING_EXECUTING_WORKFLOWS_BY_MASTER_ERROR);
        }
    }

    @Override
    public List<MasterTaskInstanceDispatchingDto> listingDispatchingTaskInstanceByMasterAddress(String masterAddress) {
        if (StringUtils.isBlank(masterAddress)) {
            return Collections.emptyList();
        }
        String url = getUrl(masterAddress, LISTING_DISPATCHING_TASKINSTANCES_BY_MASTER_ADDRESS_URI);
        try {
            String response = OkHttpUtils.get(url);
            if (StringUtils.isEmpty(response)) {
                log.error(
                        "Query executing task instances by master address error, the response is empty, url: {}, masterAddress: {}",
                        url, masterAddress);
                throw new ServiceException(LISTING_DISPATCHING_TASK_INSTANCES_BY_MASTER_ERROR);
            }
            return JSONUtils.parseObject(response, new TypeReference<List<MasterTaskInstanceDispatchingDto>>() {
            });
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Query executing task instances by master address error, url: {}, masterAddress: {}", url,
                    masterAddress, ex);
            throw new ServiceException(LISTING_DISPATCHING_TASK_INSTANCES_BY_MASTER_ERROR);
        }
    }

    private String getUrl(String masterAddress, String uri) {
        return rpcConfiguration.getRpcPrefix() + masterAddress + rpcConfiguration.getMasterUrlPrefix() + uri;
    }
}
