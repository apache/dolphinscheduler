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
  <div class="sql-type-model">
    <x-select
            v-model="sqlTypeId"
            :disabled="isDetails"
            @on-change="_handleSqlTypeChanged"
            style="width: 90px;">
      <x-option
              v-for="city in sqlTypeList"
              :key="city.id"
              :value="city"
              :label="city.code">
      </x-option>
    </x-select>
  </div>
</template>
<script>
  import _ from 'lodash'
  import { sqlTypeList } from './commcon'
  import disabledState from '@/module/mixin/disabledState'
  export default {
    name: 'sql-type',
    data () {
      return {
        // sql(List)
        sqlTypeList: sqlTypeList,
        // sql
        sqlTypeId: {}
      }
    },
    mixins: [disabledState],
    props: {
      sqlType: Number
    },
    methods: {
      /**
       * return sqlType
       */
      _handleSqlTypeChanged (val) {
        this.$emit('on-sqlType', val.value.id)
      }
    },
    watch: {
    },
    created () {
      this.$nextTick(() => {
        if (this.sqlType !== null) {
          this.sqlTypeId = _.filter(this.sqlTypeList, v => v.id === this.sqlType)[0]
        } else {
          this.sqlTypeId = this.sqlTypeList[0]
        }
      })
    },
    mounted () {
    }
  }
</script>
