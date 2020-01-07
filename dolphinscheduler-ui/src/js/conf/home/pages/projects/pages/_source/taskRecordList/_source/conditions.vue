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
        <x-button type="ghost" size="small" @click="_ckQuery" icon="ans-icon-search"></x-button>
      </div>
      <div class="list">
        <x-datepicker
                :value="[searchParams.startDate,searchParams.endDate]"
                ref="datepicker"
                @on-change="_onChangeStartStop"
                type="daterange"
                format="YYYY-MM-DD HH:mm:ss"
                placement="bottom-end"
                :panelNum="2">
          <x-input slot="input" readonly slot-scope="{value}" :value="value" style="width: 310px;" size="small" :placeholder="$t('Select date range')">
            <em slot="suffix"
               @click.stop="_dateEmpty()"
               class="ans-icon-fail-solid"
               v-show="value"
               style="font-size: 13px;cursor: pointer;margin-top: 1px;">
            </em>
          </x-input>
        </x-datepicker>
      </div>
      <div class="list">
        <x-input v-model="searchParams.destTable" style="width: 120px;" size="small" :placeholder="$t('Target Table')"></x-input>
      </div>
      <div class="list">
        <x-input v-model="searchParams.sourceTable" style="width: 120px;" size="small" :placeholder="$t('Source Table')"></x-input>
      </div>
      <div class="list">
        <x-select style="width: 90px;" @on-change="_onChangeState" :value="searchParams.state">
          <x-input slot="trigger" readonly :value="selectedModel ? selectedModel.label : ''" slot-scope="{ selectedModel }" style="width: 90px;" size="small" :placeholder="$t('State')" suffix-icon="ans-icon-arrow-down"></x-input>
          <x-option
                  v-for="city in stateList"
                  :key="city.label"
                  :value="city.code"
                  :label="city.label">
          </x-option>
        </x-select>
      </div>
      <div class="list">
        <x-datepicker
                v-model="searchParams.taskDate"
                @on-change="_onChangeDate"
                format="YYYY-MM-DD"
                :panelNum="1">
          <x-input slot="input" readonly slot-scope="{value}" style="width: 130px;" :value="value" size="small" :placeholder="$t('Date')"></x-input>
        </x-datepicker>
      </div>
      <div class="list">
        <x-input v-model="searchParams.taskName" style="width: 130px;" size="small" :placeholder="$t('Task Name')"></x-input>
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
            label: `${this.$t('none')}`,
            code: ``
          },
          {
            label: `${this.$t('success')}`,
            code: `成功`
          },
          {
            label: `${this.$t('waiting')}`,
            code: `等待`
          },
          {
            label: `${this.$t('execution')}`,
            code: `执行中`
          },
          {
            label: `${this.$t('finish')}`,
            code: `完成`
          }, {
            label: `${this.$t('failed')}`,
            code: `失败`
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
        }
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
        this.searchParams.state = val.value
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
        this.searchParams.taskDate = val.replace(/-/g, '')
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