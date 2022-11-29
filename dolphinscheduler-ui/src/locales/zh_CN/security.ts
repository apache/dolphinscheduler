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
  tenant: {
    tenant_manage: '租户管理',
    create_tenant: '创建租户',
    search_tips: '请输入关键词',
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
  alarm_group: {
    create_alarm_group: '创建告警组',
    edit_alarm_group: '编辑告警组',
    search_tips: '请输入关键词',
    alert_group_name_tips: '请输入告警组名称',
    alarm_plugin_instance: '告警组实例',
    alarm_plugin_instance_tips: '请选择告警组实例',
    alarm_group_description_tips: '请输入告警组描述',
    alert_group_name: '告警组名称',
    alarm_group_description: '告警组描述',
    create_time: '创建时间',
    update_time: '更新时间',
    operation: '操作',
    delete_confirm: '确定删除吗?',
    edit: '编辑',
    delete: '删除'
  },
  worker_group: {
    create_worker_group: '创建Worker分组',
    edit_worker_group: '编辑Worker分组',
    search_tips: '请输入关键词',
    operation: '操作',
    delete_confirm: '确定删除吗?',
    edit: '编辑',
    delete: '删除',
    group_name: '分组名称',
    group_name_tips: '请输入分组名称',
    worker_addresses: 'Worker地址',
    worker_addresses_tips: '请选择Worker地址',
    create_time: '创建时间',
    update_time: '更新时间'
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
    delete: '删除',
    delete_confirm: '确定删除吗?',
    queue_name_tips: '请输入队列名',
    queue_value_tips: '请输入队列值'
  },
  environment: {
    create_environment: '创建环境',
    edit_environment: '编辑环境',
    search_tips: '请输入关键词',
    edit: '编辑',
    delete: '删除',
    environment_name: '环境名称',
    environment_config: '环境配置',
    environment_desc: '环境描述',
    worker_groups: 'Worker分组',
    create_time: '创建时间',
    update_time: '更新时间',
    operation: '操作',
    delete_confirm: '确定删除吗?',
    environment_name_tips: '请输入环境名',
    environment_config_tips: '请输入环境配置',
    environment_description_tips: '请输入环境描述',
    worker_group_tips: '请选择Worker分组'
  },
  cluster: {
    create_cluster: '创建集群',
    edit_cluster: '编辑集群',
    search_tips: '请输入关键词',
    edit: '编辑',
    delete: '删除',
    cluster_name: '集群名称',
    cluster_components: '集群模块',
    cluster_config: '集群配置',
    kubernetes_config: 'Kubernetes配置',
    yarn_config: 'Yarn配置',
    cluster_desc: '集群描述',
    create_time: '创建时间',
    update_time: '更新时间',
    operation: '操作',
    delete_confirm: '确定删除吗?',
    cluster_name_tips: '请输入集群名',
    cluster_config_tips: '请输入集群配置',
    cluster_description_tips: '请输入集群描述'
  },
  token: {
    create_token: '创建令牌',
    edit_token: '编辑令牌',
    search_tips: '请输入关键词',
    user: '用户',
    user_tips: '请选择用户',
    token: '令牌',
    token_tips: '请点击获取令牌',
    expiration_time: '失效时间',
    expiration_time_tips: '请选择失效时间',
    create_time: '创建时间',
    update_time: '更新时间',
    operation: '操作',
    edit: '编辑',
    delete: '删除',
    delete_confirm: '确定删除吗?'
  },
  user: {
    user_manage: '用户管理',
    create_user: '创建用户',
    edit_user: '编辑用户',
    delete_user: '删除用户',
    delete_confirm: '确定删除吗?',
    project: '项目',
    resource: '资源',
    file_resource: '文件资源',
    udf_resource: 'UDF资源',
    datasource: '数据源',
    udf: 'UDF函数',
    namespace: '命名空间',
    revoke_auth: '撤销权限',
    grant_read: '授予读权限',
    grant_all:'授予所有权限',
    authorize_project: '项目授权',
    authorize_resource: '资源授权',
    authorize_namespace: '命名空间授权',
    authorize_datasource: '数据源授权',
    authorize_udf: 'UDF函数授权',
    username: '用户名',
    username_exists: '用户名已存在',
    username_tips: '请输入用户名',
    user_password: '密码',
    user_password_tips: '请输入包含字母和数字，长度在6～20之间的密码',
    user_type: '用户类型',
    ordinary_user: '普通用户',
    administrator: '管理员',
    tenant_code: '租户',
    tenant_id_tips: '请选择租户',
    queue: '队列',
    queue_tips: '默认为租户关联队列',
    email: '邮件',
    email_empty_tips: '请输入邮箱',
    emial_correct_tips: '请输入正确的邮箱格式',
    phone: '手机',
    phone_empty_tips: '请输入手机号码',
    phone_correct_tips: '请输入正确的手机格式',
    state: '状态',
    state_enabled: '启用',
    state_disabled: '停用',
    create_time: '创建时间',
    update_time: '更新时间',
    operation: '操作',
    edit: '编辑',
    delete: '删除',
    authorize: '授权',
    save_error_msg: '保存失败，请重试',
    delete_error_msg: '删除失败，请重试',
    auth_error_msg: '授权失败，请重试',
    auth_success_msg: '授权成功',
    enable: '启用',
    disable: '停用'
  },
  alarm_instance: {
    search_input_tips: '请输入关键字',
    alarm_instance_manage: '告警实例管理',
    alarm_instance_name: '告警实例名称',
    alarm_instance_name_tips: '请输入告警实例名称',
    alarm_plugin_name: '告警插件名称',
    create_time: '创建时间',
    update_time: '更新时间',
    operation: '操作',
    edit_alarm_instance: '编辑告警实例',
    delete: '删除',
    edit: '编辑',
    delete_confirm: '删除？',
    confirm: '确定',
    cancel: '取消',
    submit: '提交',
    create_alarm_instance: '创建告警实例',
    select_plugin: '选择插件',
    select_plugin_tips: '请选择告警插件',
    instance_parameter_exception: '实例参数异常',
    WebHook: 'Web钩子',
    webHook: 'Web钩子',
    WarningType: '告警类型',
    IsEnableProxy: '启用代理',
    Proxy: '代理',
    Port: '端口',
    User: '用户',
    corpId: '企业ID',
    secret: '密钥',
    Secret: '密钥',
    users: '群员',
    userSendMsg: '群员信息',
    'agentId/chatId': '应用ID或群聊ID',
    showType: '内容展示类型',
    receivers: '收件人',
    receiverCcs: '抄送人',
    serverHost: 'SMTP服务器',
    serverPort: 'SMTP端口',
    sender: '发件人',
    enableSmtpAuth: '请求认证',
    Password: '密码',
    starttlsEnable: 'STARTTLS连接',
    sslEnable: 'SSL连接',
    smtpSslTrust: 'SSL证书信任',
    url: 'URL',
    requestType: '请求方式',
    headerParams: '请求头',
    bodyParams: '请求体',
    contentField: '内容字段',
    Keyword: '关键词',
    userParams: '自定义参数',
    path: '脚本路径',
    type: '类型',
    sendType: '发送类型',
    username: '用户名',
    botToken: '机器人Token',
    chatId: '频道ID',
    parseMode: '解析类型',
    IntegrationKey: '集成密钥',
    BotAccessToken: '访问令牌',
    RoomId: '房间',
    ToPersonId: '用户',
    ToPersonEmail: '用户邮箱',
    // eslint-disable-next-line quotes
    AtSomeoneInRoom: "{'@'}房间中的成员",
    Destination: '目的地',
    // eslint-disable-next-line quotes
    AtMobiles: "被{'@'}人的手机号",
    // eslint-disable-next-line quotes
    AtUserIds: "被{'@'}人的用户ID",
    MsgType: '消息类型',
    // eslint-disable-next-line quotes
    IsAtAll: "{'@'}所有人"
  },
  k8s_namespace: {
    create_namespace: '创建命名空间',
    edit_namespace: '编辑命名空间',
    search_tips: '请输入关键词',
    k8s_namespace: 'K8S命名空间',
    k8s_namespace_tips: '请输入k8s命名空间',
    k8s_cluster: 'K8S集群',
    k8s_cluster_tips: '请输入k8s集群',
    limit_cpu: '最大CPU',
    limit_cpu_tips: '请输入最大CPU',
    limit_memory: '最大内存',
    limit_memory_tips: '请输入最大内存',
    create_time: '创建时间',
    update_time: '更新时间',
    operation: '操作',
    edit: '编辑',
    delete: '删除',
    delete_confirm: '确定删除吗?'
  }
}
