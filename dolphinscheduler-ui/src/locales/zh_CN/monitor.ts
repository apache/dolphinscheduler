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
    cpu_usage: '处理器使用量',
    memory_usage: '内存使用量',
    disk_available: '磁盘可用容量',
    load_average: '平均负载量',
    create_time: '创建时间',
    last_heartbeat_time: '最后心跳时间',
    directory_detail: '目录详情',
    host: '主机',
    directory: '注册目录',
    master_no_data_result_title: 'Master节点不存在',
    master_no_data_result_desc:
      '目前没有任何Master节点，请先创建Master节点，再访问该页面'
  },
  worker: {
    cpu_usage: '处理器使用量',
    memory_usage: '内存使用量',
    disk_available: '磁盘可用容量',
    load_average: '平均负载量',
    thread_pool_usage: '线程池使用量',
    create_time: '创建时间',
    last_heartbeat_time: '最后心跳时间',
    directory_detail: '目录详情',
    host: '主机',
    directory: '注册目录',
    worker_no_data_result_title: 'Worker节点不存在',
    worker_no_data_result_desc:
      '目前没有任何Worker节点，请先创建Worker节点，再访问该页面'
  },
  alert_server: {
    alert_server_no_data_result_title: 'Alert Server节点不存在',
    alert_server_no_data_result_desc:
      '目前没有任何Alert Server节点，请先创建Alert Server节点，再访问该页面'
  },
  db: {
    health_state: '健康状态',
    max_connections: '最大连接数',
    threads_connections: '当前连接数',
    threads_running_connections: '数据库当前活跃连接数',
    db_no_data_result_title: 'DB节点不存在',
    db_no_data_result_desc: '目前没有任何DB节点，请先创建DB节点，再访问该页面'
  },
  statistics: {
    command_number_of_waiting_for_running: '待执行的命令数',
    failure_command_number: '执行失败的命令数'
  },
  audit_log: {
    user_name: '用户名称',
    operation_type: '操作类型',
    model_type: '模型类型',
    model_name: '模型名称',
    latency: '耗时',
    description: '描述',
    create_time: '创建时间',
    start_time: '开始时间',
    end_time: '结束时间',
    user_audit: '用户管理审计',
    project_audit: '项目管理审计',
    create: '创建',
    update: '更新',
    delete: '删除',
    read: '读取'
  }
}
