
# ana-charts

echarts 扩展

## 安装

```
npm i @analysys/ana-charts
```

## 特性

- 统一的数据结构
- 支持 ECharts 原生操作

## 快速开始

### 全量导入

```html
<template>
  <div>
    <div id="chart" style="height:500px"></div>
  </div>
</template>

<script>
import Chart from '@analysys/ana-charts'

export default {
  mounted () {
    Chart.line('#chart', [
      { key: 'Monday', value: 1 },
      { key: 'Tuesday', value: 2 },
      { key: 'Wednesday', value: 3 }
    ])
  }
}
</script>
```

### 按需导入

```html
<template>
  <div>
    <div id="chart" style="height:500px"></div>
  </div>
</template>

<script>
import { Line } from '@analysys/ana-charts'

export default {
  mounted () {
    Line.init('#chart', [
      { key: 'Monday', value: 1 },
      { key: 'Tuesday', value: 2 },
      { key: 'Wednesday', value: 3 }
    ])
  }
}
</script>
```

## APIs

### 一般用法 

``` js
const myChart = Chart.line(el, data, options)
// const myChart = Chart.bar(el, data, options)
// const myChart = Chart.pie(el, data, options)
// const myChart = Chart.radar(el, data, options)
// const myChart = Chart.funnel(el, data, options)
// const myChart = Chart.scatter(el, data, options)

// 刷新数据
myChart.setData(data)
```

### 注入属性

``` js
// 以 line 折线图为例，bar、funnel、pie、scatter 均可使用
Chart.line(el, data, {
  insertSeries: [
    {
      // index 可选 `all`，`start`，`end`，也可指定需要被注入的索引数组，如 [0, 2, 4]
      index: 'all', 
      // 以下属性会被注入到指定的序列中
      areaStyle: {}
    }
  ]
})
```

### ECharts 对象

> 初始化图表后返回的对象上保存了 ECharts 对象的引用，可以通过该属性来设置图表配置和监听事件

``` js
const myChart = Chart.line(el, data, options)
// 设置可配置项
myChart.echart.setOption({
  // 与 ECharts 参考文档用法一致
})
```

### 折线图

#### 基本用法

``` js
Chart.line('#chart', [
  { key: 'Monday', value: 1 },
  { key: 'Tuesday', value: 2 },
  { key: 'Wednesday', value: 3 },
  ...
])
```

#### 多条折线图

``` js
Chart.line('#chart', [
  { typeName: 'apple', key: 'Monday', value: 1 },
  { typeName: 'apple', key: 'Tuesday', value: 2 },
  { typeName: 'apple', key: 'Wednesday', value: 3 },
  { typeName: 'pear', key: 'Monday', value: 11 },
  { typeName: 'pear', key: 'Tuesday', value: 21 },
  { typeName: 'pear', key: 'Wednesday', value: 31 },
  { typeName: 'banana', key: 'Monday', value: 31 },
  { typeName: 'banana', key: 'Tuesday', value: 32 },
  { typeName: 'banana', key: 'Wednesday', value: 33 },
  ...
])
```

#### options 可配置参数

| 属性 | 说明 | required | 类型 | 默认值 |
| :----| :------| :--------| :---:| :------|
| title | 图表标题 | - | String | '单条折线图' 或 '多条折线图' |
| keyMap | 数据列表的属性字典 | - | Object | 详见后续属性 |
| keyMap.xAxisKey | x 轴对应的属性名称 | - | String | 'key' |
| keyMap.dataKey | 数据值对应的属性名称 | - | String | 'value' |
| keyMap.legendKey | 图例对应的属性名称 | - | String | 'typeName' |
| reverseAxis | 是否为横向图 | - | Boolean | false |

### 柱状图

#### 基本用法

``` js
Chart.bar('#chart', [
  { key: 'Monday', value: 1 },
  { key: 'Tuesday', value: 2 },
  { key: 'Wednesday', value: 3 },
  ...
])
```

#### 多条柱状图

``` js
Chart.bar('#chart', [
  { typeName: 'apple', key: 'Monday', value: 1 },
  { typeName: 'apple', key: 'Tuesday', value: 2 },
  { typeName: 'apple', key: 'Wednesday', value: 3 },
  { typeName: 'pear', key: 'Monday', value: 11 },
  { typeName: 'pear', key: 'Tuesday', value: 21 },
  { typeName: 'pear', key: 'Wednesday', value: 31 },
  { typeName: 'banana', key: 'Monday', value: 31 },
  { typeName: 'banana', key: 'Tuesday', value: 32 },
  { typeName: 'banana', key: 'Wednesday', value: 33 },
  ...
])
```

