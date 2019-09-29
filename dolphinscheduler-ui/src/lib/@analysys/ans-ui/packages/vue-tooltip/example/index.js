import Vue from 'vue'
import App from './app.vue'
import { xTooltip } from '../src'

Vue.use(xTooltip)

new Vue({
  el: '#app',
  render: h => h(App),
  mounted () {
    console.log('success')
  }
})
