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

import { VNode } from 'vue'
import type { SelectOption } from 'naive-ui'
import type { TaskType } from '@/views/projects/task/constants/task-type'
import type { IDataBase } from '@/service/modules/data-source/types'
import type {
  IFormItem,
  IJsonItem,
  FormRules,
  IJsonItemParams
} from '@/components/form/types'
export type { EditWorkflowDefinition } from '@/views/projects/workflow/components/dag/types'
export type {
  IWorkflowTaskInstance,
  WorkflowInstance
} from '@/views/projects/workflow/components/dag/types'
export type { IResource, ProgramType, IMainJar } from '@/store/project/types'
export type { ITaskState } from '@/common/types'

type SourceType = 'MYSQL' | 'HDFS' | 'HIVE'
type ModelType = 'import' | 'export'
type RelationType = 'AND' | 'OR'
type ITaskType = TaskType

interface IOption {
  label: string
  value: string | number
}

interface ITaskPriorityOption extends SelectOption {
  icon: VNode
  color: string
}
interface IEnvironmentNameOption {
  label: string
  value: string
  workerGroups?: string[]
}

interface ILocalParam {
  prop: string
  direct?: string
  type?: string
  value?: string
}

interface IResponseJsonItem extends Omit<IJsonItemParams, 'type'> {
  type: 'input' | 'select' | 'radio' | 'group'
  emit: 'change'[]
}

interface IDependpendItem {
  depTaskCode?: number
  status?: 'SUCCESS' | 'FAILURE'
  definitionCodeOptions?: IOption[]
  depTaskCodeOptions?: IOption[]
  dateOptions?: IOption[]
  projectCode?: number
  definitionCode?: number
  cycle?: 'month' | 'week' | 'day' | 'hour'
  dateValue?: string
}

interface IDependTask {
  condition?: string
  nextNode?: number
  relation?: RelationType
  dependItemList?: IDependpendItem[]
}

interface ISwitchResult {
  dependTaskList?: IDependTask[]
  nextNode?: number
}

interface ISourceItem {
  id: number
}

interface ISqoopTargetData {
  targetHiveDatabase?: string
  targetHiveTable?: string
  targetHiveCreateTable?: boolean
  targetHiveDropDelimiter?: boolean
  targetHiveOverWrite?: boolean
  targetHiveTargetDir?: string
  targetHiveReplaceDelimiter?: string
  targetHivePartitionKey?: string
  targetHivePartitionValue?: string
  targetHdfsTargetPath?: string
  targetHdfsDeleteTargetDir?: boolean
  targetHdfsCompressionCodec?: string
  targetHdfsFileType?: string
  targetHdfsFieldsTerminated?: string
  targetHdfsLinesTerminated?: string
  targetMysqlType?: string
  targetMysqlDatasource?: string
  targetMysqlTable?: string
  targetMysqlColumns?: string
  targetMysqlFieldsTerminated?: string
  targetMysqlLinesTerminated?: string
  targetMysqlIsUpdate?: string
  targetMysqlTargetUpdateKey?: string
  targetMysqlUpdateMode?: string
}

interface ISqoopSourceData {
  srcQueryType?: '1' | '0'
  srcTable?: string
  srcColumnType?: '1' | '0'
  srcColumns?: string
  sourceMysqlSrcQuerySql?: string
  sourceMysqlType?: string
  sourceMysqlDatasource?: string
  mapColumnHive?: ILocalParam[]
  mapColumnJava?: ILocalParam[]
  sourceHdfsExportDir?: string
  sourceHiveDatabase?: string
  sourceHiveTable?: string
  sourceHivePartitionKey?: string
  sourceHivePartitionValue?: string
}

interface ISqoopTargetParams {
  hiveDatabase?: string
  hiveTable?: string
  createHiveTable?: boolean
  dropDelimiter?: boolean
  hiveOverWrite?: boolean
  hiveTargetDir?: string
  replaceDelimiter?: string
  hivePartitionKey?: string
  hivePartitionValue?: string
  targetPath?: string
  deleteTargetDir?: boolean
  compressionCodec?: string
  fileType?: string
  fieldsTerminated?: string
  linesTerminated?: string
  targetType?: string
  targetDatasource?: string
  targetTable?: string
  targetColumns?: string
  isUpdate?: string
  targetUpdateKey?: string
  targetUpdateMode?: string
}
interface ISqoopSourceParams {
  srcTable?: string
  srcColumnType?: '1' | '0'
  srcColumns?: string
  srcQuerySql?: string
  srcQueryType?: '1' | '0'
  srcType?: string
  srcDatasource?: string
  mapColumnHive?: ILocalParam[]
  mapColumnJava?: ILocalParam[]
  exportDir?: string
  hiveDatabase?: string
  hiveTable?: string
  hivePartitionKey?: string
  hivePartitionValue?: string
}
interface ISparkParameters {
  deployMode?: string
  driverCores?: number
  driverMemory?: string
  executorCores?: number
  executorMemory?: string
  numExecutors?: number
  others?: string
}

