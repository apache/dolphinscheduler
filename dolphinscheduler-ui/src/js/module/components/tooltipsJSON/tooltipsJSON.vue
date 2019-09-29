<template>
  <div class="tooltips-model">
    <x-poptip
            trigger="click"
            placement="bottom-start">
      <pre :id="'result-' + id" class="result"></pre>
      <i class="iconfont" slot="reference">
        <slot name="reference"></slot>
      </i>
    </x-poptip>
  </div>
</template>
<script>
  import { syntaxHighlight } from '@/module/util/util'

  export default {
    name: 'tooltips',
    data () {
      return {}
    },
    props: {
      JSON: String,
      id: Number
    },
    mounted () {
      $(`#result-${this.id}`).html(syntaxHighlight(this.JSON))
    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .tooltips-model {
    .result {
      border: 0;
      padding: 5px;
      background: #fff;
      font-size: 12px;
    }
    .string { color: green; }
    .number { color: darkorange; }
    .boolean { color: blue; }
    .null { color: magenta; }
    .key { color: red; }
  }
</style>
