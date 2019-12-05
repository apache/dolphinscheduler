<template>
  <div>
    <section class="demo-section">
      <h4>基础用法</h4>
      <div>
        <x-poptip
          ref="poptip1"
          placement="top-start"
          title="标题"
          :width="200"
          trigger="hover"
          content="这是一段内容,这是一段内容,这是一段内容,这是一段内容。">
        </x-poptip>

        <x-poptip
          ref="poptip2"
          placement="bottom-start"
          :width="200"
          trigger="click"
          :visible-arrow="false"
          content="这是一段内容,这是一段内容,这是一段内容,这是一段内容。">
        </x-poptip>

        <x-button v-poptip:poptip1>hover 激活</x-button>
        <x-button v-poptip:poptip2>click 激活</x-button>

        <x-poptip
          placement="right"
          title="标题"
          :width="200"
          confirm
          trigger="focus"
          content="这是一段内容,这是一段内容,这是一段内容,这是一段内容。">
          <x-button slot="reference">focus 激活</x-button>
          <span slot="title">我是title</span>
        </x-poptip>

        <x-poptip
          placement="right"
          title="标题"
          :width="200"
          confirm
          trigger="manual"
          :ok-text="'yes'"
          :cancel-text="'no'"
          content="这是一段内容,这是一段内容,这是一段内容,这是一段内容。"
          v-model="visible4">
          <x-button slot="reference" @click="visible4 = !visible4">manual 激活</x-button>
        </x-poptip>
      </div>
    </section>
    <section class="demo-section">
      <h4>嵌套操作</h4>
      <div>
        <x-poptip
          ref="poptip5"
          placement="bottom-start"
          :width="180"
          v-model="visible2">
          <p>这是一段内容这是一段内容确定删除吗？</p>
          <div style="text-align: right; margin: 0">
            <x-button type="text" @click="visible2 = false">取消</x-button>
            <x-button type="primary" @click="visible2 = false">确定</x-button>
          </div>
        </x-poptip>
        <x-button v-poptip:poptip5>删除</x-button>
      </div>
    </section>
  </div>
</template>

<script>
  import { xButton } from '../../vue-button/src'
  import { xPoptip } from '../src'

  export default {
    components: { xButton, xPoptip },
    data () {
      return {
        visible2: false,
        visible3: false,
        visible4: false
      }
    },
    methods: {

    }
  }
</script>

<style lang="scss"></style>
