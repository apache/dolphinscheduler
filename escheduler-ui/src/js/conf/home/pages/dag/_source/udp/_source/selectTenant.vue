<template>
  <x-select
          :disabled="isDetails"
          @on-change="_onChange"
          v-model="value"
          style="width: 180px">
    <x-option
            v-for="item in itemList"
            :key="item.id"
            :value="item.id"
            :label="item.tenantName">
    </x-option>
  </x-select>
</template>
<script>
  import disabledState from '@/module/mixin/disabledState'
  export default {
    name: 'form-tenant',
    data () {
      return {
        itemList: []
      }
    },
    mixins: [disabledState],
    props: {
      value: {
        type: Number,
        default: -1
      }
    },
    model: {
      prop: 'value',
      event: 'tenantSelectEvent'
    },
    methods: {
      _onChange (o) {
        this.value = o.value
        this.$emit('tenantSelectEvent', o.value)
      }
    },
    watch: {
    },
    created () {
      let stateTenantAllList = this.store.state.security.tenantAllList || []
      if (stateTenantAllList.length) {
        this.itemList = stateTenantAllList
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
