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
  login: 'Login'
}

const modal = {
  cancel: 'Cancel',
  confirm: 'Confirm'
}

const theme = {
  light: 'Light',
  dark: 'Dark'
}

const userDropdown = {
  profile: 'Profile',
  password: 'Password',
  logout: 'Logout'
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
  environment_manage: 'Environment Manage',
  token_manage: 'Token Manage'
}

const home = {
  task_state_statistics: 'Task State Statistics',
  process_state_statistics: 'Process State Statistics',
  process_definition_statistics: 'Process Definition Statistics',
  number: 'Number',
  state: 'State'
}

const password = {
  edit_password: 'Edit Password',
  password: 'Password',
  confirm_password: 'Confirm Password',
  password_tips: 'Please enter your password',
  confirm_password_tips: 'Please enter your confirm password',
  two_password_entries_are_inconsistent:
    'Two password entries are inconsistent',
  submit: 'Submit'
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
  disable: 'Disable'
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
    directory: 'Directory'
  },
  worker: {
    cpu_usage: 'CPU Usage',
    memory_usage: 'Memory Usage',
    load_average: 'Load Average',
    create_time: 'Create Time',
    last_heartbeat_time: 'Last Heartbeat Time',
    directory_detail: 'Directory Detail',
    host: 'Host',
    directory: 'Directory'
  },
  db: {
    health_state: 'Health State',
    max_connections: 'Max Connections',
    threads_connections: 'Threads Connections',
    threads_running_connections: 'Threads Running Connections'
  },
  statistics: {
    command_number_of_waiting_for_running:
      'Command Number Of Waiting For Running',
    failure_command_number: 'Failure Command Number',
    tasks_number_of_waiting_running: 'Tasks Number Of Waiting Running',
    task_number_of_ready_to_kill: 'Task Number Of Ready To Kill'
  }
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
    save: 'Save'
  },
  udf: {
    udf_resources: 'UDF resources',
    create_folder: 'Create Folder',
    upload_udf_resources: 'Upload UDF Resources',
    id: '#',
    udf_source_name: 'UDF Resource Name',
    whether_directory: 'Whether directory',
    file_name: 'File Name',
    file_size: 'File Size',
    description: 'Description',
    create_time: 'Create Time',
    update_time: 'Update Time',
    operation: 'Operation',
    yes: 'Yes',
    no: 'No',
    edit: 'Edit',
    download: 'Download',
    delete: 'Delete',
    delete_confirm: 'Delete?',
    success: 'Success',
    folder_name: 'Folder Name',
    upload: 'Upload',
    upload_files: 'Upload Files',
    file_upload: 'File Upload',
    enter_keyword_tips: 'Please enter keyword',
    enter_name_tips: 'Please enter name',
    enter_description_tips: 'Please enter description'
  },
  task_group_option: {
    id: 'No.',
    manage: 'Task group manage',
    option: 'Task group option',
    create: 'Create task group',
    edit: 'Edit task group',
    delete: 'Delete task group',
    view_queue: 'View the queue of the task group',
    switch_status: 'Switch status',
    code: 'Task group code',
    name: 'Task group name',
    project_name: 'Project name',
    resource_pool_size: 'Resource pool size',
    resource_pool_size_be_a_number:
      'The size of the task group resource pool should be more than 1',
    resource_used_pool_size: 'Used resource',
    desc: 'Task group desc',
    status: 'Task group status',
    enable_status: 'Enable',
    disable_status: 'Disable',
    please_enter_name: 'Please enter task group name',
    please_enter_desc: 'Please enter task group description',
    please_enter_resource_pool_size:
      'Please enter task group resource pool size',
    please_select_project: 'Please select a project',
    create_time: 'Create time',
    update_time: 'Update time',
    actions: 'Actions',
    please_enter_keywords: 'Please enter keywords'
  },
  task_group_queue: {
    queue: 'Task group queue',
    priority: 'Priority',
    priority_be_a_number:
      'The priority of the task group queue should be a positive number',
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

const project = {
  list: {
    create_project: 'Create Project',
    edit_project: 'Edit Project',
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
    delete_confirm: 'Delete?'
  }
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
    delete: 'Delete'
  },
  alarm_group: {
    create_alarm_group: 'Create Alarm Group',
    edit_alarm_group: 'Edit Alarm Group',
    search_tips: 'Please enter keywords',
    alert_group_name_tips: 'Please enter your alert group name',
    alarm_plugin_instance: 'Alarm Plugin Instance',
    alarm_plugin_instance_tips: 'Please select alert plugin instance',
    alarm_group_description_tips: 'Please enter your alarm group description',
    alert_group_name: 'Alert Group Name',
    alarm_group_description: 'Alarm Group Description',
    create_time: 'Create Time',
    update_time: 'Update Time',
    operation: 'Operation',
    delete_confirm: 'Delete?',
    edit: 'Edit',
    delete: 'Delete'
  },
  worker_group: {
    create_worker_group: 'Create Worker Group',
    edit_worker_group: 'Edit Worker Group',
    search_tips: 'Please enter keywords',
    operation: 'Operation',
    delete_confirm: 'Delete?',
    edit: 'Edit',
    delete: 'Delete',
    group_name: 'Group Name',
    group_name_tips: 'Please enter your group name',
    worker_addresses: 'Worker Addresses',
    worker_addresses_tips: 'Please select worker addresses',
    create_time: 'Create Time',
    update_time: 'Update Time'
  },
  yarn_queue: {
    create_queue: 'Create Queue',
    edit_queue: 'Edit Queue',
    search_tips: 'Please enter keywords',
    queue_name: 'Queue Name',
    queue_value: 'Queue Value',
    create_time: 'Create Time',
    update_time: 'Update Time',
    operation: 'Operation',
    edit: 'Edit',
    queue_name_tips: 'Please enter your queue name',
    queue_value_tips: 'Please enter your queue value'
  },
  environment: {
    create_environment: 'Create Environment',
    edit_environment: 'Edit Environment',
    search_tips: 'Please enter keywords',
    edit: 'Edit',
    delete: 'Delete',
    environment_name: 'Environment Name',
    environment_config: 'Environment Config',
    environment_desc: 'Environment Desc',
    worker_groups: 'Worker Groups',
    create_time: 'Create Time',
    update_time: 'Update Time',
    operation: 'Operation',
    delete_confirm: 'Delete?',
    environment_name_tips: 'Please enter your environment name',
    environment_config_tips: 'Please enter your environment config',
    environment_description_tips: 'Please enter your environment description',
    worker_group_tips: 'Please select worker group'
  },
  token: {
    create_token: 'Create Token',
    edit_token: 'Edit Token',
    search_tips: 'Please enter keywords',
    user: 'User',
    user_tips: 'Please select user',
    token: 'Token',
    token_tips: 'Please enter your token',
    expiration_time: 'Expiration Time',
    expiration_time_tips: 'Please select expiration time',
    create_time: 'Create Time',
    update_time: 'Update Time',
    operation: 'Operation',
    edit: 'Edit',
    delete: 'Delete',
    delete_confirm: 'Delete?'
  }
}

