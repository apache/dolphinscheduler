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

const login = {
  test: '测试',
  userName: '用户名',
  userName_tips: '请输入用户名',
  userPassword: '密码',
  userPassword_tips: '请输入密码',
  login: '登录',
}

const theme = {
  light: '浅色',
  dark: '深色',
}

const userDropdown = {
  profile: '用户信息',
  password: '密码管理',
  logout: '退出登录',
}

const menu = {
  home: '首页',
  project: '项目管理',
  resources: '资源中心',
  datasource: '数据源中心',
  monitor: '监控中心',
  security: '安全中心',
  workflow_monitoring: '工作流监控',
  workflow_relationships: '工作流关系',
  workflow: '工作流',
  workflow_definition: '工作流定义',
  workflow_instance: '工作流实例',
  task_instance: '任务实例',
  task_definition: '任务定义',
  file_manage: '文件管理',
  udf_manage: 'UDF管理',
  resource_manage: '资源管理',
  function_manage: '函数管理',
  task_group_manage: '任务组管理',
  task_group_option: '任务组配置',
  task_group_queue: '任务组队列',
  service_manage: '服务管理',
  master: 'Master',
  worker: 'Worker',
  db: 'DB',
  statistical_manage: '统计管理',
  statistics: 'Statistics',
  tenant_manage: '租户管理',
  user_manage: '用户管理',
  alarm_group_manage: '告警组管理',
  alarm_instance_manage: '告警实例管理',
  worker_group_manage: 'Worker分组管理',
  yarn_queue_manage: 'Yarn队列管理',
  environmental_manage: '环境管理',
  token_manage: '令牌管理',
}

const home = {
  task_state_statistics: '任务状态统计',
  process_state_statistics: '流程状态统计',
  process_definition_statistics: '流程定义统计',
  number: '数量',
  state: '状态',
}

const password = {
  edit_password: '修改密码',
  password: '密码',
  confirm_password: '确认密码',
  password_tips: '请输入密码',
  confirm_password_tips: '请输入确认密码',
  two_password_entries_are_inconsistent: '两次密码输入不一致',
  submit: '提交',
}

const profile = {
  profile: '用户信息',
  edit: '编辑',
  username: '用户名',
  email: '邮箱',
  phone: '手机',
  permission: '权限',
  create_time: '创建时间',
  update_time: '更新时间',
}

const resource = {
  task_group_manage: 'task group manage',
  task_group_option: 'task group option',
  create_task_group: 'Create task group',
  edit_task_group: 'Edit task group',
  delete_task_group: 'Delete task group',
  task_group_code: 'task group code',
  task_group_name: 'task group name',
  task_group_resource_pool_size: 'Resource pool size',
  task_group_resource_pool_size_be_a_number: 'the size of the task group resource pool should be more than 1',
  task_group_resource_used_pool_size: 'Used resource',
  task_group_desc: 'task group desc',
  task_group_status: 'task group status',
  task_group_enable_status: 'Enable',
  task_group_disable_status: 'Disable',
  please_enter_task_group_desc: 'please enter task group description',
  please_enter_task_group_resource_pool_size: 'please enter task group resource pool size',
  please_select_project: 'please select a project',
  task_group_queue: 'task group queue',
  task_group_queue_priority: 'priority',
  task_group_queue_priority_be_a_number: 'the priority of the task group queue should be a positive number',
  task_group_queue_force_starting_status: 'Starting status',
  task_group_in_queue: 'In queue',
  task_group_queue_status: 'task status',
  view_task_group_queue: 'View task group queue',
  task_group_queue_the_status_of_waiting: 'Waiting into the queue',
  task_group_queue_the_status_of_queuing: 'Queuing',
  task_group_queue_the_status_of_releasing: 'Released',
  modify_task_group_queue_priority: 'Edit the priority of the task group queue',
  priority_not_empty: 'the value of priority can not be empty',
  priority_must_be_number: 'the value of priority should be number',
  please_select_task_name: 'please select a task name'
}

export default {
  login,
  theme,
  userDropdown,
  menu,
  home,
  password,
  profile,
  resource
}
