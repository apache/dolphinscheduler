<template>
  <x-select
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
      value: Number
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
      this.workerGroupsList = this.store.state.security.workerGroupsListAll || []
      if (!this.value) {
        this.$emit('workerGroupsEvent', this.workerGroupsList[0].id)
      }
    }
  }
</script>
