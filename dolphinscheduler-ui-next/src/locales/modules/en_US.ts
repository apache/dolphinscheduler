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

const resource = {
  file: {
    file_manage: 'File Manage',
    create_folder: 'Create Folder',
    create_file: 'Create File',
    upload_files: 'Upload Files',
    enter_keyword_tips: 'Please enter keyword',
    id: '#',
    name: 'Name',
    user_name: 'Resource userName',
    whether_directory: 'Whether directory',
    file_name: 'File Name',
    description: 'Description',
    size: 'Size',
    update_time: 'Update Time',
    operation: 'Operation',
    edit: 'Edit',
    rename: 'Rename',
    download: 'Download',
    delete: 'Delete',
    yes: 'Yes',
    no: 'No',
    folder_name: 'Folder Name',
    enter_name_tips: 'Please enter name',
    enter_description_tips: 'Please enter description',
    enter_content_tips: 'Please enter the resource content',
    file_format: 'File Format',
    file_content: 'File Content',
    delete_confirm: 'Delete?',
    confirm: 'Confirm',
    cancel: 'Cancel',
    success: 'Success',
    file_details: 'File Details',
    return: 'Return',
    save: 'Save',
  }
}

const project = {
  list: {
    create_project: 'Create Project',
    project_list: 'Project List',
    project_tips: 'Please enter your project',
    description_tips: 'Please enter your description',
    username_tips: 'Please enter your username',
    project_name: 'Project Name',
    project_description: 'Project Description',
    owned_users: 'Owned Users',
    workflow_define_count: 'Workflow Define Count',
    process_instance_running_count: 'Process Instance Running Count',
    description: 'Description',
    create_time: 'Create Time',
    update_time: 'Update Time',
    operation: 'Operation',
    edit: 'Edit',
    delete: 'Delete',
    confirm: 'Confirm',
    cancel: 'Cancel',
    delete_confirm: 'Delete?',
  },
}

const security = {
  tenant: {
    tenant_manage: 'Tenant Manage',
    create_tenant: 'Create Tenant',
    search_tips: 'Please enter keywords',
    num: 'Serial number',
    tenant_code: 'Operating System Tenant',
    description: 'Description',
    queue_name: 'QueueName',
    create_time: 'Create Time',
    update_time: 'Update Time',
    actions: 'Operation',
    edit_tenant: 'Edit Tenant',
    tenant_code_tips: 'Please enter the operating system tenant',
    queue_name_tips: 'Please select queue',
    description_tips: 'Please enter a description',
    delete_confirm: 'Delete?',
    edit: 'Edit',
    delete: 'Delete',
  }
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
  monitor,
  resource,
  project,
  security
}
