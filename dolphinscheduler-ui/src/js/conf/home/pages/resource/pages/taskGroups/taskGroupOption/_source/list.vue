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
        <el-table-column prop="name" :label="$t('Task group name')" width="150"></el-table-column>
        <el-table-column prop="projectName" :label="$t('Project Name')"></el-table-column>
        <el-table-column prop="groupSize" :label="$t('Task group resource pool size')" min-width="50"></el-table-column>
        <el-table-column prop="useSize" :label="$t('Task group resource used pool size')" min-width="50"></el-table-column>
        <el-table-column prop="description" :label="$t('Task group desc')" min-width="50"></el-table-column>
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
        <el-table-column prop="status" :label="$t('Task group status')" min-width="50">
          <template slot-scope="scope">
            <el-tooltip :content="scope.row.status? $t('Task group enable status'):$t('Task group disable status')" placement="top">
              <el-switch
                v-model="scope.row.status"
                active-color="#13ce66"
                inactive-color="#ff4949"
                @change="_switchTaskGroupStatus(scope.row)"/>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Operation')" width="100">
          <template slot-scope="scope">
          <el-tooltip :content="$t('Edit')" placement="top">
             <el-button type="primary" size="mini" icon="el-icon-edit-outline" @click="_edit(scope.row)" circle></el-button>
           </el-tooltip>
           <el-tooltip :content="$t('View task group queue')" placement="top">
             <el-button type="success" size="mini" icon="el-icon-tickets" @click="_switchTaskGroupQueue(scope.row)" circle></el-button>
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
      taskGroupList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('resource', ['closeTaskGroup', 'startTaskGroup']),
      _switchTaskGroupStatus (item, i) {
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
      _switchTaskGroupQueue (item) {
        this.$router.push({ path: `/resource/task-group-queue?id=${item.id}` })
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
