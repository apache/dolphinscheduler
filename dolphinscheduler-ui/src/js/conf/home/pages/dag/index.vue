<template>
  <div class="home-main index-model">
    <m-dag v-if="!isLoading"></m-dag>
    <m-spin :is-spin="isLoading"></m-spin>
  </div>
</template>
<script>
  import mDag from './_source/dag.vue'
  import { mapActions, mapMutations } from 'vuex'
  import mSpin from '@/module/components/spin/spin'
  import Affirm from './_source/jumpAffirm'
  import disabledState from '@/module/mixin/disabledState'

  export default {
    name: 'create-index',
    data () {
      return {
        // loading
        isLoading: true
      }
    },
    // mixins
    mixins: [disabledState],
    props: {},
    methods: {
      ...mapMutations('dag', ['resetParams']),
      ...mapActions('dag', ['getProcessList','getProjectList', 'getResourcesList']),
      ...mapActions('security', ['getTenantList','getWorkerGroupsAll']),
      /**
       * init
       */
      init () {
        this.isLoading = true
        // Initialization parameters
        this.resetParams()
        // Promise Get node needs data
        Promise.all([
          // get process definition
          this.getProcessList(),
          // get project
          this.getProjectList(),
          // get resource
          this.getResourcesList(),
          // get worker group list
          this.getWorkerGroupsAll(),
          this.getTenantList()
        ]).then((data) => {
          this.isLoading = false
          // Whether to pop up the box?
          Affirm.init(this.$root)
        }).catch(() => {
          this.isLoading = false
        })
      }
    },
    watch: {
      '$route': {
        deep: true,
        handler () {
          this.init()
        }
      }
    },
    created () {
      this.init()
    },
    mounted () {
    },
    components: { mDag, mSpin }
  }
</script>
