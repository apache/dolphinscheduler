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
