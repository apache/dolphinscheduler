# 프론트엔드 개발 문서

### 기술선정```
Vue mvvm framework

Es6 ECMAScript 6.0

Ans-ui Analysys-ui

D3  Visual Library Chart Library

Jsplumb connection plugin library

Lodash high performance JavaScript utility library
````

### 개발 환경

-

#### 노드 설치

노드 패키지 다운로드(참고 버전 v12.20.2) `https://nodejs.org/download/release/v12.20.2/`

-

#### 프론트엔드 프로젝트 구축

명령줄 모드 `cd`를 사용하여 `dolphinscheduler-ui` 프로젝트 디렉터리를 입력하고 `npm install`을 실행하여 프로젝트 종속성 패키지를 가져옵니다.

> `npm install`이 매우 느린 경우 taobao 미러를 설정할 수 있습니다.```
npm config set registry http://registry.npmmirror.com/
````

- 백엔드와 상호 작용하도록 `dolphinscheduler-ui/.env.development` 파일에서 `VITE_APP_DEV_WEB_URL`을 수정합니다.```
# back end interface address
VITE_APP_DEV_WEB_URL = 'http://127.0.0.1:12345'
````

##### !!!여기에 특별한주의를 기울이십시오.프로젝트가 종속성 패키지를 가져오는 동안 "node-sass error" 오류를 보고하는 경우 실행 후 다음 명령을 다시 실행하십시오.```bash
npm install node-sass --unsafe-perm #Install node-sass dependency separately
````

-

#### 개발환경 운영

