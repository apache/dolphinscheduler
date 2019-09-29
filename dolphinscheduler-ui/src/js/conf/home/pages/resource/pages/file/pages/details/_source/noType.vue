<template>
  <div class="no-type-model">
    <img src="./down_error.png" alt="">
    <p>{{$t('Online viewing type is not supported')}}</p>
  </div>
</template>
<script>
  export default {
    name: 'no-type'
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .no-type-model {
    text-align: center;
    padding-top: 160px;
    >p {
      padding-top: 10px;
      color: #666;
    }
  }
</style>
