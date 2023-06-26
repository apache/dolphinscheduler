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
    task_name: 'Task Name',
    workflow_instance: 'Workflow Instance',
    rule_type: 'Rule Type',
    rule_name: 'Rule Name',
    state: 'State',
    actual_value: 'Actual Value',
    excepted_value: 'Excepted Value',
    check_type: 'Check Type',
    operator: 'Operator',
    threshold: 'Threshold',
    failure_strategy: 'Failure Strategy',
    excepted_value_type: 'Excepted Value Type',
    error_output_path: 'Error Output Path',
    username: 'Username',
    create_time: 'Create Time',
    update_time: 'Update Time',
    undone: 'Undone',
    success: 'Success',
    failure: 'Failure',
    single_table: 'Single Table',
    single_table_custom_sql: 'Single Table Custom Sql',
    multi_table_accuracy: 'Multi Table Accuracy',
    multi_table_comparison: 'Multi Table Comparison',
    expected_and_actual_or_expected: '(Expected - Actual) / Expected x 100%',
    expected_and_actual: 'Expected - Actual',
    actual_and_expected: 'Actual - Expected',
    actual_or_expected: 'Actual / Expected x 100%'
  },
  rule: {
    actions: 'Actions',
    name: 'Rule Name',
    type: 'Rule Type',
    username: 'User Name',
    create_time: 'Create Time',
    update_time: 'Update Time',
    input_item: 'Rule input item',
    view_input_item: 'View input items',
    input_item_title: 'Input item title',
    input_item_placeholder: 'Input item placeholder',
    input_item_type: 'Input item type',
    src_connector_type: 'SrcConnType',
    src_datasource_id: 'SrcSource',
    src_database: 'SrcDatabase',
    src_table: 'SrcTable',
    src_filter: 'SrcFilter',
    src_field: 'SrcField',
    statistics_name: 'ActualValName',
    check_type: 'CheckType',
    operator: 'Operator',
    threshold: 'Threshold',
    failure_strategy: 'FailureStrategy',
    target_connector_type: 'TargetConnType',
    target_datasource_id: 'TargetSourceId',
    target_database: 'TargetDatabase',
    target_table: 'TargetTable',
    target_filter: 'TargetFilter',
    mapping_columns: 'OnClause',
    statistics_execute_sql: 'ActualValExecSql',
    comparison_name: 'ExceptedValName',
    comparison_execute_sql: 'ExceptedValExecSql',
    comparison_type: 'ExceptedValType',
    writer_connector_type: 'WriterConnType',
    writer_datasource_id: 'WriterSourceId',
    target_field: 'TargetField',
    field_length: 'FieldLength',
    logic_operator: 'LogicOperator',
    regexp_pattern: 'RegexpPattern',
    deadline: 'Deadline',
    datetime_format: 'DatetimeFormat',
    enum_list: 'EnumList',
    begin_time: 'BeginTime',
    fix_value: 'FixValue',
    null_check: 'NullCheck',
    custom_sql: 'Custom Sql',
    single_table: 'Single Table',
    single_table_custom_sql: 'Single Table Custom Sql',
    multi_table_accuracy: 'Multi Table Accuracy',
    multi_table_value_comparison: 'Multi Table Compare',
    field_length_check: 'FieldLengthCheck',
    uniqueness_check: 'UniquenessCheck',
    regexp_check: 'RegexpCheck',
    timeliness_check: 'TimelinessCheck',
    enumeration_check: 'EnumerationCheck',
    table_count_check: 'TableCountCheck',
    all: 'All',
    FixValue: 'FixValue',
    DailyAvg: 'DailyAvg',
    WeeklyAvg: 'WeeklyAvg',
    MonthlyAvg: 'MonthlyAvg',
    Last7DayAvg: 'Last7DayAvg',
    Last30DayAvg: 'Last30DayAvg',
    SrcTableTotalRows: 'SrcTableTotalRows',
    TargetTableTotalRows: 'TargetTableTotalRows'
  }
}
