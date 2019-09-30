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
  <div class="starting-params-dag-index">
    <template v-if="isView && isActive">
       <div class="box">
         <p class="box-hd"><i class="fa fa-chevron-circle-right"></i><b>{{$t('Startup parameter')}}</b></p>
         <ul class="box-bd">
           <li><span>{{$t('Startup type')}}：</span><span>{{_rtRunningType(startupParam.commandType)}}</span></li>
           <li><span>{{$t('Complement range')}}：</span><span v-if="startupParam.commandParam && startupParam.commandParam.complementStartDate">{{startupParam.commandParam.complementStartDate}}-{{startupParam.commandParam.complementEndDate}}</span><span v-else>-</span></li>
           <li><span>{{$t('Failure Strategy')}}：</span><span>{{startupParam.failureStrategy === 'END' ? $t('End') : $t('Continue')}}</span></li>
           <li><span>{{$t('Process priority')}}：</span><span>{{startupParam.processInstancePriority}}</span></li>
           <li><span>{{$t('Worker group')}}：</span><span v-if="workerGroupList.length">{{_rtWorkerGroupName(startupParam.workerGroupId)}}</span></li>
           <li><span>{{$t('Notification strategy')}}：</span><span>{{_rtWarningType(startupParam.warningType)}}</span></li>
           <li><span>{{$t('Notification group')}}：</span><span v-if="notifyGroupList.length">{{_rtNotifyGroupName(startupParam.warningGroupId)}}</span></li>
           <li><span>{{$t('Recipient')}}：</span><span>{{startupParam.receivers || '-'}}</span></li>
           <li><span>{{$t('Cc')}}：</span><span>{{startupParam.receiversCc || '-'}}</span></li>
         </ul>
       </div>
    </template>
  </div>
</template>
<script>
  import store from '@/conf/home/store'
  import { runningType } from '@/conf/home/pages/dag/_source/config'
  import { warningTypeList } from '@/conf/home/pages/projects/pages/definition/pages/list/_source/util'

  export default {
    name: 'starting-params-dag-index',
    data () {
      return {
        store,
        startupParam: store.state.dag.startup,
        isView: false,
        isActive: true,
        notifyGroupList: null,
        workerGroupList: null
      }
    },
    methods: {
      _toggleParam () {
        this.isView = !this.isView
      },
      _rtRunningType (code) {
        return _.filter(runningType, v => v.code === code)[0].desc
      },
      _rtWarningType (id) {
        return _.filter(warningTypeList, v => v.id === id)[0].code
      },
      _rtNotifyGroupName (id) {
        let o = _.filter(this.notifyGroupList, v => v.id === id)
        if (o && o.length) {
          return o[0].code
        }
        return '-'
      },
      _rtWorkerGroupName (id) {
        let o =  _.filter(this.workerGroupList, v => v.id === id)
        if (o && o.length) {
          return o[0].name
        }
        return '-'
      },
      _getNotifyGroupList () {
        let notifyGroupListS = _.cloneDeep(this.store.state.dag.notifyGroupListS) || []
        if (!notifyGroupListS.length) {
          this.store.dispatch('dag/getNotifyGroupList').then(res => {
            this.notifyGroupList = res
          })
        } else {
          this.notifyGroupList = notifyGroupListS
        }
      },
      _getWorkerGroupList () {
        let stateWorkerGroupsList = this.store.state.security.workerGroupsListAll || []
        if (!stateWorkerGroupsList.length) {
          this.store.dispatch('security/getWorkerGroupsAll').then(res => {
            this.workerGroupList = res
          })
        } else {
          this.workerGroupList = stateWorkerGroupsList
        }
      }
    },
    watch: {
      '$route': {
        deep: true,
        handler () {
          this.isActive = false
          this.$nextTick(() => (this.isActive = true))
        }
      }
    },
    mounted () {
      this._getNotifyGroupList()
      this._getWorkerGroupList()
    }
  }
</script>
<style lang="scss">
  .starting-params-dag-index {
    .box {
      padding: 5px 10px 10px;
      .box-hd {
        .fa {
          color: #0097e0;
          margin-right: 4px;
        }
        font-size: 16px;
      }
      .box-bd {
        margin-left: 20px;
      }
    }
  }
</style>
