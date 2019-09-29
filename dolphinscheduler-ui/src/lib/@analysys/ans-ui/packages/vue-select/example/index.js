import Vue from 'vue'
import App from './dynamic.vue'

new Vue({
  el: '#app',
  render: h => h(App),
  mounted () {
    console.log('success')
  }
})