- `pnpm run dev` 프로젝트 개발 환경(시작 주소 http://localhost:8888 이후)

#### 프론트엔드 프로젝트 릴리스

- `pnpm run build:prod` 프로젝트 패키징(패키징 후 루트 디렉터리는 Nginx 온라인 게시를 위해 dist라는 폴더를 생성합니다)

`pnpm run build:prod` 명령을 실행하여 패키지 파일(dist) 패키지를 생성합니다.

서버의 해당 디렉터리(프런트 엔드 서비스 정적 페이지 저장소 디렉터리)에 복사합니다.

방문주소` http://localhost:8888`

#### Linux에서 노드와 데몬으로 시작

pm2 `npm install -g pm2` 설치

`pm2 start npm -- run dev`를 실행하여 프로젝트 `dolphinscheduler-ui`root 디렉토리에서 프로젝트를 시작합니다.

#### 명령

-`pm2 start npm -- run dev` 시작

-`pm2 stop npm`을 중지하세요.

- `pm2 삭제 npm` 삭제

- 상태`pm2 목록````

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

````

### 프로젝트 디렉터리 구조

패키징 및 개발 환경 프로젝트를 위한 일부 웹팩 구성 '빌드'

`node_modules` 개발 환경 노드 종속성 패키지

`src` 프로젝트 필수 서류

`src => 콤보` 프로젝트 타사 리소스 현지화 `npm run 콤보` 특정 뷰 `build/combo.js`

`src => 글꼴` 글꼴 아이콘 라이브러리는 https://www.iconfont.cn을 방문하여 추가할 수 있습니다. 참고: 글꼴 라이브러리는 자체 보조 개발을 사용하여 자체 라이브러리 `src/sass/common/_font.scss`를 다시 도입합니다.

`src => 이미지` 공개 이미지 저장

`src => js` js/vue

`src => lib` 회사 내부 구성요소 (회사 구성요소 라이브러리는 오픈소스 이후 삭제될 수 있음)

`src => sass` sass 파일 한 페이지는 sass 파일에 해당합니다.

`src => view` 페이지 파일 한 페이지는 html 파일에 해당합니다.```
> Projects are developed using vue single page application (SPA)
- All page entry files are in the `src/js/conf/${ corresponding page filename => home} index.js` entry file
- The corresponding sass file is in `src/sass/conf/${corresponding page filename => home}/index.scss`
- The corresponding html file is in `src/view/${corresponding page filename => home}/index.html`
````

공개 모듈 및 유틸리티 `src/js/module`

`comComponents` => 내부 프로젝트 공통 구성요소

`다운로드` => 구성 요소 다운로드

`echarts` => 차트 구성 요소

`filter` => 필터 및 vue 파이프라인

`i18n` => 국제화

`io` => io는 Axios를 기반으로 캡슐화를 요청합니다.

`mixin` => 비활성화된 작업에 대한 vue mixin 공개 부분

`permissions` => 권한 작업

`util` => 도구

### 시스템 기능 모듈

홈 => `http://localhost:8888/#/home`

프로젝트 관리 => `http://localhost:8888/#/projects/list````
| Project Home
| Workflow
  - Workflow definition
  - Workflow instance
  - Task instance
````

자원 관리 => `http://localhost:8888/#/resource/file````
| File Management
````

데이터 소스 관리 => `http://localhost:8888/#/datasource/list`

보안 센터 => `http://localhost:8888/#/security/tenant````
| Tenant Management
| User Management
| Alarm Group Management
  - master
  - worker
````

사용자 센터 => `http://localhost:8888/#/user/account`

## 라우팅 및 상태 관리

`src/js/conf/home` 프로젝트는 다음과 같이 나누어집니다.

`pages` => 페이지 디렉토리로 라우팅```
The page file corresponding to the routing address
````

`라우터` => 경로 관리```
vue router, the entry file index.js in each page will be registered. Specific operations: https://router.vuejs.org/zh/
````

`store` => 상태 관리```
The page corresponding to each route has a state management file divided into:

actions => mapActions => Details：https://vuex.vuejs.org/zh/guide/actions.html

getters => mapGetters => Details：https://vuex.vuejs.org/zh/guide/getters.html

index => entrance

mutations => mapMutations => Details：https://vuex.vuejs.org/zh/guide/mutations.html

state => mapState => Details：https://vuex.vuejs.org/zh/guide/state.html

Specific action：https://vuex.vuejs.org/zh/
````

## 사양

## Vue 사양

##### 1.컴포넌트 이름

구성 요소는 여러 단어로 명명되었으며 HTML 태그와의 충돌을 방지하고 보다 명확한 구조를 위해 와이어(-)로 연결됩니다.```
// positive example
export default {
    name: 'page-article-item'
}
````

##### 2.컴포넌트 파일

`src/js/module/comComponents` 프로젝트의 내부 공통 구성 요소는 파일 이름과 동일한 이름으로 폴더 이름을 작성합니다.공통 구성 요소 내부에 분할된 하위 구성 요소와 유틸리티 도구는 구성 요소의 내부 `_source` 폴더에 배치됩니다.```
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
````

##### 3.발의안

Prop을 정의할 때 항상 카멜 형식(camelCase)으로 이름을 지정해야 하며 상위 컴포넌트에 값을 지정할 때 연결선(-)을 사용해야 합니다.
이는 HTML 태그에서 대소문자를 구분하지 않고 링크 사용이 더 친숙하기 때문에 각 언어의 특성을 따릅니다.JavaScript에서는 혹 이름이 더 자연스럽습니다.```
// Vue
props: {
    articleStatus: Boolean
}
// HTML
<article-item :article-status="true"></article-item>
````

Prop의 정의에서는 가능한 한 유형, 기본값 및 유효성 검사를 지정해야 합니다.

예:```
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
````

##### 4.v-for

순회를 위한 v-를 수행할 때 DOM을 업데이트할 때 렌더링을 보다 효율적으로 만들기 위해 항상 키 값을 가져와야 합니다.```
<ul>
    <li v-for="item in list" :key="item.id">
        {{ item.title }}
    </li>
</ul>
````

v-for는 v-if보다 우선순위가 높기 때문에 v-if와 동일한 요소(예: <li>`)에서는 v-for를 사용하지 않아야 합니다.잘못된 계산 및 렌더링을 방지하려면 v-if Put을 컨테이너의 상위 요소 위에 배치하는 방법을 사용해야 합니다.```
<ul v-if="showList">
    <li v-for="item in list" :key="item.id">
        {{ item.title }}
    </li>
</ul>
````

##### 5.v-if / v-else-if / v-else

동일한 v-if 논리 제어 세트의 요소가 논리적으로 동일한 경우 Vue는 '예: 값'과 같은 보다 효율적인 요소 전환을 위해 동일한 부분을 재사용합니다.다중화의 불합리한 효과를 피하기 위해서는 식별을 위해 동일한 요소에 키를 추가해야 합니다.```
<div v-if="hasData" key="mazey-data">
    <span>{{ mazeyData }}</span>
</div>
<div v-else key="mazey-none">
    <span>no data</span>
</div>
````

##### 6.명령어 약어

사양의 통일성을 위해 항상 명령어 약어를 사용합니다.`v-bind`를 사용하면 `v-on`도 나쁘지 않습니다.여기에는 통일된 사양만 있습니다.```
<input :value="mazeyUser" @click="verifyUser">
````

##### 7. 단일 파일 구성 요소의 최상위 요소 순서

스타일은 파일에 패키지되어 있으며 모든 스타일은 단일 vue 파일에 정의되어 있으며 다른 파일의 동일한 이름도 적용됩니다.구성 요소를 만들기 전에 모두 최상위 클래스 이름을 갖게 됩니다.
참고: 프로젝트에 sass 플러그인이 추가되었으며, sas 구문을 단일 vue 파일에 직접 작성할 수 있습니다.
통일성과 가독성을 위해 `<template>`、`<script>`、`<style>` 순으로 배치해야 합니다.```
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

````

## 자바스크립트 사양

##### 1.var / let / const

더 이상 var를 사용하지 않는 것이 좋지만 let/const를 사용하고 const를 선호합니다.function으로 정의된 함수는 어디에나 배치될 수 있다는 점을 제외하고 모든 변수의 사용은 미리 선언되어야 합니다.

##### 2.인용문```
const foo = 'after division'
const bar = `${foo}，ront-end engineer`
````

##### 3.기능

익명 함수는 화살표 함수를 균일하게 사용합니다.여러 매개변수/반환 값이 사용되는 경우 개체의 구조 할당이 먼저 사용됩니다.```
function getPersonInfo ({name, sex}) {
    // ...
    return {name, gender}
}
````

함수 이름은 낙타 이름을 사용하여 통일적으로 명명됩니다.대문자의 시작은 생성자입니다.소문자는 일반 함수로 시작하며, new 연산자를 일반 함수 연산에 사용하면 안 됩니다.

##### 4.물체```
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
````

##### 5.모듈

가져오기/내보내기를 사용하여 프로젝트 모듈을 통합 관리합니다.```
// lib.js
export default {}

// app.js
import app from './lib'
````

가져오기는 파일 상단에 배치됩니다.

모듈에 출력 값이 하나만 있는 경우 '기본값 내보내기'를 사용하고, 그렇지 않으면 아니요를 사용하세요.

## HTML / CSS

##### 1. 라벨

외부 CSS나 JavaScript를 참조할 때는 type 속성을 쓰지 마세요.HTML5 기본 유형은 text/css 및 text/javascript 속성이므로 지정할 필요가 없습니다.```
<link rel="stylesheet" href="//www.test.com/css/test.css">
<script src="//www.test.com/js/test.js"></script>
````

##### 2.네이밍

클래스 및 ID의 이름은 의미론적이어야 하며 이름을 보면 수행 중인 작업을 알 수 있습니다.여러 단어가 링크로 연결됩니다.```
// positive example
.test-header{
    font-size: 20px;
}
````

##### 3.속성 약어

CSS 속성은 코드의 효율성과 이해의 용이성을 높이기 위해 가능한 한 약어를 사용합니다.```
// counter example
border-width: 1px;
border-style: solid;
border-color: #ccc;

// positive example
border: 1px solid #ccc;
````

##### 4.문서 종류

항상 HTML5 표준을 사용해야 합니다.```
<!DOCTYPE html>
````

##### 5.주의사항

블록 주석은 모듈 파일에 기록되어야 합니다.```
/**
* @module mazey/api
* @author Mazey <mazey@mazey.net>
* @description test.
* */
````

## 인터페이스

##### 모든 인터페이스는 Promise로 반환됩니다.

캐치를 잡는 데 0이 아닌 것은 잘못된 것입니다.```
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
````

정상반환```
{
  code:0,
  data:{}
  msg:'success'
}
````

오류 반환```
{
  code:10000, 
  data:{}
  msg:'failed'
}
````

인터페이스가 게시 요청인 경우 Content-Type의 기본값은 application/x-www-form-urlencoded입니다.Content-Type이 application/json으로 변경된 경우
인터페이스 매개변수 전송을 다음과 같은 방법으로 변경해야 합니다.```
io.post('url', payload, null, null, { emulateJSON: false } res => {
  resolve(res)
}).catch(e => {
  reject(e)
})
````

##### 관련 인터페이스 경로

dag 관련 인터페이스`src/js/conf/home/store/dag/actions.js`

데이터 소스 센터 관련 인터페이스 `src/js/conf/home/store/datasource/actions.js`

프로젝트 관리 관련 인터페이스`src/js/conf/home/store/projects/actions.js`

리소스 센터 관련 인터페이스`src/js/conf/home/store/resource/actions.js`

보안 센터 관련 인터페이스`src/js/conf/home/store/security/actions.js`

사용자 센터 관련 인터페이스`src/js/conf/home/store/user/actions.js`

## 확장된 개발

##### 1.노드 추가

(1) 먼저 `src/js/conf/home/pages/dag/img` 폴더에 해당 노드의 아이콘 아이콘을 배치하고, 백그라운드에서 `toolbar_${로 정의된 노드의 영문 이름을 적어둡니다.예: SHELL}.png`

(2) `src/js/conf/home/pages/dag/_source/config.js`에서 `tasksType` 객체를 찾아 추가합니다.```
'DEPENDENT': {  //  The background definition node type English name is used as the key value
  desc: 'DEPENDENT',  // tooltip desc
  color: '#2FBFD8'  // The color represented is mainly used for tree and gantt
}
````

(3) `src/js/conf/home/pages/dag/_source/formModel/tasks`에 `${node type (소문자)}`.vue 파일을 추가합니다.현재 노드와 관련된 구성 요소의 내용이 여기에 기록됩니다.노드 구성 요소에 속해야 합니다. _verification() 함수가 있어야 합니다. 확인이 성공한 후 현재 구성 요소의 관련 데이터가 상위 구성 요소에 전달됩니다.```
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
````

(4) 노드 컴포넌트 내부에서 사용되는 공통 컴포넌트는 `_source`에 있으며, 공용 데이터 구성에는 `commcon.js`가 사용됩니다.

##### 2.상태 유형을 늘리세요.

(1) `src/js/conf/home/pages/dag/_source/config.js`에서 `tasksState` 객체를 찾아 추가합니다.```
 'WAITTING_DEPEND': {  // 'WAITTING_DEPEND': {  //Backend defines state type, frontend is used as key value
  id: 11,  // front-end definition id is used as a sort
  desc: `${i18n.$t('waiting for dependency')}`,  // tooltip desc
  color: '#5101be',  // The color represented is mainly used for tree and gantt
  icoUnicode: '&#xe68c;',  // font icon
  isSpin: false  // whether to rotate (requires code judgment)
}
````

##### 3.액션바 도구 추가

(1) `src/js/conf/home/pages/dag/_source/config.js`에서 `toolOper` 객체를 찾아 추가합니다.```
{
  code: 'pointer',  // tool identifier
  icon: '&#xe781;',  // tool icon
  disable: disable,  // disable
  desc: `${i18n.$t('Drag node and selected item')}`  // tooltip desc
}
````

(2) 도구 클래스는 `src/js/conf/home/pages/dag/_source/plugIn` 생성자로 반환됩니다.

`downChart.js` => dag 이미지 다운로드 처리

`dragZoom.js` => 마우스 확대/축소 효과 처리

`jsPlumbHandle.js` => 드래그 앤 드롭 라인 처리

`util.js` => `plugIn` 도구 클래스에 속함

작업은 `src/js/conf/home/pages/dag/_source/dag.js` => `toolbarEvent` 이벤트에서 처리됩니다.

##### 3. 라우팅 페이지 추가

(1) 먼저 경로 관리에 라우팅 주소 `src/js/conf/home/router/index.js`를 추가합니다.```
routing address{
  path: '/test',  // routing address
  name: 'test',  // alias
  component: resolve => require(['../pages/test/index'], resolve),  // route corresponding component entry file
  meta: {
    title: `${i18n.$t('test')} - EasyScheduler`  // title display
  }
},
````

(2) `src/js/conf/home/pages`에 `test` 폴더를 생성하고 해당 폴더에 `index.vue` 항목 파일을 생성합니다.

이렇게 하면`http://localhost:8888/#/test`에 직접 액세스할 수 있습니다.

##### 4. 사전 설정된 메일함을 늘립니다.

'src/lib/localData/email.js' 시작 및 시간 제한 이메일 주소 입력을 찾아 자동으로 일치 항목을 가져옵니다.```
export default ["test@analysys.com.cn","test1@analysys.com.cn","test3@analysys.com.cn"]
````

##### 5. 권한 관리 및 비활성화 상태 처리

이 권한은 backUser 인터페이스 `getUserInfo` 인터페이스에 따라 userType에 `"ADMIN_USER/GENERAL_USER" `페이지 작업 버튼의 비활성화 여부를 제어하는 권한을 부여합니다.

특정 작업:`src/js/module/permissions/index.js`

비활성화된 처리:`src/js/module/mixin/disabledState.js`