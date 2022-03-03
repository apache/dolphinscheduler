package org.apache.dolphinscheduler.plugin.task.api.k8s;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_KILL;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_SUCCESS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.RUNNING_CODE;

import org.apache.dolphinscheduler.plugin.task.api.K8sTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.utils.K8sUtils;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobStatus;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;


public abstract class AbstractK8sTaskExecutor {
    protected Logger logger;
    protected TaskExecutionContext taskRequest;
    protected K8sUtils k8sUtils;
    protected Job job;
    protected StringBuffer logStringBuffer;

    public AbstractK8sTaskExecutor(Logger logger, TaskExecutionContext taskRequest) {
        this.logger = logger;
        this.taskRequest = taskRequest;
        this.k8sUtils = new K8sUtils();
        this.logStringBuffer = new StringBuffer();
    }

    public TaskResponse run(String k8sParameterStr) throws Exception {
        TaskResponse result = new TaskResponse();
        int taskInstanceId = taskRequest.getTaskInstanceId();
        K8sTaskMainParameters k8STaskMainParameters = JSONUtils.parseObject(k8sParameterStr, K8sTaskMainParameters.class);
        try {
            if (null == TaskExecutionContextCacheManager.getByTaskInstanceId(taskInstanceId)) {
                result.setExitStatusCode(EXIT_CODE_KILL);
                return result;
            }
            if (StringUtils.isEmpty(k8sParameterStr)) {
                TaskExecutionContextCacheManager.removeByTaskInstanceId(taskInstanceId);
                return result;
            }
            K8sTaskExecutionContext k8sTaskExecutionContext = taskRequest.getK8sTaskExecutionContext();
            String configYaml = k8sTaskExecutionContext.getConfigYaml();
            k8sUtils.buildClient(configYaml);
            submitJob2k8s(taskRequest, k8STaskMainParameters);
            registerBatchJobWatcher(job, Integer.toString(taskInstanceId), result, k8STaskMainParameters);
        } catch (Exception e) {
            result.setExitStatusCode(EXIT_CODE_FAILURE);
            throw e;
        }
        return result;
    }

    public void cancelApplication() {
        K8sTaskMainParameters k8STaskMainParameters = JSONUtils.parseObject(taskRequest.getTaskParams(), K8sTaskMainParameters.class);
        if (job != null) {
            stopJobOnK8s(job.getMetadata().getName(), k8STaskMainParameters);
        }
    }

