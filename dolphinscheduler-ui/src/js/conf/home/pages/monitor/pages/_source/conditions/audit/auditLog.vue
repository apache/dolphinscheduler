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
        <el-select style="width: 140px;" @change="_onChangeResource" :value="searchParams.resourceType" :placeholder="$t('Resource Type')" size="mini">
          <el-option
            v-for="module in resourceTypeList"
            :key="module.label"
            :value="module.code"
            :label="module.label">
          </el-option>
        </el-select>
      </div>
      <div class="list">
        <el-select style="width: 140px;" @change="_onChangeOperation" :value="searchParams.operationType" :placeholder="$t('Operation')" size="mini">
          <el-option
            v-for="operation in operationTypeList"
            :key="operation.label"
            :value="operation.code"
            :label="operation.label">
          </el-option>
        </el-select>
      </div>
      <div class="list">
        <el-input v-model="searchParams.userName" @keyup.enter.native="_ckQuery" style="width: 140px;" size="mini" :placeholder="$t('User Name')"></el-input>
      </div>
    </template>
  </m-conditions>
</template>
<script>
  import _ from 'lodash'
  import { resourceType, operationType } from './common'
  import mConditions from '@/module/components/conditions/conditions'
  export default {
    name: 'monitor-log-conditions',
    data () {
      return {
        // state(list)
        resourceTypeList: resourceType,
        operationTypeList: operationType,
        searchParams: {
          // resource type
          resourceType: '',
          // operation
          operationType: '',
          // start date
          startDate: '',
          // end date
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
        this.dataTime[0] = val[0]
        this.dataTime[1] = val[1]
      },
      /**
       * change resource
       */
      _onChangeResource (val) {
        this.searchParams.resourceType = val
      },
      /**
       * change operation
       */
      _onChangeOperation (val) {
        this.searchParams.operationType = val
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
