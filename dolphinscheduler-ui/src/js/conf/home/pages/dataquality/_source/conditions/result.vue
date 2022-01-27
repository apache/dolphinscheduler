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
        <el-button size="mini" @click="_ckQuery" icon="el-icon-search"></el-button>
      </div>
      <div class="list">
        <el-date-picker
          style="width: 310px"
          v-model="dataTime"
          size="mini"
          @change="_onChangeStartStop"
          type="datetimerange"
          range-separator="-"
          :start-placeholder="$t('startDate')"
          :end-placeholder="$t('endDate')"
          value-format="yyyy-MM-dd HH:mm:ss">
        </el-date-picker>
      </div>
      <div class="list">
        <el-select style="width: 140px;" @change="_onChangeState" :value="searchParams.state" :placeholder="$t('State')" size="mini">
          <el-option
                  v-for="item in stateTypeList"
                  :key="item.label"
                  :value="item.code"
                  :label="item.label">
          </el-option>
        </el-select>
      </div>
      <div class="list">
        <el-select style="width: 140px;" @change="_onChangeRuleType" :value="searchParams.ruleType" :placeholder="$t('Rule Type')" size="mini">
          <el-option
                  v-for="item in ruleTypeList"
                  :key="item.label"
                  :value="item.code"
                  :label="item.label">
          </el-option>
        </el-select>
      </div>
      <div class="list">
        <el-input v-model="searchParams.searchVal" @keyup.enter.native="_ckQuery" style="width: 160px;" size="mini" :placeholder="$t('Task Name')"></el-input>
      </div>
    </template>
  </m-conditions>
</template>
<script>
  import _ from 'lodash'
  import { ruleType, dataQualityTaskState } from '../common'
  import mConditions from '@/module/components/conditions/conditions'
  export default {
    name: 'result-conditions',
    data () {
      return {
        // state(list)
        stateTypeList: dataQualityTaskState,
        // ruleType(list)
        ruleTypeList: ruleType,

        searchParams: {
          // state
          state: '',
          // start date
          startDate: '',
          // end date
          endDate: '',
          // search value
          searchVal: '',
          // host
          ruleType: -1
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
        if (val === null) {
          this.searchParams.startDate = ''
          this.searchParams.endDate = ''
          this.dataTime = []
        } else {
          this.searchParams.startDate = val[0]
          this.searchParams.endDate = val[1]
          this.dataTime[0] = val[0]
          this.dataTime[1] = val[1]
        }
      },
      /**
       * change state
       */
      _onChangeState (val) {
        this.searchParams.state = val
      },

      /**
       * change state
       */
      _onChangeRuleType (val) {
        this.searchParams.ruleType = val
      }
    },
    watch: {
    },
    created () {
      // Routing parameter merging
      if (!_.isEmpty(this.$route.query)) {
        this.searchParams = _.assign(this.searchParams, this.$route.query)
      }
    },
    mounted () {
    },
    computed: {
    },
    components: { mConditions }
  }
</script>