#### 折柱混合图

``` js
Chart.bar('#chart', [
  { typeName: 'apple', key: 'Monday', value: 1 },
  { typeName: 'apple', key: 'Tuesday', value: 2 },
  { typeName: 'apple', key: 'Wednesday', value: 3 },
  { typeName: 'pear', key: 'Monday', value: 11 },
  { typeName: 'pear', key: 'Tuesday', value: 21 },
  { typeName: 'pear', key: 'Wednesday', value: 31 },
  { typeName: 'banana', key: 'Monday', value: 31 },
  { typeName: 'banana', key: 'Tuesday', value: 32 },
  { typeName: 'banana', key: 'Wednesday', value: 33 },
  ...
], {
  lineTypes: ['banana']
})
```

#### 时间轴柱状图

``` js
Chart.bar('#chart', [
  { timeline: 2015, typeName: 'apple', key: 'Monday', value: 1 },
  { timeline: 2015, typeName: 'apple', key: 'Tuesday', value: 2 },
  { timeline: 2015, typeName: 'apple', key: 'Wednesday', value: 3 },
  { timeline: 2015, typeName: 'pear', key: 'Monday', value: 11 },
  { timeline: 2015, typeName: 'pear', key: 'Tuesday', value: 21 },
  { timeline: 2015, typeName: 'pear', key: 'Wednesday', value: 31 },
  { timeline: 2015, typeName: 'banana', key: 'Monday', value: 31 },
  { timeline: 2015, typeName: 'banana', key: 'Tuesday', value: 32 },
  { timeline: 2015, typeName: 'banana', key: 'Wednesday', value: 33 },
  { timeline: 2016, typeName: 'apple', key: 'Monday', value: 1 },
  { timeline: 2016, typeName: 'apple', key: 'Tuesday', value: 2 },
  { timeline: 2016, typeName: 'apple', key: 'Wednesday', value: 3 },
  { timeline: 2016, typeName: 'pear', key: 'Monday', value: 11 },
  { timeline: 2016, typeName: 'pear', key: 'Tuesday', value: 21 },
  { timeline: 2016, typeName: 'pear', key: 'Wednesday', value: 31 },
  { timeline: 2016, typeName: 'banana', key: 'Monday', value: 31 },
  { timeline: 2016, typeName: 'banana', key: 'Tuesday', value: 32 },
  { timeline: 2016, typeName: 'banana', key: 'Wednesday', value: 33 },
  ...
], {
  // 可以使用 $timeline 进行占位，该字符串将替换为 timeline 属性的值
  title: '$timeline时间轴柱状图'
})
```

#### options 可配置参数

| 属性 | 说明 | required | 类型 | 默认值 |
| :----| :------| :--------| :---:| :------|
| title | 图表标题 | - | String | '单条柱状图' 或 '多条柱状图' |
| keyMap | 数据列表的属性字典 | - | Object | 详见后续属性 |
| keyMap.xAxisKey | x 轴对应的属性名称 | - | String | 'key' |
| keyMap.dataKey | 数据值对应的属性名称 | - | String | 'value' |
| keyMap.legendKey | 图例对应的属性名称 | - | String | 'typeName' |
| keyMap.timelineKey | 时间轴对应的属性名称 | - | String | 'timeline' |
| reverseAxis | 是否为横向图 | - | Boolean | false |
| stack | 是否为堆叠图 | - | Boolean | false |
| lineTypes | 折柱混合图中折线数据对应的图例名称数组 | - | Array | - |
| yAxis | 自定义的 y 轴，请参考 echarts 配置 | - | Object | - |

### 饼状图

#### 基本用法

``` js
Chart.pie('#chart', [
  { key: 'Monday', value: 1 },
  { key: 'Tuesday', value: 2 },
  { key: 'Wednesday', value: 3 },
  ...
])
```

#### 环形图

``` js
Chart.pie('#chart', [
  { key: 'Monday', value: 1 },
  { key: 'Tuesday', value: 2 },
  { key: 'Wednesday', value: 3 },
  ...
], { 
  ring: true 
})
```

#### options 可配置参数

| 属性 | 说明 | required | 类型 | 默认值 |
| :----| :------| :--------| :---:| :------|
| title | 图表标题 | - | String | '饼图' |
| keyMap | 数据列表的属性字典 | - | Object | 详见后续属性 |
| keyMap.textKey | 文本对应的属性名称 | - | String | 'key' |
| keyMap.dataKey | 数据值对应的属性名称 | - | String | 'value' |
| ring | 是否环形图 | - | Boolean | false |

