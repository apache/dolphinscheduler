<template>
  <m-popup
          ref="popup"
          :ok-text="item ? $t('确认编辑') : $t('确认提交')"
          :nameText="item ? $t('编辑队列') : $t('创建队列')"
          @ok="_ok">
    <template slot="content">
      <div class="create-tenement-model">
        <m-list-box-f>
          <template slot="name"><b>*</b>名称</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="tenantName"
                    :placeholder="$t('请输入name')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><b>*</b>队列值</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="tenantName"
                    :placeholder="$t('请输入name')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>

      </div>
    </template>
  </m-popup>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import store from '@/conf/home/store'
  import mPopup from '@/module/components/popup/popup'
  import mListBoxF from '@/module/components/listBoxF/listBoxF'

  export default {
    name: 'create-tenement',
    data () {
      return {
        store,
      }
    },
    props: {
      item: Object
    },
    methods: {

    },
    watch: {
    },
    created () {

    },
    mounted () {

    },
    components: { mPopup, mListBoxF }
  }
</script>