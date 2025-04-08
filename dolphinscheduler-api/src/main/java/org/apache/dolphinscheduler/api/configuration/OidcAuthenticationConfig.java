package org.apache.dolphinscheduler.api.configuration;

import org.apache.dolphinscheduler.api.security.impl.sso.GenericOidcAuthenticator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OidcAuthenticationConfig {

    @Bean
    public GenericOidcAuthenticator genericOidcAuthenticator() {
        return new GenericOidcAuthenticator();
    }
}
