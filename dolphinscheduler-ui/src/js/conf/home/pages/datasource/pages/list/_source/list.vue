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
            <span>{{$t('Datasource Name')}}</span>
          </th>
          <th scope="col" width="120">
            <span>{{$t('Datasource Type')}}</span>
          </th>
          <th scope="col" width="100">
            <span>{{$t('Datasource Parameter')}}</span>
          </th>
          <th scope="col">
            <span>{{$t('Description')}}</span>
          </th>
          <th scope="col" width="150">
            <span>{{$t('Create Time')}}</span>
          </th>
          <th scope="col" width="150">
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
            <span class="ellipsis">
              {{item.name}}
            </span>
          </td>
          <td>
            <span>{{item.type}}</span>
          </td>
          <td>
            <m-tooltips-JSON :JSON="JSON.parse(item.connectionParams)" :id="item.id">
              <span slot="reference">
                <a href="javascript:" class="links" style="font-size: 12px;">{{$t('Click to view')}}</a>
              </span>
            </m-tooltips-JSON>
          </td>
          <td>
            <span v-if="item.note" class="ellipsis" v-tooltip.large.top.start.light="{text: item.note, maxWidth: '500px'}">{{item.note}}</span>
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
                    icon="ans-icon-edit"
                    @click="_edit(item)">
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
                        icon="ans-icon-trash"
                        data-toggle="tooltip"
                        :title="$t('delete')">
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
       * Close delete popup layer
       */
      _closeDelete (i) {
        this.$refs[`poptip-delete-${i}`][0].doClose()
      },
      /**
       * Delete current line
       */
      _delete (item, i) {
        this.$refs[`poptip-delete-${i}`][0].doClose()
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
