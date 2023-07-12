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
  task_result: {
    task_name: '任务名称',
    workflow_instance: '工作流实例',
    rule_type: '规则类型',
    rule_name: '规则名称',
    state: '状态',
    actual_value: '实际值',
    excepted_value: '期望值',
    check_type: '检测类型',
    operator: '操作符',
    threshold: '阈值',
    failure_strategy: '失败策略',
    excepted_value_type: '期望值类型',
    error_output_path: '错误数据路径',
    username: '用户名',
    create_time: '创建时间',
    update_time: '更新时间',
    undone: '未完成',
    success: '成功',
    failure: '失败',
    single_table: '单表检测',
    single_table_custom_sql: '自定义SQL',
    multi_table_accuracy: '多表准确性',
    multi_table_comparison: '两表值对比',
    expected_and_actual_or_expected: '(期望值-实际值)/实际值 x 100%',
    expected_and_actual: '期望值-实际值',
    actual_and_expected: '实际值-期望值',
    actual_or_expected: '实际值/期望值 x 100%'
  },
  rule: {
    actions: '操作',
    name: '规则名称',
    type: '规则类型',
    username: '用户名',
    create_time: '创建时间',
    update_time: '更新时间',
    input_item: '规则输入项',
    view_input_item: '查看规则输入项信息',
    input_item_title: '输入项标题',
    input_item_placeholder: '输入项占位符',
    input_item_type: '输入项类型',
    src_connector_type: '源数据类型',
    src_datasource_id: '源数据源',
    src_database: '源数据库',
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
    target_database: '目标数据库',
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
    null_check: '空值检测',
    custom_sql: '自定义SQL',
    single_table: '单表检测',
    multi_table_accuracy: '多表准确性',
    multi_table_value_comparison: '两表值比对',
    field_length_check: '字段长度校验',
    uniqueness_check: '唯一性校验',
    regexp_check: '正则表达式',
    timeliness_check: '及时性校验',
    enumeration_check: '枚举值校验',
    table_count_check: '表行数校验',
    all: '全部',
    FixValue: '固定值',
    DailyAvg: '日均值',
    WeeklyAvg: '周均值',
    MonthlyAvg: '月均值',
    Last7DayAvg: '最近7天均值',
    Last30DayAvg: '最近30天均值',
    SrcTableTotalRows: '源表总行数',
    TargetTableTotalRows: '目标表总行数'
  }
}
