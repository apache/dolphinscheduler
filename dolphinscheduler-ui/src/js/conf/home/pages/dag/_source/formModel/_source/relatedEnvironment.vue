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
        selectedValue: this.value,
        environmentOptions: []
      }
    },
    mixins: [disabledState],
    props: {
      value: {
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
      }
    },
    watch: {
      value (val) {
        this.selectedValue = val
      }
    },
    created () {
      let stateEnvironmentList = this.store.state.security.environmentListAll || []
      let environmentList = []

      console.log(this.value)
      console.log(stateEnvironmentList)

      if (stateEnvironmentList.length && stateEnvironmentList.length > 0) {
        console.log('1')
        environmentList = stateEnvironmentList
      } else {
        console.log('2')
        this.store.commit('security/getEnvironmentAll').then(res => {
          console.log('dispatch....')
          console.log(res)
          environmentList = res
        })
      }

      environmentList.forEach(item => {
        this.environmentOptions.push({ code: item.code, name: item.name })
      })

      console.log('environment list ....')
      console.log(environmentList)
    }
  }
</script>
