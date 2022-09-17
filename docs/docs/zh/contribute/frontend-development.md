# 前端开发文档

### 技术选型

```
Vue mvvm 框架

Es6 ECMAScript 6.0

Ans-ui Analysys-ui

D3 可视化库图表库

Jsplumb 连线插件库

Lodash 高性能的 JavaScript 实用工具库
```

### 开发环境搭建

- 

#### Node安装

Node包下载 (注意版本 v12.20.2) `https://nodejs.org/download/release/v12.20.2/`

- 

#### 前端项目构建

用命令行模式 `cd`  进入 `dolphinscheduler-ui`项目目录并执行 `npm install` 拉取项目依赖包

> 如果 `npm install` 速度非常慢，你可以设置淘宝镜像

```
npm config set registry http://registry.npm.taobao.org/
```

- 修改 `dolphinscheduler-ui/.env` 文件中的 `API_BASE`，用于跟后端交互：

```
# 代理的接口地址（自行修改）
API_BASE = http://127.0.0.1:12345
```

##### ！！！这里特别注意 项目如果在拉取依赖包的过程中报 " node-sass error " 错误，请在执行完后再次执行以下命令

```bash
npm install node-sass --unsafe-perm #单独安装node-sass依赖
```

- 

#### 开发环境运行

- `npm start` 项目开发环境 (启动后访问地址 http://localhost:8888)

#### 前端项目发布

- `npm run build` 项目打包 (打包后根目录会创建一个名为dist文件夹，用于发布线上Nginx)

运行 `npm run build` 命令，生成打包文件（dist）包

再拷贝到服务器对应的目录下（前端服务静态页面存放目录）

访问地址 `http://localhost:8888`

#### Linux下使用node启动并且守护进程

安装pm2 `npm install -g pm2`

在项目`dolphinscheduler-ui`根目录执行 `pm2 start npm -- run dev` 启动项目

#### 命令

- 启用 `pm2 start npm -- run dev`

- 停止 `pm2 stop npm`

- 删除 `pm2 delete npm`

- 状态 `pm2 list`

```

[root@localhost dolphinscheduler-ui]# pm2 start npm -- run dev
[PM2] Applying action restartProcessId on app [npm](ids: 0)
[PM2] [npm](0) ✓
[PM2] Process successfully started
┌──────────┬────┬─────────┬──────┬──────┬────────┬─────────┬────────┬─────┬──────────┬──────┬──────────┐
│ App name │ id │ version │ mode │ pid  │ status │ restart │ uptime │ cpu │ mem      │ user │ watching │
├──────────┼────┼─────────┼──────┼──────┼────────┼─────────┼────────┼─────┼──────────┼──────┼──────────┤
│ npm      │ 0  │ N/A     │ fork │ 6168 │ online │ 31      │ 0s     │ 0%  │ 5.6 MB   │ root │ disabled │
└──────────┴────┴─────────┴──────┴──────┴────────┴─────────┴────────┴─────┴──────────┴──────┴──────────┘
 Use `pm2 show <id|name>` to get more details about an app

```

### 项目目录结构

`build` 打包及开发环境项目的一些webpack配置

`node_modules` 开发环境node依赖包

`src` 项目所需文件

`src => combo` 项目第三方资源本地化 `npm run combo`具体查看`build/combo.js`

`src => font` 字体图标库可访问 `https://www.iconfont.cn` 进行添加 注意：字体库用的自己的 二次开发需要重新引入自己的库 `src/sass/common/_font.scss`

`src => images` 公共图片存放

`src => js` js/vue

`src => lib` 公司内部组件（公司组件库开源后可删掉）

`src => sass` sass文件 一个页面对应一个sass文件

`src => view` 页面文件 一个页面对应一个html文件

```
> 项目采用vue单页面应用(SPA)开发
- 所有页面入口文件在 `src/js/conf/${对应页面文件名 => home}` 的 `index.js` 入口文件
- 对应的sass文件则在 `src/sass/conf/${对应页面文件名 => home}/index.scss`
- 对应的html文件则在 `src/view/${对应页面文件名 => home}/index.html`
```

公共模块及util `src/js/module`

`components` => 内部项目公共组件

`download` => 下载组件

`echarts` => 图表组件

`filter` => 过滤器和vue管道

`i18n` => 国际化

`io` => io请求封装 基于axios

`mixin` => vue mixin 公共部分 用于disabled操作

`permissions` => 权限操作

`util` => 工具

### 系统功能模块

首页 => `http://localhost:8888/#/home`

项目管理 => `http://localhost:8888/#/projects/list`

```
| 项目首页
| 工作流
  - 工作流定义
  - 工作流实例
  - 任务实例
```

资源管理 => `http://localhost:8888/#/resource/file`

```
| 文件管理
| UDF管理
  - 资源管理
  - 函数管理
```

数据源管理 => `http://localhost:8888/#/datasource/list`

安全中心 => `http://localhost:8888/#/security/tenant`

```
| 租户管理
| 用户管理
| 告警组管理
  - master
  - worker
```

用户中心 => `http://localhost:8888/#/user/account`

## 路由和状态管理

项目 `src/js/conf/home` 下分为

`pages` => 路由指向页面目录

```
路由地址对应的页面文件
```

`router` => 路由管理

```
vue的路由器，在每个页面的入口文件index.js 都会注册进来 具体操作：https://router.vuejs.org/zh/
```

`store` => 状态管理

```
每个路由对应的页面都有一个状态管理的文件 分为：

actions => mapActions => 详情：https://vuex.vuejs.org/zh/guide/actions.html

getters => mapGetters => 详情：https://vuex.vuejs.org/zh/guide/getters.html

index => 入口

mutations => mapMutations => 详情：https://vuex.vuejs.org/zh/guide/mutations.html

state => mapState => 详情：https://vuex.vuejs.org/zh/guide/state.html

具体操作：https://vuex.vuejs.org/zh/
```

## 规范

## Vue规范

##### 1.组件名

组件名为多个单词，并且用连接线（-）连接，避免与 HTML 标签冲突，并且结构更加清晰。

```
// 正例
export default {
    name: 'page-article-item'
}
```

##### 2.组件文件

`src/js/module/components`项目内部公共组件书写文件夹名与文件名同名,公共组件内部所拆分的子组件与util工具都放置组件内部 `_source`文件夹里。

```
└── components
    ├── header
        ├── header.vue
        └── _source
            └── nav.vue
            └── util.js
    ├── conditions
        ├── conditions.vue
        └── _source
            └── search.vue
            └── util.js
```

##### 3.Prop

定义 Prop 的时候应该始终以驼峰格式（camelCase）命名，在父组件赋值的时候使用连接线（-）。
这里遵循每个语言的特性，因为在 HTML 标记中对大小写是不敏感的，使用连接线更加友好；而在 JavaScript 中更自然的是驼峰命名。

```
// Vue
props: {
    articleStatus: Boolean
}
// HTML
<article-item :article-status="true"></article-item>
```

Prop 的定义应该尽量详细的指定其类型、默认值和验证。

示例：

```
props: {
    attrM: Number,
    attrA: {
        type: String,
        required: true
    },
    attrZ: {
        type: Object,
        // 数组/对象的默认值应该由一个工厂函数返回
        default: function () {
            return {
                msg: '成就你我'
            }
        }
    },
    attrE: {
        type: String,
        validator: function (v) {
            return !(['success', 'fail'].indexOf(v) === -1) 
        }
    }
}
```

##### 4.v-for

在执行 v-for 遍历的时候，总是应该带上 key 值使更新 DOM 时渲染效率更高。

```
<ul>
    <li v-for="item in list" :key="item.id">
        {{ item.title }}
    </li>
</ul>
```

v-for 应该避免与 v-if 在同一个元素（`例如：<li>`）上使用，因为 v-for 的优先级比 v-if 更高，为了避免无效计算和渲染，应该尽量将 v-if 放到容器的父元素之上。

```
<ul v-if="showList">
    <li v-for="item in list" :key="item.id">
        {{ item.title }}
    </li>
</ul>
```

##### 5.v-if / v-else-if / v-else

若同一组 v-if 逻辑控制中的元素逻辑相同，Vue 为了更高效的元素切换，会复用相同的部分，`例如：value`。为了避免复用带来的不合理效果，应该在同种元素上加上 key 做标识。

```
<div v-if="hasData" key="mazey-data">
    <span>{{ mazeyData }}</span>
</div>
<div v-else key="mazey-none">
    <span>无数据</span>
</div>
```

##### 6.指令缩写

为了统一规范始终使用指令缩写，使用`v-bind`，`v-on`并没有什么不好，这里仅为了统一规范。

```
<input :value="mazeyUser" @click="verifyUser">
```

##### 7.单文件组件的顶级元素顺序

样式后续都是打包在一个文件里，所有在单个vue文件中定义的样式，在别的文件里同类名的样式也是会生效的所有在创建一个组件前都会有个顶级类名
注意：项目内已经增加了sass插件，单个vue文件里可以直接书写sass语法
为了统一和便于阅读，应该按 `<template>`、`<script>`、`<style>`的顺序放置。

```
<template>
  <div class="test-model">
    test
  </div>
</template>
<script>
  export default {
    name: "test",
    data() {
      return {}
    },
    props: {},
    methods: {},
    watch: {},
    beforeCreate() {
    },
    created() {
    },
    beforeMount() {
    },
    mounted() {
    },
    beforeUpdate() {
    },
    updated() {
    },
    beforeDestroy() {
    },
    destroyed() {
    },
    computed: {},
    components: {},
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .test-model {

  }
</style>

```

## JavaScript规范

##### 1.var / let / const

建议不再使用 var，而使用 let / const，优先使用 const。任何一个变量的使用都要提前申明，除了 function 定义的函数可以随便放在任何位置。

##### 2.引号

```
const foo = '后除'
const bar = `${foo}，前端工程师`
```

##### 3.函数

匿名函数统一使用箭头函数，多个参数/返回值时优先使用对象的结构赋值。

```
function getPersonInfo ({name, sex}) {
    // ...
    return {name, gender}
}
```

函数名统一使用驼峰命名，以大写字母开头申明的都是构造函数，使用小写字母开头的都是普通函数，也不该使用 new 操作符去操作普通函数。

##### 4.对象

```
const foo = {a: 0, b: 1}
const bar = JSON.parse(JSON.stringify(foo))

const foo = {a: 0, b: 1}
const bar = {...foo, c: 2}

const foo = {a: 3}
Object.assign(foo, {b: 4})

const myMap = new Map([])
for (let [key, value] of myMap.entries()) {
    // ...
}
```

##### 5.模块

统一使用 import / export 的方式管理项目的模块。

```
// lib.js
export default {}

// app.js
import app from './lib'
```

import 统一放在文件顶部。

如果模块只有一个输出值，使用 `export default`，否则不用。

## HTML / CSS

###### 1.标签

在引用外部 CSS 或 JavaScript 时不写 type 属性。HTML5 默认 type 为 `text/css` 和 `text/javascript` 属性，所以没必要指定。

```
<link rel="stylesheet" href="//www.test.com/css/test.css">
<script src="//www.test.com/js/test.js"></script>
```

##### 2.命名

Class 和 ID 的命名应该语义化，通过看名字就知道是干嘛的；多个单词用连接线 - 连接。

```
// 正例
.test-header{
    font-size: 20px;
}
```

##### 3.属性缩写

CSS 属性尽量使用缩写，提高代码的效率和方便理解。

```
// 反例
border-width: 1px;
border-style: solid;
border-color: #ccc;

// 正例
border: 1px solid #ccc;
```

##### 4.文档类型

应该总是使用 HTML5 标准。

```
<!DOCTYPE html>
```

##### 5.注释

应该给一个模块文件写一个区块注释。

```
/**
* @module mazey/api
* @author Mazey <mazey@mazey.net>
* @description test.
* */
```

## 接口

##### 所有的接口都以 Promise 形式返回

注意非0都为错误走catch

```
const test = () => {
  return new Promise((resolve, reject) => {
    resolve({
      a:1
    })
  })
}

// 调用
test.then(res => {
  console.log(res)
  // {a:1}
})
```

正常返回

```
{
  code:0,
  data:{}
  msg:'成功'
}
```

错误返回

```
{
  code:10000, 
  data:{}
  msg:'失败'
}
```

接口如果是post请求，Content-Type默认为application/x-www-form-urlencoded；如果Content-Type改成application/json，
接口传参需要改成下面的方式

```
io.post('url', payload, null, null, { emulateJSON: false } res => {
  resolve(res)
}).catch(e => {
  reject(e)
})
```

##### 相关接口路径

dag 相关接口 `src/js/conf/home/store/dag/actions.js`

数据源中心 相关接口 `src/js/conf/home/store/datasource/actions.js`

项目管理 相关接口 `src/js/conf/home/store/projects/actions.js`

资源中心 相关接口 `src/js/conf/home/store/resource/actions.js`

安全中心 相关接口 `src/js/conf/home/store/security/actions.js`

用户中心 相关接口 `src/js/conf/home/store/user/actions.js`

## 扩展开发

##### 1.增加节点

(1) 先将节点的icon小图标放置`src/js/conf/home/pages/dag/img`文件夹内，注意 `toolbar_${后台定义的节点的英文名称 例如:SHELL}.png`

(2) 找到 `src/js/conf/home/pages/dag/_source/config.js` 里的 `tasksType` 对象，往里增加

```
'DEPENDENT': {  // 后台定义节点类型英文名称用作key值
  desc: 'DEPENDENT',  // tooltip desc
  color: '#2FBFD8'  // 代表的颜色主要用于 tree和gantt 两张图
}
```

(3) 在 `src/js/conf/home/pages/dag/_source/formModel/tasks` 增加一个 `${节点类型（小写）}`.vue 文件，跟当前节点相关的组件内容都在这里写。 属于节点组件内的必须拥有一个函数 `_verification()` 验证成功后将当前组件的相关数据往父组件抛。

```
/**
 * 验证
*/
  _verification () {
    // datasource 子组件验证
    if (!this.$refs.refDs._verifDatasource()) {
      return false
    }

    // 验证函数
    if (!this.method) {
      this.$message.warning(`${i18n.$t('请输入方法')}`)
      return false
    }

    // localParams 子组件验证
    if (!this.$refs.refLocalParams._verifProp()) {
      return false
    }
    // 存储
    this.$emit('on-params', {
      type: this.type,
      datasource: this.datasource,
      method: this.method,
      localParams: this.localParams
    })
    return true
  }
```

(4) 节点组件内部所用到公共的组件都在`_source`下，`commcon.js`用于配置公共数据

##### 2.增加状态类型

(1) 找到 `src/js/conf/home/pages/dag/_source/config.js` 里的 `tasksState` 对象，往里增加

```
'WAITTING_DEPEND': {  //后端定义状态类型 前端用作key值
  id: 11,  // 前端定义id 后续用作排序
  desc: `${i18n.$t('等待依赖')}`,  // tooltip desc
  color: '#5101be',  // 代表的颜色主要用于 tree和gantt 两张图
  icoUnicode: '&#xe68c;',  // 字体图标 
  isSpin: false  // 是否旋转（需代码判断）
}
```

##### 3.增加操作栏工具

(1) 找到 `src/js/conf/home/pages/dag/_source/config.js` 里的 `toolOper` 对象，往里增加

```
{
  code: 'pointer',  // 工具标识
  icon: '&#xe781;',  // 工具图标 
  disable: disable,  // 是否禁用
  desc: `${i18n.$t('拖动节点和选中项')}`  // tooltip desc
}
```

(2) 工具类都以一个构造函数返回 `src/js/conf/home/pages/dag/_source/plugIn`

`downChart.js`  =>  dag 图片下载处理

`dragZoom.js`  =>  鼠标缩放效果处理

`jsPlumbHandle.js`  =>  拖拽线条处理

`util.js`  =>   属于 `plugIn` 工具类

操作则在 `src/js/conf/home/pages/dag/_source/dag.js` => `toolbarEvent` 事件中处理。

##### 3.增加一个路由页面

(1) 首先在路由管理增加一个路由地址`src/js/conf/home/router/index.js`

```
{
  path: '/test',  // 路由地址 
  name: 'test',  // 别名
  component: resolve => require(['../pages/test/index'], resolve),  // 路由对应组件入口文件
  meta: {
    title: `${i18n.$t('test')} - DolphinScheduler`  // title 显示
  }
},
```

(2) 在`src/js/conf/home/pages` 建一个 `test` 文件夹，在文件夹里建一个`index.vue`入口文件。

        这样就可以直接访问 `http://localhost:8888/#/test`

##### 4.增加预置邮箱

找到`src/lib/localData/email.js`启动和定时邮箱地址输入可以自动下拉匹配。

```
export default ["test@analysys.com.cn","test1@analysys.com.cn","test3@analysys.com.cn"]
```

##### 5.权限管理及disabled状态处理

权限根据后端接口`getUserInfo`接口给出`userType: "ADMIN_USER/GENERAL_USER"`权限控制页面操作按钮是否`disabled`

具体操作：`src/js/module/permissions/index.js`

disabled处理：`src/js/module/mixin/disabledState.js`

