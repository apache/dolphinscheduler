# Front-end development documentation

### Technical selection

```
Vue mvvm framework

Es6 ECMAScript 6.0

Ans-ui Analysys-ui

D3  Visual Library Chart Library

Jsplumb connection plugin library

Lodash high performance JavaScript utility library
```

### Development environment

- 

#### Node installation

Node package download (note version v12.20.2) `https://nodejs.org/download/release/v12.20.2/`

- 

#### Front-end project construction

Use the command line mode `cd`  enter the `dolphinscheduler-ui` project directory and execute `npm install` to pull the project dependency package.

> If `npm install` is very slow, you can set the taobao mirror

```
npm config set registry http://registry.npm.taobao.org/
```

- Modify `API_BASE` in the file `dolphinscheduler-ui/.env` to interact with the backend:

```
# back end interface address
API_BASE = http://127.0.0.1:12345
```

##### ! ! ! Special attention here. If the project reports a "node-sass error" error while pulling the dependency package, execute the following command again after execution.

```bash
npm install node-sass --unsafe-perm #Install node-sass dependency separately
```

- 

#### Development environment operation

- `npm start` project development environment (after startup address http://localhost:8888)

#### Front-end project release

- `npm run build` project packaging (after packaging, the root directory will create a folder called dist for publishing Nginx online)

Run the `npm run build` command to generate a package file (dist) package

Copy it to the corresponding directory of the server (front-end service static page storage directory)

Visit address` http://localhost:8888`

#### Start with node and daemon under Linux

Install pm2 `npm install -g pm2`

Execute `pm2 start npm -- run dev` to start the project in the project `dolphinscheduler-ui `root directory

#### command

- Start `pm2 start npm -- run dev`

- Stop `pm2 stop npm`

- delete `pm2 delete npm`

- Status  `pm2 list`

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

### Project directory structure

`build` some webpack configurations for packaging and development environment projects

`node_modules` development environment node dependency package

`src` project required documents

`src => combo` project third-party resource localization `npm run combo` specific view `build/combo.js`

`src => font` Font icon library can be added by visiting https://www.iconfont.cn Note: The font library uses its own secondary development to reintroduce its own library `src/sass/common/_font.scss`

`src => images` public image storage

`src => js` js/vue

`src => lib` internal components of the company (company component library can be deleted after open source)

`src => sass` sass file One page corresponds to a sass file

`src => view` page file One page corresponds to an html file

```
> Projects are developed using vue single page application (SPA)
- All page entry files are in the `src/js/conf/${ corresponding page filename => home} index.js` entry file
- The corresponding sass file is in `src/sass/conf/${corresponding page filename => home}/index.scss`
- The corresponding html file is in `src/view/${corresponding page filename => home}/index.html`
```

Public module and util `src/js/module`

`components` => internal project common components

`download` => download component

`echarts` => chart component

`filter` => filter and vue pipeline

`i18n` => internationalization

`io` => io request encapsulation based on axios

`mixin` => vue mixin public part for disabled operation

`permissions` => permission operation

`util` => tool

### System function module

Home  => `http://localhost:8888/#/home`

Project Management => `http://localhost:8888/#/projects/list`

```
| Project Home
| Workflow
  - Workflow definition
  - Workflow instance
  - Task instance
```

Resource Management => `http://localhost:8888/#/resource/file`

```
| File Management
| udf Management
  - Resource Management
  - Function management
```

Data Source Management => `http://localhost:8888/#/datasource/list`

Security Center => `http://localhost:8888/#/security/tenant`

```
| Tenant Management
| User Management
| Alarm Group Management
  - master
  - worker
```

User Center => `http://localhost:8888/#/user/account`

## Routing and state management

The project `src/js/conf/home` is divided into

`pages` => route to page directory

```
The page file corresponding to the routing address
```

`router` => route management

```
vue router, the entry file index.js in each page will be registered. Specific operations: https://router.vuejs.org/zh/
```

`store` => status management

```
The page corresponding to each route has a state management file divided into:

actions => mapActions => Details：https://vuex.vuejs.org/zh/guide/actions.html

getters => mapGetters => Details：https://vuex.vuejs.org/zh/guide/getters.html

index => entrance

mutations => mapMutations => Details：https://vuex.vuejs.org/zh/guide/mutations.html

state => mapState => Details：https://vuex.vuejs.org/zh/guide/state.html

Specific action：https://vuex.vuejs.org/zh/
```

## specification

## Vue specification

##### 1.Component name

The component is named multiple words and is connected with a wire (-) to avoid conflicts with HTML tags and a clearer structure.

```
// positive example
export default {
    name: 'page-article-item'
}
```

##### 2.Component files

The internal common component of the `src/js/module/components` project writes the folder name with the same name as the file name. The subcomponents and util tools that are split inside the common component are placed in the internal `_source` folder of the component.

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

When you define Prop, you should always name it in camel format (camelCase) and use the connection line (-) when assigning values to the parent component.
This follows the characteristics of each language, because it is case-insensitive in HTML tags, and the use of links is more friendly; in JavaScript, the more natural is the hump name.

```
// Vue
props: {
    articleStatus: Boolean
}
// HTML
<article-item :article-status="true"></article-item>
```

The definition of Prop should specify its type, defaults, and validation as much as possible.

Example：

```
props: {
    attrM: Number,
    attrA: {
        type: String,
        required: true
    },
    attrZ: {
        type: Object,
        //  The default value of the array/object should be returned by a factory function
        default: function () {
            return {
                msg: 'achieve you and me'
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

When performing v-for traversal, you should always bring a key value to make rendering more efficient when updating the DOM.

```
<ul>
    <li v-for="item in list" :key="item.id">
        {{ item.title }}
    </li>
</ul>
```

v-for should be avoided on the same element as v-if (`for example: <li>`) because v-for has a higher priority than v-if. To avoid invalid calculations and rendering, you should try to use v-if Put it on top of the container's parent element.

```
<ul v-if="showList">
    <li v-for="item in list" :key="item.id">
        {{ item.title }}
    </li>
</ul>
```

##### 5.v-if / v-else-if / v-else

If the elements in the same set of v-if logic control are logically identical, Vue reuses the same part for more efficient element switching, `such as: value`. In order to avoid the unreasonable effect of multiplexing, you should add key to the same element for identification.

```
<div v-if="hasData" key="mazey-data">
    <span>{{ mazeyData }}</span>
</div>
<div v-else key="mazey-none">
    <span>no data</span>
</div>
```

##### 6.Instruction abbreviation

In order to unify the specification, the instruction abbreviation is always used. Using `v-bind`, `v-on` is not bad. Here is only a unified specification.

```
<input :value="mazeyUser" @click="verifyUser">
```

##### 7.Top-level element order of single file components

Styles are packaged in a file, all the styles defined in a single vue file, the same name in other files will also take effect. All will have a top class name before creating a component.
Note: The sass plugin has been added to the project, and the sas syntax can be written directly in a single vue file.
For uniformity and ease of reading, they should be placed in the order of  `<template>`、`<script>`、`<style>`.

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

## JavaScript specification

##### 1.var / let / const

It is recommended to no longer use var, but use let / const, prefer const. The use of any variable must be declared in advance, except that the function defined by function can be placed anywhere.

##### 2.quotes

```
const foo = 'after division'
const bar = `${foo}，ront-end engineer`
```

##### 3.function

Anonymous functions use the arrow function uniformly. When multiple parameters/return values are used, the object's structure assignment is used first.

```
function getPersonInfo ({name, sex}) {
    // ...
    return {name, gender}
}
```

The function name is uniformly named with a camel name. The beginning of the capital letter is a constructor. The lowercase letters start with ordinary functions, and the new operator should not be used to operate ordinary functions.

##### 4.object

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

##### 5.module

Unified management of project modules using import / export.

```
// lib.js
export default {}

// app.js
import app from './lib'
```

Import is placed at the top of the file.

If the module has only one output value, use `export default`，otherwise no.

## HTML / CSS

##### 1.Label

Do not write the type attribute when referencing external CSS or JavaScript. The HTML5 default type is the text/css and text/javascript properties, so there is no need to specify them.

```
<link rel="stylesheet" href="//www.test.com/css/test.css">
<script src="//www.test.com/js/test.js"></script>
```

##### 2.Naming

The naming of Class and ID should be semantic, and you can see what you are doing by looking at the name; multiple words are connected by a link.

```
// positive example
.test-header{
    font-size: 20px;
}
```

##### 3.Attribute abbreviation

CSS attributes use abbreviations as much as possible to improve the efficiency and ease of understanding of the code.

```
// counter example
border-width: 1px;
border-style: solid;
border-color: #ccc;

// positive example
border: 1px solid #ccc;
```

##### 4.Document type

The HTML5 standard should always be used.

```
<!DOCTYPE html>
```

##### 5.Notes

A block comment should be written to a module file.

```
/**
* @module mazey/api
* @author Mazey <mazey@mazey.net>
* @description test.
* */
```

## interface

##### All interfaces are returned as Promise

Note that non-zero is wrong for catching catch

```
const test = () => {
  return new Promise((resolve, reject) => {
    resolve({
      a:1
    })
  })
}

// transfer
test.then(res => {
  console.log(res)
  // {a:1}
})
```

Normal return

```
{
  code:0,
  data:{}
  msg:'success'
}
```

Error return

```
{
  code:10000, 
  data:{}
  msg:'failed'
}
```

If the interface is a post request, the Content-Type defaults to application/x-www-form-urlencoded; if the Content-Type is changed to application/json,
Interface parameter transfer needs to be changed to the following way

```
io.post('url', payload, null, null, { emulateJSON: false } res => {
  resolve(res)
}).catch(e => {
  reject(e)
})
```

##### Related interface path

dag related interface `src/js/conf/home/store/dag/actions.js`

Data Source Center Related Interfaces  `src/js/conf/home/store/datasource/actions.js`

Project Management Related Interfaces `src/js/conf/home/store/projects/actions.js`

Resource Center Related Interfaces `src/js/conf/home/store/resource/actions.js`

Security Center Related Interfaces `src/js/conf/home/store/security/actions.js`

User Center Related Interfaces `src/js/conf/home/store/user/actions.js`

## Extended development

##### 1.Add node

(1) First place the icon icon of the node in the `src/js/conf/home/pages/dag/img `folder, and note the English name of the node defined by the `toolbar_${in the background. For example: SHELL}.png`

(2)  Find the `tasksType` object in `src/js/conf/home/pages/dag/_source/config.js` and add it to it.

```
'DEPENDENT': {  //  The background definition node type English name is used as the key value
  desc: 'DEPENDENT',  // tooltip desc
  color: '#2FBFD8'  // The color represented is mainly used for tree and gantt
}
```

(3)  Add a `${node type (lowercase)}`.vue file in `src/js/conf/home/pages/dag/_source/formModel/tasks`. The contents of the components related to the current node are written here. Must belong to a node component must have a function _verification () After the verification is successful, the relevant data of the current component is thrown to the parent component.

```
/**
 * Verification
*/
  _verification () {
    // datasource subcomponent verification
    if (!this.$refs.refDs._verifDatasource()) {
      return false
    }

    // verification function
    if (!this.method) {
      this.$message.warning(`${i18n.$t('Please enter method')}`)
      return false
    }

    // localParams subcomponent validation
    if (!this.$refs.refLocalParams._verifProp()) {
      return false
    }
    // store
    this.$emit('on-params', {
      type: this.type,
      datasource: this.datasource,
      method: this.method,
      localParams: this.localParams
    })
    return true
  }
```

(4) Common components used inside the node component are under` _source`, and `commcon.js` is used to configure public data.

##### 2.Increase the status type

(1) Find the `tasksState` object in `src/js/conf/home/pages/dag/_source/config.js` and add it to it.

```
 'WAITTING_DEPEND': {  // 'WAITTING_DEPEND': {  //Backend defines state type, frontend is used as key value
  id: 11,  // front-end definition id is used as a sort
  desc: `${i18n.$t('waiting for dependency')}`,  // tooltip desc
  color: '#5101be',  // The color represented is mainly used for tree and gantt
  icoUnicode: '&#xe68c;',  // font icon
  isSpin: false  // whether to rotate (requires code judgment)
}
```

##### 3.Add the action bar tool

(1)  Find the `toolOper` object in `src/js/conf/home/pages/dag/_source/config.js` and add it to it.

```
{
  code: 'pointer',  // tool identifier
  icon: '&#xe781;',  // tool icon
  disable: disable,  // disable
  desc: `${i18n.$t('Drag node and selected item')}`  // tooltip desc
}
```

(2) Tool classes are returned as a constructor  `src/js/conf/home/pages/dag/_source/plugIn`

`downChart.js`  =>  dag image download processing

`dragZoom.js`  =>  mouse zoom effect processing

`jsPlumbHandle.js`  =>  drag and drop line processing

`util.js`  =>   belongs to the `plugIn` tool class

The operation is handled in the `src/js/conf/home/pages/dag/_source/dag.js` => `toolbarEvent` event.

##### 3.Add a routing page

(1) First add a routing address`src/js/conf/home/router/index.js` in route management

```
routing address{
  path: '/test',  // routing address
  name: 'test',  // alias
  component: resolve => require(['../pages/test/index'], resolve),  // route corresponding component entry file
  meta: {
    title: `${i18n.$t('test')} - EasyScheduler`  // title display
  }
},
```

(2)Create a `test` folder in `src/js/conf/home/pages` and create an `index.vue `entry file in the folder.

        This will give you direct access to`http://localhost:8888/#/test`

##### 4.Increase the preset mailbox

Find the `src/lib/localData/email.js` startup and timed email address input to automatically pull down the match.

```
export default ["test@analysys.com.cn","test1@analysys.com.cn","test3@analysys.com.cn"]
```

##### 5.Authority management and disabled state processing

The permission gives the userType according to the backUser interface `getUserInfo` interface: `"ADMIN_USER/GENERAL_USER" `permission to control whether the page operation button is `disabled`.

specific operation：`src/js/module/permissions/index.js`

disabled processing：`src/js/module/mixin/disabledState.js`

