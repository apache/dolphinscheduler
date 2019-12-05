## Box

Box包含modal、message、notice三个组件。

### Modal options

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
title | 标题 | String / DOM | - | -
content | 内容 | String / DOM | - | -
width | 宽度 | Number / String | - | 520
className | 自定义样式名称 | String | - | -
closable | 是否显示关闭 | Boolean | - | true
escClose | 是否按 esc 键关闭 | Boolean | - | false
ok | 点击确定的回调 | Object | {show [Boolean] ,text [String], handle[Function]} | -
cancel | 点击取消的回调 | Object | {show [Boolean] ,text [String], handle[Function]} | -
render | 自定义内容 | Function | 使用时 content, title ,ok , cancel 失效 | -
showMask | 是否显示遮罩 | Boolean | - | false
maskClosable | 点击遮罩是否关闭 | Boolean | - | false

#### Modal 实例方法

instance.remove() 销毁当前实例

#### Modal 全局相关

this.$modal.destroy() 全局销毁所有实例

### Message options

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
content | 内容 | String | - | -
duration | 自动关闭的延时，单位秒，不关闭可以写 0 | Number | - | 1.5
onClose | 关闭时的回调 | Function | - | -
closable | 是否显示关闭图标 | Boolean | - | false

#### Message 全局相关

this.$message.destroy() 全局销毁所有实例

this.$message.config(options) 全局配置

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
top | 提示组件距离顶端的距离，单位像素 | Number | - | 60
duration | 默认自动关闭的延时，单位秒 | Number | - | 1.5
transitionName | 默认动画类名 | String | - | x-ani-move-in
fixed | 显示是否固定位置 | String | Boolean | true

### Notice options

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
title | 标题 | String | - | -
content | 内容 | String | - | -
duration | 自动关闭的延时，单位秒，不关闭可以写 0 | Number | - | 1.5
onClose | 关闭时的回调 | Function | - | -
closable | 是否显示关闭图标 | Boolean | - | false

#### Notice 全局相关

this.$notice.destroy() 全局销毁所有实例

this.$notice.config(options) 全局配置

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
top | 提示组件距离顶端的距离，单位像素 | Number | - | 60
right | 提示组件距离屏幕右侧的距离，单位像素 | Number | - | 20
duration | 默认自动关闭的延时，单位秒 | Number | - | 1.5
transitionName | 默认动画类名 | String | - | x-ani-move-right
list | 显示是否以列表形式展示 | Boolean | - | true