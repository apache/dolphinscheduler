<template>
  <router-view></router-view>
</template>
<script>
  export default {
    name: 'projects-index'
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .toolbar-color-sp,.state-tasks-color-sp {
    >a {
      display: inline-block;
      margin-right: 10px;
      cursor:default;
      &:hover {
        span {
          color: #333;
        }
      }
      >i {
        border-radius: 10px;
        display: inline-block;
        vertical-align: middle;
        font-size: 14px;
      }
      span {
        vertical-align: middle;
        font-size: 12px;
      }
    }
  }
  .toolbar-color-sp {
    >a {
      >i {
        font-size: 15px;
      }
    }
  }
</style>
