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
  <x-select
          :disabled="isDetails"
          @on-change="_onChange"
          v-model="value"
          style="width: 180px">
    <x-option
            v-for="item in workerGroupsList"
            :key="item.id"
            :value="item.id"
            :label="item.name">
    </x-option>
  </x-select>
</template>
<script>
  import disabledState from '@/module/mixin/disabledState'
  export default {
    name: 'form-worker-group',
    data () {
      return {
        workerGroupsList: []
      }
    },
    mixins: [disabledState],
    props: {
      value: {
        type: String,
        default: 'default'
      }
    },
    model: {
      prop: 'value',
      event: 'workerGroupsEvent'
    },
    methods: {
      _onChange (o) {
        this.value = o.value
        this.$emit('workerGroupsEvent', o.value)
      }
    },
    watch: {
    },
    created () {
      let stateWorkerGroupsList = this.store.state.security.workerGroupsListAll || []
      if (stateWorkerGroupsList.length) {
        this.workerGroupsList = stateWorkerGroupsList
      } else {
        this.store.dispatch('security/getWorkerGroupsAll').then(res => {
          this.$nextTick(() => {
            this.workerGroupsList = res
          })
        })
      }
    }
  }
</script>
