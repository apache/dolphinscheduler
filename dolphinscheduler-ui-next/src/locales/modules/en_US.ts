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
  test: 'Test',
  userName: 'Username',
  userName_tips: 'Please enter your username',
  userPassword: 'Password',
  userPassword_tips: 'Please enter your password',
  login: 'Login',
}

const theme = {
  light: 'Light',
  dark: 'Dark',
}

const userDropdown = {
  profile: 'Profile',
  password: 'Password',
  logout: 'Logout',
}

const menu = {
  home: 'Home',
  project: 'Project',
  resources: 'Resources',
  datasource: 'Datasource',
  monitor: 'Monitor',
  security: 'Security',
  workflow_monitoring: 'Workflow Monitoring',
  workflow_relationships: 'Workflow Relationships',
  workflow: 'Workflow',
  workflow_definition: 'Workflow Definition',
  workflow_instance: 'Workflow Instance',
  task_instance: 'Task Instance',
  task_definition: 'Task Definition',
  file_manage: 'File Manage',
  udf_manage: 'UDF Manage',
  resource_manage: 'Resource Manage',
  function_manage: 'Function Manage',
  task_group_manage: 'Task group manage',
  task_group_option: 'Task group option',
  task_group_queue: 'Task group queue',
  service_manage: 'Service Manage',
  master: 'Master',
  worker: 'Worker',
  db: 'DB',
  statistical_manage: 'Statistical Manage',
  statistics: 'Statistics',
  tenant_manage: 'Tenant Manage',
  user_manage: 'User Manage',
  alarm_group_manage: 'Alarm Group Manage',
  alarm_instance_manage: 'Alarm Instance Manage',
  worker_group_manage: 'Worker Group Manage',
  yarn_queue_manage: 'Yarn Queue Manage',
  environmental_manage: 'Environmental Manage',
  token_manage: 'Token Manage',
}

const home = {
  task_state_statistics: 'Task State Statistics',
  process_state_statistics: 'Process State Statistics',
  process_definition_statistics: 'Process Definition Statistics',
  number: 'Number',
  state: 'State',
}

const password = {
  edit_password: 'Edit Password',
  password: 'Password',
  confirm_password: 'Confirm Password',
  password_tips: 'Please enter your password',
  confirm_password_tips: 'Please enter your confirm password',
  two_password_entries_are_inconsistent:
    'Two Password Entries Are Inconsistent',
  submit: 'Submit',
}

const profile = {
  profile: 'Profile',
  edit: 'Edit',
  username: 'Username',
  email: 'Email',
  phone: 'Phone',
  permission: 'Permission',
  create_time: 'Create Time',
  update_time: 'Update Time',
}

const resource = {
  task_group_manage: '任务组管理',
  task_group_option: '任务组配置',
  create_task_group: '创建任务组',
  edit_task_group: '编辑任务组',
  delete_task_group: '删除任务组',
  task_group_code: '任务组编号',
  task_group_name: '任务组名称',
  task_group_resource_pool_size: '资源容量',
  task_group_resource_used_pool_size: '已用资源',
  task_group_desc: '描述信息',
  task_group_status: '任务组状态',
  task_group_enable_status: '启用',
  task_group_disable_status: '不可用',
  please_enter_task_group_desc: '请输入任务组描述',
  please_enter_task_group_resource_pool_size: '请输入资源容量大小',
  task_group_resource_pool_size_be_a_number: '资源容量大小必须大于等于1的数值',
  please_select_project: '请选择项目',
  task_group_queue: '任务组队列',
  task_group_queue_priority: '组内优先级',
  task_group_queue_priority_be_a_number: '优先级必须是大于等于0的数值',
  task_group_queue_force_starting_status: '是否强制启动',
  task_group_in_queue: '是否排队中',
  task_group_queue_status: '任务状态',
  view_task_group_queue: '查看任务组队列',
  task_group_queue_the_status_of_waiting: '等待入队',
  task_group_queue_the_status_of_queuing: '排队中',
  task_group_queue_the_status_of_releasing: '已释放',
  modify_task_group_queue_priority: '修改优先级',
  force_to_start_task: '强制启动',
  priority_not_empty: '优先级不能为空',
  priority_must_be_number: '优先级必须是数值',
  please_select_task_name: '请选择节点名称'
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
