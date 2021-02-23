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
        <el-select style="width: 140px;" @change="_onChangeState" :value="searchParams.stateType" :placeholder="$t('State')" size="mini">
          <el-option
                  v-for="city in stateTypeList"
                  :key="city.label"
                  :value="city.code"
                  :label="city.label">
          </el-option>
        </el-select>
      </div>
      <div class="list">
        <el-input v-model="searchParams.host" @keyup.enter.native="_ckQuery" style="width: 140px;" size="mini" :placeholder="$t('host')"></el-input>
      </div>
      <div class="list">
        <el-input v-model="searchParams.executorName" @keyup.enter.native="_ckQuery" style="width: 140px;" size="mini" :placeholder="$t('Executor')"></el-input>
      </div>
      <div class="list">
        <el-input v-model="searchParams.searchVal" @keyup.enter.native="_ckQuery" style="width: 200px;" size="mini" :placeholder="$t('name')"></el-input>
      </div>
    </template>
  </m-conditions>
</template>
<script>
  import _ from 'lodash'
  import { stateType } from './common'
  import mConditions from '@/module/components/conditions/conditions'
  export default {
    name: 'process-instance-conditions',
    data () {
      return {
        // state(list)
        stateTypeList: stateType,
        searchParams: {
          // state
          stateType: '',
          // start date
          startDate: '',
          // end date
          endDate: '',
          // search value
          searchVal: '',
          // host
          host: '',
          // executor name
          executorName: ''
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
        this.dataTime[0] = val[0]
        this.dataTime[1] = val[1]
      },
      /**
       * change state
       */
      _onChangeState (val) {
        this.searchParams.stateType = val
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
