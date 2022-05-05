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
export type TaskType =
  | 'SHELL'
  | 'SUB_PROCESS'
  | 'PROCEDURE'
  | 'SQL'
  | 'SPARK'
  | 'FLINK'
  | 'MR'
  | 'PYTHON'
  | 'DEPENDENT'
  | 'HTTP'
  | 'DATAX'
  | 'PIGEON'
  | 'SQOOP'
  | 'CONDITIONS'
  | 'DATA_QUALITY'
  | 'SWITCH'
  | 'SEATUNNEL'
  | 'EMR'
  | 'ZEPPELIN'

export const TASK_TYPES_MAP = {
  SHELL: {
    alias: 'SHELL'
  },
  SUB_PROCESS: {
    alias: 'SUB_PROCESS'
  },
  PROCEDURE: {
    alias: 'PROCEDURE'
  },
  SQL: {
    alias: 'SQL'
  },
  SPARK: {
    alias: 'SPARK'
  },
  FLINK: {
    alias: 'FLINK'
  },
  MR: {
    alias: 'MapReduce',
    helperLinkDisable: true
  },
  PYTHON: {
    alias: 'PYTHON'
  },
  DEPENDENT: {
    alias: 'DEPENDENT'
  },
  HTTP: {
    alias: 'HTTP'
  },
  DATAX: {
    alias: 'DataX'
  },
  PIGEON: {
    alias: 'PIGEON'
  },
  SQOOP: {
    alias: 'SQOOP',
    helperLinkDisable: true
  },
  CONDITIONS: {
    alias: 'CONDITIONS'
  },
  DATA_QUALITY: {
    alias: 'DATA_QUALITY',
    helperLinkDisable: true
  },
  SWITCH: {
    alias: 'SWITCH'
  },
  SEATUNNEL: {
    alias: 'SeaTunnel',
    helperLinkDisable: true
  },
  EMR: {
    alias: 'AmazonEMR',
    helperLinkDisable: true
  },
  ZEPPELIN: {
    alias: 'ZEPPELIN',
    helperLinkDisable: true
  }
} as { [key in TaskType]: { alias: string; helperLinkDisable?: boolean } }
