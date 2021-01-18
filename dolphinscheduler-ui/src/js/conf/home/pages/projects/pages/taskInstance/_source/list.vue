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
      <el-table :data="list" size="mini" style="width: 100%">
        <el-table-column type="index" :label="$t('#')" width="50"></el-table-column>
        <el-table-column prop="name" :label="$t('Name')"></el-table-column>
        <el-table-column :label="$t('Process Instance')" min-width="200">
          <template slot-scope="scope">
            <el-popover trigger="hover" placement="top">
              <p>{{ scope.row.processInstanceName }}</p>
              <div slot="reference" class="name-wrapper">
                <a href="javascript:" class="links" @click="_go(scope.row)"><span class="ellipsis" :title="scope.row.processInstanceName">{{scope.row.processInstanceName}}</span></a>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="executorName" :label="$t('Executor')"></el-table-column>
        <el-table-column prop="taskType" :label="$t('Node Type')"></el-table-column>
        <el-table-column :label="$t('State')" width="50">
          <template slot-scope="scope">
            <span v-html="_rtState(scope.row.state)" style="cursor: pointer;"></span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Submit Time')" width="135">
          <template slot-scope="scope">
            <span v-if="scope.row.submitTime">{{scope.row.submitTime | formatDate}}</span>
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
        <el-table-column prop="host" :label="$t('host')" width="150"></el-table-column>
        <el-table-column prop="duration" :label="$t('Duration')"></el-table-column>
        <el-table-column prop="retryTimes" :label="$t('Retry Count')"></el-table-column>
        <el-table-column :label="$t('Operation')" width="80" fixed="right">
          <template slot-scope="scope">
            <div>
              <el-tooltip :content="$t('Force success')" placement="top" :enterable="false">
                <span>
                  <el-button type="primary" size="mini" icon="el-icon-success" :disabled="!(scope.row.state === 'FAILURE' || scope.row.state === 'NEED_FAULT_TOLERANCE' || scope.row.state === 'KILL')" @click="_forceSuccess(scope.row)" circle></el-button>
                </span>
              </el-tooltip>
              <el-tooltip :content="$t('View log')" placement="top" :enterable="false">
                <span><el-button type="primary" size="mini" :disabled="scope.row.taskType==='SUB_PROCESS'? true: false"  icon="el-icon-tickets" @click="_refreshLog(scope.row)" circle></el-button></span>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog
      :show-close="false"
      :visible.sync="logDialog"
      width="auto">
      <m-log :item="item" :source="source" :logId="logId" @ok="ok" @close="close"></m-log>
    </el-dialog>
  </div>
</template>
<script>
  import Permissions from '@/module/permissions'
  import mLog from '@/conf/home/pages/dag/_source/formModel/log'
  import { tasksState } from '@/conf/home/pages/dag/_source/config'
  import { mapActions } from 'vuex'

  export default {
    name: 'list',
    data () {
      return {
        list: [],
        isAuth: Permissions.getAuth(),
        backfillItem: {},
        logDialog: false,
        item: {},
        source: '',
        logId: null
      }
    },
    props: {
      taskInstanceList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('dag', ['forceTaskSuccess']),
      _rtState (code) {
        let o = tasksState[code]
        return `<em class="${o.icoUnicode} ${o.isSpin ? 'as as-spin' : ''}" style="color:${o.color}" data-toggle="tooltip" data-container="body" title="${o.desc}"></em>`
      },
      _refreshLog (item) {
        this.item = item
        this.source = 'list'
        this.logId = item.id
        this.logDialog = true
      },
      ok () {},
      close () {
        this.logDialog = false
      },
      _forceSuccess (item) {
        this.forceTaskSuccess({ taskInstanceId: item.id }).then(res => {
          if (res.code === 0) {
            this.$message.success(res.msg)
            setTimeout(this._onUpdate, 1000)
          } else {
            this.$message.error(res.msg)
          }
        }).catch(e => {
          this.$message.error(e.msg)
        })
      },
      _onUpdate () {
        this.$emit('on-update')
      },
      _go (item) {
        this.$router.push({ path: `/projects/instance/list/${item.processInstanceId}` })
      }
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
    components: { mLog }
  }
</script>