interface IRuleParameters {
  check_type?: string
  comparison_execute_sql?: string
  comparison_name?: string
  comparison_type?: number
  failure_strategy?: string
  operator?: string
  src_connector_type?: number
  src_datasource_id?: number
  src_table?: string
  src_filter?: string
  src_field?: string
  statistics_execute_sql?: string
  statistics_name?: string
  target_connector_type?: number
  target_datasource_id?: number
  target_table?: string
  threshold?: string
}

interface ITaskParams {
  resourceList?: ISourceItem[]
  mainJar?: ISourceItem
  localParams?: ILocalParam[]
  rawScript?: string
  initScript?: string
  programType?: string
  sparkVersion?: string
  flinkVersion?: string
  jobManagerMemory?: string
  taskManagerMemory?: string
  slot?: number
  taskManager?: number
  parallelism?: number
  mainClass?: string
  deployMode?: string
  appName?: string
  driverCores?: number
  driverMemory?: string
  numExecutors?: number
  executorMemory?: string
  executorCores?: number
  mainArgs?: string
  others?: string
  httpMethod?: string
  httpCheckCondition?: string
  httpParams?: []
  url?: string
  condition?: string
  connectTimeout?: number
  socketTimeout?: number
  type?: string
  datasource?: string
  sql?: string
  sqlType?: string
  segmentSeparator?: string
  sendEmail?: boolean
  displayRows?: number
  title?: string
  groupId?: string
  preStatements?: string[]
  postStatements?: string[]
  method?: string
  jobType?: 'CUSTOM' | 'TEMPLATE'
  customShell?: string
  jobName?: string
  hadoopCustomParams?: ILocalParam[]
  sqoopAdvancedParams?: ILocalParam[]
  concurrency?: number
  modelType?: ModelType
  sourceType?: SourceType
  targetType?: SourceType
  targetParams?: string
  sourceParams?: string
  queue?: string
  master?: string
  switchResult?: ISwitchResult
  dependTaskList?: IDependTask[]
  nextNode?: number
  dependence?: {
    relation?: RelationType
    dependTaskList?: IDependTask[]
  }
  customConfig?: number
  json?: string
  dsType?: string
  dataSource?: number
  dtType?: string
  dataTarget?: number
  targetTable?: string
  jobSpeedByte?: number
  jobSpeedRecord?: number
  xms?: number
  xmx?: number
  sparkParameters?: ISparkParameters
  ruleId?: number
  ruleInputParameter?: IRuleParameters
  jobFlowDefineJson?: string
  zeppelinNoteId?: string
  zeppelinParagraphId?: string
  noteId?: string
  paragraphId?: string
  processDefinitionCode?: number
  conditionResult?: {
    successNode?: number[]
    failedNode?: number[]
  }
  udfs?: string
  connParams?: string
  targetJobName?: string
}

interface INodeData
  extends Omit<
      ITaskParams,
      | 'resourceList'
      | 'mainJar'
      | 'targetParams'
      | 'sourceParams'
      | 'dependence'
      | 'sparkParameters'
      | 'conditionResult'
      | 'udfs'
      | 'customConfig'
    >,
    ISqoopTargetData,
    ISqoopSourceData,
    IRuleParameters {
  id?: string
  taskType?: ITaskType
  processName?: number
  delayTime?: number
  description?: string
  environmentCode?: number | null
  failRetryInterval?: number
  failRetryTimes?: number
  flag?: 'YES' | 'NO'
  taskGroupId?: number
  taskGroupPriority?: number
  taskPriority?: string
  timeout?: number
  timeoutFlag?: boolean
  timeoutNotifyStrategy?: string[]
  workerGroup?: string
  code?: number
  name?: string
  preTasks?: number[]
  preTaskOptions?: []
  postTaskOptions?: []
  resourceList?: number[]
  mainJar?: number
  timeoutSetting?: boolean
  isCustomTask?: boolean
  method?: string
  masterUrl?: string
  resourceFiles?: { id: number; fullName: string }[] | null
  relation?: RelationType
  definition?: object
  successBranch?: number
  failedBranch?: number
  udfs?: string[]
  customConfig?: boolean
}

interface ITaskData
  extends Omit<
    INodeData,
    'timeoutFlag' | 'taskPriority' | 'timeoutNotifyStrategy'
  > {
  name?: string
  taskPriority?: string
  timeoutFlag?: 'OPEN' | 'CLOSE'
  timeoutNotifyStrategy?: string | []
  taskParams?: ITaskParams
}

export {
  ITaskPriorityOption,
  IEnvironmentNameOption,
  ILocalParam,
  ITaskType,
  ITaskData,
  INodeData,
  ITaskParams,
  IOption,
  IDataBase,
  ModelType,
  SourceType,
  ISqoopSourceParams,
  ISqoopTargetParams,
  IDependTask,
  IDependpendItem,
  IFormItem,
  IJsonItem,
  FormRules,
  IJsonItemParams,
  IResponseJsonItem
}
