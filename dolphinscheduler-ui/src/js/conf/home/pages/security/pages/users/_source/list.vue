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
        <el-table-column :label="$t('User Type')">
          {{userType === 'GENERAL_USER' ? `${$t('Ordinary users')}` : `${$t('Administrator')}`}}
        </el-table-column>
        <el-table-column prop="tenantName" :label="$t('Tenant')" width="160"></el-table-column>
        <el-table-column prop="queue" :label="$t('Queue')"></el-table-column>
        <el-table-column prop="email" :label="$t('Email')" min-width="120"></el-table-column>
        <el-table-column prop="phone" :label="$t('Phone')" min-width="90"></el-table-column>
        <el-table-column :label="$t('State')">
          {{state == 1 ? `${$t('Enable')}` : `${$t('Disable')}`}}
        </el-table-column>
        <el-table-column :label="$t('Create Time')" min-width="120">
          <template slot-scope="scope">
            <span>{{scope.row.createTime | formatDate}}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Update Time')" min-width="120">
          <template slot-scope="scope">
            <span>{{scope.row.updateTime | formatDate}}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Operation')" width="130">
          <template slot-scope="scope">
            <el-dropdown trigger="click">
              <el-button type="warning" size="mini" icon="el-icon-user" circle></el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item @click.native="_authProject(scope.row,scope.row.id)">{{$t('Project')}}</el-dropdown-item>
                <el-dropdown-item @click.native="_authFile(scope.row,scope.row.id)">{{$t('Resources')}}</el-dropdown-item>
                <el-dropdown-item @click.native="_authDataSource(scope.row,scope.row.id)">{{$t('Datasource')}}</el-dropdown-item>
                <el-dropdown-item @click.native="_authUdfFunc(scope.row,scope.row.id)">{{$t('UDF Function')}}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
            <el-tooltip :content="$t('Edit')" placement="top">
              <el-button type="primary" size="mini" icon="el-icon-edit" @click="_edit(scope.row)" circle></el-button>
            </el-tooltip>
            <el-tooltip :content="$t('delete')" placement="top">
              <el-button type="danger" size="mini" icon="el-icon-delete" circle></el-button>
              <el-popconfirm
                :confirmButtonText="$t('Confirm')"
                :cancelButtonText="$t('Cancel')"
                icon="el-icon-info"
                iconColor="red"
                :title="$t('Delete?')"
                :disabled="scope.row.userType === 'ADMIN_USER'"
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
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import { mapActions } from 'vuex'
  import mTransfer from '@/module/components/transfer/transfer'
  import mResource from '@/module/components/transfer/resource'

  export default {
    name: 'user-list',
    data () {
      return {
        list: []
      }
    },
    props: {
      userList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('security', ['deleteUser', 'getAuthList', 'grantAuthorization','getResourceList']),
      _closeDelete (i) {
        this.$refs[`poptip-delete-${i}`][0].doClose()
      },
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
          let self = this
          let modal = this.$modal.dialog({
            closable: false,
            showMask: true,
            escClose: true,
            className: 'v-modal-custom',
            transitionName: 'opacityp',
            render (h) {
              return h(mTransfer, {
                on: {
                  onUpdate (projectIds) {
                    self._grantAuthorization('users/grant-project', {
                      userId: item.id,
                      projectIds: projectIds
                    })
                    modal.remove()
                  },
                  close () {
                    modal.remove()
                  }
                },
                props: {
                  sourceListPrs: sourceListPrs,
                  targetListPrs: targetListPrs,
                  type: {
                    name: `${i18n.$t('Project')}`
                  }
                }
              })
            }
          })
        })
      },
      /*
        getAllLeaf
       */
      getAllLeaf (data) {
        let result = []
        let getLeaf = (data)=> {
          data.forEach(item => {
            if (item.children.length==0) {
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
          // let sourceListPrs = _.map(data[0], v => {
          //   return {
          //     id: v.id,
          //     name: v.alias,
          //     type: v.type
          //   }
          // })
          let fileSourceList = []
          let udfSourceList = []
          data[0].forEach((value,index,array)=>{
            if(value.type =='FILE'){
              fileSourceList.push(value)
            } else{
              udfSourceList.push(value)
            }
          })
          let fileTargetList = []
          let udfTargetList = []

          let pathId = []
          data[1].forEach(v=>{
            let arr = []
            arr[0] = v
            if(this.getAllLeaf(arr).length>0) {
              pathId.push(this.getAllLeaf(arr)[0])
            }
          })
          data[1].forEach((value,index,array)=>{
            if(value.type =='FILE'){
              fileTargetList.push(value)
            } else{
              udfTargetList.push(value)
            }
          })
          fileTargetList = _.map(fileTargetList, v => {
            return v.id
          })
          udfTargetList = _.map(udfTargetList, v => {
            return v.id
          })
          let self = this
          let modal = this.$modal.dialog({
            closable: false,
            showMask: true,
            escClose: true,
            className: 'v-modal-custom',
            transitionName: 'opacityp',
            render (h) {
              return h(mResource, {
                on: {
                  onUpdate (resourceIds) {
                    self._grantAuthorization('users/grant-file', {
                      userId: item.id,
                      resourceIds: resourceIds
                    })
                    modal.remove()
                  },
                  close () {
                    modal.remove()
                  }
                },
                props: {
                  // sourceListPrs: sourceListPrs,
                  // targetListPrs: targetListPrs,
                  fileSourceList: fileSourceList,
                  udfSourceList: udfSourceList,
                  fileTargetList: fileTargetList,
                  udfTargetList: udfTargetList,
                  type: {
                    name: `${i18n.$t('Resources')}`
                  }
                }
              })
            }
          })
        })
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
          let self = this
          let modal = this.$modal.dialog({
            closable: false,
            showMask: true,
            escClose: true,
            className: 'v-modal-custom',
            transitionName: 'opacityp',
            render (h) {
              return h(mTransfer, {
                on: {
                  onUpdate (datasourceIds) {
                    self._grantAuthorization('users/grant-datasource', {
                      userId: item.id,
                      datasourceIds: datasourceIds
                    })
                    modal.remove()
                  },
                  close () {
                    modal.remove()
                  }
                },
                props: {
                  sourceListPrs: sourceListPrs,
                  targetListPrs: targetListPrs,
                  type: {
                    name: `${i18n.$t('Datasource')}`
                  }
                }
              })
            }
          })
        })
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
          let self = this
          let modal = this.$modal.dialog({
            closable: false,
            showMask: true,
            escClose: true,
            className: 'v-modal-custom',
            transitionName: 'opacityp',
            render (h) {
              return h(mTransfer, {
                on: {
                  onUpdate (udfIds) {
                    self._grantAuthorization('users/grant-udf-func', {
                      userId: item.id,
                      udfIds: udfIds
                    })
                    modal.remove()
                  },
                  close () {
                    modal.remove()
                  }
                },
                props: {
                  sourceListPrs: sourceListPrs,
                  targetListPrs: targetListPrs,
                  type: {
                    name: 'UDF Function'
                  }
                }
              })
            }
          })
        })
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
    components: { }
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
