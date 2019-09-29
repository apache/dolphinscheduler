## Ans-UI

component x base on vue.js

### Install
安装node > 8的LTS版本，https://nodejs.org/en/

```sh
yarn add @analysys/ans-ui | npm i @analysys/ans-ui
```

### Usage

全部引入

```javascript
import Vue from 'vue';
import '@analysys/ans-ui/lib/ans-ui.min.css';
import ans from '@analysys/ans-ui/lib/ans-ui.min.js';

Vue.use(ans);
```

按需引入

```javascript
import Vue from 'vue';
import '@analysys/ans-ui/lib/ans-ui.min.css';
import { xButton } from '@analysys/ans-ui/lib/ans-ui.min.js';

Vue.use(xButton);
```

### Build
```sh
yarn global add parcel-bundler | npm i -g parcel-bundler
# development default listen to 4000
yarn dev | npm run dev

# production
yarn build | npm run build
```

### Build Single Component
```sh
yarn global add parcel-bundler | npm i -g parcel-bundler
# development button 可以替换为任意组件名
yarn dev:c button | npm run dev:c button

# production button 可以替换为任意组件名
yarn build:c button | npm run build:c button

#or
yarn dev:c | npm run dev:c
```
