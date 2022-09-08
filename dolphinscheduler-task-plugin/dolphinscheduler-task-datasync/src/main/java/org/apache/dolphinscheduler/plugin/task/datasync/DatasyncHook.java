package org.apache.dolphinscheduler.plugin.task.datasync;

import lombok.Data;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.datasync.DataSyncClient;
import software.amazon.awssdk.services.datasync.model.CancelTaskExecutionRequest;
import software.amazon.awssdk.services.datasync.model.CancelTaskExecutionResponse;
import software.amazon.awssdk.services.datasync.model.CreateTaskRequest;
import software.amazon.awssdk.services.datasync.model.CreateTaskResponse;
import software.amazon.awssdk.services.datasync.model.DescribeTaskExecutionRequest;
import software.amazon.awssdk.services.datasync.model.DescribeTaskExecutionResponse;
import software.amazon.awssdk.services.datasync.model.DescribeTaskRequest;
import software.amazon.awssdk.services.datasync.model.DescribeTaskResponse;
import software.amazon.awssdk.services.datasync.model.FilterRule;
import software.amazon.awssdk.services.datasync.model.StartTaskExecutionRequest;
import software.amazon.awssdk.services.datasync.model.StartTaskExecutionResponse;
import software.amazon.awssdk.services.datasync.model.TagListEntry;
import software.amazon.awssdk.services.datasync.model.TaskExecutionStatus;
import software.amazon.awssdk.services.datasync.model.TaskStatus;

import java.util.Arrays;
import java.util.List;

@Data
public class DatasyncHook {
    public static TaskExecutionStatus[] doneStatus = {TaskExecutionStatus.ERROR, TaskExecutionStatus.SUCCESS, TaskExecutionStatus.UNKNOWN_TO_SDK_VERSION};
    public static TaskStatus[] taskFinishFlags = {TaskStatus.UNAVAILABLE, TaskStatus.UNKNOWN_TO_SDK_VERSION};
    protected final Logger logger = LoggerFactory.getLogger(String.format(TaskConstants.TASK_LOG_LOGGER_NAME_FORMAT, getClass()));
    private DataSyncClient client;
    private String taskArn;
    private String taskExecArn;

    public DatasyncHook() {
        client = createClient();
    }

    protected DataSyncClient createClient() {
        final String awsAccessKeyId = PropertyUtils.getString(TaskConstants.AWS_ACCESS_KEY_ID);
        final String awsSecretAccessKey = PropertyUtils.getString(TaskConstants.AWS_SECRET_ACCESS_KEY);
        final String awsRegion = PropertyUtils.getString(TaskConstants.AWS_REGION);

        final AwsBasicCredentials basicAWSCredentials = AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey);
        final AwsCredentialsProvider awsCredentialsProvider = StaticCredentialsProvider.create(basicAWSCredentials);

