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
  <el-select
          style="width: 157px;"
          :disabled="isDetails"
          size="small"
          @change="_onChange"
          v-model="selectedValue"
          filterable
          allow-create>
      <el-input
              ref="input"
              slot="trigger"
              v-if="isInput"
              :disabled="isDetails"
              slot-scope="{ selectedModel }"
              maxlength="4"
              @blur="_onBlur"
              :placeholder="$t('Please choose')"
              :value="selectedModel === null ? '0' : selectedModel.value"
              style="width: 100%;"
              @click="_ckIcon">
        <em slot="suffix" class="el-icon-error" style="font-size: 15px;cursor: pointer;" v-show="!isIconState"></em>
        <em slot="suffix" class="el-icon-arrow-down" style="font-size: 12px;" v-show="isIconState"></em>
      </el-input>
    <el-option
            v-for="city in list"
            :key="city"
            :value="city"
            :label="city">
    </el-option>
  </el-select>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import disabledState from '@/module/mixin/disabledState'

  export default {
    name: 'form-select-input',
    data () {
      return {
        selectedValue: this.value,
        isIconState: false,
        isInput: true
      }
    },
    mixins: [disabledState],
    props: {
      value: String,
      list: Array
    },
    model: {
      prop: 'value',
      event: 'valueEvent'
    },
    methods: {
      _onChange (o) {
        // positive integer judgment
        const r = /^\+?[1-9][0-9]*$/
        if (!r.test(o)) {
          this.$message.warning(`${i18n.$t('Please enter a positive integer')}`)
        } else {
          this.$emit('valueEvent', +o)
          this._setIconState(+o)
        }
      },
      _setIconState (value) {
        // Whether there is a list
        this.isIconState = _.includes(this.list, parseInt(value))
      },
      _ckIcon () {
        if (this.isDetails) {
          return
        }
        this.isInput = false
        this.$emit('valueEvent', +this.list[0])
        this.isIconState = true
        // Refresh instance
        setTimeout(() => {
          this.isInput = true
        }, 1)
      },
      _onBlur () {
        let val = $(this.$refs.input.$el).find('input')[0].value
        if (this._validation(val)) {
          this.$emit('valueEvent', val)
          this._setIconState(val)
        }
      },
      _validation (val) {
        if (val === '0') return true

        if (!(/(^[0-9]*[1-9][0-9]*$)/.test(val))) {
          this.$message.warning(`${i18n.$t('Please enter a positive integer')}`)
          // init
          this._ckIcon()
          return false
        }
        return true
      }
    },
    watch: {
      value (val) {
        this.selectedValue = val
      }
    },
    created () {
      this._setIconState(this.selectedValue)
    },
    mounted () {
    },
    components: {}
  }
</script>
