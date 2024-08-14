package org.apache.dolphinscheduler.oauth;

import lombok.Data;

@Data
public class OAuthUserInfo {

    private String username;

    private String name;

    private String email;
}