const datasource = {
  datasource: 'DataSource',
  create_datasource: 'Create DataSource',
  search_input_tips: 'Please input the keywords',
  serial_number: '#',
  datasource_name: 'Datasource Name',
  datasource_name_tips: 'Please enter datasource name',
  datasource_user_name: 'Owner',
  datasource_type: 'Datasource Type',
  datasource_parameter: 'Datasource Parameter',
  description: 'Description',
  description_tips: 'Please enter description',
  create_time: 'Create Time',
  update_time: 'Update Time',
  operation: 'Operation',
  click_to_view: 'Click to view',
  delete: 'Delete',
  confirm: 'Confirm',
  cancel: 'Cancel',
  create: 'Create',
  edit: 'Edit',
  success: 'Success',
  test_connect: 'Test Connect',
  ip: 'IP',
  ip_tips: 'Please enter IP',
  port: 'Port',
  port_tips: 'Please enter port',
  database_name: 'Database Name',
  database_name_tips: 'Please enter database name',
  oracle_connect_type: 'ServiceName or SID',
  oracle_connect_type_tips: 'Please select serviceName or SID',
  oracle_service_name: 'ServiceName',
  oracle_sid: 'SID',
  jdbc_connect_parameters: 'jdbc connect parameters',
  principal_tips: 'Please enter Principal',
  krb5_conf_tips:
    'Please enter the kerberos authentication parameter java.security.krb5.conf',
  keytab_username_tips:
    'Please enter the kerberos authentication parameter login.user.keytab.username',
  keytab_path_tips:
    'Please enter the kerberos authentication parameter login.user.keytab.path',
  format_tips: 'Please enter format',
  connection_parameter: 'connection parameter',
  user_name: 'User Name',
  user_name_tips: 'Please enter your username',
  user_password: 'Password',
  user_password_tips: 'Please enter your password'
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
  security,
  datasource
}
