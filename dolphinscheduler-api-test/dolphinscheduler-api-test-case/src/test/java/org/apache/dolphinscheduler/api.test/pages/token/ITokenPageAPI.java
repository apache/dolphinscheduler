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

package org.apache.dolphinscheduler.api.test.pages.token;

import org.apache.dolphinscheduler.api.test.base.IPageAPI;
import org.apache.dolphinscheduler.api.test.entity.PageRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.token.entity.TokenGenerateEntity;
import org.apache.dolphinscheduler.api.test.pages.token.entity.TokenRequestEntity;
import org.apache.dolphinscheduler.api.test.utils.RestResponse;
import org.apache.dolphinscheduler.api.test.utils.Result;

public interface ITokenPageAPI extends IPageAPI {
    RestResponse<Result> generateToken(TokenGenerateEntity tokenGenerateEntity);

    RestResponse<Result> createToken(TokenRequestEntity tokenRequestEntity);

    RestResponse<Result> updateToken(TokenRequestEntity tokenRequestEntity, int id);

    RestResponse<Result> queryTokenByUser(int userId);

    RestResponse<Result> queryTokenList(PageRequestEntity pageParamEntity);
}
