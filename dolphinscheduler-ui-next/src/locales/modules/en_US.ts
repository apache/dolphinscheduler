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

const modal = {
  cancel: 'Cancel',
  confirm: 'Confirm',
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
  task_group_manage: 'Task group manage',
  task_group_option: 'Task group option',
  task_group_queue: 'Task group queue',
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
    'Two password entries are inconsistent',
  submit: 'Submit',
}

const profile = {
  profile: 'Profile',
  edit: 'Edit',
  username: 'Username',
  email: 'Email',
  phone: 'Phone',
  state: 'State',
  permission: 'Permission',
  create_time: 'Create Time',
  update_time: 'Update Time',
  administrator: 'Administrator',
  ordinary_user: 'Ordinary User',
  edit_profile: 'Edit Profile',
  username_tips: 'Please enter your username',
  email_tips: 'Please enter your email',
  email_correct_tips: 'Please enter your email in the correct format',
  phone_tips: 'Please enter your phone',
  state_tips: 'Please choose your state',
  enable: 'Enable',
  disable: 'Disable',
}

const resource = {
  task_group_option: {
    id: 'No.',
    manage: 'Task group manage',
    option: 'Task group option',
    create: 'Create task group',
    edit: 'Edit task group',
    delete: 'Delete task group',
    code: 'Task group code',
    name: 'Task group name',
    project_name: 'Project name',
    resource_pool_size: 'Resource pool size',
    resource_pool_size_be_a_number: 'The size of the task group resource pool should be more than 1',
    resource_used_pool_size: 'Used resource',
    desc: 'Task group desc',
    status: 'Task group status',
    enable_status: 'Enable',
    disable_status: 'Disable',
    please_enter_desc: 'Please enter task group description',
    please_enter_resource_pool_size: 'Please enter task group resource pool size',
    please_select_project: 'Please select a project',
    create_time: 'Create time',
    update_time: 'Update time',
    actions: 'Actions',
    please_enter_keywords: 'Please enter keywords',
  },
  task_group_queue: {
    queue: 'Task group queue',
    priority: 'Priority',
    priority_be_a_number: 'The priority of the task group queue should be a positive number',
    force_starting_status: 'Starting status',
    in_queue: 'In queue',
    task_status: 'Task status',
    view: 'View task group queue',
    the_status_of_waiting: 'Waiting into the queue',
    the_status_of_queuing: 'Queuing',
    the_status_of_releasing: 'Released',
    modify_priority: 'Edit the priority of the task group queue',
    priority_not_empty: 'The value of priority can not be empty',
    priority_must_be_number: 'The value of priority should be number',
    please_select_task_name: 'Please select a task name'
  }
}

const monitor = {
  master: {
    cpu_usage: 'CPU Usage',
    memory_usage: 'Memory Usage',
    load_average: 'Load Average',
    create_time: 'Create Time',
    last_heartbeat_time: 'Last Heartbeat Time',
    directory_detail: 'Directory Detail',
    host: 'Host',
    directory: 'Directory',
  },
  worker: {
    cpu_usage: 'CPU Usage',
    memory_usage: 'Memory Usage',
    load_average: 'Load Average',
    create_time: 'Create Time',
    last_heartbeat_time: 'Last Heartbeat Time',
    directory_detail: 'Directory Detail',
    host: 'Host',
    directory: 'Directory',
  },
  db: {
    health_state: 'Health State',
    max_connections: 'Max Connections',
    threads_connections: 'Threads Connections',
    threads_running_connections: 'Threads Running Connections',
  },
  statistics: {
    command_number_of_waiting_for_running:
        'Command Number Of Waiting For Running',
    failure_command_number: 'Failure Command Number',
    tasks_number_of_waiting_running: 'Tasks Number Of Waiting Running',
    task_number_of_ready_to_kill: 'Task Number Of Ready To Kill',
  },
}

export default {
  login,
  modal,
  theme,
  userDropdown,
  menu,
  home,
  password,
  profile,
  resource,
  monitor
}
