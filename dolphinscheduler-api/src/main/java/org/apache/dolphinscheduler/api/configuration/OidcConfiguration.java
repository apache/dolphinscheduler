package org.apache.dolphinscheduler.api.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Configuration
@ConditionalOnProperty(prefix = "security.authentication.oidc", name = "enable", havingValue = "true")
@ConfigurationProperties(prefix = "security.authentication.oidc")
public class OidcConfiguration {

    private Map<String, OidcProviderProperties> provider = new HashMap<>();

    @Getter
    @Setter
    public static class OidcProviderProperties {
        private String issuerUri;
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String callbackUrl;
        private String iconUri;
        private String provider;
    }
}
