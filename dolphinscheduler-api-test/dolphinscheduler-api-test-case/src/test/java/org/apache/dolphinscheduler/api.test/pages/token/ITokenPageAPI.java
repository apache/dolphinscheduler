package org.apache.dolphinscheduler.api.test.pages.token;

import org.apache.dolphinscheduler.api.test.base.IPageAPI;
import org.apache.dolphinscheduler.api.test.entity.PageRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.token.entity.TokenRequestEntity;
import org.apache.dolphinscheduler.api.test.utils.RestResponse;
import org.apache.dolphinscheduler.api.test.utils.Result;

public interface ITokenPageAPI extends IPageAPI {
    RestResponse<Result> createToken(TokenRequestEntity tokenRequestEntity);

    RestResponse<Result> updateToken(TokenRequestEntity tokenRequestEntity, int id);

    RestResponse<Result> queryTokenByUser(int userId);

    RestResponse<Result> queryTokenList(PageRequestEntity pageParamEntity);
}
