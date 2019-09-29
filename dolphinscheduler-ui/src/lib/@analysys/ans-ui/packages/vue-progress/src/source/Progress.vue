<template>
  <div :class="wrapperClass">
    <template v-if="lineType">
      <div class="progress-bar" :style="graphStyles">
        <div
          class="progress-percentage"
          :class="{[status]: status}"
          :style="percentageStyles">
          <div class="progress-inner">
            <slot name="inline">
              <span v-if="showInlineText" class="inner-text">{{percentage}}%</span>
            </slot>
          </div>
        </div>
      </div>
      <slot name="outside">
        <div v-if="showOutsideText" class="progress-right">
          <span v-if="!status" class="right-text">{{percentage}}%</span>
          <template v-else>
            <i v-if="status === 'success'" class="ans-icon-success-solid success"></i>
            <i v-else class="ans-icon-fail-solid exception"></i>
          </template>
        </div>
      </slot>
    </template>
    <div v-else class="progress-ring" :style="graphStyles">
      <svg xmlns="http://www.w3.org/2000/svg" version="1.1" :width="width" :height="width">
        <circle
          :cx="halfWidth"
          :cy="halfWidth"
          :r="radius"
          :stroke-width="strokeWidth"
          class="progress-background"/>
        <path
          :d="percentagePath"
          stroke-linecap="round"
          :stroke-width="strokeWidth"
          class="progress-percentage"
          :class="{[status]: status}"
          :style="percentageStyles"></path>
      </svg>
      <div class="progress-center">
        <slot name="circle">
          <div v-if="showCircleText" class="progress-inner">
            <span v-if="!status" class="right-text">{{percentage}}%</span>
            <template v-else>
              <i v-if="status === 'success'" class="ans-icon-check success"></i>
              <i v-else class="ans-icon-close exception"></i>
            </template>
          </div>
        </slot>
      </div>
    </div>
  </div>
</template>

<script>
import { LIB_NAME } from '../../../../src/util'

export default {
  name: 'xProgress',
  data () {
    return {
      wrapperClass: `${LIB_NAME}-progress`
    }
  },
  props: {
    // 百分比
    percentage: {
      type: Number,
      default: 0,
      required: true,
      validator: val => val >= 0 && val <= 100
    },
    // 进度条类型
    type: {
      type: String,
      default: 'line',
      validator (value) {
        return ['line', 'circle'].includes(value)
      }
    },
    // 进度条宽度
    strokeWidth: {
      type: Number,
      default: 8
    },
    // 进度条状态
    status: {
      type: String,
      validator (value) {
        return ['success', 'exception'].includes(value)
      }
    },
    // 进度条颜色
    color: String,
    // 环形进度条宽度
    width: {
      type: Number,
      default: 100
    },
    // 是否显示进度条内文本
    showInlineText: {
      type: Boolean,
      default: false
    },
    // 是否显示进度条外文本
    showOutsideText: {
      type: Boolean,
      default: true
    },
    // 是否显示进度条里面文本
    showCircleText: {
      type: Boolean,
      default: true
    }
  },
  computed: {
    lineType () {
      return this.type === 'line'
    },
    graphStyles () {
      const strokeWidth = this.strokeWidth + 'px'
      if (this.lineType) {
        return {
          height: strokeWidth,
          'border-radius': strokeWidth
        }
      } else {
        const width = this.width + 'px'
        return {
          width: width,
          height: width
        }
      }
    },
    percentageStyles () {
      if (this.lineType) {
        const styles = {
          width: this.percentage + '%',
          'border-radius': this.strokeWidth + 'px'
        }
        if (this.color) {
          styles.background = this.color
        }
        return styles
      } else {
        const perimeter = 2 * Math.PI * this.radius
        const styles = {
          'stroke-dasharray': perimeter + 'px',
          'stroke-dashoffset': `${perimeter * (1 - this.percentage * 0.01)}px`
        }
        if (this.color) {
          styles.stroke = this.color
        }
        return styles
      }
    },
    halfWidth () {
      return this.width ? this.width * 0.5 : 0
    },
    radius () {
      return this.halfWidth - this.strokeWidth * 0.5
    },
    percentagePath () {
      const c = this.halfWidth
      const r = this.radius
      return `M ${c},${c} m ${0},${-r} a ${r} ${r} 0 1 1 0 ${2 * r} a ${r} ${r} 0 1 1 0 ${-2 * r}`
    }
  }
}
</script>
