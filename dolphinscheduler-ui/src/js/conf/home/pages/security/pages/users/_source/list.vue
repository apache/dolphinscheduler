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
      <table>
        <tr>
          <th>
            <span>{{$t('#')}}</span>
          </th>
          <th>
            <span>{{$t('User Name')}}</span>
          </th>
          <th>
            <span>用户类型</span>
          </th>
          <th>
            <span>{{$t('Tenant')}}</span>
          </th>
          <th>
            <span>{{$t('Queue')}}</span>
          </th>
          <th>
            <span>{{$t('Email')}}</span>
          </th>
          <th>
            <span>{{$t('Phone')}}</span>
          </th>

          <th>
            <span>{{$t('Create Time')}}</span>
          </th>
          <th>
            <span>{{$t('Update Time')}}</span>
          </th>
          <th width="120">
            <span>{{$t('Operation')}}</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in list" :key="item.id">
          <td>
            <span>{{parseInt(pageNo === 1 ? ($index + 1) : (($index + 1) + (pageSize * (pageNo - 1))))}}</span>
          </td>
          <td>
            <span>
              {{item.userName || '-'}}
            </span>
          </td>
          <td>
            <span>{{item.userType === 'GENERAL_USER' ? `${$t('Ordinary users')}` : `${$t('Administrator')}`}}</span>
          </td>
          <td><span>{{item.tenantName || '-'}}</span></td>
          <td><span>{{item.queue || '-'}}</span></td>
          <td>
            <span>{{item.email || '-'}}</span>
          </td>
          <td>
            <span>{{item.phone || '-'}}</span>
          </td>
          <td>
            <span v-if="item.createTime">{{item.createTime | formatDate}}</span>
            <span v-else>-</span>
          </td>
          <td>
            <span v-if="item.updateTime">{{item.updateTime | formatDate}}</span>
            <span v-else>-</span>
          </td>
          <td>
            <x-poptip
                    :ref="'poptip-auth-' + $index"
                    popper-class="user-list-poptip"
                    placement="bottom-end">
              <div class="auth-select-box">
                <a href="javascript:" @click="_authProject(item,$index)">{{$t('Project')}}</a>
                <a href="javascript:" @click="_authFile(item,$index)">{{$t('Resources')}}</a>
                <a href="javascript:" @click="_authDataSource(item,$index)">{{$t('Datasource')}}</a>
                <a href="javascript:" @click="_authUdfFunc(item,$index)">{{$t('UDF Function')}}</a>
              </div>
              <template slot="reference">
                <x-button type="warning" shape="circle" size="xsmall" data-toggle="tooltip" :title="$t('Authorize')" icon="ans-icon-user-empty" :disabled="item.userType === 'ADMIN_USER'"></x-button>
              </template>
            </x-poptip>

            <x-button type="info" shape="circle" size="xsmall" data-toggle="tooltip" icon="ans-icon-edit" :title="$t('Edit')" @click="_edit(item)">
            </x-button>
            <x-poptip
                    :ref="'poptip-delete-' + $index"
                    placement="bottom-end"
                    width="90">
              <p>{{$t('Delete?')}}</p>
              <div style="text-align: right; margin: 0;padding-top: 4px;">
                <x-button type="text" size="xsmall" shape="circle" @click="_closeDelete($index)">{{$t('Cancel')}}</x-button>
                <x-button type="primary" size="xsmall" shape="circle" @click="_delete(item,$index)">{{$t('Confirm')}}</x-button>
              </div>
              <template slot="reference">
                <x-button
                        type="error"
                        shape="circle"
                        size="xsmall"
                        data-toggle="tooltip"
                        :title="$t('delete')"
                        :disabled="item.userType === 'ADMIN_USER'"
                        icon="ans-icon-trash">
                </x-button>
              </template>
            </x-poptip>
          </td>
        </tr>
      </table>
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
          this.$refs[`poptip-delete-${i}`][0].doClose()
          this.$emit('on-update')
          this.$message.success(res.msg)
        }).catch(e => {
          this.$refs[`poptip-delete-${i}`][0].doClose()
          this.$message.error(e.msg || '')
        })
      },
      _edit (item) {
        this.$emit('on-edit', item)
      },
      _authProject (item, i) {
        this.$refs[`poptip-auth-${i}`][0].doClose()
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
        this.$refs[`poptip-auth-${i}`][0].doClose()
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
        this.$refs[`poptip-auth-${i}`][0].doClose()
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
        this.$refs[`poptip-auth-${i}`][0].doClose()
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
