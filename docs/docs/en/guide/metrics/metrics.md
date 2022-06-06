# Introduction

Apache DolphinScheduler has export some metrics to monitor the system. We use micrometer for the exporter facade, and
the default exporter is prometheus, more exporter is coming soon.

## Quick Start

You can add the following config in master/worker's yaml file to open the metrics exporter.

```yaml
metrics:
  enabled: true
```

Once you open the metrics exporter, you can access the metrics by the url: `http://ip:port/actuator/prometheus`
For example, you can get the master metrics by `curl http://localhost:5679/actuator/prometheus`

We have prepared the out-of-the-box grafana for you, you can find the grafana dashboard
at `dolphinscheduler-meter/resources/grafana`, you can directly import these dashboards to grafana.

If you want to try at docker, you can use the following command to start the prometheus with grafana:

```shell
cd dolphinscheduler-meter/src/main/resources/grafana-demo
docker compose up
```

Then you can access the grafana by the url: `http://localhost/3001`

## Master Metrics

Master metrics are exported by the DolphinScheduler master server.

### System metrics

* dolphinscheduler_master_overload_count: Indicates the number of times the master has been overloaded.
* dolphinscheduler_master_consume_command_count: Indicates the number of commands has consumed.

### Process metrics

* dolphinscheduler_process_instance_submit_count: Indicates the number of process has been submitted.
* dolphinscheduler_process_instance_running_gauge: Indicates the number of process are running now.
* dolphinscheduler_process_instance_timeout_count: Indicates the number of process has been timeout.
* dolphinscheduler_process_instance_finish_count: Indicates the number of process has been finished, include success or
  failure.
* dolphinscheduler_process_instance_success_count: Indicates the number of process has been successful.
* dolphinscheduler_process_instance_stop_count: Indicates the number of process has been stopped.

### Task metrics

* dolphinscheduler_task_timeout_count: Indicates the number of tasks has been timeout.
* dolphinscheduler_task_finish_count: Indicates the number of tasks has been finished, include success or failure.
* dolphinscheduler_task_success_count: Indicates the number of tasks has been successful.
* dolphinscheduler_task_timeout_count: Indicates the number of tasks has been timeout.
* dolphinscheduler_task_retry_count: Indicates the number of tasks has been retry.

## Worker Metrics

Worker metrics are exported by the DolphinScheduler worker server.

### System metrics

* dolphinscheduler_worker_overload_count: Indicates the number of times the worker has been overloaded.
* dolphinscheduler_worker_submit_queue_is_full_count: Indicates the number of times the worker's submit queue has been
  full.

### Task metrics

* dolphinscheduler_task_execute_count: Indicates the number of times a task has been executed, it contains a tag -
  `task_type`.
* dolphinscheduler_task_execution_count: Indicates the total number of task has been executed.
* dolphinscheduler_task_execution_timer: Indicates the time spent executing tasks.