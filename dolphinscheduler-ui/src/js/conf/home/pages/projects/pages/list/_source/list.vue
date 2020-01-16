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
      <table class="fixed">
        <tr>
          <th scope="col">
            <span>{{$t('#')}}</span>
          </th>
          <th scope="col">
            <span>{{$t('Project Name')}}</span>
          </th>
          <th scope="col">
            <span>{{$t('Owned Users')}}</span>
          </th>
          <th scope="col">
            <span>{{$t('Process Define Count')}}</span>
          </th>
          <th scope="col">
            <span>{{$t('Process Instance Running Count')}}</span>
          </th>
          <th scope="col">
            <span>{{$t('Description')}}</span>
          </th>
          <th scope="col">
            <span>{{$t('Create Time')}}</span>
          </th>
          <th scope="col">
            <span>{{$t('Update Time')}}</span>
          </th>
          <th scope="col" width="80">
            <span>{{$t('Operation')}}</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in list" :key="$index">
          <td>
            <span>{{parseInt(pageNo === 1 ? ($index + 1) : (($index + 1) + (pageSize * (pageNo - 1))))}}</span>
          </td>
          <td>
            <span>
              <a href="javascript:" @click="_switchProjects(item)" class="links">{{item.name}}</a>
            </span>
          </td>
          <td>
            <span>{{item.userName || '-'}}</span>
          </td>
          <td>
            <span>{{item.defCount}}</span>
          </td>
          <td>
            <span>{{item.instRunningCount}}</span>
          </td>
          <td>
            <span v-if="item.description" class="ellipsis" v-tooltip.large.top.start.light="{text: item.description, maxWidth: '500px'}">{{item.description}}</span>
            <span v-else>-</span>
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
            <x-button
                    type="info"
                    shape="circle"
                    size="xsmall"
                    data-toggle="tooltip"
                    :title="$t('Edit')"
                    @click="_edit(item)"
                    icon="ans-icon-edit">
            </x-button>
            <x-poptip
                    :ref="'poptip-' + $index"
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
        this.$router.push({ path: `/projects/index` })
      },
      _closeDelete (i) {
        this.$refs[`poptip-${i}`][0].doClose()
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
          this.$refs[`poptip-${i}`][0].doClose()
          this.$emit('on-update')
          this.$message.success(res.msg)
        }).catch(e => {
          this.$refs[`poptip-${i}`][0].doClose()
          this.$message.error(e.msg || '')
        })
      },
      /**
       * edit project
       * @param item Current record
       */
      _edit (item) {
        findComponentDownward(this.$root, 'projects-list')._create(item)
      },

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
