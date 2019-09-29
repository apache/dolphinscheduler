<template>
  <div>
    <section class="demo-section">
      <h4>基本用法</h4>
      <div>
        <x-form ref="formVal" :model="formVal" :rules="formRules" label-width="80" label-height="32">
          <x-form-item prop="username" label="帐号">
            <x-input v-model="formVal.username" placeholder="请输入帐号" style="width: 200px;"></x-input>
          </x-form-item>
          <x-form-item prop="passcode" label="密码">
            <x-input v-model="formVal.passcode" type="password" placeholder="请输入密码" style="width: 200px;"></x-input>
          </x-form-item>
          <x-form-item prop="gender" label="性别">
            <x-radio-group v-model="formVal.gender">
              <x-radio label="1">男</x-radio>
              <x-radio label="2">女</x-radio>
            </x-radio-group>
          </x-form-item>
          <x-form-item prop="fruit" label="水果">
            <x-checkbox-group v-model="formVal.fruit">
              <x-checkbox label="香蕉">香蕉</x-checkbox>
              <x-checkbox label="苹果">苹果</x-checkbox>
              <x-checkbox label="橘子">橘子</x-checkbox>
            </x-checkbox-group>
          </x-form-item>
          <x-form-item prop="city" label="城市">
            <x-select v-model="formVal.city" clearable style="width: 200px;">
              <x-option v-for="item in cities" :key="item.value" :value="item.value" :label="item.label"></x-option>
            </x-select>
          </x-form-item>
          <x-form-item prop="startTime" label="开始时间">
            <x-datepicker v-model="formVal.startTime" placeholder="选择单日" style="width: 200px;"></x-datepicker>
          </x-form-item>
          <x-form-item prop="desc" label="备注">
            <x-input type="textarea" v-model="formVal.desc" placeholder="请输入备注" style="width: 200px;"></x-input>
          </x-form-item>
          <x-form-item label=" ">
            <x-button @click="submit()">登 录</x-button>
          </x-form-item>
        </x-form>
      </div>
    </section>
  </div>
</template>

<script>
import { xForm, xFormItem } from '../src'
import { xInput } from '../../vue-input/src'
import { xButton } from '../../vue-button/src'
import { xRadio, xRadioGroup } from '../../vue-radio/src'
import { xCheckbox, xCheckboxGroup } from '../../vue-checkbox/src'
import { xSelect, xOption, xOptionGroup } from '../../vue-select/src'
import { xDatepicker } from '../../vue-datepicker/src'

export default {
  name: 'app',
  components: {
    xForm,
    xFormItem,
    xInput,
    xButton,
    xRadio,
    xRadioGroup,
    xCheckbox,
    xCheckboxGroup,
    xSelect,
    xOption,
    xOptionGroup,
    xDatepicker
  },
  data () {
    return {
      formVal: {
        username: '',
        passcode: '',
        gender: '',
        fruit: [],
        city: '',
        startTime: '',
        desc: ''
      },
      formRules: {
        username: [
          { required: true, message: '请输入帐号', trigger: 'blur' }
        ],
        passcode: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { required: true, min: 6, max: 12, message: '密码长度在6-12之间', trigger: 'blur' }
        ],
        gender: [
          { required: true, message: '请选择性别', trigger: 'change' }
        ],
        fruit: [
          { required: true, type: 'array', message: '请选择水果', trigger: 'change' }
        ],
        city: [
          { required: true, message: '请选择城市', trigger: 'change' }
        ],
        startTime: [
          { required: true, message: '请选择日期', trigger: 'change' }
        ],
        desc: [
          { required: true, message: '请输入备注', trigger: 'blur' }
        ]
      },
      cities: [{
        value: 'Beijing',
        label: '北京'
      }, {
        value: 'Shanghai',
        label: '上海'
      }, {
        value: 'Nanjing',
        label: '南京'
      }, {
        value: 'Chengdu',
        label: '成都'
      }, {
        value: 'Shenzhen',
        label: '深圳'
      }, {
        value: 'Guangzhou',
        label: '广州'
      }]
    }
  },
  methods: {
    submit () {
      this.$refs['formVal'].validate(valid => {
        if (valid) {
          console.log('success')
        } else {
          console.log('error')
        }
      })
    }
  }
}
</script>

<style lang="scss"></style>
