package org.apache.dolphinscheduler.api.test.pages.security.user;

import org.apache.dolphinscheduler.api.test.base.IPageAPI;
import org.apache.dolphinscheduler.api.test.entity.PageRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.security.user.entity.UserCreateRequestEntity;
import org.apache.dolphinscheduler.api.test.pages.security.user.entity.UserCreateResponseEntity;
import org.apache.dolphinscheduler.api.test.utils.RestResponse;
import org.apache.dolphinscheduler.api.test.utils.Result;

public interface IUserPageAPI extends IPageAPI {
    RestResponse<Result> getUsers(PageRequestEntity pageParamEntity);

    RestResponse<Result> createUser(UserCreateRequestEntity userCreateRequestEntity);

    UserCreateResponseEntity createUser();

    UserCreateResponseEntity createUserByUserEntity(UserCreateRequestEntity userCreateRequestEntity);

    UserCreateRequestEntity getUserEntityInstance();

    UserCreateRequestEntity getUserEntityInstance(String userName, String passWord, String email);
}
