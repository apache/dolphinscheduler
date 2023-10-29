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

interface PluginInstanceReq {
  instanceName: string
  pluginDefineId: number
  instanceType: string
  warningType: string
  pluginInstanceParams: string
}

interface InstanceNameReq {
  alertInstanceName: string
}

interface UpdatePluginInstanceReq {
  alertPluginInstanceId: number
  instanceName: string
  warningType: string
  pluginInstanceParams: string
}

interface AlertPluginItem {
  id: number
  pluginDefineId: number
  instanceName: string
  pluginInstanceParams: string
  createTime: string
  updateTime: string
  alertPluginName: string
}

type IdReq = number

export {
  ListReq,
  PluginInstanceReq,
  InstanceNameReq,
  IdReq,
  UpdatePluginInstanceReq,
  AlertPluginItem
}
