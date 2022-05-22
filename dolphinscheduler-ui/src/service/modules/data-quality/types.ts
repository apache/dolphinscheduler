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

interface ListReq {
  pageNo: number
  pageSize: number
  searchVal?: string
}

interface RuleListReq extends ListReq {
  endDate?: string
  startDate?: string
  ruleType?: string
}

interface ResultListReq extends ListReq {
  endDate?: string
  startDate?: string
  ruleType?: string
  state?: string
}

interface ResultItem {
  id: number
  processDefinitionId: number
  processDefinitionName: string
  processDefinitionCode: number
  processInstanceId: number
  processInstanceName: string
  projectCode: number
  taskInstanceId: number
  taskName: string
  ruleType: number
  ruleName: string
  statisticsValue: number
  comparisonValue: number
  comparisonType: number
  comparisonTypeName: string
  checkType: number
  threshold: number
  operator: number
  failureStrategy: number
  userId: number
  userName: string
  state: number
  errorOutputPath: string
  createTime: string
  updateTime: string
}

interface ResultListRes {
  totalList: ResultItem[]
  total: number
  totalPage: number
  pageSize: number
  currentPage: number
  start: number
}

interface Rule {
  id: number
  name: string
  ruleJson: string
  type: number
  userId: number
  userName: string
  createTime: string
  updateTime: string
}

interface RuleRes {
  totalList: Rule[]
  total: number
  totalPage: number
  pageSize: number
  currentPage: number
  start: number
}

export { RuleListReq, ResultListReq, ResultItem, ResultListRes, Rule, RuleRes }
