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
        <el-table-column prop="groupId" :label="$t('Task group name')" width="120"></el-table-column>
        <el-table-column prop="priority" :label="$t('Task group queue priority')" min-width="50"></el-table-column>
        <el-table-column prop="forceStart" :label="$t('Task group queue force starting status')" min-width="100"></el-table-column>
        <el-table-column prop="inQueue" :label="$t('Task group in queue')" min-width="100"></el-table-column>
        <el-table-column prop="status" :label="$t('Task group queue status')" min-width="50"></el-table-column>
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
          <el-tooltip :content="$t('Edit')" placement="top">
             <el-button type="primary" size="mini" icon="el-icon-edit-outline" @click="_edit(scope.row)" circle></el-button>
           </el-tooltip>
           <el-tooltip :content="$t('Delete')" placement="top">
             <el-button type="danger" size="mini" icon="el-icon-delete" circle></el-button>
             <el-popconfirm
               :confirmButtonText="$t('Confirm')"
               :cancelButtonText="$t('Cancel')"
               icon="el-icon-info"
               iconColor="red"
               :title="$t('Delete?')"
               @onConfirm="_delete(scope.row,scope.row.id)"
             >
               <el-button type="danger" size="mini" icon="el-icon-delete" circle slot="reference"></el-button>
             </el-popconfirm>
           </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
<script>
  import { mapActions } from 'vuex'

  export default {
    name: 'task-group-list',
    data () {
      return {
        list: [],
        switchValue: true
      }
    },
    props: {
      taskGroupQueue: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('resource', ['closeTaskGroup', 'startTaskGroup']),
      ...mapActions('resource', ['getTaskListInTaskGroupQueueById']),
      _switchTaskGroupStatus (item, i) {
        console.log('switch...')
        console.log(item)
        if (item.status) {
          this.startTaskGroup({
            id: item.id
          }).then(res => {
            let newList = []
            this.list.forEach(item => {
              if (item.id !== i) {
                newList.push(item)
              }
            })
            this.list = newList
            this.$message.success(res.msg)
          }).catch(e => {
            this.$message.error(e.msg || '')
          })
        } else {
          this.closeTaskGroup({
            id: item.id
          }).then(res => {
            let newList = []
            this.list.forEach(item => {
              if (item.id !== i) {
                newList.push(item)
              }
            })
            this.list = newList
            this.$message.success(res.msg)
          }).catch(e => {
            this.$message.error(e.msg || '')
          })
        }
      },
      _edit (item) {
        this.$emit('on-edit', item)
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
      this.list = this.taskGroupList
    },
    mounted () {
    },
    components: { }
  }
</script>
