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
        <el-table-column prop="projectName" :label="$t('Project Name')" width="120"></el-table-column>
        <el-table-column prop="taskName" :label="$t('Task Name')" width="120"></el-table-column>
        <el-table-column prop="processInstanceName" :label="$t('Process Instance')" min-width="120"></el-table-column>
        <el-table-column prop="taskGroupName" :label="$t('Task group name')" width="120"></el-table-column>
        <el-table-column prop="priority" :label="$t('Task group queue priority')" min-width="70"></el-table-column>
        <el-table-column prop="forceStart" :label="$t('Task group queue force starting status')" min-width="100"></el-table-column>
        <el-table-column prop="inQueue" :label="$t('Task group in queue')" min-width="100"></el-table-column>
        <el-table-column prop="status" :label="$t('Task group queue status')" min-width="70"></el-table-column>
        <el-table-column :label="$t('Create Time')" min-width="50">
          <template slot-scope="scope">
            <span>{{scope.row.createTime | formatDate}}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Update Time')" min-width="50">
          <template slot-scope="scope">
            <span>{{scope.row.updateTime | formatDate}}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Operation')" width="100">
          <template slot-scope="scope">
          <el-tooltip :content="$t('Modify task group queue priority')" placement="top">
             <el-button type="primary" size="mini" icon="el-icon-edit-outline" @click="_edit(scope.row)" circle></el-button>
           </el-tooltip>
           <el-tooltip :content="$t('Force to start task')" placement="top">
             <el-button type="primary" size="mini" icon="el-icon-video-play" @click="_forceStart(scope.row)" circle></el-button>
           </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <el-dialog
        v-if="dialogVisible"
        :title="$t('Modify task group queue priority')"
        :visible.sync="dialogVisible"
        width="25%"
        center
        modal>
        <el-form :model="priorityForm" ref="priorityForm">
          <el-form-item prop="priority" :label="$t('Task group queue priority')"  :rules="[{ required: true, message: notEmptyMessage }, { type: 'number', message: mustBeNumberMessage}]">
            <el-input
              type="input"
              v-model.number="priorityForm.priority"
              maxlength="60"
              autocomplete="off"
              size="mini">
            </el-input>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">{{ $t('Cancel') }}</el-button>
        <el-button type="primary" @click="_editPriority()">{{ $t('Confirm') }}</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>
<script>
  import { mapActions } from 'vuex'
  import _ from 'lodash'
  import i18n from '@/module/i18n'

  export default {
    name: 'task-group-list',
    data () {
      return {
        list: [],
        switchValue: true,
        dialogVisible: false,
        notEmptyMessage: $t('Priority not empty'),
        mustBeNumberMessage: $t('Priority must be number'),
        priorityForm: {
          priority: 0,
          queueId: 0
        }
      }
    },
    props: {
      taskGroupQueue: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('resource', ['modifyPriority', 'forceStartTaskInQueue']),
      ...mapActions('resource', ['getTaskListInTaskGroupQueueById']),
      _edit (item) {
        this.priorityForm.priority = item.priority
        this.priorityForm.queueId = item.id
        this.dialogVisible = true
        this.$emit('on-edit', item)
      },
      _editPriority () {
        if (this.priorityForm.priority >= 0 || _.parseInt(this.priorityForm.priority) >= 0) {
          const params = {
            queueId: this.priorityForm.queueId,
            priority: this.priorityForm.priority
          }
          this.modifyPriority(params).then(res => {
            this.$emit('on-edit-priority')
            this.$message.success(res.msg)
          }).catch(e => {
            this.$message.error(e.msg || '')
          })
        } else {
          this.$message.warning(`${i18n.$t('Task group queue priority be a number')}`)
        }
      },
      _forceStart (item) {
        const params = {
          queueId: item.id
        }
        this.forceStartTaskInQueue(params).then(res => {
          this.$emit('on-force-start', item)
          this.$message.success(res.msg)
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      }
    },
    watch: {
      taskGroupList (a) {
        this.list = []
        setTimeout(() => {
          this.list = a
        })
      }
    },
    created () {
      this.list = this.taskGroupQueue
    },
    mounted () {
    },
    components: { }
  }
</script>
