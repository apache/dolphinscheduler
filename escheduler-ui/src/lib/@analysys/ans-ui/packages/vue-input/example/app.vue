<template>
  <div>
    <section class="demo-section">
      <h4>基本用法</h4>
      <div>
        <x-input :value="value" @on-click="changeValue" style="width: 300px;"></x-input>
      </div>
    </section>
    <section class="demo-section">
      <h4>双向绑定</h4>
      <div>
        <x-input v-model="model" style="width: 200px;"></x-input>{{model}}
      </div>
    </section>
    <section class="demo-section">
      <h4>可清空</h4>
      <div>
        <x-input clearable value="可清空测试" style="width: 200px;"></x-input>
      </div>
    </section>
    <section class="demo-section">
      <h4>原生属性</h4>
      <div>
        <p><x-input readonly name="readonly" value="readonly" style="width: 200px;"></x-input></p>
        <p><x-input disabled name="disabled" value="disabled" style="width: 200px;"></x-input></p>
        <p><x-input autofocus value="autofocus" style="width: 200px;"></x-input></p>
        <p><x-input autocomplete="on" value="autocomplete" style="width: 200px;"></x-input></p>
        <p><x-input :maxlength="10" value="maxlength" style="width: 200px;"></x-input></p>
        <p><x-input :tabindex="10" value="tabindex" style="width: 200px;"></x-input></p>
      </div>
    </section>
    <section class="demo-section">
      <h4>不同尺寸</h4>
      <div>
        <x-input size="large" value="大尺寸" style="width: 200px;"></x-input>
        <x-input size="default" value="默认尺寸" style="width: 200px;"></x-input>
        <x-input size="small" value="小尺寸" style="width: 200px;"></x-input>
      </div>
    </section>
    <section class="demo-section">
      <h4>无边框</h4>
      <div>
        <x-input no-border style="width: 200px;"></x-input>
      </div>
    </section>
    <section class="demo-section">
      <h4>密码框</h4>
      <div>
        <x-input type="password" placeholder="请输入密码" style="width: 200px;"></x-input>
      </div>
    </section>
    <section class="demo-section">
      <h4>文本域</h4>
      <div>
        <x-input type="textarea" style="width: 200px;"></x-input>
      </div>
      <div>
        <x-input type="textarea" resize="none" value="不可拉伸" style="width: 200px;"></x-input>
      </div>
      <div>
        <x-input type="textarea" resize="none" :autosize="{minRows:2,maxRows:4}" value="自适应" style="width: 200px;"></x-input>
      </div>
    </section>
    <section class="demo-section">
      <h4>图标</h4>
      <div>
        <div>
          <x-input :prefix-icon="searchClass" style="width: 200px;"></x-input>
        </div>
        <div>
          <x-input :suffix-icon="searchClass" style="width: 200px;"></x-input>
        </div>
      </div>
    </section>
    <section class="demo-section">
      <h4>插槽</h4>
      <div>
        <x-input size="large" value="前置图标插槽" style="width: 200px;">
          <i slot="prefix" :class="searchClass"></i>
        </x-input>
      </div>
      <div>
        <x-input size="large" value="后置图标插槽" style="width: 200px;">
          <i slot="suffix" :class="searchClass"></i>
        </x-input>
      </div>
      <div>
        <x-input size="large" value="前置内容插槽" style="width: 200px;">
          <span slot="prepend">Http://</span>
        </x-input>
      </div>
      <div>
        <x-input size="large" value="后置内容插槽" style="width: 200px;">
          <span slot="append">忘记密码？</span>
        </x-input>
      </div>
    </section>
  </div>
</template>

<script>
import { xInput } from '../src'

export default {
  name: 'app',
  components: { xInput },
  data () {
    return {
      model: '双向绑定数据',
      value: '点击测试响应绑定值',
      searchClass: `ans-icon-search`
    }
  },
  methods: {
    changeValue () {
      this.value = '输入框绑定的值改变了'
    }
  }
}
</script>

<style lang="scss"></style>
