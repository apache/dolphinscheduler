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
  login: '登录'
}

const modal = {
  cancel: '取消',
  confirm: '确定'
}

const theme = {
  light: '浅色',
  dark: '深色'
}

const userDropdown = {
  profile: '用户信息',
  password: '密码管理',
  logout: '退出登录'
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
  token_manage: '令牌管理'
}

const home = {
  task_state_statistics: '任务状态统计',
  process_state_statistics: '流程状态统计',
  process_definition_statistics: '流程定义统计',
  number: '数量',
  state: '状态'
}

const password = {
  edit_password: '修改密码',
  password: '密码',
  confirm_password: '确认密码',
  password_tips: '请输入密码',
  confirm_password_tips: '请输入确认密码',
  two_password_entries_are_inconsistent: '两次密码输入不一致',
  submit: '提交'
}

const profile = {
  profile: '用户信息',
  edit: '编辑',
  username: '用户名',
  email: '邮箱',
  phone: '手机',
  state: '状态',
  permission: '权限',
  create_time: '创建时间',
  update_time: '更新时间',
  administrator: '管理员',
  ordinary_user: '普通用户',
  edit_profile: '编辑用户',
  username_tips: '请输入用户名',
  email_tips: '请输入邮箱',
  email_correct_tips: '请输入正确格式的邮箱',
  phone_tips: '请输入手机号',
  state_tips: '请选择状态',
  enable: '启用',
  disable: '禁用'
}

const monitor = {
  master: {
    cpu_usage: '处理器使用量',
    memory_usage: '内存使用量',
    load_average: '平均负载量',
    create_time: '创建时间',
    last_heartbeat_time: '最后心跳时间',
    directory_detail: '目录详情',
    host: '主机',
    directory: '注册目录'
  },
  worker: {
    cpu_usage: '处理器使用量',
    memory_usage: '内存使用量',
    load_average: '平均负载量',
    create_time: '创建时间',
    last_heartbeat_time: '最后心跳时间',
    directory_detail: '目录详情',
    host: '主机',
    directory: '注册目录'
  },
  db: {
    health_state: '健康状态',
    max_connections: '最大连接数',
    threads_connections: '当前连接数',
    threads_running_connections: '数据库当前活跃连接数'
  },
  statistics: {
    command_number_of_waiting_for_running: '待执行的命令数',
    failure_command_number: '执行失败的命令数',
    tasks_number_of_waiting_running: '待运行任务数',
    task_number_of_ready_to_kill: '待杀死任务数'
  }
}

const resource = {
  file: {
    file_manage: '文件管理',
    create_folder: '创建文件夹',
    create_file: '创建文件',
    upload_files: '上传文件',
    enter_keyword_tips: '请输入关键词',
    id: '编号',
    name: '名称',
    user_name: '所属用户',
    whether_directory: '是否文件夹',
    file_name: '文件名称',
    description: '描述',
    size: '大小',
    update_time: '更新时间',
    operation: '操作',
    edit: '编辑',
    rename: '重命名',
    download: '下载',
    delete: '删除',
    yes: '是',
    no: '否',
    folder_name: '文件夹名称',
    enter_name_tips: '请输入名称',
    enter_description_tips: '请输入描述',
    enter_content_tips: '请输入资源内容',
    enter_suffix_tips: '请输入文件后缀',
    file_format: '文件格式',
    file_content: '文件内容',
    delete_confirm: '确定删除吗?',
    confirm: '确定',
    cancel: '取消',
    success: '成功',
    file_details: '文件详情',
    return: '返回',
    save: '保存'
  }
}

const project = {
  list: {
    create_project: '创建项目',
    edit_project: '编辑项目',
    project_list: '项目列表',
    project_tips: '请输入项目名称',
    description_tips: '请输入项目描述',
    username_tips: '请输入所属用户',
    project_name: '项目名称',
    project_description: '项目描述',
    owned_users: '所属用户',
    workflow_define_count: '工作流定义数',
    process_instance_running_count: '正在运行的流程数',
    description: '描述',
    create_time: '创建时间',
    update_time: '更新时间',
    operation: '操作',
    edit: '编辑',
    delete: '删除',
    confirm: '确定',
    cancel: '取消',
    delete_confirm: '确定删除吗?'
  }
}

const security = {
  tenant: {
    tenant_manage: '租户管理',
    create_tenant: '创建租户',
    search_tips: '请输入关键词',
    num: '编号',
    tenant_code: '操作系统租户',
    description: '描述',
    queue_name: '队列',
    create_time: '创建时间',
    update_time: '更新时间',
    actions: '操作',
    edit_tenant: '编辑租户',
    tenant_code_tips: '请输入操作系统租户',
    queue_name_tips: '请选择队列',
    description_tips: '请输入描述',
    delete_confirm: '确定删除吗?',
    edit: '编辑',
    delete: '删除'
  },
  yarn_queue: {
    create_queue: '创建队列',
    edit_queue: '编辑队列',
    search_tips: '请输入关键词',
    queue_name: '队列名',
    queue_value: '队列值',
    create_time: '创建时间',
    update_time: '更新时间',
    operation: '操作',
    edit: '编辑',
    queue_name_tips: '请输入队列名称',
    queue_value_tips: '请输入队列值'
  }
}
const datasource = {
  datasource: '数据源',
  create_datasource: '创建数据源',
  search_input_tips: '请输入关键字',
  serial_number: '编号',
  datasource_name: '数据源名称',
  datasource_name_tips: '请输入数据源名称',
  datasource_user_name: '所属用户',
  datasource_type: '数据源类型',
  datasource_parameter: '数据源参数',
  description: '描述',
  description_tips: '请输入描述',
  create_time: '创建时间',
  update_time: '更新时间',
  operation: '操作',
  click_to_view: '点击查看',
  delete: '删除',
  confirm: '确定',
  cancel: '取消',
  create: '创建',
  edit: '编辑',
  success: '成功',
  test_connect: '测试连接',
  ip: 'IP主机名',
  ip_tips: '请输入IP主机名',
  port: '端口',
  port_tips: '请输入端口',
  database_name: '数据库名',
  database_name_tips: '请输入数据库名',
  oracle_connect_type: '服务名或SID',
  oracle_connect_type_tips: '请选择服务名或SID',
  oracle_service_name: '服务名',
  oracle_sid: 'SID',
  jdbc_connect_parameters: 'jdbc连接参数',
  principal_tips: '请输入Principal',
  krb5_conf_tips: '请输入kerberos认证参数 java.security.krb5.conf',
  keytab_username_tips: '请输入kerberos认证参数 login.user.keytab.username',
  keytab_path_tips: '请输入kerberos认证参数 login.user.keytab.path',
  format_tips: '请输入格式为',
  connection_parameter: '连接参数',
  user_name: '用户名',
  user_name_tips: '请输入用户名',
  user_password: '密码',
  user_password_tips: '请输入密码'
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
