/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

export default {
  cluster: {
    disk: 'Disk',
    title: 'Cluster Overview',
    subtitle: 'Service topology and runtime status',
    normal: 'Normal',
    busy: 'Busy',
    unknown: 'Metadata unavailable',
    role_unknown: 'Role unavailable',
    group_unknown: 'Group unavailable',
    running_workflows: 'Running Workflows',
    running_tasks: 'Running Tasks',
    all_normal: 'All normal',
    service_instances: 'service instances',
    view: 'View',
    refresh: 'Refresh',
    topology: 'Cluster Topology',
    node_list: 'Node List',
    node: 'Node',
    role_group: 'Service / Group',
    status: 'Status',
    memory_usage: 'Memory Usage',
    details: 'Details',
    process_id: 'Process ID',
    startup_time: 'Startup Time',
    rpc_address: 'RPC Address',
    directory: 'Registry Directory',
    service_type: 'Service Type',
    ha_role: 'HA Role',
    worker_group: 'Worker Group',
    coordinator_role: 'Coordination Role',
    metadata_time: 'Metadata Time',
    unavailable: 'Unavailable',
    memory: 'Memory',
    thread_pool: 'Thread Pool',
    group_count: '{count} Worker groups',
    worker_count: '{count} Workers',
    busy_count: '{count} busy',
    unknown_count: '{count} unavailable',
    node_detail_label: '{name}, {status}, {role}, view details',
    group_label: '{name}, {count} Workers, {action} group',
    collapse: 'Collapse',
    expand: 'Expand',
    no_nodes: 'No registered service instances',
    unknown_note:
      'Unavailable metadata means collection failed; it does not imply the node is offline.',
    metadata_unavailable:
      'The node is still registered, but metadata collection failed. Resource metrics and execution load are unavailable.',
    topology_description:
      'Logical architecture: API, Master and Worker from top to bottom. Connections do not represent observed network traffic.',
    load_error:
      'Could not refresh {roles}. Any previously loaded nodes are retained with unavailable metrics.',
    refreshed_at: 'Last refreshed',
    no_permission: 'Administrator permission is required',
    workflow_name: 'Workflow Instance',
    workflow_state: 'State',
    start_time: 'Start Time',
    run_times: 'Run Times',
    task_name: 'Task Name',
    task_type: 'Task Type',
    query_failed: 'Running-instance query failed',
    query_failed_description:
      'Could not retrieve running instances from this node. Please retry.',
    retry: 'Retry'
  },
  metadata_unavailable: 'Metadata unavailable or stale',
  master: {
    cpu_usage: 'CPU Usage',
    memory_usage: 'Memory Usage',
    disk_usage: 'Disk Usage',
    create_time: 'Create Time',
    last_heartbeat_time: 'Last Heartbeat Time',
    directory_detail: 'Directory Detail',
    host: 'Host',
    directory: 'Directory',
    running_workflows: 'Running Workflows',
    workflow_name: 'Workflow Instance',
    workflow_state: 'State',
    workflow_start_time: 'Start Time',
    workflow_run_times: 'Run Times',
    running_tasks: 'Running Tasks',
    master_no_data_result_title: 'No Master Nodes Exist',
    master_no_data_result_desc:
      'Currently, there are no master nodes exist, please create a master node and refresh this page'
  },
  worker: {
    cpu_usage: 'CPU Usage',
    memory_usage: 'Memory Usage',
    disk_usage: 'Disk Usage',
    thread_pool_usage: 'Thread Pool Usage',
    create_time: 'Create Time',
    last_heartbeat_time: 'Last Heartbeat Time',
    directory_detail: 'Directory Detail',
    host: 'Host',
    directory: 'Directory',
    running_tasks: 'Running Tasks',
    task_name: 'Task Name',
    task_type: 'Task Type',
    task_workflow_instance: 'Workflow Instance',
    task_start_time: 'Start Time',
    worker_no_data_result_title: 'No Worker Nodes Exist',
    worker_no_data_result_desc:
      'Currently, there are no worker nodes exist, please create a worker node and refresh this page'
  },
  alert_server: {
    alert_server_no_data_result_title: 'No Alert Server Nodes Exist',
    alert_server_no_data_result_desc:
      'Currently, there are no alert server nodes exist, please create a alert server node and refresh this page'
  },
  db: {
    health_state: 'Health State',
    max_connections: 'Max Connections',
    threads_connections: 'Threads Connections',
    threads_running_connections: 'Threads Running Connections',
    db_no_data_result_title: 'No DB Nodes Exist',
    db_no_data_result_desc:
      'Currently, there are no DB nodes exist, please create a DB node and refresh this page'
  },
  statistics: {
    command_statistics_list: 'Command Statistics List',
    failure_command_statistics_list: 'Failure Command Statistics List',
    command_type: 'Command Type',
    command_param: 'Command Param',
    task_info: 'Task Info',
    task_params: 'Task Params',
    worker_info: 'Worker Info',
    warning_info: 'Warning Info',
    executor_id: 'Executor Id',
    message: 'Message',
    time: 'Time'
  },
  audit_log: {
    user_name: 'User Name',
    operation_type: 'Operation Type',
    model_type: 'Model Type',
    model_name: 'Model Name',
    latency: 'Latency',
    description: 'Description',
    create_time: 'Create Time',
    start_time: 'Start Time',
    end_time: 'End Time',
    user_audit: 'User Audit',
    project_audit: 'Project Audit',
    create: 'Create',
    update: 'Update',
    delete: 'Delete',
    read: 'Read'
  }
}
