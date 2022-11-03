package org.apache.dolphinscheduler.api.security.plugins.oauth2;

import org.apache.dolphinscheduler.api.security.AbstractLoginCredentials;

import lombok.Data;

import org.springframework.security.oauth2.core.user.OAuth2User;

@Data
public class OAuth2LoginCredentials extends AbstractLoginCredentials {

    public OAuth2User principal;

}
