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
  <div class="define-user-count-model">
    <div v-if="msg">
      <div class="data-area" v-spin="isSpin">
        <div id="process-definition-bar" style="height:500px"></div>
      </div>
    </div>
    <div v-else>
      <m-no-data :height="530"></m-no-data>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import { bar } from './chartConfig'
  import Chart from '@/module/ana-charts'
  import mNoData from '@/module/components/noData/noData'
  export default {
    name: 'define-user-count',
    data () {
      return {
        isSpin: true,
        msg: true,
        parameter: {projectId: 0}
      }
    },
    props: {
      projectId: Number
    },
    methods: {
      ...mapActions('projects', ['getDefineUserCount']),
      _handleDefineUser (res) {
        let data = res.data.userList || []
        this.defineUserList = _.map(data, v => {
          return {
            key: v.userName + ',' + v.userId + ',' + v.count,
            value: v.count
          }
        })
        const myChart = Chart.bar('#process-definition-bar', this.defineUserList, {})
        myChart.echart.setOption(bar)
        // Jump not allowed on home page
        if (this.projectId) {
          myChart.echart.on('click', e => {
            this.$router.push({
              name: 'projects-definition-list',
              query: {
                userId: e.name.split(',')[1]
              }
            })
          })
        }
      }
    },
    created () {
      this.isSpin = true
      this.parameter.projectId = this.projectId;
      this.getDefineUserCount(this.parameter).then(res => {
        this.msg = res.data.count > 0 ? true : false
        this.defineUserList = []
        this._handleDefineUser(res)
        this.isSpin = false
      }).catch(e => {
        this.isSpin = false
      })
    },
    mounted () {
    },
    components: { mNoData }
  }
</script>
