<template>
  <div class="clearfix list-box-f">
    <div class="text">
      <slot name="name"></slot>
    </div>
    <div class="cont">
      <slot name="content"></slot>
    </div>
  </div>
</template>
<script>
  export default {
    name: 'list-box-f'
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .list-box-f {
    margin-bottom: 20px;
    &:last-child {
      margin-bottom: 0;
    }
    .text {
      width: 144px;
      float: left;
      text-align: right;
      line-height: 32px;
      padding-right: 8px;
      >b {
        color: #ff0000;
        padding-right: 4px;
      }
    }
    .cont {
      width: calc(100% - 162px);
      float: left;
      .textarea-content {
        .v-input {
          textarea {
            min-height: 400px !important;
            font-size: 12px;
          }
        }
      }
      >.v-radio-group,>.ans-radio-group {
        padding-top: 7px;
      }
      >.v-input {
        textarea {
          height: 70px;
        }
      }
      .v-radio-group-item {
        font-weight: normal;
        margin-top: 1px;
      }
    }
  }
</style>
