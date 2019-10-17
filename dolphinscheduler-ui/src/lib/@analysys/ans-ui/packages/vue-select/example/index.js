import Vue from 'vue'
import App from './navigation.vue'

new Vue({
  el: '#app',
  render: h => h(App),
  mounted () {
    console.log('success')
  }
})
