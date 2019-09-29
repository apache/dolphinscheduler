<template>
  <div class="main-layout-box" :class="!isProjectsList ? '' : 'no'">
    <m-secondary-menu :type="'projects'" v-if="!isProjectsList"></m-secondary-menu>
    <router-view></router-view>
  </div>
</template>
<script>
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  export default {
    name: 'projects-index',
    data () {
      return {
        isProjectsList: this.$router.history.current.name === 'projects-list'
      }
    },
    watch: {
      '$route' ({ name }) {
        this.isProjectsList = name === 'projects-list'
      }
    },
    components: { mSecondaryMenu }
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
