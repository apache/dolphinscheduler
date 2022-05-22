# About DolphinScheduler

Apache DolphinScheduler is a distributed, easy to extend visual DAG workflow task scheduling open-source system. Solves the intricate dependencies of data R&D ETL and the inability to monitor the health status of tasks. DolphinScheduler assembles tasks in the DAG streaming way, which can monitor the execution status of tasks in time, and supports operations like retry, recovery failure from specified nodes, pause, resume and kill tasks, etc.

## Simple to Use

- DolphinScheduler has DAG monitoring user interfaces, users can customize DAG by dragging and dropping. All process definitions are visualized, supports rich third-party systems APIs and one-click deployment.

## High Reliability

- Decentralized multi-masters and multi-workers, support HA, select queues to avoid overload.

## Rich Scenarios

- Support features like multi-tenants, suspend and resume operations to cope with big data scenarios. Support many task types like Spark, Flink, Hive, MR, shell, python, sub_process.

## High Scalability

- Supports customized task types, distributed scheduling, and the overall scheduling capability increases linearly with the scale of the cluster.