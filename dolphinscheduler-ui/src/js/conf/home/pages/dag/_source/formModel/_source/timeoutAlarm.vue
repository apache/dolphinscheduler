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
  <div class="timeout-alarm-model">
    <div class="clearfix list">
      <div class="text-box">
        <span>{{$t('Timeout alarm')}}</span>
      </div>
      <div class="cont-box">
        <label class="label-box">
          <div style="padding-top: 5px;">
            <x-switch v-model="enable" @on-click="_onSwitch" :disabled="isDetails"></x-switch>
          </div>
        </label>
      </div>
    </div>
    <div class="clearfix list" v-if="enable">
      <div class="text-box">
        <span>{{$t('Timeout strategy')}}</span>
      </div>
      <div class="cont-box">
        <label class="label-box">
          <div style="padding-top: 6px;">
            <x-checkbox-group v-model="strategy">
              <x-checkbox label="WARN" :disabled="isDetails">{{$t('Timeout alarm')}}</x-checkbox>
              <x-checkbox label="FAILED" :disabled="isDetails">{{$t('Timeout failure')}}</x-checkbox>
            </x-checkbox-group>
          </div>
        </label>
      </div>
    </div>
    <div class="clearfix list" v-if="enable">
      <div class="text-box">
        <span>{{$t('Timeout period')}}</span>
      </div>
      <div class="cont-box">
        <label class="label-box">
          <x-input v-model="interval" style="width: 200px;" :disabled="isDetails" maxlength="9">
            <span slot="append">{{$t('Minute')}}</span>
          </x-input>
        </label>
      </div>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import disabledState from '@/module/mixin/disabledState'

  export default {
    name: 'form-timeout-alarm',
    data () {
      return {
        // Timeout display hiding
        enable: false,
        // Timeout strategy
        strategy: [],
        // Timeout period
        interval: null
      }
    },
    mixins: [disabledState],
    props: {
      backfillItem: Object
    },
    methods: {
      _onSwitch (is) {
        // Timeout strategy
        this.strategy = is ? ['WARN'] : []
        // Timeout period
        this.interval = is ? 30 : null
      },
      _verification () {
        // Verification timeout policy
        if (this.enable && !this.strategy.length) {
          this.$message.warning(`${this.$t('Timeout strategy must be selected')}`)
          return false
        }
        // Verify timeout duration Non 0 positive integer
        if (this.enable && !parseInt(this.interval) && !_.isInteger(this.interval)) {
          this.$message.warning(`${this.$t('Timeout must be a positive integer')}`)
          return false
        }
        this.$emit('on-timeout', {
          strategy: (() => {
            // Handling checkout sequence
            let strategy = this.strategy
            if (strategy.length === 2 && strategy[0] === 'FAILED') {
              return [strategy[1], strategy[0]].join(',')
            } else {
              return strategy.join(',')
            }
          })(),
          interval: parseInt(this.interval),
          enable: this.enable
        })
        return true
      }
    },
    watch: {
    },
    created () {
      let o = this.backfillItem
      // Non-null objects represent backfill
      if (!_.isEmpty(o) && o.timeout) {
        this.enable = o.timeout.enable || false
        this.strategy = _.split(o.timeout.strategy, ',') || ['WARN']
        this.interval = o.timeout.interval || null
      }
    },
    mounted () {
    },
    components: {}
  }
</script>