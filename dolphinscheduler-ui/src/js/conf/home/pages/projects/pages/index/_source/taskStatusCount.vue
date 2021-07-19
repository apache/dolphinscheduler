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
  <div class="task-status-count-model">
    <div v-show="!msg">
      <div class="data-area" v-spin="isSpin" style="height: 430px;">
        <div class="col-md-7">
          <div id="task-status-pie" style="height:260px;margin-top: 100px;"></div>
        </div>
        <div class="col-md-5">
          <div class="table-small-model">
            <table>
              <tr>
                <th width="40">{{$t('#')}}</th>
                <th>{{$t('Number')}}</th>
                <th>{{$t('State')}}</th>
              </tr>
              <tr v-for="(item,$index) in taskStatusList" :key="$index">
                <td><span>{{$index+1}}</span></td>
                <td>
                  <span>
                    <a href="javascript:" @click="searchParams.projectCode && _goTask(item.key)" :class="searchParams.projectCode ?'links':''">{{item.value}}</a>
                  </span>
                </td>
                <td><span class="ellipsis" style="width: 98%;" :title="item.key">{{item.key}}</span></td>
              </tr>
            </table>
          </div>
        </div>
      </div>
    </div>
    <div v-show="msg">
      <m-no-data :msg="msg" v-if="msg" :height="430"></m-no-data>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import { pie } from './chartConfig'
  import Chart from '@/module/ana-charts'
  import mNoData from '@/module/components/noData/noData'
  import { stateType } from '@/conf/home/pages/projects/pages/_source/conditions/instance/common'

  export default {
    name: 'task-status-count',
    data () {
      return {
        isSpin: true,
        msg: '',
        taskStatusList: []
      }
    },
    props: {
      searchParams: Object
    },
    methods: {
      ...mapActions('projects', ['getTaskStatusCount']),
      _goTask (name) {
        this.$router.push({
          name: 'task-instance',
          query: {
            stateType: _.find(stateType, ['label', name]).code,
            startDate: this.searchParams.startDate,
            endDate: this.searchParams.endDate
          }
        })
      },
      _handleTaskStatus (res) {
        let data = res.data.taskCountDtos
        this.taskStatusList = _.map(data, v => {
          return {
            // CHECK!!
            key: _.find(stateType, ['code', v.taskStateType]).label,
            value: v.count,
            type: 'type'
          }
        })
        const myChart = Chart.pie('#task-status-pie', this.taskStatusList, { title: '' })
        myChart.echart.setOption(pie)

        // Jump forbidden in index page
        if (this.searchParams.projectCode) {
          myChart.echart.on('click', e => {
            this._goTask(e.data.name)
          })
        }
      }
    },
    watch: {
      searchParams: {
        deep: true,
        immediate: true,
        handler (o) {
          this.isSpin = true
          this.getTaskStatusCount(o).then(res => {
            this.taskStatusList = []
            this._handleTaskStatus(res)
            this.isSpin = false
          }).catch(e => {
            console.log(e)
            this.msg = e.msg || 'error'
            this.isSpin = false
          })
        }
      }
    },
    beforeCreate () {
    },
    created () {
    },
    beforeMount () {
    },
    mounted () {
    },
    beforeUpdate () {
    },
    updated () {
    },
    beforeDestroy () {
    },
    destroyed () {
    },
    computed: {},
    components: { mNoData }
  }
</script>
