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
  <div class="command-state-count-model">
    <div v-show="!msg">
      <div class="data-area" v-spin="isSpin">
        <div id="command-state-bar" style="height:500px"></div>
      </div>
    </div>
    <div v-show="msg">
      <m-no-data :msg="msg" v-if="msg" :height="530"></m-no-data>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import { simple } from './chartConfig'
  import Chart from '@/module/ana-charts'
  import mNoData from '@/module/components/noData/noData'

  export default {
    name: 'command-state-count',
    data () {
      return {
        isSpin: true,
        msg: ''
      }
    },
    props: {
      searchParams: Object
    },
    methods: {
      ...mapActions('projects', ['getCommandStateCount']),
      _handleCommandState (res) {
        let data = []
        _.forEach(res.data, (v, i) => {
          let key = _.keys(v)
          if (key[0] === 'errorCount') {
            data.push({ typeName: `${this.$t('Error command count')}`, key: v.commandState, value: v.errorCount })
          }
        })
        _.forEach(res.data, (v, i) => {
          let key = _.keys(v)
          if (key[1] === 'normalCount') {
            data.push({ typeName: `${this.$t('Normal command count')}`, key: v.commandState, value: v.normalCount })
          }
        })
        const myChart = Chart.bar('#command-state-bar', data, {
          title: ''
        })
        myChart.echart.setOption(simple)
      }
    },
    created () {

    },
    watch: {
      'searchParams': {
        deep: true,
        immediate: true,
        handler (o) {
          this.isSpin = true
          this.getCommandStateCount(o).then(res => {
            this._handleCommandState(res)
            this.isSpin = false
          }).catch(e => {
            this.msg = e.msg || 'error'
            this.isSpin = false
          })
        }
      }
    },
    mounted () {

    },
    computed: {},
    components: { mNoData }
  }
</script>