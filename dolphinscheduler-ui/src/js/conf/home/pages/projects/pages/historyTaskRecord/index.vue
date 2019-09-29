<template>
  <m-list :config="config"></m-list>
</template>
<script>
  import mList from '@/conf/home/pages/projects/pages/_source/taskRecordList'
  export default {
    name: 'history-task-record',
    data () {
      return {
        config: {
          title: `${this.$t('History task record')}`,
          apiFn: 'getHistoryTaskRecordList'
        }
      }
    },
    components: { mList }
  }
</script>
