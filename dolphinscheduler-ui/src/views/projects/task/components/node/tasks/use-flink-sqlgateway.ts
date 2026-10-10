import { reactive } from 'vue'
import * as Fields from '../fields/index'
import type { IJsonItem, INodeData, ITaskData } from '../types'

export function useFlinkSqlGateway(params: any) {
  const { projectCode, from = 0, readonly, data } = params

  const model = reactive<INodeData>({
    taskType: 'FLINK_SGW',
    name: '',
    flag: 'YES',
    description: '',
    timeoutFlag: false,
    localParams: [],
    environmentCode: null,
    failRetryInterval: 1,
    failRetryTimes: 0,
    workerGroup: 'default',
    delayTime: 0,
    timeout: 30,
    timeoutNotifyStrategy: ['WARN'],

    // --- FLINK_SGW custom fields ---
    flinkJdbcUrl: 'jdbc:flink://host:port',
    rawScriptType: 'FILE',
    initScriptType: 'FILE',
    initScript: '',
    rawScript: '',
    resourceList: [],
    initScriptResourceList: [],
    statementSeparator: ';',
    maxPrintRows: 0,
    jdbcProperties: {}
  })

  return {
    json: [
      Fields.useName(from),
      ...Fields.useTaskDefinition({ projectCode, from, readonly, data, model }),
      Fields.useRunFlag(),
      Fields.useDescription(),
      Fields.useTaskPriority(),
      Fields.useWorkerGroup(projectCode),
      Fields.useEnvironmentName(model, !data?.id),
      ...Fields.useTaskGroup(model, projectCode),
      ...Fields.useFailed(),
      Fields.useDelayTime(model),
      ...Fields.useTimeoutAlarm(model),
      ...Fields.useFlinkSqlGateway(model),
      Fields.usePreTasks()
    ] as IJsonItem[],
    model
  }
}
