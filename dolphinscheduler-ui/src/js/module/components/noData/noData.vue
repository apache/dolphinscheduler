<template>
  <div class="no-data-model" :style="{height:height + 'px'}">
    <div class="no-data-box">
      <div class="img">
        <img src="./images/errorTip.png" alt="">
      </div>
      <div class="text">{{msg || $t('No data')}}</div>
    </div>
  </div>
</template>
<script>
  export default {
    name: 'no-data',
    props: {
      msg: String,
      height: Number
    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .no-data-model {
    position: relative;
    width: calc(100%);
    height: calc(100vh - 200px);
    .no-data-box {
      width: 210px;
      height: 210px;
      position: absolute;
      left: 50%;
      top: 50%;
      margin-left: -105px;
      margin-top: -105px;
      text-align: center;
      .text {
        padding-top: 10px;
        color: #666;
      }
    }
  }
</style>
