# Introduction

Apache DolphinScheduler has export some metrics to monitor the system. We use micrometer for the exporter facade, and
the default exporter is prometheus, more exporter is coming soon.

## Quick Start

You can add the following config in master/worker/alert/api's yaml file to open the metrics exporter.

```yaml
metrics:
  enabled: true
```

Once you open the metrics exporter, you can access the metrics by the url: `http://ip:port/actuator/prometheus`

The exporter port is the `server.port` defined in application.yaml, e.g: master: `server.port: 5679`, worker: `server.port: 1235`, alert: `server.port: 50053`, api: `server.port: 12345`.

For example, you can get the master metrics by `curl http://localhost:5679/actuator/prometheus`

We have prepared the out-of-the-box Grafana configuration for you, you can find the Grafana dashboard
at `dolphinscheduler-meter/resources/grafana`, you can directly import these dashboards to grafana.

If you want to try at docker, you can use the following command to start the prometheus with grafana:

```shell
cd dolphinscheduler-meter/src/main/resources/grafana-demo
docker compose up
```

Then you can access the grafana by the url: `http://localhost/3001`

## Master Metrics

Master metrics are exported by the DolphinScheduler master server.

### System Metrics

* dolphinscheduler_master_overload_count: Indicates the number of times the master has been overloaded.
* dolphinscheduler_master_consume_command_count: Indicates the number of commands has consumed.

### Process Metrics

* dolphinscheduler_process_instance_submit_count: Indicates the number of process has been submitted.
* dolphinscheduler_process_instance_running_gauge: Indicates the number of process are running now.
* dolphinscheduler_process_instance_timeout_count: Indicates the number of process has been timeout.
* dolphinscheduler_process_instance_finish_count: Indicates the number of process has been finished, include success or
  failure.
* dolphinscheduler_process_instance_success_count: Indicates the number of process has been successful.
* dolphinscheduler_process_instance_stop_count: Indicates the number of process has been stopped.
* dolphinscheduler_process_instance_failover_count: Indicates the number of process has been failed over.

### Task Metrics

* dolphinscheduler_task_timeout_count: Indicates the number of tasks has been timeout.
* dolphinscheduler_task_finish_count: Indicates the number of tasks has been finished, include success or failure.
* dolphinscheduler_task_success_count: Indicates the number of tasks has been successful.
* dolphinscheduler_task_timeout_count: Indicates the number of tasks has been timeout.
* dolphinscheduler_task_retry_count: Indicates the number of tasks has been retry.
* dolphinscheduler_task_failover_count: Indicates the number of tasks has been failover.

## Worker Metrics

Worker metrics are exported by the DolphinScheduler worker server.

### System Metrics

* dolphinscheduler_worker_overload_count: Indicates the number of times the worker has been overloaded.
* dolphinscheduler_worker_submit_queue_is_full_count: Indicates the number of times the worker's submit queue has been
  full.

### Task Metrics

* dolphinscheduler_task_execute_count: Indicates the number of times a task has been executed, it contains a tag -
  `task_type`.
* dolphinscheduler_task_execution_count: Indicates the total number of task has been executed.
* dolphinscheduler_task_execution_timer: Indicates the time spent executing tasks.

## Default System Metrics

In each server, there are some default metrics related to the system instance.

### Database Metrics

* hikaricp_connections_creation_seconds_max: Connection creation time max.
* hikaricp_connections_creation_seconds_count: Connection creation time count.
* hikaricp_connections_creation_seconds_sum: Connection creation time sum.
* hikaricp_connections_acquire_seconds_max: Connection acquire time max.
* hikaricp_connections_acquire_seconds_count: Connection acquire time count.
* hikaricp_connections_acquire_seconds_sum: Connection acquire time sum.
* hikaricp_connections_usage_seconds_max: Connection usage max.
* hikaricp_connections_usage_seconds_count: Connection usage time count.
* hikaricp_connections_usage_seconds_sum: Connection usage time sum.
* hikaricp_connections_max: Max connections.
* hikaricp_connections_min Min connections
* hikaricp_connections_active: Active connections.
* hikaricp_connections_idle: Idle connections.
* hikaricp_connections_pending: Pending connections.
* hikaricp_connections_timeout_total: Timeout connections.
* hikaricp_connections: Total connections
* jdbc_connections_max: Maximum number of active connections that can be allocated at the same time.
* jdbc_connections_min: Minimum number of idle connections in the pool.
* jdbc_connections_idle: Number of established but idle connections.
* jdbc_connections_active: Current number of active connections that have been allocated from the data source.

### JVM Metrics

* jvm_buffer_total_capacity_bytes: An estimate of the total capacity of the buffers in this pool.
* jvm_buffer_count_buffers: An estimate of the number of buffers in the pool.
* jvm_buffer_memory_used_bytes: An estimate of the memory that the Java virtual machine is using for this buffer pool.
* jvm_memory_committed_bytes: The amount of memory in bytes that is committed for the Java virtual machine to use.
* jvm_memory_max_bytes: The maximum amount of memory in bytes that can be used for memory management.
* jvm_memory_used_bytes: The amount of used memory.
* jvm_threads_peak_threads: The peak live thread count since the Java virtual machine started or peak was reset.
* jvm_threads_states_threads: The current number of threads having NEW state.
* jvm_gc_memory_allocated_bytes_total: Incremented for an increase in the size of the (young) heap memory pool after one GC to before the next.
* jvm_gc_max_data_size_bytes: Max size of long-lived heap memory pool.
* jvm_gc_pause_seconds_count: Time spent count in GC pause.
* jvm_gc_pause_seconds_sum: Time spent sum in GC pause.
* jvm_gc_pause_seconds_max: Time spent max in GC pause.
* jvm_gc_live_data_size_bytes: Size of long-lived heap memory pool after reclamation.
* jvm_gc_memory_promoted_bytes_total: Count of positive increases in the size of the old generation memory pool before GC to after GC.
* jvm_classes_loaded_classes: The number of classes that are currently loaded in the Java virtual machine.
* jvm_threads_live_threads: The current number of live threads including both daemon and non-daemon threads.
* jvm_threads_daemon_threads: The current number of live daemon threads.
* jvm_classes_unloaded_classes_total: The total number of classes unloaded since the Java virtual machine has started execution.
* process_cpu_usage: The "recent cpu usage" for the Java Virtual Machine process.
* process_start_time_seconds: Start time of the process since unix epoch.
* process_uptime_seconds: The uptime of the Java virtual machine.


## Other Metrics
* jetty_threads_config_max: The maximum number of threads in the pool.
* jetty_threads_config_min: The minimum number of threads in the pool.
* jetty_threads_current: The total number of threads in the pool.
* jetty_threads_idle: The number of idle threads in the pool.
* jetty_threads_busy: The number of busy threads in the pool.
* jetty_threads_jobs: Number of jobs queued waiting for a thread.
* process_files_max_files: The maximum file descriptor count.
* process_files_open_files: The open file descriptor count.
* system_cpu_usage: The "recent cpu usage" for the whole system.
* system_cpu_count: The number of processors available to the Java virtual machine.
* system_load_average_1m: The sum of the number of runnable entities queued to available processors and the number of runnable entities running on the available processors averaged over a period of time.
* logback_events_total: Number of level events that made it to the logs