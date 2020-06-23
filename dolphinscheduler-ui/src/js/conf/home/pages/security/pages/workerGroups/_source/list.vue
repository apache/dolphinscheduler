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
      <table>
        <tr>
          <th>
            <span>{{$t('#')}}</span>
          </th>
          <th>
            <span>{{$t('Group')}}</span>
          </th>
          <th>
            <span>IPList</span>
          </th>
          <th>
            <span>{{$t('Create Time')}}</span>
          </th>
          <th>
            <span>{{$t('Update Time')}}</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in list" :key="$index">
          <td>
            <span>{{parseInt(pageNo === 1 ? ($index + 1) : (($index + 1) + (pageSize * (pageNo - 1))))}}</span>
          </td>
          <td>
            <span>
              {{item.name}}
            </span>
          </td>
          <td>
            <span>{{item.ipList.join(',')}}</span>
          </td>
          <td>
            <span v-if="item.createTime">{{item.createTime | formatDate}}</span>
            <span v-else>-</span>
          </td>
          <td>
            <span v-if="item.updateTime">{{item.updateTime | formatDate}}</span>
            <span v-else>-</span>
          </td>
        </tr>
      </table>
    </div>
  </div>
</template>
<script>
  import { mapActions } from 'vuex'

  export default {
    name: 'user-list',
    data () {
      return {
        list: []
      }
    },
    props: {
      workerGroupList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('security', ['deleteWorkerGroups']),
      _closeDelete (i) {
        this.$refs[`poptip-delete-${i}`][0].doClose()
      },
      _delete (item, i) {
        this.deleteWorkerGroups({
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
      }
    },
    watch: {
      workerGroupList (a) {
        this.list = []
        setTimeout(() => {
          this.list = a
        })
      }
    },
    created () {
      this.list = this.workerGroupList
    },
    mounted () {},
    components: {},
  }
</script>