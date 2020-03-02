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
  <m-list-construction :title="$t('statistics') + $t('Manage')">
    <template slot="content">
      <div class="servers-wrapper mysql-model" v-show="2">
        <div class="row">
          <div class="col-md-3">
            <div class="text-num-model text">
              <div class="title">
                <span>{{$t('command number of waiting for running')}}</span>
              </div>
              <div class="value-p">
                <strong :style="{color:color[0]}"> {{commandCountData.normalCount}}</strong>
              </div>
            </div>
          </div>
        <div class="col-md-3">
          <div class="text-num-model text">
            <div class="title">
              <span >{{$t('failure command number')}}</span>
            </div>
            <div class="value-p">
              <strong :style="{color:color[1]}"> {{commandCountData.errorCount}}</strong>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="text-num-model text">
            <div class="title">
              <span >{{$t('tasks number of waiting running')}}</span>
            </div>
            <div class="value-p">
              <strong :style="{color:color[0]}"> {{queueCount.taskQueue}}</strong>
            </div>
          </div>
        </div>
        <div class="col-md-3">
          <div class="text-num-model text">
            <div class="title">
              <span >{{$t('task number of ready to kill')}}</span>
            </div>
            <div class="value-p">
              <strong :style="{color:color[1]}">{{queueCount.taskKill}}</strong>
            </div>
          </div>
        </div>
      </div>
      </div>
      <m-spin :is-spin="isLoading" ></m-spin>
    </template>
  </m-list-construction>
</template>

<script>
  import { mapActions } from 'vuex'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import themeData from '@/module/echarts/themeData.json'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
      name: 'statistics',
    data () {
      return {
        isLoading: false,
        queueCount: {},
        commandCountData: {},
        color: themeData.color
      }
    },
    props:{},
    methods: {
      //...mapActions('monitor', ['getDatabaseData'])
      // ...mapActions('projects', ['getCommandStateCount']),
      ...mapActions('projects', ['getQueueCount']),
      ...mapActions('projects', ['getCommandStateCount']),
    },
    watch: {},
    created () {
      this.isLoading = true
      this.getQueueCount().then(res => {
        this.queueCount = res.data
        this.isLoading = false
      }).catch(() => {
        this.isLoading = false
       })

      this.getCommandStateCount().then(res => {
        let normal = 0
        let error = 0
        _.forEach(res.data, (v, i) => {
          let key = _.keys(v)
          if(key[0] == 'errorCount') {
            error = error + v.errorCount
          }
          if(key[1] == 'normalCount'){
            normal = normal + v.normalCount
          }
          }
        )
        this.commandCountData = {
          'normalCount': normal,
          'errorCount' : error
        }
      }).catch( () => {
      })
    },
    mounted () {
    },
    components: { mListConstruction, mSpin, mNoData }
  }

</script>
<style lang="scss" rel="stylesheet/scss">
  @import "./servers";
</style>
