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
            <x-switch v-model="enable" @on-click="_onSwitch(0, $event)" :disabled="isDetails"></x-switch>
          </div>
        </label>
      </div>
    </div>
    <div class="clearfix list" v-if="enable">
      <div class="text-box">
        <span>{{$t('Waiting Dependent start')}}</span>
      </div>
      <div class="cont-box">
        <label class="label-box">
          <div style="padding: 5px 0;">
            <x-switch v-model="waitStartTimeout.enable" @on-click="_onSwitch(1, $event)" :disabled="isDetails"></x-switch>
          </div>
        </label>
      </div>
    </div>
    <div class="clearfix list" v-if="enable && waitStartTimeout.enable">
      <div class="cont-box">
        <label class="label-box">
          <span class="text-box">
            <span>{{$t('Timeout period')}}</span>
          </span>
          <x-input v-model="waitStartTimeout.interval" style="width: 100px;" :disabled="isDetails" maxlength="9">
            <span slot="append">{{$t('Minute')}}</span>
          </x-input>
          <span class="text-box">
            <span>{{$t('Check interval')}}</span>
          </span>
          <x-input v-model="waitStartTimeout.checkInterval" style="width: 100px;" :disabled="isDetails" maxlength="9">
            <span slot="append">{{$t('Minute')}}</span>
          </x-input>
          <span class="text-box">
            <span>{{$t('Timeout strategy')}}</span>
          </span>
          <div style="padding-top: 6px;">
            <x-checkbox-group v-model="waitStartTimeout.strategy">
              <x-checkbox label="FAILED" :disabled="true">{{$t('Timeout failure')}}</x-checkbox>
            </x-checkbox-group>
          </div>
        </label>
      </div>
    </div>
    <div class="clearfix list" v-if="enable">
      <div class="text-box">
        <span>{{$t('Waiting Dependent complete')}}</span>
      </div>
      <div class="cont-box">
        <label class="label-box">
          <div style="padding: 5px 0;">
            <x-switch v-model="waitCompleteTimeout.enable" @on-click="_onSwitch(2, $event)" :disabled="isDetails"></x-switch>
          </div>
        </label>
      </div>
    </div>
    <div class="clearfix list" v-if="enable && waitCompleteTimeout.enable">
      <div class="cont-box">
        <label class="label-box">
          <span class="text-box">
            <span>{{$t('Timeout period')}}</span>
          </span>
          <x-input v-model="waitCompleteTimeout.interval" style="width: 100px;" :disabled="isDetails" maxlength="9">
            <span slot="append">{{$t('Minute')}}</span>
          </x-input>
          <span class="text-box">
            <span>{{$t('Timeout strategy')}}</span>
          </span>
          <div style="padding-top: 6px;">
            <x-checkbox-group v-model="waitCompleteTimeout.strategy">
              <x-checkbox label="WARN" :disabled="isDetails">{{$t('Timeout alarm')}}</x-checkbox>
              <x-checkbox label="FAILED" :disabled="isDetails">{{$t('Timeout failure')}}</x-checkbox>
            </x-checkbox-group>
          </div>
        </label>
      </div>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import disabledState from '@/module/mixin/disabledState'

  export default {
    name: 'form-dependent-timeout',
    data () {
      return {
        // Timeout display hiding
        enable: false,
        waitStartTimeout: {
          enable: false,
          // Timeout strategy
          strategy: ['FAILED'],
          // Timeout period
          interval: null,
          checkInterval: null
        },
        waitCompleteTimeout: {
          enable: false,
          // Timeout strategy
          strategy: [],
          // Timeout period
          interval: null
        }
      }
    },
    mixins: [disabledState],
    props: {
      backfillItem: Object
    },
    methods: {
      _onSwitch (p, is) {
        // reset timeout setting when switch timeout on/off.
        // p = 0 for timeout switch; p = 1 for wait start timeout switch; p = 2 for wait complete timeout switch.
        if (p === 1 || p === 0) {
          this.waitStartTimeout.interval = is ? 30 : null
          this.waitStartTimeout.checkInterval = is ? 1 : null
        }
        if (p === 2 || p === 0) {
          this.waitCompleteTimeout.strategy = is ? ['WARN'] : []
          this.waitCompleteTimeout.interval = is ? 30 : null
        }        
      },
      _verification () {
        // Verification timeout policy
        if (this.enable 
          && (this.waitCompleteTimeout.enable && !this.waitCompleteTimeout.strategy.length)
          || (this.waitStartTimeout.enable && !this.waitStartTimeout.strategy.length)) {
          this.$message.warning(`${this.$t('Timeout strategy must be selected')}`)
          return false
        }
        // Verify timeout duration Non 0 positive integer
        const reg = /^[1-9]\d*$/
        if (this.enable 
          && (this.waitCompleteTimeout.enable && !reg.test(this.waitCompleteTimeout.interval))
          || (this.waitStartTimeout.enable && (!reg.test(this.waitStartTimeout.interval || !reg.test(this.waitStartTimeout.checkInterval))))) {
          this.$message.warning(`${this.$t('Timeout must be a positive integer')}`)
          return false
        }
        // Verify timeout duration longer than check interval
        if (this.enable && this.waitStartTimeout.enable && this.waitStartTimeout.checkInterval >= this.waitStartTimeout.interval) {
          this.$message.warning(`${this.$t('Timeout must be longer than check interval')}`)
          return false
        }
        this.$emit('on-timeout', {
          waitStartTimeout: {
            strategy: 'FAILED',
            interval: parseInt(this.waitStartTimeout.interval),
            checkInterval: parseInt(this.waitStartTimeout.checkInterval),
            enable: this.waitStartTimeout.enable
          },
          waitCompleteTimeout: {
            strategy: (() => {
            // Handling checkout sequence
            let strategy = this.waitCompleteTimeout.strategy
            if (strategy.length === 2 && strategy[0] === 'FAILED') {
              return [strategy[1], strategy[0]].join(',')
            } else {
              return strategy.join(',')
            }
          })(),
          interval: parseInt(this.waitCompleteTimeout.interval),
          enable: this.waitCompleteTimeout.enable
          }
        })
        return true
      }
    },
    watch: {
    },
    created () {
      let o = this.backfillItem
      // Non-null objects represent backfill
      if (!_.isEmpty(o)) {
        if (o.timeout) {
          this.enable = true
          this.waitCompleteTimeout.enable = o.timeout.enable || false
          this.waitCompleteTimeout.strategy = _.split(o.timeout.strategy, ',') || ['WARN']
          this.waitCompleteTimeout.interval = o.timeout.interval || null
        }
        if (o.waitStartTimeout) {
          this.enable = true
          this.waitStartTimeout.enable = o.waitStartTimeout.enable || false
          this.waitStartTimeout.strategy = ['FAILED']
          this.waitStartTimeout.interval = o.waitStartTimeout.interval || null
          this.waitStartTimeout.checkInterval = o.waitStartTimeout.checkInterval || null
        }
      }
    },
    mounted () {
    },
    components: {}
  }
</script>