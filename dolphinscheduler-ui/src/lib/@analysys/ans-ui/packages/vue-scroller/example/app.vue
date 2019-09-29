<template>
  <div style="margin-bottom: 20px;">
    <section class="demo-section">
      <h4>基本用法</h4>
      <div style="display:flex;justify-content:center;margin-bottom:50px;">
        <x-scroller max-width="500" max-height="400" show-scrollbar="active" check-on-mounted style="border:1px solid #ddd;">
          <div v-for="i in 5" :key="i" style="width:2100px;">
            <div
              v-for="j in 5" :key="j"
              style="display:inline-block;width:300px;height:100px;background:rgb(158, 150, 219);margin:20px;"></div>
          </div>
        </x-scroller>
      </div>
    </section>
  </div>
</template>

<script>
import { xScroller } from '../src'

export default {
  name: 'app',
  data () {
    return {}
  },
  components: { xScroller }
}
</script>
