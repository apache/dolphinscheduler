package org.apache.dolphinscheduler.api.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.api.configuration.RpcConfiguration;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.WorkerRestfulService;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.utils.OkHttpUtils;
import org.apache.dolphinscheduler.remote.dto.WorkerTaskInstanceWaitingDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static org.apache.dolphinscheduler.api.enums.Status.LISTING_EXECUTING_TASK_EXECUTION_CONTEXT_BY_WORKER_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.LISTING_WAITING_TASK_INSTANCE_BY_WORKER_ERROR;

@Slf4j
@Service
public class WorkerRestfulServiceImpl implements WorkerRestfulService {

    @Autowired
    private RpcConfiguration rpcConfiguration;
    private static final String LISTING_EXECUTING_TASK_EXECUTION_CONTEXT = "/task/listingExecutionContext";
    private static final String LISTING_WAITING_TASK = "/task/listingWaitingTask";

    @Override
    public List<TaskExecutionContext> listingExecutingTaskExecutionContext(String workerAddress) {
        if (StringUtils.isBlank(workerAddress)) {
            return Collections.emptyList();
        }
        String url = getUrl(workerAddress, LISTING_EXECUTING_TASK_EXECUTION_CONTEXT);
        try {
            String response = OkHttpUtils.get(getUrl(workerAddress, LISTING_EXECUTING_TASK_EXECUTION_CONTEXT));
            if (StringUtils.isEmpty(response)) {
                log.error(
                        "Query TaskExecutionContext by worker address error, the response is empty, url: {}, workerAddress: {}",
                        url, workerAddress);
                throw new ServiceException(LISTING_EXECUTING_TASK_EXECUTION_CONTEXT_BY_WORKER_ERROR);
            }
            return JSONUtils.parseObject(response, new TypeReference<List<TaskExecutionContext>>() {
            });
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Query TaskExecutionContext by worker address error, url: {}, workerAddress: {}", url,
                    workerAddress, ex);
            throw new ServiceException(LISTING_EXECUTING_TASK_EXECUTION_CONTEXT_BY_WORKER_ERROR);
        }
    }

    @Override
    public List<WorkerTaskInstanceWaitingDto> listingWaitingTask(String workerAddress) {
        if (StringUtils.isBlank(workerAddress)) {
            return Collections.emptyList();
        }
        String url = getUrl(workerAddress, LISTING_WAITING_TASK);
        try {
            String response = OkHttpUtils.get(url);
            if (StringUtils.isEmpty(response)) {
                log.error(
                        "Query TaskExecutionContext by worker address error the response data from worker is empty, url: {}, workerAddress: {}",
                        url, workerAddress);
                throw new ServiceException(LISTING_WAITING_TASK_INSTANCE_BY_WORKER_ERROR);
            }
            return JSONUtils.parseObject(response, new TypeReference<List<WorkerTaskInstanceWaitingDto>>() {
            });
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error(
                    "Query TaskExecutionContext by worker address error, get an unknown exception, url: {}, workerAddress: {}",
                    url, workerAddress, ex);
            throw new ServiceException(LISTING_WAITING_TASK_INSTANCE_BY_WORKER_ERROR);
        }
    }

    private String getUrl(String workerAddress, String uri) {
        return rpcConfiguration.getRpcPrefix() + workerAddress + rpcConfiguration.getWorkerUrlPrefix() + uri;
    }
}
