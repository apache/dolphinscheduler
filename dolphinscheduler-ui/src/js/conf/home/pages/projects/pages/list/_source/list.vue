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
        <el-table-column :label="$t('Project Name')">
          <template slot-scope="scope">
            <el-popover trigger="hover" placement="top">
              <p>{{ scope.row.name }}</p>
              <div slot="reference" class="name-wrapper">
                <a href="javascript:" class="links" @click="_switchProjects(scope.row)">{{ scope.row.name }}</a>
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column prop="userName" :label="$t('Owned Users')"></el-table-column>
        <el-table-column prop="defCount" :label="$t('Process Define Count')"></el-table-column>
        <el-table-column prop="instRunningCount" :label="$t('Process Instance Running Count')"></el-table-column>
        <el-table-column :label="$t('Description')" width="200">
          <template slot-scope="scope">
            <span>{{scope.row.description | filterNull}}</span>
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
  import { mapActions, mapMutations } from 'vuex'
  import localStore from '@/module/util/localStorage'
  import { findComponentDownward } from '@/module/util/'

  export default {
    name: 'projects-list',
    data () {
      return {
        list: []
      }
    },
    props: {
      projectsList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('projects', ['deleteProjects']),
      ...mapMutations('dag', ['setProjectName']),
      _switchProjects (item) {
        this.setProjectName(item.name)
        localStore.setItem('projectName', `${item.name}`)
        localStore.setItem('projectId', `${item.id}`)
        this.$router.push({ path: '/projects/index' })
      },
      /**
       * Delete Project
       * @param item Current record
       * @param i index
       */
      _delete (item, i) {
        this.deleteProjects({
          projectId: item.id
        }).then(res => {
          this.$emit('on-update')
          this.$message.success(res.msg)
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      /**
       * edit project
       * @param item Current record
       */
      _edit (item) {
        findComponentDownward(this.$root, 'projects-list')._create(item)
      }

    },
    watch: {
      projectsList (a) {
        this.list = []
        setTimeout(() => {
          this.list = a
        })
      }
    },
    created () {
    },
    mounted () {
      this.list = this.projectsList
    },
    components: { }
  }
</script>
