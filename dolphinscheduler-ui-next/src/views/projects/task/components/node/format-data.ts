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

import { find, omit } from 'lodash'
import type {
  INodeData,
  ITaskData,
  ITaskParams,
  ISqoopTargetParams,
  ISqoopSourceParams
} from './types'

export function formatParams(data: INodeData): {
  processDefinitionCode: string
  upstreamCodes: string
  taskDefinitionJsonObj: object
} {
  const taskParams: ITaskParams = {}
  if (
    data.taskType === 'SPARK' ||
    data.taskType === 'MR' ||
    data.taskType === 'FLINK'
  ) {
    taskParams.programType = data.programType
    taskParams.mainClass = data.mainClass
    if (data.mainJar) {
      taskParams.mainJar = { id: data.mainJar }
    }
    taskParams.deployMode = data.deployMode
    taskParams.appName = data.appName
    taskParams.mainArgs = data.mainArgs
    taskParams.others = data.others
  }

  if (data.taskType === 'SPARK') {
    taskParams.sparkVersion = data.sparkVersion
    taskParams.driverCores = data.driverCores
    taskParams.driverMemory = data.driverMemory
    taskParams.numExecutors = data.numExecutors
    taskParams.executorMemory = data.executorMemory
    taskParams.executorCores = data.executorCores
  }

  if (data.taskType === 'FLINK') {
    taskParams.flinkVersion = data.flinkVersion
    taskParams.jobManagerMemory = data.jobManagerMemory
    taskParams.taskManagerMemory = data.taskManagerMemory
    taskParams.slot = data.slot
    taskParams.taskManager = data.taskManager
    taskParams.parallelism = data.parallelism
  }
  if (data.taskType === 'HTTP') {
    taskParams.httpMethod = data.httpMethod
    taskParams.httpCheckCondition = data.httpCheckCondition
    taskParams.httpParams = data.httpParams
    taskParams.url = data.url
    taskParams.condition = data.condition
    taskParams.connectTimeout = data.connectTimeout
    taskParams.socketTimeout = data.socketTimeout
  }

  if (data.taskType === 'SQOOP') {
    taskParams.jobType = data.isCustomTask ? 'CUSTOM' : 'TEMPLATE'
    taskParams.localParams = data.localParams
    if (data.isCustomTask) {
      taskParams.customShell = data.customShell
    } else {
      taskParams.jobName = data.jobName
      taskParams.hadoopCustomParams = data.hadoopCustomParams
      taskParams.sqoopAdvancedParams = data.sqoopAdvancedParams
      taskParams.concurrency = data.concurrency
      taskParams.modelType = data.modelType
      taskParams.sourceType = data.sourceType
      taskParams.targetType = data.targetType
      let targetParams: ISqoopTargetParams = {}
      let sourceParams: ISqoopSourceParams = {}
      switch (data.targetType) {
        case 'HIVE':
          targetParams = {
            hiveDatabase: data.targetHiveDatabase,
            hiveTable: data.targetHiveTable,
            createHiveTable: data.targetHiveCreateTable,
            dropDelimiter: data.targetHiveDropDelimiter,
            hiveOverWrite: data.targetHiveOverWrite,
            hiveTargetDir: data.targetHiveTargetDir,
            replaceDelimiter: data.targetHiveReplaceDelimiter,
            hivePartitionKey: data.targetHivePartitionKey,
            hivePartitionValue: data.targetHivePartitionValue
          }
          break
        case 'HDFS':
          targetParams = {
            targetPath: data.targetHdfsTargetPath,
            deleteTargetDir: data.targetHdfsDeleteTargetDir,
            compressionCodec: data.targetHdfsCompressionCodec,
            fileType: data.targetHdfsFileType,
            fieldsTerminated: data.targetHdfsFieldsTerminated,
            linesTerminated: data.targetHdfsLinesTerminated
          }
          break
        case 'MYSQL':
          targetParams = {
            targetType: data.targetMysqlType,
            targetDatasource: data.targetMysqlDatasource,
            targetTable: data.targetMysqlTable,
            targetColumns: data.targetMysqlColumns,
            fieldsTerminated: data.targetMysqlFieldsTerminated,
            linesTerminated: data.targetMysqlLinesTerminated,
            isUpdate: data.targetMysqlIsUpdate,
            targetUpdateKey: data.targetMysqlTargetUpdateKey,
            targetUpdateMode: data.targetMysqlUpdateMode
          }
          break
        default:
          break
      }
      switch (data.sourceType) {
        case 'MYSQL':
          sourceParams = {
            srcTable: data.srcQueryType === '1' ? '' : data.srcTable,
            srcColumnType: data.srcQueryType === '1' ? '0' : data.srcColumnType,
            srcColumns:
              data.srcQueryType === '1' || data.srcColumnType === '0'
                ? ''
                : data.srcColumns,
            srcQuerySql:
              data.srcQueryType === '0' ? '' : data.sourceMysqlSrcQuerySql,
            srcQueryType: data.srcQueryType,
            srcType: data.sourceMysqlType,
            srcDatasource: data.sourceMysqlDatasource,
            mapColumnHive: data.mapColumnHive,
            mapColumnJava: data.mapColumnJava
          }
          break
        case 'HDFS':
          sourceParams = {
            exportDir: data.sourceHdfsExportDir
          }
          break
        case 'HIVE':
          sourceParams = {
            hiveDatabase: data.sourceHiveDatabase,
            hiveTable: data.sourceHiveTable,
            hivePartitionKey: data.sourceHivePartitionKey,
            hivePartitionValue: data.sourceHivePartitionValue
          }
          break
        default:
          break
      }
      taskParams.targetParams = JSON.stringify(targetParams)
      taskParams.sourceParams = JSON.stringify(sourceParams)
    }
  }

  if (data.taskType === 'SQL') {
    taskParams.type = data.type
    taskParams.datasource = data.datasource
    taskParams.sql = data.sql
    taskParams.sqlType = data.sqlType
    taskParams.preStatements = data.preStatements
    taskParams.postStatements = data.postStatements
  }

  if (data.taskType === 'PROCEDURE') {
    taskParams.type = data.type
    taskParams.datasource = data.datasource
    taskParams.method = data.method
  }

  if (data.taskType === 'SEATUNNEL') {
    if (data.deployMode === 'local') {
      data.master = 'local'
      data.masterUrl = ''
      data.deployMode = 'client'
    }
    buildRawScript(data)
  }

  if (data.taskType === 'SWITCH') {
    taskParams.switchResult = {}
    taskParams.switchResult.dependTaskList = data.dependTaskList
    taskParams.switchResult.nextNode = data.nextNode
  }

  if (data.taskType === 'DATAX') {
    taskParams.customConfig = 0
    taskParams.dsType = data.dsType
    taskParams.dataSource = data.dataSource
    taskParams.dtType = data.dtType
    taskParams.dataTarget = data.dataTarget
    taskParams.sql = data.sql
    taskParams.targetTable = data.targetTable
    taskParams.jobSpeedByte = data.jobSpeedByte
    taskParams.jobSpeedRecord = data.jobSpeedRecord
    taskParams.preStatements = data.preStatements
    taskParams.postStatements = data.postStatements
    taskParams.xms = data.xms
    taskParams.xmx = data.xmx
  }

  const params = {
    processDefinitionCode: data.processName ? String(data.processName) : '',
    upstreamCodes: data?.preTasks?.join(','),
    taskDefinitionJsonObj: {
      code: data.code,
      delayTime: data.delayTime ? String(data.delayTime) : '0',
      description: data.description,
      environmentCode: data.environmentCode || -1,
      failRetryInterval: data.failRetryInterval
        ? String(data.failRetryInterval)
        : '0',
      failRetryTimes: data.failRetryTimes ? String(data.failRetryTimes) : '0',
      flag: data.flag,
      name: data.name,
      taskGroupId: data.taskGroupId || 0,
      taskGroupPriority: data.taskGroupPriority,
      taskParams: {
        localParams: data.localParams,
        rawScript: data.rawScript,
        resourceList: data.resourceList?.length
          ? data.resourceList.map((id: number) => ({ id }))
          : [],
        ...taskParams
      },
      taskPriority: data.taskPriority,
      taskType: data.taskType,
      timeout: data.timeout,
      timeoutFlag: data.timeoutFlag ? 'OPEN' : 'CLOSE',
      timeoutNotifyStrategy: data.timeoutNotifyStrategy?.join(''),
      workerGroup: data.workerGroup
    }
  } as {
    processDefinitionCode: string
    upstreamCodes: string
    taskDefinitionJsonObj: { timeout: number; timeoutNotifyStrategy: string }
  }
  if (!data.timeoutFlag) {
    params.taskDefinitionJsonObj.timeout = 0
    params.taskDefinitionJsonObj.timeoutNotifyStrategy = ''
  }

  return params
}

