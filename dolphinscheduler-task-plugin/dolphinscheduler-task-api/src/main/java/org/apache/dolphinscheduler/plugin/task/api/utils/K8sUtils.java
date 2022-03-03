package org.apache.dolphinscheduler.plugin.task.api.utils;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.LOG_LINES;

import org.apache.dolphinscheduler.plugin.task.api.TaskException;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;

public class K8sUtils {
    private static final Logger log = LoggerFactory.getLogger(K8sUtils.class);
    private KubernetesClient client;

    public void createJob(String namespace, Job job) {
        try {
            client.batch().v1()
                .jobs()
                .inNamespace(namespace)
                .create(job);
        } catch (Exception e) {
            log.error("fail to create job: ", e);
            throw new TaskException("fail to create job");
        }
    }

    public void deleteJob(String jobName, String namespace, String k8s) {
        try {
            client.batch().v1()
                .jobs()
                .inNamespace(namespace)
                .withName(jobName)
                .delete();
        } catch (Exception e) {
            log.error("fail to delete job: ", e);
            throw new TaskException("fail to delete job");
        }
    }

    public Boolean jobExist(String jobName, String namespace, String k8s) {
        Optional<Job> result;
        try {
            JobList jobList = client.batch().v1().jobs().inNamespace(namespace).list();
            List<Job> jobs = jobList.getItems();
            result = jobs.stream()
                .filter(job -> job.getMetadata().getName().equals(jobName))
                .findFirst();
            return result.isPresent();
        } catch (Exception e) {
            log.error("fail to check job: ", e);
            throw new TaskException("fail to check job: ", e);
        }
    }

    public Watch createBatchJobWatcher(String jobName, Watcher watcher) {
        try {
            return client.batch().v1()
                .jobs().withName(jobName).watch(watcher);
        } catch (Exception e) {
            log.error("fail to register batch job watcher: ", e);
            throw new TaskException("fail to register batch job watcher");
        }
    }

    public String getPodLog(String jobName, String namespace) {
        try {
            List<Pod> podList = client.pods().inNamespace(namespace).list().getItems();
            String podName = null;
            for (Pod pod : podList) {
                podName = pod.getMetadata().getName();
                if (jobName.equals(podName.substring(0, pod.getMetadata().getName().lastIndexOf("-")))) {
                    break;
                }
            }
            return client.pods().inNamespace(namespace)
                .withName(podName)
                .tailingLines(LOG_LINES)
                .getLog(Boolean.TRUE);
        } catch (Exception e) {
            log.error("fail to getPodLog", e);
            log.error("response bodies : {}", e.getMessage());
        }
        return null;
    }

    public void buildClient(String configYaml) {
        try {
            Config config = Config.fromKubeconfig(configYaml);
            client = new DefaultKubernetesClient(config);
        } catch (Exception e) {
            log.error("fail to build k8s ApiClient", e);
            throw new TaskException("fail to build k8s ApiClient");
        }
    }

}
