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

import { formatDate } from '@/module/filter/filter'
import { tasksState } from '@/conf/home/pages/dag/_source/config'

/**
 * Node prompt dom
 */
const rtInstancesTooltip = (data) => {
  let str = '<div style="text-align: left;word-break:break-all">'
  str += `id : ${data.id ? data.id : '-'}</br>`
  str += `host : ${data.host ? data.host : '-'}</br>`
  str += `name : ${data.name ? data.name : '-'}</br>`
  str += `state : ${data.state ? tasksState[data.state].desc : '-'}（${data.state ? data.state : '-'}）</br>`
  if (data.type) {
    str += `type : ${data.type ? data.type : '-'}</br>`
  }
  str += `startTime : ${data.startTime ? formatDate(data.startTime) : '-'}</br>`
  str += `endTime : ${data.endTime ? formatDate(data.endTime) : '-'}</br>`
  str += `duration : ${data.duration  ? data.duration : '-'}</br>`
  str += '</div>'
  return str
}

/**
 * Calculate the maximum node length
 * Easy to calculate the width dynamically
 */
const rtCountMethod = list => {
  const arr = []
  function count (list, t) {
    let toggle = false
    list.forEach(v => {
      if (v.children && v.children.length > 0) {
        if (!toggle) {
          toggle = true
          t += '*'
          arr.push(t)
        }
        count(v.children, t)
      }
    })
  }
  count(list, '*')
  let num = 6
  arr.forEach(v => {
    if (v.length > num) {
      num = v.length
    }
  })
  return num
}

export {
  rtInstancesTooltip,
  rtCountMethod
}
