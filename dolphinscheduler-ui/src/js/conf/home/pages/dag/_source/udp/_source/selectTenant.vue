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
            v-for="item in itemList"
            :key="item.id"
            :value="item.id"
            :label="item.tenantCode">
    </el-option>
  </el-select>
</template>
<script>
  import disabledState from '@/module/mixin/disabledState'
  export default {
    name: 'form-tenant',
    data () {
      return {
        selectedValue: this.value,
        itemList: []
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
      event: 'tenantSelectEvent'
    },
    mounted () {
      let result = this.itemList.some(item => {
        if (item.id === this.value) {
          return true
        }
      })
      if (!result) {
        this.selectedValue = 'default'
      }
    },
    methods: {
      _onChange (id) {
        const tenant = this.itemList.find(item => item.id === id)
        if (tenant) {
          this.$emit('tenantSelectEvent', tenant.tenantCode)
        }
      }
    },
    watch: {
      value (val) {
        this.selectedValue = val
      }
    },
    created () {
      let stateTenantAllList = this.store.state.security.tenantAllList || []
      let stateTenantAllListExceptDefault = null
      /*
       * if stateTenantAllList's size is greater than 1
       * whe should filter out the 'default' tenant who's id is -1.
       */
      if (stateTenantAllList.length > 1) {
        stateTenantAllListExceptDefault = stateTenantAllList.filter(item => item.id !== -1)
      } else {
        stateTenantAllListExceptDefault = stateTenantAllList
      }
      if (stateTenantAllListExceptDefault.length) {
        this.itemList = stateTenantAllListExceptDefault
      } else {
        this.store.dispatch('security/getTenantList').then(res => {
          this.$nextTick(() => {
            this.itemList = res
          })
        })
      }
    }
  }
</script>
