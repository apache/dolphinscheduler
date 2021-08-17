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
        <el-table-column prop="name" :label="$t('Name')"></el-table-column>
        <el-table-column :label="$t('Rule Type')" width="120">
          <template slot-scope="scope">
            {{_rtRuleType(scope.row.type)}}
          </template>
        </el-table-column>
        <el-table-column :label="$t('DataQuality Rule Json')">
          <template slot-scope="scope">
            <div>
              <m-tooltips-JSON :JSON="JSON.parse(scope.row.ruleJson)" :id="scope.row.id">
                <span slot="reference">
                  <el-button size="small" type="text">{{$t('Click to view')}}</el-button>
                </span>
            </m-tooltips-JSON>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="userName" :label="$t('User Name')"></el-table-column>
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
      </el-table>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import { ruleType } from '@/conf/home/pages/dataquality/_source/common'
  import mTooltipsJSON from '@/module/components/tooltipsJSON/tooltipsJSON'
  export default {
    name: 'rule-list',
    data () {
      return {
        list: []
      }
    },
    props: {
      ruleList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      _rtRuleType (code) {
        return _.filter(ruleType, v => v.code === code)[0].label
      }
    },
    watch: {
      ruleList (a) {
        this.list = []
        setTimeout(() => {
          this.list = a
        })
      }
    },
    created () {
      this.list = this.ruleList
    },
    mounted () {
    },
    components: { mTooltipsJSON }
  }
</script>
