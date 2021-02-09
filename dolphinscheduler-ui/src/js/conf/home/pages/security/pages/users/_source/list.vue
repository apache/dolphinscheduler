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
  <div class="list-model user-list-model">
    <div class="table-box">
      <el-table :data="list" size="mini" style="width: 100%">
        <el-table-column type="index" :label="$t('#')" width="50"></el-table-column>
        <el-table-column prop="userName" :label="$t('User Name')"></el-table-column>
        <el-table-column :label="$t('User Type')" width="80">
          <template slot-scope="scope">
            {{scope.row.userType === 'GENERAL_USER' ? `${$t('Ordinary users')}` : `${$t('Administrator')}`}}
          </template>
        </el-table-column>
        <el-table-column prop="tenantCode" :label="$t('Tenant')" min-width="120"></el-table-column>
        <el-table-column prop="queue" :label="$t('Queue')" width="90"></el-table-column>
        <el-table-column prop="email" :label="$t('Email')" min-width="200"></el-table-column>
        <el-table-column prop="phone" :label="$t('Phone')" width="100">
          <template slot-scope="scope">
            <span>{{scope.row.phone | filterNull}}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('State')" width="60">
          <template slot-scope="scope">
            {{scope.row.state === 1 ? `${$t('Enable')}` : `${$t('Disable')}`}}
          </template>
        </el-table-column>
        <el-table-column :label="$t('Create Time')" width="135">
          <template slot-scope="scope">
            <span>{{scope.row.createTime | formatDate}}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Update Time')" width="135">
          <template slot-scope="scope">
            <span>{{scope.row.updateTime | formatDate}}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Operation')" width="120" fixed="right">
          <template slot-scope="scope">
            <el-tooltip :content="$t('Authorize')" placement="top">
              <el-dropdown trigger="click">
                <el-button type="warning" size="mini" icon="el-icon-user" circle></el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item @click.native="_authProject(scope.row,scope.$index)">{{$t('Project')}}</el-dropdown-item>
                  <el-dropdown-item @click.native="_authFile(scope.row,scope.$index)">{{$t('Resources')}}</el-dropdown-item>
                  <el-dropdown-item @click.native="_authDataSource(scope.row,scope.$index)">{{$t('Datasource')}}</el-dropdown-item>
                  <el-dropdown-item @click.native="_authUdfFunc(scope.row,scope.$index)">{{$t('UDF Function')}}</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </el-tooltip>
            <el-tooltip :content="$t('Edit')" placement="top">
              <el-button type="primary" size="mini" icon="el-icon-edit-outline" @click="_edit(scope.row)" circle></el-button>
            </el-tooltip>
            <el-tooltip :content="$t('delete')" placement="top">
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
    <el-dialog
      v-if="authProjectDialog"
      :visible.sync="authProjectDialog"
      width="auto">
      <m-transfer :transferData="transferData" @onUpdateAuthProject="onUpdateAuthProject" @closeAuthProject="closeAuthProject"></m-transfer>
    </el-dialog>

    <el-dialog
      v-if="authDataSourceDialog"
      :visible.sync="authDataSourceDialog"
      width="auto">
      <m-transfer :transferData="transferData" @onUpdateAuthDataSource="onUpdateAuthDataSource" @closeAuthDataSource="closeAuthDataSource"></m-transfer>
    </el-dialog>

    <el-dialog
      v-if="authUdfFuncDialog"
      :visible.sync="authUdfFuncDialog"
      width="auto">
      <m-transfer :transferData="transferData" @onUpdateAuthUdfFunc="onUpdateAuthUdfFunc" @closeAuthUdfFunc="closeAuthUdfFunc"></m-transfer>
    </el-dialog>

    <el-dialog
      v-if="resourceDialog"
      :visible.sync="resourceDialog"
      width="auto">
      <m-resource :resourceData="resourceData" @onUpdateAuthResource="onUpdateAuthResource" @closeAuthResource="closeAuthResource"></m-resource>
    </el-dialog>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import { mapActions } from 'vuex'
  import mTransfer from '@/module/components/transfer/transfer'
  import mResource from '@/module/components/transfer/resource'

  export default {
    name: 'user-list',
    data () {
      return {
        list: [],
        authProjectDialog: false,
        transferData: {
          sourceListPrs: [],
          targetListPrs: [],
          type: {
            name: ''
          }
        },
        item: {},
        authDataSourceDialog: false,
        authUdfFuncDialog: false,
        resourceData: {
          fileSourceList: [],
          udfSourceList: [],
          fileTargetList: [],
          udfTargetList: [],
          type: {
            name: ''
          }
        },
        resourceDialog: false
      }
    },
    props: {
      userList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('security', ['deleteUser', 'getAuthList', 'grantAuthorization', 'getResourceList']),
      _delete (item, i) {
        this.deleteUser({
          id: item.id
        }).then(res => {
          this.$emit('on-update')
          this.$message.success(res.msg)
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      _edit (item) {
        this.$emit('on-edit', item)
      },
      _authProject (item, i) {
        this.getAuthList({
          id: item.id,
          type: 'project',
          category: 'projects'
        }).then(data => {
          let sourceListPrs = _.map(data[0], v => {
            return {
              id: v.id,
              name: v.name
            }
          })
          let targetListPrs = _.map(data[1], v => {
            return {
              id: v.id,
              name: v.name
            }
          })
          this.item = item
          this.transferData.sourceListPrs = sourceListPrs
          this.transferData.targetListPrs = targetListPrs
          this.transferData.type.name = `${i18n.$t('Project')}`
          this.authProjectDialog = true
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      onUpdateAuthProject (projectIds) {
        this._grantAuthorization('users/grant-project', {
          userId: this.item.id,
          projectIds: projectIds
        })
        this.authProjectDialog = false
      },

      closeAuthProject () {
        this.authProjectDialog = false
      },

      /*
        getAllLeaf
       */
      getAllLeaf (data) {
        let result = []
        let getLeaf = (data) => {
          data.forEach(item => {
            if (item.children.length === 0) {
              result.push(item)
            } else {
              getLeaf(item.children)
            }
          })
        }
        getLeaf(data)
        return result
      },
      _authFile (item, i) {
        this.getResourceList({
          id: item.id,
          type: 'file',
          category: 'resources'
        }).then(data => {
          let fileSourceList = []
          let udfSourceList = []
          data[0].forEach((value, index, array) => {
            if (value.type === 'FILE') {
              fileSourceList.push(value)
            } else {
              udfSourceList.push(value)
            }
          })
          let fileTargetList = []
          let udfTargetList = []

          let pathId = []
          data[1].forEach(v => {
            let arr = []
            arr[0] = v
            if (this.getAllLeaf(arr).length > 0) {
              pathId.push(this.getAllLeaf(arr)[0])
            }
          })
          data[1].forEach((value, index, array) => {
            if (value.type === 'FILE') {
              fileTargetList.push(value)
            } else {
              udfTargetList.push(value)
            }
          })
          fileTargetList = _.map(fileTargetList, v => {
            return v.id
          })
          udfTargetList = _.map(udfTargetList, v => {
            return v.id
          })
          this.item = item
          this.resourceData.fileSourceList = fileSourceList
          this.resourceData.udfSourceList = udfSourceList
          this.resourceData.fileTargetList = fileTargetList
          this.resourceData.udfTargetList = udfTargetList
          this.resourceData.type.name = `${i18n.$t('Resources')}`
          this.resourceDialog = true
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },

      onUpdateAuthResource (resourceIds) {
        this._grantAuthorization('users/grant-file', {
          userId: this.item.id,
          resourceIds: resourceIds
        })
        this.resourceDialog = false
      },

      closeAuthResource () {
        this.resourceDialog = false
      },

      _authDataSource (item, i) {
        this.getAuthList({
          id: item.id,
          type: 'datasource',
          category: 'datasources'
        }).then(data => {
          let sourceListPrs = _.map(data[0], v => {
            return {
              id: v.id,
              name: v.name
            }
          })
          let targetListPrs = _.map(data[1], v => {
            return {
              id: v.id,
              name: v.name
            }
          })
          this.item = item
          this.transferData.sourceListPrs = sourceListPrs
          this.transferData.targetListPrs = targetListPrs
          this.transferData.type.name = `${i18n.$t('Datasource')}`
          this.authDataSourceDialog = true
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      onUpdateAuthDataSource (datasourceIds) {
        this._grantAuthorization('users/grant-datasource', {
          userId: this.item.id,
          datasourceIds: datasourceIds
        })
        this.authDataSourceDialog = false
      },
      closeAuthDataSource () {
        this.authDataSourceDialog = false
      },

      _authUdfFunc (item, i) {
        this.getAuthList({
          id: item.id,
          type: 'udf-func',
          category: 'resources'
        }).then(data => {
          let sourceListPrs = _.map(data[0], v => {
            return {
              id: v.id,
              name: v.funcName
            }
          })
          let targetListPrs = _.map(data[1], v => {
            return {
              id: v.id,
              name: v.funcName
            }
          })
          this.item = item
          this.transferData.sourceListPrs = sourceListPrs
          this.transferData.targetListPrs = targetListPrs
          this.transferData.type.name = `${i18n.$t('UDF Function')}`
          this.authUdfFuncDialog = true
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      onUpdateAuthUdfFunc (udfIds) {
        this._grantAuthorization('users/grant-udf-func', {
          userId: this.item.id,
          udfIds: udfIds
        })
        this.authUdfFuncDialog = false
      },

      closeAuthUdfFunc () {
        this.authUdfFuncDialog = false
      },

      _grantAuthorization (api, param) {
        this.grantAuthorization({
          api: api,
          param: param
        }).then(res => {
          this.$message.success(res.msg)
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      }
    },
    watch: {
      userList (a) {
        this.list = []
        setTimeout(() => {
          this.list = a
        })
      }
    },
    created () {
      this.list = this.userList
    },
    mounted () {
    },
    components: { mTransfer, mResource }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .user-list-model {
    .user-list-poptip {
      min-width: 90px !important;
      .auth-select-box {
        a {
          font-size: 14px;
          height: 28px;
          line-height: 28px;
          display: block;
        }
      }
    }
  }
</style>
