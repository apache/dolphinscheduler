package org.apache.dolphinscheduler.api.security.impl.sso;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OidcUserInfo {
    private String username;
    private String email;
}