    public void registerBatchJobWatcher(Job job, String taskInstanceId, TaskResponse taskResponse, K8sTaskMainParameters k8STaskMainParameters) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Watcher watcher = new Watcher<Job>() {
            @Override
            public void eventReceived(Action action, Job job) {
                if (action != Action.ADDED) {
                    int jobStatus = getK8sJobStatus(job);
                    if (jobStatus == EXIT_CODE_SUCCESS || jobStatus == EXIT_CODE_FAILURE) {
                        if (null == TaskExecutionContextCacheManager.getByTaskInstanceId(Integer.valueOf(taskInstanceId))) {
                            logStringBuffer.append(String.format("[K8sJobExecutor-%s] killed", job.getMetadata().getName()));
                            taskResponse.setExitStatusCode(EXIT_CODE_KILL);
                        } else if (jobStatus == EXIT_CODE_SUCCESS) {
                            logStringBuffer.append(String.format("[K8sJobExecutor-%s] succeed in k8s", job.getMetadata().getName()));
                            taskResponse.setExitStatusCode(EXIT_CODE_SUCCESS);
                        } else {
                            String errorMessage = k8sUtils.getPodLog(job.getMetadata().getName(), k8STaskMainParameters.getNamespace());
                            logStringBuffer.append(String.format("[K8sJobExecutor-%s] fail in k8s: %s", job.getMetadata().getName(), errorMessage));
                            taskResponse.setExitStatusCode(EXIT_CODE_FAILURE);
                        }
                        countDownLatch.countDown();
                    }
                }
            }

            @Override
            public void onClose(WatcherException e) {
                logStringBuffer.append(String.format("[K8sJobExecutor-%s] fail in k8s: %s", job.getMetadata().getName(), e.getMessage()));
                taskResponse.setExitStatusCode(EXIT_CODE_FAILURE);
                countDownLatch.countDown();
            }

            @Override
            public void onClose() {
                logger.warn("Watch gracefully closed");
            }
        };
        Watch watch = null;
        try {
            watch = k8sUtils.createBatchJobWatcher(job.getMetadata().getName(), watcher);
            boolean timeoutFlag = taskRequest.getTaskTimeoutStrategy() == TaskTimeoutStrategy.FAILED
                    || taskRequest.getTaskTimeoutStrategy() == TaskTimeoutStrategy.WARNFAILED;
            if (timeoutFlag) {
                Boolean timeout = !(countDownLatch.await(taskRequest.getTaskTimeout(), TimeUnit.SECONDS));
                waitTimeout(timeout);
            } else {
                countDownLatch.await();
            }
            flushLog(taskResponse);
        } catch (Exception e) {
            logger.error("job failed in k8s: {}", e.getMessage(), e);
            taskResponse.setExitStatusCode(EXIT_CODE_FAILURE);
        } finally {
            if (watch != null) {
                watch.close();
            }
        }
    }

    private void waitTimeout(Boolean timeout) throws TaskException {
        if (timeout) {
            throw new TaskException("K8sTask is timeout");
        }
        return;
    }

    private void flushLog(TaskResponse taskResponse) {
        if (logStringBuffer.length() != 0 && taskResponse.getExitStatusCode() == EXIT_CODE_FAILURE) {
            logger.error(logStringBuffer.toString());
        } else if (logStringBuffer.length() != 0) {
            logger.info(logStringBuffer.toString());
        }
    }

    private int getK8sJobStatus(Job job) {
        JobStatus jobStatus = job.getStatus();
        if (jobStatus.getSucceeded() != null && jobStatus.getSucceeded() == 1) {
            return EXIT_CODE_SUCCESS;
        } else if (jobStatus.getFailed() != null && jobStatus.getFailed() == 1) {
            return EXIT_CODE_FAILURE;
        } else {
            return RUNNING_CODE;
        }
    }

    public void submitJob2k8s(TaskExecutionContext taskRequest, K8sTaskMainParameters k8STaskMainParameters) {
        int taskInstanceId = taskRequest.getTaskInstanceId();
        String taskName = taskRequest.getTaskName().toLowerCase(Locale.ROOT);
        try {
            logger.info("[K8sJobExecutor-{}-{}] start to submit job", taskName, taskInstanceId);
            job = buildK8sJob(taskRequest, k8STaskMainParameters);
            stopJobOnK8s(job.getMetadata().getName(), k8STaskMainParameters);
            String namespace = k8STaskMainParameters.getNamespace();
            k8sUtils.createJob(namespace, job);
            logger.info("[K8sJobExecutor-{}-{}]  submitted job successfully", taskName, taskInstanceId);
        } catch (Exception e) {
            logger.error("[K8sJobExecutor-{}-{}]  fail to submit job", taskName, taskInstanceId);
            throw new TaskException("K8sJobExecutor fail to submit job", e);
        }
    }

    public abstract Job buildK8sJob(TaskExecutionContext taskRequest, K8sTaskMainParameters k8STaskMainParameters);

    public void stopJobOnK8s(String jobName, K8sTaskMainParameters k8STaskMainParameters) {
        String namespace = k8STaskMainParameters.getNamespace();
        String clusterName = k8STaskMainParameters.getClusterName();
        try {
            if (k8sUtils.jobExist(jobName, namespace, clusterName)) {
                k8sUtils.deleteJob(jobName, namespace, clusterName);
            }
        } catch (Exception e) {
            logger.error("[K8sJobExecutor-{}]  fail to stop job", jobName);
            throw new TaskException("K8sJobExecutor fail to stop job", e);
        }
    }
}
