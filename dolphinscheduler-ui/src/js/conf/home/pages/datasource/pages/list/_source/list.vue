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
        <el-table-column prop="name" :label="$t('Datasource Name')"></el-table-column>
        <el-table-column prop="userName" :label="$t('Datasource userName')"></el-table-column>
        <el-table-column prop="type" :label="$t('Datasource Type')"></el-table-column>
        <el-table-column :label="$t('Datasource Parameter')">
          <template slot-scope="scope">
            <div>
              <m-tooltips-JSON :JSON="JSON.parse(scope.row.connectionParams)" :id="scope.row.id">
                <span slot="reference">
                  <el-button size="small" type="text">{{$t('Click to view')}}</el-button>
                </span>
            </m-tooltips-JSON>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Description')" min-width="100">
          <template slot-scope="scope">
            <span>{{scope.row.note | filterNull}}</span>
          </template>
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
        <el-table-column :label="$t('Operation')" width="150">
          <template slot-scope="scope">
            <el-tooltip :content="$t('Edit')" placement="top" :enterable="false">
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
  </div>
</template>
<script>
  import { mapActions } from 'vuex'
  import { findComponentDownward } from '@/module/util/'
  import mTooltipsJSON from '@/module/components/tooltipsJSON/tooltipsJSON'

  export default {
    name: 'datasource-list',
    data () {
      return {
        // list
        list: []
      }
    },
    props: {
      // External incoming data
      datasourcesList: Array,
      // current page number
      pageNo: Number,
      // Total number of articles
      pageSize: Number
    },
    methods: {
      ...mapActions('datasource', ['deleteDatasource']),
      /**
       * Delete current line
       */
      _delete (item, i) {
        this.deleteDatasource({
          id: item.id
        }).then(res => {
          this.$emit('on-update')
          this.$message.success(res.msg)
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      /**
       * edit
       */
      _edit (item) {
        findComponentDownward(this.$root, 'datasource-indexP')._create(item)
      }
    },
    watch: {
      /**
       * Monitor external data changes
       */
      datasourcesList (a) {
        this.list = []
        setTimeout(() => {
          this.list = a
        })
      }
    },
    created () {
      this.list = this.datasourcesList
    },
    mounted () {
    },
    components: { mTooltipsJSON }
  }
</script>
