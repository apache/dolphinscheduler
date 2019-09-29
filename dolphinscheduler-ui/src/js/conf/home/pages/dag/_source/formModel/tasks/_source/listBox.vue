<template>
  <div class="clearfix list">
    <div class="text-box">
      <span><slot name="text"></slot></span>
    </div>
    <div class="cont-box">
      <div class="label-box">
        <slot name="content"></slot>
      </div>
    </div>
  </div>
</template>
<script>
  export default {
    name: 'list-box'
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .cont-box {
    .label-box {
      .ans-radio-group {
        margin-top: 7px;
      }
    }
  }

  .v-checkbox-wrapper {
    &.v-checkbox-wrapper-disabled {
      color: #999 ;
      .v-checkbox {
        .v-checkbox-inner{
          border-color:#dddee1;
          background: #f7f7f7;
          color: #bbbec4 ;
          &:after{
            border: 2px solid #ddd;
            border-top: 0;
            border-left: 0;
          }
        }
      }
    }
  }

</style>
