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
        <el-table-column type="index" :label="$t('#')" width="100"></el-table-column>
        <el-table-column prop="name" :label="$t('TQname')" min-width="200"></el-table-column>
        <el-table-column prop="description" :label="$t('TQdes')" width="200"></el-table-column>
        <el-table-column prop="groupSize" :label="$t('TQnum')" width="200"></el-table-column>
        <el-table-column prop="useSize" :label="$t('TQusernum')" min-width="100"></el-table-column>
        <el-table-column prop="createTime" :label="$t('TQcreateTime')" width="300"></el-table-column>
        </el-table-column>
                <el-table-column :label="$t('Operation')" width="150" fixed="right">
                  <template slot-scope="scope">
                    <el-tooltip  v-if="scope.row.status == 1"
                          :content="$t('TQstart')" placement="top">
                        <el-popconfirm
                          :confirmButtonText="$t('Confirm')"
                          :cancelButtonText="$t('Cancel')"
                          icon="el-icon-info"
                          iconColor="red"
                          :title="$t('Start?')"
                          @onConfirm="_startTQ(scope.row,scope.row.id)"
                        >
                          <el-button type="primary" size="mini" icon="el-icon-video-play" circle slot="reference"></el-button>
                        </el-popconfirm>
                      </el-tooltip>
                    <el-tooltip :content="$t('Edit')" placement="top">
                      <el-button type="info" size="mini" icon="el-icon-edit-outline" @click="_edit(scope.row)" circle></el-button>
                    </el-tooltip>
                    <el-tooltip :content="$t('TQcheck')" placement="top">
                      <el-button type="primary" size="mini" icon="el-icon-view" @click="_edit(scope.row)" circle></el-button>
                    </el-tooltip>
                    <el-tooltip  v-if="scope.row.status == 1"
                        :content="$t('Close')" placement="top">
                      <el-popconfirm
                        :confirmButtonText="$t('Confirm')"
                        :cancelButtonText="$t('Cancel')"
                        icon="el-icon-info"
                        iconColor="red"
                        :title="$t('Close?')"
                        @onConfirm="_closeTQ(scope.row,scope.row.id)"
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
      ...mapActions('security', ['closeTaskGroup','startTaskGroup', 'getAuthList', 'grantAuthorization', 'getResourceList']),
      _closeTQ (item, i) {
        this.closeTaskGroup({
          id: item.id
        }).then(res => {
          this.$emit('on-update')
          this.$message.success(res.msg)
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      _startTQ (item, i) {
              this.startTaskGroup({
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
