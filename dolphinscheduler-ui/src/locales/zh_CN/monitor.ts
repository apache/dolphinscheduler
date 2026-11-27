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
    disk: '磁盘',
    title: '集群总览',
    subtitle: '服务拓扑与运行状态',
    normal: '正常',
    busy: '繁忙',
    unknown: '元数据未知',
    role_unknown: '角色未知',
    group_unknown: '未获取分组',
    running_workflows: '运行中的工作流',
    running_tasks: '运行中的任务',
    all_normal: '全部正常',
    service_instances: '服务实例',
    view: '查看',
    refresh: '刷新',
    topology: '集群拓扑',
    node_list: '节点列表',
    node: '节点',
    role_group: '服务 / 分组',
    status: '状态',
    memory_usage: '内存利用率',
    details: '详情',
    process_id: '进程 ID',
    startup_time: '启动时间',
    rpc_address: 'RPC 地址',
    directory: '注册目录',
    service_type: '服务类型',
    ha_role: 'HA 角色',
    worker_group: 'Worker 分组',
    coordinator_role: '协调角色',
    metadata_time: '元数据时间',
    unavailable: '未获取',
    memory: '内存',
    thread_pool: '线程池',
    group_count: '{count} 个 Worker 分组',
    worker_count: '{count} 个 Worker',
    busy_count: '{count} 繁忙',
    unknown_count: '{count} 未知',
    node_detail_label: '{name}，{status}，{role}，查看详情',
    group_label: '{name}，{count} 个 Worker，{action}分组',
    collapse: '收起',
    expand: '展开',
    no_nodes: '暂无已注册的服务实例',
    unknown_note: '元数据未知表示采集未成功，不等同于节点离线。',
    metadata_unavailable:
      '节点仍在注册列表中，本次元数据采集未成功。资源指标与运行负载未知。',
    topology_description:
      '服务架构示意：API、Master、Worker 从上到下分层，连线不代表实时通信',
    load_error: '无法刷新 {roles}。保留上次获取的节点，其指标标记为未知。',
    refreshed_at: '最后刷新',
    no_permission: '需要管理员权限',
    workflow_name: '工作流实例',
    workflow_state: '状态',
    start_time: '开始时间',
    run_times: '运行次数',
    task_name: '任务名称',
    task_type: '任务类型',
    query_failed: '运行实例查询失败',
    query_failed_description: '无法获取该节点的运行实例，请重试。',
    retry: '重新加载'
  },
  metadata_unavailable: '元数据尚未获取或已过期',
  master: {
    cpu_usage: '处理器使用量',
    memory_usage: '内存使用量',
    disk_usage: '磁盘使用量',
    create_time: '创建时间',
    last_heartbeat_time: '最后心跳时间',
    directory_detail: '目录详情',
    host: '主机',
    directory: '注册目录',
    running_workflows: '运行中的工作流',
    workflow_name: '工作流实例',
    workflow_state: '状态',
    workflow_start_time: '开始时间',
    workflow_run_times: '运行次数',
    running_tasks: '运行中的任务',
    master_no_data_result_title: 'Master节点不存在',
    master_no_data_result_desc:
      '目前没有任何Master节点，请先创建Master节点，再访问该页面'
  },
  worker: {
    cpu_usage: '处理器使用量',
    memory_usage: '内存使用量',
    disk_usage: '磁盘使用量',
    thread_pool_usage: '线程池使用量',
    create_time: '创建时间',
    last_heartbeat_time: '最后心跳时间',
    directory_detail: '目录详情',
    host: '主机',
    directory: '注册目录',
    running_tasks: '运行中的任务',
    task_name: '任务名称',
    task_type: '任务类型',
    task_workflow_instance: '工作流实例',
    task_start_time: '开始时间',
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
    command_statistics_list: '命令统计列表',
    failure_command_statistics_list: '失败命令统计列表',
    command_type: '命令类型',
    command_param: '命令参数',
    task_info: '任务信息',
    task_params: '任务参数',
    worker_info: 'Worker信息',
    warning_info: '警告信息',
    executor_id: '执行用户Id',
    message: '错误信息',
    time: '时间'
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
