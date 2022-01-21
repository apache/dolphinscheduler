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
  'User Name': '用户名',
  'Please enter user name': '请输入用户名',
  Password: '密码',
  'Please enter your password': '请输入密码',
  'Password consists of at least two combinations of numbers, letters, and characters, and the length is between 6-22': '密码至少包含数字，字母和字符的两种组合，长度在6-22之间',
  Login: '登录',
  Home: '首页',
  'Failed to create node to save': '未创建节点保存失败',
  'Global parameters': '全局参数',
  'Local parameters': '局部参数',
  'Copy success': '复制成功',
  'The browser does not support automatic copying': '该浏览器不支持自动复制',
  'Whether to save the DAG graph': '是否保存DAG图',
  'Current node settings': '当前节点设置',
  'View history': '查看历史',
  'View log': '查看日志',
  'Force success': '强制成功',
  'Enter this child node': '进入该子节点',
  'Node name': '节点名称',
  'Please enter name (required)': '请输入名称(必填)',
  'Run flag': '运行标志',
  Normal: '正常',
  'Prohibition execution': '禁止执行',
  'Please enter description': '请输入描述',
  'Number of failed retries': '失败重试次数',
  Times: '次',
  'Failed retry interval': '失败重试间隔',
  Minute: '分',
  'Delay execution time': '延时执行时间',
  'Delay execution': '延时执行',
  'Forced success': '强制成功',
  Cancel: '取消',
  'Confirm add': '确认添加',
  'The newly created sub-Process has not yet been executed and cannot enter the sub-Process': '新创建子工作流还未执行，不能进入子工作流',
  'The task has not been executed and cannot enter the sub-Process': '该任务还未执行，不能进入子工作流',
  'Name already exists': '名称已存在请重新输入',
  'Download Log': '下载日志',
  'Refresh Log': '刷新日志',
  'Enter full screen': '进入全屏',
  'Cancel full screen': '取消全屏',
  Close: '关闭',
  'Update log success': '更新日志成功',
  'No more logs': '暂无更多日志',
  'No log': '暂无日志',
  'Loading Log...': '正在努力请求日志中...',
  'Set the DAG diagram name': '设置DAG图名称',
  'Please enter description(optional)': '请输入描述(选填)',
  'Set global': '设置全局',
  'Whether to go online the process definition': '是否上线流程定义',
  'Whether to update the process definition': '是否更新流程定义',
  Add: '添加',
  'DAG graph name cannot be empty': 'DAG图名称不能为空',
  'Create Datasource': '创建数据源',
  'Project Home': '工作流监控',
  'Project Manage': '项目管理',
  'Create Project': '创建项目',
  'Cron Manage': '定时管理',
  'Copy Workflow': '复制工作流',
  'Tenant Manage': '租户管理',
  'Create Tenant': '创建租户',
  'User Manage': '用户管理',
  'Create User': '创建用户',
  'User Information': '用户信息',
  'Edit Password': '密码修改',
  Success: '成功',
  Failed: '失败',
  Delete: '删除',
  'Please choose': '请选择',
  'Please enter a positive integer': '请输入正整数',
  'Program Type': '程序类型',
  'Main Class': '主函数的Class',
  'Main Package': '主程序包',
  'Please enter main package': '请选择主程序包',
  'Please enter main class': '请填写主函数的Class',
  'Main Arguments': '主程序参数',
  'Please enter main arguments': '请输入主程序参数',
  'Option Parameters': '选项参数',
  'Please enter option parameters': '请输入选项参数',
  Resources: '资源',
  'Custom Parameters': '自定义参数',
  'Custom template': '自定义模版',
  Datasource: '数据源',
  methods: '方法',
  'Please enter the procedure method': '请输入存储脚本 \n\n调用存储过程：{call <procedure-name>[(<arg1>,<arg2>, ...)]}\n\n调用存储函数：{?= call <procedure-name>[(<arg1>,<arg2>, ...)]} ',
  'The procedure method script example': '示例：{call <procedure-name>[(?,?, ...)]} 或 {?= call <procedure-name>[(?,?, ...)]}',
  Script: '脚本',
  'Please enter script(required)': '请输入脚本(必填)',
  'Deploy Mode': '部署方式',
  'Driver Cores': 'Driver核心数',
  'Please enter Driver cores': '请输入Driver核心数',
  'Driver Memory': 'Driver内存数',
  'Please enter Driver memory': '请输入Driver内存数',
  'Executor Number': 'Executor数量',
  'Please enter Executor number': '请输入Executor数量',
  'The Executor number should be a positive integer': 'Executor数量为正整数',
  'Executor Memory': 'Executor内存数',
  'Please enter Executor memory': '请输入Executor内存数',
  'Executor Cores': 'Executor核心数',
  'Please enter Executor cores': '请输入Executor核心数',
  'Memory should be a positive integer': '内存数为数字',
  'Core number should be positive integer': '核心数为正整数',
  'Flink Version': 'Flink版本',
  'JobManager Memory': 'JobManager内存数',
  'Please enter JobManager memory': '请输入JobManager内存数',
  'TaskManager Memory': 'TaskManager内存数',
  'Please enter TaskManager memory': '请输入TaskManager内存数',
  'Slot Number': 'Slot数量',
  'Please enter Slot number': '请输入Slot数量',
  Parallelism: '并行度',
  'Custom Parallelism': '自定义并行度',
  'Please enter Parallelism': '请输入并行度',
  'Parallelism number should be positive integer': '并行度必须为正整数',
  'Parallelism tip': '如果存在大量任务需要补数时,可以利用自定义并行度将补数的任务线程设置成合理的数值,避免对服务器造成过大的影响',
  'TaskManager Number': 'TaskManager数量',
  'Please enter TaskManager number': '请输入TaskManager数量',
  'App Name': '任务名称',
  'Please enter app name(optional)': '请输入任务名称(选填)',
  'SQL Type': 'sql类型',
  'Send Email': '发送邮件',
  'Log display': '日志显示',
  'rows of result': '行查询结果',
  Title: '主题',
  'Please enter the title of email': '请输入邮件主题',
  Table: '表名',
  TableMode: '表格',
  Attachment: '附件',
  'SQL Parameter': 'sql参数',
  'SQL Statement': 'sql语句',
  'UDF Function': 'UDF函数',
  'Please enter a SQL Statement(required)': '请输入sql语句(必填)',
  'Please enter a JSON Statement(required)': '请输入json语句(必填)',
  'One form or attachment must be selected': '表格、附件必须勾选一个',
  'Mail subject required': '邮件主题必填',
  'Child Node': '子节点',
  'Please select a sub-Process': '请选择子工作流',
  Edit: '编辑',
  'Switch To This Version': '切换到该版本',
  'Datasource Name': '数据源名称',
  'Please enter datasource name': '请输入数据源名称',
  IP: 'IP主机名',
  'Please enter IP': '请输入IP主机名',
  Port: '端口',
  'Please enter port': '请输入端口',
  'Database Name': '数据库名',
  'Please enter database name': '请输入数据库名',
  'Oracle Connect Type': '服务名或SID',
  'Oracle Service Name': '服务名',
  'Oracle SID': 'SID',
  'jdbc connect parameters': 'jdbc连接参数',
  'Test Connect': '测试连接',
  'Please enter resource name': '请输入数据源名称',
  'Please enter resource folder name': '请输入资源文件夹名称',
  'Please enter a non-query SQL statement': '请输入非查询sql语句',
  'Please enter IP/hostname': '请输入IP/主机名',
  'jdbc connection parameters is not a correct JSON format': 'jdbc连接参数不是一个正确的JSON格式',
  '#': '编号',
  'Datasource Type': '数据源类型',
  'Datasource Parameter': '数据源参数',
  'Create Time': '创建时间',
  'Update Time': '更新时间',
  Operation: '操作',
  'Current Version': '当前版本',
  'Click to view': '点击查看',
  'Delete?': '确定删除吗?',
  'Switch Version Successfully': '切换版本成功',
  'Confirm Switch To This Version?': '确定切换到该版本吗?',
  Confirm: '确定',
  'Task status statistics': '任务状态统计',
  Number: '数量',
  State: '状态',
  'Dry-run flag': '空跑标识',
  'Process Status Statistics': '流程状态统计',
  'Process Definition Statistics': '流程定义统计',
  'Project Name': '项目名称',
  'Please enter name': '请输入名称',
  'Owned Users': '所属用户',
  'Process Pid': '进程Pid',
  'Zk registration directory': 'zk注册目录',
  cpuUsage: 'cpuUsage',
  memoryUsage: 'memoryUsage',
  'Last heartbeat time': '最后心跳时间',
  'Edit Tenant': '编辑租户',
  'OS Tenant Code': '操作系统租户',
  'Tenant Name': '租户名称',
  Queue: '队列',
  'Please select a queue': '默认为租户关联队列',
  'Please enter the os tenant code in English': '请输入操作系统租户只允许英文',
  'Please enter os tenant code in English': '请输入英文操作系统租户',
  'Please enter os tenant code': '请输入操作系统租户',
  'Please enter tenant Name': '请输入租户名称',
  'The os tenant code. Only letters or a combination of letters and numbers are allowed': '操作系统租户只允许字母或字母与数字组合',
  'Edit User': '编辑用户',
  Tenant: '租户',
  Email: '邮件',
  Phone: '手机',
  'User Type': '用户类型',
  'Please enter phone number': '请输入手机',
  'Please enter email': '请输入邮箱',
  'Please enter the correct email format': '请输入正确的邮箱格式',
  'Please enter the correct mobile phone format': '请输入正确的手机格式',
  Project: '项目',
  Authorize: '授权',
  'File resources': '文件资源',
  'UDF resources': 'UDF资源',
  'UDF resources directory': 'UDF资源目录',
  'Please select UDF resources directory': '请选择UDF资源目录',
  'Alarm group': '告警组',
  'Alarm group required': '告警组必填',
  'Edit alarm group': '编辑告警组',
  'Create alarm group': '创建告警组',
  'Create Alarm Instance': '创建告警实例',
  'Edit Alarm Instance': '编辑告警实例',
  'Group Name': '组名称',
  'Alarm instance name': '告警实例名称',
  'Alarm plugin name': '告警插件名称',
  'Select plugin': '选择插件',
  'Select Alarm plugin': '请选择告警插件',
  'Please enter group name': '请输入组名称',
  'Instance parameter exception': '实例参数异常',
  'Group Type': '组类型',
  'Alarm plugin instance': '告警插件实例',
  'Please enter alarm plugin instance name': '请输入告警实例名称',
  'Select Alarm plugin instance': '请选择告警插件实例',
  Remarks: '备注',
  SMS: '短信',
  'Managing Users': '管理用户',
  Permission: '权限',
  Administrator: '管理员',
  'Confirm Password': '确认密码',
  'Please enter confirm password': '请输入确认密码',
  'Password cannot be in Chinese': '密码不能为中文',
  'Please enter a password (6-22) character password': '请输入密码(6-22)字符密码',
  'Confirmation password cannot be in Chinese': '确认密码不能为中文',
  'Please enter a confirmation password (6-22) character password': '请输入确认密码(6-22)字符密码',
  'The password is inconsistent with the confirmation password': '密码与确认密码不一致,请重新确认',
  'Please select the datasource': '请选择数据源',
  'Please select resources': '请选择资源',
  Query: '查询',
  'Non Query': '非查询',
  'prop(required)': 'prop(必填)',
  'value(optional)': 'value(选填)',
  'value(required)': 'value(必填)',
  'prop is empty': '自定义参数prop不能为空',
  'value is empty': 'value不能为空',
  'prop is repeat': 'prop中有重复',
  'Start Time': '开始时间',
  'End Time': '结束时间',
  crontab: 'crontab',
  'Failure Strategy': '失败策略',
  online: '上线',
  offline: '下线',
  'Task Status': '任务状态',
  'Process Instance': '工作流实例',
  'Task Instance': '任务实例',
  'Select date range': '选择日期区间',
  startDate: '开始日期',
  endDate: '结束日期',
  Date: '日期',
  Waiting: '等待',
  Execution: '执行中',
  Finish: '完成',
  'Create File': '创建文件',
  'Create folder': '创建文件夹',
  'File Name': '文件名称',
  'Folder Name': '文件夹名称',
  'File Format': '文件格式',
  'Folder Format': '文件夹格式',
  'File Content': '文件内容',
  'Upload File Size': '文件大小不能超过1G',
  Create: '创建',
  'Please enter the resource content': '请输入资源内容',
  'Resource content cannot exceed 3000 lines': '资源内容不能超过3000行',
  'File Details': '文件详情',
  'Download Details': '下载详情',
  Return: '返回',
  Save: '保存',
  'File Manage': '文件管理',
  'Upload Files': '上传文件',
  'Create UDF Function': '创建UDF函数',
  'Upload UDF Resources': '上传UDF资源',
  'Service-Master': '服务管理-Master',
  'Service-Worker': '服务管理-Worker',
  'Process Name': '工作流名称',
  Executor: '执行用户',
  'Run Type': '运行类型',
  'Scheduling Time': '调度时间',
  'Run Times': '运行次数',
  host: 'host',
  'fault-tolerant sign': '容错标识',
  Rerun: '重跑',
  'Recovery Failed': '恢复失败',
  Stop: '停止',
  Pause: '暂停',
  'Recovery Suspend': '恢复运行',
  Gantt: '甘特图',
  'Node Type': '节点类型',
  'Submit Time': '提交时间',
  Duration: '运行时长',
  'Retry Count': '重试次数',
  'Task Name': '任务名称',
  'Task Date': '任务日期',
  'Source Table': '源表',
  'Record Number': '记录数',
  'Target Table': '目标表',
  'Online viewing type is not supported': '不支持在线查看类型',
  Size: '大小',
  Rename: '重命名',
  Download: '下载',
  Export: '导出',
  'Version Info': '版本信息',
  Submit: '提交',
  'Edit UDF Function': '编辑UDF函数',
  type: '类型',
  'UDF Function Name': 'UDF函数名称',
  FILE: '文件',
  UDF: 'UDF',
  'File Subdirectory': '文件子目录',
  'Please enter a function name': '请输入函数名',
  'Package Name': '包名类名',
  'Please enter a Package name': '请输入包名类名',
  Parameter: '参数',
  'Please enter a parameter': '请输入参数',
  'UDF Resources': 'UDF资源',
  'Upload Resources': '上传资源',
  Instructions: '使用说明',
  'Please enter a instructions': '请输入使用说明',
  'Please enter a UDF function name': '请输入UDF函数名称',
  'Select UDF Resources': '请选择UDF资源',
  'Class Name': '类名',
  'Jar Package': 'jar包',
  'Library Name': '库名',
  'UDF Resource Name': 'UDF资源名称',
  'File Size': '文件大小',
  Description: '描述',
  'Drag Nodes and Selected Items': '拖动节点和选中项',
  'Select Line Connection': '选择线条连接',
  'Delete selected lines or nodes': '删除选中的线或节点',
  'Full Screen': '全屏',
  Unpublished: '未发布',
  'Start Process': '启动工作流',
  'Execute from the current node': '从当前节点开始执行',
  'Recover tolerance fault process': '恢复被容错的工作流',
  'Resume the suspension process': '恢复运行流程',
  'Execute from the failed nodes': '从失败节点开始执行',
  'Complement Data': '补数',
  'Scheduling execution': '调度执行',
  'Recovery waiting thread': '恢复等待线程',
  'Submitted successfully': '提交成功',
  Executing: '正在执行',
  'Ready to pause': '准备暂停',
  'Ready to stop': '准备停止',
  'Need fault tolerance': '需要容错',
  Kill: 'Kill',
  'Waiting for thread': '等待线程',
  'Waiting for dependence': '等待依赖',
  Start: '运行',
  Copy: '复制节点',
  'Copy name': '复制名称',
  'Copy path': '复制路径',
  'Please enter keyword': '请输入关键词',
  'File Upload': '文件上传',
  'Drag the file into the current upload window': '请将文件拖拽到当前上传窗口内！',
  'Drag area upload': '拖动区域上传',
  Upload: '上传',
  'ReUpload File': '重新上传文件',
  'Please enter file name': '请输入文件名',
  'Please select the file to upload': '请选择要上传的文件',
  'Resources manage': '资源中心',
  Security: '安全中心',
  Logout: '退出',
  'No data': '查询无数据',
  'Uploading...': '文件上传中',
  'Loading...': '正在努力加载中...',
  List: '列表',
  'Unable to download without proper url': '无下载url无法下载',
  Process: '工作流',
  'Process definition': '工作流定义',
  'Task record': '任务记录',
  'Warning group manage': '告警组管理',
  'Warning instance manage': '告警实例管理',
  'Servers manage': '服务管理',
  'UDF manage': 'UDF管理',
  'Resource manage': '资源管理',
  'Function manage': '函数管理',
  'Edit password': '修改密码',
  'Ordinary users': '普通用户',
  'Create process': '创建工作流',
  'Import process': '导入工作流',
  'Timing state': '定时状态',
  Timing: '定时',
  Timezone: '时区',
  TreeView: '树形图',
  'Mailbox already exists! Recipients and copyers cannot repeat': '邮箱已存在！收件人和抄送人不能重复',
  'Mailbox input is illegal': '邮箱输入不合法',
  'Please set the parameters before starting': '启动前请先设置参数',
  Continue: '继续',
  End: '结束',
  'Node execution': '节点执行',
  'Backward execution': '向后执行',
  'Forward execution': '向前执行',
  'Execute only the current node': '仅执行当前节点',
  'Notification strategy': '通知策略',
  'Notification group': '通知组',
  'Please select a notification group': '请选择通知组',

  'Whether it is a complement process?': '是否补数',
  'Schedule date': '调度日期',
  'Mode of execution': '执行方式',
  'Serial execution': '串行执行',
  'Parallel execution': '并行执行',
  'Set parameters before timing': '定时前请先设置参数',
  'Start and stop time': '起止时间',
  'Please select time': '请选择时间',
  'Please enter crontab': '请输入crontab',
  none_1: '都不发',
  success_1: '成功发',
  failure_1: '失败发',
  All_1: '成功或失败都发',
  Toolbar: '工具栏',
  'View variables': '查看变量',
  'Format DAG': '格式化DAG',
  'Refresh DAG status': '刷新DAG状态',
  Return_1: '返回上一节点',
  'Please enter format': '请输入格式为',
  'connection parameter': '连接参数',
  'Process definition details': '流程定义详情',
  'Create process definition': '创建流程定义',
  'Scheduled task list': '定时任务列表',
  'Process instance details': '流程实例详情',
  'Create Resource': '创建资源',
  'User Center': '用户中心',
  AllStatus: '全部状态',
  None: '无',
  Name: '名称',
  'Process priority': '流程优先级',
  'Task priority': '任务优先级',
  'Task timeout alarm': '任务超时告警',
  'Timeout strategy': '超时策略',
  'Timeout alarm': '超时告警',
  'Timeout failure': '超时失败',
  'Timeout period': '超时时长',
  'Waiting Dependent complete': '等待依赖完成',
  'Waiting Dependent start': '等待依赖启动',
  'Check interval': '检查间隔',
  'Timeout must be longer than check interval': '超时时间必须比检查间隔长',
  'Timeout strategy must be selected': '超时策略必须选一个',
  'Timeout must be a positive integer': '超时时长必须为正整数',
  'Add dependency': '添加依赖',
  'Whether dry-run': '是否空跑',
  and: '且',
  or: '或',
  month: '月',
  week: '周',
  day: '日',
  hour: '时',
  Running: '正在运行',
  'Waiting for dependency to complete': '等待依赖完成',
  Selected: '已选',
  CurrentHour: '当前小时',
  Last1Hour: '前1小时',
  Last2Hours: '前2小时',
  Last3Hours: '前3小时',
  Last24Hours: '前24小时',
  today: '今天',
  Last1Days: '昨天',
  Last2Days: '前两天',
  Last3Days: '前三天',
  Last7Days: '前七天',
  ThisWeek: '本周',
  LastWeek: '上周',
  LastMonday: '上周一',
  LastTuesday: '上周二',
  LastWednesday: '上周三',
  LastThursday: '上周四',
  LastFriday: '上周五',
  LastSaturday: '上周六',
  LastSunday: '上周日',
  ThisMonth: '本月',
  LastMonth: '上月',
  LastMonthBegin: '上月初',
  LastMonthEnd: '上月末',
  'Refresh status succeeded': '刷新状态成功',
  'Queue manage': 'Yarn 队列管理',
  'Create queue': '创建队列',
  'Edit queue': '编辑队列',
  'Datasource manage': '数据源中心',
  'History task record': '历史任务记录',
  'Please go online': '不要忘记上线',
  'Queue value': '队列值',
  'Please enter queue value': '请输入队列值',
  'Worker group manage': 'Worker分组管理',
  'Create worker group': '创建Worker分组',
  'Edit worker group': '编辑Worker分组',
  'Token manage': '令牌管理',
  'Create token': '创建令牌',
  'Edit token': '编辑令牌',
  Addresses: '地址',
  'Worker Addresses': 'Worker地址',
  'Please select the worker addresses': '请选择Worker地址',
  'Failure time': '失效时间',
  'Expiration time': '失效时间',
  User: '用户',
  'Please enter token': '请输入令牌',
  'Generate token': '生成令牌',
  Monitor: '监控中心',
  Group: '分组',
  'Queue statistics': '队列统计',
  'Command status statistics': '命令状态统计',
  'Task kill': '等待kill任务',
  'Task queue': '等待执行任务',
  'Error command count': '错误指令数',
  'Normal command count': '正确指令数',
  Manage: '管理',
  'Number of connections': '连接数',
  Sent: '发送量',
  Received: '接收量',
  'Min latency': '最低延时',
  'Avg latency': '平均延时',
  'Max latency': '最大延时',
  'Node count': '节点数',
  'Query time': '当前查询时间',
  'Node self-test status': '节点自检状态',
  'Health status': '健康状态',
  'Max connections': '最大连接数',
  'Threads connections': '当前连接数',
  'Max used connections': '同时使用连接最大数',
  'Threads running connections': '数据库当前活跃连接数',
  'Worker group': 'Worker分组',
  'Please enter a positive integer greater than 0': '请输入大于 0 的正整数',
  'Pre Statement': '前置sql',
  'Post Statement': '后置sql',
  'Statement cannot be empty': '语句不能为空',
  'Process Define Count': '工作流定义数',
  'Process Instance Running Count': '正在运行的流程数',
  'command number of waiting for running': '待执行的命令数',
  'failure command number': '执行失败的命令数',
  'tasks number of waiting running': '待运行任务数',
  'task number of ready to kill': '待杀死任务数',
  'Statistics manage': '统计管理',
  statistics: '统计',
  'select tenant': '选择租户',
  'Please enter Principal': '请输入Principal',
  'Please enter the kerberos authentication parameter java.security.krb5.conf': '请输入kerberos认证参数 java.security.krb5.conf',
  'Please enter the kerberos authentication parameter login.user.keytab.username': '请输入kerberos认证参数 login.user.keytab.username',
  'Please enter the kerberos authentication parameter login.user.keytab.path': '请输入kerberos认证参数 login.user.keytab.path',
  'The start time must not be the same as the end': '开始时间和结束时间不能相同',
  'Startup parameter': '启动参数',
  'Startup type': '启动类型',
  'warning of timeout': '超时告警',
  'Next five execution times': '接下来五次执行时间',
  'Execute time': '执行时间',
  'Complement range': '补数范围',
  'Http Url': '请求地址',
  'Http Method': '请求类型',
  'Http Parameters': '请求参数',
  'Http Parameters Key': '参数名',
  'Http Parameters Position': '参数位置',
  'Http Parameters Value': '参数值',
  'Http Check Condition': '校验条件',
  'Http Condition': '校验内容',
  'Please Enter Http Url': '请填写请求地址(必填)',
  'Please Enter Http Condition': '请填写校验内容',
  'There is no data for this period of time': '该时间段无数据',
  'Worker addresses cannot be empty': 'Worker地址不能为空',
  'Please generate token': '请生成Token',
  'Please Select token': '请选择Token失效时间',
  'Spark Version': 'Spark版本',
  TargetDataBase: '目标库',
  TargetTable: '目标表',
  TargetJobName: '目标任务名',
  'Please enter Pigeon job name': '请输入Pigeon任务名',
  'Please enter the table of target': '请输入目标表名',
  'Please enter a Target Table(required)': '请输入目标表(必填)',
  SpeedByte: '限流(字节数)',
  SpeedRecord: '限流(记录数)',
  '0 means unlimited by byte': 'KB，0代表不限制',
  '0 means unlimited by count': '0代表不限制',
  'Modify User': '修改用户',
  'Whether directory': '是否文件夹',
  Yes: '是',
  No: '否',
  'Hadoop Custom Params': 'Hadoop参数',
  'Sqoop Advanced Parameters': 'Sqoop参数',
  'Sqoop Job Name': '任务名称',
  'Please enter Mysql Database(required)': '请输入Mysql数据库(必填)',
  'Please enter Mysql Table(required)': '请输入Mysql表名(必填)',
  'Please enter Columns (Comma separated)': '请输入列名，用 , 隔开',
  'Please enter Target Dir(required)': '请输入目标路径(必填)',
  'Please enter Export Dir(required)': '请输入数据源路径(必填)',
  'Please enter Hive Database(required)': '请输入Hive数据库(必填)',
  'Please enter Hive Table(required)': '请输入Hive表名(必填)',
  'Please enter hive target dir': '请输入Hive临时目录',
  'Please enter Hive Partition Keys': '请输入分区键',
  'Please enter Hive Partition Values': '请输入分区值',
  'Please enter Replace Delimiter': '请输入替换分隔符',
  'Please enter Fields Terminated': '请输入列分隔符',
  'Please enter Lines Terminated': '请输入行分隔符',
  'Please enter Concurrency': '请输入并发度',
  'Please enter Update Key': '请输入更新列',
  'Please enter Job Name(required)': '请输入任务名称(必填)',
  'Please enter Custom Shell(required)': '请输入自定义脚本',
  Direct: '流向',
  Type: '类型',
  ModelType: '模式',
  ColumnType: '列类型',
  Database: '数据库',
  Column: '列',
  'Map Column Hive': 'Hive类型映射',
  'Map Column Java': 'Java类型映射',
  'Export Dir': '数据源路径',
  'Hive partition Keys': 'Hive 分区键',
  'Hive partition Values': 'Hive 分区值',
  FieldsTerminated: '列分隔符',
  LinesTerminated: '行分隔符',
  IsUpdate: '是否更新',
  UpdateKey: '更新列',
  UpdateMode: '更新类型',
  'Target Dir': '目标路径',
  DeleteTargetDir: '是否删除目录',
  FileType: '保存格式',
  CompressionCodec: '压缩类型',
  CreateHiveTable: '是否创建新表',
  DropDelimiter: '是否删除分隔符',
  OverWriteSrc: '是否覆盖数据源',
  ReplaceDelimiter: '替换分隔符',
  Concurrency: '并发度',
  Form: '表单',
  OnlyUpdate: '只更新',
  AllowInsert: '无更新便插入',
  'Data Source': '数据来源',
  'Data Target': '数据目的',
  'All Columns': '全表导入',
  'Some Columns': '选择列',
  'Branch flow': '分支流转',
  'Custom Job': '自定义任务',
  'Custom Script': '自定义脚本',
  'Cannot select the same node for successful branch flow and failed branch flow': '成功分支流转和失败分支流转不能选择同一个节点',
  'Successful branch flow and failed branch flow are required': 'conditions节点成功和失败分支流转必填',
  'No resources exist': '不存在资源',
  'Please delete all non-existing resources': '请删除所有不存在资源',
  'Unauthorized or deleted resources': '未授权或已删除资源',
  'Please delete all non-existent resources': '请删除所有未授权或已删除资源',
  Kinship: '工作流关系',
  Reset: '重置',
  KinshipStateActive: '当前选择',
  KinshipState1: '已上线',
  KinshipState0: '工作流未上线',
  KinshipState10: '调度未上线',
  'Dag label display control': 'Dag节点名称显隐',
  Enable: '启用',
  Disable: '停用',
  'The Worker group no longer exists, please select the correct Worker group!': '该Worker分组已经不存在，请选择正确的Worker分组！',
  'Please confirm whether the workflow has been saved before downloading': '下载前请确定工作流是否已保存',
  'User name length is between 3 and 39': '用户名长度在3～39之间',
  'Timeout Settings': '超时设置',
  'Connect Timeout': '连接超时',
  'Socket Timeout': 'Socket超时',
  'Connect timeout be a positive integer': '连接超时必须为数字',
  'Socket Timeout be a positive integer': 'Socket超时必须为数字',
  ms: '毫秒',
  'Please Enter Url': '请直接填写地址,例如:127.0.0.1:7077',
  Master: 'Master',
  'Please select the seatunnel resources': '请选择 seatunnel 配置文件',
  zkDirectory: 'zk注册目录',
  'Directory detail': '查看目录详情',
  'Connection name': '连线名',
  'Current connection settings': '当前连线设置',
  'Please save the DAG before formatting': '格式化前请先保存DAG',
  'Batch copy': '批量复制',
  'Related items': '关联项目',
  'Project name is required': '项目名称必填',
  'Batch move': '批量移动',
  Version: '版本',
  'Pre tasks': '前置任务',
  'Running Memory': '运行内存',
  'Max Memory': '最大内存',
  'Min Memory': '最小内存',
  'The workflow canvas is abnormal and cannot be saved, please recreate': '该工作流画布异常，无法保存，请重新创建',
  Info: '提示',
  'Datasource userName': '所属用户',
  'Resource userName': '所属用户',
  'Environment manage': '环境管理',
  'Create environment': '创建环境',
  'Edit environment': '编辑',
  'Environment value': 'Environment value',
  'Environment Name': '环境名称',
  'Environment Code': '环境编码',
  'Environment Config': '环境配置',
  'Environment Desc': '详细描述',
  'Environment Worker Group': 'Worker组',
  'Please enter environment config': '请输入环境配置信息',
  'Please enter environment desc': '请输入详细描述',
  'Please select worker groups': '请选择Worker分组',
  condition: '条件',
  'The condition content cannot be empty': '条件内容不能为空',
  'Reference from': '使用已有任务',
  'No more...': '没有更多了...',
  'Task Definition': '任务定义',
  'Create task': '创建任务',
  'Task Type': '任务类型',
  'Process execute type': '执行策略',
  parallel: '并行',
  'Serial wait': '串行等待',
  'Serial discard': '串行抛弃',
  'Serial priority': '串行优先',
  'Recover serial wait': '串行恢复',
  IsEnableProxy: '启用代理',
  WebHook: 'Web钩子',
  webHook: 'Web钩子',
  Keyword: '关键词',
  Secret: '密钥',
  AtMobiles: '@手机号',
  AtUserIds: '@用户ID',
  IsAtAll: '@所有人',
  Proxy: '代理',
  receivers: '收件人',
  receiverCcs: '抄送人',
  transportProtocol: '邮件协议',
  serverHost: 'SMTP服务器',
  serverPort: 'SMTP端口',
  sender: '发件人',
  enableSmtpAuth: '请求认证',
  starttlsEnable: 'STARTTLS连接',
  sslEnable: 'SSL连接',
  smtpSslTrust: 'SSL证书信任',
  url: 'URL',
  requestType: '请求方式',
  headerParams: '请求头',
  bodyParams: '请求体',
  contentField: '内容字段',
  path: '脚本路径',
  userParams: '自定义参数',
  corpId: '企业ID',
  secret: '密钥',
  teamSendMsg: '群发信息',
  userSendMsg: '群员信息',
  agentId: '应用ID',
  users: '群员',
  Username: '用户名',
  username: '用户名',
  showType: '内容展示类型',
  'Please select a task type (required)': '请选择任务类型(必选)',
  layoutType: '布局类型',
  gridLayout: '网格布局',
  dagreLayout: '层次布局',
  rows: '行数',
  cols: '列数',
  processOnline: '已上线',
  searchNode: '搜索节点',
  dagScale: '缩放',
  workflowName: '工作流名称',
  scheduleStartTime: '定时开始时间',
  scheduleEndTime: '定时结束时间',
  crontabExpression: 'Crontab',
  workflowPublishStatus: '工作流上线状态',
  schedulePublishStatus: '定时状态',
  'Task group manage': '任务组管理',
  'Task group option': '任务组配置',
  'Create task group': '创建任务组',
  'Edit task group': '编辑任务组',
  'Delete task group': '删除任务组',
  'Task group code': '任务组编号',
  'Task group name': '任务组名称',
  'Task group resource pool size': '资源容量',
  'Task group resource used pool size': '已用资源',
  'Task group desc': '描述信息',
  'Task group status': '任务组状态',
  'Task group enable status': '启用',
  'Task group disable status': '不可用',
  'Please enter task group desc': '请输入任务组描述',
  'Please enter task group resource pool size': '请输入资源容量大小',
  'Task group resource pool size be a number': '资源容量大小必须大于等于1的数值',
  'Please select project': '请选择项目',
  'Task group queue': '任务组队列',
  'Task group queue priority': '组内优先级',
  'Task group queue priority be a number': '优先级必须是大于等于0的数值',
  'Task group queue force starting status': '是否强制启动',
  'Task group in queue': '是否排队中',
  'Task group queue status': '任务状态',
  'View task group queue': '查看任务组队列',
  'Task group queue the status of waiting': '等待入队',
  'Task group queue the status of queuing': '排队中',
  'Task group queue the status of releasing': '已释放',
  'Modify task group queue priority': '修改优先级',
  'Force to start task': '强制启动',
  'Priority not empty': '优先级不能为空',
  'Priority must be number': '优先级必须是数值',
  'Please select task name': '请选择节点名称',
  'Process State': '工作流状态',
  'Upstream Tasks': '上游任务',
  'and {n} more': '…等{n}个',
  'Move task': '移动任务',
  'Delete task {taskName} from process {processName}?': '将任务 {taskName} 从工作流 {processName} 中删除？',
  'Delete task completely': '彻底删除任务',
  'Please select a process': '请选择工作流',
  'Delete {taskName}?': '确定删除 {taskName} ?',
  'Please select a process (required)': '请选择工作流（必选）'
}
