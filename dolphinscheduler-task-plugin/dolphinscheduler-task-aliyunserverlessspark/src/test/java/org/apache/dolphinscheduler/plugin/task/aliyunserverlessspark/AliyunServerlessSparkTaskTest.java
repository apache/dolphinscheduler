package org.apache.dolphinscheduler.plugin.task.aliyunserverlessspark;

import java.util.Collections;

import com.aliyun.emr_serverless_spark20230808.Client;
import com.aliyun.emr_serverless_spark20230808.models.StartJobRunResponse;
import com.aliyun.emr_serverless_spark20230808.models.Tag;
import com.aliyun.tea.TeaException;

public class AliyunServerlessSparkTaskTest {
    public static void main(String[] args) throws Exception {
        com.aliyun.emr_serverless_spark20230808.Client client = AliyunServerlessSparkTaskTest.createClient();
        com.aliyun.emr_serverless_spark20230808.models.StartJobRunRequest startJobRunRequest = new com.aliyun.emr_serverless_spark20230808.models.StartJobRunRequest();
        startJobRunRequest.setRegionId("cn-hangzhou");
        startJobRunRequest.setResourceQueueId("root_queue");
        startJobRunRequest.setCodeType("JAR");
        startJobRunRequest.setName("ds-test");
        startJobRunRequest.setReleaseVersion("esr-2.1-native (Spark 3.3.1, Scala 2.12, Native Runtime)");
        Tag tag = new Tag();
        tag.setKey("environment");
        tag.setValue("production");
        startJobRunRequest.setTags(Collections.singletonList(tag));
        com.aliyun.emr_serverless_spark20230808.models.JobDriver.JobDriverSparkSubmit jobDriverSparkSubmit = new com.aliyun.emr_serverless_spark20230808.models.JobDriver.JobDriverSparkSubmit()
            .setEntryPoint("oss://datadev-oss-hdfs-test/spark-resource/examples/jars/spark-examples_2.12-3.3.1.jar")
            .setEntryPointArguments(java.util.Arrays.asList(
                "1"
            ))
            .setSparkSubmitParameters("--class org.apache.spark.examples.SparkPi --conf spark.executor.cores=4 --conf spark.executor.memory=20g --conf spark.driver.cores=4 --conf spark.driver.memory=8g --conf spark.executor.instances=1");
        com.aliyun.emr_serverless_spark20230808.models.JobDriver jobDriver = new com.aliyun.emr_serverless_spark20230808.models.JobDriver()
            .setSparkSubmit(jobDriverSparkSubmit);
        startJobRunRequest.setJobDriver(jobDriver);
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        java.util.Map<String, String> headers = new java.util.HashMap<>();
        try {
            // 复制代码运行请自行打印 API 的返回值
            StartJobRunResponse startJobRunResponse = client.startJobRunWithOptions("w-f7b841e8c73211be", startJobRunRequest, headers, runtime);
            System.out.println(startJobRunResponse.getBody().getRequestId());
            System.out.println(startJobRunResponse.getBody().getJobRunId());
        } catch (TeaException error) {
            // 此处仅做打印展示，请谨慎对待异常处理，在工程项目中切勿直接忽略异常。
            // 错误 message
            System.out.println(error.getMessage());
            // 诊断地址
            System.out.println(error.getData().get("Recommend"));
            com.aliyun.teautil.Common.assertAsString(error.message);
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 此处仅做打印展示，请谨慎对待异常处理，在工程项目中切勿直接忽略异常。
            // 错误 message
            System.out.println(error.getMessage());
            // 诊断地址
            System.out.println(error.getData().get("Recommend"));
            com.aliyun.teautil.Common.assertAsString(error.message);
        }
    }

    public static Client createClient() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
            .setAccessKeyId(System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID"))
            .setAccessKeySecret(System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET"));
        config.endpoint = "emr-serverless-spark.cn-hangzhou.aliyuncs.com";
        return new com.aliyun.emr_serverless_spark20230808.Client(config);
    }
}
