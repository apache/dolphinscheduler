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
          :disabled="isDetails"
          clearable
          @change="_onChange"
          v-model="selectedValue"
          size="small"
          style="width: 180px">
    <el-option
            v-for="item in environmentOptions"
            :key="item.code"
            :value="item.code"
            :label="item.name">
    </el-option>
  </el-select>
</template>
<script>
  import disabledState from '@/module/mixin/disabledState'
  export default {
    name: 'form-related-environment',
    data () {
      return {
        selectedValue: '',
        selectedWorkerGroup: this.workerGroup,
        environmentOptions: [],
        environmentList: []
      }
    },
    mixins: [disabledState],
    props: {
      value: {
        type: String
      },
      workerGroup: {
        type: String
      }
    },
    model: {
      prop: 'value',
      event: 'environmentCodeEvent'
    },
    methods: {
      _onChange (o) {
        this.$emit('environmentCodeEvent', o)
      },
      _getEnvironmentAll () {
        return new Promise((resolve, reject) => {
          this.store.dispatch('security/getEnvironmentAll').then(res => {
            resolve(res)
          }).catch(e => {
            reject(e)
          })
        })
      },
      _initEnvironmentOptions (workerGroup) {
        this.environmentOptions = []
        if (this.environmentList && workerGroup) {
          this.environmentList.forEach(item => {
            if (item.workerGroups && item.workerGroups.length > 0) {
              if (item.workerGroups.indexOf(workerGroup) >= 0) {
                this.environmentOptions.push({ code: item.code, name: item.name })
                if (item.code === this.value) {
                  this.selectedValue = this.value
                }
              }
            }
          })
        }

        if (this.environmentOptions.length > 0) {
          /// default to select this environment when only have one environment
          if (this.environmentOptions.length === 1 && this.selectedValue === '') {
            this.selectedValue = this.environmentOptions[0].code
            this.$emit('environmentCodeEvent', this.selectedValue)
          }
        } else {
          this.selectedValue = ''
          this.$emit('environmentCodeEvent', this.selectedValue)
        }
      }
    },
    watch: {
      value: function (val) {
      },
      workerGroup: function (val) {
        this.selectedWorkerGroup = val
        this._initEnvironmentOptions(this.selectedWorkerGroup)
      }
    },
    created () {
      let stateEnvironmentList = this.store.state.security.environmentListAll || []

      if (stateEnvironmentList.length && stateEnvironmentList.length > 0) {
        this.environmentList = stateEnvironmentList
        this._initEnvironmentOptions(this.workerGroup)
      } else {
        this._getEnvironmentAll().then(res => {
          this.environmentList = res
          this._initEnvironmentOptions(this.workerGroup)
        })
      }
    }
  }
</script>
