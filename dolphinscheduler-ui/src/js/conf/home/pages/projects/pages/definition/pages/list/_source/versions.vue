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
  <div class="container">

    <div class="title-box">
      <span class="name">{{$t('Version Info')}}</span>
    </div>

    <div class="table-box" v-if="versionData.processDefinitionVersions.length > 0">
      <el-table :data="versionData.processDefinitionVersions" size="mini" style="width: 100%">
        <el-table-column type="index" :label="$t('#')" width="50"></el-table-column>
        <el-table-column prop="userName" :label="$t('Version')">
          <template slot-scope="scope">
            <span v-if="scope.row.version">
              <span v-if="scope.row.version === versionData.processDefinition.version" style="color: green"><strong>{{scope.row.version}} {{$t('Current Version')}}</strong></span>
              <span v-else>{{scope.row.version}}</span>
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" :label="$t('Description')"></el-table-column>
        <el-table-column :label="$t('Create Time')" min-width="120">
          <template slot-scope="scope">
            <span>{{scope.row.createTime | formatDate}}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Operation')" width="100">
          <template slot-scope="scope">
            <el-tooltip :content="$t('Switch To This Version')" placement="top">
              <el-popconfirm
                :confirmButtonText="$t('Confirm')"
                :cancelButtonText="$t('Cancel')"
                icon="el-icon-info"
                iconColor="red"
                :title="$t('Confirm Switch To This Version?')"
                @onConfirm="_mVersionSwitchProcessDefinitionVersion(scope.row)"
              >
                <el-button type="primary" size="mini" icon="el-icon-warning" circle slot="reference"></el-button>
              </el-popconfirm>
            </el-tooltip>
            <el-tooltip :content="$t('delete')" placement="top">
              <el-popconfirm
                :confirmButtonText="$t('Confirm')"
                :cancelButtonText="$t('Cancel')"
                icon="el-icon-info"
                iconColor="red"
                :title="$t('Delete?')"
                @onConfirm="_mVersionDeleteProcessDefinitionVersion(scope.row,scope.row.id)"
              >
                <el-button type="danger" size="mini" icon="el-icon-delete" circle slot="reference"></el-button>
              </el-popconfirm>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div v-if="versionData.processDefinitionVersions.length === 0">
      <m-no-data><!----></m-no-data>
    </div>

    <div v-if="versionData.processDefinitionVersions.length > 0">
      <div class="bottom-box">
        <el-pagination
            style="float:right"
            background
            @current-change="_mVersionGetProcessDefinitionVersionsPage"
            layout="prev, pager, next"
            :total="versionData.total">
          </el-pagination>
        <el-button type="text" size="mini" @click="_close()" style="float:right">{{$t('Cancel')}}</el-button>
      </div>
    </div>

  </div>
</template>

<script>
  import mNoData from '@/module/components/noData/noData'

  export default {
    name: 'versions',
    data () {
      return {
        tableHeaders: [
          {
            label: 'version',
            prop: 'version'
          },
          {
            label: 'createTime',
            prop: 'createTime'
          }
        ]
      }
    },
    props: {
      versionData: Object
    },
    methods: {
      /**
       * switch version in process definition version list
       */
      _mVersionSwitchProcessDefinitionVersion (item) {
        this.$emit('mVersionSwitchProcessDefinitionVersion', {
          version: item.version,
          processDefinitionId: this.versionData.processDefinition.id,
          fromThis: this
        })
      },

      /**
       * delete one version of process definition
       */
      _mVersionDeleteProcessDefinitionVersion (item) {
        this.$emit('mVersionDeleteProcessDefinitionVersion', {
          version: item.version,
          processDefinitionId: this.versionData.processDefinition.id,
          fromThis: this
        })
      },

      /**
       * Paging event of process definition versions
       */
      _mVersionGetProcessDefinitionVersionsPage (val) {
        this.$emit('mVersionGetProcessDefinitionVersionsPage', {
          pageNo: val,
          pageSize: this.pageSize,
          processDefinitionId: this.versionData.processDefinition.id,
          fromThis: this
        })
      },
      /**
       * Close and destroy component and component internal events
       */
      _close () {
        // flag Whether to delete a node this.$destroy()
        this.$emit('closeVersion')
      }
    },
    created () {
    },
    mounted () {
    },
    components: { mNoData }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .container {
    width: 500px;
    position: relative;

    .title-box {
      height: 61px;
      border-bottom: 1px solid #DCDEDC;
      position: relative;

      .name {
        position: absolute;
        left: 24px;
        top: 18px;
        font-size: 16px;
      }
    }

    .bottom-box {
      position: absolute;
      bottom: 0;
      left: 0;
      width: 100%;
      text-align: right;
      height: 60px;
      line-height: 60px;
      border-top: 1px solid #DCDEDC;
      background: #fff;

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
