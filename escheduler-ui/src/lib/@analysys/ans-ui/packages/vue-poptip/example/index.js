import Vue from 'vue'
import App from './app.vue'
import { xPoptip } from '../src'

Vue.use(xPoptip)

new Vue({
  el: '#app',
  render: h => h(App),
  mounted () {
    console.log('success')
  }
})
