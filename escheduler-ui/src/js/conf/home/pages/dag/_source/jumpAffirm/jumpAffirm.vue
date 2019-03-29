<template>
  <div class="affirm-model">
    <m-popup :ok-text="$t('确认保存')"
            :nameText="$t('是否保存DAG图')"
            @close="_close"
            @ok="_ok">
    </m-popup>
  </div>
</template>
<script>
  import mPopup from '@/module/components/popup/popup'

  export default {
    name: 'affirm',
    methods: {
      _ok () {
        this.$emit('ok')
      },
      _close () {
        this.$emit('close')
      }
    },
    components: { mPopup }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .affirm-model {
    .popup-model  {
      .top-p {
        height: 50px;
      }
      .content-p {
        min-height: 0px;
        min-width: 250px;
      }
    }
  }
</style>
