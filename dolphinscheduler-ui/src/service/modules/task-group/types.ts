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
  projectCode?: number
}

interface TaskGroupIdReq {
  id: number
}

interface TaskGroupReq {
  name: string
  projectCode: string
  groupSize: string
  status: number
  description: string
}

interface TaskGroupUpdateReq extends TaskGroupReq, TaskGroupIdReq {}

interface TaskGroup {
  id: number
  name: string
  projectCode: number
  projectName: string
  groupSize: number
  useSize: number
  status: number
  description: string
  createTime: string
  updateTime: string
}

interface TaskGroupRes {
  totalList: TaskGroup[]
  total: number
  totalPage: number
  pageSize: number
  currentPage: number
  start: number
}

interface TaskGroupQueue {
  id: number
  taskId: number
  taskName: string
  projectName: string
  projectCode: string
  processInstanceName: string
  groupId: number
  processId: number
  priority: number
  forceStart: number
  inQueue: number
  status: string
  createTime: string
  updateTime: string
}

interface TaskGroupQueueRes {
  totalList: TaskGroupQueue[]
  total: number
  totalPage: number
  pageSize: number
  currentPage: number
  start: number
}

interface TaskGroupQueueIdReq {
  queueId: number
}

interface TaskGroupQueuePriorityUpdateReq extends TaskGroupQueueIdReq {
  priority: number
}

export {
  ListReq,
  TaskGroupIdReq,
  TaskGroupReq,
  TaskGroupUpdateReq,
  TaskGroup,
  TaskGroupRes,
  TaskGroupQueue,
  TaskGroupQueueRes,
  TaskGroupQueueIdReq,
  TaskGroupQueuePriorityUpdateReq
}
