<template>
  <div class="progress-bar-model">
    <div class="progress-bar-box">
      <div class="bar" :style="{width:value + '%',height:'4px'}">&nbsp;</div>
    </div>
    <template v-if="textPlacement === 'bottom'">
      <div class="textp text-1">
        {{$t('Uploading...')}} <span>({{value}}%)</span>
      </div>
    </template>
    <template v-if="textPlacement === 'left-right'">
      <div class="textp text-2-1">{{$t('Uploading...')}}</div>
      <div class="textp text-2-2"><span>({{value}}%)</span></div>
    </template>
  </div>
</template>
<script>
  export default {
    name: 'progress-bar',
    data () {
      return {}
    },
    props: {
      value: Number,
      textPlacement: String
    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .progress-bar-model {
    position: relative;
    margin-right: 46px;
    .progress-bar-box{
      width: 100%;
      height: 4px;
      background: #f6faff;
      border-radius: 4px;
      .bar {
        background: #2d8cf0;
        border-radius: 3px;
      }
    }
    .textp {
      font-size: 12px;
      color: #fff;
      display: inline-block;
      span {
        color: #2d8cf0;
        font-weight: bold;
      }
    }
    .text-1 {
      padding-top: 6px;
    }
    .text-2-1 {
      position: absolute;
      left: -70px;
      top: -7px;
    }
    .text-2-2 {
      position: absolute;
      right: -42px;
      top: -7px;
    }
  }
</style>
