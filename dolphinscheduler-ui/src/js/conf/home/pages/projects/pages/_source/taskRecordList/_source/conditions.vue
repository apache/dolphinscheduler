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
  <m-conditions>
    <template slot="search-group">
      <div class="list">
        <el-button type="ghost" size="mini" @click="_ckQuery" icon="el-icon-search"></el-button>
      </div>
      <div class="list">
        <el-date-picker
          v-model="dataTime"
          type="datetimerange"
          size="mini"
          @change="_onChangeStartStop"
          range-separator="-"
          :start-placeholder="$t('startDate')"
          :end-placeholder="$t('endDate')"
          value-format="yyyy-MM-dd HH:mm:ss">
        </el-date-picker>
      </div>
      <div class="list">
        <el-input v-model="searchParams.destTable" style="width: 120px;" size="mini" :placeholder="$t('Target Table')"></el-input>
      </div>
      <div class="list">
        <el-input v-model="searchParams.sourceTable" style="width: 120px;" size="mini" :placeholder="$t('Source Table')"></el-input>
      </div>
      <div class="list">
        <el-select style="width: 90px;" @change="_onChangeState" :value="searchParams.state" :placeholder="$t('State')" size="mini">
          <el-option
                  v-for="city in stateList"
                  :key="city.label"
                  :value="city.code"
                  :label="city.label">
          </el-option>
        </el-select>
      </div>
      <div class="list">
        <el-date-picker
          v-model="searchParams.taskDate"
          value-format="yyyy-MM-dd"
          size="mini"
          @change="_onChangeDate"
          type="date"
          :placeholder="$t('Date')">
        </el-date-picker>
      </div>
      <div class="list">
        <el-input v-model="searchParams.taskName" style="width: 130px;" size="mini" :placeholder="$t('Task Name')"></el-input>
      </div>
    </template>
  </m-conditions>
</template>
<script>
  import _ from 'lodash'
  import mConditions from '@/module/components/conditions/conditions'
  export default {
    name: 'conditions',
    data () {
      return {
        stateList: [
          {
            label: `${this.$t('None')}`,
            code: ''
          },
          {
            label: `${this.$t('Success')}`,
            code: '成功'
          },
          {
            label: `${this.$t('Waiting')}`,
            code: '等待'
          },
          {
            label: `${this.$t('Execution')}`,
            code: '执行中'
          },
          {
            label: `${this.$t('Finish')}`,
            code: '完成'
          }, {
            label: `${this.$t('Failed')}`,
            code: '失败'
          }
        ],
        searchParams: {
          taskName: '',
          state: '',
          sourceTable: '',
          destTable: '',
          taskDate: '',
          startDate: '',
          endDate: ''
        },
        dataTime: []
      }
    },
    props: {},
    methods: {
      _ckQuery () {
        this.$emit('on-query', this.searchParams)
      },
      /**
       * change times
       */
      _onChangeStartStop (val) {
        this.searchParams.startDate = val[0]
        this.searchParams.endDate = val[1]
      },
      /**
       * change state
       */
      _onChangeState (val) {
        this.searchParams.state = val
      },
      /**
       * empty date
       */
      _dateEmpty () {
        this.searchParams.startDate = ''
        this.searchParams.endDate = ''
        this.$refs.datepicker.empty()
      },
      _onChangeDate (val) {
        this.searchParams.taskDate = val
      }
    },
    created () {
      // Routing parameter merging
      if (!_.isEmpty(this.$route.query)) {
        this.searchParams = _.assign(this.searchParams, this.$route.query)
      }
    },
    mounted () {
    },
    components: { mConditions }
  }
</script>
