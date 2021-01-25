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
  <div class="list-model" style="position: relative;">
    <div class="table-box">
      <el-table class="fixed" :data="list" size="mini" style="width: 100%" @selection-change="_arrDelChange">
        <el-table-column type="selection" width="50"></el-table-column>
        <el-table-column type="index" :label="$t('#')" width="50"></el-table-column>
        <el-table-column :label="$t('Process Name')" min-width="200">
          <template slot-scope="scope">
            <el-popover trigger="hover" placement="top">
              <p>{{ scope.row.name }}</p>
              <div slot="reference" class="name-wrapper">
                <router-link :to="{ path: '/projects/instance/list/' + scope.row.id , query:{id: scope.row.processDefinitionId}}" tag="a" class="links" :title="scope.row.name">{{scope.row.name}}</router-link>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column :label="$t('State')" width="50">
          <template slot-scope="scope">
            <span v-html="_rtState(scope.row.state)" style="cursor: pointer;"></span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Run Type')">
          <template slot-scope="scope">
            {{_rtRunningType(scope.row.commandType)}}
          </template>
        </el-table-column>
        <el-table-column :label="$t('Scheduling Time')" width="135">
          <template slot-scope="scope">
            <span v-if="scope.row.scheduleTime">{{scope.row.scheduleTime | formatDate}}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Start Time')" width="135">
          <template slot-scope="scope">
            <span>{{scope.row.startTime | formatDate}}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('End Time')" width="135">
          <template slot-scope="scope">
            <span>{{scope.row.endTime | formatDate}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="duration" :label="$t('Duration')"></el-table-column>
        <el-table-column prop="runTimes" :label="$t('Run Times')"></el-table-column>
        <el-table-column prop="recovery" :label="$t('fault-tolerant sign')"></el-table-column>
        <el-table-column prop="executorName" :label="$t('Executor')"></el-table-column>
        <el-table-column prop="host" :label="$t('host')" width="150"></el-table-column>
        <el-table-column :label="$t('Operation')" width="240" fixed="right">
          <template slot-scope="scope">
            <div v-show="scope.row.disabled">
              <el-tooltip :content="$t('Edit')" placement="top" :enterable="false">
                <span>
                  <el-button type="primary" size="mini" icon="el-icon-edit-outline" :disabled="scope.row.state !== 'SUCCESS' && scope.row.state !== 'PAUSE' && scope.row.state !== 'FAILURE' && scope.row.state !== 'STOP'" @click="_reEdit(scope.row)" circle></el-button>
                </span>
              </el-tooltip>
              <el-tooltip :content="$t('Rerun')" placement="top" :enterable="false">
                <span><el-button type="primary" size="mini" :disabled="scope.row.state !== 'SUCCESS' && scope.row.state !== 'PAUSE' && scope.row.state !== 'FAILURE' && scope.row.state !== 'STOP'"  icon="el-icon-refresh" @click="_reRun(scope.row,scope.$index)" circle></el-button></span>
              </el-tooltip>
              <el-tooltip :content="$t('Recovery Failed')" placement="top" :enterable="false">
                <span>
                  <el-button type="success" size="mini" icon="el-icon-circle-close" :disabled="scope.row.state !== 'FAILURE'" @click="_restore(scope.row,scope.$index)" circle></el-button>
                </span>
              </el-tooltip>
              <el-tooltip :content="scope.row.state === 'STOP' ? $t('Recovery Suspend') : $t('Stop')" placement="top" :enterable="false">
                <span><el-button type="warning" size="mini" :disabled="scope.row.state !== 'RUNNING_EXECUTION' && scope.row.state !== 'STOP'"  :icon="scope.row.state === 'STOP' ? 'el-icon-video-play' : 'el-icon-close'" @click="_stop(scope.row,scope.$index)" circle></el-button></span>
              </el-tooltip>
              <el-tooltip :content="scope.row.state === 'PAUSE' ? $t('Recovery Suspend') : $t('Pause')" placement="top" :enterable="false">
                <span><el-button type="error" size="mini" :icon="scope.row.state === 'PAUSE' ? 'el-icon-video-play' : 'el-icon-video-pause'" :disabled="scope.row.state !== 'RUNNING_EXECUTION' && scope.row.state !== 'PAUSE'" @click="_suspend(scope.row,scope.$index)" circle></el-button></span>
              </el-tooltip>
              <el-tooltip :content="$t('delete')" placement="top" :enterable="false">
                <el-popconfirm
                  :confirmButtonText="$t('Confirm')"
                  :cancelButtonText="$t('Cancel')"
                  icon="el-icon-info"
                  iconColor="red"
                  :title="$t('Delete?')"
                  @onConfirm="_delete(scope.row,scope.row.id)">
                  <el-button type="danger" size="mini" icon="el-icon-delete" :disabled="scope.row.state !== 'SUCCESS' && scope.row.state !== 'FAILURE' && scope.row.state !== 'STOP' && scope.row.state !== 'PAUSE'" circle slot="reference"></el-button>
                </el-popconfirm>
              </el-tooltip>
              <el-tooltip :content="$t('Gantt')" placement="top" :enterable="false">
                <span><el-button type="primary" size="mini" icon="el-icon-s-operation" @click="_gantt(scope.row)" circle></el-button></span>
              </el-tooltip>
            </div>
            <div v-show="!scope.row.disabled">
              <!--Edit-->
              <el-button
                  type="info"
                  size="mini"
                  icon="el-icon-edit-outline"
                  disabled="true"
                  circle>
              </el-button>

              <!--Rerun-->
              <span>
                <el-button
                  v-show="buttonType === 'run'"
                  type="info"
                  size="mini"
                  disabled="true"
                  circle>
                  <span style="padding: 0 2px">{{scope.row.count}}</span>
                </el-button>
              </span>
              <el-button
                  v-show="buttonType !== 'run'"
                  type="info"
                  size="mini"
                  icon="el-icon-refresh"
                  disabled="true"
                  circle>
              </el-button>

              <!--Store-->
              <span>
                <el-button
                  v-show="buttonType === 'store'"
                  type="success"
                  size="mini"
                  circle
                  disabled="true">
                  <span style="padding: 0 3px">{{scope.row.count}}</span>
                </el-button>
              </span>
              <el-button
                  v-show="buttonType !== 'store'"
                  type="success"
                  size="mini"
                  circle
                  icon="el-icon-circle-close"
                  disabled="true">
              </el-button>

              <!--Recovery Suspend/Pause-->
              <span>
                <el-button
                  v-show="(scope.row.state === 'PAUSE' || scope.row.state === 'STOP') && buttonType === 'suspend'"
                  type="warning"
                  size="mini"
                  circle
                  disabled="true">
                  <span style="padding: 0 3px">{{scope.row.count}}</span>
                </el-button>
              </span>

              <!--Recovery Suspend-->
              <el-button
                  v-show="(scope.row.state === 'PAUSE' || scope.row.state === 'STOP') && buttonType !== 'suspend'"
                  type="warning"
                  size="mini"
                  circle
                  icon="el-icon-video-play"
                  disabled="true">
              </el-button>

              <!--Pause-->
              <span>
                <el-button
                  v-show="scope.row.state !== 'PAUSE'"
                  type="warning"
                  size="mini"
                  circle
                  icon="el-icon-close"
                  disabled="true">
                </el-button>
              </span>

              <!--Stop-->
              <span>
                <el-button
                  v-show="scope.row.state !== 'STOP'"
                  type="warning"
                  size="mini"
                  circle
                  icon="el-icon-video-pause"
                  disabled="true">
                </el-button>
              </span>

              <!--Delete-->
              <el-button
                  type="danger"
                  circle
                  size="mini"
                  icon="el-icon-delete"
                  :disabled="true">
              </el-button>

              <!--Gantt-->
              <el-button
                  type="success"
                  circle
                  size="mini"
                  icon="el-icon-s-operation"
                  disabled="true">
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-tooltip :content="$t('delete')" placement="top" :enterable="false">
      <el-popconfirm
        :confirmButtonText="$t('Confirm')"
        :cancelButtonText="$t('Cancel')"
        :title="$t('Delete?')"
        @onConfirm="_delete({},-1)"
      >
        <el-button style="position: absolute; bottom: -48px; left: 19px;"  type="primary" size="mini" :disabled="!strDelete" slot="reference">{{$t('Delete')}}</el-button>
      </el-popconfirm>
    </el-tooltip>
  </div>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import { tasksState, runningType } from '@/conf/home/pages/dag/_source/config'

  export default {
    name: 'list',
    data () {
      return {
        // data
        list: [],
        // btn type
        buttonType: '',
        strDelete: '',
        checkAll: false
      }
    },
    props: {
      processInstanceList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('dag', ['editExecutorsState', 'deleteInstance', 'batchDeleteInstance']),
      /**
       * Return run type
       */
      _rtRunningType (code) {
        return _.filter(runningType, v => v.code === code)[0].desc
      },
      /**
       * Return status
       */
      _rtState (code) {
        let o = tasksState[code]
        return `<em class="fa ansfont ${o.icoUnicode} ${o.isSpin ? 'as as-spin' : ''}" style="color:${o.color}" data-toggle="tooltip" data-container="body" title="${o.desc}"></em>`
      },
      /**
       * delete
       */
      _delete (item, i) {
        // remove tow++
        if (i < 0) {
          this._batchDelete()
          return
        }
        // remove one
        this.deleteInstance({
          processInstanceId: item.id
        }).then(res => {
          this._onUpdate()
          this.$message.success(res.msg)
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      /**
       * edit
       */
      _reEdit (item) {
        this.$router.push({ path: `/projects/instance/list/${item.id}` })
      },
      /**
       * Rerun
       * @param REPEAT_RUNNING
       */
      _reRun (item, index) {
        this._countDownFn({
          id: item.id,
          executeType: 'REPEAT_RUNNING',
          index: index,
          buttonType: 'run'
        })
      },
      /**
       * Resume running
       * @param PAUSE => RECOVER_SUSPENDED_PROCESS
       * @param FAILURE => START_FAILURE_TASK_PROCESS
       */
      _restore (item, index) {
        this._countDownFn({
          id: item.id,
          executeType: 'START_FAILURE_TASK_PROCESS',
          index: index,
          buttonType: 'store'
        })
      },
      /**
       * stop
       * @param STOP
       */
      _stop (item, index) {
        if (item.state === 'STOP') {
          this._countDownFn({
            id: item.id,
            executeType: 'RECOVER_SUSPENDED_PROCESS',
            index: index,
            buttonType: 'suspend'
          })
        } else {
          this._upExecutorsState({
            processInstanceId: item.id,
            executeType: 'STOP'
          })
        }
      },
      /**
       * pause
       * @param PAUSE
       */
      _suspend (item, index) {
        if (item.state === 'PAUSE') {
          this._countDownFn({
            id: item.id,
            executeType: 'RECOVER_SUSPENDED_PROCESS',
            index: index,
            buttonType: 'suspend'
          })
        } else {
          this._upExecutorsState({
            processInstanceId: item.id,
            executeType: 'PAUSE'
          })
        }
      },
      /**
       * operating
       */
      _upExecutorsState (o) {
        this.editExecutorsState(o).then(res => {
          this.$message.success(res.msg)
          $('body').find('.tooltip.fade.top.in').remove()
          this._onUpdate()
        }).catch(e => {
          this.$message.error(e.msg || '')
          this._onUpdate()
        })
      },
      /**
       * Countdown method refresh
       */
      _countDownFn (param) {
        this.buttonType = param.buttonType
        this.editExecutorsState({
          processInstanceId: param.id,
          executeType: param.executeType
        }).then(res => {
          this.list[param.index].disabled = false
          $('body').find('.tooltip.fade.top.in').remove()
          this.$forceUpdate()
          this.$message.success(res.msg)
          // Countdown
          this._countDown(() => {
            this._onUpdate()
          }, param.index)
        }).catch(e => {
          this.$message.error(e.msg || '')
          this._onUpdate()
        })
      },
      /**
       * update
       */
      _onUpdate () {
        this.$emit('on-update')
      },
      /**
       * list data handle
       */
      _listDataHandle (data) {
        if (data.length) {
          _.map(data, v => {
            v.disabled = true
            v.count = 9
          })
        }
        return data
      },
      /**
       * Countdown
       */
      _countDown (fn, index) {
        const TIME_COUNT = 10
        let timer
        let $count
        if (!timer) {
          $count = TIME_COUNT
          timer = setInterval(() => {
            if ($count > 0 && $count <= TIME_COUNT) {
              $count--
              this.list[index].count = $count
              this.$forceUpdate()
            } else {
              fn()
              clearInterval(timer)
              timer = null
            }
          }, 1000)
        }
      },
      _gantt (item) {
        this.$router.push({ path: `/projects/instance/gantt/${item.id}` })
      },
      _topCheckBoxClick (v) {
        this.list.forEach((item, i) => {
          this.$set(this.list[i], 'isCheck', v)
        })
        this._arrDelChange()
      },
      // _arrDelChange (v) {
      //   let arr = []
      //   this.list.forEach((item)=>{
      //     if (item.isCheck) {
      //       arr.push(item.id)
      //     }
      //   })
      //   this.strDelete = _.join(arr, ',')
      //   if (v === false) {
      //     this.checkAll = false
      //   }
      // },
      _arrDelChange (v) {
        let arr = []
        arr = _.map(v, 'id')
        this.strDelete = _.join(arr, ',')
      },
      _batchDelete () {
        this.batchDeleteInstance({
          processInstanceIds: this.strDelete
        }).then(res => {
          this._onUpdate()
          this.checkAll = false
          this.strDelete = ''
          this.$message.success(res.msg)
        }).catch(e => {
          this.checkAll = false
          this.strDelete = ''
          this.$message.error(e.msg || '')
        })
      }
    },
    watch: {
      processInstanceList: {
        handler (a) {
          this.checkAll = false
          this.list = []
          setTimeout(() => {
            this.list = _.cloneDeep(this._listDataHandle(a))
          })
        },
        immediate: true,
        deep: true
      },
      pageNo () {
        this.strDelete = ''
      }
    },
    created () {
    },
    mounted () {
    },
    components: { }
  }
</script>
