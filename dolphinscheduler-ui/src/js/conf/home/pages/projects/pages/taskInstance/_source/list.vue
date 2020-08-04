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
<template>
  <div class="list-model">
    <div class="table-box">
      <table class="fixed">
        <tr>
          <th scope="col" style="min-width: 50px">
            <span>{{$t('#')}}</span>
          </th>
          <th scope="col" style="min-width: 200px;max-width: 300px;">
            <span>{{$t('Name')}}</span>
          </th>
          <th scope="col" style="min-width: 200px;max-width: 300px;">
            <span>{{$t('Process Instance')}}</span>
          </th>
          <th scope="col" style="min-width: 60px">
            <span>{{$t('Executor')}}</span>
          </th>
          <th scope="col" style="min-width: 70px">
            <span style="margin-left: 5px">{{$t('Node Type')}}</span>
          </th>
          <th scope="col" style="min-width: 30px">
            <span>{{$t('State')}}</span>
          </th>
          <th scope="col" style="min-width: 130px">
            <span>{{$t('Submit Time')}}</span>
          </th>
          <th scope="col" style="min-width: 130px">
            <span>{{$t('Start Time')}}</span>
          </th>
          <th scope="col" style="min-width: 130px">
            <span>{{$t('End Time')}}</span>
          </th>
          <th scope="col" style="min-width: 130px">
            <span>{{$t('host')}}</span>
          </th>
          <th scope="col" style="min-width: 70px">
            <span>{{$t('Duration')}}(s)</span>
          </th>
          <th scope="col" style="min-width: 60px">
            <div style="width: 50px">
              <span>{{$t('Retry Count')}}</span>
            </div>
          </th>
          <th scope="col" style="min-width: 60px">
            <span>{{$t('Operation')}}</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in list" :key="item.id">
          <td>
            <span>{{parseInt(pageNo === 1 ? ($index + 1) : (($index + 1) + (pageSize * (pageNo - 1))))}}</span>
          </td>
          <td style="min-width: 200px;max-width: 300px;padding-right: 10px;">
            <span class="ellipsis" :title="item.name">{{item.name}}</span>
          </td>
          <td style="min-width: 200px;max-width: 300px;padding-right: 10px;"><a href="javascript:" class="links" @click="_go(item)"><span class="ellipsis" :title="item.processInstanceName">{{item.processInstanceName}}</span></a></td>
          <td>
            <span v-if="item.executorName">{{item.executorName}}</span>
            <span v-else>-</span>
          </td>
          <td><span style="margin-left: 5px">{{item.taskType}}</span></td>
          <td><span v-html="_rtState(item.state)" style="cursor: pointer;"></span></td>
          <td>
            <span v-if="item.submitTime">{{item.submitTime | formatDate}}</span>
            <span v-else>-</span>
          </td>
          <td>
            <span v-if="item.startTime">{{item.startTime | formatDate}}</span>
            <span v-else>-</span>
          </td>
          <td>
            <span v-if="item.endTime">{{item.endTime | formatDate}}</span>
            <span v-else>-</span>
          </td>
          <td><span>{{item.host || '-'}}</span></td>
          <td><span>{{item.duration}}</span></td>
          <td><span>{{item.retryTimes}}</span></td>
          <td>
            <x-button
                    type="info"
                    shape="circle"
                    size="xsmall"
                    data-toggle="tooltip"
                    :title="$t('View log')"
                    icon="ans-icon-log"
                    :disabled="item.taskType==='SUB_PROCESS'? true: false"
                    @click="_refreshLog(item)">
            </x-button>
          </td>
        </tr>
      </table>
    </div>
  </div>
</template>
<script>
  import Permissions from '@/module/permissions'
  import mLog from '@/conf/home/pages/dag/_source/formModel/log'
  import { tasksState } from '@/conf/home/pages/dag/_source/config'

  export default {
    name: 'list',
    data () {
      return {
        list: [],
        isAuth: Permissions.getAuth(),
        backfillItem: {}
      }
    },
    props: {
      taskInstanceList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      _rtState (code) {
        let o = tasksState[code]
        return `<em class="${o.icoUnicode} ${o.isSpin ? 'as as-spin' : ''}" style="color:${o.color}" data-toggle="tooltip" data-container="body" title="${o.desc}"></em>`
      },
      _refreshLog (item) {
        let self = this
        let instance = this.$modal.dialog({
          closable: false,
          showMask: true,
          escClose: true,
          className: 'v-modal-custom',
          transitionName: 'opacityp',
          render (h) {
            return h(mLog, {
              on: {
                ok () {
                },
                close () {
                  instance.remove()
                }
              },
              props: {
                self: self,
                source: 'list',
                logId: item.id
              }
            })
          }
        })
      },
      _go (item) {
        this.$router.push({ path: `/projects/instance/list/${item.processInstanceId}` })
      },
    },
    watch: {
      taskInstanceList (a) {
        this.list = []
        setTimeout(() => {
          this.list = a
        })
      }
    },
    created () {
    },
    mounted () {
      this.list = this.taskInstanceList
    },
    components: { }
  }
</script>
