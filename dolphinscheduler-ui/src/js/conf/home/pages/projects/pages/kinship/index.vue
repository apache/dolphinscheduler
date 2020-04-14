<template>
  <div class="home-main index-model">
    <div class="project-kinship-content">
      <div class="search-bar">
        <x-select filterable clearable
                  :placeholder="$t('Process Name')"
                  @on-change="onChange"
                  :style="inputFocusStyle"
                  v-tooltip="tooltipOption(currentItemName)"
                  size="small">
          <x-option
            v-for="work in workList"
            :key="work.id"
            :value="work.id"
            :label="work.name"
            v-tooltip="tooltipOption(work.name)"
            >
          </x-option>
        </x-select>
        <x-button type="primary"
                  icon="ans-icon-dot-circle"
                  size="small"
                  v-tooltip.small.top.start="$t('Reset')"
                  @click="reset"
                  ></x-button>
        <x-button type="ghost"
                  icon="ans-icon-eye"
                  size="small"
                  v-tooltip.small.top="$t('Dag label display control')"
                  @click="changeLabel"
                  ></x-button>
      </div>
      <graph-grid v-if="!isLoading && !!locations.length" :isShowLabel="isShowLabel"></graph-grid>
      <template v-if="!isLoading && !locations.length">
        <m-no-data style="height: 100%;"></m-no-data>
      </template>
    </div>
    <m-spin :is-spin="isLoading" :fullscreen="false"></m-spin>
  </div>
</template>
<script>
  import _ from 'lodash'
  import { mapActions, mapState } from 'vuex'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import listUrlParamHandle from '@/module/mixin/listUrlParamHandle'
  import graphGrid from './_source/graphGrid.vue'



  export default {
    name: 'projects-kinship-index',
    components: { graphGrid, mSpin, mNoData },
    data () {
      return {
        isLoading: true,
        isShowLabel: true,
        currentItemName: '',
      }
    },
    props: {},
    methods: {
      ...mapActions('kinship', ['getWorkFlowList','getWorkFlowDAG']),
      /**
       * init
       */
      init () {
        this.isLoading = true
        // Promise Get node needs data
        Promise.all([
          // get process definition
          this.getWorkFlowList(),
          this.getWorkFlowDAG(),
        ]).then((data) => {
          this.isLoading = false
        }).catch(() => {
          this.isLoading = false
        })
      },
      /**
       * reset
       */
      reset() {
        this.isLoading = true;
        this.$nextTick(() => {
          this.isLoading = false;
        })
      },
      async onChange(item) {
        const { value, label } = item || {};
        this.isLoading = true;
        this.currentItemName = label;
        try {
          await this.getWorkFlowDAG(value);
        } catch (error) {
          this.$message.error(error.msg || '')
        }
        this.isLoading = false;
      },
      tooltipOption(text) {
        return {
          text,
          maxWidth: '500px',
          placement: 'top',
          theme: 'dark',
          triggerEvent: 'mouseenter',
          large: false,
        }
      },
      changeLabel() {
        this.isLoading = true;
        this.isShowLabel = !this.isShowLabel;
        this.$nextTick(() => {
          this.isLoading = false;
        })
      }
    },
    watch: {
      // router
      '$route' (a) {
        // url no params get instance list
      }
    },
    created () {
      this.init()
    },
    computed: {
      ...mapState('kinship', ['locations', 'workList']),
      inputFocusStyle() {
        return `width:280px`
      },
    },
    mounted () {
    }
  }
</script>
<style lang="scss" rel="stylesheet/scss">
  .project-kinship-content {
    position: relative;
    width: 100%;
    height: calc(100vh - 100px);
    background: url("./_source/img/dag_bg.png");
    .search-bar {
      position: absolute;
      right: 8px;
      top: 10px;
      z-index: 2;
      .ans-input {
        transition: width 300ms ease-in-out;
      }
    }
  }
</style>
