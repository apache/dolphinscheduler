<template>
  <div>
    <div style="padding: 20px;">
      <x-button @click="_lang('zh_CN')">中文</x-button>
      <x-button @click="_lang('en_US')">英文</x-button>
    </div>
    <div style="padding: 40px">
      {{value}}
    </div>
    <div v-if="is">
      <v-crontab v-model="value" :locale="lang"></v-crontab>
    </div>
  </div>
</template>
<script>
  import { vCrontab } from '../src'
  export default {
    name: 'app',
    data: function () {
      return {
        value: '* * * * * ? *',
        lang: 'zh_CN',
        is: false
      }
    },
    watch: {
    },
    mounted () {
      setTimeout(() => {
        // this.value = '4/6 4/6 4/6 4/6 4/6 ? 2019/2'
      }, 2000)

      this.is = true
    },
    methods: {
      _lang (type) {
        this.is = false
        this.lang = type
        setTimeout(() => {
          this.is = true
        }, 1)
      }
    },
    components: { vCrontab }
  }
</script>
<style lang="scss"></style>