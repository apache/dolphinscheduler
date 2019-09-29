<template>
  <div>
    <section class="demo-section">
      <h4>单独使用</h4>
      <div>
        <p><x-radio :name="'eguan'" v-model="disabledSingle">{{ disabledSingle }}</x-radio></p>
        <p><x-radio v-model="disabledSingle2" :true-value="'Radio1-true'" :false-value="'Radio1-false'">{{ disabledSingle2 }}</x-radio></p>
      </div>
    </section>
    <section class="demo-section">
      <h4>大小</h4>
      <div>
        <x-radio :size="'large'">large</x-radio>
        <x-radio :size="'default'" >default</x-radio>
        <x-radio :size="'small'" >small</x-radio>
      </div>
    </section>
    <section class="demo-section">
      <h4>组合</h4>
      <div>
        <x-radio-group v-model="groupSize">
          <x-radio :label="'small'">small</x-radio>
          <x-radio :label="'default'">default</x-radio>
          <x-radio :label="'large'">large</x-radio>
        </x-radio-group>
      </div>
    </section>
    <section class="demo-section">
      <h4>状态</h4>
      <div>
        <x-radio-group v-model="disabledGroup" :size="groupSize">
          <x-radio :label="'千帆'" disabled>我是千帆（禁用）</x-radio>
          <x-radio :label="'万像'">我是万像<i slot="icon" class="ans-icon-notice-empty" style="font-size:12px;"></i></x-radio>
          <x-radio :label="'方舟'">我是方舟</x-radio>
        </x-radio-group>
      </div>
    </section>
    <section class="demo-section">
      <h4>垂直排列</h4>
      <div>
        <x-radio-group v-model="disabledGroup2" vertical>
          <x-radio :label="'千帆'" disabled>我是千帆（禁用）</x-radio>
          <x-radio :label="'万像'">我是万像<i slot="icon" class="ans-icon-notice-empty" style="font-size:12px;"></i></x-radio>
          <x-radio :label="'方舟'">我是方舟</x-radio>
        </x-radio-group>
      </div>
    </section>
  </div>
</template>

<script>
import { xRadio, xRadioGroup } from '../src'

export default {
  name: 'app',
  data () {
    return {
      disabledSingle: false,
      disabledSingle2: 'Radio1-false',
      disabledGroup: '万像',
      disabledGroup2: '千帆',
      groupSize: 'small'
    }
  },
  components: { xRadio, xRadioGroup }
}
</script>

<style lang="scss">

</style>
