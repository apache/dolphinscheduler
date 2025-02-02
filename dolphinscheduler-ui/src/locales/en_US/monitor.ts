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
  master: {
    cpu_usage: 'CPU Usage',
    memory_usage: 'Memory Usage',
    disk_usage: 'Disk Usage',
    create_time: 'Create Time',
    last_heartbeat_time: 'Last Heartbeat Time',
    directory_detail: 'Directory Detail',
    host: 'Host',
    directory: 'Directory',
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
