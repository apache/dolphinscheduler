<template>
  <div class="home-main index-model">
    <m-dag v-if="!isLoading" :type="'definition'" :release-state="releaseState"></m-dag>
    <m-spin :is-spin="isLoading" ></m-spin>
  </div>
</template>
<script>
  import mDag from './_source/dag.vue'
  import mSpin from '@/module/components/spin/spin'
  import Affirm from './_source/jumpAffirm'
  import disabledState from '@/module/mixin/disabledState'
  import { mapActions, mapMutations } from 'vuex'

  export default {
    name: 'definition-details',
    data () {
      return {
        // loading
        isLoading: true,
        // state
        releaseState: ''
      }
    },
    mixins: [disabledState],
    props: {},
    methods: {
      ...mapMutations('dag', ['resetParams', 'setIsDetails']),
      ...mapActions('dag', ['getProcessList','getProjectList', 'getResourcesList', 'getProcessDetails']),
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
          // Node details
          this.getProcessDetails(this.$route.params.id),
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
          let item = data[0]
          this.setIsDetails(item.releaseState === 'ONLINE')
          this.releaseState = item.releaseState
          this.isLoading = false
          // Whether to pop up the box?
          Affirm.init(this.$root)
        }).catch(() => {
          this.isLoading = false
        })
      },
      /**
       * Redraw (refresh operation)
       */
      _reset () {
        this.getProcessDetails(this.$route.params.id).then(res => {
          let item = res
          this.setIsDetails(item.releaseState === 'ONLINE')
          this.releaseState = item.releaseState
        })
      }
    },
    watch: {
      // Listening for routing changes
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
