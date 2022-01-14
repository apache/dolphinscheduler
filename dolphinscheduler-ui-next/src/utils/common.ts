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

/**
 * Intelligent display kb m
 */
export const bytesToSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024 // or 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))

  return parseInt((bytes / Math.pow(k, i)).toPrecision(3)) + ' ' + sizes[i]
}

export const fileTypeArr = [
  'txt',
  'log',
  'sh',
  'bat',
  'conf',
  'cfg',
  'py',
  'java',
  'sql',
  'xml',
  'hql',
  'properties',
  'json',
  'yml',
  'yaml',
  'ini',
  'js'
]
