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
        <el-table-column prop="groupName" :label="$t('Group Name')"></el-table-column>
        <el-table-column :label="$t('Group Type')" width="100">
          {{groupType === 'EMAIL' ? `${$t('Email')}` : `${$t('SMS')}`}}
        </el-table-column>
        <el-table-column prop="description" :label="$t('Remarks')" width="200"></el-table-column>
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
            <el-tooltip :content="$t('Managing Users')" placement="top">
              <el-button type="primary" size="mini" icon="el-icon-user" @click="_mangeUser(scope.row, scope.$index)" circle></el-button>
            </el-tooltip>
            <el-tooltip :content="$t('Edit')" placement="top">
              <span><el-button type="primary" size="mini" icon="el-icon-edit-outline" @click="_edit(scope.row)" circle></el-button></span>
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
                <el-button type="danger" size="mini" icon="el-icon-delete" circle slot="reference" :disabled="scope.row.id==1?true: false"></el-button>
              </el-popconfirm>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog
      :visible.sync="transferDialog"
      width="40%">
      <m-transfer :transferData="transferData" @onUpdate="onUpdate" @close="close"></m-transfer>
    </el-dialog>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import { mapActions } from 'vuex'
  import mTransfer from '@/module/components/transfer/transfer'

  export default {
    name: 'user-list',
    data () {
      return {
        list: [],
        transferDialog: false,
        item: {},
        transferData: {
          sourceListPrs: [],
          targetListPrs: [],
          type: {
            name: `${i18n.$t('Managing Users')}`
          }
        }
      }
    },
    props: {
      alertgroupList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('security', ['deleteAlertgrou', 'getAuthList', 'grantAuthorization']),
      _delete (item, i) {
        this.deleteAlertgrou({
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
      _mangeUser (item, i) {
        this.getAuthList({
          id: item.id,
          type: 'user',
          category: 'users'
        }).then(data => {
          let sourceListPrs = _.map(data[0], v => {
            return {
              id: v.id,
              name: v.userName
            }
          })
          let targetListPrs = _.map(data[1], v => {
            return {
              id: v.id,
              name: v.userName
            }
          })
          this.item = item
          this.transferData.sourceListPrs = sourceListPrs
          this.transferData.targetListPrs = targetListPrs
          this.transferDialog = true
        })
      },
      onUpdate (userIds) {
        this._grantAuthorization('alert-group/grant-user', {
          userIds: userIds,
          alertgroupId: this.item.id
        })
        this.transferDialog = false
      },
      close () {
        this.transferDialog = false
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
      alertgroupList (a) {
        this.list = []
        setTimeout(() => {
          this.list = a
        })
      }
    },
    created () {
      this.list = this.alertgroupList
    },
    mounted () {
    },
    components: { mTransfer }
  }
</script>