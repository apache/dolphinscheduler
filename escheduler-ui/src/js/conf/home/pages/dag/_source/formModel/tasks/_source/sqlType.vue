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
