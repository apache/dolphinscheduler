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
  <div class="resource-list-model">
    <x-select multiple
              v-model="value"
              filterable
              :disabled="isDetails"
              :placeholder="$t('Please select resources')"
              style="width: 100%;">
      <x-option
              v-for="city in resList"
              :key="city.code"
              :value="city.code"
              :label="city.code">
      </x-option>
    </x-select>
  </div>
</template>
<script>
  import _ from 'lodash'
  import disabledState from '@/module/mixin/disabledState'

  export default {
    name: 'resourceList',
    data () {
      return {
        // Resource(List)
        resList: [],
        // Resource
        value: []
      }
    },
    mixins: [disabledState],
    props: {
      resourceList: Array
    },
    methods: {
      /**
       * Verify data source
       */
      _verifResources () {
        this.$emit('on-resourcesData', _.map(this.value, v => {
          return {
            res: v
          }
        }))
        return true
      }
    },
    watch: {
      // Listening data source
      resourceList (a) {
        this.value = _.map(_.cloneDeep(a), v => v.res)
      },
      value (val) {
        this.$emit('on-cache-resourcesData', _.map(val, v => {
          return {
            res: v
          }
        }))
      }
    },
    created () {
      this.resList = _.map(_.cloneDeep(this.store.state.dag.resourcesListS), v => {
        return {
          code: v.alias
        }
      })

      if (this.resourceList.length) {
        this.value = _.map(_.cloneDeep(this.resourceList), v => v.res)
      }
    },
    mounted () {

    },
    components: { }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .resource-list-model {
    .select-listpp {
      margin-bottom: 6px;
      .lt-add {
        padding-left: 4px;
        a {
          .iconfont {
            font-size: 18px;
            vertical-align: middle;
            margin-bottom: -2px;
            display: inline-block;
          }
        }
      }
    }
    >.add {
      a {
        .iconfont {
          font-size: 18px;
          vertical-align: middle;
          display: inline-block;
          margin-top: 1px;
        }
      }
    }
  }
</style>
