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
  <div class="list-model" style="position: relative">
    <div class="table-box">
      <el-table
        :data="list"
        size="mini"
        style="width: 100%"
        @selection-change="select"
      >
        <el-table-column
          prop="id"
          :label="$t('#')"
          width="50"
        ></el-table-column>
        <el-table-column :label="$t('Task Name')" min-width="200">
          <template v-slot="scope">
            <el-popover trigger="hover" placement="top">
              <p>{{ scope.row.name }}</p>
              <div slot="reference" class="name-wrapper">
                <a
                  href="javascript:"
                  class="links"
                  @click="_switchTasks(scope.row)"
                >
                  {{ scope.row.name }}
                </a>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Task Type')" prop="taskType" width="135">
        </el-table-column>
        <el-table-column
          :label="$t('User Name')"
          prop="userName"
          width="135"
        ></el-table-column>
        <el-table-column :label="$t('Version Info')" prop="version" width="135">
        </el-table-column>
        <el-table-column :label="$t('Description')">
          <template v-slot="scope">
            <span>{{ scope.row.description | filterNull }} </span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Create Time')" width="135">
          <template v-slot="scope">
            <span>
              {{ scope.row.createTime | formatDate }}
            </span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Update Time')" width="135">
          <template v-slot="scope">
            <span>
              {{ scope.row.updateTime | formateDate }}
            </span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Operation')" width="200" fixed="right">
          <template v-slot="scope">
            <el-tooltip
              :content="$t('Edit')"
              placement="top"
              :enterable="false"
            >
              <span>
                <el-button
                  type="primary"
                  size="mini"
                  icon="el-icon-edit-outline"
                  circle
                  @click="_edit(scope.row)"
                ></el-button>
              </span>
            </el-tooltip>
            <el-tooltip
              :content="$t('Delete')"
              placement="top"
              :enterable="false"
            >
              <el-popconfirm
                :confirmButtonText="$t('Confirm')"
                :cancelButtonText="$t('Cancel')"
                icon="el-icon-info"
                iconColor="red"
                :title="$t('Delete?')"
                @onConfirm="_delete(scope.row.code, scope.row.projectCode)"
              >
                <el-button
                  type="danger"
                  size="mini"
                  icon="el-icon-delete"
                  slot="reference"
                  circle
                >
                </el-button>
              </el-popconfirm>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'

  export default {
    name: 'task-list',
    data () {
      return {
        list: []
      }
    },
    props: {
      tasksList: Array
    },
    watch: {
      tasksList: {
        handler (a) {
          this.list = []
          setTimeout(() => {
            this.list = _.cloneDeep(a)
          })
        },
        immediate: true,
        deep: true
      }
    },
    created () {
    // this.list = this.tasksList
    },
    methods: {
      ...mapActions('dag', ['deleteTaskDefinition']),
      /**
       * onUpdate
       */
      _onUpdate () {
        this.$emit('on-update')
      },
      /**
       * deleteTaskDefinition
       */
      _delete (code, projectCode) {
        this.deleteTaskDefinition({
          code: code
        })
          .then((res) => {
            this._onUpdate()
            this.$message.success(res.msg)
          })
          .catch((e) => {
            this.$message.error(e.msg || '')
          })
      },
      /**
       * taskdefinition detail
       */
      _switchTasks () {},
      /**
       * task edit
       */
      _edit () {}
    }
  }
</script>

<style>
</style>
