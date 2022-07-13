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
  },
  workflow: {
    workflow_relation: '工作流关系',
    create_workflow: '创建工作流',
    import_workflow: '导入工作流',
    workflow_name: '工作流名称',
    current_selection: '当前选择',
    online: '已上线',
    offline: '已下线',
    refresh: '刷新',
    show_hide_label: '显示 / 隐藏标签',
    workflow_offline: '工作流下线',
    schedule_offline: '调度下线',
    schedule_start_time: '定时开始时间',
    schedule_end_time: '定时结束时间',
    crontab_expression: 'Crontab',
    workflow_publish_status: '工作流上线状态',
    schedule_publish_status: '定时状态',
    workflow_definition: '工作流定义',
    workflow_instance: '工作流实例',
    status: '状态',
    create_time: '创建时间',
    update_time: '更新时间',
    description: '描述',
    create_user: '创建用户',
    modify_user: '修改用户',
    operation: '操作',
    edit: '编辑',
    confirm: '确定',
    cancel: '取消',
    start: '运行',
    timing: '定时',
    timezone: '时区',
    up_line: '上线',
    down_line: '下线',
    copy_workflow: '复制工作流',
    cron_manage: '定时管理',
    delete: '删除',
    tree_view: '工作流树形图',
    tree_limit: '限制大小',
    export: '导出',
    batch_copy: '批量复制',
    version_info: '版本信息',
    version: '版本',
    file_upload: '文件上传',
    upload_file: '上传文件',
    upload: '上传',
    file_name: '文件名称',
    success: '成功',
    set_parameters_before_starting: '启动前请先设置参数',
    set_parameters_before_timing: '定时前请先设置参数',
    start_and_stop_time: '起止时间',
    next_five_execution_times: '接下来五次执行时间',
    execute_time: '执行时间',
    failure_strategy: '失败策略',
    notification_strategy: '通知策略',
    workflow_priority: '流程优先级',
    worker_group: 'Worker分组',
    environment_name: '环境名称',
    alarm_group: '告警组',
    complement_data: '补数',
    startup_parameter: '启动参数',
    whether_dry_run: '是否空跑',
    continue: '继续',
    end: '结束',
    none_send: '都不发',
    success_send: '成功发',
    failure_send: '失败发',
    all_send: '成功或失败都发',
    whether_complement_data: '是否是补数',
    schedule_date: '调度日期',
    select_date: '日期选择',
    enter_date: '手动输入',
    schedule_date_tips: '格式为yyyy-MM-dd HH:mm:ss，多个逗号分割',
    schedule_date_limit: '输入日期不满足<=100条',
    mode_of_execution: '执行方式',
    serial_execution: '串行执行',
    parallel_execution: '并行执行',
    parallelism: '并行度',
    custom_parallelism: '自定义并行度',
    please_enter_parallelism: '请输入并行度',
    please_choose: '请选择',
    start_time: '开始时间',
    end_time: '结束时间',
    crontab: 'Crontab',
    delete_confirm: '确定删除吗?',
    enter_name_tips: '请输入名称',
    switch_version: '切换到该版本',
    confirm_switch_version: '确定切换到该版本吗?',
    current_version: '当前版本',
    run_type: '运行类型',
    scheduling_time: '调度时间',
    duration: '运行时长',
    run_times: '运行次数',
    fault_tolerant_sign: '容错标识',
    dry_run_flag: '空跑标识',
    executor: '执行用户',
    host: '主机',
    start_process: '启动工作流',
    execute_from_the_current_node: '从当前节点开始执行',
    recover_tolerance_fault_process: '恢复被容错的工作流',
    resume_the_suspension_process: '恢复运行流程',
    execute_from_the_failed_nodes: '从失败节点开始执行',
    scheduling_execution: '调度执行',
    rerun: '重跑',
    stop: '停止',
    pause: '暂停',
    recovery_waiting_thread: '恢复等待线程',
    recover_serial_wait: '串行恢复',
    recovery_suspend: '恢复运行',
    recovery_failed: '重跑失败任务',
    gantt: '甘特图',
    name: '名称',
    all_status: '全部状态',
    submit_success: '提交成功',
    running: '正在运行',
    ready_to_pause: '准备暂停',
    ready_to_stop: '准备停止',
    failed: '失败',
    need_fault_tolerance: '需要容错',
    kill: 'Kill',
    waiting_for_thread: '等待线程',
    waiting_for_dependence: '等待依赖',
    waiting_for_dependency_to_complete: '等待依赖完成',
    delay_execution: '延时执行',
    forced_success: '强制成功',
    serial_wait: '串行等待',
    dispatch: '派发',
    pending: '挂起',
    executing: '正在执行',
    startup_type: '启动类型',
    complement_range: '补数范围',
    parameters_variables: '参数变量',
    global_parameters: '全局参数',
    local_parameters: '局部参数',
    type: '类型',
    retry_count: '重试次数',
    submit_time: '提交时间',
    refresh_status_succeeded: '刷新状态成功',
    view_log: '查看日志',
    update_log_success: '更新日志成功',
    no_more_log: '暂无更多日志',
    no_log: '暂无日志',
    loading_log: '正在努力请求日志中...',
    close: '关闭',
    download_log: '下载日志',
    refresh_log: '刷新日志',
    enter_full_screen: '进入全屏',
    cancel_full_screen: '取消全屏',
    task_state: '任务状态',
    mode_of_dependent: '依赖模式',
    open: '打开',
    project_name_required: '项目名称必填',
    related_items: '关联项目',
    project_name: '项目名称',
    project_tips: '请选择项目',
    workflow_relation_no_data_result_title: '工作流关系不存在',
    workflow_relation_no_data_result_desc:
      '目前没有任何工作流，请先创建工作流，再访问该页面'
  },
  task: {
    cancel_full_screen: '取消全屏',
    enter_full_screen: '全屏',
    current_task_settings: '当前任务设置',
    online: '已上线',
    offline: '已下线',
    task_name: '任务名称',
    task_type: '任务类型',
    create_task: '创建任务',
    workflow_instance: '工作流实例',
    workflow_name: '工作流名称',
    workflow_name_tips: '请选择工作流名称',
    workflow_state: '工作流状态',
    version: '版本',
    current_version: '当前版本',
    switch_version: '切换到该版本',
    confirm_switch_version: '确定切换到该版本吗?',
    description: '描述',
    move: '移动',
    upstream_tasks: '上游任务',
    executor: '执行用户',
    node_type: '节点类型',
    state: '状态',
    submit_time: '提交时间',
    start_time: '开始时间',
    create_time: '创建时间',
    update_time: '更新时间',
    end_time: '结束时间',
    duration: '运行时间',
    retry_count: '重试次数',
    dry_run_flag: '空跑标识',
    host: '主机',
    operation: '操作',
    edit: '编辑',
    delete: '删除',
    delete_confirm: '确定删除吗?',
    forced_success: '强制成功',
    view_log: '查看日志',
    download_log: '下载日志',
    refresh: '刷新'
  },
  dag: {
    create: '创建工作流',
    search: '搜索',
    download_png: '下载工作流图片',
    fullscreen_open: '全屏',
    fullscreen_close: '退出全屏',
    save: '保存',
    close: '关闭',
    format: '格式化',
    refresh_dag_status: '刷新DAG状态',
    layout_type: '布局类型',
    grid_layout: '网格布局',
    dagre_layout: '层次布局',
    rows: '行数',
    cols: '列数',
    copy_success: '复制成功',
    workflow_name: '工作流名称',
    description: '描述',
    tenant: '租户',
    timeout_alert: '超时告警',
    process_execute_type: '执行策略',
    parallel: '并行',
    serial_wait: '串行等待',
    serial_discard: '串行抛弃',
    serial_priority: '串行优先',
    recover_serial_wait: '串行恢复',
    global_variables: '全局变量',
    basic_info: '基本信息',
    minute: '分',
    key: '键',
    value: '值',
    success: '成功',
    delete_cell: '删除选中的线或节点',
    online_directly: '是否上线工作流定义',
    update_directly: '是否更新工作流定义',
    dag_name_empty: 'DAG图名称不能为空',
    positive_integer: '请输入大于 0 的正整数',
    prop_empty: '自定义参数prop不能为空',
    prop_repeat: 'prop中有重复',
    node_not_created: '未创建节点保存失败',
    copy_name: '复制名称',
    view_variables: '查看变量',
    startup_parameter: '启动参数',
    online: '已上线'
  },
  node: {
    return_back: '返回上一节点',
    current_node_settings: '当前节点设置',
    instructions: '使用说明',
    view_history: '查看历史',
    view_log: '查看日志',
    enter_this_child_node: '进入该子节点',
    name: '节点名称',
    task_name: '任务名称',
    task_name_tips: '任务名称（必填）',
    name_tips: '请输入名称(必填)',
    task_type: '任务类型',
    task_type_tips: '请选择任务类型(必选)',
    workflow_name: '工作流名称',
    workflow_name_tips: '请选择工作流(必选)',
    child_node: '子节点',
    child_node_tips: '请选择子节点(必选)',
    run_flag: '运行标志',
    normal: '正常',
    prohibition_execution: '禁止执行',
    description: '描述',
    description_tips: '请输入描述',
    task_priority: '任务优先级',
    worker_group: 'Worker分组',
    worker_group_tips: '该Worker分组已经不存在，请选择正确的Worker分组！',
    environment_name: '环境名称',
    task_group_name: '任务组名称',
    task_group_queue_priority: '组内优先级',
    number_of_failed_retries: '失败重试次数',
    cpu_quota: 'CPU配额',
    memory_max: '最大内存',
    times: '次',
    failed_retry_interval: '失败重试间隔',
    minute: '分',
    delay_execution_time: '延时执行时间',
    namespace_cluster: '命名空间(集群)',
    min_cpu: '最小cpu',
    min_memory: '最小内存',
    cores: '核',
    mb: 'MB',
    image: '镜像',
    image_tips: '请输入镜像',
    min_memory_tips: '请输入最小内存',
    state: '状态',
    branch_flow: '分支流转',
    cancel: '取消',
    loading: '正在努力加载中...',
    confirm: '确定',
    success: '成功',
    failed: '失败',
    backfill_tips: '新创建子工作流还未执行，不能进入子工作流',
    task_instance_tips: '该任务还未执行，不能进入子工作流',
    branch_tips: '成功分支流转和失败分支流转不能选择同一个节点',
    timeout_alarm: '超时告警',
    timeout_strategy: '超时策略',
    timeout_strategy_tips: '超时策略必须选一个',
    timeout_failure: '超时失败',
    timeout_period: '超时时长',
    timeout_period_tips: '超时时长必须为正整数',
    script: '脚本',
    script_tips: '请输入脚本（必填）',
    init_script: '初始化脚本',
    init_script_tips: '请输入初始化脚本',
    resources: '资源',
    resources_tips: '请选择资源',
    resources_limit_tips: '请重新选择，资源个数限制:',
    no_resources_tips: '请删除所有未授权或已删除资源',
    useless_resources_tips: '未授权或已删除资源',
    custom_parameters: '自定义参数',
    copy_failed: '该浏览器不支持自动复制',
    prop_tips: 'prop(必填)',
    prop_repeat: 'prop中有重复',
    value_tips: 'value(选填)',
    value_required_tips: 'value(必填)',
    pre_tasks: '前置任务',
    program_type: '程序类型',
    spark_version: 'Spark版本',
    main_class: '主函数的Class',
    main_class_tips: '请填写主函数的Class',
    main_package: '主程序包',
    main_package_tips: '请选择主程序包',
    deploy_mode: '部署方式',
    app_name: '任务名称',
    app_name_tips: '请输入任务名称(选填)',
    driver_cores: 'Driver核心数',
    driver_cores_tips: '请输入Driver核心数',
    driver_memory: 'Driver内存数',
    driver_memory_tips: '请输入Driver内存数',
    executor_number: 'Executor数量',
    executor_number_tips: '请输入Executor数量',
    executor_memory: 'Executor内存数',
    executor_memory_tips: '请输入Executor内存数',
    executor_cores: 'Executor核心数',
    executor_cores_tips: '请输入Executor核心数',
    main_arguments: '主程序参数',
    main_arguments_tips: '请输入主程序参数',
    option_parameters: '选项参数',
    option_parameters_tips: '请输入选项参数',
    positive_integer_tips: '应为正整数',
    flink_version: 'Flink版本',
    job_manager_memory: 'JobManager内存数',
    job_manager_memory_tips: '请输入JobManager内存数',
    task_manager_memory: 'TaskManager内存数',
    task_manager_memory_tips: '请输入TaskManager内存数',
    slot_number: 'Slot数量',
    slot_number_tips: '请输入Slot数量',
    parallelism: '并行度',
    custom_parallelism: '自定义并行度',
    parallelism_tips: '请输入并行度',
    parallelism_number_tips: '并行度必须为正整数',
    parallelism_complement_tips:
      '如果存在大量任务需要补数时,可以利用自定义并行度将补数的任务线程设置成合理的数值,避免对服务器造成过大的影响',
    task_manager_number: 'TaskManager数量',
    task_manager_number_tips: '请输入TaskManager数量',
    http_url: '请求地址',
    http_url_tips: '请填写请求地址(必填)',
    http_url_validator: '请求地址需包含http或者https',
    http_method: '请求类型',
    http_parameters: '请求参数',
    http_check_condition: '校验条件',
    http_condition: '校验内容',
    http_condition_tips: '请填写校验内容',
    timeout_settings: '超时设置',
    connect_timeout: '连接超时',
    ms: '毫秒',
    socket_timeout: 'Socket超时',
    status_code_default: '默认响应码200',
    status_code_custom: '自定义响应码',
    body_contains: '内容包含',
    body_not_contains: '内容不包含',
    http_parameters_position: '参数位置',
    target_task_name: '目标任务名',
    target_task_name_tips: '请输入Pigeon任务名(必填)',
    datasource_type: '数据源类型',
    datasource_instances: '数据源实例',
    sql_type: 'SQL类型',
    sql_type_query: '查询',
    sql_type_non_query: '非查询',
    sql_statement: 'SQL语句',
    pre_sql_statement: '前置SQL语句',
    post_sql_statement: '后置SQL语句',
    sql_input_placeholder: '请输入非查询SQL语句',
    sql_empty_tips: '语句不能为空',
    procedure_method: 'SQL语句',
    procedure_method_tips: '请输入存储脚本',
    procedure_method_snippet:
      '--请输入存储脚本 \n\n--调用存储过程: call <procedure-name>[(<arg1>,<arg2>, ...)] \n\n--调用存储函数：?= call <procedure-name>[(<arg1>,<arg2>, ...)]',
    start: '运行',
    edit: '编辑',
    copy: '复制节点',
    delete: '删除',
    custom_job: '自定义任务',
    custom_script: '自定义脚本',
    sqoop_job_name: '任务名称',
    sqoop_job_name_tips: '请输入任务名称(必填)',
    direct: '流向',
    hadoop_custom_params: 'Hadoop参数',
    sqoop_advanced_parameters: 'Sqoop参数',
    data_source: '数据来源',
    type: '类型',
    datasource: '数据源',
    datasource_tips: '请选择数据源',
    model_type: '模式',
    form: '表单',
    table: '表名',
    table_tips: '请输入Mysql表名(必填)',
    column_type: '列类型',
    all_columns: '全表导入',
    some_columns: '选择列',
    column: '列',
    column_tips: '请输入列名，用 , 隔开',
    database: '数据库',
    database_tips: '请输入Hive数据库(必填)',
    hive_table_tips: '请输入Hive表名(必填)',
    hive_partition_keys: 'Hive 分区键',
    hive_partition_keys_tips: '请输入分区键',
    hive_partition_values: 'Hive 分区值',
    hive_partition_values_tips: '请输入分区值',
    export_dir: '数据源路径',
    export_dir_tips: '请输入数据源路径(必填)',
    sql_statement_tips: 'SQL语句(必填)',
    map_column_hive: 'Hive类型映射',
    map_column_java: 'Java类型映射',
    data_target: '数据目的',
    create_hive_table: '是否创建新表',
    drop_delimiter: '是否删除分隔符',
    over_write_src: '是否覆盖数据源',
    hive_target_dir: 'Hive目标路径',
    hive_target_dir_tips: '请输入Hive临时目录',
    replace_delimiter: '替换分隔符',
    replace_delimiter_tips: '请输入替换分隔符',
    target_dir: '目标路径',
    target_dir_tips: '请输入目标路径(必填)',
    delete_target_dir: '是否删除目录',
    compression_codec: '压缩类型',
    file_type: '保存格式',
    fields_terminated: '列分隔符',
    fields_terminated_tips: '请输入列分隔符',
    lines_terminated: '行分隔符',
    lines_terminated_tips: '请输入行分隔符',
    is_update: '是否更新',
    update_key: '更新列',
    update_key_tips: '请输入更新列',
    update_mode: '更新类型',
    only_update: '只更新',
    allow_insert: '无更新便插入',
    concurrency: '并发度',
    concurrency_tips: '请输入并发度',
    sea_tunnel_master: 'Master',
    sea_tunnel_master_url: 'Master URL',
    sea_tunnel_queue: '队列',
    sea_tunnel_master_url_tips: '请直接填写地址,例如:127.0.0.1:7077',
    add_pre_task_check_condition: '添加前置检查条件',
    switch_condition: '条件',
    switch_branch_flow: '分支流转',
    switch_branch_flow_tips: '请选择分支流转',
    and: '且',
    or: '或',
    datax_custom_template: '自定义模板',
    datax_json_template: 'JSON',
    datax_target_datasource_type: '目标源类型',
    datax_target_database: '目标源实例',
    datax_target_table: '目标表',
    datax_target_table_tips: '请输入目标表名',
    datax_target_database_pre_sql: '目标库前置SQL',
    datax_target_database_post_sql: '目标库后置SQL',
    datax_non_query_sql_tips: '请输入非查询SQL语句',
    datax_job_speed_byte: '限流(字节数)',
    datax_job_speed_byte_info: '(KB，0代表不限制)',
    datax_job_speed_record: '限流(记录数)',
    datax_job_speed_record_info: '(0代表不限制)',
    datax_job_runtime_memory: '运行内存',
    datax_job_runtime_memory_xms: '最小内存',
    datax_job_runtime_memory_xmx: '最大内存',
    datax_job_runtime_memory_unit: 'G',
    chunjun_custom_template: '自定义模板',
    chunjun_json_template: 'JSON',
    current_hour: '当前小时',
    last_1_hour: '前1小时',
    last_2_hour: '前2小时',
    last_3_hour: '前3小时',
    last_24_hour: '前24小时',
    today: '今天',
    last_1_days: '昨天',
    last_2_days: '前两天',
    last_3_days: '前三天',
    last_7_days: '前七天',
    this_week: '本周',
    last_week: '上周',
    last_monday: '上周一',
    last_tuesday: '上周二',
    last_wednesday: '上周三',
    last_thursday: '上周四',
    last_friday: '上周五',
    last_saturday: '上周六',
    last_sunday: '上周日',
    this_month: '本月',
    this_month_begin: '本月初',
    last_month: '上月',
    last_month_begin: '上月初',
    last_month_end: '上月末',
    month: '月',
    week: '周',
    day: '日',
    hour: '时',
    add_dependency: '添加依赖',
    waiting_dependent_start: '等待依赖启动',
    check_interval: '检查间隔',
    waiting_dependent_complete: '等待依赖完成',
    project_name: '项目名称',
    project_name_tips: '项目名称（必填）',
    process_name: '工作流名称',
    process_name_tips: '工作流名称（必填）',
    cycle_time: '时间周期',
    cycle_time_tips: '时间周期（必填）',
    date_tips: '日期（必填）',
    rule_name: '规则名称',
    null_check: '空值检测',
    custom_sql: '自定义SQL',
    multi_table_accuracy: '多表准确性',
    multi_table_value_comparison: '两表值比对',
    field_length_check: '字段长度校验',
    uniqueness_check: '唯一性校验',
    regexp_check: '正则表达式',
    timeliness_check: '及时性校验',
    enumeration_check: '枚举值校验',
    table_count_check: '表行数校验',
    src_connector_type: '源数据类型',
    src_datasource_id: '源数据源',
    src_table: '源数据表',
    src_filter: '源表过滤条件',
    src_field: '源表检测列',
    statistics_name: '实际值名',
    check_type: '校验方式',
    operator: '校验操作符',
    threshold: '阈值',
    failure_strategy: '失败策略',
    target_connector_type: '目标数据类型',
    target_datasource_id: '目标数据源',
    target_table: '目标数据表',
    target_filter: '目标表过滤条件',
    mapping_columns: 'ON语句',
    statistics_execute_sql: '实际值计算SQL',
    comparison_name: '期望值名',
    comparison_execute_sql: '期望值计算SQL',
    comparison_type: '期望值类型',
    writer_connector_type: '输出数据类型',
    writer_datasource_id: '输出数据源',
    target_field: '目标表检测列',
    field_length: '字段长度限制',
    logic_operator: '逻辑操作符',
    regexp_pattern: '正则表达式',
    deadline: '截止时间',
    datetime_format: '时间格式',
    enum_list: '枚举值列表',
    begin_time: '起始时间',
    fix_value: '固定值',
    required: '必填',
    emr_flow_define_json: 'jobFlowDefineJson',
    emr_flow_define_json_tips: '请输入工作流定义',
    emr_steps_define_json: 'stepsDefineJson',
    emr_steps_define_json_tips: '请输入EMR步骤定义',
    segment_separator: '分段执行符号',
    segment_separator_tips: '请输入分段执行符号',
    zeppelin_note_id: 'zeppelin_note_id',
    zeppelin_note_id_tips: '请输入zeppelin note id',
    zeppelin_paragraph_id: 'zeppelin_paragraph_id',
    zeppelin_paragraph_id_tips: '请输入zeppelin paragraph id',
    zeppelin_parameters: 'parameters',
    zeppelin_parameters_tips: '请输入zeppelin dynamic form参数',
    jupyter_conda_env_name: 'condaEnvName',
    jupyter_conda_env_name_tips: '请输入papermill所在的conda环境名',
    jupyter_input_note_path: 'inputNotePath',
    jupyter_input_note_path_tips: '请输入jupyter note的输入路径',
    jupyter_output_note_path: 'outputNotePath',
    jupyter_output_note_path_tips: '请输入jupyter note的输出路径',
    jupyter_parameters: 'parameters',
    jupyter_parameters_tips: '请输入jupyter parameterization参数',
    jupyter_kernel: 'kernel',
    jupyter_kernel_tips: '请输入jupyter kernel名',
    jupyter_engine: 'engine',
    jupyter_engine_tips: '请输入引擎名称',
    jupyter_execution_timeout: 'executionTimeout',
    jupyter_execution_timeout_tips: '请输入jupyter note cell的执行最长时间',
    jupyter_start_timeout: 'startTimeout',
    jupyter_start_timeout_tips: '请输入jupyter kernel的启动最长时间',
    jupyter_others: 'others',
    jupyter_others_tips: '请输入papermill的其他参数',
    mlflow_algorithm: '算法',
    mlflow_algorithm_tips: 'svm',
    mlflow_params: '参数',
    mlflow_params_tips: ' ',
    mlflow_searchParams: '参数搜索空间',
    mlflow_searchParams_tips: ' ',
    mlflow_isSearchParams: '是否搜索参数',
    mlflow_dataPath: '数据路径',
    mlflow_dataPath_tips:
      ' 文件/文件夹的绝对路径, 若文件需以.csv结尾, 文件夹需包含train.csv和test.csv ',
    mlflow_dataPath_error_tips: ' 数据路径不能为空 ',
    mlflow_experimentName: '实验名称',
    mlflow_experimentName_tips: 'experiment_001',
    mlflow_registerModel: '注册模型',
    mlflow_modelName: '注册的模型名称',
    mlflow_modelName_tips: 'model_001',
    mlflow_mlflowTrackingUri: 'MLflow Tracking Server URI',
    mlflow_mlflowTrackingUri_tips: 'http://127.0.0.1:5000',
    mlflow_mlflowTrackingUri_error_tips: ' MLflow Tracking Server URI 不能为空',
    mlflow_jobType: '任务类型',
    mlflow_automlTool: 'AutoML工具',
    mlflow_taskType: 'MLflow 任务类型',
    mlflow_deployType: '部署类型',
    mlflow_deployModelKey: '部署的模型URI',
    mlflow_deployPort: '监听端口',
    mlflowProjectRepository: '运行仓库',
    mlflowProjectRepository_tips: '可以为github仓库或worker上的路径',
    mlflowProjectVersion: '项目版本',
    mlflowProjectVersion_tips: '项目git版本',
    mlflow_cpuLimit: '最大cpu限制',
    mlflow_memoryLimit: '最大内存限制',
    openmldb_zk_address: 'zookeeper地址',
    openmldb_zk_address_tips: '请输入zookeeper地址',
    openmldb_zk_path: 'zookeeper路径',
    openmldb_zk_path_tips: '请输入zookeeper路径',
    openmldb_execute_mode: '执行模式',
    openmldb_execute_mode_tips: '请选择执行模式',
    openmldb_execute_mode_offline: '离线',
    openmldb_execute_mode_online: '在线',
    dvc_task_type: 'DVC任务类型',
    dvc_repository: 'DVC仓库',
    dvc_repository_tips: '请输入DVC仓库地址',
    dvc_version: '数据版本',
    dvc_version_tips: '数据版本标识，会以git tag的形式标记',
    dvc_data_location: 'DVC仓库中的数据路径',
    dvc_message: '提交信息',
    dvc_load_save_data_path: 'Worker中数据路径',
    dvc_store_url: '数据存储地址',
    dvc_empty_tips: '该参数不能为空',
    send_email: '发送邮件',
    log_display: '日志显示',
    rows_of_result: '行查询结果',
    title: '主题',
    title_tips: '请输入邮件主题',
    alarm_group: '告警组',
    alarm_group_tips: '告警组必填',
    integer_tips: '请输入一个正整数',
    sql_parameter: 'sql参数',
    format_tips: '请输入格式为',
    udf_function: 'UDF函数',
    unlimited: '不限制',
    please_select_source_connector_type: '请选择源数据类型',
    please_select_source_datasource_id: '请选择源数据源',
    please_enter_source_table_name: '请选择源数据表',
    please_enter_filter_expression: '请输入源表过滤条件',
    please_enter_column_only_single_column_is_supported: '请选择源表检测列',
    please_enter_threshold_number_is_needed: '请输入阈值',
    please_enter_comparison_title: '请选择期望值类型',
    custom_config: '自定义配置',
    engine: '引擎',
    engine_tips: '请选择引擎',
    run_mode: '运行模式',
    dinky_address: 'dinky 地址',
    dinky_address_tips: '请输入 Dinky 地址',
    dinky_task_id: 'dinky 作业ID',
    dinky_task_id_tips: '请输入作业 ID',
    dinky_online: '是否上线作业'
  }
}
