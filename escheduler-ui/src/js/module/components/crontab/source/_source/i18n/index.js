
import { findLocale } from './config'
import { template } from './util'

export default {
  methods: {
    $t (str, data) {
      return template(findLocale(window.localeCrontab).locale[str], data)
    }
  }
}