export function formatModel(data: ITaskData) {
  const params = {
    ...omit(data, [
      'environmentCode',
      'timeoutFlag',
      'timeoutNotifyStrategy',
      'taskParams'
    ]),
    ...omit(data.taskParams, ['resourceList', 'mainJar', 'localParams']),
    environmentCode: data.environmentCode === -1 ? null : data.environmentCode,
    timeoutFlag: data.timeoutFlag === 'OPEN',
    timeoutNotifyStrategy: [data.timeoutNotifyStrategy] || [],
    localParams: data.taskParams?.localParams || []
  } as INodeData

  if (data.timeoutNotifyStrategy === 'WARNFAILED') {
    params.timeoutNotifyStrategy = ['WARN', 'FAILED']
  }
  if (data.taskParams?.resourceList) {
    params.resourceList = data.taskParams.resourceList.map(
      (item: { id: number }) => item.id
    )
  }
  if (
    data.taskParams?.connectTimeout !== 60000 ||
    data.taskParams?.socketTimeout !== 60000
  ) {
    params.timeoutSetting = true
  }
  if (data.taskParams?.mainJar) {
    params.mainJar = data.taskParams?.mainJar.id
  }

  if (data.taskParams?.method) {
    params.method = data.taskParams?.method
  }

  if (data.taskParams?.targetParams) {
    const targetParams: ISqoopTargetParams = JSON.parse(
      data.taskParams.targetParams
    )
    params.targetHiveDatabase = targetParams.hiveDatabase
    params.targetHiveTable = targetParams.hiveTable
    params.targetHiveCreateTable = targetParams.createHiveTable
    params.targetHiveDropDelimiter = targetParams.dropDelimiter
    params.targetHiveOverWrite = targetParams.hiveOverWrite
    params.targetHiveTargetDir = targetParams.hiveTargetDir
    params.targetHiveReplaceDelimiter = targetParams.replaceDelimiter
    params.targetHivePartitionKey = targetParams.hivePartitionKey
    params.targetHivePartitionValue = targetParams.hivePartitionValue
    params.targetHdfsTargetPath = targetParams.targetPath
    params.targetHdfsDeleteTargetDir = targetParams.deleteTargetDir
    params.targetHdfsCompressionCodec = targetParams.compressionCodec
    params.targetHdfsFileType = targetParams.fileType
    params.targetHdfsFieldsTerminated = targetParams.fieldsTerminated
    params.targetHdfsLinesTerminated = targetParams.linesTerminated
    params.targetMysqlType = targetParams.targetType
    params.targetMysqlDatasource = targetParams.targetDatasource
    params.targetMysqlTable = targetParams.targetTable
    params.targetMysqlColumns = targetParams.targetColumns
    params.targetMysqlFieldsTerminated = targetParams.fieldsTerminated
    params.targetMysqlLinesTerminated = targetParams.linesTerminated
    params.targetMysqlIsUpdate = targetParams.isUpdate
    params.targetMysqlTargetUpdateKey = targetParams.targetUpdateKey
    params.targetMysqlUpdateMode = targetParams.targetUpdateMode
  }
  if (data.taskParams?.sourceParams) {
    const sourceParams: ISqoopSourceParams = JSON.parse(
      data.taskParams.sourceParams
    )
    params.srcTable = sourceParams.srcTable
    params.srcColumnType = sourceParams.srcColumnType
    params.srcColumns = sourceParams.srcColumns
    params.sourceMysqlSrcQuerySql = sourceParams.srcQuerySql
    params.srcQueryType = sourceParams.srcQueryType
    params.sourceMysqlType = sourceParams.srcType
    params.sourceMysqlDatasource = sourceParams.srcDatasource
    params.mapColumnHive = sourceParams.mapColumnHive
    params.mapColumnJava = sourceParams.mapColumnJava
    params.sourceHdfsExportDir = sourceParams.exportDir
    params.sourceHiveDatabase = sourceParams.hiveDatabase
    params.sourceHiveTable = sourceParams.hiveTable
    params.sourceHivePartitionKey = sourceParams.hivePartitionKey
    params.sourceHivePartitionValue = sourceParams.hivePartitionValue
  }

  if (data.taskParams?.rawScript) {
    params.rawScript = data.taskParams?.rawScript
  }

  if (data.taskParams?.switchResult) {
    params.switchResult = data.taskParams.switchResult
    params.dependTaskList = data.taskParams.switchResult?.dependTaskList
      ? data.taskParams.switchResult?.dependTaskList
      : []
    params.nextNode = data.taskParams.switchResult?.nextNode
  }

  return params
}

const buildRawScript = (model: INodeData) => {
  const baseScript = 'sh ${WATERDROP_HOME}/bin/start-waterdrop.sh'
  if (!model.resourceList) return

  let master = model.master
  let masterUrl = model?.masterUrl ? model?.masterUrl : ''
  let deployMode = model.deployMode
  let queue = model.queue

  if (model.deployMode === 'local') {
    master = 'local'
    masterUrl = ''
    deployMode = 'client'
  }

  if (master === 'yarn' || master === 'local') {
    masterUrl = ''
  }

  let localParams = ''
  model?.localParams?.forEach((param: any) => {
    localParams = localParams + ' --variable ' + param.prop + '=' + param.value
  })

  let rawScript = ''
  model.resourceList?.forEach((id: number) => {
    let item = find(model.resourceFiles, { id: id })

    rawScript =
      rawScript +
      baseScript +
      ' --master ' +
      master +
      masterUrl +
      ' --deploy-mode ' +
      deployMode +
      ' --queue ' +
      queue
    if (item && item.fullName) {
      rawScript = rawScript + ' --config ' + item.fullName
    }
    rawScript = rawScript + localParams + ' \n'
  })
  model.rawScript = rawScript ? rawScript : ''
}
