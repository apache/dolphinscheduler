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
        <el-table-column type="index" :label="$t('#')" min-width="120"></el-table-column>
        <el-table-column :label="$t('UDF Function Name')">
          <template slot-scope="scope">
            <el-popover trigger="hover" placement="top">
              <p>{{ scope.row.funcName }}</p>
              <div slot="reference" class="name-wrapper">
                <a href="javascript:" class="links">{{ scope.row.funcName }}</a>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="className" :label="$t('Class Name')" min-width="120"></el-table-column>
        <el-table-column prop="type" :label="$t('type')"></el-table-column>
        <el-table-column :label="$t('Description')" min-width="150">
          <template slot-scope="scope">
            <span>{{scope.row.description | filterNull}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="resourceName" :label="$t('Jar Package')" min-width="150"></el-table-column>
        <el-table-column :label="$t('Update Time')" min-width="120">
          <template slot-scope="scope">
            <span>{{scope.row.updateTime | formatDate}}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Operation')" min-width="100">
          <template slot-scope="scope">
            <el-tooltip :content="$t('Rename')" placement="top" :enterable="false">
              <span><el-button type="primary" size="mini" icon="el-icon-edit-outline" @click="_edit(scope.row)" circle></el-button></span>
            </el-tooltip>
            <el-tooltip :content="$t('delete')" placement="top" :enterable="false">
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
      :visible.sync="createUdfDialog"
      width="auto">
      <m-create-udf :item="item" @onUpdate="onUpdate" @close="close"></m-create-udf>
    </el-dialog>
  </div>
</template>
<script>
  import { mapActions } from 'vuex'
  import mCreateUdf from './createUdf'

  export default {
    name: 'udf-manage-list',
    data () {
      return {
        list: [],
        spinnerLoading: false,
        createUdfDialog: false,
        item: {}
      }
    },
    props: {
      udfFuncList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('resource', ['deleteUdf']),
      _delete (item, i) {
        this.spinnerLoading = true
        this.deleteUdf({
          id: item.id
        }).then(res => {
          this.$emit('on-update')
          this.$message.success(res.msg)
          this.spinnerLoading = false
        }).catch(e => {
          this.$message.error(e.msg || '')
          this.spinnerLoading = false
        })
      },
      _edit (item) {
        this.item = item
        this.createUdfDialog = true
      },
      onUpdate () {
        this.$emit('on-update')
        this.createUdfDialog = false
      },
      close () {
        this.createUdfDialog = false
      }
    },
    watch: {
      udfFuncList (a) {
        this.list = []
        setTimeout(() => {
          this.list = a
        })
      }
    },
    created () {
    },
    mounted () {
      this.list = this.udfFuncList
    },
    components: { mCreateUdf }
  }
</script>
