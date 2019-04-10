<template>
  <form :class="wrapClasses">
    <slot></slot>
  </form>
</template>

<script>
import { LIB_NAME } from '../../../../src/util'

const prefixCls = `${LIB_NAME}-form`

export default {
  name: 'xForm',
  props: {
    model: {
      type: Object
    },
    rules: {
      type: Object
    },
    labelWidth: {
      type: [Number, String]
    },
    labelHeight: {
      type: [Number, String]
    }
  },
  provide () {
    return { form: this }
  },
  data () {
    return {
      fields: []
    }
  },
  computed: {
    wrapClasses () {
      return [
        `${prefixCls}`
      ]
    }
  },
  methods: {
    resetFields () {
      this.fields.forEach(field => {
        field.resetField()
      })
    },
    validate (callback) {
      return new Promise(resolve => {
        let valid = true
        let count = 0

        this.fields.forEach(field => {
          field.validate('', error => {
            if (error) {
              valid = false
            }

            if (++count === this.fields.length) {
              resolve(valid)
              if (typeof callback === 'function') {
                callback(valid)
              }
            }
          })
        })
      })
    },
    validateField (prop, cb) {
      const field = this.fields.filter(field => field.prop === prop)[0]
      if (!field) {
        throw new Error('请校验有效的prop')
      }

      field.validate('', cb)
    }
  },
  created () {
    this.$on('on-form-item-add', field => {
      if (field) {
        this.fields.push(field)
      }
    })

    this.$on('on-form-item-remove', field => {
      if (field.prop) {
        this.fields.splice(this.fields.indexOf(field), 1)
      }
    })
  }
}
</script>
