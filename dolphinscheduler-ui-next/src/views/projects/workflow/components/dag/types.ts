export interface ProcessDefinition {
  id: number
  code: number
  name: string
  version: number
  releaseState: string
  projectCode: number
  description: string
  globalParams: string
  globalParamList: any[]
  globalParamMap: any
  createTime: string
  updateTime: string
  flag: string
  userId: number
  userName?: any
  projectName?: any
  locations: string
  scheduleReleaseState?: any
  timeout: number
  tenantId: number
  tenantCode: string
  modifyBy?: any
  warningGroupId: number
}

export interface ProcessTaskRelationList {
  id: number
  name: string
  processDefinitionVersion: number
  projectCode: any
  processDefinitionCode: any
  preTaskCode: number
  preTaskVersion: number
  postTaskCode: any
  postTaskVersion: number
  conditionType: string
  conditionParams: any
  createTime: string
  updateTime: string
}

export interface TaskDefinitionList {
  id: number
  code: any
  name: string
  version: number
  description: string
  projectCode: any
  userId: number
  taskType: string
  taskParams: any
  taskParamList: any[]
  taskParamMap: any
  flag: string
  taskPriority: string
  userName?: any
  projectName?: any
  workerGroup: string
  environmentCode: number
  failRetryTimes: number
  failRetryInterval: number
  timeoutFlag: string
  timeoutNotifyStrategy: string
  timeout: number
  delayTime: number
  resourceIds: string
  createTime: string
  updateTime: string
  modifyBy?: any
  dependence: string
}

export interface WorkflowDefinition {
  processDefinition: ProcessDefinition
  processTaskRelationList: ProcessTaskRelationList[]
  taskDefinitionList: TaskDefinitionList[]
}
