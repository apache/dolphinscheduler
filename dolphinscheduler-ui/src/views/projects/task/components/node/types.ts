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
import type { TaskExecuteType, TaskType } from '@/store/project/types'
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
type IDateType = 'hour' | 'day' | 'week' | 'month'

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
  field_length?: number
  begin_time?: string
  deadline?: string
  datetime_format?: string
  target_filter?: string
  regexp_pattern?: string
  enum_list?: string
  src_filter?: string
  src_field?: string
  statistics_execute_sql?: string
  statistics_name?: string
  target_connector_type?: number
  target_datasource_id?: number
  target_table?: string
  threshold?: string
  mapping_columns?: string
}

interface ITaskParams {
  resourceList?: ISourceItem[]
  mainJar?: ISourceItem
  localParams?: ILocalParam[]
  runType?: string
  jvmArgs?: string
  isModulePath?: boolean
  rawScript?: string
  initScript?: string
  programType?: string
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
  splitBy?: string
  modelType?: ModelType
  sourceType?: SourceType
  targetType?: SourceType
  targetParams?: string
  sourceParams?: string
  queue?: string
  master?: string
  masterUrl?: string
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
  stepsDefineJson?: string
  zeppelinNoteId?: string
  zeppelinParagraphId?: string
  zeppelinRestEndpoint?: string
  restEndpoint?: string
  zeppelinProductionNoteDirectory?: string
  productionNoteDirectory?: string
  hiveCliOptions?: string
  hiveSqlScript?: string
  hiveCliTaskExecutionType?: string
  noteId?: string
  paragraphId?: string
  condaEnvName?: string
  inputNotePath?: string
  outputNotePath?: string
  parameters?: string
  kernel?: string
  engine?: string
  executionTimeout?: string
  startTimeout?: string
  processDefinitionCode?: number
  conditionResult?: {
    successNode?: number[]
    failedNode?: number[]
  }
  udfs?: string
  connParams?: string
  targetJobName?: string
  cluster?: string
  namespace?: string
  clusterNamespace?: string
  minCpuCores?: string
  minMemorySpace?: string
  image?: string
  algorithm?: string
  params?: string
  searchParams?: string
  dataPath?: string
  experimentName?: string
  modelName?: string
  mlflowTrackingUri?: string
  mlflowJobType?: string
  automlTool?: string
  registerModel?: boolean
  mlflowTaskType?: string
  mlflowProjectRepository?: string
  mlflowProjectVersion?: string
  deployType?: string
  deployPort?: string
  deployModelKey?: string
  cpuLimit?: string
  memoryLimit?: string
  zk?: string
  zkPath?: string
  executeMode?: string
  useCustom?: boolean
  runMode?: string
  dvcTaskType?: string
  dvcRepository?: string
  dvcVersion?: string
  dvcDataLocation?: string
  dvcMessage?: string
  dvcLoadSaveDataPath?: string
  dvcStoreUrl?: string
  address?: string
  taskId?: string
  online?: boolean
  sagemakerRequestJson?: string
  script?: string
  scriptParams?: string
  pythonPath?: string
  isCreateEnvironment?: string
  pythonCommand?: string
  pythonEnvTool?: string
  requirements?: string
  condaPythonVersion?: string
  isRestartTask?: boolean
  isJsonFormat?: boolean
  jsonData?: string
  migrationType?: string
  replicationTaskIdentifier?: string
  sourceEndpointArn?: string
  targetEndpointArn?: string
  replicationInstanceArn?: string
  tableMappings?: string
  replicationTaskArn?: string
  jsonFormat?: boolean
  destinationLocationArn?: string
  sourceLocationArn?: string
  name?: string
  cloudWatchLogGroupArn?: string
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
    Omit<IRuleParameters, 'mapping_columns'> {
  id?: string
  taskType?: ITaskType
  processName?: number
  delayTime?: number
  description?: string
  environmentCode?: number | null
  failRetryInterval?: number
  failRetryTimes?: number
  cpuQuota?: number
  memoryMax?: number
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
  resourceFiles?: { id: number; fullName: string }[] | null
  relation?: RelationType
  definition?: object
  successBranch?: number
  failedBranch?: number
  udfs?: string[]
  customConfig?: boolean
  mapping_columns?: object[]
  taskExecuteType?: TaskExecuteType
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
  IResponseJsonItem,
  IDateType
}
