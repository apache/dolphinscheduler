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
  <div class="queue-count-model">
    <div v-show="!msg">
      <div class="data-area" v-spin="isSpin" style="height: 430px;">
        <div class="col-md-7">
          <div id="queue-pie" style="height:260px;margin-top: 100px;"></div>
        </div>
        <div class="col-md-5">
          <div class="table-small-model">
            <table>
              <tr>
                <th width="40">{{$t('#')}}</th>
                <th>{{$t('Number')}}</th>
                <th>{{$t('State')}}</th>
              </tr>
              <tr v-for="(item,$index) in queueList">
                <td><span>{{$index+1}}</span></td>
                <td><span><a href="javascript:" >{{item.value}}</a></span></td>
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
  export default {
    name: 'queue-count',
    data () {
      return {
        isSpin: true,
        msg: '',
        queueList: []
      }
    },
    props: {
      searchParams: Object
    },
    methods: {
      ...mapActions('projects', ['getQueueCount']),
      _handleQueue (res) {
        _.forEach(res.data, (v, k) => this.queueList.push({
          key: k === 'taskQueue' ? `${this.$t('Task queue')}` : `${this.$t('Task kill')}`,
          value: v
        }))
        const myChart = Chart.pie('#queue-pie', this.queueList, { title: '' })
        myChart.echart.setOption(_.assign(_.cloneDeep(pie), {
          color: ['#D5050B', '#0398E1']
        }))
      }
    },
    watch: {
      'searchParams': {
        deep: true,
        immediate: true,
        handler (o) {
          this.isSpin = true
          this.getQueueCount(o).then(res => {
            this.queueList = []
            this._handleQueue(res)
            this.isSpin = false
          }).catch(e => {
            this.msg = e.msg || 'error'
            this.isSpin = false
          })
        }
      }
    },
    created () {
    },
    mounted () {
    },
    components: { mNoData }
  }
</script>