        logger.info(awsAccessKeyId);
        logger.info(awsSecretAccessKey);
        logger.info(awsRegion);
        // create a datasync client
        return DataSyncClient.builder().region(Region.of(awsRegion)).credentialsProvider(awsCredentialsProvider).build();
    }

    public Boolean createDatasyncTask(DatasyncParameters parameters) {
        logger.info("createDatasyncTask ......");
        CreateTaskRequest.Builder builder = CreateTaskRequest.builder()
                .name(parameters.getName())
                .sourceLocationArn(parameters.getSourceLocationArn())
                .destinationLocationArn(parameters.getDestinationLocationArn());

        String cloudWatchLogGroupArn = parameters.getCloudWatchLogGroupArn();
        if (StringUtils.isNotEmpty(cloudWatchLogGroupArn)) {
            builder.cloudWatchLogGroupArn(cloudWatchLogGroupArn);
        }
        List<TagListEntry> tag = parameters.getTags();
        if (tag != null && tag.size() > 0) {
            builder.tags(tag);
        }
        if (parameters.getOptions() != null) {
            builder.options(parameters.getOptions());
        }
        List<FilterRule> excludes = parameters.getExcludes();
        if (excludes != null && excludes.size() > 0) {
            builder.excludes(excludes);
        }
        List<FilterRule> includes = parameters.getIncludes();
        if (includes != null && includes.size() > 0) {
            builder.includes(includes);
        }
        if (parameters.getSchedule() != null) {
            builder.schedule(parameters.getSchedule());
        }

        CreateTaskResponse task = client.createTask(builder.build());
        if (task.sdkHttpResponse().isSuccessful()) {
            taskArn = task.taskArn();
        }
        logger.info("finished createDatasyncTask ......");
        return doubleCheckTaskStatus(TaskStatus.AVAILABLE, taskFinishFlags);
    }

    public Boolean startDatasyncTask() {
        logger.info("startDatasyncTask ......");
        StartTaskExecutionRequest start = StartTaskExecutionRequest.builder().taskArn(taskArn).build();
        StartTaskExecutionResponse response = client.startTaskExecution(start);
        if (response.sdkHttpResponse().isSuccessful()) {
            taskExecArn = response.taskExecutionArn();
        }
        return doubleCheckExecStatus(TaskExecutionStatus.LAUNCHING, doneStatus);
    }


    public Boolean cancelDatasyncTask() {
        logger.info("cancelTask ......");
        CancelTaskExecutionRequest cancel = CancelTaskExecutionRequest.builder().taskExecutionArn(taskExecArn).build();
        CancelTaskExecutionResponse response = client.cancelTaskExecution(cancel);
        if (response.sdkHttpResponse().isSuccessful()) {
            return true;
        }
        return false;
    }

    public TaskStatus queryDatasyncTaskStatus() {
        logger.info("queryDatasyncTaskStatus ......");

        DescribeTaskRequest request = DescribeTaskRequest.builder().taskArn(taskArn).build();
        DescribeTaskResponse describe = client.describeTask(request);

        if (describe.sdkHttpResponse().isSuccessful()) {
            logger.info("queryDatasyncTaskStatus ......{}", describe.statusAsString());
            return describe.status();
        }
        return null;
    }

    public TaskExecutionStatus queryDatasyncTaskExecStatus() {
        logger.info("queryDatasyncTaskExecStatus ......");
        DescribeTaskExecutionRequest request = DescribeTaskExecutionRequest.builder().taskExecutionArn(taskExecArn).build();
        DescribeTaskExecutionResponse describe = client.describeTaskExecution(request);

        if (describe.sdkHttpResponse().isSuccessful()) {
            logger.info("queryDatasyncTaskExecStatus ......{}", describe.statusAsString());
            return describe.status();
        }
        return null;
    }

    public Boolean doubleCheckTaskStatus(TaskStatus exceptStatus, TaskStatus[] stopStatus) {

        List<TaskStatus> stopStatusSet = Arrays.asList(stopStatus);
        int maxRetry = 5;
        while (maxRetry > 0) {
            TaskStatus status = queryDatasyncTaskStatus();

            if (status == null) {
                maxRetry--;
                continue;
            }

            if (exceptStatus.equals(status)) {
                logger.info("double check success");
                return true;
            } else if (stopStatusSet.contains(status)) {
                break;
            }
        }
        logger.warn("double check error");
        return false;
    }

    public Boolean doubleCheckExecStatus(TaskExecutionStatus exceptStatus, TaskExecutionStatus[] stopStatus) {

        List<TaskExecutionStatus> stopStatusSet = Arrays.asList(stopStatus);
        int maxRetry = 5;
        while (maxRetry > 0) {
            TaskExecutionStatus status = queryDatasyncTaskExecStatus();

            if (status == null) {
                maxRetry--;
                continue;
            }

            if (exceptStatus.equals(status)) {
                logger.info("double check success");
                return true;
            } else if (stopStatusSet.contains(status)) {
                break;
            }
        }
        logger.warn("double check error");
        return false;
    }

    public Boolean doubleCheckFinishStatus(TaskExecutionStatus exceptStatus, TaskExecutionStatus[] stopStatus) {

        List<TaskExecutionStatus> stopStatusSet = Arrays.asList(stopStatus);
        while (true) {
            TaskExecutionStatus status = queryDatasyncTaskExecStatus();

            if (status == null) {
                continue;
            }

            if (exceptStatus.equals(status)) {
                logger.info("double check success");
                return true;
            } else if (stopStatusSet.contains(status)) {
                break;
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        logger.warn("double check error");
        return false;
    }
}