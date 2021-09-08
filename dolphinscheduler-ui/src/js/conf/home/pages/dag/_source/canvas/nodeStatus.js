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

import Vue from 'vue'
import './nodeStatus.scss'
import i18n from '@/module/i18n'
import { formatDate } from '@/module/filter/filter'

const nodeStatus = ({ stateProps, taskInstance }) => {
  const Instance = new Vue({
    data: {},
    methods: {},
    render (h) {
      return (
        <el-tooltip placement="top">
          <div slot="content">
            <ul class="status-info">
              <li>{i18n.$t('Name')}：{taskInstance.name}</li>
              <li>{i18n.$t('State')}：{stateProps.desc}</li>
              <li>{i18n.$t('type')}：{taskInstance.taskType}</li>
              <li>{i18n.$t('host')}：{taskInstance.host || '-'}</li>
              <li>{i18n.$t('Retry Count')}：{taskInstance.retryTimes}</li>
              <li>{i18n.$t('Submit Time')}：{formatDate(taskInstance.submitTime)}</li>
              <li>{i18n.$t('Start Time')}：{formatDate(taskInstance.startTime)}</li>
              <li>{i18n.$t('End Time')}：{taskInstance.endTime ? formatDate(taskInstance.endTime) : '-'}</li>
            </ul>
          </div>
          <em ref="statusIcon" class={`status-icon ${stateProps.icoUnicode}`} style={{
            color: stateProps.color
          }}></em>
        </el-tooltip>
      )
    }
  })
  return Instance
}

export default nodeStatus
