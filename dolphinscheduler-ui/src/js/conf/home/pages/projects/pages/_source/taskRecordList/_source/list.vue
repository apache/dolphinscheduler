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
        <el-table-column :label="$t('Task Name')" min-width="100">
          <template slot-scope="scope">
            <el-popover trigger="hover" placement="top">
              <p>{{ scope.row.procName }}</p>
              <div slot="reference" class="name-wrapper">
                {{ scope.row.procName }}
              </div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Task Date')">
          <template slot-scope="scope">
            <span>{{scope.row.procDate | formatDate}}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Start Time')" width="135">
          <template slot-scope="scope">
            <span>{{scope.row.startTime | formatDate}}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('End Time')" width="135">
          <template slot-scope="scope">
            <span>{{scope.row.endTime | formatDate}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" :label="$t('Duration')+'(s)'"></el-table-column>
        <el-table-column prop="sourceTab" :label="$t('Source Table')"></el-table-column>
        <el-table-column prop="sourceRowCount" :label="$t('Record Number')"></el-table-column>
        <el-table-column prop="targetTab" :label="$t('Target Table')"></el-table-column>
        <el-table-column prop="targetRowCount" :label="$t('Record Number')"></el-table-column>
        <el-table-column prop="note" :label="$t('State')"></el-table-column>
      </el-table>
    </div>
  </div>
</template>
<script>
  export default {
    name: 'list',
    data () {
      return {
        list: [],
        backfillItem: {}
      }
    },
    props: {
      taskRecordList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      _rtTooltip (name) {
        return `<div style="word-wrap:break-word;text-align: left;">${name}</div>`
      }
    },
    watch: {
      taskRecordList (a) {
        this.list = []
        setTimeout(() => {
          this.list = a
        })
      }
    },
    created () {
    },
    mounted () {
      this.list = this.taskRecordList
    },
    components: { }
  }
</script>
