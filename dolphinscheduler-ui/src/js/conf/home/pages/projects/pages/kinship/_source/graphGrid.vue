<template>
  <div ref="graph-grid" class="graph-grid"></div>
</template>
<script>
  import echarts from 'echarts'
  import { mapActions, mapState, mapMutations } from 'vuex'
  import graphGridOption from './graphGridOption'

  export default {
    name: 'graphGrid',
    data () {
      return {}
    },
    props: {
      id: String,
      locations: Array,
      connects: Array,
      isShowLabel: Boolean
    },
    methods: {
      init () {
      },
    },
    created () {
    },
    mounted () {
      const graphGrid = echarts.init(this.$refs['graph-grid'])
      graphGrid.setOption(graphGridOption(this.locations, this.connects, this.sourceWorkFlowId, this.isShowLabel), true)
      graphGrid.on('click', (params) => {
      // Jump to the definition page
        this.$router.push({ path: `/projects/definition/list/${params.data.id}`})
      });
    },
    components: {},
    computed: {
      ...mapState('kinship', ['locations', 'connects', 'sourceWorkFlowId'])
    },
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .graph-grid {
    width: 100%;
    height: calc(100vh - 100px);
    background: url("./img/dag_bg.png");
  }
</style>
