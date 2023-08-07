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

import React from 'react';
import ReactDOM from 'react-dom';
import cookie from 'js-cookie';
import Language from '../../components/language';
import Header from '../../components/header';
import Footer from '../../components/footer';
import Md2Html from '../../components/md2html';
import Sidemenu from '../../components/sidemenu';
import siteConfig from '../../../site_config/site';
import docs120Config from '../../../site_config/docs1-2-0';
import docs121Config from '../../../site_config/docs1-2-1';
import docs131Config from '../../../site_config/docs1-3-1';
import docs132Config from '../../../site_config/docs1-3-2';
import docs133Config from '../../../site_config/docs1-3-3';
import docs134Config from '../../../site_config/docs1-3-4';
import docs135Config from '../../../site_config/docs1-3-5';
import docs136Config from '../../../site_config/docs1-3-6';
import docs138Config from '../../../site_config/docs1-3-8';
import docs139Config from '../../../site_config/docs1-3-9';
import docs200Config from '../../../site_config/docs2-0-0';
import docs201Config from '../../../site_config/docs2-0-1';
import docs202Config from '../../../site_config/docs2-0-2';
import docs203Config from '../../../site_config/docs2-0-3';
import docs205Config from '../../../site_config/docs2-0-5';
import docs206Config from '../../../site_config/docs2-0-6';
import docs207Config from '../../../site_config/docs2-0-7';
import docs300Config from '../../../site_config/docs3-0-0';
import docs301Config from '../../../site_config/docs3-0-1';
import docs302Config from '../../../site_config/docs3-0-2';
import docs303Config from '../../../site_config/docs3-0-3';
import docs304Config from '../../../site_config/docs3-0-4';
import docs305Config from '../../../site_config/docs3-0-5';
import docs306Config from '../../../site_config/docs3-0-6';
import docs310Config from '../../../site_config/docs3-1-0';
import docs311Config from '../../../site_config/docs3-1-1';
import docs312Config from '../../../site_config/docs3-1-2';
import docs313Config from '../../../site_config/docs3-1-3';
import docs314Config from '../../../site_config/docs3-1-4';
import docs315Config from '../../../site_config/docs3-1-5';
import docs316Config from '../../../site_config/docs3-1-6';
import docs317Config from '../../../site_config/docs3-1-7';
import docs318Config from '../../../site_config/docs3-1-8';
import docsDevConfig from '../../../site_config/docsdev';

const docsSource = {
  '1.2.0': docs120Config,
  '1.2.1': docs121Config,
  '1.3.1': docs131Config,
  '1.3.2': docs132Config,
  '1.3.3': docs133Config,
  '1.3.4': docs134Config,
  '1.3.5': docs135Config,
  '1.3.6': docs136Config,
  '1.3.8': docs138Config,
  '1.3.9': docs139Config,
  '2.0.0': docs200Config,
  '2.0.1': docs201Config,
  '2.0.2': docs202Config,
  '2.0.3': docs203Config,
  '2.0.5': docs205Config,
  '2.0.6': docs206Config,
  '2.0.7': docs207Config,
  '3.0.0': docs300Config,
  '3.0.1': docs301Config,
  '3.0.2': docs302Config,
  '3.0.3': docs303Config,
  '3.0.4': docs304Config,
  '3.0.5': docs305Config,
  '3.0.6': docs306Config,
  '3.1.0': docs310Config,
  '3.1.1': docs311Config,
  '3.1.2': docs312Config,
  '3.1.3': docs313Config,
  '3.1.4': docs314Config,
  '3.1.5': docs315Config,
  '3.1.6': docs316Config,
  '3.1.7': docs317Config,
  '3.1.8': docs318Config,
  dev: docsDevConfig,
};

const isValidVersion = version => version && docsSource.hasOwnProperty(version);

class Docs extends Md2Html(Language) {
  render() {
    const language = this.getLanguage();
    let dataSource = {};
    // from location path
    let version = window.location.pathname.split('/')[3];
    if (isValidVersion(version) || version === 'latest') {
      cookie.set('docs_version', version);
    }
    // from rendering html
    if (!version && this.props.subdir) {
      version = this.props.subdir.split('/')[0];
    }
    if (isValidVersion(version)) {
      dataSource = docsSource[version][language];
    } else if (isValidVersion(cookie.get('docs_version'))) {
      dataSource = docsSource[cookie.get('docs_version')][language];
    } else if (isValidVersion(siteConfig.docsLatest)) {
      dataSource = docsSource[siteConfig.docsLatest][language];
      dataSource.sidemenu.forEach((menu) => {
        menu.children.forEach((submenu) => {
          if (!submenu.children) {
            submenu.link = submenu.link.replace(`docs/${siteConfig.docsLatest}`, 'docs/latest');
          } else {
            submenu.children.forEach((menuLevel3) => {
              menuLevel3.link = menuLevel3.link.replace(`docs/${siteConfig.docsLatest}`, 'docs/latest');
            });
          }
        });
      });
    } else {
      return null;
    }
    const __html = this.props.__html || this.state.__html;
    return (
      <div className="md2html docs-page">
        <Header
          currentKey="docs"
          type="dark"
          logo="/img/hlogo_white.svg"
          language={language}
          onLanguageChange={this.onLanguageChange}
        />
        <section className="content-section">
          <Sidemenu dataSource={dataSource.sidemenu} />
          <div
            className="doc-content markdown-body"
            ref={(node) => { this.markdownContainer = node; }}
            dangerouslySetInnerHTML={{ __html }}
          />
        </section>
        <Footer logo="/img/ds_gray.svg" language={language} />
      </div>
    );
  }
}

document.getElementById('root') && ReactDOM.render(<Docs />, document.getElementById('root'));

export default Docs;
