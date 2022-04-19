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
import { Component } from 'vue'

export type ITaskState =
  | 'SUBMITTED_SUCCESS'
  | 'RUNNING_EXECUTION'
  | 'READY_PAUSE'
  | 'PAUSE'
  | 'READY_STOP'
  | 'STOP'
  | 'FAILURE'
  | 'SUCCESS'
  | 'NEED_FAULT_TOLERANCE'
  | 'KILL'
  | 'WAITING_THREAD'
  | 'WAITING_DEPEND'
  | 'DELAY_EXECUTION'
  | 'FORCED_SUCCESS'
  | 'SERIAL_WAIT'
  | 'DISPATCH'

export type ITaskStateConfig = {
  [key in ITaskState]: {
    id: number
    desc: string
    color: string
    icon: Component
    isSpin: boolean
    classNames?: string
  }
}
