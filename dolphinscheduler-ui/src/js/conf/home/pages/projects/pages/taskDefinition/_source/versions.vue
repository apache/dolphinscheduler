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
  <el-drawer
    :title="$t('Version Info')"
    :visible.sync="visible"
    :with-header="false"
    size=""
  >
    <!-- fix the bug that Element-ui(2.13.2) auto focus on the first input -->
    <div style="width: 0px; height: 0px; overflow: hidden">
      <el-input type="text" />
    </div>
    <div class="container">
      <div class="versions-header">
        <span class="name">{{ $t("Version Info") }}</span>
      </div>
      <div class="table-box" v-if="taskVersions.length > 0">
        <el-table :data="taskVersions" size="mini" style="width: 100%">
          <el-table-column
            type="index"
            :label="$t('#')"
            width="50"
          ></el-table-column>
          <el-table-column prop="userName" :label="$t('Version')">
            <template slot-scope="scope">
              <span v-if="scope.row.version">
                <span
                  v-if="scope.row.version === taskRow.taskVersion"
                  style="color: green"
                  ><strong
                    >V{{ scope.row.version }}
                    {{ $t("Current Version") }}</strong
                  ></span
                >
                <span v-else>V{{ scope.row.version }}</span>
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="description"
            :label="$t('Description')"
          ></el-table-column>
          <el-table-column :label="$t('Create Time')" min-width="120">
            <template slot-scope="scope">
              <span>{{ scope.row.updateTime | formatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="$t('Operation')" width="100">
            <template slot-scope="scope">
              <el-tooltip
                :content="$t('Switch To This Version')"
                placement="top"
              >
                <el-popconfirm
                  :confirmButtonText="$t('Confirm')"
                  :cancelButtonText="$t('Cancel')"
                  icon="el-icon-info"
                  iconColor="red"
                  :title="$t('Confirm Switch To This Version?')"
                  @onConfirm="swtichVersion(scope.row)"
                >
                  <el-button
                    :disabled="
                      taskRow.processReleaseState === 'ONLINE' ||
                      scope.row.version === taskRow.taskVersion
                    "
                    type="primary"
                    size="mini"
                    icon="el-icon-warning"
                    circle
                    slot="reference"
                  ></el-button>
                </el-popconfirm>
              </el-tooltip>
              <el-tooltip :content="$t('Delete')" placement="top">
                <el-popconfirm
                  :confirmButtonText="$t('Confirm')"
                  :cancelButtonText="$t('Cancel')"
                  icon="el-icon-info"
                  iconColor="red"
                  :title="$t('Delete?')"
                  @onConfirm="deleteVersion(scope.row)"
                >
                  <el-button
                    :disabled="scope.row.version === taskRow.taskVersion"
                    type="danger"
                    size="mini"
                    icon="el-icon-delete"
                    circle
                    slot="reference"
                  ></el-button>
                </el-popconfirm>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="taskVersions.length === 0">
        <m-no-data />
      </div>

      <div v-if="taskVersions.length > 0">
        <div class="versions-footer">
          <el-button size="mini" @click="close()">{{ $t("Cancel") }}</el-button>
          <el-pagination
            background
            @current-change="changePageNo"
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
          >
          </el-pagination>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script>
  import mNoData from '@/module/components/noData/noData'
  import { mapActions } from 'vuex'

  export default {
    name: 'task-definition-versions',
    data () {
      return {
        visible: false,
        taskVersions: [],
        pageNo: 1,
        pageSize: 10,
        total: 0
      }
    },
    props: {
      taskRow: Object
    },
    methods: {
      ...mapActions('dag', [
        'getTaskVersions',
        'switchTaskVersion',
        'deleteTaskVersion'
      ]),
      show () {
        this.visible = true
      },
      close () {
        this.visible = false
        this.taskVersions = []
      },
      changePageNo (val) {
        this.pageNo = val
        this.reload()
      },
      reload () {
        this.getTaskVersions({
          taskCode: this.taskRow.taskCode,
          pageNo: this.pageNo,
          pageSize: this.pageSize
        }).then((res) => {
          this.taskVersions = res.totalList
          this.total = res.total
        })
      },
      swtichVersion (row) {
        this.switchTaskVersion({ taskCode: row.code, version: row.version })
          .then((res) => {
            this.$message.success(res.msg)
            this.$emit('reloadList')
            this.close()
          })
          .catch((err) => {
            this.$message.error(err.msg || '')
          })
      },
      deleteVersion (row) {
        this.deleteTaskVersion({ taskCode: row.code, version: row.version })
          .then((res) => {
            this.$message.success(res.msg)
            this.$emit('reloadList')
            this.close()
          })
          .catch((err) => {
            this.$message.error(err.msg || '')
          })
      }
    },
    components: { mNoData },
    watch: {
      visible (bool, a, b) {
        if (bool && this.taskRow) {
          this.reload()
        }
      }
    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
.container {
  width: 500px;
  position: relative;

  .versions-header {
    height: 60px;
    position: relative;
    line-height: 60px;

    .name {
      font-size: 16px;
    }
  }

  .versions-footer {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    border-top: 1px solid #dcdedc;
    background: #fff;
    display: flex;
    padding: 20px;
    align-items: center;
    justify-content: flex-end;

    .ans-page {
      display: inline-block;
    }
  }

  .table-box {
    overflow-y: scroll;
    height: calc(100vh - 61px);
    padding-bottom: 60px;
  }
}
</style>
