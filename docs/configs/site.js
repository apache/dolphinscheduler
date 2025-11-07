/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

// 全局的一些配置
export default {
  rootPath: '',
  port: 8080,
  domain: 'dolphinscheduler.apache.org',
  copyToDist: ['asset', 'img', 'file', '.asf.yaml', 'sitemap.xml', '.nojekyll', '.htaccess', 'googled0df7b96f277a143.html'],
  docsLatest: '3.3.2',
  defaultSearch: 'google', // default search engine
  defaultLanguage: 'en-us',
  'en-us': {
    banner: {
      text: '🤔 Have queries regarding Apache DolphinScheduler, Join Slack channel to discuss them ',
      link: 'https://s.apache.org/dolphinscheduler-slack'
    },
    pageMenu: [
      {
        key: 'home',
        text: 'HOME',
        link: '/en-us/index.html',
      },
      {
        key: 'docs',
        text: 'DOCS',
        link: '/en-us/docs/latest/user_doc/about/introduction.html',
        children: [
          {
            key: 'docs0',
            text: 'latest(3.3.2)',
            link: '/en-us/docs/latest/user_doc/about/introduction.html',
          },
          {
            key: 'docs1',
            text: '3.1.9',
            link: '/en-us/docs/3.1.9/user_doc/about/introduction.html',
          },
          {
            key: 'docs2',
            text: '2.0.7',
            link: '/en-us/docs/2.0.7/user_doc/guide/quick-start.html',
          },
          {
            key: 'docsHistory',
            text: 'Older Versions',
            link: '/en-us/docs/release/history-versions.html',
          }
        ],
      },
      {
        key: 'download',
        text: 'DOWNLOAD',
        link: '/en-us/download/download.html',
      },
      { key: 'blog',
        text: 'BLOG',
        link: '/en-us/blog/index.html',
      },
      {
        key: 'community',
        text: 'COMMUNITY',
        link: '/en-us/community/community.html',
      },
      {
        key: 'ASF',
        text: 'ASF',
        target: '_blank',
        link: 'https://www.apache.org/',
        children: [
          {
            key: 'Foundation',
            text: 'Foundation',
            target: '_blank',
            link: 'https://www.apache.org/',
          },
          {
            key: 'License',
            text: 'License',
            target: '_blank',
            link: 'https://www.apache.org/licenses/',
          },
          {
            key: 'Events',
            text: 'Events',
            target: '_blank',
            link: 'https://www.apache.org/events/current-event',
          },
          {
            key: 'Security',
            text: 'Security',
            target: '_blank',
            link: 'https://www.apache.org/security/',
          },
          {
            key: 'Sponsorship',
            text: 'Sponsorship',
            target: '_blank',
            link: 'https://www.apache.org/foundation/sponsorship.html',
          },
          {
            key: 'Thanks',
            text: 'Thanks',
            target: '_blank',
            link: 'https://www.apache.org/foundation/thanks.html',
          },
        ],
      },
      {
        key: 'user',
        text: 'USER',
        link: '/en-us/user/index.html',
      },
    ],
    contact: {
      title: 'About us',
      content: 'Do you need feedback? Please contact us through the following ways.',
      list: [
        {
          name: 'Slack',
          img1: '/img/slack.png',
          img2: '/img/slack-selected.png',
          link: 'https://s.apache.org/dolphinscheduler-slack',
        },
        {
          name: 'Email List',
          img1: '/img/emailgray.png',
          img2: '/img/emailblue.png',
          link: '/en-us/docs/latest/user_doc/contribute/join/subscribe.html',
        },
        {
          name: 'Twitter',
          img1: '/img/twittergray.png',
          img2: '/img/twitterblue.png',
          link: 'https://twitter.com/dolphinschedule',
        },
      ],
    },
    copyright: 'Copyright © 2019-2022 The Apache Software Foundation. Apache DolphinScheduler, DolphinScheduler, and its feather logo are trademarks of The Apache Software Foundation.',
  },
  'zh-cn': {
    banner: {
      text: '🤔 有关于 Apache DolphinScheduler 的疑问，加入 Slack 频道来讨论他们 ',
      link: 'https://s.apache.org/dolphinscheduler-slack'
    },
    pageMenu: [
      {
        key: 'home',
        text: '首页',
        link: '/zh-cn/index.html',
      },
      {
        key: 'docs',
        text: '文档',
        link: '/zh-cn/docs/latest/user_doc/about/introduction.html',
        children: [
          {
            key: 'docs0',
            text: '最新版本latest(3.3.2)',
            link: '/zh-cn/docs/latest/user_doc/about/introduction.html',
          },
          {
            key: 'docs1',
            text: '3.1.9',
            link: '/zh-cn/docs/3.1.9/user_doc/about/introduction.html',
          },
          {
            key: 'docs2',
            text: '2.0.7',
            link: '/zh-cn/docs/2.0.7/user_doc/guide/quick-start.html',
          },
          {
            key: 'docsHistory',
            text: '历史版本',
            link: '/zh-cn/docs/release/history-versions.html',
          }
        ],
      },
      {
        key: 'download',
        text: '下载',
        link: '/zh-cn/download/download.html',
      },
      {
        key: 'blog',
        text: '博客',
        link: '/zh-cn/blog/index.html',
      },
      {
        key: 'community',
        text: '社区',
        link: '/zh-cn/community/community.html',
      },
      {
        key: 'ASF',
        text: 'ASF',
        target: '_blank',
        link: 'https://www.apache.org/',
        children: [
          {
            key: 'Foundation',
            text: 'Foundation',
            target: '_blank',
            link: 'https://www.apache.org/',
          },
          {
            key: 'License',
            text: 'License',
            target: '_blank',
            link: 'https://www.apache.org/licenses/',
          },
          {
            key: 'Events',
            text: 'Events',
            target: '_blank',
            link: 'https://www.apache.org/events/current-event',
          },
          {
            key: 'Security',
            text: 'Security',
            target: '_blank',
            link: 'https://www.apache.org/security/',
          },
          {
            key: 'Sponsorship',
            text: 'Sponsorship',
            target: '_blank',
            link: 'https://www.apache.org/foundation/sponsorship.html',
          },
          {
            key: 'Thanks',
            text: 'Thanks',
            target: '_blank',
            link: 'https://www.apache.org/foundation/thanks.html',
          },
        ],
      },
      {
        key: 'user',
        text: '用户',
        // link: '',
        link: '/zh-cn/user/index.html',
      },
    ],
    contact: {
      title: '联系我们',
      content: '有问题需要反馈？请通过以下方式联系我们。',
      list: [
        {
          name: 'Slack',
          img1: '/img/slack.png',
          img2: '/img/slack-selected.png',
          link: 'https://s.apache.org/dolphinscheduler-slack',
        },
        {
          name: '邮件列表',
          img1: '/img/emailgray.png',
          img2: '/img/emailblue.png',
          link: '/zh-cn/docs/latest/user_doc/contribute/join/subscribe.html',
        },
        {
          name: 'Twitter',
          img1: '/img/twittergray.png',
          img2: '/img/twitterblue.png',
          link: 'https://twitter.com/dolphinschedule',
        },
      ],
    },
    copyright: 'Copyright © 2019-2022 The Apache Software Foundation. Apache DolphinScheduler, DolphinScheduler, and its feather logo are trademarks of The Apache Software Foundation.',
  },
};