### 雷达图

#### 基本用法

``` js
Chart.radar('#chart', [
  { typeName: 'apple', key: 'Monday', value: 1 },
  { typeName: 'apple', key: 'Tuesday', value: 2 },
  { typeName: 'apple', key: 'Wednesday', value: 3 },
  { typeName: 'pear', key: 'Monday', value: 11 },
  { typeName: 'pear', key: 'Tuesday', value: 21 },
  { typeName: 'pear', key: 'Wednesday', value: 31 },
  { typeName: 'banana', key: 'Monday', value: 31 },
  { typeName: 'banana', key: 'Tuesday', value: 32 },
  { typeName: 'banana', key: 'Wednesday', value: 33 },
  ...
])
```

#### options 可配置参数

| 属性 | 说明 | required | 类型 | 默认值 |
| :----| :------| :--------| :---:| :------|
| title | 图表标题 | - | String | '雷达图' |
| keyMap | 数据列表的属性字典 | - | Object | 详见后续属性 |
| keyMap.textKey | 指标对应的属性名称 | - | String | 'key' |
| keyMap.dataKey | 数据值对应的属性名称 | - | String | 'value' |
| keyMap.legendKey | 图例对应的属性名称 | - | String | 'typeName' |

### 漏斗图

#### 基本用法

``` js
Chart.funnel('#chart', [
  { key: 'Monday', value: 1 },
  { key: 'Tuesday', value: 2 },
  { key: 'Wednesday', value: 3 },
  ...
])
```

#### options 可配置参数

| 属性 | 说明 | required | 类型 | 默认值 |
| :----| :------| :--------| :---:| :------|
| title | 图表标题 | - | String | '漏斗图' |
| keyMap | 数据列表的属性字典 | - | Object | 详见后续属性 |
| keyMap.textKey | 文本对应的属性名称 | - | String | 'key' |
| keyMap.dataKey | 数据值对应的属性名称 | - | String | 'value' |

### 气泡图

#### 基本用法

``` js
Chart.scatter('#chart', [
  { typeName: 'apple', text: 'Monday', x: 1, y: 1, size: 1 },
  { typeName: 'apple', text: 'Tuesday', x: 2, y: 2, size: 2 },
  { typeName: 'apple', text: 'Wednesday', x: 3, y: 3, size: 3 },
  { typeName: 'pear', text: 'Monday', x: 11, y: 11, size: 11 },
  { typeName: 'pear', text: 'Tuesday', x: 21, y: 21, size: 21 },
  { typeName: 'pear', text: 'Wednesday', x: 31, y: 31, size: 31 },
  { typeName: 'banana', text: 'Monday', x: 31, y: 31, size: 31 },
  { typeName: 'banana', text: 'Tuesday', x: 32, y: 32, size: 32 },
  { typeName: 'banana', text: 'Wednesday', x: 33, y: 33, size: 33 },
  ...
])
```

#### options 可配置参数

| 属性 | 说明 | required | 类型 | 默认值 |
| :----| :------| :--------| :---:| :------|
| title | 图表标题 | - | String | '气泡图' |
| keyMap | 数据列表的属性字典 | - | Object | 详见后续属性 |
| keyMap.xKey | x 坐标对应的属性名称 | - | String | 'x' |
| keyMap.yKey | y 坐标对应的属性名称 | - | String | 'y' |
| keyMap.sizeKey | 气泡大小对应的属性名称 | - | String | 'size' |
| keyMap.textKey | 气泡文本对应的属性名称 | - | String | 'text' |
| keyMap.legendKey | 图例对应的属性名称 | - | String | 'typeName' |

### 全局配置

#### 主题

注册并按需使用
``` js
import themeData from './theme.json'

// 注册主题
Chart.config({
  theme: {
    name: 'themeName',
    data: themeData
  }
})

// 使用主题
Chart.line('#chart', data, { theme: 'themeName' })
```

注册并全局使用
``` js
import themeData from './theme.json'

// 注册为默认主题后，所有的图表均使用该主题，不需要特别指定
Chart.config({
  theme: {
    name: 'themeName',
    data: themeData,
    default: true
  }
})
```

### 实例 API

| 方法 | 说明 | 参数 | 参数类型 | 返回值 |
| :----| :------| :--------| :---:| :------|
| setData | 重新设置数据 | data | Array | - |

## License

[MIT](http://opensource.org/licenses/MIT)
