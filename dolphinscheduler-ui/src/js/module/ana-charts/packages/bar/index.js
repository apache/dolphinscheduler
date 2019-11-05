/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import Base from '../base'
import { checkKeyInModel, init } from '../../common'

const TYPE = 'bar'

/**
 * 柱状图
 */
export default class Bar extends Base {
  /**
   * 单独导出时调用的初始化方法
   * @param {*} el 选择器或者 DOM 对象
   * @param {*} data 数据源
   * @param {*} options 可选项
   */
  static init (el, data, options) {
    return init(Bar, el, data, options)
  }

  /**
   * 将用户配置转换为符合 ECharts API 格式的配置格式
   */
  transform () {
    const { data = [] } = this.settings

    if (data.length === 0) {
      throw new Error('数据源为空！')
    }

    if (Object.keys(data[0]).length > 2) {
      return this.setMultipleBars()
    } else {
      this.simple = true
      return this.setSingleBar()
    }
  }

  /**
   * 单条柱
   */
  setSingleBar () {
    const {
      // 数据
      data = [],
      // 属性字典
      keyMap = {
        xAxisKey: 'key',
        dataKey: 'value'
      },
      // 图表标题
      title = '单条柱状图'
    } = this.settings

    // x 轴对应属性名，数据值对应的属性名
    const { xAxisKey, dataKey } = keyMap
    checkKeyInModel(data[0], xAxisKey, dataKey)

    const series = [{
      type: TYPE,
      data: []
    }]
    const xAxis = {
      type: 'category',
      data: []
    }

    for (let i = 0; i < data.length; i++) {
      xAxis.data.push(data[i][xAxisKey])
      series[0].data.push(data[i][dataKey])
    }

    return { title, xAxis, series }
  }

  /**
   * 多条柱
   */
  setMultipleBars () {
    const {
      // 数据
      data = [],
      // 属性字典
      keyMap = {
        xAxisKey: 'key',
        legendKey: 'typeName',
        dataKey: 'value'
      },
      // 图表标题
      title = '多条柱状图',
      // 折柱混合时，指定的折线数据索引
      lineTypes
    } = this.settings

    // x 轴对应属性名，图例对应的属性名，数据值对应的属性名
    const { xAxisKey, legendKey, dataKey } = keyMap
    // 是否使用时间轴数据
    const timeline = Object.keys(data[0]).length === 4
    const timelineKey = keyMap.timelineKey || 'timeline'
    if (timeline) {
      checkKeyInModel(data[0], xAxisKey, legendKey, dataKey, timelineKey)
    } else {
      checkKeyInModel(data[0], xAxisKey, legendKey, dataKey)
    }

    // 规范折柱混合索引
    let lineTypeList = []
    if (lineTypes) {
      if (!Array.isArray(lineTypes)) {
        lineTypeList = [lineTypes]
      } else {
        lineTypeList = lineTypes
      }
    }

    // 时间轴默认配置
    const timelineOptions = {
      timeline: {
        axisType: 'category',
        autoPlay: true,
        playInterval: 1000,
        data: []
      },
      options: []
    }

    // 初始值
    const legendData = []
    const series = []
    const xAxis = {
      type: 'category',
      data: []
    }
    for (let i = 0; i < data.length; i++) {
      const legendItem = data[i][legendKey]
      const xAxisItem = data[i][xAxisKey]
      const dataItem = data[i][dataKey]

      // 图例
      if (!legendData.includes(legendItem)) {
        legendData.push(legendItem)
      }

      // x 轴
      if (!xAxis.data.includes(xAxisItem)) {
        xAxis.data.push(xAxisItem)
      }

      // 时间轴
      if (timeline) {
        const timelineItem = data[i][timelineKey]
        // 设置时间轴 label
        if (!timelineOptions.timeline.data.includes(timelineItem)) {
          timelineOptions.timeline.data.push(timelineItem)
        }
        // 通用的系列配置
        if (!series.some(s => s.name === legendItem)) {
          let seriesType = TYPE
          if (lineTypeList.length !== 0 && lineTypeList.includes(legendItem)) {
            seriesType = 'line'
          }
          series.push({
            name: legendItem,
            type: seriesType
          })
        }
        // 系列数据
        let targetOptions = timelineOptions.options.find(o => o._helpName === timelineItem)
        if (!targetOptions) {
          // 初始化 option
          targetOptions = {
            _helpName: timelineItem,
            title: { text: title.replace('$timeline', timelineItem) },
            series: []
          }
          timelineOptions.options.push(targetOptions)
        }
        let targetSeries = targetOptions.series.find(d => d._helpName === legendItem)
        if (!targetSeries) {
          // 初始化系列数据
          targetSeries = {
            _helpName: legendItem,
            data: []
          }
          targetOptions.series.push(targetSeries)
        }
        targetSeries.data.push(dataItem)
      } else {
        // 非时间轴数据处理
        let targetSeries = series.find(s => s.name === legendItem)
        if (!targetSeries) {
          let seriesType = TYPE
          if (lineTypeList.length !== 0 && lineTypeList.includes(legendItem)) {
            seriesType = 'line'
          }
          targetSeries = {
            name: legendItem,
            type: seriesType,
            data: []
          }
          series.push(targetSeries)
        }
        targetSeries.data.push(dataItem)
      }
    }

    if (timeline) {
      return { title, xAxis, series, legendData, timelineOptions }
    }
    return { title, xAxis, series, legendData }
  }

  /**
   * 绘制图表
   */
  apply () {
    const { title, xAxis, series, legendData, timelineOptions } = this.options
    const {
      // 是否为横向图
      reverseAxis = false,
      // 自定义 y 轴
      yAxis,
      // 是否为堆叠图
      stack = false,
      // 注入配置到 series
      insertSeries
    } = this.settings
    const valueAxis = { type: 'value' }
    let yAxisModel = reverseAxis ? xAxis : valueAxis
    let xAxisModel = reverseAxis ? valueAxis : xAxis
    // 使用自定义 y 轴覆盖
    if (yAxis) {
      yAxisModel = yAxis
    }
    // 设置堆叠图
    if (stack) {
      series.forEach(set => {
        set.stack = '总量'
        set.label = {
          normal: {
            show: true,
            position: reverseAxis ? 'insideRight' : 'insideTop'
          }
        }
      })
    }

    let _series = series
    if (insertSeries && insertSeries.length && series.length) {
      _series = this.injectDataIntoSeries(insertSeries, _series)
    }

    // 时间轴
    if (timelineOptions) {
      let opts = {
        baseOption: {
          timeline: timelineOptions.timeline,
          tooltip: {
            trigger: 'axis'
          },
          grid: {
            top: 80,
            bottom: 100,
            containLabel: true
          },
          legend: {
            x: 'right',
            data: legendData
          },
          xAxis: xAxisModel,
          yAxis: yAxisModel,
          series: _series
        },
        options: timelineOptions.options
      }

      this.echart.setOption(opts, true)
      this.echart.clear()
      this.echart.setOption(opts, true)
    } else {
      // 简单图表标题为空时，图表垂直居中
      const top = !title && this.simple ? '3%' : 60

      let opts = {
        title: {
          text: title
        },
        tooltip: {
          trigger: 'axis'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top,
          containLabel: true
        },
        legend: {
          data: legendData
        },
        xAxis: xAxisModel,
        yAxis: yAxisModel,
        series: _series
      }

      this.echart.setOption(opts, true)
      this.echart.clear()
      this.echart.setOption(opts, true)
    }
  }
}
